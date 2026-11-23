package com.sanchous98.eldritchhorror.world.transport;

import com.sanchous98.eldritchhorror.world.EarthMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.PriorityQueue;
import org.jspecify.annotations.Nullable;

/**
 * A lane through water the coarse {@link SeaGrid} cannot resolve: the last attempt before a
 * harbour is declared unreachable.
 *
 * <p>The 64-block sea grid samples one land fact per cell, so a strait narrower than the safety
 * margin (Gibraltar, the Gulf of Suez, the Bosphorus) is either missing or too thin to carry a
 * cell path. This class keeps the authored berth as the visual mooring and builds the lane from
 * the landmask's own 8-block pixels:
 *
 * <ol>
 *   <li>a <em>loose</em> grid marks a cell navigable when <b>any</b> of its pixels is water, and
 *       A* over it (corner cuts allowed, because only the pixel pass has to be honest) gives a
 *       corridor between the two berth cells;</li>
 *   <li>the corridor is threaded cell by cell: each hop either is straight water on the landmask
 *       or is replaced by a short pixel A* inside its own window, so the corridor only has to
 *       point the way — the landmask decides the lane;</li>
 *   <li>a corridor hop the landmask cannot honour marks its cell as blocked and the corridor is
 *       rebuilt, so a shortcut across an isthmus is abandoned for the real channel;</li>
 *   <li>a greedy line-of-sight pass drops waypoints it can skip without touching land.</li>
 * </ol>
 *
 * <p>Every returned polyline is validated against {@link EarthMap#isLand}, so it never crosses
 * land; where the map really is a land bridge (an isthmus, a filled canal) the pair stays
 * unlinked. Server-side and cached once.
 */
final class SeaChannel {
    private static final float SQRT2 = (float) Math.sqrt(2.0);
    /** Landmask sampling step while simplifying a finished lane, in blocks. */
    private static final int VALIDATE_STEP = 6;
    /** Corridors tried before the pair is given up: each retry blocks one failed hop. */
    private static final int MAX_ATTEMPTS = 8;
    /** Slack around a repaired hop, in blocks, so the pixel search can round a headland. */
    private static final int REPAIR_MARGIN = 192;
    /** Largest pixel window a single repair may search, per axis. */
    private static final int MAX_WINDOW_PIXELS = 512;
    /** Longest straight hop the simplifier will take between waypoints, in blocks. */
    private static final int MAX_HOP_BLOCKS = 512;
    /** How far a harbour point may be nudged onto a water pixel, in pixels. */
    private static final int SNAP_RADIUS_PIXELS = 4;

    private static volatile @Nullable Loose cached;

    private SeaChannel() {
    }

    /** The cached loose grid: any water pixel makes a cell navigable. */
    private record Loose(SeaGrid grid, boolean[] passable) {
    }

    /**
     * Threads a lane between two harbour water points through the landmask.
     *
     * @param map  the baked landmask
     * @param from origin harbour water point
     * @param to   destination harbour water point
     * @return a land-free polyline from {@code from} to {@code to}, or {@code null} when the two
     *     points are not connected by water at pixel resolution
     */
    static @Nullable List<SeaPoint> lane(EarthMap map, SeaPoint from, SeaPoint to) {
        Loose loose = loose(map);
        int width = loose.grid().width();
        int height = loose.grid().height();
        boolean[] blocked = new boolean[width * height];
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            SeaGrid grid = corridorGrid(loose, blocked);
            SeaCell start = cellOf(grid, from);
            SeaCell goal = cellOf(grid, to);
            if (start == null || goal == null) {
                return null;
            }
            List<SeaCell> cells = SeaRouteFinder.findRaw(grid, start, goal, 0, true);
            if (cells == null) {
                return null;
            }
            Repair repair = repair(map, cells, from, to);
            if (repair.points() != null) {
                return simplify(map, repair.points());
            }
            SeaCell failed = repair.failed();
            if (failed == null || blocked[failed.z() * width + failed.x()]) {
                return null;
            }
            blocked[failed.z() * width + failed.x()] = true;
        }
        return null;
    }

    private record Repair(@Nullable List<SeaPoint> points, @Nullable SeaCell failed) {
    }

    /**
     * Turns a corridor into a real lane, one cell at a time.
     *
     * @return either the threaded polyline or the corridor cell the landmask refused
     */
    private static Repair repair(EarthMap map, List<SeaCell> cells, SeaPoint from, SeaPoint to) {
        List<SeaPoint> points = new ArrayList<>();
        points.add(from);
        SeaPoint at = from;
        for (int i = 1; i < cells.size(); i++) {
            SeaCell cell = cells.get(i);
            SeaPoint target = waterPoint(map, cell, at);
            if (target == null) {
                return new Repair(null, cell);
            }
            List<SeaPoint> hop = hop(map, at, target);
            if (hop == null) {
                return new Repair(null, cell);
            }
            points.addAll(hop.subList(1, hop.size()));
            at = target;
        }
        List<SeaPoint> last = hop(map, at, to);
        if (last == null) {
            return new Repair(null, cells.get(cells.size() - 1));
        }
        points.addAll(last.subList(1, last.size()));
        return new Repair(points, null);
    }

    /** @return a land-free hop from {@code from} to {@code to}, or {@code null}. */
    private static @Nullable List<SeaPoint> hop(EarthMap map, SeaPoint from, SeaPoint to) {
        if (waterSegment(map, from, to)) {
            List<SeaPoint> direct = new ArrayList<>(2);
            direct.add(from);
            direct.add(to);
            return direct;
        }
        return pixelPath(map, from, to);
    }

    /**
     * @return the water pixel in the cell that is nearest to {@code near}, with the straight line
     *     from {@code near} clear of land when possible
     */
    private static @Nullable SeaPoint waterPoint(EarthMap map, SeaCell cell, SeaPoint near) {
        SeaPoint best = null;
        double bestDistance = Double.MAX_VALUE;
        int x0 = SeaGrid.centreX(cell.x()) - SeaGrid.CELL_BLOCKS / 2;
        int z0 = SeaGrid.centreZ(cell.z()) - SeaGrid.CELL_BLOCKS / 2;
        for (int dz = 0; dz < SeaGrid.CELL_BLOCKS; dz += EarthMap.BLOCKS_PER_PIXEL) {
            for (int dx = 0; dx < SeaGrid.CELL_BLOCKS; dx += EarthMap.BLOCKS_PER_PIXEL) {
                SeaPoint point = new SeaPoint(x0 + dx + EarthMap.BLOCKS_PER_PIXEL / 2,
                        z0 + dz + EarthMap.BLOCKS_PER_PIXEL / 2);
                if (map.isLand(point.x(), point.z())) {
                    continue;
                }
                double distance = near.distanceTo(point);
                if (distance < bestDistance) {
                    bestDistance = distance;
                    best = point;
                }
            }
        }
        return best;
    }

    /**
     * A* over the landmask's 8-block pixels inside a window around the hop.
     *
     * @return the polyline from {@code from} to {@code to} through water, or {@code null}
     */
    private static @Nullable List<SeaPoint> pixelPath(EarthMap map, SeaPoint from, SeaPoint to) {
        int margin = Math.max(1, REPAIR_MARGIN / EarthMap.BLOCKS_PER_PIXEL);
        int minX = Math.min(pixelX(from.x()), pixelX(to.x())) - margin;
        int maxX = Math.max(pixelX(from.x()), pixelX(to.x())) + margin;
        int minZ = Math.min(pixelZ(from.z()), pixelZ(to.z())) - margin;
        int maxZ = Math.max(pixelZ(from.z()), pixelZ(to.z())) + margin;
        int width = maxX - minX + 1;
        int height = maxZ - minZ + 1;
        if (width < 1 || height < 1 || width > MAX_WINDOW_PIXELS || height > MAX_WINDOW_PIXELS) {
            return null;
        }
        int size = width * height;
        float[] best = new float[size];
        Arrays.fill(best, Float.POSITIVE_INFINITY);
        int[] cameFrom = new int[size];
        Arrays.fill(cameFrom, -1);
        boolean[] closed = new boolean[size];
        int start = snap(map, minX, minZ, width, height, from);
        int goal = snap(map, minX, minZ, width, height, to);
        if (start < 0 || goal < 0) {
            return null;
        }
        int goalX = goal % width;
        int goalZ = goal / width;
        best[start] = 0.0F;
        PriorityQueue<Open> open = new PriorityQueue<>();
        open.add(new Open(heuristic(start % width, start / width, goalX, goalZ), start));
        while (!open.isEmpty()) {
            Open current = open.poll();
            int at = current.index();
            if (closed[at]) {
                continue;
            }
            closed[at] = true;
            if (at == goal) {
                return rebuild(map, minX, minZ, width, cameFrom, goal, from, to);
            }
            int px = minX + at % width;
            int pz = minZ + at / width;
            for (int dz = -1; dz <= 1; dz++) {
                for (int dx = -1; dx <= 1; dx++) {
                    if (dx == 0 && dz == 0) {
                        continue;
                    }
                    int nx = px + dx;
                    int nz = pz + dz;
                    if (!inWindow(minX, minZ, width, height, nx, nz) || !water(map, nx, nz)) {
                        continue;
                    }
                    if (dx != 0 && dz != 0 && (!water(map, nx, pz) || !water(map, px, nz))) {
                        continue;
                    }
                    int next = (nz - minZ) * width + nx - minX;
                    if (closed[next]) {
                        continue;
                    }
                    float tentative = best[at] + (dx != 0 && dz != 0 ? SQRT2 : 1.0F);
                    if (tentative < best[next]) {
                        best[next] = tentative;
                        cameFrom[next] = at;
                        open.add(new Open(tentative + heuristic(nx - minX, nz - minZ, goalX, goalZ),
                                next));
                    }
                }
            }
        }
        return null;
    }

    private static boolean inWindow(int minX, int minZ, int width, int height, int px, int pz) {
        return px >= minX && px < minX + width && pz >= minZ && pz < minZ + height;
    }

    /** @return the window-local index of the nearest water pixel to a harbour point, or -1. */
    private static int snap(EarthMap map, int minX, int minZ, int width, int height,
                            SeaPoint point) {
        int px = pixelX(point.x());
        int pz = pixelZ(point.z());
        for (int ring = 0; ring <= SNAP_RADIUS_PIXELS; ring++) {
            for (int dx = -ring; dx <= ring; dx++) {
                for (int dz = -ring; dz <= ring; dz++) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != ring) {
                        continue;
                    }
                    int x = px + dx;
                    int z = pz + dz;
                    if (!inWindow(minX, minZ, width, height, x, z) || !water(map, x, z)) {
                        continue;
                    }
                    if (!waterSegment(map, point,
                            new SeaPoint(pixelCentreX(x), pixelCentreZ(z)))) {
                        continue;
                    }
                    return (z - minZ) * width + x - minX;
                }
            }
        }
        return -1;
    }

    private static List<SeaPoint> rebuild(EarthMap map, int minX, int minZ, int width,
                                          int[] cameFrom, int goal, SeaPoint from, SeaPoint to) {
        List<SeaPoint> reversed = new ArrayList<>();
        for (int at = goal; at >= 0; at = cameFrom[at]) {
            int px = minX + at % width;
            int pz = minZ + at / width;
            reversed.add(new SeaPoint(pixelCentreX(px), pixelCentreZ(pz)));
        }
        List<SeaPoint> points = new ArrayList<>(reversed.size() + 2);
        points.add(from);
        for (int i = reversed.size() - 1; i >= 0; i--) {
            SeaPoint point = reversed.get(i);
            if (!point.equals(points.get(points.size() - 1))) {
                points.add(point);
            }
        }
        if (!to.equals(points.get(points.size() - 1))) {
            points.add(to);
        }
        return points;
    }

    private static boolean water(EarthMap map, int px, int pz) {
        return !map.isLand(pixelCentreX(Math.floorMod(px, pixelsWide())),
                pixelCentreZ(pz));
    }

    private static float heuristic(int fromX, int fromZ, int toX, int toZ) {
        int dx = Math.abs(fromX - toX);
        int dz = Math.abs(fromZ - toZ);
        int min = Math.min(dx, dz);
        return Math.max(dx, dz) + (SQRT2 - 1.0F) * min;
    }

    /** Greedy line-of-sight simplification over the real landmask. */
    private static List<SeaPoint> simplify(EarthMap map, List<SeaPoint> points) {
        List<SeaPoint> out = new ArrayList<>();
        out.add(points.get(0));
        int at = 0;
        int last = points.size() - 1;
        while (at < last) {
            int reach = Math.min(last, at + MAX_HOP_BLOCKS / EarthMap.BLOCKS_PER_PIXEL + 1);
            int next = at + 1;
            for (int candidate = reach; candidate > at + 1; candidate--) {
                if (waterSegment(map, points.get(at), points.get(candidate))) {
                    next = candidate;
                    break;
                }
            }
            out.add(points.get(next));
            at = next;
        }
        return out;
    }

    private static boolean waterSegment(EarthMap map, SeaPoint a, SeaPoint b) {
        double dx = EarthMap.wrapDelta(a.x(), b.x());
        double dz = b.z() - (double) a.z();
        int steps = Math.max(1, (int) Math.ceil(Math.sqrt(dx * dx + dz * dz) / VALIDATE_STEP));
        for (int step = 0; step <= steps; step++) {
            double t = step / (double) steps;
            int x = (int) Math.round(a.x() + dx * t);
            int z = (int) Math.round(a.z() + dz * t);
            if (map.isLand(x, z)) {
                return false;
            }
        }
        return true;
    }

    /**
     * The loose grid: a cell is navigable when any of its pixels is water. Sampled once and
     * cached, then copied with blocked cells for each corridor retry.
     */
    private static Loose loose(EarthMap map) {
        Loose local = cached;
        if (local == null) {
            int width = 2 * EarthMap.HALF_WIDTH / SeaGrid.CELL_BLOCKS;
            int height = 2 * EarthMap.HALF_HEIGHT / SeaGrid.CELL_BLOCKS;
            boolean[] passable = new boolean[width * height];
            for (int z = 0; z < height; z++) {
                for (int x = 0; x < width; x++) {
                    passable[z * width + x] = anyWaterPixel(map, x, z);
                }
            }
            local = new Loose(SeaGrid.of(width, height, 0,
                    (x, z) -> !passable[z * width + x]), passable);
            cached = local;
        }
        return local;
    }

    private static SeaGrid corridorGrid(Loose loose, boolean[] blocked) {
        int width = loose.grid().width();
        boolean[] passable = loose.passable();
        return SeaGrid.of(width, loose.grid().height(), 0,
                (x, z) -> !passable[z * width + x] || blocked[z * width + x]);
    }

    private static boolean anyWaterPixel(EarthMap map, int cellX, int cellZ) {
        int x0 = SeaGrid.centreX(cellX) - SeaGrid.CELL_BLOCKS / 2;
        int z0 = SeaGrid.centreZ(cellZ) - SeaGrid.CELL_BLOCKS / 2;
        for (int dz = 0; dz < SeaGrid.CELL_BLOCKS; dz += EarthMap.BLOCKS_PER_PIXEL) {
            for (int dx = 0; dx < SeaGrid.CELL_BLOCKS; dx += EarthMap.BLOCKS_PER_PIXEL) {
                if (!map.isLand(x0 + dx + EarthMap.BLOCKS_PER_PIXEL / 2,
                        z0 + dz + EarthMap.BLOCKS_PER_PIXEL / 2)) {
                    return true;
                }
            }
        }
        return false;
    }

    /** @return the navigable loose-grid cell holding or nearest to a water point. */
    private static @Nullable SeaCell cellOf(SeaGrid grid, SeaPoint point) {
        int centreX = SeaGrid.cellX(point.x());
        int centreZ = SeaGrid.cellZ(point.z());
        for (int ring = 0; ring <= SNAP_RADIUS_PIXELS; ring++) {
            for (int dx = -ring; dx <= ring; dx++) {
                for (int dz = -ring; dz <= ring; dz++) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != ring) {
                        continue;
                    }
                    int x = grid.wrapX(centreX + dx);
                    int z = centreZ + dz;
                    if (grid.water(x, z)) {
                        return new SeaCell(x, z);
                    }
                }
            }
        }
        return null;
    }

    private static int pixelsWide() {
        return 2 * EarthMap.HALF_WIDTH / EarthMap.BLOCKS_PER_PIXEL;
    }

    private static int pixelX(int blockX) {
        return Math.floorMod(Math.floorDiv(blockX + EarthMap.HALF_WIDTH, EarthMap.BLOCKS_PER_PIXEL),
                pixelsWide());
    }

    private static int pixelZ(int blockZ) {
        return Math.floorDiv(blockZ + EarthMap.HALF_HEIGHT, EarthMap.BLOCKS_PER_PIXEL);
    }

    private static int pixelCentreX(int px) {
        return px * EarthMap.BLOCKS_PER_PIXEL - EarthMap.HALF_WIDTH
                + EarthMap.BLOCKS_PER_PIXEL / 2;
    }

    private static int pixelCentreZ(int pz) {
        return pz * EarthMap.BLOCKS_PER_PIXEL - EarthMap.HALF_HEIGHT
                + EarthMap.BLOCKS_PER_PIXEL / 2;
    }

    /** One open-set entry; ordered by f-score, then by index for determinism. */
    private record Open(float score, int index) implements Comparable<Open> {
        @Override
        public int compareTo(Open other) {
            int byScore = Float.compare(this.score, other.score);
            return byScore != 0 ? byScore : Integer.compare(this.index, other.index);
        }
    }
}
