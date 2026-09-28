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
            y = seaToPeak(metres);
        } else {
            y = seaToTrench(metres);
        }
        int rounded = (int) Math.round(y);
        return Math.max(MIN_Y, Math.min(MAX_Y, rounded));
    }

    /** 0 m → {@link #SEA_LEVEL}; +8,800 m → ~285. Slightly super-linear so peaks stand out. */
    private static double seaToPeak(double m) {
        double t = m / 8800.0;            // 0..1
        double eased = Math.pow(t, 1.15); // exaggerate the top
        return SEA_LEVEL + eased * (285 - SEA_LEVEL);
    }

    /** 0 m → {@link #SEA_LEVEL}; −11,000 m → ~−40. Ocean floors quickly, then tapers. */
    private static double seaToTrench(double m) {
        double d = -m;                    // 0..11000
        double t = Math.log1p(d / 400.0) / Math.log1p(11000.0 / 400.0); // 0..1, fast at first
        return SEA_LEVEL + t * (-40 - SEA_LEVEL);
    }
}
