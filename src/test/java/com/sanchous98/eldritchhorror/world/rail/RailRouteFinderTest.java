package com.sanchous98.eldritchhorror.world.rail;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.IntBinaryOperator;
import org.junit.jupiter.api.Test;

/**
 * Guards the pure rail A* (design/30 stage 2): inland routes, short water crossings, refusal of long
 * water, the geometric length cap and cylinder wrapping.
 */
class RailRouteFinderTest {
    private static final int SEA = 63;

    private static RailGrid grid(int width, int height, BiPredicate<Integer, Integer> water,
                                 IntBinaryOperator surface) {
        return RailGrid.of(width, height, new RailGrid.Terrain() {
            @Override
            public boolean isLand(int cellX, int cellZ) {
                return !water.test(cellX, cellZ);
            }

            @Override
            public int surfaceY(int cellX, int cellZ) {
                return surface.applyAsInt(cellX, cellZ);
            }
        });
    }

    private static RailGrid flatLand(int width, int height) {
        return grid(width, height, (x, z) -> false, (x, z) -> SEA + 1);
    }

    @Test
    void findsAnInlandRoute() {
        RailGrid grid = flatLand(20, 20);
        List<RailCell> path = RailRouteFinder.find(grid, new RailCell(1, 1),
                new RailCell(18, 18), new RailRouteFinder.Options(0, 64, 10.0, 0.0));
        assertNotNull(path);
        assertEquals(new RailCell(1, 1), path.get(0));
        assertEquals(new RailCell(18, 18), path.get(path.size() - 1));
        assertTrue(RailRouteFinder.pathLength(path) <= 18.0 * Math.sqrt(2.0) + 0.1,
                "the smoothed route should be close to the diagonal");
    }

    @Test
    void crossesAShortWaterRun() {
        RailGrid grid = grid(20, 20, (x, z) -> z == 9 || z == 10,
                (x, z) -> z == 9 || z == 10 ? SEA - 10 : SEA + 1);
        List<RailCell> path = RailRouteFinder.find(grid, new RailCell(10, 2),
                new RailCell(10, 17), new RailRouteFinder.Options(2, 64, 18.0, 0.05));
        assertNotNull(path);
        boolean crossed = false;
        for (RailCell cell : path) {
            if (!grid.land(cell.x(), cell.z())) {
                crossed = true;
                break;
            }
        }
        assertTrue(crossed, "the route must use the short water crossing");
    }

    @Test
    void refusesAnOceanCrossing() {
        RailGrid grid = grid(20, 20, (x, z) -> z >= 6 && z <= 14,
                (x, z) -> z >= 6 && z <= 14 ? SEA - 10 : SEA + 1);
        assertNull(RailRouteFinder.find(grid, new RailCell(10, 2),
                new RailCell(10, 18), new RailRouteFinder.Options(2, 64, 18.0, 0.05)));
    }

    @Test
    void respectsTheGeometricLengthCap() {
        RailGrid grid = flatLand(32, 32);
        assertNull(RailRouteFinder.find(grid, new RailCell(1, 1),
                new RailCell(30, 30), new RailRouteFinder.Options(0, 10, 10.0, 0.0)));
    }

    @Test
    void pathLengthChargesTheRealDistanceBetweenSmoothedWaypoints() {
        // Old code charged 1.0 for any single waypoint pair; ten cells apart is ten blocks.
        assertEquals(10.0, RailRouteFinder.pathLength(List.of(
                new RailCell(0, 0), new RailCell(10, 0))), 1.0E-9);
        assertEquals(4.0 + (Math.sqrt(2.0) - 1.0) * 3.0, RailRouteFinder.pathLength(List.of(
                new RailCell(0, 0), new RailCell(3, 4))), 1.0E-9);
    }

    @Test
    void longitudeWrapsAroundTheCylinder() {
        RailGrid grid = flatLand(8, 4);
        List<RailCell> path = RailRouteFinder.find(grid, new RailCell(7, 1),
                new RailCell(1, 1), new RailRouteFinder.Options(0, 8, 10.0, 0.0));
        assertNotNull(path);
        assertEquals(2, path.size(), "the short way crosses the antimeridian");
    }
}
