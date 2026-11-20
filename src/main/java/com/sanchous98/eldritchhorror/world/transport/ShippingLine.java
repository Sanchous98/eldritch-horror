package com.sanchous98.eldritchhorror.world.transport;

/**
 * One bidirectional sea service: two harbours and the cached lane between them.
 *
 * <p>Direction {@code 0} runs {@link #a()} to {@link #b()}, direction {@code 1} the reverse. The
 * schedule lives in {@link ShipService}, not here, because it comes from config.
 *
 * @param id    stable id, {@code <fromCity>-<toCity>} in {@link #a()}/{@link #b()} order
 * @param a     first harbour
 * @param b     second harbour
 * @param route lane from {@code a} to {@code b}
 */
public record ShippingLine(String id, Port a, Port b, SeaRoute route) {

    /** @return the harbour the ship leaves for {@code direction}. */
    public Port origin(int direction) {
        return direction == 0 ? this.a : this.b;
    }

    /** @return the harbour the ship reaches for {@code direction}. */
    public Port destination(int direction) {
        return direction == 0 ? this.b : this.a;
    }

    /**
     * @param direction 0 = a to b, 1 = b to a
     * @param index     step along this direction
     * @return the waypoint, counting from the origin harbour
     */
    public SeaPoint waypoint(int direction, int index) {
        int resolved = direction == 0 ? index : this.route.size() - 1 - index;
        return this.route.at(resolved);
    }

    /** @return the lane length in blocks (the same both ways). */
    public double length() {
        return this.route.length();
    }
}
