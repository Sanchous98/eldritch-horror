package com.sanchous98.eldritchhorror.world.city;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Guards the game-free reclaim arithmetic (design/21): the taint factor, the work budget and the
 * district selection. {@link CityReclaimMath} imports no Minecraft types, so it loads in a bare JVM;
 * the default factor's promise — a Fallen district comes back below the Fallen cut-off — is checked
 * against the documented default thresholds (0.55 Fallen, 0.30 Besieged), not against config.
 */
class CityReclaimMathTest {

    /** Default factor from {@code ModConfig.RECLAIM_TAINT_FACTOR}. */
    private static final double FACTOR = 0.35;
    /** Default {@code cityStateFallenTaint}. */
    private static final double FALLEN_THRESHOLD = 0.55;
    /** Default {@code cityStateBesiegedTaint}. */
    private static final double BESIEGED_THRESHOLD = 0.30;
    private static final double EPSILON = 1.0E-9;

    @Test
    void zeroTaintStaysZero() {
        assertEquals(0.0, CityReclaimMath.taintAfter(0.0, FACTOR), EPSILON);
        assertEquals(0.0, CityReclaimMath.taintAfter(-0.2, FACTOR), EPSILON);
    }

    @Test
    void factorZeroClearsTheTaint() {
        assertEquals(0.0, CityReclaimMath.taintAfter(0.9, 0.0), EPSILON);
        assertEquals(0.0, CityReclaimMath.taintAfter(1.0, 0.0), EPSILON);
    }

    @Test
    void factorOneChangesNothing() {
        assertEquals(0.42, CityReclaimMath.taintAfter(0.42, 1.0), EPSILON);
    }

    @Test
    void theResultIsClampedAndFactorsAreSane() {
        assertEquals(1.0, CityReclaimMath.taintAfter(10.0, 1.0), EPSILON);
        assertEquals(0.9, CityReclaimMath.taintAfter(0.9, 2.0), EPSILON); // a broken factor never amplifies
        assertEquals(0.0, CityReclaimMath.taintAfter(0.9, -0.5), EPSILON);
        assertEquals(1.0, CityReclaimMath.clampFactor(5.0), EPSILON);
        assertEquals(0.0, CityReclaimMath.clampFactor(-5.0), EPSILON);
    }

    @Test
    void theDefaultFactorLiftsAFallenDistrictBackBelowTheCutOff() {
        double after = CityReclaimMath.taintAfter(1.0, FACTOR);
        assertEquals(0.35, after, EPSILON);
        assertTrue(after < FALLEN_THRESHOLD, "a fully tainted district must no longer be Fallen");
        assertTrue(after >= BESIEGED_THRESHOLD, "full taint should land in Besieged, not a full cure");
        // A district just past the Fallen cut-off falls all the way to Uneasy.
        assertTrue(CityReclaimMath.taintAfter(FALLEN_THRESHOLD, FACTOR) < BESIEGED_THRESHOLD);
    }

    @Test
    void theDistrictBudgetIsCappedButNeverExpands() {
        // The largest curated radius (400) is a 51×51 square, over the cap.
        assertEquals(51, CityReclaimMath.districtSide(400));
        assertTrue(CityReclaimMath.districtChunks(400) > CityReclaimMath.MAX_CHUNKS);
        assertEquals(CityReclaimMath.MAX_CHUNKS, CityReclaimMath.chunkBudget(400));
        // The smallest curated radius (220) is still over the cap.
        assertEquals(CityReclaimMath.MAX_CHUNKS, CityReclaimMath.chunkBudget(220));
        // A tiny radius is bounded by its own district, not by the cap.
        assertEquals(1, CityReclaimMath.chunkBudget(0));
        assertEquals(CityReclaimMath.districtChunks(16), CityReclaimMath.chunkBudget(16));
    }

    @Test
    void districtSelectionIsCircularAndInclusiveAtTheCentre() {
        assertTrue(CityReclaimMath.withinDistrict(0, 0, 220));
        assertTrue(CityReclaimMath.withinDistrict(13, 0, 220));   // 208 blocks, inside
        assertFalse(CityReclaimMath.withinDistrict(14, 0, 220));  // 224 blocks, outside
        assertFalse(CityReclaimMath.withinDistrict(0, 0, -1));
        assertEquals(0, CityReclaimMath.districtChunks(-1));
    }

    @Test
    void theStateSampleChunksAreCoveredByOneReclaim() {
        // CityStates reads nine chunks spread over the district; a reclaim cleans them first, so all
        // nine must lie inside the district circle (and the factor must lift every one of them).
        int[][] samples = CityReclaimMath.stateSampleOffsets(293);
        assertEquals(9, samples.length);
        assertEquals(0, samples[0][0]);
        assertEquals(0, samples[0][1]);
        for (int[] offset : samples) {
            assertTrue(CityReclaimMath.withinDistrict(offset[0], offset[1], 293));
            assertTrue(CityReclaimMath.taintAfter(1.0, FACTOR) < FALLEN_THRESHOLD);
        }
        // The sample step grows with the district: 1 chunk per 32 blocks, at least 1.
        assertEquals(9, CityReclaimMath.stateSampleOffsets(293)[1][0] * -1);
        assertEquals(1, CityReclaimMath.stateSampleOffsets(0)[1][0] * -1);
    }
}
