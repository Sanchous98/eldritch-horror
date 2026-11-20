package com.sanchous98.eldritchhorror.endgame;

/**
 * The game-free arithmetic of the doom clock ({@code design/11-endgame.md}, "The doom clock", pinned
 * 2026-11). Kept apart from {@link DoomClock} so the meter's rate, the rift acceleration cap, the
 * warning ladder and the clamps can be asserted without a running server.
 *
 * <p>One unit of doom is one percent of the bar ({@code 0..100}). The passive rate is expressed per
 * game tick: the default {@code doomMinutesToLoss = 150} yields {@code 100 / (150 × 60 × 20)} per
 * tick, i.e. roughly +1 every 90 seconds.
 */
public final class DoomMath {

    /** The top of the meter: at this value the Choir wins. */
    public static final double MAX = 100.0;

    /** How many open rifts the acceleration counts at most (design/11). */
    public static final int MAX_ACCELERATED_RIFTS = 8;

    /** Upper bound on the rift multiplier, so a field of rifts cannot skip the meter. */
    public static final double MAX_RIFT_MULTIPLIER = 2.0;

    /** The once-only warning ladder (design/11: "warnings at 50 / 70 / 85 / 95"). */
    private static final int[] WARNINGS = {50, 70, 85, 95};

    private DoomMath() {
    }

    /**
     * The base passive rise per game tick for a world that must reach {@link #MAX} in
     * {@code minutesToLoss} minutes. A non-positive duration means "no rise".
     */
    public static double risePerTick(double minutesToLoss) {
        if (minutesToLoss <= 0.0) {
            return 0.0;
        }
        return MAX / (minutesToLoss * 60.0 * 20.0);
    }

    /**
     * The passive-rate multiplier from open rifts: {@code 1 + acceleration × min(rifts, 8)}, capped
     * at {@link #MAX_RIFT_MULTIPLIER} so the cap holds even at the maximum config acceleration.
     */
    public static double riftMultiplier(int openRifts, double acceleration, double cap) {
        int counted = Math.clamp(openRifts, 0, MAX_ACCELERATED_RIFTS);
        return Math.min(cap, 1.0 + acceleration * counted);
    }

    /** The fully-multiplied passive rise per game tick. */
    public static double risePerTick(double minutesToLoss, int openRifts, double acceleration,
                                     double cap) {
        return risePerTick(minutesToLoss) * riftMultiplier(openRifts, acceleration, cap);
    }

    /** Clamps a doom value into {@code [0, 100]}. */
    public static double clamp(double doom) {
        return Math.clamp(doom, 0.0, MAX);
    }

    /** Adds a signed delta to a doom value and clamps. */
    public static double add(double doom, double delta) {
        return clamp(doom + delta);
    }

    /**
     * The lowest warning threshold the meter has reached but the mask has not recorded, or {@code 0}
     * when there is nothing new to announce. Warning {@code 50} is bit 0, {@code 70} bit 1, and so on.
     */
    public static int nextWarning(double doom, int mask) {
        for (int i = 0; i < WARNINGS.length; i++) {
            if (doom >= WARNINGS[i] && (mask & (1 << i)) == 0) {
                return WARNINGS[i];
            }
        }
        return 0;
    }

    /** Records a warning threshold in the mask; an unknown threshold leaves the mask unchanged. */
    public static int markWarning(int mask, int threshold) {
        for (int i = 0; i < WARNINGS.length; i++) {
            if (WARNINGS[i] == threshold) {
                return mask | (1 << i);
            }
        }
        return mask;
    }

    /** @return whether the meter has reached the awakening fraction ({@code 0.70} by default). */
    public static boolean awakened(double doom, double awakenFraction) {
        return doom >= MAX * Math.clamp(awakenFraction, 0.0, 1.0);
    }

    /** @return whether the meter is full (the Choir's loss). */
    public static boolean lost(double doom) {
        return doom >= MAX;
    }
}
