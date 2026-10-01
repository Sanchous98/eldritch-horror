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
 * Bounded spawner for {@link LesserSwarm}. Family: lesser horror, and the design has it "seeks you
 * in the dark", so it spawns only where it is dark (vanilla monster darkness test) or where a chunk
 * is tainted. It is meant to be numerous: a higher per-pass count and cap than the husk. Loaded
 * chunks only, no force-load, deterministic sampling, all bounds config-driven.
 */
@EventBusSubscriber(modid = EldritchHorror.MODID)
public final class LesserSwarmSpawner {

    private LesserSwarmSpawner() {
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (!ModConfig.ENABLE_LESSER_SWARM_SPAWNS.get()) {
            return;
        }
        int interval = ModConfig.LESSER_SWARM_SPAWN_INTERVAL_TICKS.get();
        if (event.getServer().getTickCount() % interval != 0) {
            return;
        }
        ServerLevel level = event.getServer().getLevel(Level.OVERWORLD);
        if (level == null) {
            return;
        }
        ServerChunkCache cache = level.getChunkSource();
        int perPass = ModConfig.LESSER_SWARM_SPAWN_COUNT.get();
        int tick = event.getServer().getTickCount();
        for (ServerPlayer player : level.players()) {
            BestiarySupport.topUp(level, cache, player,
                    ModEntities.LESSER_SWARM.get(), LesserSwarm.class,
                    ModConfig.LESSER_SWARM_SPAWN_RADIUS.get(),
                    ModConfig.LESSER_SWARM_SPAWN_CAP.get(), perPass, tick,
                    (lvl, spot, dark, tainted) -> dark || tainted);
        }
    }
}
