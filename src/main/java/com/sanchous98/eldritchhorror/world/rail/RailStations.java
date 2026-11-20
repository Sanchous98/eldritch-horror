package com.sanchous98.eldritchhorror.world.rail;

import com.sanchous98.eldritchhorror.world.EarthMap;
import com.sanchous98.eldritchhorror.world.ElevationCurve;
import com.sanchous98.eldritchhorror.world.city.City;
import com.sanchous98.eldritchhorror.world.loc.city.CityLocation;
import org.jspecify.annotations.Nullable;

/**
 * Picks a station site at the edge of a curated city ({@code design/30-transport.md}, stage 2).
 *
 * <p>The district built by {@link CityLocation} reaches 220–400 blocks from the centre, so the
 * station is placed on a ring just outside it, in the direction of the city's chosen partner. The
 * scan prefers the closest flat, dry 9×9 patch; if the straight line to the partner runs into the
 * sea it fans out to the sides and finally all round the city. The chosen patch is pure terrain
 * sampling: no chunk is loaded, and the result is deterministic.
 *
 * <p>Server-side and cached with the line network. Never call from client code.
 */
public final class RailStations {
    /** Extra clearance between the built district and the station ring, in blocks. */
    private static final int RING_CLEARANCE = 48;
    /** First ring radius when the district is unusually small. */
    private static final int MIN_RING = 160;
    /** How far beyond the ring the scan may reach, in blocks. */
    private static final int MAX_EXTRA = 192;
    /** Distance step while walking outward, in blocks. */
    private static final int STEP = 16;
    /** Angular samples taken around the preferred bearing. */
    private static final int SIDES = 24;
    /** Half-size of the flat patch that must be land. */
    private static final int PATCH = 4;

    private RailStations() {
    }

    /**
     * Chooses a station site.
     *
     * @param map     baked Earth map
     * @param city    city that owns the station
     * @param partner the city whose direction the station faces
     * @return the station (never {@code null}; a fallback points at the city centre)
     */
    public static RailStation place(EarthMap map, City city, City partner) {
        int district = Math.min(new CityLocation(city).renderRadius(), 380);
        int ring = Math.max(MIN_RING, district + RING_CLEARANCE);
        double bearing = Math.atan2(partner.z() - (double) city.z(),
                wrapDelta(city.x(), partner.x(), 2 * EarthMap.HALF_WIDTH));
        int bestX = city.x();
        int bestZ = city.z();
        int bestSurface = surface(map, city.x(), city.z());
        double bestScore = Double.MAX_VALUE;
        double bestAngle = 0.0;
        for (int side = 0; side < SIDES; side++) {
            double offset = side == 0 ? 0.0 : (side % 2 == 1 ? 1 : -1)
                    * Math.ceil(side / 2.0) * Math.PI / 12.0;
            double angle = bearing + offset;
            double cos = Math.cos(angle);
            double sin = Math.sin(angle);
            for (int extra = 0; extra <= MAX_EXTRA; extra += STEP) {
                int distance = ring + extra;
                int x = city.x() + (int) Math.round(cos * distance);
                int z = city.z() + (int) Math.round(sin * distance);
                if (!flatLand(map, x, z)) {
                    continue;
                }
                int y = surface(map, x, z);
                int relief = relief(map, x, z);
                double score = distance + relief * 6.0 + Math.abs(offset) * 24.0;
                if (score < bestScore) {
                    bestScore = score;
                    bestX = x;
                    bestZ = z;
                    bestSurface = y;
                    bestAngle = angle;
                }
            }
        }
        int dirX = Math.abs(Math.cos(bestAngle)) >= Math.abs(Math.sin(bestAngle))
                ? (int) Math.signum(Math.round(Math.cos(bestAngle)))
                : 0;
        int dirZ = dirX == 0 ? (int) Math.signum(Math.round(Math.sin(bestAngle))) : 0;
        if (dirX == 0 && dirZ == 0) {
            dirX = 1;
        }
        int acrossX = -dirZ;
        int acrossZ = dirX;
        int boardX = bestX + acrossX * 2;
        int boardZ = bestZ + acrossZ * 2;
        if (!map.isLand(boardX, boardZ)) {
            boardX = bestX;
            boardZ = bestZ;
        }
        return new RailStation(city.id(), city.name(), bestX, bestZ, bestSurface + 1,
                boardX, boardZ, dirX, dirZ);
    }

    /** @return the nearest other curated city to a city, or {@code null} when it is the only one. */
    public static @Nullable City nearestOther(City city, java.util.List<City> cities) {
        City best = null;
        double bestDistance = Double.MAX_VALUE;
        for (City other : cities) {
            if (other == city || other.id().equals(city.id())) {
                continue;
            }
            double distance = distanceSqr(city, other);
            if (distance < bestDistance
                    || (distance == bestDistance && best != null
                    && other.id().compareTo(best.id()) < 0)) {
                bestDistance = distance;
                best = other;
            }
        }
        return best;
    }

    /** @return squared cylinder distance between two city centres. */
    public static double distanceSqr(City a, City b) {
        double dx = wrapDelta(a.x(), b.x(), 2 * EarthMap.HALF_WIDTH);
        double dz = b.z() - (double) a.z();
        return dx * dx + dz * dz;
    }

    /** @return the shortest signed X distance on the cylinder. */
    public static int wrapDelta(int fromX, int toX, int circumference) {
        int delta = Math.floorMod(toX - fromX, circumference);
        return delta > circumference / 2 ? delta - circumference : delta;
    }

    private static boolean flatLand(EarthMap map, int x, int z) {
        for (int dx = -PATCH; dx <= PATCH; dx += PATCH) {
            for (int dz = -PATCH; dz <= PATCH; dz += PATCH) {
                if (!map.isLand(x + dx, z + dz)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static int relief(EarthMap map, int x, int z) {
        int low = Integer.MAX_VALUE;
        int high = Integer.MIN_VALUE;
        for (int dx = -8; dx <= 8; dx += 8) {
            for (int dz = -8; dz <= 8; dz += 8) {
                int y = surface(map, x + dx, z + dz);
                low = Math.min(low, y);
                high = Math.max(high, y);
            }
        }
        return high - low;
    }

    /** @return the worldgen surface block Y at a block column. */
    public static int surface(EarthMap map, int x, int z) {
        return Math.clamp(ElevationCurve.toY(map.elevationMetres(x, z)), -60, 300);
    }
}
