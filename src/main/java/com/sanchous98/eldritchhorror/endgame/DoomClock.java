package com.sanchous98.eldritchhorror.endgame;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.core.ModConfig;
import com.sanchous98.eldritchhorror.corruption.CorruptionSystem;
import com.sanchous98.eldritchhorror.entity.AncientOne;
import com.sanchous98.eldritchhorror.entity.BestiarySupport;
import com.sanchous98.eldritchhorror.event.Rifts;
import com.sanchous98.eldritchhorror.registry.ModEntities;
import com.sanchous98.eldritchhorror.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.jspecify.annotations.Nullable;

/**
 * The doom clock ({@code design/11-endgame.md}, "The doom clock", pinned 2026-11): a per-overworld
 * {@code 0..100} meter that ends the world without a player choosing the Hollow Choir path, and
 * gives a session a legible natural length.
 *
 * <p>It rises passively while at least one player is in the overworld ({@link DoomMath#risePerTick}
 * scaled by open rifts), is pushed by completed NPC rituals ({@code +doomRitualPush}) and pushed back
 * by interrupting one ({@code -doomInterruptRelief}) and by closing a rift
 * ({@code -doomRiftRelief}). At {@code doomAwakenAt} (70% by default) the Choir awakens the Horror at
 * the deepest loaded rift — with a global warning — and at 100% the world resolves to the summon
 * ending through {@link Endgame#summon}. The normal banish/seal rites still resolve an awakened world,
 * so the window between 70 and 100 is real counter-play.
 *
 * <p>Persistence and bounds: one {@link SavedData} per overworld, ticking every 20 game ticks,
 * stopping forever once {@link EndgameState#outcome()} is anything but {@link
 * EndgameState.Outcome#NONE}. {@code lastGameTime} is deliberately <b>not</b> persisted: on load it
 * is set to the current game time, so offline time never accumulates. No chunk is force-loaded — the
 * rift scan and the fallback placement only read loaded chunks.
 *
 * <p>The 100% resolution calls {@link Endgame#summon}, which pins the corruption floor on the
 * <b>nearest player</b> only: the world is given over, but the permanent Claimed floor is a
 * per-player fact and does not silently rewrite every other player's meter.
 */
@EventBusSubscriber(modid = EldritchHorror.MODID)
public final class DoomClock extends SavedData {

    /** The clock ticks once a second, as design/11 specifies. */
    private static final int TICK_INTERVAL = 20;
    /** Guard against a stalled server dumping minutes of doom into one pass. */
    private static final long MAX_ELAPSED_TICKS = 200L;
    /** Radius of the "already awake" check before the 70% awakening spawns another Horror. */
    private static final int AWAKEN_NEAR_RADIUS = 128;
    /** Search radius around a player for the fallback awakening spot. */
    private static final int FALLBACK_RADIUS = 8;

    private static final Codec<DoomClock> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.DOUBLE.optionalFieldOf("doom", 0.0).forGetter(DoomClock::doom),
            Codec.INT.optionalFieldOf("warnings", 0).forGetter(DoomClock::warningMask),
            Codec.BOOL.optionalFieldOf("awakened", false).forGetter(DoomClock::awakened)
    ).apply(instance, DoomClock::new));

    private static final SavedDataType<DoomClock> TYPE =
            new SavedDataType<>(EldritchHorror.id("doom_clock"), DoomClock::new, CODEC);

    private double doom;
    private int warningMask;
    private boolean awakened;
    /** Last game time counted; {@code -1} until the first tick, and never serialised. */
    private long lastGameTime = -1L;

    private DoomClock() {
    }

    private DoomClock(double doom, int warningMask, boolean awakened) {
        this.doom = DoomMath.clamp(doom);
        this.warningMask = warningMask;
        this.awakened = awakened;
    }

    /** @return the overworld's clock (created on first read); safe to call from any dimension. */
    public static DoomClock of(ServerLevel level) {
        MinecraftServer server = level.getServer();
        ServerLevel overworld = server.overworld();
        return overworld.getDataStorage().computeIfAbsent(TYPE);
    }

    /** @return the current doom, {@code 0..100}. */
    public double doom() {
        return this.doom;
    }

    /** @return the persisted one-shot warning mask. */
    private int warningMask() {
        return this.warningMask;
    }

    /** @return whether the 70% awakening has already fired. */
    public boolean awakened() {
        return this.awakened;
    }

    /**
     * Pushes the meter by {@code delta} (clamped). A no-op when the clock is disabled or the world's
     * decision has already been made — the clock stops, it does not rewind.
     */
    public static void addDoom(ServerLevel level, double delta) {
        if (!ModConfig.ENABLE_DOOM_CLOCK.get()) {
            return;
        }
        ServerLevel overworld = level.getServer().overworld();
        if (EndgameState.of(overworld).outcome() != EndgameState.Outcome.NONE) {
            return;
        }
        DoomClock clock = of(overworld);
        double next = DoomMath.add(clock.doom, delta);
        if (next != clock.doom) {
            clock.doom = next;
            clock.setDirty();
        }
    }

    /** Removes {@code delta} from the meter (the ritual-interrupt and rift-close relief hooks). */
    public static void relieve(ServerLevel level, double delta) {
        addDoom(level, -delta);
    }

    /** @return the passive rise per minute at the current config and rift count (for {@code /eh doom}). */
    public static double ratePerMinute(ServerLevel level) {
        if (!ModConfig.ENABLE_DOOM_CLOCK.get()
                || EndgameState.of(level.getServer().overworld()).outcome()
                        != EndgameState.Outcome.NONE) {
            return 0.0; // the meter is stopped, so the advertised rate is zero
        }
        int rifts = RiftLedger.of(level).openCount();
        return DoomMath.risePerTick(ModConfig.DOOM_MINUTES_TO_LOSS.get(), rifts,
                ModConfig.DOOM_RIFT_ACCELERATION.get(), DoomMath.MAX_RIFT_MULTIPLIER) * 60.0 * 20.0;
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (!ModConfig.ENABLE_DOOM_CLOCK.get()) {
            return;
        }
        if (event.getServer().getTickCount() % TICK_INTERVAL != 0) {
            return;
        }
        MinecraftServer server = event.getServer();
        ServerLevel overworld = server.getLevel(Level.OVERWORLD);
        if (overworld == null || EndgameState.of(overworld).outcome() != EndgameState.Outcome.NONE) {
            return;
        }
        DoomClock clock = of(overworld);
        long now = overworld.getGameTime();
        if (clock.lastGameTime < 0L) {
            clock.lastGameTime = now; // freshly loaded: offline time never accumulates
            return;
        }
        if (overworld.players().isEmpty()) {
            clock.lastGameTime = now; // nobody is present: the meter does not run
            return;
        }
        long elapsed = Math.clamp(now - clock.lastGameTime, 0L, MAX_ELAPSED_TICKS);
        clock.lastGameTime = now;
        int rifts = RiftLedger.of(overworld).openCount();
        double rise = DoomMath.risePerTick(ModConfig.DOOM_MINUTES_TO_LOSS.get(), rifts,
                ModConfig.DOOM_RIFT_ACCELERATION.get(), DoomMath.MAX_RIFT_MULTIPLIER) * elapsed;
        if (rise > 0.0) {
            clock.doom = DoomMath.clamp(clock.doom + rise);
            clock.setDirty();
        }
        checkThresholds(overworld, clock);
    }

    /** Emits the once-only warnings, the 70% awakening and the 100% loss. */
    private static void checkThresholds(ServerLevel level, DoomClock clock) {
        int warning = DoomMath.nextWarning(clock.doom, clock.warningMask);
        while (warning != 0) {
            clock.warningMask = DoomMath.markWarning(clock.warningMask, warning);
            clock.setDirty();
            announceWarning(level, warning, clock.doom);
            warning = DoomMath.nextWarning(clock.doom, clock.warningMask);
        }
        if (!clock.awakened && DoomMath.awakened(clock.doom, ModConfig.DOOM_AWAKEN_AT.get())) {
            // With the ritual awakening disabled the threshold is a one-shot marker; otherwise it
            // latches only once the presence is actually on the ground, so a failed placement is
            // retried on the next pass instead of silently swallowing the 70% event.
            if (!ModConfig.NPC_CULT_RITUALS_CAN_AWAKEN.get() || awaken(level)) {
                clock.awakened = true;
                clock.setDirty();
            }
        }
        if (DoomMath.lost(clock.doom)) {
            lose(level);
        }
    }

    /** Chat plus the ambient whisper at every player in the overworld. */
    private static void announceWarning(ServerLevel level, int threshold, double doom) {
        Component message = Component.translatable("doom.eldritch_horror.warning." + threshold);
        for (ServerPlayer player : level.players()) {
            player.sendSystemMessage(message);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    ModSounds.AMBIENT_WHISPER.get(), SoundSource.AMBIENT, 0.7F, 0.8F);
        }
        EldritchHorror.LOGGER.info("doom warning {} (doom {})", threshold,
                String.format(java.util.Locale.ROOT, "%.1f", doom));
    }

    /**
     * The 70% event: awaken the Horror at the deepest loaded rift, or on open ground near the
     * most-corrupted player when no loaded rift can be found. Plays the awakening sound and warns
     * every player. If it cannot place the presence this pass, nothing is announced and the next
     * tick is free to try again.
     *
     * @return {@code true} when a presence was placed or one is already nearby, {@code false} when
     *         this pass could not place one (the caller must then leave the awakening unlatched)
     */
    private static boolean awaken(ServerLevel level) {
        ServerPlayer anchor = mostCorrupted(level);
        if (anchor == null) {
            return false;
        }
        if (Endgame.horrorNear(level, anchor, AWAKEN_NEAR_RADIUS)) {
            return true;
        }
        BlockPos spot = deepestRiftSpot(level);
        if (spot == null) {
            spot = openGroundNear(level, anchor);
        }
        if (spot == null) {
            return false;
        }
        AncientOne horror = ModEntities.THE_HORROR.get().spawn(level, spot, EntitySpawnReason.EVENT);
        if (horror == null) {
            return false;
        }
        level.playSound(null, horror.getX(), horror.getY(), horror.getZ(),
                ModSounds.ENDGAME_AWAKEN.get(), SoundSource.AMBIENT, 1.0F, 1.0F);
        level.getServer().getPlayerList().broadcastSystemMessage(
                Component.translatable("doom.eldritch_horror.awaken"), false);
        EldritchHorror.LOGGER.info("doom awakening: the Horror rose at {} {} {}",
                spot.getX(), spot.getY(), spot.getZ());
        return true;
    }

    /**
     * The 100% event: the Choir wins. Finds or spawns the Horror and hands the world to {@link
     * Endgame#summon}; that call records {@link EndgameState.Outcome#SUMMONED}, so the clock stops on
     * the next tick. The nearest player is the one whose corruption floor is pinned (see the class
     * javadoc).
     */
    private static void lose(ServerLevel level) {
        ServerPlayer player = mostCorrupted(level);
        if (player == null) {
            return;
        }
        AncientOne horror = Endgame.nearestHorror(level, player, AWAKEN_NEAR_RADIUS);
        if (horror == null) {
            BlockPos spot = deepestRiftSpot(level);
            if (spot == null) {
                spot = openGroundNear(level, player);
            }
            if (spot == null) {
                return;
            }
            horror = ModEntities.THE_HORROR.get().spawn(level, spot, EntitySpawnReason.EVENT);
        }
        if (horror == null) {
            return;
        }
        Endgame.summon(level, player, horror);
        level.getServer().getPlayerList().broadcastSystemMessage(
                Component.translatable("doom.eldritch_horror.loss"), false);
        EldritchHorror.LOGGER.info("doom loss: the world resolved to the summon ending");
    }

    /** @return the overworld player with the highest corruption, or {@code null} if none is online. */
    private static @Nullable ServerPlayer mostCorrupted(ServerLevel level) {
        ServerPlayer best = null;
        double highest = -1.0;
        for (ServerPlayer player : level.players()) {
            double value = CorruptionSystem.get(player);
            if (value > highest) {
                highest = value;
                best = player;
            }
        }
        return best;
    }

    /** @return the loaded rift anchor with the lowest Y near any player, or {@code null}. */
    private static @Nullable BlockPos deepestRiftSpot(ServerLevel level) {
        BlockPos best = null;
        for (ServerPlayer player : level.players()) {
            for (BlockPos rift : Rifts.near(level, player.blockPosition(), 4, 64)) {
                if (best == null || rift.getY() < best.getY()) {
                    best = rift;
                }
            }
        }
        if (best == null) {
            return null;
        }
        BlockPos above = best.above();
        return level.isLoaded(above) ? above : null;
    }

    /** A loaded open surface spot near {@code player}; never force-loads a chunk. */
    private static @Nullable BlockPos openGroundNear(ServerLevel level, ServerPlayer player) {
        BlockPos centre = player.blockPosition();
        for (int attempt = 0; attempt < 12; attempt++) {
            int angle = attempt * 30;
            int x = centre.getX() + (int) Math.round(Math.cos(Math.toRadians(angle)) * FALLBACK_RADIUS);
            int z = centre.getZ() + (int) Math.round(Math.sin(Math.toRadians(angle)) * FALLBACK_RADIUS);
            BlockPos spot = BestiarySupport.surfaceSpot(level, x, z, false);
            if (spot != null) {
                return spot;
            }
        }
        return null;
    }
}
