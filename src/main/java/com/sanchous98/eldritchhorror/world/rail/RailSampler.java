package com.sanchous98.eldritchhorror.world.rail;

import com.sanchous98.eldritchhorror.world.EarthMap;
import java.util.List;

/**
 * Distance-parameterised view of an ordered rail node list.
 *
 * <p>Positions are interpolated along the block polyline; the yaw is the direction of the current
 * segment. This is the only movement maths a locomotive needs: it advances a scalar distance and
 * asks for the sample, instead of walking waypoints and overshooting corners. Pure and
 * unit-tested.
 *
 * <p>The sampler follows the line's own geometry: a legacy line wraps at the Earth cylinder seam,
 * while a line on a 1:1 map package is a plain horizontal plane.
 */
public final class RailSampler {
    private static final int EARTH_WRAP = 2 * EarthMap.HALF_WIDTH;

    private final List<RailNode> nodes;
    private final double[] cumulative;
    private final double length;
    private final int wrapWidth;

    /**
     * @param nodes ordered rail blocks (at least two)
     */
    public RailSampler(List<RailNode> nodes) {
        this(nodes, EARTH_WRAP);
    }

    /**
     * @param nodes     ordered rail blocks (at least two)
     * @param wrapWidth cylinder circumference in blocks, or {@code 0} for a plane
     */
    public RailSampler(List<RailNode> nodes, int wrapWidth) {
        if (nodes.size() < 2) {
            throw new IllegalArgumentException("a rail line needs at least two nodes");
        }
        this.nodes = List.copyOf(nodes);
        this.wrapWidth = wrapWidth;
        this.cumulative = new double[this.nodes.size()];
        for (int i = 1; i < this.nodes.size(); i++) {
            this.cumulative[i] = this.cumulative[i - 1] + distance(this.nodes.get(i - 1),
                    this.nodes.get(i), this.wrapWidth);
        }
        this.length = this.cumulative[this.cumulative.length - 1];
        if (this.length <= 0.0) {
            throw new IllegalArgumentException("a rail line needs a non-zero length");
        }
    }

    /** @return the polyline length in blocks. */
    public double length() {
        return this.length;
    }

    /**
     * @param distance distance from the first node, clamped to the line
     * @return the interpolated sample
     */
    public RailSample sample(double distance) {
        double clamped = Math.clamp(distance, 0.0, this.length);
        int index = segment(clamped);
        RailNode from = this.nodes.get(index);
        RailNode to = this.nodes.get(index + 1);
        double segmentLength = this.cumulative[index + 1] - this.cumulative[index];
        double t = segmentLength <= 0.0 ? 0.0 : (clamped - this.cumulative[index]) / segmentLength;
        double dx = RailPathMath.deltaX(from.x(), to.x(), this.wrapWidth);
        double x = RailPathMath.foldX(from.x() + dx * t, this.wrapWidth);
        double y = from.y() + (to.y() - from.y()) * t;
        double z = from.z() + (to.z() - from.z()) * t;
        float yaw = yaw(from, to, this.wrapWidth);
        // Fold after the half-block centring offset so the returned X is always on the cylinder.
        return new RailSample(RailPathMath.foldX(x + 0.5, this.wrapWidth), y, z + 0.5, yaw);
    }

    /**
     * @param distance distance from the first node
     * @return the node at or immediately before that distance (for block checks)
     */
    public RailNode nodeAt(double distance) {
        double clamped = Math.clamp(distance, 0.0, this.length);
        return this.nodes.get(segment(clamped));
    }

    /**
     * @param index node index
     * @return the node at that index
     */
    public RailNode node(int index) {
        return this.nodes.get(Math.clamp(index, 0, this.nodes.size() - 1));
    }

    /** @return the number of nodes. */
    public int size() {
        return this.nodes.size();
    }

    /** @return the wrap width this sampler was built with ({@code 0} = plane). */
    public int wrapWidth() {
        return this.wrapWidth;
    }

    private int segment(double distance) {
        int low = 0;
        int high = this.cumulative.length - 1;
        while (low < high - 1) {
            int mid = (low + high) >>> 1;
            if (this.cumulative[mid] <= distance) {
                low = mid;
            } else {
                high = mid;
            }
        }
        return Math.min(low, this.cumulative.length - 2);
    }

    private static double distance(RailNode a, RailNode b, int wrapWidth) {
        double dx = RailPathMath.deltaX(a.x(), b.x(), wrapWidth);
        double dy = b.y() - (double) a.y();
        double dz = b.z() - (double) a.z();
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    private static float yaw(RailNode from, RailNode to, int wrapWidth) {
        double dx = RailPathMath.deltaX(from.x(), to.x(), wrapWidth);
        double dz = to.z() - (double) from.z();
        if (dx == 0.0 && dz == 0.0) {
            return 0.0F;
        }
        return (float) Math.toDegrees(Math.atan2(-dx, dz));
    }
}
