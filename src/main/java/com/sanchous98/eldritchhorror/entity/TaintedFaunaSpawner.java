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
 * Bounded spawner for {@link TaintedFauna}. Family: tainted, so its attachment is the corruption
 * field: it appears in chunks whose taint has risen past the spread threshold (the intended
 * "converts from mundane fauna when taint rises" hook — mundane fauna are suppressed, so the taint
 * itself is the source). It also rarely appears in plain darkness at night, like a wrong animal
 * that wandered out of the blight. Loaded chunks only, per-player cap, deterministic.
 */
@EventBusSubscriber(modid = EldritchHorror.MODID)
public final class TaintedFaunaSpawner {

    private TaintedFaunaSpawner() {
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (!ModConfig.ENABLE_TAINTED_FAUNA_SPAWNS.get()) {
            return;
        }
        int interval = ModConfig.TAINTED_FAUNA_SPAWN_INTERVAL_TICKS.get();
        if (event.getServer().getTickCount() % interval != 0) {
            return;
        }
        ServerLevel level = event.getServer().getLevel(Level.OVERWORLD);
        if (level == null) {
            return;
        }
        ServerChunkCache cache = level.getChunkSource();
        int perPass = ModConfig.TAINTED_FAUNA_SPAWN_COUNT.get();
        int tick = event.getServer().getTickCount();
        for (ServerPlayer player : level.players()) {
            BestiarySupport.topUp(level, cache, player,
                    ModEntities.TAINTED_FAUNA.get(), TaintedFauna.class,
                    ModConfig.TAINTED_FAUNA_SPAWN_RADIUS.get(),
                    ModConfig.TAINTED_FAUNA_SPAWN_CAP.get(), perPass, tick,
                    (lvl, spot, dark, tainted) -> tainted || dark);
        }
    }
}
