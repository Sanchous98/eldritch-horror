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
 * @param label   sign text for {@link Kind#STATION_SIGN}, otherwise {@code ""}
 */
public record RailStamp(int x, int y, int z, Kind kind, RailShape shape, boolean bridge,
                        boolean tunnel, int dirX, int dirZ, String label) {

    /** What the worldgen pass should write. */
    public enum Kind {
        /** Ballast/deck, sleeper rail, bridge railings or a tunnel corridor. */
        TRACK,
        /** A station platform floor block (the pass clears the platform surface and five above). */
        STATION_FLOOR,
        /** A station railing post. */
        STATION_FENCE,
        /** A station lamp. */
        STATION_LANTERN,
        /** A canopy block above the platform. */
        STATION_CANOPY,
        /** A canopy pillar standing on the platform. */
        STATION_PILLAR,
        /** A station sign carrying {@link RailStamp#label()}. */
        STATION_SIGN,
        /** A buffer stop / platform end wall. */
        STATION_BUFFER,
        /** A head-room cell that must be air even when no line runs through the station. */
        STATION_CLEAR
    }

    /** @return a track stamp. */
    public static RailStamp track(int x, int y, int z, RailShape shape, boolean bridge,
                                  boolean tunnel, int dirX, int dirZ) {
        return new RailStamp(x, y, z, Kind.TRACK, shape, bridge, tunnel, dirX, dirZ, "");
    }

    /** @return a station platform-floor stamp. */
    public static RailStamp floor(int x, int y, int z) {
        return new RailStamp(x, y, z, Kind.STATION_FLOOR, RailShape.NORTH_SOUTH,
                false, false, 0, 0, "");
    }

    /** @return a station railing stamp. */
    public static RailStamp fence(int x, int y, int z) {
        return new RailStamp(x, y, z, Kind.STATION_FENCE, RailShape.NORTH_SOUTH,
                false, false, 0, 0, "");
    }

    /** @return a station lamp stamp. */
    public static RailStamp lantern(int x, int y, int z) {
        return new RailStamp(x, y, z, Kind.STATION_LANTERN, RailShape.NORTH_SOUTH,
                false, false, 0, 0, "");
    }

    /** @return a station canopy block stamp. */
    public static RailStamp canopy(int x, int y, int z) {
        return new RailStamp(x, y, z, Kind.STATION_CANOPY, RailShape.NORTH_SOUTH,
                false, false, 0, 0, "");
    }

    /** @return a station canopy pillar stamp. */
    public static RailStamp pillar(int x, int y, int z) {
        return new RailStamp(x, y, z, Kind.STATION_PILLAR, RailShape.NORTH_SOUTH,
                false, false, 0, 0, "");
    }

    /** @return a station sign stamp. */
    public static RailStamp sign(int x, int y, int z, String label) {
        return new RailStamp(x, y, z, Kind.STATION_SIGN, RailShape.NORTH_SOUTH,
                false, false, 0, 0, label);
    }

    /** @return a buffer-stop / end-wall stamp. */
    public static RailStamp buffer(int x, int y, int z) {
        return new RailStamp(x, y, z, Kind.STATION_BUFFER, RailShape.NORTH_SOUTH,
                false, false, 0, 0, "");
    }

    /** @return a head-room clearing stamp. */
    public static RailStamp clear(int x, int y, int z) {
        return new RailStamp(x, y, z, Kind.STATION_CLEAR, RailShape.NORTH_SOUTH,
                false, false, 0, 0, "");
    }
}
