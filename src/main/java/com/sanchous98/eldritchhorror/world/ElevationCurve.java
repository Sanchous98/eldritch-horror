package com.sanchous98.eldritchhorror.world;

/**
 * Maps real-world elevation (metres) to Minecraft Y on the Earth map.
 *
 * <p>A linear map of Earth's range (−11,000 m … +8,800 m) onto the build height would
 * flatten everything (Everest would reach only ~Y+96), so land is exaggerated more than
 * ocean: highlands read as high, and the abyss as deep.
 *
 * <p>Pure and Minecraft-independent so it can be unit-tested.
 */
public final class ElevationCurve {
    /** Minecraft sea level. */
    public static final int SEA_LEVEL = 63;
    /** Absolute floor and ceiling the curve is allowed to reach. */
    public static final int MIN_Y = -64;
    public static final int MAX_Y = 320;

    private ElevationCurve() {
    }

    /**
     * @param metres real elevation (negative = below sea level)
     * @return Minecraft Y for the surface at that column
     */
    public static int toY(double metres) {
        double y;
        if (metres >= 0) {
            // 0 m -> sea level; +8,800 m (Everest) -> ~285
            y = SEA_LEVEL + clamp(metres / 8800.0, 0.0, 1.0) * (285 - SEA_LEVEL);
        } else {
            // 0 m -> sea level; -11,000 m (trench) -> ~-40; -4,000 m (ocean floor) -> ~25
            y = SEA_LEVEL + clamp(metres / 11000.0, -1.0, 0.0) * (SEA_LEVEL + 40);
        }
        return (int) clamp(Math.round(y), MIN_Y, MAX_Y);
    }

    /** Pure (no Minecraft types) so it stays unit-testable. */
    private static double clamp(double v, double lo, double hi) {
        return v < lo ? lo : (v > hi ? hi : v);
    }
}
