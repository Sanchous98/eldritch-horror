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
 * Bounded spawner for {@link DrownedThrall}. Reuses the shared {@link BestiarySupport#topUp} loop;
 * the only per-mob parts are the config bounds and the gate. Its attachment is the <b>coast</b>: the
 * gate requires water within a few blocks of a valid standing spot, so it appears at the tide line
 * and never spawns inland (the "spawns attach to the world" rule). Darkness is not required — it is
 * a day-roaming coastal threat. Loaded chunks only, deterministic sampling.
 */
@EventBusSubscriber(modid = EldritchHorror.MODID)
public final class DrownedThrallSpawner {

    private DrownedThrallSpawner() {
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (!ModConfig.ENABLE_DROWNED_THRALL_SPAWNS.get()) {
            return;
        }
        int interval = ModConfig.DROWNED_THRALL_SPAWN_INTERVAL_TICKS.get();
        if (event.getServer().getTickCount() % interval != 0) {
            return;
        }
        ServerLevel level = event.getServer().getLevel(Level.OVERWORLD);
        if (level == null) {
            return;
        }
        ServerChunkCache cache = level.getChunkSource();
        int perPass = ModConfig.DROWNED_THRALL_SPAWN_COUNT.get();
        int tick = event.getServer().getTickCount();
        for (ServerPlayer player : level.players()) {
            BestiarySupport.topUp(level, cache, player,
                    ModEntities.DROWNED_THRALL.get(), DrownedThrall.class,
                    ModConfig.DROWNED_THRALL_SPAWN_RADIUS.get(),
                    ModConfig.DROWNED_THRALL_SPAWN_CAP.get(), perPass, tick,
                    (lvl, spot, dark, tainted) -> BestiarySupport.nearWater(lvl, spot));
        }
    }
}
