package com.sanchous98.eldritchhorror.endgame;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Guards the game-free doom arithmetic ({@code design/11-endgame.md}, "The doom clock", pinned
 * 2026-11): the passive rate, the capped rift acceleration, the once-only warning ladder and the
 * clamps. {@link DoomMath} touches no Minecraft type, so no server is needed.
 */
class DoomMathTest {

    private static final float EPSILON = 1.0E-6F;

    @Test
    void passiveRateReachesOneHundredInTheConfiguredMinutes() {
        double perTick = DoomMath.risePerTick(150.0);
        assertEquals(100.0, perTick * 150.0 * 60.0 * 20.0, EPSILON);
    }

    @Test
    void defaultRateIsRoughlyOnePointPerNinetySeconds() {
        double perTick = DoomMath.risePerTick(150.0);
        assertEquals(1.0, perTick * 90.0 * 20.0, 1.0E-4);
    }

    @Test
    void noDurationMeansNoRise() {
        assertEquals(0.0, DoomMath.risePerTick(0.0), EPSILON);
        assertEquals(0.0, DoomMath.risePerTick(-5.0), EPSILON);
    }

    @Test
    void riftMultiplierCountsAtMostEightRifts() {
        assertEquals(1.0, DoomMath.riftMultiplier(0, 0.15, 2.0), EPSILON);
        assertEquals(1.6, DoomMath.riftMultiplier(4, 0.15, 2.0), EPSILON);
        assertEquals(2.0, DoomMath.riftMultiplier(8, 0.15, 2.0), 1.0E-4);
        assertEquals(2.0, DoomMath.riftMultiplier(40, 0.15, 2.0), 1.0E-4);
    }

    @Test
    void riftMultiplierIsCapped() {
        assertEquals(2.0, DoomMath.riftMultiplier(8, 0.15, 2.0), EPSILON);
        assertEquals(1.0, DoomMath.riftMultiplier(-3, 0.15, 2.0), EPSILON);
    }

    @Test
    void riftAccelerationMultipliesTheBaseRate() {
        double base = DoomMath.risePerTick(150.0);
        double accelerated = DoomMath.risePerTick(150.0, 4, 0.15, 2.0);
        assertEquals(base * 1.6, accelerated, EPSILON);
    }

    @Test
    void addAndClampStayInsideTheMeter() {
        assertEquals(0.0, DoomMath.add(-5.0, 0.0), EPSILON);
        assertEquals(0.0, DoomMath.add(2.0, -6.0), EPSILON);
        assertEquals(100.0, DoomMath.add(98.0, 6.0), EPSILON);
        assertEquals(39.5, DoomMath.add(42.0, -2.5), EPSILON);
    }

    @Test
    void warningsFireInOrderAndOnlyOnce() {
        int mask = 0;
        assertEquals(50, DoomMath.nextWarning(51.0, mask));
        mask = DoomMath.markWarning(mask, 50);
        assertEquals(70, DoomMath.nextWarning(71.0, mask));
        mask = DoomMath.markWarning(mask, 70);
        assertEquals(85, DoomMath.nextWarning(86.0, mask));
        mask = DoomMath.markWarning(mask, 85);
        assertEquals(95, DoomMath.nextWarning(99.0, mask));
        mask = DoomMath.markWarning(mask, 95);
        assertEquals(0, DoomMath.nextWarning(100.0, mask));
    }

    @Test
    void aJumpedMeterAnnouncesEveryCrossedWarning() {
        int mask = 0;
        assertEquals(50, DoomMath.nextWarning(96.0, mask));
        mask = DoomMath.markWarning(mask, 50);
        assertEquals(70, DoomMath.nextWarning(96.0, mask));
        mask = DoomMath.markWarning(mask, 70);
        assertEquals(85, DoomMath.nextWarning(96.0, mask));
        mask = DoomMath.markWarning(mask, 85);
        assertEquals(95, DoomMath.nextWarning(96.0, mask));
    }

    @Test
    void belowTheFirstWarningNothingFires() {
        assertEquals(0, DoomMath.nextWarning(49.9, 0));
        assertEquals(0, DoomMath.markWarning(0, 42));
    }

    @Test
    void awakeningAndLossUseTheConfiguredFraction() {
        assertFalse(DoomMath.awakened(69.9, 0.70));
        assertTrue(DoomMath.awakened(70.0, 0.70));
        assertTrue(DoomMath.awakened(90.0, 0.0));
        assertTrue(DoomMath.awakened(100.0, 1.5));
        assertFalse(DoomMath.lost(99.99));
        assertTrue(DoomMath.lost(100.0));
    }
}
