package com.sanchous98.eldritchhorror.world.rail;

import com.sanchous98.eldritchhorror.world.EarthMap;
import com.sanchous98.eldritchhorror.world.ElevationCurve;
import com.sanchous98.eldritchhorror.world.city.City;
import com.sanchous98.eldritchhorror.world.loc.Palette;
import com.sanchous98.eldritchhorror.world.loc.city.CityLocation;
import com.sanchous98.eldritchhorror.world.loc.city.plan.CityPlan;
import com.sanchous98.eldritchhorror.world.loc.city.plan.CityPlanProbe;
import com.sanchous98.eldritchhorror.world.loc.city.plan.CityPlans;
import com.sanchous98.eldritchhorror.world.loc.city.plan.CityPoi;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.world.level.block.RailBlock;
import net.minecraft.world.level.block.state.properties.RailShape;
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
    /** How far from a STATION anchor its authored rails may be and still be found. */
    private static final int RAIL_SNAP_RADIUS = 32;
    /** Platform column the boarding point sits on, measured from the rail centreline. */
    private static final int BOARD_ACROSS = 2;

    /** Authored rail point per city, so a multi-partner terminal does not rebuild its plan. */
    private static final Map<String, List<CityPlanProbe.Rail>> AUTHORED_RAILS =
            new ConcurrentHashMap<>();

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
        RailStation packaged = packageStation(city, partner);
        if (packaged != null) {
            return packaged;
        }
        RailStation authored = authoredStation(map, city, partner);
        if (authored != null) {
            return authored;
        }
        int district = Math.min(new CityLocation(city).renderRadius(), 380);
        int ring = Math.max(MIN_RING, district + RING_CLEARANCE);
        double bearing = Math.atan2(partner.z() - (double) city.z(),
                RailGeometry.deltaX(city.x(), partner.x()));
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
        int side = Integer.signum((city.x() - bestX) * acrossX + (city.z() - bestZ) * acrossZ);
        if (side == 0) {
            side = 1;
        }
        int outSign = Integer.signum((int) Math.round(
                RailGeometry.deltaX(bestX, partner.x()) * dirX
                        + (partner.z() - (double) bestZ) * dirZ));
        return new RailStation(city.id(), city.name(), bestX, bestZ, bestSurface + 1,
                bestX + acrossX * BOARD_ACROSS * side, bestZ + acrossZ * BOARD_ACROSS * side,
                dirX, dirZ, outSign == 0 ? 1 : outSign);
    }

    /**
     * Uses the authored {@code STATION} anchor when the city plan has one. The anchor is the
     * station site; its nearest authored rail becomes the rail point, so a plan's platform, shed
     * and track stay aligned. {@code data} must say {@code north_south} or {@code east_west} to
     * fix the orientation; a rail's own shape is the fallback, then the partner bearing.
     */
    /** The package station facing {@code partner}, or {@code null} when the city has none. */
    private static @Nullable RailStation packageStation(City city, City partner) {
        var entry = com.sanchous98.eldritchhorror.world.city.MapPackage.entry(city.id());
        if (entry == null || entry.stations().isEmpty()) {
            return null;
        }
        double toX = RailGeometry.deltaX(city.x(), partner.x());
        double toZ = partner.z() - (double) city.z();
        double length = Math.max(1.0, Math.hypot(toX, toZ));
        double ux = toX / length;
        double uz = toZ / length;
        var best = entry.stations().get(0);
        double bestScore = -Double.MAX_VALUE;
        for (var station : entry.stations()) {
            double dx = Math.clamp(station.dx(), -4800, 4800);
            double dz = Math.clamp(station.dz(), -4800, 4800);
            double distance = Math.max(1.0, Math.hypot(dx, dz));
            double score = dx / distance * ux + dz / distance * uz;
            if (score > bestScore) {
                bestScore = score;
                best = station;
            }
        }
        int x = city.x() + Math.clamp(best.dx(), -4800, 4800);
        int z = city.z() + Math.clamp(best.dz(), -4800, 4800);
        boolean alongX = best.axis().contains("east_west");
        int dirX = alongX ? 1 : 0;
        int dirZ = alongX ? 0 : 1;
        int boardX = x - dirZ * BOARD_ACROSS;
        int boardZ = z + dirX * BOARD_ACROSS;
        int sign = Integer.signum((int) Math.round(ux * dirX + uz * dirZ));
        return new RailStation(city.id(), city.name(), x, z, best.y() == null ? 64 : best.y(),
                boardX, boardZ, dirX, dirZ, sign == 0 ? 1 : sign);
    }

    private static @Nullable RailStation authoredStation(EarthMap map, City city, City partner) {
        CityPlan plan = CityPlans.forCity(city.name());
        if (plan == null) {
            return null;
        }
        for (CityPoi poi : plan.pois()) {
            if (poi.kind() != CityPoi.Kind.STATION) {
                continue;
            }
            int anchorX = city.x() + poi.dx();
            int anchorZ = city.z() + poi.dz();
            String data = poi.data().toLowerCase(Locale.ROOT);
            boolean northSouth = data.contains("north_south") || data.contains("south_north");
            boolean eastWest = data.contains("east_west") || data.contains("west_east");
            CityPlanProbe.Rail rail = authoredRailNear(map, city, plan, anchorX, anchorZ);
            int x = rail == null ? anchorX : rail.x();
            int z = rail == null ? anchorZ : rail.z();
            int y = rail == null
                    ? CityLocation.authoredGround(map, city) + poi.dy() + 1
                    : rail.y();
            int dirX = 0;
            int dirZ = 0;
            if (northSouth) {
                dirZ = 1;
            } else if (eastWest) {
                dirX = 1;
            } else if (rail != null) {
                boolean railEastWest = axisX(rail) == 1;
                dirX = railEastWest ? 1 : 0;
                dirZ = railEastWest ? 0 : 1;
            } else {
                double bearing = Math.atan2(partner.z() - (double) city.z(),
                        RailGeometry.deltaX(city.x(), partner.x()));
                dirX = Math.abs(Math.cos(bearing)) >= Math.abs(Math.sin(bearing))
                        ? (int) Math.signum(Math.round(Math.cos(bearing))) : 0;
                dirZ = dirX == 0 ? (int) Math.signum(Math.round(Math.sin(bearing))) : 0;
                if (dirX == 0 && dirZ == 0) {
                    dirX = 1;
                }
            }
            int acrossX = -dirZ;
            int acrossZ = dirX;
            // The authored anchor sits on the platform/apron, so its side of the rail is the side
            // the station is boarded from. Fall back to the city side when the anchor is on the
            // centreline itself.
            int side = Integer.signum((anchorX - x) * acrossX + (anchorZ - z) * acrossZ);
            if (side == 0) {
                side = Integer.signum((city.x() - x) * acrossX + (city.z() - z) * acrossZ);
            }
            if (side == 0) {
                side = 1;
            }
            int outSign = Integer.signum((int) Math.round(
                    RailGeometry.deltaX(x, partner.x()) * dirX
                            + (partner.z() - (double) z) * dirZ));
            return new RailStation(city.id(), city.name(), x, z, y,
                    x + acrossX * BOARD_ACROSS * side, z + acrossZ * BOARD_ACROSS * side,
                    dirX, dirZ, outSign == 0 ? 1 : outSign);
        }
        return null;
    }

    /** @return 1 for an east-west authored rail, 0 otherwise (including no rail). */
    private static int axisX(CityPlanProbe.@Nullable Rail rail) {
        if (rail == null) {
            return 0;
        }
        RailShape shape = rail.state().getValue(RailBlock.SHAPE);
        return shape == RailShape.EAST_WEST || shape == RailShape.ASCENDING_EAST
                || shape == RailShape.ASCENDING_WEST ? 1 : 0;
    }

    private static CityPlanProbe.@Nullable Rail authoredRailNear(EarthMap map, City city,
                                                                CityPlan plan, int x, int z) {
        List<CityPlanProbe.Rail> cached = AUTHORED_RAILS.get(city.id());
        if (cached == null) {
            int ground = CityLocation.authoredGround(map, city);
            Palette palette = Palette.fromBiome(map.koppenClass(city.x(), city.z()), false);
            CityPlanProbe probe = CityPlanProbe.record(plan, palette, city.x(), city.z(), ground,
                    new MapTerrain(map), CityPlanProbe.Bounds.around(x, z, RAIL_SNAP_RADIUS));
            CityPlanProbe.Rail rail = probe.nearestRail(x, z, RAIL_SNAP_RADIUS);
            cached = rail == null ? List.of() : List.of(rail);
            AUTHORED_RAILS.put(city.id(), cached);
        }
        return cached.isEmpty() ? null : cached.getFirst();
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

    /** @return squared cylinder/plane distance between two city centres. */
    public static double distanceSqr(City a, City b) {
        double dx = RailGeometry.deltaX(a.x(), b.x());
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

    /** The baked map, as the headless plan probe sees it. */
    private record MapTerrain(EarthMap map) implements CityPlanProbe.Terrain {

        @Override
        public int surfaceY(int x, int z) {
            return surface(this.map, x, z);
        }

        @Override
        public boolean isLand(int x, int z) {
            return this.map.isLand(x, z);
        }
    }
}
