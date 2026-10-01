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
 * Bounded spawner for {@link VeilStalker}. Reuses the shared {@link BestiarySupport#topUp} loop; the
 * only per-mob parts are the config bounds and the gate. It ambushes from dark or tainted loaded
 * terrain, on a longer interval and a low cap (an ambusher should be a surprise, not a horde).
 * Loaded chunks only, deterministic sampling.
 */
@EventBusSubscriber(modid = EldritchHorror.MODID)
public final class VeilStalkerSpawner {

    private VeilStalkerSpawner() {
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (!ModConfig.ENABLE_VEIL_STALKER_SPAWNS.get()) {
            return;
        }
        int interval = ModConfig.VEIL_STALKER_SPAWN_INTERVAL_TICKS.get();
        if (event.getServer().getTickCount() % interval != 0) {
            return;
        }
        ServerLevel level = event.getServer().getLevel(Level.OVERWORLD);
        if (level == null) {
            return;
        }
        ServerChunkCache cache = level.getChunkSource();
        int perPass = ModConfig.VEIL_STALKER_SPAWN_COUNT.get();
        int tick = event.getServer().getTickCount();
        for (ServerPlayer player : level.players()) {
            BestiarySupport.topUp(level, cache, player,
                    ModEntities.VEIL_STALKER.get(), VeilStalker.class,
                    ModConfig.VEIL_STALKER_SPAWN_RADIUS.get(),
                    ModConfig.VEIL_STALKER_SPAWN_CAP.get(), perPass, tick,
                    (lvl, spot, dark, tainted) -> dark || tainted);
        }
    }
}
