package com.sanchous98.eldritchhorror.world.ward;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Guards the game-free ward arithmetic (design/26, "Warding and sealing"): circular coverage with an
 * inclusive boundary, expiry that never reports a negative remain, and the boss damage window's
 * clamp. {@link WardMath} touches no Minecraft type, so no server is needed.
 */
class WardMathTest {

    private static final float EPSILON = 1.0E-4F;

    @Test
    void coversInsideTheRing() {
        assertTrue(WardMath.covers(0.0, 0.0, 12.0));
        assertTrue(WardMath.covers(3.0, 4.0, 6.0));
    }

    @Test
    void refusesOutsideTheRing() {
        assertFalse(WardMath.covers(12.1, 0.0, 12.0));
        assertFalse(WardMath.covers(9.0, 9.0, 12.0));
    }

    @Test
    void theBoundaryIsPartOfTheWard() {
        assertTrue(WardMath.covers(12.0, 0.0, 12.0));
        assertTrue(WardMath.covers(0.0, -12.0, 12.0));
    }

    @Test
    void aNegativeRadiusCoversNothing() {
        assertFalse(WardMath.covers(0.0, 0.0, -1.0));
    }

    @Test
    void remainingTicksCountsDownAndFloorsAtZero() {
        assertEquals(200L, WardMath.remainingTicks(100L, 300L));
        assertEquals(0L, WardMath.remainingTicks(300L, 300L));
        assertEquals(0L, WardMath.remainingTicks(400L, 300L));
    }

    @Test
    void expiryHandlesPastExactAndNever() {
        assertTrue(WardMath.expired(400L, 300L));
        assertTrue(WardMath.expired(300L, 300L));
        assertFalse(WardMath.expired(100L, 300L));
        assertFalse(WardMath.expired(100L, Long.MAX_VALUE));
        assertEquals(Long.MAX_VALUE - 100L, WardMath.remainingTicks(100L, Long.MAX_VALUE));
    }

    @Test
    void bossBonusScalesTheBaseDamage() {
        assertEquals(12.5F, WardMath.scaledBossDamage(10.0F, 0.25), EPSILON);
    }

    @Test
    void bossBonusClampsToTheAcceptedRange() {
        assertEquals(10.0F, WardMath.scaledBossDamage(10.0F, -0.5), EPSILON);
        assertEquals(20.0F, WardMath.scaledBossDamage(10.0F, 5.0), EPSILON);
    }

    @Test
    void zeroAndNegativeBaseDamageStayZero() {
        assertEquals(0.0F, WardMath.scaledBossDamage(0.0F, 0.25), EPSILON);
        assertEquals(0.0F, WardMath.scaledBossDamage(-4.0F, 0.25), EPSILON);
    }
}
