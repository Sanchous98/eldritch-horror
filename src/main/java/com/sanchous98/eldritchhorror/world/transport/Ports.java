package com.sanchous98.eldritchhorror.world.transport;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.world.EarthMap;
import com.sanchous98.eldritchhorror.world.EarthMaps;
import com.sanchous98.eldritchhorror.world.city.Cities;
import com.sanchous98.eldritchhorror.world.city.City;
import java.util.ArrayList;
import java.util.List;
import org.jspecify.annotations.Nullable;

/**
 * Picks the harbours of the curated cities ({@code design/30-transport.md}).
 *
 * <p>A city is a port when a 32-point ring scan finds ocean within {@value #MAX_OFFSET_BLOCKS}
 * blocks of its centre: at this map's scale a real harbour city (London, Rome) can sit a few hundred
 * blocks inland, while inland cities (Paris, Moscow) are thousands. The harbour point itself is six
 * blocks seaward of the first water sample, far enough out that the packet floats clear of the
 * beach. The scan is read-only over the baked landmask, so it costs no chunks.
 *
 * <p>Server-side and cached once. Never call from client code.
 */
public final class Ports {
    /** How far from a city centre ocean may be and still count as its harbour. */
    public static final int MAX_OFFSET_BLOCKS = 320;
    /** Directions sampled around a city (22.5 degrees apart). */
    private static final int DIRECTIONS = 32;
    /** Search step in blocks. */
    private static final int STEP = 4;
    /** How far seaward the harbour point sits from the first water sample. */
    private static final int OFFSHORE = 6;

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
            }
        }
        EldritchHorror.LOGGER.info("transport: {} coastal ports from {} curated cities",
                out.size(), Cities.all().size());
        return List.copyOf(out);
    }

    private static @Nullable Port harbour(EarthMap map, City city) {
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
        return new Port(city.id(), city.name(), waterX, waterZ, shoreX, shoreZ);
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
}
