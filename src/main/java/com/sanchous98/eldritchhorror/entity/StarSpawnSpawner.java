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
 * Ambient spawner for {@link StarSpawn}. Reuses the shared {@link BestiarySupport#topUp} loop, but
 * is <b>OFF by default</b> ({@code enableStarSpawnSpawns = false}): a star-spawn is summoned by the
 * {@code summon_star_spawn} rite, not wandered into ({@code design/25-bestiary-and-entities.md}).
 * Turning the toggle on gives it a dark/tainted gate on the same loaded-chunks-only, deterministic
 * contract. Kept as its own class so the rite engine can later call a shared helper here.
 */
@EventBusSubscriber(modid = EldritchHorror.MODID)
public final class StarSpawnSpawner {

    private StarSpawnSpawner() {
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (!ModConfig.ENABLE_STAR_SPAWN_SPAWNS.get()) {
            return;
        }
        int interval = ModConfig.STAR_SPAWN_SPAWN_INTERVAL_TICKS.get();
        if (event.getServer().getTickCount() % interval != 0) {
            return;
        }
        ServerLevel level = event.getServer().getLevel(Level.OVERWORLD);
        if (level == null) {
            return;
        }
        ServerChunkCache cache = level.getChunkSource();
        int perPass = ModConfig.STAR_SPAWN_SPAWN_COUNT.get();
        int tick = event.getServer().getTickCount();
        for (ServerPlayer player : level.players()) {
            BestiarySupport.topUp(level, cache, player,
                    ModEntities.STAR_SPAWN.get(), StarSpawn.class,
                    ModConfig.STAR_SPAWN_SPAWN_RADIUS.get(),
                    ModConfig.STAR_SPAWN_SPAWN_CAP.get(), perPass, tick,
                    (lvl, spot, dark, tainted) -> dark || tainted);
        }
    }
}
