package com.sanchous98.eldritchhorror.world.town;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * Guards the pure population rules: the pass cadence, the per-pass budget and cap, the
 * deterministic trade/spot choice, and the taint gate that abandons a town.
 */
class TownPopulationMathTest {

    @Test
    void aPassIsDueOnTheIntervalAndNowhereElse() {
        assertTrue(TownPopulationMath.due(0, 200));
        assertTrue(TownPopulationMath.due(400, 200));
        assertFalse(TownPopulationMath.due(399, 200));
        assertFalse(TownPopulationMath.due(400, 0), "a zero interval is never due");
        assertFalse(TownPopulationMath.due(400, -20));
        assertTrue(TownPopulationMath.due(20, 20));
    }

    @Test
    void theBudgetIsPerPassAndStopsAtTheCap() {
        assertEquals(2, TownPopulationMath.budget(0, 5, 2));
        assertEquals(2, TownPopulationMath.budget(2, 5, 2));
        assertEquals(1, TownPopulationMath.budget(4, 5, 2), "never overshoot the cap");
        assertEquals(0, TownPopulationMath.budget(5, 5, 2));
        assertEquals(0, TownPopulationMath.budget(9, 5, 2), "above the cap means no spawns");
        assertEquals(0, TownPopulationMath.budget(0, 0, 2), "a disabled cap disables the pass");
        assertEquals(0, TownPopulationMath.budget(0, 5, 0));
        assertEquals(0, TownPopulationMath.budget(0, 5, -1));
    }

    @Test
    void tradesAreSpreadAcrossThePoolAndDeterministic() {
        Set<TownProfession> seen = EnumSet.noneOf(TownProfession.class);
        for (int i = 0; i < 5; i++) {
            TownProfession trade = TownPopulationMath.profession(0x51A7, i);
            seen.add(trade);
            assertEquals(trade, TownPopulationMath.profession(0x51A7, i),
                    "the same town and slot must pick the same trade");
        }
        assertEquals(EnumSet.allOf(TownProfession.class), seen,
                "a full five-villager town covers the whole trade pool");
        assertNotEquals(TownPopulationMath.profession(1, 0),
                TownPopulationMath.profession(1, 1), "adjacent slots differ");
    }

    @Test
    void spawnSpotsStayOnTheRingInsideTheFootprint() {
        for (int attempt = 0; attempt < 30; attempt++) {
            int[] offset = TownPopulationMath.spotOffset(0x7E57, attempt, 41);
            assertEquals(offset[0], TownPopulationMath.spotOffset(0x7E57, attempt, 41)[0]);
            assertEquals(offset[1], TownPopulationMath.spotOffset(0x7E57, attempt, 41)[1]);
            double distance = Math.hypot(offset[0], offset[1]);
            assertTrue(distance >= 11.5, "never spawn in the plaza furniture");
            assertTrue(distance <= 0.7 * 41 + 1.5, "spawns stay inside the town: " + distance);
        }
        // A tiny radius must not make the min distance negative or crash.
        int[] tiny = TownPopulationMath.spotOffset(1, 0, 4);
        assertTrue(Math.hypot(tiny[0], tiny[1]) >= 11.5);
    }

    @Test
    void heavyTaintAbandonsATownButZeroDisablesTheGate() {
        assertTrue(TownPopulationMath.abandoned(0.6, 0.5));
        assertTrue(TownPopulationMath.abandoned(0.5, 0.5), "the threshold is inclusive");
        assertFalse(TownPopulationMath.abandoned(0.49, 0.5));
        assertFalse(TownPopulationMath.abandoned(1.0, 0.0), "0 disables abandonment");
    }
}
