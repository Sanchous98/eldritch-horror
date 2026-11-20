package com.sanchous98.eldritchhorror.world.transport;

/**
 * A waypoint on a sea lane, in world block coordinates (the ship sails from point to point).
 *
 * @param x world X
 * @param z world Z
 */
public record SeaPoint(int x, int z) {

    /** @return the straight-line distance to {@code other}, in blocks. */
    public double distanceTo(SeaPoint other) {
        double dx = other.x - (double) this.x;
        double dz = other.z - (double) this.z;
        return Math.sqrt(dx * dx + dz * dz);
    }
}
