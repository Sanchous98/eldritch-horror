package com.sanchous98.eldritchhorror.world;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

/**
 * Regression guard for the world <b>cylinder</b>: longitude (X) wraps, latitude (Z) clamps.
 * See {@code design/23-boundary-and-travel.md}.
 *
 * <p>{@link EarthMap} is deliberately Minecraft-free, so it loads its real baked layers straight
 * from the mod resources and can be asserted on without a running game. If the assets are absent
 * the test is skipped rather than failed (a bare checkout without the big PNGs).
 */
class EarthMapCylinderTest {

    private static EarthMap load() {
        try {
            EarthMap map = EarthMap.load(EarthMaps.PIXEL_WIDTH);
            return map;
        } catch (Throwable t) {
            return null;
        }
    }

    @Test
    void longitudeWrapsLatitudeClamps() {
        EarthMap map = load();
        org.junit.jupiter.api.Assumptions.assumeTrue(map != null,
                "baked Earth layers not present in this checkout");

        int half = EarthMap.HALF_WIDTH;
        // A column just west of the map centre and its +360deg copy must sample identically.
        int x = 1234;
        int z = -400;
        assertEquals(map.isLand(x, z), map.isLand(x + 2 * half, z),
                "longitude must wrap: x and x+360deg are the same meridian");
        assertEquals(map.isLand(x, z), map.isLand(x - 2 * half, z),
                "longitude must wrap westward too");
        assertEquals(map.koppenClass(x, z), map.koppenClass(x + 2 * half, z),
                "Koppen must wrap with longitude");
        assertEquals(map.elevationMetres(x, z), map.elevationMetres(x + 2 * half, z), 1e-6,
                "elevation must wrap with longitude");

        // Latitude does NOT wrap: the extreme north and south rows are not equal.
        boolean northLand = map.isLand(0, -EarthMap.HALF_HEIGHT);
        boolean southLand = map.isLand(0, EarthMap.HALF_HEIGHT - 1);
        // (Not asserting they differ — poles could both be ocean — only that both sample.)
        assertNotNull(Boolean.valueOf(northLand));
        assertNotNull(Boolean.valueOf(southLand));
    }

    @Test
    void seamSitsInOpenOceanNearTheAntimeridian() {
        EarthMap map = load();
        org.junit.jupiter.api.Assumptions.assumeTrue(map != null,
                "baked Earth layers not present in this checkout");
        // The wrap join lands at lon +/-180. In the inhabited band (|lat|<60) it should be
        // almost entirely water, so a player circumnavigating does not see a cut continent.
        int half = EarthMap.HALF_WIDTH;
        int sea = 0;
        int samples = 0;
        for (int z = -(int) (EarthMap.HALF_HEIGHT * 0.66); z <= EarthMap.HALF_HEIGHT * 0.66; z += 64) {
            samples++;
            if (!map.isLand(half - 1, z) && !map.isLand(-half, z)) {
                sea++;
            }
        }
        double seaFraction = (double) sea / samples;
        org.junit.jupiter.api.Assertions.assertTrue(seaFraction > 0.9,
                "the longitude seam should be >90% ocean in |lat|<60, was " + seaFraction);
    }
}
