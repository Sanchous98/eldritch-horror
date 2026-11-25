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
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.properties.RailShape;
import org.jspecify.annotations.Nullable;

/**
 * The stage-2 rail network ({@code design/30-transport.md}): a small, curated set of station pairs,
 * each with a real A* route over the baked landmask and a finished block-level track.
 *
 * <p>Selection is nearest-reachable: for each curated city, up to {@code maxPartnersPerCity} of the
 * closest other cities whose coarse route fits inside {@code trainMaxLineBlocks} become partners,
 * one line per partner. Duplicate pairs collapse, so the result is still a sparse graph rather than
 * every city pair. A city gets one station per partner (a major city's authored terminal can serve
 * several directions); a city with no reachable partner still gets a (dormant) station platform.
 *
 * <p>Routes are computed once on first use and cached for the server's lifetime; a tick never
 * pathfinds. The finished line is bucket-indexed by chunk, so worldgen pays a map lookup and a tiny
 * list per chunk. Server-side only: the builder reads {@link EarthMaps} and samples the world map.
 */
public final class RailLines {
    /** How many nearest cities are tried per wanted partner when looking for a reachable one. */
    private static final int PARTNER_ATTEMPTS = 5;
    /** Partner cap used when the server config is not loaded (bare unit probes). */
    private static final int DEFAULT_MAX_PARTNERS = 3;
    /** First platform column from the rail centreline: the edge sits three blocks out. */
    private static final int PLATFORM_INNER = 3;
    /** Last platform column from the rail centreline: five columns, so the platform is 5 wide. */
    private static final int PLATFORM_OUTER = 7;
    /** Canopy course above the rail (five clear blocks + the slab). */
    private static final int PLATFORM_ROOF = 6;
    /** Spacing of canopy pillars and lamps along the platform. */
    private static final int PLATFORM_POST_SPACING = 8;

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
        int maxBlocks = ModConfig.loaded()
                ? Math.clamp(ModConfig.TRAIN_MAX_LINE_BLOCKS.get(), 500, 400_000)
                : 25_000;
        int maxPartners = ModConfig.loaded()
                ? Math.clamp(ModConfig.MAX_PARTNERS_PER_CITY.get(), 1, 8)
                : DEFAULT_MAX_PARTNERS;
        RailRouteFinder.Options options = RailRouteFinder.Options.forLine(maxBlocks);
        RailGrid grid = grid(map);
        RailRouteFinder.Scratch scratch = new RailRouteFinder.Scratch();

        Map<String, City> byId = new HashMap<>();
        for (City city : cities) {
            byId.put(city.id(), city);
        }
        Map<String, List<City>> partners = choosePartners(cities, grid, options, maxBlocks,
                maxPartners, scratch);

        TreeMap<String, City[]> edges = new TreeMap<>();
        for (Map.Entry<String, List<City>> entry : partners.entrySet()) {
            City a = byId.get(entry.getKey());
            if (a == null) {
                continue;
            }
            for (City b : entry.getValue()) {
                edges.putIfAbsent(pairKey(a.id(), b.id()), orderedPair(byId, a.id(), b.id()));
            }
        }

        TreeMap<String, RailStation> stationMap = new TreeMap<>();
        Map<String, RailStation> placed = new HashMap<>();
        Set<String> stationCities = new HashSet<>();
        for (City[] edge : edges.values()) {
            placeStation(map, stationMap, placed, stationCities, edge[0], edge[1]);
            placeStation(map, stationMap, placed, stationCities, edge[1], edge[0]);
        }
        for (City city : cities) {
            if (stationCities.contains(city.id())) {
                continue;
            }
            City partner = RailStations.nearestOther(city, cities);
            if (partner == null) {
                partner = city;
            }
            placeStation(map, stationMap, placed, stationCities, city, partner);
        }

        List<RailLine> lines = new ArrayList<>();
        for (City[] edge : edges.values()) {
            RailLine line = buildLine(map, grid, options, maxBlocks, stationMap, edge[0], edge[1],
                    scratch);
            if (line != null) {
                lines.add(line);
            }
        }
        RailStation[] stationArray = new LinkedHashSet<>(stationMap.values())
                .toArray(new RailStation[0]);
        Map<Long, List<RailStamp>> stamps = buildStamps(lines, stationArray, map);
        EldritchHorror.LOGGER.info("rail: {} lines from {} pairs, {} stations in {} ms",
                lines.size(), edges.size(), stationArray.length,
                (System.nanoTime() - started) / 1_000_000L);
        return new Network(List.copyOf(lines), List.of(stationArray), stamps);
    }

    /**
     * Places the station for one end of one edge, reusing an existing platform when the scan lands
     * on the same site (an authored terminal serves every direction from one building).
     */
    private static void placeStation(EarthMap map, Map<String, RailStation> stationMap,
                                     Map<String, RailStation> placed, Set<String> stationCities,
                                     City city, City partner) {
        RailStation station = RailStations.place(map, city, partner);
        String site = city.id() + "|" + station.x() + "|" + station.z();
        RailStation existing = placed.putIfAbsent(site, station);
        if (existing != null) {
            station = existing;
        }
        stationMap.put(city.id() + "|" + partner.id(), station);
        stationCities.add(city.id());
    }

    /**
     * For each city, tries its nearest candidates on the coarse grid and keeps up to
     * {@code maxPartners} whose route fits. The returned map is keyed by the selecting city, so two
     * cities can select each other (the pair is deduplicated later). A pair's reachability is
     * checked once and reused when the other city tries the same candidate.
     */
    private static Map<String, List<City>> choosePartners(List<City> cities, RailGrid grid,
                                                          RailRouteFinder.Options options,
                                                          int maxBlocks, int maxPartners,
                                                          RailRouteFinder.Scratch scratch) {
        Map<String, List<City>> partners = new HashMap<>();
        Map<String, Boolean> reachable = new HashMap<>();
        for (City city : cities) {
            List<City> candidates = new ArrayList<>(cities);
            candidates.removeIf(other -> other.id().equals(city.id()));
            candidates.sort((left, right) -> {
                double byDistance = Double.compare(RailStations.distanceSqr(city, left),
                        RailStations.distanceSqr(city, right));
                return byDistance != 0 ? (int) Math.signum(byDistance)
                        : left.id().compareTo(right.id());
            });
            int attempts = Math.min(PARTNER_ATTEMPTS * maxPartners, candidates.size());
            List<City> chosen = new ArrayList<>(maxPartners);
            for (int i = 0; i < attempts && chosen.size() < maxPartners; i++) {
                City candidate = candidates.get(i);
                // Candidates are sorted by distance, so every later one is also out of range.
                if (Math.sqrt(RailStations.distanceSqr(city, candidate)) > maxBlocks) {
                    break;
                }
                String key = pairKey(city.id(), candidate.id());
                Boolean fits = reachable.get(key);
                if (fits == null) {
                    fits = routeFits(grid, options, maxBlocks, city, candidate, scratch);
                    reachable.put(key, fits);
                }
                if (fits) {
                    chosen.add(candidate);
                }
            }
            if (!chosen.isEmpty()) {
                partners.put(city.id(), List.copyOf(chosen));
            }
        }
        return partners;
    }

    private static boolean routeFits(RailGrid grid, RailRouteFinder.Options options, int maxBlocks,
                                     City from, City to, RailRouteFinder.Scratch scratch) {
        RailCell start = anchor(grid, from.x(), from.z());
        RailCell goal = anchor(grid, to.x(), to.z());
        if (start == null || goal == null) {
            return false;
        }
        List<RailCell> route = RailRouteFinder.find(grid, start, goal, options, scratch);
        return route != null
                && RailRouteFinder.pathLength(route, grid.width()) * RailGrid.CELL_BLOCKS
                <= maxBlocks;
    }

    /** @return the undirected pair key, in stable id order. */
    private static String pairKey(String cityA, String cityB) {
        return cityA.compareTo(cityB) <= 0 ? cityA + "|" + cityB : cityB + "|" + cityA;
    }

    /** @return the edge's two cities in stable id order. */
    private static City[] orderedPair(Map<String, City> byId, String cityA, String cityB) {
        return cityA.compareTo(cityB) <= 0
                ? new City[]{byId.get(cityA), byId.get(cityB)}
                : new City[]{byId.get(cityB), byId.get(cityA)};
    }

    private static @Nullable RailLine buildLine(EarthMap map, RailGrid grid,
                                                RailRouteFinder.Options options, int maxBlocks,
                                                Map<String, RailStation> stations,
                                                City cityA, City cityB,
                                                RailRouteFinder.Scratch scratch) {
        RailStation a = stations.get(cityA.id() + "|" + cityB.id());
        RailStation b = stations.get(cityB.id() + "|" + cityA.id());
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
        for (RailStamp stamp : stationStamps(station)) {
            addStamp(index, stamp);
        }
    }

    /**
     * The station's block plan: a five-wide platform on each side with its edge three blocks from
     * the rail centreline, five clear blocks above the rail head, a canopy at rail+6, pillars,
     * hanging lanterns, a sign on the city-side platform and a buffer stop at the rear end. The
     * buffer stamp is written before the line stamps, so a real line crossing it clears it.
     *
     * @param station the station to describe
     * @return every stamp the station writes, in worldgen order
     */
    static List<RailStamp> stationStamps(RailStation station) {
        List<RailStamp> out = new ArrayList<>();
        int dirX = station.dirX();
        int dirZ = station.dirZ();
        int acrossX = station.acrossX();
        int acrossZ = station.acrossZ();
        int middle = (PLATFORM_INNER + PLATFORM_OUTER) / 2;
        int side = citySide(station);
        // The consist spawns nose-first at the station point, so its 64-80 blocks trail behind in
        // the direction opposite the partner. The platform covers that side (plus a short front
        // apron) rather than running symmetrically into the terminus building.
        int outSign = station.outSign();
        int front = 5 * outSign;
        int back = -80 * outSign;
        int first = Math.min(front, back);
        int last = Math.max(front, back);
        for (int along = first; along <= last; along++) {
            int baseX = station.x() + dirX * along;
            int baseZ = station.z() + dirZ * along;
            for (int offset = PLATFORM_INNER; offset <= PLATFORM_OUTER; offset++) {
                int across = side * offset;
                int x = baseX + acrossX * across;
                int z = baseZ + acrossZ * across;
                out.add(RailStamp.floor(x, station.y() - 1, z));
                out.add(RailStamp.canopy(x, station.y() + PLATFORM_ROOF, z));
            }
            // Keep the rail corridor itself open even when no line reaches this station.
            for (int across = -2; across <= 2; across++) {
                int x = baseX + acrossX * across;
                int z = baseZ + acrossZ * across;
                for (int dy = 1; dy <= PLATFORM_ROOF - 1; dy++) {
                    out.add(RailStamp.clear(x, station.y() + dy, z));
                }
            }
        }
        for (int along = first; along <= last; along++) {
            if (Math.floorMod(along, PLATFORM_POST_SPACING) == 0) {
                continue;
            }
            int across = side * PLATFORM_OUTER;
            out.add(RailStamp.fence(station.x() + dirX * along + acrossX * across,
                    station.y(), station.z() + dirZ * along + acrossZ * across));
        }
        for (int along = first; along <= last; along += PLATFORM_POST_SPACING) {
            int across = side * PLATFORM_OUTER;
            int x = station.x() + dirX * along + acrossX * across;
            int z = station.z() + dirZ * along + acrossZ * across;
            for (int dy = 1; dy <= PLATFORM_ROOF - 1; dy++) {
                out.add(RailStamp.pillar(x, station.y() + dy, z));
            }
            // One warm lamp at the canopy course (rail+6), leaving the five-clear volume intact.
            int lampAcross = side * middle;
            out.add(RailStamp.lantern(
                    station.x() + dirX * along + acrossX * lampAcross,
                    station.y() + PLATFORM_ROOF,
                    station.z() + dirZ * along + acrossZ * lampAcross));
        }
        int signAcross = side * middle;
        int signAlong = front - 4 * outSign;
        out.add(RailStamp.sign(
                station.x() + dirX * signAlong + acrossX * signAcross,
                station.y() + 1,
                station.z() + dirZ * signAlong + acrossZ * signAcross,
                station.cityName()));
        int bufferAlong = back - outSign * 2;
        for (int across = -1; across <= 1; across++) {
            int x = station.x() + dirX * bufferAlong + acrossX * across;
            int z = station.z() + dirZ * bufferAlong + acrossZ * across;
            for (int dy = 1; dy <= 2; dy++) {
                out.add(RailStamp.buffer(x, station.y() + dy, z));
            }
        }
        return out;
    }

    /** @return -1 or +1: the side of the track the city (and therefore the main platform) is on. */
    private static int citySide(RailStation station) {
        int dx = station.boardX() - station.x();
        int dz = station.boardZ() - station.z();
        int side = Integer.signum(dx * station.acrossX() + dz * station.acrossZ());
        return side == 0 ? 1 : side;
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
