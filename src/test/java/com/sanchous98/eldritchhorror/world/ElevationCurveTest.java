package com.sanchous98.eldritchhorror.world;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Pure arithmetic of the elevation curve: sea level, both extremes, clamping and monotonicity. */
class ElevationCurveTest {

    @Test
    void seaLevelMapsToMinecraftSeaLevel() {
        assertEquals(ElevationCurve.SEA_LEVEL, ElevationCurve.toY(0));
    }

    @Test
    void extremesReachTheDocumentedCeilingAndFloor() {
        assertEquals(285, ElevationCurve.toY(8_800), "Everest's 8,800 m should reach Y 285");
        assertEquals(-40, ElevationCurve.toY(-11_000), "the trench's -11,000 m should reach Y -40");
    }

    @Test
    void valuesBeyondEarthsRangeAreClamped() {
        assertEquals(285, ElevationCurve.toY(50_000));
        assertEquals(-40, ElevationCurve.toY(-50_000));
    }

    @Test
    void outputStaysInsideTheAllowedBuildHeight() {
        for (double metres = -20_000; metres <= 20_000; metres += 137) {
            int y = ElevationCurve.toY(metres);
            assertTrue(y >= ElevationCurve.MIN_Y && y <= ElevationCurve.MAX_Y,
                    "Y " + y + " out of range for " + metres + " m");
        }
    }

    @Test
    void higherGroundIsNeverLower() {
        int previous = Integer.MIN_VALUE;
        for (double metres = -12_000; metres <= 9_000; metres += 25) {
            int y = ElevationCurve.toY(metres);
            assertTrue(y >= previous, "curve must be monotonic at " + metres + " m");
            previous = y;
        }
    }
}
