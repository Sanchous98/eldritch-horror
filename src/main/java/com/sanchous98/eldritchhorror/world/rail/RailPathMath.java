package com.sanchous98.eldritchhorror.world.rail;

import com.sanchous98.eldritchhorror.world.EarthMap;
import java.util.ArrayList;
import java.util.List;

/**
 * Rasterises a coarse waypoint chain into a one-block-wide, <b>orthogonal</b> track line.
 *
 * <p>Vanilla rails connect north/south and east/west (with curved corners); a true diagonal block
 * line is therefore not possible. A diagonal waypoint pair becomes a gentle staircase: after each
 * run of up to {@code run} blocks along the dominant axis, one block is taken on the minor axis, so
 * long diagonals read as a sweeping line rather than a 1/1 zigzag. The output is contiguous (every
 * consecutive pair differs by exactly one block on one axis).
 *
 * <p>A wrap width of {@code 0} means the map is a plane (a 1:1 map package); a positive width means
 * the legacy Earth cylinder, where a step across the antimeridian is one block, not a lap.
 *
 * <p>Pure: no Minecraft type appears here.
 */
public final class RailPathMath {
    /** Default run before a one-block lateral step on a diagonal segment. */
    public static final int DEFAULT_RUN = 8;
    /** The legacy cylinder's circumference in blocks. */
    private static final int EARTH_WRAP = 2 * EarthMap.HALF_WIDTH;

    private RailPathMath() {
    }

    /** A block column on the rail line (X/Z only; the height profile is applied later). */
    public record RailPoint(int x, int z) {
    }

    /**
     * Converts a waypoint chain to a contiguous orthogonal block path on the Earth cylinder.
     *
     * @param waypoints ordered waypoints (at least one)
     * @param run       maximum straight run on the dominant axis of a diagonal segment
     * @return every block from the first waypoint to the last, without duplicate neighbours
     */
    public static List<RailPoint> orthogonalize(List<RailPoint> waypoints, int run) {
        return orthogonalize(waypoints, run, EARTH_WRAP);
    }

    /**
     * Converts a waypoint chain to a contiguous orthogonal block path.
     *
     * @param waypoints ordered waypoints (at least one)
     * @param run       maximum straight run on the dominant axis of a diagonal segment
     * @param wrapWidth cylinder circumference in blocks, or {@code 0} for a plane
     * @return every block from the first waypoint to the last, without duplicate neighbours
     */
    public static List<RailPoint> orthogonalize(List<RailPoint> waypoints, int run,
                                                int wrapWidth) {
        if (waypoints.isEmpty()) {
            throw new IllegalArgumentException("a rail path needs at least one waypoint");
        }
        if (run < 1) {
            throw new IllegalArgumentException("run must be positive");
        }
        List<RailPoint> out = new ArrayList<>();
        RailPoint first = waypoints.get(0);
        out.add(first);
        RailPoint previous = first;
        for (int i = 1; i < waypoints.size(); i++) {
            RailPoint target = waypoints.get(i);
            appendSegment(out, previous.x(), previous.z(), target.x(), target.z(), run, wrapWidth);
            previous = target;
        }
        return out;
    }

    /**
     * Appends one segment as unit orthogonal steps.
     *
     * @param out       destination list, already containing the start point
     * @param x0        start X
     * @param z0        start Z
     * @param x1        end X
     * @param z1        end Z
     * @param run       maximum dominant-axis run before a lateral step
     * @param wrapWidth cylinder circumference in blocks, or {@code 0} for a plane
     */
    private static void appendSegment(List<RailPoint> out, int x0, int z0, int x1, int z1,
                                      int run, int wrapWidth) {
        // Longitude wraps: step toward the nearest copy of the target across the seam.
        int targetX = x0 + deltaX(x0, x1, wrapWidth);
        int x = x0;
        int z = z0;
        while (x != targetX || z != z1) {
            int dx = targetX - x;
            int dz = z1 - z;
            if (dx == 0) {
                z += Integer.signum(dz);
                out.add(new RailPoint((int) foldX(x, wrapWidth), z));
                continue;
            }
            if (dz == 0) {
                x += Integer.signum(dx);
                out.add(new RailPoint((int) foldX(x, wrapWidth), z));
                continue;
            }
            if (Math.abs(dx) >= Math.abs(dz)) {
                int step = Math.min(Math.abs(dx), run);
                int sign = Integer.signum(dx);
                for (int i = 0; i < step; i++) {
                    x += sign;
                    out.add(new RailPoint((int) foldX(x, wrapWidth), z));
                }
                if (x != targetX) {
                    z += Integer.signum(dz);
                    out.add(new RailPoint((int) foldX(x, wrapWidth), z));
                }
            } else {
                int step = Math.min(Math.abs(dz), run);
                int sign = Integer.signum(dz);
                for (int i = 0; i < step; i++) {
                    z += sign;
                    out.add(new RailPoint((int) foldX(x, wrapWidth), z));
                }
                if (z != z1) {
                    x += Integer.signum(dx);
                    out.add(new RailPoint((int) foldX(x, wrapWidth), z));
                }
            }
        }
    }

    /**
     * @param points a block path
     * @return the horizontal length of the path on the Earth cylinder, in blocks
     */
    public static double length(List<RailPoint> points) {
        return length(points, EARTH_WRAP);
    }

    /**
     * @param points    a block path
     * @param wrapWidth cylinder circumference in blocks, or {@code 0} for a plane
     * @return the horizontal length of the path in blocks
     */
    public static double length(List<RailPoint> points, int wrapWidth) {
        double total = 0.0;
        for (int i = 1; i < points.size(); i++) {
            RailPoint from = points.get(i - 1);
            RailPoint to = points.get(i);
            total += Math.abs(deltaX(from.x(), to.x(), wrapWidth))
                    + Math.abs(to.z() - from.z());
        }
        return total;
    }

    /**
     * @param fromX     source block X
     * @param toX       target block X
     * @param wrapWidth cylinder circumference in blocks, or {@code 0} for a plane
     * @return the signed horizontal delta
     */
    static int deltaX(int fromX, int toX, int wrapWidth) {
        if (wrapWidth == 0) {
            return toX - fromX;
        }
        int delta = Math.floorMod(toX - fromX, wrapWidth);
        return delta >= wrapWidth / 2 ? delta - wrapWidth : delta;
    }

    /**
     * @param x         a block X
     * @param wrapWidth cylinder circumference in blocks, or {@code 0} for a plane
     * @return the X folded onto the cylinder, or the input on a plane
     */
    static double foldX(double x, int wrapWidth) {
        return wrapWidth == 0 ? x : EarthMap.wrapBlockX(x);
    }
}
