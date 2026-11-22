package com.sanchous98.eldritchhorror.world.transport;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.world.EarthMap;
import com.sanchous98.eldritchhorror.world.EarthMaps;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map;
import org.jspecify.annotations.Nullable;

/**
 * The stage-1 shipping network ({@code design/30-transport.md}): a small curated set of port pairs,
 * each with a real A* sea lane over the baked landmask.
 *
 * <p>Routes are computed once on first use and cached for the server's lifetime; a tick never
 * pathfinds. A pair whose lane cannot be found, or whose polyline still touches land, is simply
 * absent: "no lane, no line". The lane is bidirectional, so one A* serves both directions.
 *
 * <p>Server-side only: the route builder reads {@link EarthMaps} and samples the world map.
 */
public final class ShippingLines {
    /** Port pairs that make up the stage-1 network, in curated-city ids. */
    private static final String[][] PAIRS = {
            {"london", "new_york"},
            {"cape_town", "rio_de_janeiro"},
            {"sydney", "jakarta"},
            {"los_angeles", "lima"},
            {"bangkok", "jakarta"},
            // Trans-Pacific bridges: without one, the Asian/Australian sea component is isolated
            // from the Americas/Europe/Africa component.
            {"shanghai", "los_angeles"},
            {"tokyo", "los_angeles"},
    };

    /** Safety margin from land, in coarse cells (64 blocks each). */
    private static final int MARGIN_CELLS = 1;
    /** How far from a harbour a lane may use unsafe coastal water. */
    private static final int ESCAPE_CELLS = 8;
    /** How many cells to search for a harbour's route anchor. */
    private static final int ANCHOR_CELLS = 12;
    /** Landmask sampling step while validating a finished lane, in blocks. */
    private static final int VALIDATE_STEP = 6;

    private static volatile @Nullable List<ShippingLine> cached;
    private static volatile @Nullable Map<String, ShippingLine> byId;
    private static volatile @Nullable SeaGrid grid;

    private ShippingLines() {
    }

    /** @return every pair whose sea lane exists (built on first call). */
    public static List<ShippingLine> all() {
        List<ShippingLine> local = cached;
        if (local == null) {
            synchronized (ShippingLines.class) {
                local = cached;
                if (local == null) {
                    local = build();
                    cached = local;
                }
            }
        }
        return local;
    }

    /** @return the line with this id, or {@code null}. */
    public static @Nullable ShippingLine byId(String id) {
        Map<String, ShippingLine> local = byId;
        if (local == null) {
            Map<String, ShippingLine> built = new HashMap<>();
            for (ShippingLine line : all()) {
                built.put(line.id(), line);
            }
            byId = built;
            local = built;
        }
        return local.get(id);
    }

    private static List<ShippingLine> build() {
        EarthMap map = EarthMaps.getOrNull();
        if (map == null) {
            EldritchHorror.LOGGER.warn("transport: earth map missing, no sea lanes");
            return List.of();
        }
        long started = System.nanoTime();
        SeaGrid sea = grid(map);
        List<ShippingLine> lines = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (String[] pair : PAIRS) {
            Port from = Ports.byCity(pair[0]);
            Port to = Ports.byCity(pair[1]);
            if (from == null || to == null) {
                EldritchHorror.LOGGER.warn("transport: pair {} has no harbour, skipped", pair[0] + "-" + pair[1]);
                continue;
            }
            addLane(sea, map, from, to, seen, lines);
        }
        // Authored ports are spread over every coast; the fixed pairs alone leave most of them
        // isolated. Connect each port to its nearest neighbours so a player can actually reach the
        // authored harbour network (the route finder still rejects a lane that touches land).
        List<Port> ports = Ports.all();
        for (Port from : ports) {
            List<Port> candidates = new ArrayList<>(ports);
            candidates.removeIf(other -> other.cityId().equals(from.cityId()));
            candidates.sort((left, right) -> Double.compare(from.distanceSqr(
                    left.waterX(), left.waterZ()), from.distanceSqr(right.waterX(), right.waterZ())));
            int attempts = Math.min(3, candidates.size());
            for (int i = 0; i < attempts; i++) {
                addLane(sea, map, from, candidates.get(i), seen, lines);
            }
        }
        EldritchHorror.LOGGER.info("transport: {} sea lines in {} ms", lines.size(),
                (System.nanoTime() - started) / 1_000_000L);
        return List.copyOf(lines);
    }

    private static void addLane(SeaGrid sea, EarthMap map, Port from, Port to,
                                Set<String> seen, List<ShippingLine> lines) {
        String key = from.cityId().compareTo(to.cityId()) <= 0
                ? from.cityId() + "|" + to.cityId() : to.cityId() + "|" + from.cityId();
        if (!seen.add(key)) {
            return;
        }
        SeaRoute route = route(sea, map, from, to);
        if (route == null) {
            EldritchHorror.LOGGER.debug("transport: no sea lane {} -> {}", from.cityId(), to.cityId());
            return;
        }
        ShippingLine line = new ShippingLine(from.cityId() + "-" + to.cityId(), from, to, route);
        lines.add(line);
        EldritchHorror.LOGGER.info("transport: lane {} ({} blocks, {} waypoints)", line.id(),
                Math.round(route.length()), route.size());
    }

    private static SeaGrid grid(EarthMap map) {
        SeaGrid local = grid;
        if (local == null) {
            int width = 2 * EarthMap.HALF_WIDTH / SeaGrid.CELL_BLOCKS;
            int height = 2 * EarthMap.HALF_HEIGHT / SeaGrid.CELL_BLOCKS;
            local = SeaGrid.of(width, height, MARGIN_CELLS,
                    (cx, cz) -> map.isLand(centreX(cx), centreZ(cz)));
            grid = local;
        }
        return local;
    }

    private static @Nullable SeaRoute route(SeaGrid sea, EarthMap map, Port from, Port to) {
        SeaRoute safe = attempt(sea, map, from, to, true);
        if (safe != null) {
            return safe;
        }
        return attempt(sea, map, from, to, false);
    }

    /**
     * One lane attempt. The safe pass anchors both ends on water that already has the full land
     * margin and needs no coastal escape; if either harbour has no such anchor (New York's bay,
     * for example) the coastal pass anchors on plain water and lets A* hug the coast within
     * {@link #ESCAPE_CELLS} cells. The coastal pass prefers the smoothed lane but falls back to the
     * raw cell path, whose tighter turns are less likely to clip a headland.
     */
    private static @Nullable SeaRoute attempt(SeaGrid sea, EarthMap map, Port from, Port to,
                                              boolean safeAnchors) {
        SeaCell start = anchor(sea, map, from, safeAnchors);
        SeaCell goal = anchor(sea, map, to, safeAnchors);
        if (start == null || goal == null) {
            return null;
        }
        int escape = safeAnchors ? 0 : ESCAPE_CELLS;
        List<SeaCell> raw = SeaRouteFinder.findRaw(sea, start, goal, escape);
        if (raw == null) {
            if (!safeAnchors) {
                EldritchHorror.LOGGER.debug("transport: A* failed {} {}({},{}) -> {}({},{})",
                        from.cityId(), to.cityId(), start.x(), start.z(), to.cityId(),
                        goal.x(), goal.z());
            }
            return null;
        }
        SeaRoute lane = build(map, from, to, SeaRouteFinder.smooth(sea, raw));
        if (lane == null && !safeAnchors) {
            lane = build(map, from, to, raw);
        }
        if (lane == null && !safeAnchors) {
            EldritchHorror.LOGGER.debug("transport: lane {} -> {} touches land; rejected",
                    from.cityId(), to.cityId());
        }
        return lane;
    }

    private static @Nullable SeaRoute build(EarthMap map, Port from, Port to,
                                            List<SeaCell> cells) {
        List<SeaPoint> points = new ArrayList<>(cells.size() + 2);
        addPoint(points, new SeaPoint(from.waterX(), from.waterZ()));
        for (SeaCell cell : cells) {
            addPoint(points, new SeaPoint(centreX(cell.x()), centreZ(cell.z())));
        }
        addPoint(points, new SeaPoint(to.waterX(), to.waterZ()));
        if (points.size() < 2 || firstLand(map, points) != null) {
            return null;
        }
        double length = 0.0;
        for (int i = 1; i < points.size(); i++) {
            length += points.get(i - 1).distanceTo(points.get(i));
        }
        return new SeaRoute(points, length);
    }

    private static void addPoint(List<SeaPoint> points, SeaPoint point) {
        if (points.isEmpty() || !points.get(points.size() - 1).equals(point)) {
            points.add(point);
        }
    }

    private static @Nullable SeaCell anchor(SeaGrid sea, EarthMap map, Port port, boolean safeOnly) {
        SeaCell centre = new SeaCell(cellX(port.waterX()), cellZ(port.waterZ()));
        SeaCell best = null;
        double bestDistance = Double.MAX_VALUE;
        for (int ring = 0; ring <= ANCHOR_CELLS; ring++) {
            for (int dx = -ring; dx <= ring; dx++) {
                for (int dz = -ring; dz <= ring; dz++) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != ring) {
                        continue;
                    }
                    int x = sea.wrapX(centre.x() + dx);
                    int z = centre.z() + dz;
                    if (safeOnly ? !sea.safe(x, z) : !sea.water(x, z)) {
                        continue;
                    }
                    if (!waterSegment(map, port.waterX(), port.waterZ(),
                            centreX(x), centreZ(z))) {
                        continue;
                    }
                    double distance = (double) dx * dx + (double) dz * dz;
                    if (distance < bestDistance) {
                        bestDistance = distance;
                        best = new SeaCell(x, z);
                    }
                }
            }
            if (best != null) {
                return best;
            }
        }
        return null;
    }

    private static @Nullable SeaPoint firstLand(EarthMap map, List<SeaPoint> points) {
        for (int i = 1; i < points.size(); i++) {
            SeaPoint a = points.get(i - 1);
            SeaPoint b = points.get(i);
            // Longitude wraps: sample the short way round the seam.
            double dx = EarthMap.wrapDelta(a.x(), b.x());
            double dz = b.z() - (double) a.z();
            int steps = Math.max(1, (int) Math.ceil(Math.sqrt(dx * dx + dz * dz) / VALIDATE_STEP));
            for (int step = 0; step <= steps; step++) {
                double t = step / (double) steps;
                int x = (int) Math.round(a.x() + dx * t);
                int z = (int) Math.round(a.z() + dz * t);
                if (map.isLand(x, z)) {
                    return new SeaPoint(x, z);
                }
            }
        }
        return null;
    }

    private static boolean waterSegment(EarthMap map, int x1, int z1, int x2, int z2) {
        // Longitude wraps: sample the short way round the seam.
        double dx = EarthMap.wrapDelta(x1, x2);
        double dz = z2 - (double) z1;
        int steps = Math.max(1, (int) Math.ceil(Math.sqrt(dx * dx + dz * dz) / VALIDATE_STEP));
        for (int step = 0; step <= steps; step++) {
            double t = step / (double) steps;
            int x = (int) Math.round(x1 + dx * t);
            int z = (int) Math.round(z1 + dz * t);
            if (map.isLand(x, z)) {
                return false;
            }
        }
        return true;
    }

    private static int cellX(int blockX) {
        return Math.floorDiv(blockX + EarthMap.HALF_WIDTH, SeaGrid.CELL_BLOCKS);
    }

    private static int cellZ(int blockZ) {
        return Math.floorDiv(blockZ + EarthMap.HALF_HEIGHT, SeaGrid.CELL_BLOCKS);
    }

    private static int centreX(int cellX) {
        return -EarthMap.HALF_WIDTH + cellX * SeaGrid.CELL_BLOCKS + SeaGrid.CELL_BLOCKS / 2;
    }

    private static int centreZ(int cellZ) {
        return -EarthMap.HALF_HEIGHT + cellZ * SeaGrid.CELL_BLOCKS + SeaGrid.CELL_BLOCKS / 2;
    }
}
