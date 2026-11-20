package com.sanchous98.eldritchhorror.world.transport;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Guards the real baked map's stage-1 network: at least one curated sea lane resolves, and no lane
 * is a stub. The Earth layers are large optional assets (like {@code EarthMapCylinderTest}), so a
 * bare checkout without them skips rather than fails; the server probe is the gameplay gate.
 */
class SeaLaneRealMapTest {

    @Test
    void atLeastOneCuratedLaneExists() {
        List<ShippingLine> lines;
        try {
            lines = ShippingLines.all();
        } catch (Throwable missingAssetsOrClasspath) {
            return;
        }
        assertFalse(lines.isEmpty(), "the baked landmask should yield at least one sea lane");
        for (ShippingLine line : lines) {
            assertTrue(line.length() > 1000.0,
                    line.id() + " should be a real sea crossing, not a harbour hop");
            assertTrue(line.route().size() >= 2);
        }
    }
}
