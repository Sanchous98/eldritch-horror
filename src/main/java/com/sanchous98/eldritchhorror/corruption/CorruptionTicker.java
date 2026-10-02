package com.sanchous98.eldritchhorror.corruption;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.core.ModConfig;
import com.sanchous98.eldritchhorror.registry.ModEffects;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Drives the corruption meter once a second: diffs the per-chunk taint around each player, sums the
 * enabled {@link CorruptionSource}s, applies the result, and fires the threshold effects
 * <b>on state transitions only</b>.
 *
 * <p>Server-authoritative and overworld-only — the client never computes a drain. The previous
 * state is held in a small server-side map keyed by player id (dropped on logout, so rejoining
 * re-asserts the effects); it is not synced and not part of the meter. The meter itself is synced
 * separately by its attachment. Mirrors {@link com.sanchous98.eldritchhorror.sanity.SanityTicker}.
 *
 * <p>Threshold effects are applied as <b>infinite</b> and cleared explicitly on transition, so a
 * band holds until it actually changes — a finite duration would fall off between transitions.
 */
@EventBusSubscriber(modid = EldritchHorror.MODID)
public final class CorruptionTicker {

    /** Evaluate once per second. */
    private static final int INTERVAL_TICKS = 20;

    private static final Map<UUID, CorruptionState> PREVIOUS = new ConcurrentHashMap<>();

    private CorruptionTicker() {
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (!ModConfig.ENABLE_CORRUPTION.get()) {
            return;
        }
        if (event.getServer().getTickCount() % INTERVAL_TICKS != 0) {
            return;
        }
        CorruptionSources.init();

        // The taint field diffuses first, so this tick's sources read the spread field. Only the
        // overworld is stepped, and within it only the chunks around its players.
        ServerLevel overworld = event.getServer().getLevel(Level.OVERWORLD);
        if (overworld != null) {
            TaintSystem.spread(overworld);
        }

        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            if (player.level().dimension().equals(Level.OVERWORLD)) {
                tick(player);
            }
        }
    }

    /** Drop tracked state when a player leaves, so rejoining re-asserts their effects. */
    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PREVIOUS.remove(player.getUUID());
        }
    }

    /** Death clears infinite effects, so forget the cached state and let the next tick re-apply it. */
    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PREVIOUS.remove(player.getUUID());
        }
    }

    /** One player, one second's worth of corruption. */
    private static void tick(ServerPlayer player) {
        CorruptionContext ctx = CorruptionContext.of(player);

        double delta = 0.0;
        for (CorruptionSource source : CorruptionSources.all()) {
            delta += source.deltaPerSecond(player, ctx);
        }
        CorruptionSystem.add(player, delta * ModConfig.CORRUPTION_MULTIPLIER.get());

        CorruptionState after = CorruptionSystem.state(player);
        CorruptionState before = PREVIOUS.put(player.getUUID(), after);
        if (before != after) {
            applyState(player, after);
        }
    }

    /** Clear the corrupted effect, then apply exactly the new band's set. */
    private static void applyState(ServerPlayer player, CorruptionState state) {
        clear(player, ModEffects.CORRUPTED);
        switch (state) {
            case DORMANT, TOUCHED -> {
            }
            case MARKED -> effect(player, 0);
            case CLAIMED -> effect(player, 1);
        }
    }

    /** Apply the single corruption-band effect (always {@link ModEffects#CORRUPTED}). */
    private static void effect(ServerPlayer player, int amplifier) {
        player.addEffect(new MobEffectInstance(ModEffects.CORRUPTED, MobEffectInstance.INFINITE_DURATION,
                amplifier, true, false, false));
    }

    private static void clear(ServerPlayer player, Holder<MobEffect> effect) {
        if (player.hasEffect(effect)) {
            player.removeEffect(effect);
        }
    }
}
