package com.sanchous98.eldritchhorror.world.rail;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Guards the block-level track maths (design/30 stage 2): the orthogonal rasterisation that makes
 * vanilla rails connect, and the distance sampler the locomotive advances through.
 */
class RailPathMathTest {

    @Test
    void rasterisesAContiguousOrthogonalPath() {
        List<RailPathMath.RailPoint> path = RailPathMath.orthogonalize(List.of(
                new RailPathMath.RailPoint(0, 0),
                new RailPathMath.RailPoint(20, 7)), 4);
        assertEquals(new RailPathMath.RailPoint(0, 0), path.get(0));
        assertEquals(new RailPathMath.RailPoint(20, 7), path.get(path.size() - 1));
        for (int i = 1; i < path.size(); i++) {
            RailPathMath.RailPoint previous = path.get(i - 1);
            RailPathMath.RailPoint current = path.get(i);
            int step = Math.abs(current.x() - previous.x()) + Math.abs(current.z() - previous.z());
            assertEquals(1, step, "every rail step must be exactly one orthogonal block");
        }
        assertEquals(27.0, RailPathMath.length(path), 1.0E-9);
    }

    @Test
    void aSingleWaypointIsAOneBlockPath() {
        List<RailPathMath.RailPoint> path = RailPathMath.orthogonalize(
                List.of(new RailPathMath.RailPoint(5, 5)), 8);
        assertEquals(1, path.size());
    }

    @Test
    void samplerInterpolatesDistanceAndHeading() {
        RailSampler sampler = new RailSampler(List.of(
                new RailNode(0, 63, 0),
                new RailNode(10, 63, 0),
                new RailNode(10, 63, 10)));
        assertEquals(20.0, sampler.length(), 1.0E-9);
        RailSample middle = sampler.sample(5.0);
        assertEquals(5.5, middle.x(), 1.0E-9);
        assertEquals(0.5, middle.z(), 1.0E-9);
        assertEquals(-90.0F, middle.yaw(), 1.0E-4);
        RailSample corner = sampler.sample(10.0);
        assertEquals(10.5, corner.x(), 1.0E-9);
        assertEquals(0.5, corner.z(), 1.0E-9);
        assertEquals(0.0F, corner.yaw(), 1.0E-4);
        assertEquals(0, sampler.nodeAt(15.0).z());
    }
}
