package com.sanchous98.eldritchhorror.world.town;

import com.sanchous98.eldritchhorror.world.EarthMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A coarse, game-free spatial index over {@link Town}s. Towns are bucketed into square world
 * cells, so a chunk dispatch looks at a handful of buckets instead of scanning all ~3,000
 * settlements (the O(n) cost {@code Locations.place} pays for the 24 cities).
 *
 * <p>Build it during init ({@link #add}), then {@link #seal()} it once and publish the reference.
 * After sealing it is read-only, so worldgen worker threads may query it concurrently.
 *
 * <p>Cell size is in blocks; {@link #DEFAULT_CELL} (512) keeps a town's cull query inside at most
 * 3×3 buckets while spreading the real-world settlements thinly.
 */
public final class TownIndex {

    /** Default bucket side in blocks. */
    public static final int DEFAULT_CELL = 512;

    private final int cell;
    private final Map<Long, List<Town>> buckets = new HashMap<>();
    private final List<Town> towns = new ArrayList<>();
    private boolean sealed;

    /** Creates a mutable index with the default cell size. */
    public TownIndex() {
        this(DEFAULT_CELL);
    }

    /**
     * Creates a mutable index.
     *
     * @param cell bucket side in blocks, must be positive
     */
    public TownIndex(int cell) {
        if (cell <= 0) {
            throw new IllegalArgumentException("cell size must be positive: " + cell);
        }
        this.cell = cell;
    }

    /** Bucket side in blocks. */
    public int cellSize() {
        return this.cell;
    }

    /** Number of towns added so far. */
    public int size() {
        return this.towns.size();
    }

    /**
     * Adds a town to its bucket. Only valid before {@link #seal()}.
     *
     * @throws IllegalStateException if the index has already been sealed
     */
    public void add(Town town) {
        if (this.sealed) {
            throw new IllegalStateException("town index is sealed");
        }
        this.towns.add(town);
        this.buckets.computeIfAbsent(key(cellOf(town.x()), cellOf(town.z())),
                k -> new ArrayList<>()).add(town);
    }

    /**
     * Freezes the index so it can be shared read-only across worldgen threads. Idempotent; after
     * the call every bucket is an immutable list and {@link #add} refuses further writes.
     */
    public void seal() {
        if (this.sealed) {
            return;
        }
        for (Map.Entry<Long, List<Town>> entry : this.buckets.entrySet()) {
            entry.setValue(List.copyOf(entry.getValue()));
        }
        this.sealed = true;
    }

    /**
     * All towns whose centre lies within {@code radius} blocks (Euclidean, inclusive) of
     * {@code (x, z)}. Empty buckets are skipped; the result is a fresh list, empty when nothing is
     * near, so callers may mutate it.
     */
    public List<Town> near(int x, int z, int radius) {
        if (radius < 0) {
            return List.of();
        }
        int minCellX = cellOf(x - radius);
        int maxCellX = cellOf(x + radius);
        int minCellZ = cellOf(z - radius);
        int maxCellZ = cellOf(z + radius);
        return collect(minCellX, maxCellX, minCellZ, maxCellZ, town -> {
            // X is measured across the cylinder seam; Z is ordinary.
            long dx = EarthMap.wrapDelta(x, town.x());
            long dz = (long) town.z() - z;
            return dx * dx + dz * dz <= (long) radius * radius;
        });
    }

    /**
     * All towns whose centre is within {@code reach} blocks of the chunk's 16×16 block square
     * (distance to the nearest block in the chunk, so a town just past an edge still counts).
     * This is the per-chunk dispatch query: {@code reach} is the largest cull radius used.
     *
     * @param chunkX chunk X (block X = {@code chunkX << 4})
     * @param chunkZ chunk Z (block Z = {@code chunkZ << 4})
     * @param reach maximum cull distance in blocks; negative yields an empty list
     */
    public List<Town> forChunk(int chunkX, int chunkZ, int reach) {
        if (reach < 0) {
            return List.of();
        }
        int minX = chunkX << 4;
        int minZ = chunkZ << 4;
        int maxX = minX + 15;
        int maxZ = minZ + 15;
        return collect(cellOf(minX - reach), cellOf(maxX + reach),
                cellOf(minZ - reach), cellOf(maxZ + reach), town -> {
                    long dx = xGap(minX, town.x());
                    long dz = Math.max(0L, Math.max((long) minZ - town.z(), (long) town.z() - maxZ));
                    return dx * dx + dz * dz <= (long) reach * reach;
                });
    }

    /**
     * Distance in blocks along X from {@code townX} to the nearest column of the chunk whose
     * minimum X is {@code minX}, measured across the cylinder seam; 0 when the town lies within
     * the chunk's span.
     */
    private static long xGap(int minX, int townX) {
        int delta = EarthMap.wrapDelta(minX + 8, townX);
        if (delta < -8) {
            return -8L - delta; // town west of the chunk: minX - townX
        }
        return delta > 7 ? delta - 7L : 0L; // town east of the chunk: townX - maxX
    }

    /** Visitor test for one candidate town. */
    @FunctionalInterface
    private interface Filter {
        boolean test(Town town);
    }

    /** Walks the requested bucket rectangle and keeps the towns matching {@code filter}. */
    private List<Town> collect(int minCellX, int maxCellX, int minCellZ, int maxCellZ, Filter filter) {
        List<Town> out = null;
        int cells = cellCount();
        // Wrapping is injective for a range shorter than the cylinder, so only a full-loop query
        // needs the dedupe set; the per-chunk hot path stays allocation-free.
        boolean mayRepeat = cells > 0 && (long) maxCellX - minCellX + 1 > cells;
        Set<Long> visited = mayRepeat ? new HashSet<>() : null;
        for (int cx = minCellX; cx <= maxCellX; cx++) {
            int wrappedX = cells > 0 ? wrapCell(cx, cells) : cx;
            for (int cz = minCellZ; cz <= maxCellZ; cz++) {
                if (visited != null && !visited.add(key(wrappedX, cz))) {
                    continue; // the query wrapped around and revisited a bucket
                }
                List<Town> bucket = this.buckets.get(key(wrappedX, cz));
                if (bucket == null) {
                    continue;
                }
                for (Town town : bucket) {
                    if (!filter.test(town)) {
                        continue;
                    }
                    if (out == null) {
                        out = new ArrayList<>();
                    }
                    out.add(town);
                }
            }
        }
        return out == null ? List.of() : out;
    }

    /** Bucket columns around the cylinder, or 0 when the cell size does not divide it. */
    private int cellCount() {
        int width = 2 * EarthMap.HALF_WIDTH;
        return width % this.cell == 0 ? width / this.cell : 0;
    }

    /** Folds a bucket X onto the cylinder's bucket range. */
    private static int wrapCell(int cellX, int cells) {
        return Math.floorMod(cellX + cells / 2, cells) - cells / 2;
    }

    /** Bucket coordinate containing a world block coordinate (rounds towards -infinity). */
    private int cellOf(int block) {
        return Math.floorDiv(block, this.cell);
    }

    /** Packs a bucket pair into one map key (no allocation beyond the boxed long). */
    private static long key(int cellX, int cellZ) {
        return ((long) cellX << 32) ^ (cellZ & 0xFFFFFFFFL);
    }
}
