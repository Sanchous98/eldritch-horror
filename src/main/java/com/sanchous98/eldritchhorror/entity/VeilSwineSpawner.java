package com.sanchous98.eldritchhorror.entity;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.core.ModConfig;
import com.sanchous98.eldritchhorror.registry.ModEntities;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Bounded spawner for {@link VeilSwine}. The design calls it a <b>herd</b>, so the per-pass count
 * is the "small group" (2 by default) and the cap is generous: a pass tops the group up towards the
 * cap rather than placing one straggler at a time, and {@link PackAlertGoal} makes the group turn
 * together. Gate and bounds are the husk's (server-authoritative, overworld-only, loaded chunks
 * only, deterministic sampling, dark or tainted ground).
 */
@EventBusSubscriber(modid = EldritchHorror.MODID)
public final class VeilSwineSpawner {

    private VeilSwineSpawner() {
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (!ModConfig.ENABLE_VEIL_SWINE_SPAWNS.get()) {
            return;
        }
        int interval = ModConfig.VEIL_SWINE_SPAWN_INTERVAL_TICKS.get();
        if (event.getServer().getTickCount() % interval != 0) {
            return;
        }
        ServerLevel level = event.getServer().getLevel(Level.OVERWORLD);
        if (level == null) {
            return;
        }
        int perPass = ModConfig.VEIL_SWINE_SPAWN_COUNT.get();
        if (perPass <= 0) {
            return;
        }
        ServerChunkCache cache = level.getChunkSource();
        int tick = event.getServer().getTickCount();
        int radius = ModConfig.VEIL_SWINE_SPAWN_RADIUS.get();
        int cap = ModConfig.VEIL_SWINE_SPAWN_CAP.get();
        for (ServerPlayer player : level.players()) {
            BestiarySupport.topUp(level, cache, player, ModEntities.VEIL_SWINE.get(),
                    VeilSwine.class, radius, cap, perPass, tick,
                    (lvl, spot, dark, tainted) -> dark || tainted);
        }
    }
}
