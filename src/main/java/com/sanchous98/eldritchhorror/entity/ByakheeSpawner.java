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
 * Bounded spawner for {@link Byakhee}. It uses the shared {@link BestiarySupport#topUpAir} helper
 * (the flying counterpart of {@code topUp}) because it appears in air above the terrain, not on the
 * ground. Same contract: server-authoritative, overworld-only, deterministic, loaded chunks only.
 * The gate is <b>night</b>: it swoops in the dark, which approximates the design's "swoops at low
 * sanity, not low sleep". Low count, low cap — an ambusher should be a surprise.
 */
@EventBusSubscriber(modid = EldritchHorror.MODID)
public final class ByakheeSpawner {

    private ByakheeSpawner() {
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (!ModConfig.ENABLE_BYAKHEE_SPAWNS.get()) {
            return;
        }
        int interval = ModConfig.BYAKHEE_SPAWN_INTERVAL_TICKS.get();
        int tick = event.getServer().getTickCount();
        if (tick % interval != 0) {
            return;
        }
        ServerLevel level = event.getServer().getLevel(Level.OVERWORLD);
        if (level == null || !level.isDarkOutside()) {
            return; // it only flies at night
        }
        ServerChunkCache cache = level.getChunkSource();
        int perPass = ModConfig.BYAKHEE_SPAWN_COUNT.get();
        for (ServerPlayer player : level.players()) {
            BestiarySupport.topUpAir(level, cache, player,
                    ModEntities.BYAKHEE.get(), Byakhee.class,
                    ModConfig.BYAKHEE_SPAWN_RADIUS.get(),
                    ModConfig.BYAKHEE_SPAWN_CAP.get(), perPass,
                    ModConfig.BYAKHEE_SPAWN_MIN_Y.get(), tick,
                    (lvl, spot, dark) -> dark);
        }
    }
}
