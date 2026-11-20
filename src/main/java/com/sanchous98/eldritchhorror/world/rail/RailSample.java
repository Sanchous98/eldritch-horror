package com.sanchous98.eldritchhorror.world.rail;

/**
 * A point sampled along a rail line, ready to become an entity position.
 *
 * @param x   world X (block centre)
 * @param y   world Y (rail block top)
 * @param z   world Z (block centre)
 * @param yaw entity yaw in degrees (Minecraft's 0 faces south)
 */
public record RailSample(double x, double y, double z, float yaw) {
}
