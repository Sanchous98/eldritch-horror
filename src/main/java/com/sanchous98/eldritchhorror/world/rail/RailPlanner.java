package com.sanchous98.eldritchhorror.world.rail;

import com.sanchous98.eldritchhorror.world.EarthMap;
import com.sanchous98.eldritchhorror.world.city.City;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.Nullable;

/**
 * Plans a rail line in two levels: a coarse intercity board for the long middle and a fine local
 * board for the last few kilometres around each station.
 *
 * <p>The old planner ran A* on a single 64-block board over the whole world. That was right for a
 * compressed map where curated cities sit about a kilometre apart, but a 1:1 map package places
 * them 16–280 km apart: a 180 km route is thousands of fine cells, and the old node budget gave up
 * long before the goal. The coarse board (512-block cells) keeps the intercity search at a few
 * hundred cells; the fine board (64-block cells) is sampled only in a 6 km square around each
 * station, so the track still leaves and enters the platform at neighbourhood scale.
 *
 * <p>A long route may cross an ocean that the fine board would refuse to bridge. The planner first
 * tries a strict route (short bridges only); if that fails, it retries with the water-run limit
 * raised to the whole route. Such a line is still a line, but its worldgen corridor is not stamped
 * (see {@code trainMaxCorridorBlocks}); the consist travels the planned polyline over whatever the
 * generator produced.
 */
final class RailPlanner {
    /** Intercity cell size: a 280 km world is still under a thousand cells across. */
    static final int INTERCITY_CELL = 512;
    /** Half-size of the fine station patch, in blocks: the last two kilometres each way. */
    static final int FINE_RADIUS = 2_048;
    /** Extra board around the city bounding box, in blocks, so a route may contour outward. */
    private static final int MARGIN = 8_192;
    /** Coarse bridge allowance for the strict attempt (two 512-block cells). */
    private static final int STRICT_BRIDGE_CELLS = 2;
    /** Fine bridge allowance (six 64-block cells), matching the old planner. */
    private static final int FINE_BRIDGE_CELLS = 6;
    /** Ring scan used to find a land cell (or the nearest valid cell) for an anchor. */
    private static final int ANCHOR_RINGS = 8;

    private final EarthMap map;
    private final int maxBlocks;
    private final int wrapWidth;
    private final RailGrid coarse;
    private final Map<Long, RailGrid> fine = new HashMap<>();
    private final RailRouteFinder.Scratch coarseScratch = new RailRouteFinder.Scratch();
    private final RailRouteFinder.Scratch fineScratch = new RailRouteFinder.Scratch();

    /**
     * @param map        the baked terrain layers (also the generator's oracle between cities)
     * @param cities     the curated cities of this world
     * @param maxBlocks  the configured line-length cap
     */
    RailPlanner(EarthMap map, List<City> cities, int maxBlocks) {
        this.map = map;
        this.maxBlocks = maxBlocks;
        this.wrapWidth = RailGeometry.wrapWidth();
        this.coarse = coarseGrid(cities);
    }

    /** @return the coarse intercity board (for probes and tests). */
    RailGrid coarse() {
        return this.coarse;
    }

    /**
     * Plans a line from one station to another.
     *
     * @param a first station
     * @param b second station
     * @return the waypoint chain from {@code a} to {@code b}, or {@code null} when even the relaxed
     *         search cannot reach the goal within the configured length cap
     */
    @Nullable List<RailPathMath.RailPoint> route(RailStation a, RailStation b) {
        RailCell coarseA = anchor(this.coarse, a.x(), a.z());
        RailCell coarseB = anchor(this.coarse, b.x(), b.z());
        if (coarseA == null || coarseB == null) {
            return null;
        }
        int maxCells = (int) Math.ceil(this.maxBlocks / (double) INTERCITY_CELL) + 2;
        List<RailCell> middle = null;
        if (this.coarse.land(coarseA.x(), coarseA.z())
                && this.coarse.land(coarseB.x(), coarseB.z())) {
            middle = RailRouteFinder.find(this.coarse, coarseA, coarseB,
                    RailRouteFinder.Options.of(maxCells, STRICT_BRIDGE_CELLS, 0.05),
                    this.coarseScratch);
        }
        if (middle == null) {
            middle = this.wrapWidth == 0
                    // A 1:1 package's ground between cities is prebuilt/unknown, so the relaxed
                    // attempt may bridge an ocean instead of touring a continent the baked layers
                    // only approximate.
                    ? RailRouteFinder.find(this.coarse, coarseA, coarseB,
                            RailRouteFinder.Options.of(maxCells, maxCells, 1.0, 0.05),
                            this.coarseScratch)
                    : RailRouteFinder.find(this.coarse, coarseA, coarseB,
                            RailRouteFinder.Options.of(maxCells, maxCells, 18.0, 0.05),
                            this.coarseScratch);
        }
        if (middle == null) {
            return null;
        }
        RailGrid fineA = fineGrid(a);
        RailGrid fineB = fineGrid(b);
        List<RailPathMath.RailPoint> points = new ArrayList<>();
        points.add(new RailPathMath.RailPoint(a.x(), a.z()));
        appendCells(points, fineA, fineRoute(fineA, a.x(), a.z(),
                this.coarse.centreX(coarseA.x()), this.coarse.centreZ(coarseA.z())));
        for (int i = 0; i < middle.size(); i++) {
            RailCell cell = middle.get(i);
            points.add(new RailPathMath.RailPoint(this.coarse.centreX(cell.x()),
                    this.coarse.centreZ(cell.z())));
        }
        List<RailCell> approach = fineRoute(fineB, this.coarse.centreX(coarseB.x()),
                this.coarse.centreZ(coarseB.z()), b.x(), b.z());
        if (approach != null) {
            for (int i = 1; i < approach.size(); i++) {
                RailCell cell = approach.get(i);
                points.add(new RailPathMath.RailPoint(fineB.centreX(cell.x()),
                        fineB.centreZ(cell.z())));
            }
        }
        points.add(new RailPathMath.RailPoint(b.x(), b.z()));
        return dedupe(points);
    }

    /**
     * @param points a waypoint chain
     * @return its horizontal length in blocks on the active geometry
     */
    double length(List<RailPathMath.RailPoint> points) {
        return RailPathMath.length(points, this.wrapWidth);
    }

    /** @return the wrap width this planner uses ({@code 0} on a 1:1 map plane). */
    int wrapWidth() {
        return this.wrapWidth;
    }

    private RailGrid coarseGrid(List<City> cities) {
        if (this.wrapWidth == 0 && !cities.isEmpty()) {
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
            int originX = minX - MARGIN;
            int originZ = minZ - MARGIN;
            int width = Math.max(2, (int) Math.ceil((maxX - minX + 2.0 * MARGIN)
                    / INTERCITY_CELL));
            int height = Math.max(2, (int) Math.ceil((maxZ - minZ + 2.0 * MARGIN)
                    / INTERCITY_CELL));
            return sample(width, height, INTERCITY_CELL, originX, originZ, false);
        }
        int width = Math.max(2, 2 * EarthMap.HALF_WIDTH / INTERCITY_CELL);
        int height = Math.max(2, 2 * EarthMap.HALF_HEIGHT / INTERCITY_CELL);
        return sample(width, height, INTERCITY_CELL, -EarthMap.HALF_WIDTH,
                -EarthMap.HALF_HEIGHT, true);
    }

    private RailGrid fineGrid(RailStation station) {
        long key = ((long) station.x() << 32) ^ (station.z() & 0xFFFFFFFFL);
        RailGrid cached = this.fine.get(key);
        if (cached != null) {
            return cached;
        }
        int size = Math.max(2, 2 * FINE_RADIUS / RailGrid.CELL_BLOCKS);
        RailGrid grid = sample(size, size, RailGrid.CELL_BLOCKS, station.x() - FINE_RADIUS,
                station.z() - FINE_RADIUS, false);
        this.fine.put(key, grid);
        return grid;
    }

    private RailGrid sample(int width, int height, int cellBlocks, int originX, int originZ,
                            boolean wrapped) {
        return RailGrid.of(width, height, cellBlocks, originX, originZ, wrapped,
                new RailGrid.Terrain() {
                    @Override
                    public boolean isLand(int cellX, int cellZ) {
                        return RailPlanner.this.map.isLand(blockX(cellX), blockZ(cellZ));
                    }

                    @Override
                    public int surfaceY(int cellX, int cellZ) {
                        return RailStations.surface(RailPlanner.this.map, blockX(cellX),
                                blockZ(cellZ));
                    }

                    private int blockX(int cellX) {
                        return originX + cellX * cellBlocks + cellBlocks / 2;
                    }

                    private int blockZ(int cellZ) {
                        return originZ + cellZ * cellBlocks + cellBlocks / 2;
                    }
                });
    }

    /**
     * @return the nearest land cell within the ring scan, else the base cell (a relaxed route may
     *         legitimately start on generated ocean)
     */
    private static @Nullable RailCell anchor(RailGrid grid, int blockX, int blockZ) {
        int baseX = grid.cellX(blockX);
        int baseZ = grid.cellZ(blockZ);
        for (int ring = 0; ring <= ANCHOR_RINGS; ring++) {
            for (int dz = -ring; dz <= ring; dz++) {
                for (int dx = -ring; dx <= ring; dx++) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != ring) {
                        continue;
                    }
                    int x = grid.wrapX(baseX + dx);
                    int z = baseZ + dz;
                    if (z >= 0 && z < grid.height() && x >= 0 && x < grid.width()
                            && grid.land(x, z)) {
                        return new RailCell(x, z);
                    }
                }
            }
        }
        int x = grid.wrapX(baseX);
        if (x < 0 || x >= grid.width() || baseZ < 0 || baseZ >= grid.height()) {
            return null;
        }
        return new RailCell(x, baseZ);
    }

    private @Nullable List<RailCell> fineRoute(RailGrid grid, int fromX, int fromZ, int toX,
                                               int toZ) {
        RailCell start = anchor(grid, fromX, fromZ);
        RailCell goal = anchor(grid, toX, toZ);
        if (start == null || goal == null) {
            return null;
        }
        int maxCells = 2 * FINE_RADIUS / RailGrid.CELL_BLOCKS + 2;
        return RailRouteFinder.find(grid, start, goal,
                RailRouteFinder.Options.of(maxCells, FINE_BRIDGE_CELLS, 0.05), this.fineScratch);
    }

    private static void appendCells(List<RailPathMath.RailPoint> points, RailGrid grid,
                                    @Nullable List<RailCell> cells) {
        if (cells == null) {
            return;
        }
        for (RailCell cell : cells) {
            points.add(new RailPathMath.RailPoint(grid.centreX(cell.x()), grid.centreZ(cell.z())));
        }
    }

    /** @return the chain with consecutive duplicate positions collapsed. */
    private static List<RailPathMath.RailPoint> dedupe(List<RailPathMath.RailPoint> points) {
        List<RailPathMath.RailPoint> out = new ArrayList<>(points.size());
        for (RailPathMath.RailPoint point : points) {
            if (out.isEmpty() || !out.get(out.size() - 1).equals(point)) {
                out.add(point);
            }
        }
        return out;
    }
}
