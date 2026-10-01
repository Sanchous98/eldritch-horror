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
 * Ambient spawner for {@link ShoggothMass}. Reuses the shared {@link BestiarySupport#topUp} loop, but
 * is <b>OFF by default</b> ({@code enableShoggothSpawns = false}): a shoggoth mass is a rite/event
 * minion, not a wandering one. Turning the toggle on gives it a tainted-only gate on the same
 * loaded-chunks-only, deterministic contract.
 */
@EventBusSubscriber(modid = EldritchHorror.MODID)
public final class ShoggothSpawner {

    private ShoggothSpawner() {
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (!ModConfig.ENABLE_SHOGGOTH_SPAWNS.get()) {
            return;
        }
        int interval = ModConfig.SHOGGOTH_SPAWN_INTERVAL_TICKS.get();
        if (event.getServer().getTickCount() % interval != 0) {
            return;
        }
        ServerLevel level = event.getServer().getLevel(Level.OVERWORLD);
        if (level == null) {
            return;
        }
        ServerChunkCache cache = level.getChunkSource();
        int perPass = ModConfig.SHOGGOTH_SPAWN_COUNT.get();
        int tick = event.getServer().getTickCount();
        for (ServerPlayer player : level.players()) {
            BestiarySupport.topUp(level, cache, player,
                    ModEntities.SHOGGOTH_MASS.get(), ShoggothMass.class,
                    ModConfig.SHOGGOTH_SPAWN_RADIUS.get(),
                    ModConfig.SHOGGOTH_SPAWN_CAP.get(), perPass, tick,
                    (lvl, spot, dark, tainted) -> tainted);
        }
    }
}
