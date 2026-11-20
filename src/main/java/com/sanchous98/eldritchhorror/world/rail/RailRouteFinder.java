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
     * Tuning for one search.
     *
     * @param maxWaterCells  longest consecutive run of water cells a bridge may span
     * @param maxLengthCells maximum geometric path length in cells
     * @param waterCost      cost of one water cell (land costs one)
     * @param slopeCost      cost per block of surface-height change between adjacent land cells
     */
    public record Options(int maxWaterCells, int maxLengthCells, double waterCost,
                          double slopeCost) {
        /** Validates the ranges. */
        public Options {
            if (maxWaterCells < 0 || maxLengthCells < 1) {
                throw new IllegalArgumentException("invalid rail search bounds");
            }
            if (waterCost < 1.0 || slopeCost < 0.0) {
                throw new IllegalArgumentException("invalid rail search costs");
            }
        }

        /**
         * The production tuning for a line at most {@code maxBlocks} long.
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
            return new Options(6, cells, 18.0, 0.05);
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
        if (!grid.land(start.x(), start.z()) || !grid.land(goal.x(), goal.z())) {
            return null;
        }
        int width = grid.width();
        int size = width * grid.height();
        float[] cost = new float[size];
        float[] along = new float[size];
        Arrays.fill(cost, Float.POSITIVE_INFINITY);
        Arrays.fill(along, Float.POSITIVE_INFINITY);
        int[] cameFrom = new int[size];
        Arrays.fill(cameFrom, -1);
        boolean[] closed = new boolean[size];

        int startIndex = indexOf(grid, start.x(), start.z(), width);
        int goalIndex = indexOf(grid, goal.x(), goal.z(), width);
        cost[startIndex] = 0.0F;
        along[startIndex] = 0.0F;
        PriorityQueue<Open> open = new PriorityQueue<>();
        open.add(new Open(heuristic(grid, start.x(), start.z(), goal), startIndex));
        int expanded = 0;

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
            if (++expanded > MAX_EXPANSIONS) {
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
                    if (nz < 0 || nz >= grid.height()) {
                        continue;
                    }
                    boolean diagonal = dx != 0 && dz != 0;
                    boolean nextLand = grid.land(nx, nz);
                    // Diagonal moves are land-only: a bridge is a straight orthogonal deck.
                    if (diagonal && (!nextLand || !grid.land(x, z))) {
                        continue;
                    }
                    if (!nextLand && !waterRunAllowed(grid, cameFrom, index,
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
     * @return the geometric length of a cell path in cells (orthogonal = 1, diagonal = sqrt 2)
     */
    public static double pathLength(List<RailCell> path) {
        double total = 0.0;
        for (int i = 1; i < path.size(); i++) {
            RailCell from = path.get(i - 1);
            RailCell to = path.get(i);
            int dx = Math.abs(to.x() - from.x());
            int dz = Math.abs(to.z() - from.z());
            total += dx != 0 && dz != 0 ? SQRT2 : 1.0;
        }
        return total;
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
        return z * width + grid.wrapX(x);
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
