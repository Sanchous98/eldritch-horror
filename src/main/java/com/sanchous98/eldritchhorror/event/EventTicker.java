package com.sanchous98.eldritchhorror.event;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.core.ModConfig;
import com.sanchous98.eldritchhorror.corruption.CorruptionAPI;
import com.sanchous98.eldritchhorror.corruption.CorruptionState;
import com.sanchous98.eldritchhorror.corruption.CorruptionSystem;
import com.sanchous98.eldritchhorror.sanity.SanityAPI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.MoonPhase;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.jspecify.annotations.Nullable;

/**
 * The single server tick for the events framework (design/19-events.md). One shared ticker keeps
 * events bounded so they never stack into noise:
 *
 * <ul>
 *   <li><b>Trigger cadence</b> — triggers are evaluated every {@code eventIntervalTicks} (default
 *       200 = 10s), server-authoritative and overworld-only, matching the sanity/corruption
 *       tickers' once-a-second pattern.</li>
 *   <li><b>Effects</b> — an active event's effect runs every server tick, so a darkness pulse or a
 *       whisper cadence is smooth even though triggers are cheap.</li>
 *   <li><b>Caps</b> — a global concurrent-event cap and a per-player cap; at most one new event is
 *       started per player per evaluation, chosen by weight.</li>
 *   <li><b>Cooldowns</b> — after an event ends it cannot restart for its own cooldown, tracked per
 *       player + event.</li>
 *   <li><b>Readable</b> — start/end are logged at INFO (server log only), and {@code /eh event}
 *       drives one on demand for testing.</li>
 * </ul>
 *
 * <p>State is server-side only and dropped on logout. No {@code Math.random}: selection and effect
 * sampling use a {@link RandomSource} seeded from the player id and tick.
 */
@EventBusSubscriber(modid = EldritchHorror.MODID)
public final class EventTicker {

    /**
     * The fixed {@code cult_stronghold} centre, in world X/Z (the site has no meaningful Y on the
     * surface). Hard-coded deliberately: the event hot path must not touch the lazily-built
     * {@code Locations} registry. The radius around it is config-driven
     * ({@code eventCultSiteRadius}).
     */
    private static final int CULT_SITE_X = -29127;
    private static final int CULT_SITE_Z = -13835;

    private EventTicker() {
    }

    /** A running event for one player: its definition, window, and latest snapshotted context. */
    private static final class Active {
        final EldritchEvent event;
        final int startTick;
        final int endTick;
        EventContext ctx;

        Active(EldritchEvent event, int startTick, int endTick, EventContext ctx) {
            this.event = event;
            this.startTick = startTick;
            this.endTick = endTick;
            this.ctx = ctx;
        }
    }

    private static final Map<UUID, Map<String, Active>> ACTIVE = new ConcurrentHashMap<>();
    private static final Map<UUID, Map<String, Integer>> COOLDOWN = new ConcurrentHashMap<>();

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (!ModConfig.ENABLE_EVENTS.get()) {
            return;
        }
        Events.init();
        int tick = event.getServer().getTickCount();

        // Effects run every tick; only players with an active event cost anything here.
        advance(event.getServer(), tick);

        int interval = Math.max(1, ModConfig.EVENT_INTERVAL_TICKS.get());
        if (tick % interval == 0) {
            evaluate(event.getServer(), tick);
        }
    }

    /** Drop all state when a player leaves, so a rejoin starts clean. */
    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ACTIVE.remove(player.getUUID());
            COOLDOWN.remove(player.getUUID());
        }
    }

    /**
     * Force-starts {@code id} for {@code player}, ignoring the master switch, the per-event toggle
     * and cooldowns (the operator/debug escape hatch behind {@code /eh event}). Returns the started
     * event, or {@code null} if the id is unknown or the player is not in the overworld.
     */
    public static @Nullable EldritchEvent forceStart(ServerPlayer player, String id, int tick) {
        Events.init();
        EldritchEvent def = Events.byId(id);
        if (def == null || !player.level().dimension().equals(Level.OVERWORLD)) {
            return null;
        }
        // start(player, def, tick) begins the window at the current tick; the ticker advances after
        // evaluate, so the next tick runs the effect with elapsed==1 — the same elapsed a naturally
        // started event sees on its first effect tick.
        start(player, def, tick);
        return def;
    }

    /** The currently active events for {@code player}, as {@code id@elapsed} strings. */
    public static List<String> activeFor(ServerPlayer player, int tick) {
        Map<String, Active> map = ACTIVE.get(player.getUUID());
        if (map == null || map.isEmpty()) {
            return List.of();
        }
        List<String> out = new ArrayList<>();
        for (Active active : map.values()) {
            out.add(active.event.id() + "@" + ((tick - active.startTick) / 20) + "s");
        }
        return out;
    }

    /** Number of events active right now across all players. */
    public static int activeCount() {
        int n = 0;
        for (Map<String, Active> map : ACTIVE.values()) {
            n += map.size();
        }
        return n;
    }

    /**
     * The ids of the events active for {@code player} right now. Used by the codex discovery pass
     * so an event starting near a player is learned without coupling the ticker to the codex.
     */
    public static List<String> activeIds(ServerPlayer player) {
        Map<String, Active> map = ACTIVE.get(player.getUUID());
        if (map == null || map.isEmpty()) {
            return List.of();
        }
        return List.copyOf(map.keySet());
    }

    /** Advances every running effect, ending any whose window closed or whose rift vanished. */
    private static void advance(MinecraftServer server, int tick) {
        if (ACTIVE.isEmpty()) {
            return; // nothing active: skip the per-tick key-set copy entirely
        }
        // Refresh snapshotted context at most once a second, not every tick: a rift scan is a
        // bounded but non-trivial read, and a 1s-old snapshot is plenty for these effects.
        boolean refresh = tick % 20 == 0;
        int riftChunkRadius = ModConfig.EVENT_RIFT_SCAN_CHUNK_RADIUS.get();
        int maxRifts = ModConfig.EVENT_RIFT_SCAN_MAX.get();
        for (UUID id : List.copyOf(ACTIVE.keySet())) {
            Map<String, Active> map = ACTIVE.get(id);
            ServerPlayer player = server.getPlayerList().getPlayer(id);
            if (map == null) {
                continue;
            }
            if (player == null || !player.level().dimension().equals(Level.OVERWORLD)) {
                ACTIVE.remove(id);
                continue;
            }
            Iterator<Map.Entry<String, Active>> it = map.entrySet().iterator();
            while (it.hasNext()) {
                Active active = it.next().getValue();
                if (refresh) {
                    active.ctx = context(player, tick, active.event.trigger().usesRifts(),
                            riftChunkRadius, maxRifts);
                }
                if (tick >= active.endTick || endedEarly(active)) {
                    it.remove();
                    cooldownUntil(id, active.event, tick);
                    EldritchHorror.LOGGER.info("event {} ended for {}", active.event.id(),
                            player.getName().getString());
                    continue;
                }
                active.event.effect().tick(player, active.ctx, tick - active.startTick,
                        active.event.duration(), tick);
            }
            if (map.isEmpty()) {
                ACTIVE.remove(id);
            }
        }
    }

    /** rift_bloom ends as soon as its rift is gone, rather than running out its full window. */
    private static boolean endedEarly(Active active) {
        return active.event.trigger() == EventTrigger.RIFT_AGING && active.ctx.rifts().isEmpty();
    }

    /** One trigger evaluation pass over the overworld's players. */
    private static void evaluate(MinecraftServer server, int tick) {
        if (server.getLevel(Level.OVERWORLD) == null) {
            return;
        }
        int cap = ModConfig.EVENT_MAX_ACTIVE.get();
        int perPlayer = ModConfig.EVENT_MAX_PER_PLAYER.get();
        int maxRifts = ModConfig.EVENT_RIFT_SCAN_MAX.get();
        int riftChunkRadius = ModConfig.EVENT_RIFT_SCAN_CHUNK_RADIUS.get();
        boolean anyRiftTrigger = anyRiftTrigger();

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (!player.level().dimension().equals(Level.OVERWORLD)
                    || player.isSpectator() || !player.isAlive()) {
                continue;
            }
            Map<String, Active> mine = ACTIVE.get(player.getUUID());
            if (mine == null) {
                mine = Map.of();
            }
            if (mine.size() >= perPlayer || activeCount() >= cap) {
                continue;
            }

            EventContext ctx = context(player, tick, anyRiftTrigger, riftChunkRadius, maxRifts);
            List<EldritchEvent> candidates = new ArrayList<>();
            int totalWeight = 0;
            for (EldritchEvent def : Events.all()) {
                if (!Events.enabled(def.id()) || mine.containsKey(def.id())
                        || onCooldown(player.getUUID(), def, tick)) {
                    continue;
                }
                if (def.triggerTest().test(ctx)) {
                    candidates.add(def);
                    totalWeight += Math.max(1, def.weight());
                }
            }
            if (candidates.isEmpty() || totalWeight <= 0) {
                continue;
            }
            int pick = RandomSource.create(player.getUUID().getMostSignificantBits()
                    ^ tick * 0x9E3779B97F4A7C15L).nextInt(totalWeight);
            EldritchEvent chosen = candidates.getLast();
            for (EldritchEvent candidate : candidates) {
                pick -= Math.max(1, candidate.weight());
                if (pick < 0) {
                    chosen = candidate;
                    break;
                }
            }
            // Reuse the context already snapshotted this pass (its rift list is populated whenever
            // any rift-triggered event is enabled, which is exactly when a candidate could need it).
            start(player, chosen, tick, ctx);
        }
    }

    /** Whether any enabled event needs the bounded nearby-rift scan on this tick. */
    private static boolean anyRiftTrigger() {
        return ModConfig.ENABLE_EVENT_RIFT_BLOOM.get()
                || ModConfig.ENABLE_EVENT_DARKNESS_PULSE.get()
                || ModConfig.ENABLE_EVENT_VEIL_THIN.get();
    }

    /** Snapshot the cheap facts a trigger needs; the rift scan is skipped when unused. */
    private static EventContext context(ServerPlayer player, int tick, boolean anyRiftTrigger,
                                        int riftChunkRadius, int maxRifts) {
        ServerLevel level = player.level();
        List<BlockPos> rifts = anyRiftTrigger
                ? Rifts.near(level, player.blockPosition(), riftChunkRadius, maxRifts)
                : List.of();
        MoonPhase moon = level.environmentAttributes().getValue(
                EnvironmentAttributes.MOON_PHASE, player.position(), null);
        CorruptionState corruption = CorruptionState.of(CorruptionAPI.get(player));
        BlockPos pos = player.blockPosition();
        double dx = pos.getX() - CULT_SITE_X;
        double dz = pos.getZ() - CULT_SITE_Z;
        double cultRadius = ModConfig.EVENT_CULT_SITE_RADIUS.get();
        boolean nearCultSite = dx * dx + dz * dz <= cultRadius * cultRadius;
        return new EventContext(level, level.getGameTime(), tick, level.isDarkOutside(),
                level.isThundering(), SanityAPI.get(player), CorruptionSystem.getTaintAt(player), rifts,
                moon == MoonPhase.FULL_MOON, corruption, nearCultSite);
    }

    /** Starts {@code def} for {@code player} and logs it. */
    private static void start(ServerPlayer player, EldritchEvent def, int tick) {
        start(player, def, tick, context(player, tick, def.trigger().usesRifts(),
                ModConfig.EVENT_RIFT_SCAN_CHUNK_RADIUS.get(), ModConfig.EVENT_RIFT_SCAN_MAX.get()));
    }

    /** Starts {@code def} with an already-snapshotted context and logs it. */
    private static void start(ServerPlayer player, EldritchEvent def, int tick, EventContext ctx) {
        Map<String, Active> map = ACTIVE.computeIfAbsent(player.getUUID(), k -> new HashMap<>());
        map.put(def.id(), new Active(def, tick, tick + Math.max(1, def.duration()), ctx));
        EldritchHorror.LOGGER.info("event {} started for {} ({}t)", def.id(),
                player.getName().getString(), def.duration());
    }

    private static boolean onCooldown(UUID id, EldritchEvent def, int tick) {
        Map<String, Integer> map = COOLDOWN.get(id);
        return map != null && tick < map.getOrDefault(def.id(), Integer.MIN_VALUE);
    }

    private static void cooldownUntil(UUID id, EldritchEvent def, int tick) {
        COOLDOWN.computeIfAbsent(id, k -> new HashMap<>()).put(def.id(), tick + Math.max(0, def.cooldown()));
    }
}
