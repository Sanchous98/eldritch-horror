package com.sanchous98.eldritchhorror.world.transport;

import com.sanchous98.eldritchhorror.world.EarthMap;

/**
 * A harbour: the curated city that owns it, a water point where the packet floats, and a shore point
 * where passengers are put down. Coordinates are world blocks (overworld).
 *
 * @param cityId   curated city id (stable, from {@code settlements.json})
 * @param cityName display name
 * @param waterX   harbour water point X
 * @param waterZ   harbour water point Z
 * @param shoreX   nearest walkable land X
 * @param shoreZ   nearest walkable land Z
 * @param waterY   the water surface Y the hull floats at; {@code waterY + 0.85} is its spawn height
 */
public record Port(String cityId, String cityName, int waterX, int waterZ, int shoreX, int shoreZ,
                   int waterY) {

    /** @return squared horizontal distance from a world point to the harbour water point. */
    public double distanceSqr(double x, double z) {
        double dx = EarthMap.wrapDelta(this.waterX, x);
        double dz = z - this.waterZ;
        return dx * dx + dz * dz;
    }

    /** @return a short {@code Name (x, z)} label for logs and commands. */
    public String label() {
        return this.cityName + " (" + this.waterX + ", " + this.waterZ + ")";
    }
}
