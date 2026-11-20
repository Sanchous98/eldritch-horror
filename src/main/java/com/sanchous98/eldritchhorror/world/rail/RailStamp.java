package com.sanchous98.eldritchhorror.world.rail;

import net.minecraft.world.level.block.state.properties.RailShape;

/**
 * One pre-computed block write for worldgen. The line network buckets these by chunk when it is
 * built, so {@code applyBiomeDecoration} never scans a whole line to find the sliver it owns.
 *
 * <p>A stamp carries the cross-section facts (bridge/tunnel and the outgoing unit axis) instead of
 * the {@code BlockState}, so the worldgen pass stays a small switch and the network stays
 * deterministic.
 *
 * @param x       world X
 * @param y       world Y
 * @param z       world Z
 * @param kind    what to place
 * @param shape   rail shape (meaningful only for {@link Kind#TRACK})
 * @param bridge  true when this block is on a water bridge
 * @param tunnel  true when this block is inside a carved tunnel
 * @param dirX    outgoing unit axis X (-1, 0 or 1)
 * @param dirZ    outgoing unit axis Z (-1, 0 or 1)
 */
public record RailStamp(int x, int y, int z, Kind kind, RailShape shape, boolean bridge,
                        boolean tunnel, int dirX, int dirZ) {

    /** What the worldgen pass should write. */
    public enum Kind {
        /** Ballast/deck, sleeper rail, bridge railings or a tunnel corridor. */
        TRACK,
        /** A station platform floor block (the pass clears the space above it). */
        STATION_FLOOR,
        /** A station railing post. */
        STATION_FENCE,
        /** A station lamp. */
        STATION_LANTERN
    }

    /** @return a track stamp. */
    public static RailStamp track(int x, int y, int z, RailShape shape, boolean bridge,
                                  boolean tunnel, int dirX, int dirZ) {
        return new RailStamp(x, y, z, Kind.TRACK, shape, bridge, tunnel, dirX, dirZ);
    }

    /** @return a station platform-floor stamp. */
    public static RailStamp floor(int x, int y, int z) {
        return new RailStamp(x, y, z, Kind.STATION_FLOOR, RailShape.NORTH_SOUTH,
                false, false, 0, 0);
    }

    /** @return a station railing stamp. */
    public static RailStamp fence(int x, int y, int z) {
        return new RailStamp(x, y, z, Kind.STATION_FENCE, RailShape.NORTH_SOUTH,
                false, false, 0, 0);
    }

    /** @return a station lamp stamp. */
    public static RailStamp lantern(int x, int y, int z) {
        return new RailStamp(x, y, z, Kind.STATION_LANTERN, RailShape.NORTH_SOUTH,
                false, false, 0, 0);
    }
}
