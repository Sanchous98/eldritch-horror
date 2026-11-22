package com.sanchous98.eldritchhorror.world.transport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sanchous98.eldritchhorror.world.EarthMap;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Guards the pure A* sea-lane finder (design/30): open water, a detour around land, no path when
 * land closes the way, the safety margin's meaning, cylinder wrapping, and coastal escape.
 */
class SeaRouteFinderTest {

    private static SeaGrid open(int width, int height) {
        return SeaGrid.of(width, height, 1, (x, z) -> false);
    }

    @Test
    void findsAStraightLaneAcrossOpenWater() {
        SeaGrid grid = open(16, 16);
        List<SeaCell> path = SeaRouteFinder.find(grid, new SeaCell(1, 1),
                new SeaCell(14, 14), 0);
        assertNotNull(path);
        assertEquals(new SeaCell(1, 1), path.get(0));
        assertEquals(new SeaCell(14, 14), path.get(path.size() - 1));
        assertTrue(path.size() <= 4, "open water should smooth to a few waypoints");
    }

    @Test
    void detoursAroundAWallOfLand() {
        int width = 24;
        int height = 24;
        // A horizontal barrier at z = 10..14 with gaps at the east and west ends. The cylinder
        // wraps X, so the lane can use either gap, but it must leave z = 10..14.
        SeaGrid grid = SeaGrid.of(width, height, 1,
                (x, z) -> z >= 10 && z <= 14 && x >= 4 && x <= 19);
        List<SeaCell> path = SeaRouteFinder.find(grid, new SeaCell(10, 2),
                new SeaCell(10, 22), 0);
        assertNotNull(path);
        boolean wentAround = false;
        for (SeaCell cell : path) {
            if (cell.x() <= 3 || cell.x() >= 20) {
                wentAround = true;
                break;
            }
        }
        assertTrue(wentAround, "the lane must round the barrier, not cross it");
        assertTrue(path.size() >= 3);
    }

    @Test
    void refusesWhenLandClosesTheWay() {
        // A full ring across the cylinder: there is no way round it north or south.
        SeaGrid grid = SeaGrid.of(20, 20, 1, (x, z) -> z == 10);
        assertNull(SeaRouteFinder.find(grid, new SeaCell(10, 2), new SeaCell(10, 18), 0));
    }

    @Test
    void safetyMarginErodesTheCoast() {
        SeaGrid grid = SeaGrid.of(8, 8, 1, (x, z) -> x == 3 && z == 3);
        assertTrue(grid.water(4, 4));
        assertFalse(grid.safe(4, 4), "water touching land is not safe water");
        assertTrue(grid.safe(6, 6), "open water away from land is safe");
        assertFalse(grid.safe(3, 3));
    }

    @Test
    void longitudeWrapsAroundTheCylinder() {
        SeaGrid grid = open(8, 4);
        List<SeaCell> path = SeaRouteFinder.find(grid, new SeaCell(7, 1),
                new SeaCell(1, 1), 0);
        assertNotNull(path);
        assertEquals(2, path.size(), "the short way crosses the antimeridian, not the whole map");
    }

    @Test
    void coastalEscapeLetsAPortTouchLand() {
        // Water everywhere except a one-cell pier at (5,5); the start cell (5,6) is water but unsafe.
        SeaGrid grid = SeaGrid.of(12, 12, 1, (x, z) -> x == 5 && z == 5);
        assertFalse(grid.safe(5, 6));
        List<SeaCell> path = SeaRouteFinder.find(grid, new SeaCell(5, 6),
                new SeaCell(10, 10), 2);
        assertNotNull(path, "a harbour cell may leave the safe band briefly");
        assertEquals(new SeaCell(5, 6), path.get(0));
        assertEquals(new SeaCell(10, 10), path.get(path.size() - 1));
    }

    @Test
    void smoothKeepsEndpointsAndDropsCollinearSteps() {
        SeaGrid grid = open(10, 10);
        List<SeaCell> raw = List.of(new SeaCell(1, 1), new SeaCell(2, 1),
                new SeaCell(3, 1), new SeaCell(4, 1));
        List<SeaCell> smoothed = SeaRouteFinder.smooth(grid, raw);
        assertEquals(new SeaCell(1, 1), smoothed.get(0));
        assertEquals(new SeaCell(4, 1), smoothed.get(smoothed.size() - 1));
        assertTrue(smoothed.size() < raw.size());
    }

    @Test
    void seaPointDistanceCrossesTheSeamTheShortWay() {
        int half = EarthMap.HALF_WIDTH;
        SeaPoint west = new SeaPoint(half - 5, 100);
        SeaPoint east = new SeaPoint(-half + 5, 100);
        assertEquals(10.0, west.distanceTo(east), 1.0E-9);
        assertEquals(10.0, east.distanceTo(west), 1.0E-9);
    }
}
