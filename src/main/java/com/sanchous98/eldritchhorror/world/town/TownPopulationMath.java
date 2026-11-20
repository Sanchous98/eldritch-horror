package com.sanchous98.eldritchhorror.world.town;

/**
 * Pure rules for {@link TownPopulation}: when a pass is due, how many villagers to add, where
 * around the plaza they stand, which trade they get and when a tainted town counts as abandoned.
 *
 * <p>No Minecraft types and no shared RNG: every decision is a hash of the town id and a counter,
 * so a pass is reproducible and two towns never draw from the same stream. Unit-tested.
 */
public final class TownPopulationMath {

    /** A town is considered when its centre is this close to a player, in blocks. */
    public static final int NEAR_PLAYER = 128;
    /** Villagers are counted (and the spawn ring kept) inside this radius of the town centre. */
    public static final int COUNT_RADIUS = 48;
    /** Per-pass spawn cap; a town is topped up gently, never as a horde. */
    public static final int PER_PASS = 2;
    /** Candidate offsets tried per villager, since unloaded or wet columns are skipped. */
    public static final int PROBE_FACTOR = 8;
    /** Closest a spawn may be to the well, so villagers do not stand in the plaza furniture. */
    private static final int MIN_SPAWN_DISTANCE = 12;

    private TownPopulationMath() {
    }

    /**
     * True when a population pass should run: the interval is positive and the server tick is a
     * multiple of it.
     */
    public static boolean due(int tick, int interval) {
        return interval > 0 && tick % interval == 0;
    }

    /**
     * Villagers to spawn this pass: enough to reach {@code cap}, but never more than
     * {@code perPass}. Zero when the town is full or either bound is disabled.
     */
    public static int budget(int present, int cap, int perPass) {
        if (cap <= 0 || perPass <= 0) {
            return 0;
        }
        return Math.clamp(cap - present, 0, perPass);
    }

    /**
     * True when a town's centre taint makes it read as abandoned. A threshold of {@code 0}
     * disables the gate; otherwise taint at or above the threshold stops all top-ups.
     */
    public static boolean abandoned(double taint, double threshold) {
        return threshold > 0.0 && taint >= threshold;
    }

    /**
     * The trade for the {@code ordinal}-th spawn of a pass. The town's hash rotates the pool, so
     * a full town of five gets one of each trade instead of five farmers; the same town and slot
     * always pick the same trade.
     */
    public static TownProfession profession(int seed, int ordinal) {
        int rotation = Math.floorMod(mix(seed, 0), TownProfession.POOL.length);
        return TownProfession.POOL[Math.floorMod(rotation + ordinal, TownProfession.POOL.length)];
    }

    /**
     * A deterministic spawn offset from the town centre, in blocks: a point on a ring between
     * {@value #MIN_SPAWN_DISTANCE} and 70% of the footprint radius (around the plaza and houses,
     * inside the town). Returned as {@code {dx, dz}}.
     *
     * @param seed    the town's hash mixed with the pass
     * @param attempt candidate index within the pass
     * @param radius  the town's footprint half-extent in blocks
     */
    public static int[] spotOffset(int seed, int attempt, int radius) {
        int h = mix(seed, attempt);
        int max = Math.max(MIN_SPAWN_DISTANCE, radius * 7 / 10);
        int distance = MIN_SPAWN_DISTANCE + Math.floorMod(h, max - MIN_SPAWN_DISTANCE + 1);
        double angle = (h >>> 8) * (Math.PI * 2.0 / (1 << 24));
        return new int[] {
                (int) Math.round(Math.cos(angle) * distance),
                (int) Math.round(Math.sin(angle) * distance),
        };
    }

    /** Stable per-attempt hash in the style of {@link TownLocation}'s position mix. */
    private static int mix(int seed, int n) {
        int h = seed ^ (n * 0x9E3779B9);
        h ^= h >>> 16;
        h *= 0x7FEB352D;
        h ^= h >>> 15;
        h *= 0x846CA68B;
        h ^= h >>> 16;
        return h;
    }
}
