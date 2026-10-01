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
 * Bounded spawner for {@link Watcher}. Family: lesser horror, but it is a <b>presence</b>, not a
 * fight, so it is deliberately rare: a long interval and a per-player cap of one. It attaches to
 * the corrupted — it appears only where a chunk's taint is up, or in deep darkness. At most one is
 * topped up per pass; a Watcher is meant to be the only one watching. Loaded chunks only,
 * deterministic, config-bounded.
 */
@EventBusSubscriber(modid = EldritchHorror.MODID)
public final class WatcherSpawner {

    private WatcherSpawner() {
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (!ModConfig.ENABLE_WATCHER_SPAWNS.get()) {
            return;
        }
        int interval = ModConfig.WATCHER_SPAWN_INTERVAL_TICKS.get();
        if (event.getServer().getTickCount() % interval != 0) {
            return;
        }
        ServerLevel level = event.getServer().getLevel(Level.OVERWORLD);
        if (level == null) {
            return;
        }
        ServerChunkCache cache = level.getChunkSource();
        int perPass = ModConfig.WATCHER_SPAWN_COUNT.get();
        int tick = event.getServer().getTickCount();
        for (ServerPlayer player : level.players()) {
            BestiarySupport.topUp(level, cache, player,
                    ModEntities.WATCHER.get(), Watcher.class,
                    ModConfig.WATCHER_SPAWN_RADIUS.get(),
                    ModConfig.WATCHER_SPAWN_CAP.get(), perPass, tick,
                    (lvl, spot, dark, tainted) -> tainted || dark);
        }
    }
}
