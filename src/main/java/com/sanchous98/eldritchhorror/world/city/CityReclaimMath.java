package com.sanchous98.eldritchhorror.world.city;

/**
 * Game-free arithmetic behind reclaiming a Fallen city (design/21's open question: "whether a city
 * can be reclaimed after it Falls — an expensive rite / quest"). Kept free of Minecraft types so it
 * loads in a bare JVM; {@link CityReclaim} supplies the loaded chunks and the world state.
 *
 * <p>The rule is deliberately blunt and bounded: one reclaim multiplies the taint of every touched
 * district chunk by a configured factor ({@code reclaimTaintFactor}, default {@code 0.35}), which
 * removes ~65% and floors at zero. The nine chunks {@code CityStates} derives the district state
 * from are cleaned first, so a single rite always changes what the state reads; the rest of the
 * work budget goes to the nearest remaining chunks. A district pinned at the Fallen cut-off (default
 * {@code 0.55}) comes back as {@code 0.55 × 0.35 ≈ 0.19}, i.e. below Besieged ({@code 0.30}) and
 * into Uneasy; at full taint {@code 1.0 → 0.35}, still Besieged. It is a hard push back from the
 * brink, not a full restoration — a second rite or ordinary cleansing finishes the job.
 */
public final class CityReclaimMath {

    /** Work cap: the most chunks one reclaim touches (loaded chunks only, never force-loading). */
    public static final int MAX_CHUNKS = 256;

    /** Purge cap: the most Hollow Choir worshippers one reclaim removes. */
    public static final int MAX_PURGE = 32;

    /**
     * Chunk offsets CityStates reads a district's condition at: the centre plus a 3×3 spread whose
     * step is one extra chunk per 32 blocks of radius. Mirrored here (which is why it is pure and
     * tested) so a reclaim cleans exactly the chunks the derived state is computed from — otherwise
     * a bounded clean of the near district could leave the sampled far chunks tainted and the city
     * would report Fallen after the rite. Kept in sync with {@code CityStates.SAMPLE_CHUNK_OFFSETS}.
     */
    private static final int[][] STATE_SAMPLE_OFFSETS = {
            {0, 0}, {-1, -1}, {1, 1}, {-1, 1}, {1, -1}, {0, -2}, {2, 0}, {0, 2}, {-2, 0}};

    private CityReclaimMath() {
    }

    /**
     * The chunk offsets (from the city's centre chunk) that {@code CityStates} samples for
     * {@code radiusBlocks}: the centre plus the 3×3 spread. All are inside the district circle.
     *
     * @param radiusBlocks the city's district radius in blocks
     * @return nine offsets, centre first
     */
    public static int[][] stateSampleOffsets(int radiusBlocks) {
        int spread = Math.max(1, radiusBlocks >> 5);
        int[][] out = new int[STATE_SAMPLE_OFFSETS.length][2];
        for (int i = 0; i < STATE_SAMPLE_OFFSETS.length; i++) {
            out[i][0] = STATE_SAMPLE_OFFSETS[i][0] * spread;
            out[i][1] = STATE_SAMPLE_OFFSETS[i][1] * spread;
        }
        return out;
    }

    /**
     * The reclaim factor, clamped to {@code [0, 1]}: {@code 0} clears the taint outright, {@code 1}
     * changes nothing. A broken config value can never amplify taint.
     */
    public static double clampFactor(double factor) {
        return Math.clamp(factor, 0.0, 1.0);
    }

    /**
     * The taint of a chunk after one reclaim pass: {@code current × factor}, clamped to
     * {@code [0, 1]}. Zero stays zero, and a non-positive current is never made positive.
     *
     * @param current the chunk's taint before the pass
     * @param factor  the reclaim factor (see {@link #clampFactor})
     * @return the taint after the pass
     */
    public static double taintAfter(double current, double factor) {
        if (current <= 0.0) {
            return 0.0;
        }
        return Math.clamp(current * clampFactor(factor), 0.0, 1.0);
    }

    /** @return the width in chunks of the square that covers a district of {@code radiusBlocks}. */
    public static int districtSide(int radiusBlocks) {
        if (radiusBlocks < 0) {
            return 0;
        }
        return 2 * ((radiusBlocks + 15) / 16) + 1;
    }

    /** @return every chunk in the district square (uncapped), for diagnostics and the budget test. */
    public static int districtChunks(int radiusBlocks) {
        int side = districtSide(radiusBlocks);
        return side * side;
    }

    /**
     * The work budget for one reclaim: the district square, capped at {@link #MAX_CHUNKS}. The
     * largest curated city (radius 400) spans a {@code 51 × 51} square, so the cap keeps one rite
     * cheap and spreads the rest of a huge district over further casts.
     */
    public static int chunkBudget(int radiusBlocks) {
        return Math.min(MAX_CHUNKS, districtChunks(radiusBlocks));
    }

    /**
     * Whether a chunk offset {@code (chunkDx, chunkDz)} from the city's centre chunk lies in the
     * circular district. Uses the chunk's 16-block stride rather than its exact centre so the test
     * is a pure integer decision; a negative radius covers nothing.
     */
    public static boolean withinDistrict(int chunkDx, int chunkDz, int radiusBlocks) {
        if (radiusBlocks < 0) {
            return false;
        }
        double dx = chunkDx * 16.0;
        double dz = chunkDz * 16.0;
        double radius = radiusBlocks;
        return dx * dx + dz * dz <= radius * radius;
    }
}
