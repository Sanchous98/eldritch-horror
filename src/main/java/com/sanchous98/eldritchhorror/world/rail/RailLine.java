package com.sanchous98.eldritchhorror.world.rail;

import java.util.List;
import org.jspecify.annotations.Nullable;

/**
 * One bidirectional rail service: two stations and the finished block-level line between them.
 *
 * <p>Direction {@code 0} runs {@link #a()} to {@link #b()}, direction {@code 1} the reverse. The
 * schedule lives in the train service, not here, because it comes from config. The line can be
 * sampled by travelled distance; a locomotive never walks waypoint by waypoint.
 */
public final class RailLine {
    private final String id;
    private final RailStation a;
    private final RailStation b;
    private final List<RailNode> nodes;
    private final RailSampler sampler;
    private final @Nullable RailNode firstBridge;
    private final @Nullable RailNode firstTunnel;

    /**
     * @param id          stable id, {@code <cityA>-<cityB>} in {@code a}/{@code b} order
     * @param a           first station
     * @param b           second station
     * @param nodes       finished block path from {@code a} to {@code b}
     * @param firstBridge first bridge node, or {@code null}
     * @param firstTunnel first tunnel node, or {@code null}
     */
    public RailLine(String id, RailStation a, RailStation b, List<RailNode> nodes,
                    @Nullable RailNode firstBridge, @Nullable RailNode firstTunnel) {
        this.id = id;
        this.a = a;
        this.b = b;
        this.nodes = List.copyOf(nodes);
        this.sampler = new RailSampler(this.nodes);
        this.firstBridge = firstBridge;
        this.firstTunnel = firstTunnel;
    }

    /** @return the stable line id. */
    public String id() {
        return this.id;
    }

    /** @return the first station ({@code direction 0} origin). */
    public RailStation a() {
        return this.a;
    }

    /** @return the second station ({@code direction 1} origin). */
    public RailStation b() {
        return this.b;
    }

    /** @return the immutable block path from {@code a} to {@code b}. */
    public List<RailNode> nodes() {
        return this.nodes;
    }

    /** @return the line length in blocks. */
    public double length() {
        return this.sampler.length();
    }

    /** @return the first bridge node, for the debug probe. */
    public @Nullable RailNode firstBridge() {
        return this.firstBridge;
    }

    /** @return the first tunnel node, for the debug probe. */
    public @Nullable RailNode firstTunnel() {
        return this.firstTunnel;
    }

    /** @return the station this line leaves for {@code direction}. */
    public RailStation origin(int direction) {
        return direction == 0 ? this.a : this.b;
    }

    /** @return the station this line reaches for {@code direction}. */
    public RailStation destination(int direction) {
        return direction == 0 ? this.b : this.a;
    }

    /**
     * @param direction 0 = a to b, 1 = b to a
     * @param distance  distance travelled from the origin station
     * @return the interpolated position and heading
     */
    public RailSample sample(int direction, double distance) {
        double along = direction == 0 ? distance : this.sampler.length() - distance;
        RailSample sample = this.sampler.sample(along);
        if (direction == 0) {
            return sample;
        }
        return new RailSample(sample.x(), sample.y(), sample.z(), sample.yaw() + 180.0F);
    }

    /**
     * @param direction 0 = a to b, 1 = b to a
     * @param distance  distance travelled from the origin station
     * @return the rail block at that distance (for the physical-obstruction check)
     */
    public RailNode nodeAt(int direction, double distance) {
        double along = direction == 0 ? distance : this.sampler.length() - distance;
        return this.sampler.nodeAt(along);
    }

    /** @return the number of rail blocks. */
    public int size() {
        return this.nodes.size();
    }
}
