package com.sanchous98.eldritchhorror.world.town;

/**
 * Pure siting arithmetic for the generic towns: footprint radius, curated-city overlap and the
 * land test. No Minecraft types and no config, so it loads in a bare JVM and is unit-tested.
 *
 * <p>The centre-water check takes an injected {@link Land} predicate so the rule can be tested
 * without the baked Earth layers.
 */
public final class TownSiting {

    /** Smallest footprint half-extent in blocks. */
    public static final int MIN_RADIUS = 40;
    /** Largest footprint half-extent in blocks. */
    public static final int MAX_RADIUS = 64;
    /** Population at which the footprint starts growing above {@link #MIN_RADIUS}. */
    private static final double BASE_POPULATION = 100_000.0;
    /** Blocks of radius added per decade of real population. */
    private static final double BLOCKS_PER_DECADE = 10.0;

    private TownSiting() {
    }

    /**
     * Footprint half-extent scaled from real population but clamped small: 40 blocks at ~100k
     * people, growing ~10 blocks per decade, capped at 64. A town is a few chunks at most.
     */
    public static int radiusFor(int population) {
        double decades = Math.log10(Math.max(population, 1) / BASE_POPULATION);
        long radius = Math.round(MIN_RADIUS + decades * BLOCKS_PER_DECADE);
        return Math.clamp(radius, MIN_RADIUS, MAX_RADIUS);
    }

    /**
     * True when two circles overlap, i.e. their centre distance is strictly less than the sum of
     * their radii. Used to keep a town off a curated city's district.
     */
    public static boolean intersects(int ax, int az, int aRadius, int bx, int bz, int bRadius) {
        long dx = (long) ax - bx;
        long dz = (long) az - bz;
        long sum = (long) aRadius + bRadius;
        return dx * dx + dz * dz < sum * sum;
    }

    /** A land predicate in world block coordinates. */
    @FunctionalInterface
    public interface Land {
        /** @return true when (x, z) is land rather than ocean */
        boolean isLand(int x, int z);
    }

    /**
     * The centre siting rule: a town is only built when its centre is land (the baked landmask),
     * so a coastal settlement whose real centre fell into the sea is skipped rather than drowned.
     */
    public static boolean centreOnLand(int x, int z, Land land) {
        return land.isLand(x, z);
    }
}
