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
 * Bounded spawner for {@link WailHusk}. It is a <b>flier</b>, so it uses the shared
 * {@link BestiarySupport#topUpAir} pass the byakhee and night hag already use: dark air above the
 * terrain, above {@code wailHuskSpawnMinY}, per-player capped, loaded chunks only, deterministic
 * sampling. The line-of-sight gate on its drain lives in {@link WailHusk#auraRateFor}, not here.
 */
@EventBusSubscriber(modid = EldritchHorror.MODID)
public final class WailHuskSpawner {

    private WailHuskSpawner() {
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (!ModConfig.ENABLE_WAIL_HUSK_SPAWNS.get()) {
            return;
        }
        int interval = ModConfig.WAIL_HUSK_SPAWN_INTERVAL_TICKS.get();
        int tick = event.getServer().getTickCount();
        if (tick % interval != 0) {
            return;
        }
        ServerLevel level = event.getServer().getLevel(Level.OVERWORLD);
        if (level == null) {
            return;
        }
        ServerChunkCache cache = level.getChunkSource();
        int perPass = ModConfig.WAIL_HUSK_SPAWN_COUNT.get();
        if (perPass <= 0) {
            return;
        }
        for (ServerPlayer player : level.players()) {
            BestiarySupport.topUpAir(level, cache, player, ModEntities.WAIL_HUSK.get(),
                    WailHusk.class, ModConfig.WAIL_HUSK_SPAWN_RADIUS.get(),
                    ModConfig.WAIL_HUSK_SPAWN_CAP.get(), perPass,
                    ModConfig.WAIL_HUSK_SPAWN_MIN_Y.get(), tick,
                    (lvl, spot, dark) -> dark);
        }
    }
}
