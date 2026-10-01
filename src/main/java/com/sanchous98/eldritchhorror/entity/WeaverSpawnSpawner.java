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
 * Bounded spawner for {@link WeaverSpawn}. Reuses the shared {@link BestiarySupport#topUp} loop; the
 * per-role gate is darkness or taint, like the veil stalker (the weaver takes the nest/cave niche,
 * the stalker the open ambush). Lowish cap: a nest has a few guards, not a horde.
 */
@EventBusSubscriber(modid = EldritchHorror.MODID)
public final class WeaverSpawnSpawner {

    private WeaverSpawnSpawner() {
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (!ModConfig.ENABLE_WEAVER_SPAWN_SPAWNS.get()) {
            return;
        }
        int interval = ModConfig.WEAVER_SPAWN_SPAWN_INTERVAL_TICKS.get();
        if (event.getServer().getTickCount() % interval != 0) {
            return;
        }
        ServerLevel level = event.getServer().getLevel(Level.OVERWORLD);
        if (level == null) {
            return;
        }
        ServerChunkCache cache = level.getChunkSource();
        int perPass = ModConfig.WEAVER_SPAWN_SPAWN_COUNT.get();
        int tick = event.getServer().getTickCount();
        for (ServerPlayer player : level.players()) {
            BestiarySupport.topUp(level, cache, player,
                    ModEntities.WEAVER_SPAWN.get(), WeaverSpawn.class,
                    ModConfig.WEAVER_SPAWN_SPAWN_RADIUS.get(),
                    ModConfig.WEAVER_SPAWN_SPAWN_CAP.get(), perPass, tick,
                    (lvl, spot, dark, tainted) -> dark || tainted);
        }
    }
}
