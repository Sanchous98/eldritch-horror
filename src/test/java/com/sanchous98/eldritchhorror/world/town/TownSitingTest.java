package com.sanchous98.eldritchhorror.world.town;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Guards the pure town-siting rules: the population-derived footprint clamp, the curated-city
 * overlap test used to skip a town, and the centre-water rejection (with an injected land
 * predicate, so no Earth layers are needed).
 */
class TownSitingTest {

    @Test
    void theFootprintGrowsWithPopulationButStaysSmall() {
        assertEquals(TownSiting.MIN_RADIUS, TownSiting.radiusFor(100_000));
        assertEquals(TownSiting.MIN_RADIUS, TownSiting.radiusFor(1));
        assertEquals(TownSiting.MIN_RADIUS, TownSiting.radiusFor(-5));
        assertTrue(TownSiting.radiusFor(1_000_000) > TownSiting.radiusFor(100_000));
        assertEquals(TownSiting.MAX_RADIUS, TownSiting.radiusFor(100_000_000));
        assertEquals(TownSiting.MAX_RADIUS, TownSiting.radiusFor(Integer.MAX_VALUE));
        // Monotonic over the baked populations: no growth inversion.
        int previous = 0;
        for (int population : new int[]{100_000, 250_000, 500_000, 1_000_000, 5_000_000, 18_000_000}) {
            int radius = TownSiting.radiusFor(population);
            assertTrue(radius >= previous);
            previous = radius;
        }
    }

    @Test
    void overlapIsStrictAtTheSumOfRadii() {
        // Touching exactly (10 + 20 = 30) is not an overlap; one block closer is.
        assertFalse(TownSiting.intersects(0, 0, 10, 30, 0, 20));
        assertTrue(TownSiting.intersects(0, 0, 10, 29, 0, 20));
        assertTrue(TownSiting.intersects(0, 0, 10, 0, 0, 20), "a town on a city is inside it");
        // Diagonal distance: (18,24) is 30 away.
        assertFalse(TownSiting.intersects(0, 0, 10, 18, 24, 20));
        assertTrue(TownSiting.intersects(0, 0, 10, 17, 24, 20));
        // Coordinates at the edge of the baked map do not overflow and stay far apart.
        assertFalse(TownSiting.intersects(-80_000, 0, 64, 80_000, 0, 64));
    }

    @Test
    void aWaterCentreIsRejectedByTheInjectedLandPredicate() {
        // Water = x < 0, land = x >= 0; the rule only reads the centre.
        TownSiting.Land land = (x, z) -> x >= 0;
        assertTrue(TownSiting.centreOnLand(10, -500, land));
        assertTrue(TownSiting.centreOnLand(0, 0, land));
        assertFalse(TownSiting.centreOnLand(-1, 0, land));

        // A predicate that rejects everything (fully drowned site) rejects every town.
        assertFalse(TownSiting.centreOnLand(10, -500, (x, z) -> false));
    }
}
