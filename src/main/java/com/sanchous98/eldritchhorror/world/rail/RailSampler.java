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
 */
public final class RailSampler {
    private final List<RailNode> nodes;
    private final double[] cumulative;
    private final double length;

    /**
     * @param nodes ordered rail blocks (at least two)
     */
    public RailSampler(List<RailNode> nodes) {
        if (nodes.size() < 2) {
            throw new IllegalArgumentException("a rail line needs at least two nodes");
        }
        this.nodes = List.copyOf(nodes);
        this.cumulative = new double[this.nodes.size()];
        for (int i = 1; i < this.nodes.size(); i++) {
            this.cumulative[i] = this.cumulative[i - 1] + distance(this.nodes.get(i - 1),
                    this.nodes.get(i));
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
        double dx = EarthMap.wrapDelta((double) from.x(), (double) to.x());
        double x = EarthMap.wrapBlockX(from.x() + dx * t);
        double y = from.y() + (to.y() - from.y()) * t;
        double z = from.z() + (to.z() - from.z()) * t;
        float yaw = yaw(from, to);
        // Fold after the half-block centring offset so the returned X is always on the cylinder.
        return new RailSample(EarthMap.wrapBlockX(x + 0.5), y, z + 0.5, yaw);
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

    private static double distance(RailNode a, RailNode b) {
        double dx = EarthMap.wrapDelta((double) a.x(), (double) b.x());
        double dy = b.y() - (double) a.y();
        double dz = b.z() - (double) a.z();
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    private static float yaw(RailNode from, RailNode to) {
        double dx = EarthMap.wrapDelta((double) from.x(), (double) to.x());
        double dz = to.z() - (double) from.z();
        if (dx == 0.0 && dz == 0.0) {
            return 0.0F;
        }
        return (float) Math.toDegrees(Math.atan2(-dx, dz));
    }
}
