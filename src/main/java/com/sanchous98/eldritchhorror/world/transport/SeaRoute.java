package com.sanchous98.eldritchhorror.world.transport;

import java.util.List;

/**
 * A cached sea lane between two ports: an immutable ordered list of block waypoints and its total
 * length. Built once per port pair by {@link ShippingLines}; never recomputed while a ship sails.
 *
 * @param points waypoints from the origin port to the destination port (at least two)
 * @param length total polyline length in blocks
 */
public record SeaRoute(List<SeaPoint> points, double length) {

    /** Canonical constructor: the point list is immutable and never empty. */
    public SeaRoute {
        points = List.copyOf(points);
        if (points.size() < 2) {
            throw new IllegalArgumentException("a sea route needs at least two waypoints");
        }
    }

    /** @return the number of waypoints. */
    public int size() {
        return this.points.size();
    }

    /**
     * @param index waypoint index
     * @return the waypoint at {@code index}
     */
    public SeaPoint at(int index) {
        return this.points.get(index);
    }
}
