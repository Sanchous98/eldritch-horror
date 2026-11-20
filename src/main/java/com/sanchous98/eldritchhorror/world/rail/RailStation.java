package com.sanchous98.eldritchhorror.world.rail;

/**
 * A station on the edge of a curated city: the rail point, the platform orientation and the
 * boarding point where riders are put down.
 *
 * <p>The station exists whether or not a line reaches it ({@link #label()}); only a station that is
 * an endpoint of a {@link RailLine} is usable. {@code y} is the rail block's Y: the platform surface
 * is the top of the block below it, so a player stands at {@code y}. The boarding point is two
 * blocks to the side of the track, on the platform.
 *
 * @param cityId   full curated city id
 * @param cityName display name
 * @param x        station rail-point X
 * @param z        station rail-point Z
 * @param y        rail block Y (the platform top is at this level)
 * @param boardX   boarding-point X
 * @param boardZ   boarding-point Z
 * @param dirX     platform long-axis X (-1, 0 or 1)
 * @param dirZ     platform long-axis Z (-1, 0 or 1)
 */
public record RailStation(String cityId, String cityName, int x, int z, int y,
                          int boardX, int boardZ, int dirX, int dirZ) {

    /** @return squared horizontal distance from a world point to the station rail point. */
    public double distanceSqr(double worldX, double worldZ) {
        double dx = worldX - this.x;
        double dz = worldZ - this.z;
        return dx * dx + dz * dz;
    }

    /** @return a short {@code Name (x, z)} label for logs and commands. */
    public String label() {
        return this.cityName + " (" + this.x + ", " + this.z + ")";
    }

    /** @return the perpendicular axis X (across the platform). */
    public int acrossX() {
        return -this.dirZ;
    }

    /** @return the perpendicular axis Z (across the platform). */
    public int acrossZ() {
        return this.dirX;
    }
}
