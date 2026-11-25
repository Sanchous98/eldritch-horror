package com.sanchous98.eldritchhorror.world.rail;

import java.util.List;

/**
 * Turns a block path into a rail height profile: ballast and rails follow the terrain, short water
 * runs become a bridge at a consistent height, and a hill the line cannot climb within one block
 * per step becomes a tunnel.
 *
 * <p>The profile is a moving average of the terrain target, clamped to a one-block rise/fall per
 * block and pinned to the two station heights. Water is always at least
 * {@value #BRIDGE_HEIGHT} blocks above sea level, so a bridge deck sits clear of the water. Pure:
 * the terrain comes through {@link Surface}, so the maths is unit-tested without a game.
 */
public final class RailProfile {
    /** Rail height above the sea for a water crossing. */
    public static final int BRIDGE_HEIGHT = 3;
    /** Half-window of the moving average, in blocks. */
    private static final int SMOOTH_RADIUS = 6;
    /** Highest filled embankment under a rail on land, in blocks. */
    public static final int MAX_EMBANKMENT = 8;

    private RailProfile() {
    }

    /** Supplies block-level terrain facts to {@link #build}. */
    public interface Surface {
        /** @return true when the column at world (x, z) is water. */
        boolean water(int x, int z);

        /** @return the worldgen surface block Y at world (x, z). */
        int surfaceY(int x, int z);
    }

    /**
     * Builds the rail-block Y for every point.
     *
     * @param points   contiguous block path
     * @param surface  block-level terrain
     * @param seaLevel worldgen sea level
     * @param startY   rail Y at the first station (pinned)
     * @param endY     rail Y at the last station (pinned)
     * @return one rail-block Y per point
     */
    public static int[] build(List<RailPathMath.RailPoint> points, Surface surface, int seaLevel,
                              int startY, int endY) {
        if (points.isEmpty()) {
            throw new IllegalArgumentException("a rail profile needs at least one point");
        }
        int size = points.size();
        int[] target = new int[size];
        for (int i = 0; i < size; i++) {
            RailPathMath.RailPoint point = points.get(i);
            target[i] = surface.water(point.x(), point.z())
                    ? seaLevel + BRIDGE_HEIGHT
                    : surface.surfaceY(point.x(), point.z()) + 1;
        }
        int[] y = new int[size];
        for (int i = 0; i < size; i++) {
            y[i] = average(target, Math.max(0, i - SMOOTH_RADIUS),
                    Math.min(size - 1, i + SMOOTH_RADIUS));
        }
        y[0] = startY;
        y[size - 1] = endY;
        for (int pass = 0; pass < 2; pass++) {
            for (int i = 1; i < size; i++) {
                y[i] = Math.clamp(y[i], y[i - 1] - 1, y[i - 1] + 1);
            }
            for (int i = size - 2; i >= 0; i--) {
                y[i] = Math.clamp(y[i], y[i + 1] - 1, y[i + 1] + 1);
            }
            for (int i = 0; i < size; i++) {
                RailPathMath.RailPoint point = points.get(i);
                if (surface.water(point.x(), point.z())) {
                    y[i] = Math.max(y[i], seaLevel + BRIDGE_HEIGHT);
                }
            }
        }
        y[0] = startY;
        y[size - 1] = endY;
        for (int i = 0; i < size; i++) {
            y[i] = Math.clamp(y[i], -60, 300);
        }
        return y;
    }

    private static int average(int[] values, int from, int to) {
        long sum = 0L;
        for (int i = from; i <= to; i++) {
            sum += values[i];
        }
        return (int) Math.round((double) sum / (to - from + 1));
    }

    /**
     * Builds a height for each waypoint of a <b>sparse</b> planned line (a long intercity line whose
     * corridor is not stamped). There is no one-block-per-step ramp to respect: consecutive
     * waypoints can be hundreds of blocks apart, and the locomotive interpolates the height along
     * the segment. Heights follow the terrain at the waypoints, water is lifted to bridge height, and
     * only the two station heights are pinned.
     *
     * @param points   sparse waypoint chain
     * @param surface  block-level terrain
     * @param seaLevel worldgen sea level
     * @param startY   rail Y at the first station (pinned)
     * @param endY     rail Y at the last station (pinned)
     * @return one rail-block Y per waypoint
     */
    public static int[] buildWaypoints(List<RailPathMath.RailPoint> points, Surface surface,
                                       int seaLevel, int startY, int endY) {
        if (points.isEmpty()) {
            throw new IllegalArgumentException("a rail profile needs at least one point");
        }
        int size = points.size();
        int[] y = new int[size];
        for (int i = 0; i < size; i++) {
            RailPathMath.RailPoint point = points.get(i);
            y[i] = surface.water(point.x(), point.z())
                    ? seaLevel + BRIDGE_HEIGHT
                    : surface.surfaceY(point.x(), point.z()) + 1;
        }
        y[0] = startY;
        y[size - 1] = endY;
        for (int i = 0; i < size; i++) {
            y[i] = Math.clamp(y[i], -60, 300);
        }
        return y;
    }
}
