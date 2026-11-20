package com.sanchous98.eldritchhorror.world.rail;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Guards the height profile (design/30 stage 2): water is always bridged at a consistent height,
 * station ends are pinned, and the rail never rises or falls faster than one block per step.
 */
class RailProfileTest {
    private static final int SEA = 63;

    @Test
    void waterIsBridgedAndSlopesAreGentle() {
        List<RailPathMath.RailPoint> points = new ArrayList<>();
        for (int x = 0; x <= 40; x++) {
            points.add(new RailPathMath.RailPoint(x, 0));
        }
        RailProfile.Surface surface = new RailProfile.Surface() {
            @Override
            public boolean water(int x, int z) {
                return x >= 15 && x <= 20;
            }

            @Override
            public int surfaceY(int x, int z) {
                return x >= 15 && x <= 20 ? SEA - 8 : SEA + 1 + x / 8;
            }
        };
        int[] heights = RailProfile.build(points, surface, SEA, SEA + 2, SEA + 7);
        assertEquals(SEA + 2, heights[0]);
        assertEquals(SEA + 7, heights[heights.length - 1]);
        for (int x = 15; x <= 20; x++) {
            assertTrue(heights[x] >= SEA + RailProfile.BRIDGE_HEIGHT,
                    "a bridge must clear the water");
        }
        for (int i = 1; i < heights.length; i++) {
            assertTrue(Math.abs(heights[i] - heights[i - 1]) <= 1,
                    "the profile may not step more than one block");
        }
    }
}
