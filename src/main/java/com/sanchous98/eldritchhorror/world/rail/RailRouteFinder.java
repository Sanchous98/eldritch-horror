package com.sanchous98.eldritchhorror.world.rail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.PriorityQueue;
import org.jspecify.annotations.Nullable;

/**
 * A* over a {@link RailGrid}, used once per candidate line and cached afterwards.
 *
 * <p>Land costs one unit per cell plus a small penalty for the elevation change between cells;
 * water costs several units per cell and may only be crossed in short runs (a bridge), never as an
 * open-sea shortcut. Steep terrain is therefore cheaper to tunnel or contour around than to attack
 * head-on, and a long stretch of water makes the candidate unreachable instead of drawing a bridge
 * across an ocean.
 *
 * <p>The heuristic is octile distance on the cylinder (the X delta uses
 * {@link RailGrid#wrapDelta}); the returned path is simplified by greedy line-of-sight over land so
 * the block-level track has long straight runs. Pure: no Minecraft type appears here.
 */
public final class RailRouteFinder {
    private static final double SQRT2 = Math.sqrt(2.0);
    /** Safety cap for pathological searches (the geometric length bound normally stops them). */
    private static final int MAX_EXPANSIONS = 600_000;

    private RailRouteFinder() {
    }

    /**
     * Reusable working buffers for a batch of searches over one board.
     *
     * <p>Each search still clears every array it uses, so no state leaks between searches. Not
     * thread-safe: a holder belongs to one build and is never shared across calls from other
     * threads.
     */
    public static final class Scratch {
        private float[] cost = new float[0];
        private float[] along = new float[0];
        private int[] cameFrom = new int[0];
        private boolean[] closed = new boolean[0];

        private void ensure(int size) {
            if (this.cost.length < size) {
                this.cost = new float[size];
                this.along = new float[size];
                this.cameFrom = new int[size];
                this.closed = new boolean[size];
            }
        }
    }

    /**
     * Tuning for one search.
     *
     * @param maxWaterCells  longest consecutive run of water cells a bridge may span
     * @param maxLengthCells maximum geometric path length in cells
     * @param waterCost      cost of one water cell (land costs one)
     * @param slopeCost      cost per block of surface-height change between adjacent land cells
     * @param maxExpansions  safety cap on expanded cells; scaled with the route, not a fixed count
     */
    public record Options(int maxWaterCells, int maxLengthCells, double waterCost,
                          double slopeCost, int maxExpansions) {
        /** Validates the ranges. */
        public Options {
            if (maxWaterCells < 0 || maxLengthCells < 1 || maxExpansions < 1) {
                throw new IllegalArgumentException("invalid rail search bounds");
            }
            if (waterCost < 1.0 || slopeCost < 0.0) {
                throw new IllegalArgumentException("invalid rail search costs");
            }
        }

        /**
         * Legacy four-argument tuning: keeps the historical fixed expansion safety cap.
         *
         * @param maxWaterCells  longest consecutive run of water cells a bridge may span
         * @param maxLengthCells maximum geometric path length in cells
         * @param waterCost      cost of one water cell (land costs one)
         * @param slopeCost      cost per block of surface-height change between adjacent land cells
         */
        public Options(int maxWaterCells, int maxLengthCells, double waterCost,
                       double slopeCost) {
            this(maxWaterCells, maxLengthCells, waterCost, slopeCost, MAX_EXPANSIONS);
        }

        /**
         * The legacy tuning for a fine line at most {@code maxBlocks} long.
         *
         * <p>Up to six water cells (384 blocks at the 64-block grid) may be bridged: enough for a
         * channel or estuary at this compressed map scale, not enough for an open-sea shortcut.
         * Water costs eighteen land cells, so A* still contours around a lake when it can.
         *
         * @param maxBlocks the configured line cap
         * @return options allowing six water cells and a gradual climb
         */
        public static Options forLine(double maxBlocks) {
            int cells = (int) Math.ceil(Math.max(1.0, maxBlocks) / RailGrid.CELL_BLOCKS) + 2;
            return of(cells, 6, 0.05);
        }

        /**
         * Bounds for a route of {@code maxLengthCells} cells whose water allowance is explicit.
         *
         * <p>The expansion cap grows with the square of the route, so a 180 km intercity search is
         * not aborted by a budget that was tuned for a one-kilometre hop; the board itself remains
         * the hard stop, because a closed cell is never expanded twice.
         *
         * @param maxLengthCells maximum path length in cells
         * @param maxWaterCells  longest bridge, in cells
         * @param slopeCost      cost per block of height change
         * @return the options
         */
        public static Options of(int maxLengthCells, int maxWaterCells, double slopeCost) {
            return of(maxLengthCells, maxWaterCells, 18.0, slopeCost);
        }

        /**
         * Bounds with an explicit water cost. The relaxed intercity attempt uses a low water cost
         * because a 1:1 map's ground is prebuilt/unknown: a route should not tour a continent to
         * avoid a bay whose real banks the baked layers do not describe.
         *
         * @param maxLengthCells maximum path length in cells
         * @param maxWaterCells  longest bridge, in cells
         * @param waterCost      cost of one water cell (land costs one)
         * @param slopeCost      cost per block of height change
         * @return the options
         */
        public static Options of(int maxLengthCells, int maxWaterCells, double waterCost,
                                 double slopeCost) {
            int expansions = Math.max(MAX_EXPANSIONS, maxLengthCells * 4_000);
            return new Options(maxWaterCells, maxLengthCells, waterCost, slopeCost, expansions);
        }
    }

    /**
     * Finds a rail route between two land cells.
     *
     * @param grid    sampled terrain grid
     * @param start   origin cell (must be land)
     * @param goal    destination cell (must be land)
     * @param options search bounds and costs
     * @return a smoothed cell path from start to goal, or {@code null} when no route exists
     */
    public static @Nullable List<RailCell> find(RailGrid grid, RailCell start, RailCell goal,
                                                Options options) {
        return find(grid, start, goal, options, new Scratch());
    }

    /**
     * Finds a rail route, reusing a caller-owned scratch holder for the board-sized buffers.
     *
     * @param grid    sampled terrain grid
     * @param start   origin cell (must be land)
     * @param goal    destination cell (must be land)
     * @param options search bounds and costs
     * @param scratch working buffers, reused across searches of the same build
     * @return a smoothed cell path from start to goal, or {@code null} when no route exists
     */
    public static @Nullable List<RailCell> find(RailGrid grid, RailCell start, RailCell goal,
                                                Options options, Scratch scratch) {
        int width = grid.width();
        int size = width * grid.height();
        scratch.ensure(size);
        float[] cost = scratch.cost;
        float[] along = scratch.along;
        int[] cameFrom = scratch.cameFrom;
        boolean[] closed = scratch.closed;
        Arrays.fill(cost, Float.POSITIVE_INFINITY);
        Arrays.fill(along, Float.POSITIVE_INFINITY);
        Arrays.fill(cameFrom, -1);
        Arrays.fill(closed, false);

        int startIndex = indexOf(grid, start.x(), start.z(), width);
        int goalIndex = indexOf(grid, goal.x(), goal.z(), width);
        if (startIndex < 0 || goalIndex < 0) {
            return null;
        }
        cost[startIndex] = 0.0F;
        along[startIndex] = 0.0F;
        PriorityQueue<Open> open = new PriorityQueue<>();
        open.add(new Open(heuristic(grid, start.x(), start.z(), goal), startIndex));
        int expanded = 0;
        boolean unlimitedWater = options.maxWaterCells() >= options.maxLengthCells();

        while (!open.isEmpty()) {
            Open current = open.poll();
            int index = current.index();
            if (closed[index]) {
                continue;
            }
            closed[index] = true;
            if (index == goalIndex) {
                return smooth(grid, rebuild(cameFrom, goalIndex, width));
            }
            if (++expanded > options.maxExpansions()) {
                return null;
            }
            int x = index % width;
            int z = index / width;
            for (int dz = -1; dz <= 1; dz++) {
                for (int dx = -1; dx <= 1; dx++) {
                    if (dx == 0 && dz == 0) {
                        continue;
                    }
                    int nx = grid.wrapX(x + dx);
                    int nz = z + dz;
                    if (nz < 0 || nz >= grid.height() || nx < 0 || nx >= width) {
                        continue;
                    }
                    boolean diagonal = dx != 0 && dz != 0;
                    boolean nextLand = grid.land(nx, nz);
                    // Diagonal moves are land-only: a bridge is a straight orthogonal deck.
                    if (diagonal && (!nextLand || !grid.land(x, z))) {
                        continue;
                    }
                    if (!nextLand && !unlimitedWater && !waterRunAllowed(grid, cameFrom, index,
                            options.maxWaterCells())) {
                        continue;
                    }
                    float step = diagonal ? (float) SQRT2 : 1.0F;
                    float nextAlong = along[index] + step;
                    if (nextAlong > options.maxLengthCells() + 0.001F) {
                        continue;
                    }
                    int next = nz * width + nx;
                    if (closed[next]) {
                        continue;
                    }
                    float stepCost;
                    if (nextLand) {
                        int change = Math.abs(grid.surfaceY(nx, nz) - grid.surfaceY(x, z));
                        stepCost = 1.0F + (float) (change * options.slopeCost());
                    } else {
                        stepCost = (float) options.waterCost();
                    }
                    float tentative = cost[index] + step * stepCost;
                    if (tentative < cost[next]) {
                        cost[next] = tentative;
                        along[next] = nextAlong;
                        cameFrom[next] = index;
                        open.add(new Open(tentative + heuristic(grid, nx, nz, goal), next));
                    }
                }
            }
        }
        return null;
    }

    /**
     * @param path      cell path
     * @param gridWidth grid width in cells (the wrapped axis)
     * @return the geometric length of a cell path in cells (orthogonal = 1, diagonal = sqrt 2),
     *         charging the real octile distance between consecutive smoothed waypoints
     */
    public static double pathLength(List<RailCell> path, int gridWidth) {
        double total = 0.0;
        for (int i = 1; i < path.size(); i++) {
            RailCell from = path.get(i - 1);
            RailCell to = path.get(i);
            // Longitude wraps: smoothed waypoints are far apart, so use the actual octile step.
            int dx = Math.abs(wrapDeltaCells(from.x(), to.x(), gridWidth));
            int dz = Math.abs(to.z() - from.z());
            int min = Math.min(dx, dz);
            total += Math.max(dx, dz) + (SQRT2 - 1.0) * min;
        }
        return total;
    }

    /**
     * @param path cell path
     * @param grid the board the path was found on (cylinder or plane)
     * @return the geometric length of a cell path in cells
     */
    public static double pathLength(List<RailCell> path, RailGrid grid) {
        double total = 0.0;
        for (int i = 1; i < path.size(); i++) {
            RailCell from = path.get(i - 1);
            RailCell to = path.get(i);
            int dx = Math.abs(grid.wrapDelta(from.x(), to.x()));
            int dz = Math.abs(to.z() - from.z());
            int min = Math.min(dx, dz);
            total += Math.max(dx, dz) + (SQRT2 - 1.0) * min;
        }
        return total;
    }

    /** @return the shortest signed column delta, in cells, on a cylinder of {@code width} columns. */
    private static int wrapDeltaCells(int fromX, int toX, int width) {
        int delta = Math.floorMod(toX - fromX, width);
        return delta > width / 2 ? delta - width : delta;
    }

    /**
     * Tests whether stepping from {@code fromIndex} into a water cell keeps the consecutive water
     * run within the configured bound. The parent chain of a closed cell is final.
     */
    private static boolean waterRunAllowed(RailGrid grid, int[] cameFrom, int fromIndex,
                                           int maxWaterCells) {
        int run = 1;
        int index = fromIndex;
        int width = grid.width();
        while (index >= 0 && !grid.land(index % width, index / width)) {
            run++;
            if (run > maxWaterCells) {
                return false;
            }
            index = cameFrom[index];
        }
        return true;
    }

    private static int indexOf(RailGrid grid, int x, int z, int width) {
        int column = grid.wrapX(x);
        if (column < 0 || column >= width || z < 0 || z >= grid.height()) {
            return -1;
        }
        return z * width + column;
    }

    private static float heuristic(RailGrid grid, int x, int z, RailCell goal) {
        int dx = Math.abs(grid.wrapDelta(x, goal.x()));
        int dz = Math.abs(z - goal.z());
        int min = Math.min(dx, dz);
        return Math.max(dx, dz) + (float) (SQRT2 - 1.0) * min;
    }

    private static List<RailCell> rebuild(int[] cameFrom, int goalIndex, int width) {
        ArrayList<RailCell> reversed = new ArrayList<>();
        int index = goalIndex;
        while (index >= 0) {
            reversed.add(new RailCell(index % width, index / width));
            index = cameFrom[index];
        }
        ArrayList<RailCell> path = new ArrayList<>(reversed.size());
        for (int i = reversed.size() - 1; i >= 0; i--) {
            path.add(reversed.get(i));
        }
        return path;
    }

    /**
     * Removes collinear and reachable-without-turning cells: from each kept cell, jump to the
     * farthest later cell whose straight cell line stays on land. Water crossings keep the A*
     * shape, because a long land sight-line would otherwise bridge a bay the grid did not intend.
     *
     * @param grid sampled terrain grid
     * @param path a raw A* path
     * @return the simplified path (never empty when {@code path} is non-empty)
     */
    public static List<RailCell> smooth(RailGrid grid, List<RailCell> path) {
        if (path.size() < 3) {
            return List.copyOf(path);
        }
        ArrayList<RailCell> out = new ArrayList<>();
        int at = 0;
        out.add(path.get(at));
        while (at < path.size() - 1) {
            int next = at + 1;
            for (int candidate = path.size() - 1; candidate > at + 1; candidate--) {
                if (clearLandLine(grid, path.get(at), path.get(candidate))) {
                    next = candidate;
                    break;
                }
            }
            out.add(path.get(next));
            at = next;
        }
        return out;
    }

    private static boolean clearLandLine(RailGrid grid, RailCell from, RailCell to) {
        int dx = grid.wrapDelta(from.x(), to.x());
        int dz = to.z() - from.z();
        int steps = Math.max(Math.abs(dx), Math.abs(dz));
        if (steps <= 1) {
            return true;
        }
        for (int step = 1; step < steps; step++) {
            int x = grid.wrapX(from.x() + (int) Math.round((double) dx * step / steps));
            int z = from.z() + (int) Math.round((double) dz * step / steps);
            if (!grid.land(x, z)) {
                return false;
            }
        }
        return true;
    }

    /** One open-set entry; ordered by f-score, then by cell index for determinism. */
    private record Open(float score, int index) implements Comparable<Open> {
        @Override
        public int compareTo(Open other) {
            int byScore = Float.compare(this.score, other.score);
            return byScore != 0 ? byScore : Integer.compare(this.index, other.index);
        }
    }
}
