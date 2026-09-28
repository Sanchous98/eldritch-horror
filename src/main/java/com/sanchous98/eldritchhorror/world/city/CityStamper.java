package com.sanchous98.eldritchhorror.world.city;

import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkAccess;

/**
 * Stamps any curated city that overlaps a chunk being generated. Called from the generator's
 * decoration step (not {@code buildTerrain}), so cities are built after terrain and are not
 * overwritten by it.
 *
 * <p>{@link CityBuilder} writes through {@link WorldGenLevel}, which only applies writes inside
 * the current chunk, so calling it once per overlapping chunk is safe and each chunk keeps only
 * its own part of the city.
 */
public final class CityStamper {

    private static final java.util.Set<String> LOGGED = java.util.concurrent.ConcurrentHashMap.newKeySet();

    private CityStamper() {
    }

    public static void stamp(WorldGenLevel level, ChunkAccess chunk) {
        ChunkPos cp = chunk.getPos();
        int minX = cp.getMinBlockX();
        int minZ = cp.getMinBlockZ();
        int maxX = minX + 15;
        int maxZ = minZ + 15;

        for (City city : Cities.all()) {
            int cull = city.radius() + 6; // wall/roofs extend slightly past the radius
            if (city.x() + cull < minX || city.x() - cull > maxX
                    || city.z() + cull < minZ || city.z() - cull > maxZ) {
                continue;
            }
            CityBuilder.build(level, city, cp);
            if (LOGGED.add(city.id())) {
                com.sanchous98.eldritchhorror.EldritchHorror.LOGGER.info(
                        "stamped city {} at ({}, {}), radius {}", city.name(), city.x(), city.z(),
                        city.radius());
            }
        }
    }
}
