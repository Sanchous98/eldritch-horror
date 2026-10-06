package com.sanchous98.eldritchhorror.world.city;

import com.sanchous98.eldritchhorror.corruption.CorruptionSystem;
import com.sanchous98.eldritchhorror.world.loc.city.CityLocation;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jspecify.annotations.Nullable;

/**
 * Server-side, derived city states: the condition of each curated city, from the corruption that
 * has settled on its district ({@code design/21-settlements.md}). No state is stored — it is
 * recomputed on demand from the per-chunk taint field, so it can never drift from the world.
 *
 * <p>Bounded like every other world query here: only already-loaded chunks are sampled
 * ({@code ServerChunkCache.getChunkNow}), so asking for a city's state never generates a chunk. A
 * city with no loaded chunks reports {@link CityState#THRIVING} (nothing is known to be wrong).
 */
public final class CityStates {

    /**
     * Chunk sample offsets within the district (a 3×3 spread around the centre). Enough to read the
     * district without scanning it, and cheap enough to run per player per second.
     */
    private static final int[][] SAMPLE_CHUNK_OFFSETS = {
            {0, 0}, {-1, -1}, {1, 1}, {-1, 1}, {1, -1}, {0, -2}, {2, 0}, {0, 2}, {-2, 0}};

    private CityStates() {
    }

    /** @return {@code city}'s current state (recomputed from loaded-chunk taint; never force-loads). */
    public static CityState stateOf(ServerLevel level, City city) {
        int radius = new CityLocation(city).radius();
        int centreChunkX = city.x() >> 4;
        int centreChunkZ = city.z() >> 4;
        int spread = Math.max(1, radius >> 5); // ~1 extra chunk per 32 blocks of radius
        double sum = 0.0;
        int samples = 0;
        for (int[] offset : SAMPLE_CHUNK_OFFSETS) {
            ChunkPos pos = new ChunkPos(centreChunkX + offset[0] * spread,
                    centreChunkZ + offset[1] * spread);
            LevelChunk chunk = level.getChunkSource().getChunkNow(pos.x(), pos.z());
            if (chunk != null) {
                sum += CorruptionSystem.getTaint(chunk);
                samples++;
            }
        }
        return samples == 0 ? CityState.THRIVING : CityState.of(sum / samples);
    }

    /**
     * @return the nearest curated city whose district contains {@code level, pos}, or {@code null}
     * when the position is not inside any city.
     */
    public static @Nullable City nearestCity(ServerLevel level, BlockPos pos) {
        List<City> cities = Cities.all();
        double x = pos.getX() + 0.5;
        double z = pos.getZ() + 0.5;
        City nearest = null;
        double best = Double.MAX_VALUE;
        for (City city : cities) {
            double radius = new CityLocation(city).radius();
            double dx = x - city.x();
            double dz = z - city.z();
            double distanceSq = dx * dx + dz * dz;
            if (distanceSq <= radius * radius && distanceSq < best) {
                best = distanceSq;
                nearest = city;
            }
        }
        return nearest;
    }
}
