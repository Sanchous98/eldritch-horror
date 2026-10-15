package com.sanchous98.eldritchhorror.cult;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Regression guard for the reputation drift arithmetic ({@code design/03c-reputation.md}: "slow
 * drift toward 0 for inactive players"). {@link ReputationDecayMath} is deliberately free of game
 * state, so it can be asserted on without a running game.
 */
class ReputationDecayMathTest {

    /** One period: 5 s at 20 ticks — the fastest the config allows. */
    private static final long PERIOD = 100L;

    @Test
    void nothingBelowAFullPeriod() {
        assertEquals(0, ReputationDecayMath.decayPoints(40, 0L, PERIOD));
        assertEquals(0, ReputationDecayMath.decayPoints(40, PERIOD - 1, PERIOD));
    }

    @Test
    void onePointPerFullPeriod() {
        assertEquals(1, ReputationDecayMath.decayPoints(40, PERIOD, PERIOD));
        assertEquals(3, ReputationDecayMath.decayPoints(40, 3 * PERIOD + 7, PERIOD));
    }

    @Test
    void driftNeverCrossesZeroAndLeavesGrudgesAlone() {
        // A one-point standing takes a whole period's worth of drift but only that one point.
        assertEquals(1, ReputationDecayMath.decayPoints(1, 9 * PERIOD, PERIOD));
        // At 0 and below nothing happens, however long the inactivity.
        assertEquals(0, ReputationDecayMath.decayPoints(0, 9 * PERIOD, PERIOD));
        assertEquals(0, ReputationDecayMath.decayPoints(-40, 9 * PERIOD, PERIOD));
    }
}
