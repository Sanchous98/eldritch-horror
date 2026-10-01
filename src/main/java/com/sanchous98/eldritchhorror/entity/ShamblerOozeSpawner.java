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
 * Bounded spawner for {@link ShamblerOoze}. Reuses the shared {@link BestiarySupport#topUp} loop; the
 * per-role gate is darkness or taint. It is never a horde itself — the split makes one sighting
 * become several — so the cap is modest.
 */
@EventBusSubscriber(modid = EldritchHorror.MODID)
public final class ShamblerOozeSpawner {

    private ShamblerOozeSpawner() {
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (!ModConfig.ENABLE_SHAMBLER_OOZE_SPAWNS.get()) {
            return;
        }
        int interval = ModConfig.SHAMBLER_OOZE_SPAWN_INTERVAL_TICKS.get();
        if (event.getServer().getTickCount() % interval != 0) {
            return;
        }
        ServerLevel level = event.getServer().getLevel(Level.OVERWORLD);
        if (level == null) {
            return;
        }
        ServerChunkCache cache = level.getChunkSource();
        int perPass = ModConfig.SHAMBLER_OOZE_SPAWN_COUNT.get();
        int tick = event.getServer().getTickCount();
        for (ServerPlayer player : level.players()) {
            BestiarySupport.topUp(level, cache, player,
                    ModEntities.SHAMBLER_OOZE.get(), ShamblerOoze.class,
                    ModConfig.SHAMBLER_OOZE_SPAWN_RADIUS.get(),
                    ModConfig.SHAMBLER_OOZE_SPAWN_CAP.get(), perPass, tick,
                    (lvl, spot, dark, tainted) -> dark || tainted);
        }
    }
}
