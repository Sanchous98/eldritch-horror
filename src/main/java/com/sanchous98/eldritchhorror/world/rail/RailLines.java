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
 * each with a real route over the terrain and a finished line.
 *
 * <p>Selection is nearest-first: for each curated city, up to {@code maxPartnersPerCity} of the
 * closest other cities inside {@code trainMaxLineBlocks} become partners, one line per partner.
 * Duplicate pairs collapse, so the result is still a sparse graph rather than every city pair. A
 * city gets one station per partner (a major city's authored terminal can serve several
 * directions); a city with no partner still gets a (dormant) station platform.
 *
 * <p>The route is planned in two levels by {@link RailPlanner}: a coarse 512-block intercity board
 * for the long middle and a fine 64-block board for the last few kilometres around each station.
 * This keeps a 180 km route at a few hundred A* cells instead of thousands.
 *
 * <p>A line longer than {@code trainMaxCorridorBlocks} is still a line — stations, timetable and
 * consist — but its worldgen corridor is <b>not</b> stamped: a 180 km corridor is about 11,000
 * chunks, and a train ride would generate them all. The consist travels the planned polyline and
 * the ground is whatever the generator produced.
 *
 * <p>Routes are computed once on first use and cached for the server's lifetime; a tick never
 * pathfinds. The finished corridor is bucket-indexed by chunk, so worldgen pays a map lookup and a
 * tiny list per chunk. Server-side only: the builder reads {@link EarthMaps} and samples the world
 * map.
 */
public final class RailLines {
    /** Partner cap used when the server config is not loaded (bare unit probes). */
    private static final int DEFAULT_MAX_PARTNERS = 3;
    /** Line cap used when the server config is not loaded (the 1:1 map's longest pair). */
    private static final int DEFAULT_MAX_LINE_BLOCKS = 400_000;
    /** Corridor cap used when the server config is not loaded. */
    private static final int DEFAULT_MAX_CORRIDOR_BLOCKS = 40_000;
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

    /** Forgets the cached network (a new world or a test that installs another city table). */
    public static void clear() {
        cached = null;
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
        // Whichever path builds the network first (startup event or the first chunk stamp), give
        // the station resolver the live level so stops land on real streets, not package heights.
        var server = net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer();
        if (server != null && server.overworld() != null) {
            RailStations.useSurface(server.overworld());
        }
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
                : DEFAULT_MAX_LINE_BLOCKS;
        int maxCorridor = ModConfig.loaded()
                ? Math.clamp(ModConfig.TRAIN_MAX_CORRIDOR_BLOCKS.get(), 0, 400_000)
                : DEFAULT_MAX_CORRIDOR_BLOCKS;
        int maxPartners = ModConfig.loaded()
                ? Math.clamp(ModConfig.MAX_PARTNERS_PER_CITY.get(), 1, 8)
                : DEFAULT_MAX_PARTNERS;
        RailPlanner planner = new RailPlanner(map, cities, maxBlocks);

        Map<String, City> byId = new HashMap<>();
        for (City city : cities) {
            byId.put(city.id(), city);
        }
        Map<String, List<City>> partners = choosePartners(cities, maxBlocks, maxPartners);

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
        int corridors = 0;
        for (City[] edge : edges.values()) {
            RailLine line = buildLine(map, planner, maxBlocks, maxCorridor, stationMap,
                    edge[0], edge[1]);
            if (line != null) {
                lines.add(line);
                if (line.hasCorridor()) {
                    corridors++;
                }
            }
        }
        RailStation[] stationArray = new LinkedHashSet<>(stationMap.values())
                .toArray(new RailStation[0]);
        Map<Long, List<RailStamp>> stamps = buildStamps(lines, stationArray, map);
        EldritchHorror.LOGGER.info("rail: {} lines ({} with a stamped corridor) from {} pairs, "
                        + "{} stations in {} ms",
                lines.size(), corridors, edges.size(), stationArray.length,
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
     * For each city, keeps up to {@code maxPartners} nearest candidates whose straight-line distance
     * fits {@code maxBlocks}. The route itself is planned once when the line is built, so selection
     * never runs A* and cannot be aborted by a path budget. A relaxed route always reaches its goal,
     * which is what lets every curated city of a 1:1 map keep a partner.
     */
    private static Map<String, List<City>> choosePartners(List<City> cities, int maxBlocks,
                                                          int maxPartners) {
        Map<String, List<City>> partners = new HashMap<>();
        for (City city : cities) {
            List<City> candidates = new ArrayList<>(cities);
            candidates.removeIf(other -> other.id().equals(city.id()));
            candidates.sort((left, right) -> {
                double byDistance = Double.compare(RailStations.distanceSqr(city, left),
                        RailStations.distanceSqr(city, right));
                return byDistance != 0 ? (int) Math.signum(byDistance)
                        : left.id().compareTo(right.id());
            });
            List<City> chosen = new ArrayList<>(maxPartners);
            for (City candidate : candidates) {
                if (chosen.size() >= maxPartners) {
                    break;
                }
                // Candidates are sorted by distance, so every later one is also out of range.
                if (Math.sqrt(RailStations.distanceSqr(city, candidate)) > maxBlocks) {
                    break;
                }
                chosen.add(candidate);
            }
            if (!chosen.isEmpty()) {
                partners.put(city.id(), List.copyOf(chosen));
            }
        }
        return partners;
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

    private static @Nullable RailLine buildLine(EarthMap map, RailPlanner planner, int maxBlocks,
                                                int maxCorridor,
                                                Map<String, RailStation> stations,
                                                City cityA, City cityB) {
        RailStation a = stations.get(cityA.id() + "|" + cityB.id());
        RailStation b = stations.get(cityB.id() + "|" + cityA.id());
        if (a == null || b == null) {
            return null;
        }
        List<RailPathMath.RailPoint> waypoints = planner.route(a, b);
        if (waypoints == null) {
            EldritchHorror.LOGGER.info("rail: no route {} -> {}", a.cityId(), b.cityId());
            return null;
        }
        double length = planner.length(waypoints);
        if (length > maxBlocks) {
            EldritchHorror.LOGGER.info("rail: route {} -> {} is {} blocks, over the {} cap",
                    a.cityId(), b.cityId(), Math.round(length), maxBlocks);
            return null;
        }
        boolean corridor = length <= maxCorridor;
        List<RailPathMath.RailPoint> points = corridor
                ? RailPathMath.orthogonalize(waypoints, RailPathMath.DEFAULT_RUN,
                        planner.wrapWidth())
                : waypoints;
        RailProfile.Surface surface = surface(map);
        int[] heights = corridor
                ? RailProfile.build(points, surface, ElevationCurve.SEA_LEVEL, a.y(), b.y())
                : RailProfile.buildWaypoints(points, surface, ElevationCurve.SEA_LEVEL,
                        a.y(), b.y());
        if (corridor) {
            levelledSelfCrossings(a, b, points, heights);
        }
        List<RailNode> nodes = new ArrayList<>(points.size());
        RailNode firstBridge = null;
        RailNode firstTunnel = null;
        for (int i = 0; i < points.size(); i++) {
            RailPathMath.RailPoint point = points.get(i);
            RailNode node = new RailNode(point.x(), heights[i], point.z());
            nodes.add(node);
            boolean water = surface.water(point.x(), point.z());
            if (water && firstBridge == null) {
                firstBridge = node;
            }
            if (!water && surface.surfaceY(point.x(), point.z()) > heights[i] + 1
                    && firstTunnel == null) {
                firstTunnel = node;
            }
        }
        RailLine line = new RailLine(a.cityId() + "-" + b.cityId(), a, b, nodes,
                firstBridge, firstTunnel, corridor, planner.wrapWidth());
        EldritchHorror.LOGGER.info("rail: line {} ({} blocks, {} nodes, corridor {}, "
                        + "bridge {}, tunnel {})",
                line.id(), Math.round(line.length()), nodes.size(), corridor ? "yes" : "no",
                firstBridge == null ? "none" : firstBridge.x() + " " + firstBridge.y() + " "
                        + firstBridge.z(),
                firstTunnel == null ? "none" : firstTunnel.x() + " " + firstTunnel.y() + " "
                        + firstTunnel.z());
        return line;
    }

    /**
     * A rasterised diagonal can cross its own earlier segment. Both passes share one rail block;
     * giving both the higher profile prevents a lower rail being buried in ballast. Only a stamped
     * corridor is rasterised, so this runs only for short lines.
     */
    private static void levelledSelfCrossings(RailStation a, RailStation b,
                                              List<RailPathMath.RailPoint> points,
                                              int[] heights) {
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
            EldritchHorror.LOGGER.info("rail: {} has {} self-crossing blocks, levelled",
                    a.cityId() + "-" + b.cityId(), crossings);
        }
    }

    private static RailProfile.Surface surface(EarthMap map) {
        return new SurfaceCache(map);
    }

    /**
     * A block-level view of the baked map with a one-pixel cache. Corridor stamps are laid one
     * block at a time, and eight consecutive blocks share one 8-block map pixel; without the cache
     * the network build samples the same pixel ~8× (and the elevation layer four times per sample).
     */
    private static final class SurfaceCache implements RailProfile.Surface {
        private final EarthMap map;
        private int pixelX = Integer.MIN_VALUE;
        private int pixelZ = Integer.MIN_VALUE;
        private boolean water;
        private int surfaceY;

        private SurfaceCache(EarthMap map) {
            this.map = map;
        }

        @Override
        public boolean water(int x, int z) {
            sample(x, z);
            return this.water;
        }

        @Override
        public int surfaceY(int x, int z) {
            sample(x, z);
            return this.surfaceY;
        }

        private void sample(int x, int z) {
            int px = Math.floorMod(Math.floorDiv(x + EarthMap.HALF_WIDTH,
                    EarthMap.BLOCKS_PER_PIXEL), this.map.pixelWidth());
            int pz = Math.clamp(Math.floorDiv(z + EarthMap.HALF_HEIGHT,
                    EarthMap.BLOCKS_PER_PIXEL), 0, this.map.pixelHeight() - 1);
            if (px == this.pixelX && pz == this.pixelZ) {
                return;
            }
            this.pixelX = px;
            this.pixelZ = pz;
            this.water = !this.map.isLand(x, z);
            this.surfaceY = RailStations.surface(this.map, x, z);
        }
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
            if (line.hasCorridor()) {
                addLineStamps(index, line, map);
            }
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
        RailProfile.Surface surface = surface(map);
        for (int i = 0; i < nodes.size(); i++) {
            RailNode previous = nodes.get(Math.max(0, i - 1));
            RailNode current = nodes.get(i);
            RailNode next = nodes.get(Math.min(nodes.size() - 1, i + 1));
            int dirX = (int) Math.signum(line.wrapDelta(current.x(), next.x()));
            int dirZ = Integer.signum(next.z() - current.z());
            if (dirX == 0 && dirZ == 0) {
                dirX = (int) Math.signum(line.wrapDelta(previous.x(), current.x()));
                dirZ = Integer.signum(current.z() - previous.z());
            }
            RailShape shape = shape(previous, current, next, line);
            boolean bridge = surface.water(current.x(), current.z());
            boolean tunnel = !bridge
                    && surface.surfaceY(current.x(), current.z()) > current.y() + 1;
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

    private static RailShape shape(RailNode previous, RailNode current, RailNode next,
                                   RailLine line) {
        int inX = (int) Math.signum(line.wrapDelta(previous.x(), current.x()));
        int inZ = Integer.signum(current.z() - previous.z());
        int outX = (int) Math.signum(line.wrapDelta(current.x(), next.x()));
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
