package com.sanchous98.eldritchhorror.entity;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.core.ModConfig;
import com.sanchous98.eldritchhorror.registry.ModEntities;
import com.sanchous98.eldritchhorror.sanity.SanityAPI;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Bounded spawner for {@link NightHag}. It is the design's "swoops at low sanity, not low sleep":
 * before each pass it checks the nearby player's sanity through {@link SanityAPI} and only places a
 * hag when that player is at or below {@code nightHagSanityThreshold} of their maximum. Flight uses
 * the shared {@link BestiarySupport#topUpAir} helper; the rest is the common context gate (dark).
 * It never force-loads.
 */
@EventBusSubscriber(modid = EldritchHorror.MODID)
public final class NightHagSpawner {

    private NightHagSpawner() {
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (!ModConfig.ENABLE_NIGHT_HAG_SPAWNS.get()) {
            return;
        }
        int interval = ModConfig.NIGHT_HAG_SPAWN_INTERVAL_TICKS.get();
        int tick = event.getServer().getTickCount();
        if (tick % interval != 0) {
            return;
        }
        ServerLevel level = event.getServer().getLevel(Level.OVERWORLD);
        if (level == null) {
            return;
        }
        ServerChunkCache cache = level.getChunkSource();
        int perPass = ModConfig.NIGHT_HAG_SPAWN_COUNT.get();
        for (ServerPlayer player : level.players()) {
            double max = SanityAPI.max(player);
            if (max <= 0.0 || SanityAPI.get(player) > max * ModConfig.NIGHT_HAG_SANITY_THRESHOLD.get()) {
                continue; // it only comes to the frayed
            }
            BestiarySupport.topUpAir(level, cache, player,
                    ModEntities.NIGHT_HAG.get(), NightHag.class,
                    ModConfig.NIGHT_HAG_SPAWN_RADIUS.get(),
                    ModConfig.NIGHT_HAG_SPAWN_CAP.get(), perPass,
                    ModConfig.NIGHT_HAG_SPAWN_MIN_Y.get(), tick,
                    (lvl, spot, dark) -> dark);
        }
    }
}
