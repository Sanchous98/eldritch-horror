package com.sanchous98.eldritchhorror.world.transport;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.PriorityQueue;
import org.jspecify.annotations.Nullable;

/**
 * A* over a {@link SeaGrid}, used once per port pair and cached afterwards.
 *
 * <p>Ordinary moves stay on <em>safe</em> cells (water with a full {@code margin}-cell buffer from
 * land). Near a port the lane may use plain water within {@code escapeRadius} cells of the start or
 * goal, because a harbour always touches the coast; the resulting polyline is validated against the
 * landmask and rejected if it still clips land.
 *
 * <p>Pure: no Minecraft type appears here. The heuristic is octile distance on the cylinder (the X
 * delta uses {@link SeaGrid#wrapDelta}), and the returned path is simplified by greedy line-of-sight
 * over safe cells so the ship turns rarely instead of following every grid step.
 */
public final class SeaRouteFinder {
    private static final double SQRT2 = Math.sqrt(2.0);

    private SeaRouteFinder() {
    }

    /**
     * Finds a sea lane between two cells.
     *
     * @param grid          sampled water grid
     * @param start         origin cell (must be water; need not be safe)
     * @param goal          destination cell (must be water; need not be safe)
     * @param escapeRadius  cells around either endpoint where unsafe water is allowed
     * @return a smoothed cell path from start to goal, or {@code null} when no lane exists
     */
    public static @Nullable List<SeaCell> find(SeaGrid grid, SeaCell start, SeaCell goal,
                                               int escapeRadius) {
        List<SeaCell> raw = findRaw(grid, start, goal, escapeRadius);
        return raw == null ? null : smooth(grid, raw);
    }

    /**
     * The unsmoothed A* path. Callers near a coast validate the block polyline and may prefer this
     * over a smoothed shortcut, which can clip a headland the coarse cells do not resolve.
     */
    public static @Nullable List<SeaCell> findRaw(SeaGrid grid, SeaCell start, SeaCell goal,
                                                  int escapeRadius) {
        return findRaw(grid, start, goal, escapeRadius, false);
    }

    /**
     * As {@link #findRaw(SeaGrid, SeaCell, SeaCell, int)}, but optionally allowing a diagonal step
     * whose two orthogonal neighbours are not both navigable. A corridor search over a coarse grid
     * needs this to follow a channel that crosses a cell corner; a real lane must never do it, so
     * the flag stays off for the lane itself.
     *
     * @param cornerCutting allow diagonal moves that cut a cell corner
     * @return the raw cell path, or {@code null}
     */
    public static @Nullable List<SeaCell> findRaw(SeaGrid grid, SeaCell start, SeaCell goal,
                                                  int escapeRadius, boolean cornerCutting) {
        if (!grid.water(start.x(), start.z()) || !grid.water(goal.x(), goal.z())) {
            return null;
        }
        int width = grid.width();
        int size = width * grid.height();
        return search(grid, start, goal, escapeRadius, width, size, cornerCutting);
    }

    private static @Nullable List<SeaCell> search(SeaGrid grid, SeaCell start, SeaCell goal,
                                                  int escapeRadius, int width, int size,
                                                  boolean cornerCutting) {
        float[] best = new float[size];
        Arrays.fill(best, Float.POSITIVE_INFINITY);
        int[] cameFrom = new int[size];
        Arrays.fill(cameFrom, -1);
        boolean[] closed = new boolean[size];
        byte[] allowedCache = new byte[size];

        int startIndex = indexOf(start.x(), start.z(), grid, width);
        int goalIndex = indexOf(goal.x(), goal.z(), grid, width);
        best[startIndex] = 0.0F;
        PriorityQueue<Open> open = new PriorityQueue<>();
        open.add(new Open(heuristic(grid, start.x(), start.z(), goal), startIndex));

        while (!open.isEmpty()) {
            Open current = open.poll();
            int index = current.index();
            if (closed[index]) {
                continue;
            }
            closed[index] = true;
            if (index == goalIndex) {
                return rebuild(cameFrom, goalIndex, width);
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
                    if (!allowed(grid, nx, nz, start, goal, escapeRadius, allowedCache, width)) {
                        continue;
                    }
                    if (dx != 0 && dz != 0 && !cornerCutting
                            && (!allowed(grid, nx, z, start, goal, escapeRadius, allowedCache, width)
                            || !allowed(grid, x, nz, start, goal, escapeRadius, allowedCache,
                            width))) {
                        continue;
                    }
                    int next = nz * width + nx;
                    if (closed[next]) {
                        continue;
                    }
                    float step = dx != 0 && dz != 0 ? (float) SQRT2 : 1.0F;
                    float tentative = best[index] + step;
                    if (tentative < best[next]) {
                        best[next] = tentative;
                        cameFrom[next] = index;
                        open.add(new Open(tentative + heuristic(grid, nx, nz, goal), next));
                    }
                }
            }
        }
        return null;
    }

    private static boolean allowed(SeaGrid grid, int x, int z, SeaCell start, SeaCell goal,
                                   int escapeRadius, byte[] cache, int width) {
        int index = z * width + x;
        byte cached = cache[index];
        if (cached != 0) {
            return cached == 2;
        }
        boolean ok = grid.safe(x, z);
        if (!ok && grid.water(x, z)) {
            ok = within(grid, x, z, start, escapeRadius) || within(grid, x, z, goal, escapeRadius);
        }
        cache[index] = (byte) (ok ? 2 : 1);
        return ok;
    }

    private static boolean within(SeaGrid grid, int x, int z, SeaCell centre, int radius) {
        int dx = Math.abs(grid.wrapDelta(centre.x(), x));
        int dz = Math.abs(z - centre.z());
        return Math.max(dx, dz) <= radius;
    }

    private static int indexOf(int x, int z, SeaGrid grid, int width) {
        return z * width + grid.wrapX(x);
    }

    private static float heuristic(SeaGrid grid, int x, int z, SeaCell goal) {
        int dx = Math.abs(grid.wrapDelta(x, goal.x()));
        int dz = Math.abs(z - goal.z());
        int min = Math.min(dx, dz);
        return Math.max(dx, dz) + (float) (SQRT2 - 1.0) * min;
    }

    private static List<SeaCell> rebuild(int[] cameFrom, int goalIndex, int width) {
        ArrayList<SeaCell> reversed = new ArrayList<>();
        int index = goalIndex;
        while (index >= 0) {
            reversed.add(new SeaCell(index % width, index / width));
            index = cameFrom[index];
        }
        ArrayList<SeaCell> path = new ArrayList<>(reversed.size());
        for (int i = reversed.size() - 1; i >= 0; i--) {
            path.add(reversed.get(i));
        }
        return path;
    }

    /**
     * Removes collinear and reachable-without-turning cells: from each kept cell, jump to the
     * farthest later cell whose straight cell line never leaves safe water.
     *
     * @param grid sampled water grid
     * @param path a raw A* path
     * @return the simplified path (never empty when {@code path} is non-empty)
     */
    public static List<SeaCell> smooth(SeaGrid grid, List<SeaCell> path) {
        if (path.size() < 3) {
            return List.copyOf(path);
        }
        ArrayList<SeaCell> out = new ArrayList<>();
        int at = 0;
        out.add(path.get(at));
        while (at < path.size() - 1) {
            int next = at + 1;
            for (int candidate = path.size() - 1; candidate > at + 1; candidate--) {
                if (clearLine(grid, path.get(at), path.get(candidate))) {
                    next = candidate;
                    break;
                }
            }
            out.add(path.get(next));
            at = next;
        }
        return out;
    }

    private static boolean clearLine(SeaGrid grid, SeaCell from, SeaCell to) {
        int dx = grid.wrapDelta(from.x(), to.x());
        int dz = to.z() - from.z();
        int steps = Math.max(Math.abs(dx), Math.abs(dz));
        if (steps <= 1) {
            return true;
        }
        for (int step = 1; step < steps; step++) {
            int x = from.x() + (int) Math.round((double) dx * step / steps);
            int z = from.z() + (int) Math.round((double) dz * step / steps);
            if (!grid.safe(x, z)) {
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
