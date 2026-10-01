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
 * Bounded spawner for {@link ChoirSpite}. Uses the shared {@link BestiarySupport#topUpAir} helper
 * (its flying counterpart) because a spite appears in air above the terrain. The gate is darkness or
 * a tainted column — a mote of the Choir is where the wrong already is. Low cap and a bounded
 * lifetime on the mob keep it from accumulating.
 */
@EventBusSubscriber(modid = EldritchHorror.MODID)
public final class ChoirSpiteSpawner {

    private ChoirSpiteSpawner() {
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (!ModConfig.ENABLE_CHOIR_SPITE_SPAWNS.get()) {
            return;
        }
        int interval = ModConfig.CHOIR_SPITE_SPAWN_INTERVAL_TICKS.get();
        int tick = event.getServer().getTickCount();
        if (tick % interval != 0) {
            return;
        }
        ServerLevel level = event.getServer().getLevel(Level.OVERWORLD);
        if (level == null) {
            return;
        }
        ServerChunkCache cache = level.getChunkSource();
        int perPass = ModConfig.CHOIR_SPITE_SPAWN_COUNT.get();
        for (ServerPlayer player : level.players()) {
            BestiarySupport.topUpAir(level, cache, player,
                    ModEntities.CHOIR_SPITE.get(), ChoirSpite.class,
                    ModConfig.CHOIR_SPITE_SPAWN_RADIUS.get(),
                    ModConfig.CHOIR_SPITE_SPAWN_CAP.get(), perPass,
                    ModConfig.CHOIR_SPITE_SPAWN_MIN_Y.get(), tick,
                    (lvl, spot, dark) -> dark);
        }
    }
}
