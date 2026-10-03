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
 * Bounded spawner for {@link RisenHusk}. Vanilla mobs are removed ({@code world/MobSuppressor}), and
 * mod entities are always allowed by that suppressor (namespace check), so this pass is what makes
 * the husk appear in the world. It mirrors the established {@code world/CityPopulation} /
 * {@code corruption/TaintWorld} shape: server-authoritative, overworld-only, and it only ever
 * touches already-loaded chunks.
 *
 * <p>Every {@code RISEN_HUSK_SPAWN_INTERVAL_TICKS} (config; default 10 s) each player is topped up
 * towards a per-player cap. Husks spawn on a valid surface spot in the dark (vanilla monster
 * darkness test) or in a chunk whose taint is above the configured spread threshold — the
 * "dark/tainted/wild" attachment the design asks for. Counts, radii and caps are all config-bounded.
 *
 * <p>The sampling loop itself lives in the shared {@link BestiarySupport#topUp}; this class supplies
 * only the husk's type, gate and config, so a fix to the shared loop reaches every spawner.
 */
@EventBusSubscriber(modid = EldritchHorror.MODID)
public final class RisenHuskSpawner {

    private RisenHuskSpawner() {
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (!ModConfig.ENABLE_RISEN_HUSK_SPAWNS.get()) {
            return;
        }
        int interval = ModConfig.RISEN_HUSK_SPAWN_INTERVAL_TICKS.get();
        if (event.getServer().getTickCount() % interval != 0) {
            return;
        }
        ServerLevel level = event.getServer().getLevel(Level.OVERWORLD);
        if (level == null) {
            return;
        }
        int perPass = ModConfig.RISEN_HUSK_SPAWN_COUNT.get();
        if (perPass <= 0) {
            return;
        }
        ServerChunkCache cache = level.getChunkSource();
        int tick = event.getServer().getTickCount();
        int radius = ModConfig.RISEN_HUSK_SPAWN_RADIUS.get();
        int cap = ModConfig.RISEN_HUSK_SPAWN_CAP.get();
        for (ServerPlayer player : level.players()) {
            BestiarySupport.topUp(level, cache, player, ModEntities.RISEN_HUSK.get(), RisenHusk.class,
                    radius, cap, perPass, tick, (lvl, spot, dark, tainted) -> dark || tainted);
        }
    }
}
