package com.sanchous98.eldritchhorror.cult;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.core.ModConfig;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Slow reputation decay ({@code design/03c-reputation.md}: "Slow drift toward 0 for inactive
 * players (config-gated, off by default on small servers)"): once a second, like
 * {@code SanityTicker}/{@code CorruptionTicker}, each online player's standing with every cult
 * loses one point per full period of inactivity — a period in which that standing did not change
 * ({@link CultSystem} stamps every write, including the drift itself).
 *
 * <p>Decay only ever lowers a positive standing and stops at {@code 0}; a negative standing is a
 * grudge and time alone does not forgive it. It has its own small ticker rather than riding the
 * sanity/corruption passes: those are gated on {@code ENABLE_SANITY}/{@code ENABLE_CORRUPTION}
 * and overworld-only, and reputation has no geography.
 *
 * <p>The last change per player per cult is tracked as the level's game time (a tick count) in
 * memory only: a restart resets every timer, which merely gives each player one fresh period
 * before the first drift — nothing is persisted, and nothing needs to be (it is only a timer).
 */
@EventBusSubscriber(modid = EldritchHorror.MODID)
public final class ReputationDecayTicker {

    /** Evaluate once per second, matching the other per-player tickers. */
    private static final int INTERVAL_TICKS = 20;

    /** Game tick of the last standing change, per player, per cult id (in-memory: see above). */
    private static final Map<UUID, Map<String, Long>> LAST_CHANGE = new ConcurrentHashMap<>();

    private ReputationDecayTicker() {
    }

    /** Records that the player's standing with {@code cultId} just changed, resetting its timer. */
    static void stamp(ServerPlayer player, String cultId) {
        LAST_CHANGE.computeIfAbsent(player.getUUID(), id -> new ConcurrentHashMap<>())
                .put(cultId, player.level().getGameTime());
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (!ModConfig.ENABLE_REPUTATION_DECAY.get()) {
            return;
        }
        if (event.getServer().getTickCount() % INTERVAL_TICKS != 0) {
            return;
        }
        long periodTicks =
                (long) ModConfig.REPUTATION_DECAY_PERIOD_SECONDS.get() * INTERVAL_TICKS;
        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            tick(player, periodTicks);
        }
    }

    /** One player's cult standings: at most one aggregated drift step per cult this second. */
    private static void tick(ServerPlayer player, long periodTicks) {
        Map<String, Long> stamps =
                LAST_CHANGE.computeIfAbsent(player.getUUID(), id -> new ConcurrentHashMap<>());
        long now = player.level().getGameTime();
        for (CultDefinition def : Cults.all()) {
            String cultId = def.id();
            int value = CultSystem.get(player, cultId);
            Long last = stamps.get(cultId);
            if (last == null) {
                // First sight of this player/cult: start the timer (never drift on the first
                // tick). A standing at or below 0 needs no timer — every write stamps it anyway.
                if (value > 0) {
                    stamps.put(cultId, now);
                }
                continue;
            }
            int points = ReputationDecayMath.decayPoints(value, now - last, periodTicks);
            if (points > 0) {
                // set() re-stamps, so the next point lands a full period after this one. The
                // notice is an action-bar line, not chat: at most one per cult per period.
                int after = CultSystem.set(player, cultId, value - points);
                player.sendOverlayMessage(Component.translatable("cult.eldritch_horror.decay",
                        Component.translatable("cult.eldritch_horror." + cultId), after));
            }
        }
    }
}
