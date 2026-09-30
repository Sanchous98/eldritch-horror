package com.sanchous98.eldritchhorror.corruption;

import com.sanchous98.eldritchhorror.core.ModConfig;

/**
 * The corruption state machine: four bands over the {@code [0, DEFAULT_MAX]} meter. Effects fire
 * when a transition changes the band, never every tick (see
 * {@code design/27-systems-framework.md}).
 *
 * <p>The cut-offs are fractions of the ceiling and are config-driven; their defaults are the bands
 * described in the design ({@code <0.25} dormant … {@code >=0.85} claimed).
 */
public enum CorruptionState {
    /** Clean: no penalty. */
    DORMANT,
    /** Touched: the first mark of exposure, still no effect. */
    TOUCHED,
    /** Marked: the {@code corrupted} effect takes hold. */
    MARKED,
    /** Claimed: the horror owns you; a stronger {@code corrupted} effect. */
    CLAIMED;

    /** Band of {@code corruption}, using the configured cut-offs. */
    public static CorruptionState of(double corruption) {
        double max = CorruptionSystem.DEFAULT_MAX;
        double f = max <= 0.0 ? 0.0 : Math.clamp(corruption / max, 0.0, 1.0);
        if (f >= ModConfig.CORRUPTION_CLAIMED.get()) {
            return CLAIMED;
        }
        if (f >= ModConfig.CORRUPTION_MARKED.get()) {
            return MARKED;
        }
        if (f >= ModConfig.CORRUPTION_TOUCHED.get()) {
            return TOUCHED;
        }
        return DORMANT;
    }
}
