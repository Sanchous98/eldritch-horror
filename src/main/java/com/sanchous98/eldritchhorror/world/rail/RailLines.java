package com.sanchous98.eldritchhorror.world.rail;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.core.ModConfig;
import com.sanchous98.eldritchhorror.world.EarthMap;
import com.sanchous98.eldritchhorror.world.EarthMaps;
import com.sanchous98.eldritchhorror.world.ElevationCurve;
import com.sanchous98.eldritchhorror.world.city.Cities;
import com.sanchous98.eldritchhorror.world.city.City;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.properties.RailShape;
import org.jspecify.annotations.Nullable;

/**
 * The stage-2 rail network ({@code design/30-transport.md}): a small, curated set of station pairs,
 * each with a real A* route over the baked landmask and a finished block-level track.
 *
 * <p>Selection is nearest-reachable: for each curated city, the closest other city whose coarse
 * route fits inside {@code trainMaxLineBlocks} becomes a partner; duplicate pairs collapse, so the
 * result is a sparse graph rather than every city pair. A city with no reachable partner still gets
 * a (dormant) station platform.
 *
 * <p>Routes are computed once on first use and cached for the server's lifetime; a tick never
 * pathfinds. The finished line is bucket-indexed by chunk, so worldgen pays a map lookup and a tiny
 * list per chunk. Server-side only: the builder reads {@link EarthMaps} and samples the world map.
 */
public final class RailLines {
    /** How many nearest cities are tried per city when looking for a land-reachable partner. */
    private static final int PARTNER_ATTEMPTS = 5;
    /** Station platform half-length along the track, in blocks. */
    private static final int PLATFORM_HALF_LENGTH = 12;
    /** Station platform half-width across the track, in blocks. */
    private static final int PLATFORM_HALF_WIDTH = 3;

    private static volatile @Nullable Network cached;

    private RailLines() {
    }

    /** @return every line whose route exists (built on first call). */
    public static List<RailLine> all() {
        return snapshot().lines();
    }

    /** @return every curated station, including stations no line reaches. */
    public static List<RailStation> stations() {
        return snapshot().stations();
    }

    /** @return the pre-bucketed worldgen stamps, keyed by {@link ChunkPos#pack(int, int)}. */
    public static Map<Long, List<RailStamp>> stampsByChunk() {
        return snapshot().stamps();
    }

    /** @return the line with this id, or {@code null}. */
    public static @Nullable RailLine byId(String id) {
        return snapshot().byId().get(id);
    }

    /** @return the lines that use a station at this city. */
    public static List<RailLine> atCity(String cityId) {
        List<RailLine> out = new ArrayList<>();
        for (RailLine line : all()) {
            if (line.a().cityId().equals(cityId) || line.b().cityId().equals(cityId)) {
                out.add(line);
            }
        }
        return out;
    }

    /** @return the station nearest a world point, or {@code null} when there are no cities. */
    public static @Nullable RailStation nearest(double x, double z) {
        RailStation best = null;
        double bestDistance = Double.MAX_VALUE;
        for (RailStation station : stations()) {
            double distance = station.distanceSqr(x, z);
            if (distance < bestDistance) {
                bestDistance = distance;
                best = station;
            }
        }
        return best;
    }

    private static Network snapshot() {
        Network local = cached;
        if (local == null) {
            synchronized (RailLines.class) {
                local = cached;
                if (local == null) {
                    local = build();
                    cached = local;
                }
            }
        }
        return local;
    }

    private static Network build() {
        EarthMap map = EarthMaps.getOrNull();
        if (map == null) {
            EldritchHorror.LOGGER.warn("rail: earth map missing, no lines");
            return Network.empty();
        }
        List<City> cities = Cities.all();
        if (cities.isEmpty()) {
            EldritchHorror.LOGGER.warn("rail: no curated cities, no lines");
            return Network.empty();
        }
        long started = System.nanoTime();
        int maxBlocks = Math.clamp(ModConfig.TRAIN_MAX_LINE_BLOCKS.get(), 500, 60_000);
        RailRouteFinder.Options options = RailRouteFinder.Options.forLine(maxBlocks);
        RailGrid grid = grid(map);
        RailRouteFinder.Scratch scratch = new RailRouteFinder.Scratch();

        Map<String, City> byId = new HashMap<>();
        for (City city : cities) {
            byId.put(city.id(), city);
        }
        Map<String, City> partners = choosePartners(cities, grid, options, maxBlocks, scratch);

        TreeMap<String, RailStation> stationMap = new TreeMap<>();
        for (City city : cities) {
            City partner = partners.get(city.id());
            if (partner == null) {
                partner = RailStations.nearestOther(city, cities);
            }
            if (partner == null) {
                partner = city;
            }
            stationMap.put(city.id(), RailStations.place(map, city, partner));
        }

        TreeMap<String, City[]> edges = new TreeMap<>();
        for (Map.Entry<String, City> entry : partners.entrySet()) {
            City a = byId.get(entry.getKey());
            City b = entry.getValue();
            if (a == null || b == null) {
                continue;
            }
            String[] ids = a.id().compareTo(b.id()) <= 0
                    ? new String[]{a.id(), b.id()} : new String[]{b.id(), a.id()};
            edges.putIfAbsent(ids[0] + "|" + ids[1],
                    new City[]{byId.get(ids[0]), byId.get(ids[1])});
        }

        List<RailLine> lines = new ArrayList<>();
        for (City[] edge : edges.values()) {
            RailLine line = buildLine(map, grid, options, maxBlocks, stationMap, edge[0], edge[1],
                    scratch);
            if (line != null) {
                lines.add(line);
            }
        }
        RailStation[] stationArray = stationMap.values().toArray(new RailStation[0]);
        Map<Long, List<RailStamp>> stamps = buildStamps(lines, stationArray, map);
        EldritchHorror.LOGGER.info("rail: {} lines, {} stations in {} ms", lines.size(),
                stationArray.length, (System.nanoTime() - started) / 1_000_000L);
        return new Network(List.copyOf(lines), List.of(stationArray), stamps);
    }

    /**
     * For each city, tries its nearest candidates on the coarse grid and keeps the first whose route
     * fits. The returned map is keyed by the selecting city, so two cities can select each other
     * (the pair is deduplicated later).
     */
    private static Map<String, City> choosePartners(List<City> cities, RailGrid grid,
                                                    RailRouteFinder.Options options, int maxBlocks,
                                                    RailRouteFinder.Scratch scratch) {
        Map<String, City> partners = new HashMap<>();
        for (City city : cities) {
            List<City> candidates = new ArrayList<>(cities);
            candidates.removeIf(other -> other.id().equals(city.id()));
            candidates.sort((left, right) -> {
                double byDistance = Double.compare(RailStations.distanceSqr(city, left),
                        RailStations.distanceSqr(city, right));
                return byDistance != 0 ? (int) Math.signum(byDistance)
                        : left.id().compareTo(right.id());
            });
            int attempts = Math.min(PARTNER_ATTEMPTS, candidates.size());
            for (int i = 0; i < attempts; i++) {
                City candidate = candidates.get(i);
                if (Math.sqrt(RailStations.distanceSqr(city, candidate)) > maxBlocks) {
                    continue;
                }
                RailCell start = anchor(grid, city.x(), city.z());
                RailCell goal = anchor(grid, candidate.x(), candidate.z());
                if (start == null || goal == null) {
                    continue;
                }
                List<RailCell> route = RailRouteFinder.find(grid, start, goal, options, scratch);
                if (route == null) {
                    continue;
                }
                if (RailRouteFinder.pathLength(route, grid.width()) * RailGrid.CELL_BLOCKS
                        > maxBlocks) {
                    continue;
                }
                partners.put(city.id(), candidate);
                break;
            }
        }
        return partners;
    }

    private static @Nullable RailLine buildLine(EarthMap map, RailGrid grid,
                                                RailRouteFinder.Options options, int maxBlocks,
                                                Map<String, RailStation> stations,
                                                City cityA, City cityB,
                                                RailRouteFinder.Scratch scratch) {
        RailStation a = stations.get(cityA.id());
        RailStation b = stations.get(cityB.id());
        if (a == null || b == null) {
            return null;
        }
        RailCell start = anchor(grid, a.x(), a.z());
        RailCell goal = anchor(grid, b.x(), b.z());
        if (start == null || goal == null) {
            return null;
        }
        List<RailCell> route = RailRouteFinder.find(grid, start, goal, options, scratch);
        if (route == null) {
            EldritchHorror.LOGGER.info("rail: no route {} -> {}", a.cityId(), b.cityId());
            return null;
        }
        List<RailPathMath.RailPoint> waypoints = new ArrayList<>();
        waypoints.add(new RailPathMath.RailPoint(a.x(), a.z()));
        for (RailCell cell : route) {
            waypoints.add(new RailPathMath.RailPoint(centreX(cell.x()), centreZ(cell.z())));
        }
        waypoints.add(new RailPathMath.RailPoint(b.x(), b.z()));
        List<RailPathMath.RailPoint> points =
                RailPathMath.orthogonalize(waypoints, RailPathMath.DEFAULT_RUN);
        int[] heights = RailProfile.build(points, surface(map), ElevationCurve.SEA_LEVEL,
                a.y(), b.y());
        Map<Long, Integer> highest = new HashMap<>();
        for (int i = 0; i < points.size(); i++) {
            RailPathMath.RailPoint point = points.get(i);
            highest.merge(blockKey(point.x(), point.z()), heights[i], Math::max);
        }
        int crossings = 0;
        for (int i = 0; i < points.size(); i++) {
            RailPathMath.RailPoint point = points.get(i);
            int top = highest.get(blockKey(point.x(), point.z()));
            if (top != heights[i]) {
                heights[i] = top;
                crossings++;
            }
        }
        if (crossings > 0) {
            // A rasterised diagonal can cross its own earlier segment. Both passes share one rail
            // block; giving both the higher profile prevents a lower rail being buried in ballast.
            EldritchHorror.LOGGER.info("rail: {} has {} self-crossing blocks, levelled",
                    a.cityId() + "-" + b.cityId(), crossings);
        }
        List<RailNode> nodes = new ArrayList<>(points.size());
        RailNode firstBridge = null;
        RailNode firstTunnel = null;
        for (int i = 0; i < points.size(); i++) {
            RailPathMath.RailPoint point = points.get(i);
            RailNode node = new RailNode(point.x(), heights[i], point.z());
            nodes.add(node);
            boolean water = isWater(map, point.x(), point.z());
            if (water && firstBridge == null) {
                firstBridge = node;
            }
            if (!water && RailStations.surface(map, point.x(), point.z()) > heights[i] + 1
                    && firstTunnel == null) {
                firstTunnel = node;
            }
        }
        double length = RailPathMath.length(points);
        if (length > maxBlocks) {
            EldritchHorror.LOGGER.info("rail: route {} -> {} is {} blocks, over the {} cap",
                    a.cityId(), b.cityId(), Math.round(length), maxBlocks);
            return null;
        }
        RailLine line = new RailLine(a.cityId() + "-" + b.cityId(), a, b, nodes,
                firstBridge, firstTunnel);
        EldritchHorror.LOGGER.info("rail: line {} ({} blocks, {} nodes, bridge {}, tunnel {})",
                line.id(), Math.round(length), nodes.size(),
                firstBridge == null ? "none" : firstBridge.x() + " " + firstBridge.y() + " "
                        + firstBridge.z(),
                firstTunnel == null ? "none" : firstTunnel.x() + " " + firstTunnel.y() + " "
                        + firstTunnel.z());
        return line;
    }

    private static RailGrid grid(EarthMap map) {
        int width = 2 * EarthMap.HALF_WIDTH / RailGrid.CELL_BLOCKS;
        int height = 2 * EarthMap.HALF_HEIGHT / RailGrid.CELL_BLOCKS;
        return RailGrid.of(width, height, new RailGrid.Terrain() {
            @Override
            public boolean isLand(int cellX, int cellZ) {
                return map.isLand(centreX(cellX), centreZ(cellZ));
            }

            @Override
            public int surfaceY(int cellX, int cellZ) {
                return RailStations.surface(map, centreX(cellX), centreZ(cellZ));
            }
        });
    }

    private static @Nullable RailCell anchor(RailGrid grid, int blockX, int blockZ) {
        int baseX = Math.floorDiv(blockX + EarthMap.HALF_WIDTH, RailGrid.CELL_BLOCKS);
        int baseZ = Math.floorDiv(blockZ + EarthMap.HALF_HEIGHT, RailGrid.CELL_BLOCKS);
        for (int ring = 0; ring <= 8; ring++) {
            for (int dz = -ring; dz <= ring; dz++) {
                for (int dx = -ring; dx <= ring; dx++) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != ring) {
                        continue;
                    }
                    int x = grid.wrapX(baseX + dx);
                    int z = baseZ + dz;
                    if (z >= 0 && z < grid.height() && grid.land(x, z)) {
                        return new RailCell(x, z);
                    }
                }
            }
        }
        return null;
    }

    private static RailProfile.Surface surface(EarthMap map) {
        return new RailProfile.Surface() {
            @Override
            public boolean water(int x, int z) {
                return isWater(map, x, z);
            }

            @Override
            public int surfaceY(int x, int z) {
                return RailStations.surface(map, x, z);
            }
        };
    }

    private static boolean isWater(EarthMap map, int x, int z) {
        return !map.isLand(x, z);
    }

    private static Map<Long, List<RailStamp>> buildStamps(List<RailLine> lines,
                                                          RailStation[] stations,
                                                          EarthMap map) {
        Map<Long, List<RailStamp>> index = new HashMap<>();
        // Stations first: a track that curves inside the platform must still lay its rails last,
        // otherwise the platform's head-room clearing erases the rail block.
        for (RailStation station : stations) {
            addStationStamps(index, station);
        }
        for (RailLine line : lines) {
            addLineStamps(index, line, map);
        }
        Map<Long, List<RailStamp>> sealed = new HashMap<>();
        for (Map.Entry<Long, List<RailStamp>> entry : index.entrySet()) {
            sealed.put(entry.getKey(), List.copyOf(entry.getValue()));
        }
        return Map.copyOf(sealed);
    }

    private static void addLineStamps(Map<Long, List<RailStamp>> index, RailLine line,
                                      EarthMap map) {
        List<RailNode> nodes = line.nodes();
        for (int i = 0; i < nodes.size(); i++) {
            RailNode previous = nodes.get(Math.max(0, i - 1));
            RailNode current = nodes.get(i);
            RailNode next = nodes.get(Math.min(nodes.size() - 1, i + 1));
            int dirX = Integer.signum(EarthMap.wrapDelta(current.x(), next.x()));
            int dirZ = Integer.signum(next.z() - current.z());
            if (dirX == 0 && dirZ == 0) {
                dirX = Integer.signum(EarthMap.wrapDelta(previous.x(), current.x()));
                dirZ = Integer.signum(current.z() - previous.z());
            }
            RailShape shape = shape(previous, current, next);
            boolean bridge = isWater(map, current.x(), current.z());
            boolean tunnel = !bridge
                    && RailStations.surface(map, current.x(), current.z()) > current.y() + 1;
            RailStamp stamp = RailStamp.track(current.x(), current.y(), current.z(), shape,
                    bridge, tunnel, dirX, dirZ);
            addStamp(index, stamp);
        }
    }

    private static void addStationStamps(Map<Long, List<RailStamp>> index, RailStation station) {
        List<RailStamp> floors = new ArrayList<>();
        List<RailStamp> fences = new ArrayList<>();
        List<RailStamp> lanterns = new ArrayList<>();
        int dirX = station.dirX();
        int dirZ = station.dirZ();
        int acrossX = station.acrossX();
        int acrossZ = station.acrossZ();
        for (int along = -PLATFORM_HALF_LENGTH; along <= PLATFORM_HALF_LENGTH; along++) {
            for (int across = -PLATFORM_HALF_WIDTH; across <= PLATFORM_HALF_WIDTH; across++) {
                if (across == 0) {
                    continue; // keep the track's ballast and rails
                }
                int x = station.x() + dirX * along + acrossX * across;
                int z = station.z() + dirZ * along + acrossZ * across;
                floors.add(RailStamp.floor(x, station.y() - 1, z));
                if (Math.abs(across) == PLATFORM_HALF_WIDTH) {
                    fences.add(RailStamp.fence(x, station.y(), z));
                }
            }
        }
        for (int along = -10; along <= 10; along += 10) {
            for (int across = -PLATFORM_HALF_WIDTH; across <= PLATFORM_HALF_WIDTH;
                 across += 2 * PLATFORM_HALF_WIDTH) {
                int x = station.x() + dirX * along + acrossX * across;
                int z = station.z() + dirZ * along + acrossZ * across;
                lanterns.add(RailStamp.lantern(x, station.y() + 1, z));
            }
        }
        for (RailStamp stamp : floors) {
            addStamp(index, stamp);
        }
        for (RailStamp stamp : fences) {
            addStamp(index, stamp);
        }
        for (RailStamp stamp : lanterns) {
            addStamp(index, stamp);
        }
    }

    private static void addStamp(Map<Long, List<RailStamp>> index, RailStamp stamp) {
        long key = ChunkPos.pack(stamp.x() >> 4, stamp.z() >> 4);
        index.computeIfAbsent(key, ignored -> new ArrayList<>()).add(stamp);
    }

    private static RailShape shape(RailNode previous, RailNode current, RailNode next) {
        int inX = Integer.signum(EarthMap.wrapDelta(previous.x(), current.x()));
        int inZ = Integer.signum(current.z() - previous.z());
        int outX = Integer.signum(EarthMap.wrapDelta(current.x(), next.x()));
        int outZ = Integer.signum(next.z() - current.z());
        int toPrevX = -inX;
        int toPrevZ = -inZ;
        int toNextX = outX;
        int toNextZ = outZ;
        if (next.y() > current.y()) {
            return ascending(outX, outZ);
        }
        if (previous.y() > current.y()) {
            return ascending(toPrevX, toPrevZ);
        }
        boolean hasPrevious = toPrevX != 0 || toPrevZ != 0;
        boolean hasNext = toNextX != 0 || toNextZ != 0;
        if (!hasPrevious) {
            return straight(toNextX, toNextZ);
        }
        if (!hasNext) {
            return straight(toPrevX, toPrevZ);
        }
        if (toPrevX == toNextX && toPrevZ == toNextZ) {
            return straight(toNextX, toNextZ);
        }
        return curve(toPrevX, toPrevZ, toNextX, toNextZ);
    }

    private static RailShape ascending(int dx, int dz) {
        if (dx > 0) {
            return RailShape.ASCENDING_EAST;
        }
        if (dx < 0) {
            return RailShape.ASCENDING_WEST;
        }
        return dz > 0 ? RailShape.ASCENDING_SOUTH : RailShape.ASCENDING_NORTH;
    }

    private static RailShape straight(int dx, int dz) {
        return dx != 0 ? RailShape.EAST_WEST : RailShape.NORTH_SOUTH;
    }

    private static RailShape curve(int prevX, int prevZ, int nextX, int nextZ) {
        boolean north = prevZ < 0 || nextZ < 0;
        boolean south = prevZ > 0 || nextZ > 0;
        boolean east = prevX > 0 || nextX > 0;
        boolean west = prevX < 0 || nextX < 0;
        if (north && east) {
            return RailShape.NORTH_EAST;
        }
        if (north && west) {
            return RailShape.NORTH_WEST;
        }
        if (south && east) {
            return RailShape.SOUTH_EAST;
        }
        if (south && west) {
            return RailShape.SOUTH_WEST;
        }
        return straight(nextX, nextZ);
    }

    private static long blockKey(int x, int z) {
        return ((long) x << 32) ^ (z & 0xFFFFFFFFL);
    }

    private static int centreX(int cellX) {        return -EarthMap.HALF_WIDTH + cellX * RailGrid.CELL_BLOCKS + RailGrid.CELL_BLOCKS / 2;
    }

    private static int centreZ(int cellZ) {
        return -EarthMap.HALF_HEIGHT + cellZ * RailGrid.CELL_BLOCKS + RailGrid.CELL_BLOCKS / 2;
    }

    /** Immutable init snapshot: stations, lines and the chunk index. */
    private record Network(List<RailLine> lines, List<RailStation> stations,
                           Map<Long, List<RailStamp>> stamps, Map<String, RailLine> byId) {

        private Network(List<RailLine> lines, List<RailStation> stations,
                        Map<Long, List<RailStamp>> stamps) {
            this(lines, stations, stamps, index(lines));
        }

        private static Map<String, RailLine> index(List<RailLine> lines) {
            Map<String, RailLine> out = new HashMap<>();
            for (RailLine line : lines) {
                out.put(line.id(), line);
            }
            return Map.copyOf(out);
        }

        private static Network empty() {
            return new Network(List.of(), List.of(), Map.of(), Map.of());
        }
    }
}
