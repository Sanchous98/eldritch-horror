package com.sanchous98.eldritchhorror.sanity;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.core.ModConfig;
import com.sanchous98.eldritchhorror.entity.BestiarySupport;
import com.sanchous98.eldritchhorror.registry.ModSounds;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Prediction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.jspecify.annotations.Nullable;

/**
 * Madness <b>episodes</b>: what a worsening sanity band actually does, beyond the stat effects.
 *
 * <ul>
 *   <li><b>Hallucination</b> — one or two phantom mobs appear around the player. The type is drawn
 *       from this biome's own monster spawn list, so it looks exactly like something that could
 *       really be there. A phantom chases and its hits are converted: no health damage, a sanity
 *       cost instead. It has real health and must be fought (arrows, durability), but gives no
 *       drops and no XP; killing it earns a small "it was never there" relief. It fades on its own
 *       after the configured lifetime. Nearby lucid players get a warning message — the phantom is
 *       <b>not</b> suppressed or revealed to the afflicted; a companion can only tell them.</li>
 *   <li><b>Fugue</b> — blindness and nausea, and a short involuntary displacement (8–16 blocks).</li>
 *   <li><b>Compulsion</b> — the main-hand stack is dropped; weakness/mining fatigue follow.</li>
 * </ul>
 *
 * <p>Episodes fire on a downward band transition (design/27: effects on the transition, not every
 * tick), with a per-player cooldown; at {@code MARKED} hallucinations recur. Nothing is persisted
 * across restarts: phantoms are ordinary entities tagged in persistent data, tracked here for
 * expiry, caps and cleanup.
 */
@EventBusSubscriber(modid = EldritchHorror.MODID)
public final class MadnessEpisodes {

    private static final String ILLUSION_TAG = "eh_illusion";
    private static final int TICK_INTERVAL = 20;
    private static final int HIT_COOLDOWN_TICKS = 40;
    private static final int MAX_ILLUSIONS_PER_PLAYER = 2;

    private record Illusion(UUID owner, long until) {
    }

    private static final Map<UUID, Long> LAST_EPISODE = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> LAST_HIT = new ConcurrentHashMap<>();
    private static final Map<UUID, Illusion> ILLUSIONS = new ConcurrentHashMap<>();

    private MadnessEpisodes() {
    }

    /** Called by {@link SanityTicker} when the player's band just got worse. */
    public static void onBandWorsened(ServerPlayer player, SanityState state) {
        if (!ModConfig.ENABLE_MADNESS_EPISODES.get()) {
            return;
        }
        long now = player.level().getGameTime();
        Long last = LAST_EPISODE.get(player.getUUID());
        if (last != null && now - last < ModConfig.MADNESS_EPISODE_COOLDOWN.get()) {
            return;
        }
        LAST_EPISODE.put(player.getUUID(), now);
        switch (state) {
            case MARKED -> hallucinate(player, 2);
            case BREAKING -> {
                if (player.getRandom().nextBoolean()) {
                    hallucinate(player, 1);
                } else {
                    compulsion(player);
                }
            }
            case FRAYING -> {
                if (player.getRandom().nextBoolean()) {
                    compulsion(player);
                } else {
                    fugue(player);
                }
            }
            default -> {
            }
        }
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        if (server.getTickCount() % TICK_INTERVAL != 0) {
            return;
        }
        long now = server.overworld().getGameTime();
        expire(server, now);
        if (!ModConfig.ENABLE_MADNESS_EPISODES.get()) {
            return;
        }
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (!player.level().dimension().equals(Level.OVERWORLD)
                    || SanitySystem.state(player) != SanityState.MARKED) {
                continue;
            }
            Long last = LAST_EPISODE.get(player.getUUID());
            if (last == null || now - last >= ModConfig.MADNESS_MARKED_INTERVAL.get()) {
                LAST_EPISODE.put(player.getUUID(), now);
                hallucinate(player, 1);
            }
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        UUID id = event.getEntity().getUUID();
        LAST_EPISODE.remove(id);
        LAST_HIT.remove(id);
    }

    /**
     * Phantoms deal no health damage; their hits are sanity. Environmental damage is ignored so a
     * phantom never burns or falls to death and spoils the illusion. Player damage is allowed, so a
     * phantom can be fought and killed.
     */
    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        LivingEntity victim = event.getEntity();
        if (isIllusion(victim)) {
            if (!(event.getSource().getEntity() instanceof ServerPlayer)) {
                event.setCanceled(true);
            }
            return;
        }
        Entity attacker = event.getSource().getEntity();
        if (attacker != null && isIllusion(attacker) && victim instanceof ServerPlayer player) {
            event.setCanceled(true);
            long now = player.level().getGameTime();
            Long last = LAST_HIT.get(attacker.getUUID());
            if (last == null || now - last >= HIT_COOLDOWN_TICKS) {
                LAST_HIT.put(attacker.getUUID(), now);
                SanityAPI.add(player, -ModConfig.MADNESS_ILLUSION_SANITY_COST.get());
                player.sendSystemMessage(Component.translatable("madness.eldritch_horror.touched"));
            }
        }
    }

    /** A phantom "dies" like a real mob, but leaves nothing behind. */
    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (!isIllusion(event.getEntity())) {
            return;
        }
        ServerPlayer killer = event.getSource().getEntity() instanceof ServerPlayer player
                ? player
                : null;
        pop(event.getEntity(), killer);
    }

    @SubscribeEvent
    public static void onDrops(LivingDropsEvent event) {
        if (isIllusion(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onExperience(LivingExperienceDropEvent event) {
        if (isIllusion(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    /** Spawns phantoms around the player and warns nearby lucid players. */
    private static void hallucinate(ServerPlayer player, int count) {
        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }
        long now = level.getGameTime();
        long mine = ILLUSIONS.values().stream()
                .filter(illusion -> illusion.owner().equals(player.getUUID()))
                .count();
        int budget = (int) Math.max(0, MAX_ILLUSIONS_PER_PLAYER - mine);
        if (budget <= 0) {
            return;
        }
        int spawned = 0;
        for (int i = 0; i < count && spawned < budget; i++) {
            if (spawnPhantom(level, player, now)) {
                spawned++;
            }
        }
        if (spawned > 0) {
            player.sendSystemMessage(Component.translatable("madness.eldritch_horror.hallucination"));
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    ModSounds.AMBIENT_WHISPER.get(), SoundSource.AMBIENT, 0.7F, 1.0F);
            notifyWitnesses(level, player);
        }
    }

    private static boolean spawnPhantom(ServerLevel level, ServerPlayer player, long now) {
        for (int attempt = 0; attempt < 16; attempt++) {
            double angle = level.getRandom().nextDouble() * Math.PI * 2.0;
            double distance = 6.0 + level.getRandom().nextDouble() * 6.0;
            int x = player.getBlockX() + (int) Math.round(Math.cos(angle) * distance);
            int z = player.getBlockZ() + (int) Math.round(Math.sin(angle) * distance);
            BlockPos spot = BestiarySupport.surfaceSpot(level, x, z, false);
            if (spot == null) {
                continue;
            }
            EntityType<?> type = biomeMob(level, spot);
            Entity entity = type.spawn(level, spot, EntitySpawnReason.EVENT);
            if (entity == null) {
                continue;
            }
            entity.getPersistentData().putBoolean(ILLUSION_TAG, true);
            if (entity instanceof Mob mob) {
                mob.setTarget(player);
            }
            ILLUSIONS.put(entity.getUUID(),
                    new Illusion(player.getUUID(), now + ModConfig.MADNESS_ILLUSION_LIFETIME.get()));
            return true;
        }
        return false;
    }

    /** A monster that this biome can actually spawn, with a vanilla fallback. */
    private static EntityType<?> biomeMob(ServerLevel level, BlockPos pos) {
        MobSpawnSettings spawns = level.environmentAttributes()
                .getValue(EnvironmentAttributes.NATURAL_MOB_SPAWNS, pos);
        var pick = spawns.getMobsToSpawn(MobCategory.MONSTER).getRandom(level.getRandom());
        return pick.<EntityType<?>>map(data -> data.type()).orElse(EntityTypes.ZOMBIE);
    }

    /** Tells nearby lucid players that something is wrong with the afflicted. */
    private static void notifyWitnesses(ServerLevel level, ServerPlayer afflicted) {
        double radius = ModConfig.MADNESS_WITNESS_RADIUS.get();
        double minimum = ModConfig.MADNESS_WITNESS_MIN_SANITY.get();
        for (ServerPlayer other : level.players()) {
            if (other == afflicted || other.distanceToSqr(afflicted) > radius * radius) {
                continue;
            }
            double max = SanitySystem.max(other);
            if (max > 0.0 && SanitySystem.get(other) / max >= minimum) {
                other.sendSystemMessage(Component.translatable(
                        "madness.eldritch_horror.witness", afflicted.getDisplayName()));
            }
        }
    }

    /** Blindness, nausea and a short involuntary step to a loaded surface spot. */
    private static void fugue(ServerPlayer player) {
        player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
        player.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 100, 0));
        if (player.level() instanceof ServerLevel level) {
            double angle = level.getRandom().nextDouble() * Math.PI * 2.0;
            int distance = 8 + level.getRandom().nextInt(9);
            int x = player.getBlockX() + (int) Math.round(Math.cos(angle) * distance);
            int z = player.getBlockZ() + (int) Math.round(Math.sin(angle) * distance);
            if (level.hasChunkAt(x, z)) {
                int y = level.getHeight(Heightmap.Types.OCEAN_FLOOR, x, z);
                player.teleportTo(x + 0.5, y, z + 0.5);
            }
        }
        player.sendSystemMessage(Component.translatable("madness.eldritch_horror.fugue"));
    }

    /** Drop the held stack and shake for a moment. */
    private static void compulsion(ServerPlayer player) {
        ItemStack held = player.getMainHandItem();
        if (!held.isEmpty()) {
            player.drop(held.copy(), false, Prediction.SERVER_ONLY);
            player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        }
        player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 0));
        player.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 100, 0));
        player.sendSystemMessage(Component.translatable("madness.eldritch_horror.compulsion"));
    }

    private static void expire(MinecraftServer server, long now) {
        Iterator<Map.Entry<UUID, Illusion>> iterator = ILLUSIONS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, Illusion> entry = iterator.next();
            Entity entity = server.overworld().getEntity(entry.getKey());
            if (entity == null || now >= entry.getValue().until()) {
                if (entity != null) {
                    entity.discard();
                }
                LAST_HIT.remove(entry.getKey());
                iterator.remove();
            }
        }
    }

    /** Removes a phantom with a puff; a player kill earns a small relief. */
    private static void pop(Entity illusion, @Nullable ServerPlayer striker) {
        illusion.discard();
        ILLUSIONS.remove(illusion.getUUID());
        LAST_HIT.remove(illusion.getUUID());
        if (illusion.level() instanceof ServerLevel level) {
            level.sendParticles(ParticleTypes.SMOKE, illusion.getX(), illusion.getY() + 1.0,
                    illusion.getZ(), 12, 0.4, 0.6, 0.4, 0.01);
            level.playSound(null, illusion.getX(), illusion.getY(), illusion.getZ(),
                    ModSounds.AMBIENT_WHISPER.get(), SoundSource.AMBIENT, 0.6F, 0.9F);
        }
        if (striker != null) {
            SanityAPI.add(striker, ModConfig.MADNESS_ILLUSION_REWARD.get());
            striker.sendSystemMessage(Component.translatable("madness.eldritch_horror.never_there"));
        }
    }

    private static boolean isIllusion(Entity entity) {
        return entity.getPersistentData().getBooleanOr(ILLUSION_TAG, false);
    }
}
