package com.sanchous98.eldritchhorror.sanity;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.investigator.Progression;
import com.sanchous98.eldritchhorror.core.ModConfig;
import com.sanchous98.eldritchhorror.registry.ModEffects;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Drives the sanity meter once a second: sums the enabled {@link SanitySource}s, applies the
 * result, and fires the threshold effects <b>on state transitions only</b>.
 *
 * <p>Server-authoritative and overworld-only — the client never computes a drain. The previous
 * state is held in a small server-side map keyed by player id (dropped on logout, so rejoining
 * re-asserts the effects); it is not synced and not part of the meter. The meter itself is synced
 * separately by its attachment.
 *
 * <p>Threshold effects are applied as <b>infinite</b> and cleared explicitly on transition, so a
 * band holds until it actually changes — a finite duration would fall off between transitions.
 */
@EventBusSubscriber(modid = EldritchHorror.MODID)
public final class SanityTicker {

    /** Evaluate once per second. */
    private static final int INTERVAL_TICKS = 20;

    private static final Map<UUID, SanityState> PREVIOUS = new ConcurrentHashMap<>();

    private SanityTicker() {
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (!ModConfig.ENABLE_SANITY.get()) {
            return;
        }
        if (event.getServer().getTickCount() % INTERVAL_TICKS != 0) {
            return;
        }
        SanitySources.init();
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

    /** One player, one second's worth of sanity. */
    private static void tick(ServerPlayer player) {
        SanityContext ctx = SanityContext.of(player);

        double delta = 0.0;
        for (SanitySource source : SanitySources.all()) {
            double sourceDelta = source.deltaPerSecond(player, ctx);
            // Dr. Amos recovers faster in cities: scale the city source's recovery at the source.
            if (sourceDelta > 0.0 && "city".equals(source.id())) {
                sourceDelta *= Progression.cityRecoveryMultiplier(player);
            }
            delta += sourceDelta;
        }
        // Investigator passive scales drain only: recovery is never penalised.
        if (delta < 0.0) {
            delta *= Progression.sanityDrainMultiplier(player);
        }
        SanitySystem.add(player, delta * ModConfig.SANITY_DRAIN_MULTIPLIER.get());

        SanityState after = SanitySystem.state(player);
        SanityState before = PREVIOUS.put(player.getUUID(), after);
        if (before != after) {
            applyState(player, after);
        }
    }

    /** Clear every threshold effect, then apply exactly the new band's set. */
    private static void applyState(ServerPlayer player, SanityState state) {
        clear(player, ModEffects.MADNESS);
        clear(player, ModEffects.MARKED);
        switch (state) {
            case COMPOSED, UNEASY -> {
            }
            case FRAYING -> effect(player, ModEffects.MADNESS, 0);
            case BREAKING -> effect(player, ModEffects.MADNESS, 1);
            case MARKED -> {
                effect(player, ModEffects.MADNESS, 2);
                effect(player, ModEffects.MARKED, 0);
            }
        }
    }

    private static void effect(ServerPlayer player, Holder<MobEffect> effect, int amplifier) {
        player.addEffect(new MobEffectInstance(effect, MobEffectInstance.INFINITE_DURATION,
                amplifier, true, false, false));
    }

    private static void clear(ServerPlayer player, Holder<MobEffect> effect) {
        if (player.hasEffect(effect)) {
            player.removeEffect(effect);
        }
    }
}
