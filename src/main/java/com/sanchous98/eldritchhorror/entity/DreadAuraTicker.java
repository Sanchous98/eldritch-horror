package com.sanchous98.eldritchhorror.entity;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.core.ModConfig;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * The single server tick that applies every {@link DreadAura} for every player. One handler rather
 * than three, so the shared trait actually shares its loop (the design's "no duplication storms").
 * Mirrors {@code RisenHuskSanityDrain}: once per second, overworld-only, and each aura is
 * radius-bounded and capped inside {@link BestiarySupport#tickAura}.
 *
 * <p>Each aura's own config flag gates it, so disabling one mob's hook costs nothing here.
 */
@EventBusSubscriber(modid = EldritchHorror.MODID)
public final class DreadAuraTicker {

    /** Evaluate once per second, matching the sanity source pass. */
    private static final int INTERVAL_TICKS = 20;

    private DreadAuraTicker() {
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (event.getServer().getTickCount() % INTERVAL_TICKS != 0) {
            return;
        }
        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            if (!player.level().dimension().equals(Level.OVERWORLD)) {
                continue;
            }
            BestiarySupport.tickAura(player, TaintedFauna.class, ModConfig.CORRUPTION_TAINTED_FAUNA_RADIUS.get());
            BestiarySupport.tickAura(player, LesserSwarm.class, ModConfig.SANITY_LESSER_SWARM_RADIUS.get());
            BestiarySupport.tickAura(player, Watcher.class, ModConfig.SANITY_WATCHER_RADIUS.get());
        }
    }
}
