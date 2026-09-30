package com.sanchous98.eldritchhorror.sanity;

import com.sanchous98.eldritchhorror.core.ModConfig;

/**
 * The sanity state machine: five bands over the {@code [0, max]} meter. Effects fire when a
 * transition changes the band, never every tick (see {@code design/27-systems-framework.md}).
 *
 * <p>The cut-offs are fractions of max and are config-driven; their defaults are the bands
 * described in the design ({@code >0.75} composed … {@code 0} marked).
 */
public enum SanityState {
    /** Calm: no penalty. */
    COMPOSED,
    /** A creeping unease: still no effect, but the meter is visibly low. */
    UNEASY,
    /** Fraying: a light madness penalty. */
    FRAYING,
    /** Breaking: a stronger madness penalty. */
    BREAKING,
    /** Marked: the horror has noticed you. */
    MARKED;

    /**
     * Band of {@code sanity} against a meter ceiling of {@code max}, using the configured cut-offs.
     * The ceiling is the player's {@code max_sanity} attribute, so the bands track that value.
     */
    public static SanityState of(double sanity, double max) {
        double f = max <= 0.0 ? 0.0 : Math.clamp(sanity / max, 0.0, 1.0);
        if (f > ModConfig.SANITY_UNEASY.get()) {
            return COMPOSED;
        }
        if (f > ModConfig.SANITY_FRAYING.get()) {
            return UNEASY;
        }
        if (f > ModConfig.SANITY_BREAKING.get()) {
            return FRAYING;
        }
        if (f > 0.0) {
            return BREAKING;
        }
        return MARKED;
    }
}
