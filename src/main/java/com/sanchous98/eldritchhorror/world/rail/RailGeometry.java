package com.sanchous98.eldritchhorror.world.rail;

import com.sanchous98.eldritchhorror.world.EarthMap;
import com.sanchous98.eldritchhorror.world.city.Cities;
import com.sanchous98.eldritchhorror.world.city.City;
import java.util.List;

/**
 * The horizontal geometry the rail network plans in.
 *
 * <p>The legacy world is a <b>cylinder</b>: longitude wraps at
 * {@code ±EarthMap.HALF_WIDTH}. A 1:1 map package places cities at compressed but still real
 * distances, and its coordinates reach far past the baked Earth layers (Sydney is at
 * {@code z = -181,682}); that map is a <b>plane</b>. Distances and block paths must therefore use
 * the short way round the seam only when the cities actually fit inside the baked cylinder.
 *
 * <p>The mode is derived from the installed city table, so the mod needs no extra configuration:
 * cities that fit inside the baked extents keep the old wrapping behaviour, and a package that
 * spans more than the map is treated as a plane.
 */
final class RailGeometry {

    private RailGeometry() {
    }

    /** @return true when the installed cities do not fit on the baked Earth cylinder. */
    static boolean planar() {
        List<City> cities = Cities.all();
        if (cities.size() < 2) {
            return false;
        }
        int minX = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int minZ = Integer.MAX_VALUE;
        int maxZ = Integer.MIN_VALUE;
        for (City city : cities) {
            minX = Math.min(minX, city.x());
            maxX = Math.max(maxX, city.x());
            minZ = Math.min(minZ, city.z());
            maxZ = Math.max(maxZ, city.z());
        }
        long spanX = (long) maxX - minX;
        return spanX > 2L * EarthMap.HALF_WIDTH
                || minZ < -EarthMap.HALF_HEIGHT
                || maxZ >= EarthMap.HALF_HEIGHT;
    }

    /** @return the wrap width in blocks, or {@code 0} for a plane. */
    static int wrapWidth() {
        return planar() ? 0 : 2 * EarthMap.HALF_WIDTH;
    }

    /**
     * @param fromX source block X
     * @param toX   target block X
     * @return the signed horizontal delta on the active geometry
     */
    static double deltaX(double fromX, double toX) {
        int width = wrapWidth();
        return width == 0 ? toX - fromX : EarthMap.wrapDelta(fromX, toX);
    }

    /**
     * @param fromX source block X
     * @param toX   target block X
     * @return the signed horizontal delta on the active geometry
     */
    static int deltaX(int fromX, int toX) {
        return deltaX(fromX, toX, wrapWidth());
    }

    /**
     * @param fromX source block X
     * @param toX   target block X
     * @param width wrap width in blocks, or {@code 0} for a plane
     * @return the signed horizontal delta
     */
    static int deltaX(int fromX, int toX, int width) {
        if (width == 0) {
            return toX - fromX;
        }
        int delta = Math.floorMod(toX - fromX, width);
        return delta >= width / 2 ? delta - width : delta;
    }
}
