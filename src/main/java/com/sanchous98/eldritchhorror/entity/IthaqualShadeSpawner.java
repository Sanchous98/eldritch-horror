package com.sanchous98.eldritchhorror.entity;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.core.ModConfig;
import com.sanchous98.eldritchhorror.registry.ModEntities;
import com.sanchous98.eldritchhorror.world.BoundaryTravel;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Bounded spawner for {@link IthaqualShade}. The design places it "near the cold edge and deep
 * places", so it has two bounded passes and no site trigger:
 *
 * <ul>
 *   <li><b>Polar edge</b> — untainted loaded ground inside {@code ithaqualShadeEdgeBand} blocks of
 *       {@link BoundaryTravel#morokEdgeZ()} (the clamped end of the world), where Ithaqua also
 *       walks;</li>
 *   <li><b>Deep places</b> — the shared {@link BestiarySupport#topUpUnderground} pass below
 *       {@code ithaqualShadeDeepMaxY}, dark-only, exactly as Nyogtha is placed.</li>
 * </ul>
 *
 * <p>Both passes share the same per-player cap (the shared loops count the type near the player),
 * are server-authoritative, overworld-only, loaded chunks only, deterministic, and config-bounded.
 */
@EventBusSubscriber(modid = EldritchHorror.MODID)
public final class IthaqualShadeSpawner {

    private IthaqualShadeSpawner() {
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (!ModConfig.ENABLE_ITHAQUAL_SHADE_SPAWNS.get()) {
            return;
        }
        int interval = ModConfig.ITHAQUAL_SHADE_SPAWN_INTERVAL_TICKS.get();
        if (event.getServer().getTickCount() % interval != 0) {
            return;
        }
        ServerLevel level = event.getServer().getLevel(Level.OVERWORLD);
        if (level == null) {
            return;
        }
        ServerChunkCache cache = level.getChunkSource();
        int tick = event.getServer().getTickCount();
        int radius = ModConfig.ITHAQUAL_SHADE_SPAWN_RADIUS.get();
        int cap = ModConfig.ITHAQUAL_SHADE_SPAWN_CAP.get();
        int maxY = ModConfig.ITHAQUAL_SHADE_DEEP_MAX_Y.get();
        for (ServerPlayer player : level.players()) {
            BestiarySupport.topUp(level, cache, player, ModEntities.ITHAQUAL_SHADE.get(),
                    IthaqualShade.class, radius, cap, 1, tick,
                    (lvl, spot, dark, tainted) -> !tainted && atPolarEdge(spot));
            BestiarySupport.topUpUnderground(level, cache, player, ModEntities.ITHAQUAL_SHADE.get(),
                    IthaqualShade.class, radius, cap, 1, maxY, tick,
                    (lvl, spot, dark, tainted) -> dark);
        }
    }

    /** Whether {@code spot} lies inside the polar band that ends at the charted edge. */
    private static boolean atPolarEdge(BlockPos spot) {
        int band = ModConfig.ITHAQUAL_SHADE_EDGE_BAND.get();
        return Math.abs(spot.getZ()) >= BoundaryTravel.morokEdgeZ() - band;
    }
}
