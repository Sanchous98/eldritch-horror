package com.sanchous98.eldritchhorror.world.transport;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.world.EarthMap;
import com.sanchous98.eldritchhorror.world.EarthMaps;
import com.sanchous98.eldritchhorror.world.ElevationCurve;
import com.sanchous98.eldritchhorror.world.city.Cities;
import com.sanchous98.eldritchhorror.world.city.City;
import com.sanchous98.eldritchhorror.world.loc.Palette;
import com.sanchous98.eldritchhorror.world.loc.city.CityLocation;
import com.sanchous98.eldritchhorror.world.loc.city.plan.CityPlan;
import com.sanchous98.eldritchhorror.world.loc.city.plan.CityPlanProbe;
import com.sanchous98.eldritchhorror.world.loc.city.plan.CityPlans;
import com.sanchous98.eldritchhorror.world.loc.city.plan.CityPoi;
import java.util.ArrayList;
import java.util.List;
import org.jspecify.annotations.Nullable;

/**
 * Picks the harbours of the curated cities ({@code design/30-transport.md}).
 *
 * <p>An authored {@code PORT} anchor is authoritative: the plan is probed headlessly for the water
 * body next to the anchor, and that basin's own surface Y becomes the berth level. This is what
 * makes an authored river port reachable at the ship's Y. An authored basin that is inland, lifted
 * above the ocean, or dug into a land pixel the baked landmask knows nothing about cannot carry a
 * sea lane, so it falls back to real sea water along the anchor ray; only a city with no authored
 * anchor falls back to the 32-direction ring scan over the baked landmask, which cannot see a dug
 * basin.
 *
 * <p>Server-side and cached once. Never call from client code.
 */
public final class Ports {
    /** How far from a city centre ocean may be and still count as its harbour. */
    public static final int MAX_OFFSET_BLOCKS = 320;
    /** Reach of the map-water fallback for an authored anchor (Bangkok 344, Istanbul 326). */
    private static final int AUTHORED_REACH = 512;
    /** Directions sampled around a city (22.5 degrees apart). */
    private static final int DIRECTIONS = 32;
    /** Search step in blocks. */
    private static final int STEP = 4;
    /** How far seaward the harbour point sits from the first water sample. */
    private static final int OFFSHORE = 6;
    /** Region recorded around an authored anchor while probing its plan. */
    private static final int PROBE_RADIUS = 96;
    /** An authored berth may sit this far above/below sea level and still be a sea berth. */
    private static final int SEA_LEVEL_TOLERANCE = 2;
    /** Radius searched for open sea around an authored berth point. */
    private static final int SEA_CHECK_RADIUS = 8;

    private static volatile @Nullable List<Port> cached;

    private Ports() {
    }

    /** @return every curated city that has a harbour, in curated-city order. */
    public static List<Port> all() {
        List<Port> local = cached;
        if (local == null) {
            synchronized (Ports.class) {
                local = cached;
                if (local == null) {
                    local = build();
                    cached = local;
                }
            }
        }
        return local;
    }

    /** @return the harbour nearest a world point, or {@code null} when no city is coastal. */
    public static @Nullable Port nearest(double x, double z) {
        Port best = null;
        double bestDistance = Double.MAX_VALUE;
        for (Port port : all()) {
            double distance = port.distanceSqr(x, z);
            if (distance < bestDistance) {
                bestDistance = distance;
                best = port;
            }
        }
        return best;
    }

    /**
     * @param key a full curated city id ({@code london_-43_-18751}) or its short prefix
     *            ({@code london})
     * @return the harbour of that city, or {@code null} when it has none
     */
    public static @Nullable Port byCity(String key) {
        for (Port port : all()) {
            if (port.cityId().equals(key)) {
                return port;
            }
        }
        for (Port port : all()) {
            if (port.cityId().startsWith(key + "_")) {
                return port;
            }
        }
        return null;
    }

    private static List<Port> build() {
        EarthMap map = EarthMaps.getOrNull();
        if (map == null) {
            EldritchHorror.LOGGER.warn("transport: earth map missing, no ports");
            return List.of();
        }
        List<Port> out = new ArrayList<>();
        for (City city : Cities.all()) {
            Port port = harbour(map, city);
            if (port != null) {
                out.add(port);
                EldritchHorror.LOGGER.debug("transport: port {} berth ({}, {}) y={} sea={}",
                        port.cityId(), port.waterX(), port.waterZ(), port.waterY(),
                        !map.isLand(port.waterX(), port.waterZ()));
            }
        }
        EldritchHorror.LOGGER.info("transport: {} coastal ports from {} curated cities",
                out.size(), Cities.all().size());
        return List.copyOf(out);
    }

    private static @Nullable Port harbour(EarthMap map, City city) {
        CityPlan plan = CityPlans.forCity(city.name());
        if (plan != null) {
            Port authored = authoredHarbour(map, city, plan);
            if (authored != null) {
                return authored;
            }
        }
        return scannedHarbour(map, city);
    }

    /**
     * The authored anchor is authoritative. Its plan is probed for the water body next to it: a
     * berth point, the berth surface Y, and the anchor as the shore. The berth must itself be map
     * water — a basin the plan dug into a land pixel (Buenos Aires' delta quay) exists only in the
     * plan, so it cannot carry a sea lane and the city falls back to real sea water. When the plan
     * authored no water at all (an ocean quay whose basin is the baked sea), the map is walked
     * outward from the anchor instead. The authored reach is not capped at
     * {@link #MAX_OFFSET_BLOCKS}, which used to drop Bangkok and Istanbul.
     */
    private static @Nullable Port authoredHarbour(EarthMap map, City city, CityPlan plan) {
        for (CityPoi poi : plan.pois()) {
            if (poi.kind() != CityPoi.Kind.PORT) {
                continue;
            }
            int anchorX = city.x() + poi.dx();
            int anchorZ = city.z() + poi.dz();
            int ground = CityLocation.authoredGround(map, city);
            Palette palette = Palette.fromBiome(map.koppenClass(city.x(), city.z()), true);
            CityPlanProbe probe = CityPlanProbe.record(plan, palette, city.x(), city.z(), ground,
                    new MapTerrain(map), CityPlanProbe.Bounds.around(anchorX, anchorZ,
                            PROBE_RADIUS));
            CityPlanProbe.Berth berth = probe.waterBerth(anchorX, anchorZ, PROBE_RADIUS);
            if (berth != null
                    && !map.isLand(berth.x(), berth.z())
                    && openSeaNear(map, berth.x(), berth.z())
                    && Math.abs(berth.y() - ElevationCurve.SEA_LEVEL) <= SEA_LEVEL_TOLERANCE) {
                int shoreX = map.isLand(anchorX, anchorZ) ? anchorX : city.x();
                int shoreZ = map.isLand(anchorX, anchorZ) ? anchorZ : city.z();
                return new Port(city.id(), city.name(), berth.x(), berth.z(), shoreX, shoreZ,
                        berth.y());
            }
            // An authored basin that is inland or lifted above the ocean (Mexico City's canal,
            // Lima's cliff-top Callao basin, Jakarta's drainage canal) cannot carry a sea lane on
            // the baked landmask: the lane builder starts from a map-water point, and the packet
            // rides the ocean surface. Fall through to real sea water along the anchor ray so the
            // city still has a berth; the authored quay stays as shore scenery.
            int[] water = mapWaterFrom(map, city, anchorX, anchorZ, AUTHORED_REACH);
            if (water != null) {
                return new Port(city.id(), city.name(), water[0], water[1], anchorX, anchorZ,
                        ElevationCurve.SEA_LEVEL);
            }
        }
        return null;
    }

    /**
     * @return true when the baked landmask has ocean within {@link #SEA_CHECK_RADIUS} blocks, i.e.
     *     the authored basin is reachable from the sea by a short channel and the lane builder can
     *     start a voyage there.
     */
    private static boolean openSeaNear(EarthMap map, int x, int z) {
        for (int dx = -SEA_CHECK_RADIUS; dx <= SEA_CHECK_RADIUS; dx += STEP) {
            for (int dz = -SEA_CHECK_RADIUS; dz <= SEA_CHECK_RADIUS; dz += STEP) {
                if (!map.isLand(EarthMap.wrapBlockX(x + dx), z + dz)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * The map-water fallback for an authored anchor: walk the city-to-anchor ray outward until the
     * baked landmask says water.
     */
    private static int @Nullable [] mapWaterFrom(EarthMap map, City city, int anchorX, int anchorZ,
                                                 int reach) {
        if (!map.isLand(anchorX, anchorZ)) {
            return new int[]{anchorX, anchorZ};
        }
        double dx = anchorX - (double) city.x();
        double dz = anchorZ - (double) city.z();
        double length = Math.hypot(dx, dz);
        if (length < 1.0) {
            return null;
        }
        for (int distance = (int) Math.ceil(length); distance <= reach; distance += STEP) {
            int x = city.x() + (int) Math.round(dx / length * distance);
            int z = city.z() + (int) Math.round(dz / length * distance);
            if (!map.isLand(x, z)) {
                return new int[]{x, z};
            }
        }
        return null;
    }

    /**
     * The ring scan over the baked landmask. Used only for a city whose plan authors no PORT
     * anchor: it cannot see a dug basin, but it is still the right fallback for a plain coastline.
     */
    private static @Nullable Port scannedHarbour(EarthMap map, City city) {
        int radius = city.radius();
        int bestDistance = Integer.MAX_VALUE;
        double bestAngle = 0.0;
        for (int direction = 0; direction < DIRECTIONS; direction++) {
            double angle = direction * 2.0 * Math.PI / DIRECTIONS;
            int distance = firstWater(map, city, radius, angle);
            if (distance > 0 && distance < bestDistance) {
                bestDistance = distance;
                bestAngle = angle;
            }
        }
        if (bestDistance == Integer.MAX_VALUE) {
            return null;
        }
        double cos = Math.cos(bestAngle);
        double sin = Math.sin(bestAngle);
        int waterX = city.x() + (int) Math.round(cos * (bestDistance + OFFSHORE));
        int waterZ = city.z() + (int) Math.round(sin * (bestDistance + OFFSHORE));
        if (map.isLand(waterX, waterZ)) {
            waterX = city.x() + (int) Math.round(cos * bestDistance);
            waterZ = city.z() + (int) Math.round(sin * bestDistance);
        }
        int shoreX = city.x();
        int shoreZ = city.z();
        for (int back = bestDistance - STEP; back >= radius; back -= STEP) {
            int x = city.x() + (int) Math.round(cos * back);
            int z = city.z() + (int) Math.round(sin * back);
            if (map.isLand(x, z)) {
                shoreX = x;
                shoreZ = z;
                break;
            }
        }
        return new Port(city.id(), city.name(), waterX, waterZ, shoreX, shoreZ,
                ElevationCurve.SEA_LEVEL);
    }

    /**
     * @return the distance of the first offshore water sample along {@code angle}, or
     *         {@link Integer#MAX_VALUE} when the direction stays landlocked.
     */
    private static int firstWater(EarthMap map, City city, int radius, double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        for (int distance = radius + STEP; distance <= MAX_OFFSET_BLOCKS; distance += STEP) {
            int x = city.x() + (int) Math.round(cos * distance);
            int z = city.z() + (int) Math.round(sin * distance);
            if (map.isLand(x, z)) {
                continue;
            }
            // Require open water a little further out, so a one-pixel river or pond is not a port.
            int beyondOne = distance + 8;
            int beyondTwo = distance + 16;
            if (!map.isLand(city.x() + (int) Math.round(cos * beyondOne),
                    city.z() + (int) Math.round(sin * beyondOne))
                    && !map.isLand(city.x() + (int) Math.round(cos * beyondTwo),
                    city.z() + (int) Math.round(sin * beyondTwo))) {
                return distance;
            }
        }
        return Integer.MAX_VALUE;
    }

    /** The baked map, as the headless plan probe sees it. */
    private record MapTerrain(EarthMap map) implements CityPlanProbe.Terrain {

        @Override
        public int surfaceY(int x, int z) {
            return Math.clamp(ElevationCurve.toY(this.map.elevationMetres(x, z)), -60, 300);
        }

        @Override
        public boolean isLand(int x, int z) {
            return this.map.isLand(x, z);
        }
    }
}
