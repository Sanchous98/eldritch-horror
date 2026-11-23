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
 * Server-side city condition, from the corruption that has settled on a curated city's district
 * ({@code design/21-settlements.md}).
 *
 * <p>The authoritative value is the world-level {@link CityLedger}, which keeps decaying even when
 * no chunk is loaded; {@link #sampledTaint} reads the physical per-chunk taint of the nine district
 * samples and is folded into the ledger by {@code CityDecay}. Sampling stays bounded: only
 * already-loaded chunks ({@code ServerChunkCache.getChunkNow}), never a load or a generation.
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

    /** @return {@code city}'s current state from the authoritative ledger (never force-loads). */
    public static CityState stateOf(ServerLevel level, City city) {
        return CityLedger.of(level).state(city.id());
    }

    /**
     * Average taint of the district's loaded sample chunks.
     *
     * @return {@code 0..1}, or {@code -1} when none of the nine samples is loaded
     */
    public static double sampledTaint(ServerLevel level, City city) {
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
        return samples == 0 ? -1.0 : sum / samples;
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
