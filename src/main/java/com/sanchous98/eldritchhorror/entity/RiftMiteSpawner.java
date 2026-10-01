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
 * Bounded spawner for {@link RiftMite}. Reuses the shared {@link BestiarySupport#topUp} loop; the
 * per-role gate is <b>tainted chunks only</b> — these vermin are the corruption made motile. They
 * arrive in a small group (a higher per-pass count) and stack a swarm corruption vector, so the cap
 * is the highest of the lesser mobs.
 */
@EventBusSubscriber(modid = EldritchHorror.MODID)
public final class RiftMiteSpawner {

    private RiftMiteSpawner() {
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (!ModConfig.ENABLE_RIFT_MITE_SPAWNS.get()) {
            return;
        }
        int interval = ModConfig.RIFT_MITE_SPAWN_INTERVAL_TICKS.get();
        if (event.getServer().getTickCount() % interval != 0) {
            return;
        }
        ServerLevel level = event.getServer().getLevel(Level.OVERWORLD);
        if (level == null) {
            return;
        }
        ServerChunkCache cache = level.getChunkSource();
        int perPass = ModConfig.RIFT_MITE_SPAWN_COUNT.get();
        int tick = event.getServer().getTickCount();
        for (ServerPlayer player : level.players()) {
            BestiarySupport.topUp(level, cache, player,
                    ModEntities.RIFT_MITE.get(), RiftMite.class,
                    ModConfig.RIFT_MITE_SPAWN_RADIUS.get(),
                    ModConfig.RIFT_MITE_SPAWN_CAP.get(), perPass, tick,
                    (lvl, spot, dark, tainted) -> tainted);
        }
    }
}
