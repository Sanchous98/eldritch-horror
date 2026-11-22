package com.sanchous98.eldritchhorror.world.transport;

import com.sanchous98.eldritchhorror.world.EarthMap;

/**
 * A waypoint on a sea lane, in world block coordinates (the ship sails from point to point).
 *
 * @param x world X
 * @param z world Z
 */
public record SeaPoint(int x, int z) {

    /** @return the straight-line distance to {@code other}, in blocks. */
    public double distanceTo(SeaPoint other) {
        // Longitude wraps: measure the short way round the seam, not backwards across the map.
        double dx = EarthMap.wrapDelta(this.x, other.x);
        double dz = other.z - (double) this.z;
        return Math.sqrt(dx * dx + dz * dz);
    }
}
