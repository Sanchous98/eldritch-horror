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
 * Bounded spawner for {@link BoneChoir}. It reuses the shared {@link BestiarySupport#topUp} loop —
 * the only per-mob parts are the config bounds and the gate. Bone choirs rise in dark or tainted
 * loaded terrain, like the risen husk, but on a slightly longer interval and a lower cap (they are
 * not meant to swarm). Loaded chunks only, deterministic sampling.
 */
@EventBusSubscriber(modid = EldritchHorror.MODID)
public final class BoneChoirSpawner {

    private BoneChoirSpawner() {
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (!ModConfig.ENABLE_BONE_CHOIR_SPAWNS.get()) {
            return;
        }
        int interval = ModConfig.BONE_CHOIR_SPAWN_INTERVAL_TICKS.get();
        if (event.getServer().getTickCount() % interval != 0) {
            return;
        }
        ServerLevel level = event.getServer().getLevel(Level.OVERWORLD);
        if (level == null) {
            return;
        }
        ServerChunkCache cache = level.getChunkSource();
        int perPass = ModConfig.BONE_CHOIR_SPAWN_COUNT.get();
        int tick = event.getServer().getTickCount();
        for (ServerPlayer player : level.players()) {
            BestiarySupport.topUp(level, cache, player,
                    ModEntities.BONE_CHOIR.get(), BoneChoir.class,
                    ModConfig.BONE_CHOIR_SPAWN_RADIUS.get(),
                    ModConfig.BONE_CHOIR_SPAWN_CAP.get(), perPass, tick,
                    (lvl, spot, dark, tainted) -> dark || tainted);
        }
    }
}
