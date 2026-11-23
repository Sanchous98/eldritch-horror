package com.sanchous98.eldritchhorror.world.loc.city.plan;

import com.sanchous98.eldritchhorror.world.loc.Palette;
import com.sanchous98.eldritchhorror.world.loc.StructureBuilder;
import com.sanchous98.eldritchhorror.world.loc.city.CitySurface;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jspecify.annotations.Nullable;

/**
 * Runs an authored {@link CityPlan} into an in-memory block map instead of the world.
 *
 * <p>The transport network needs to see what a plan actually authored — where its rails and its
 * water are — but it runs before the city's chunks generate, and {@code CityLocation}'s replay
 * cache is only populated per generating chunk. This probe builds the same layout once, headless
 * and deterministically, so {@code RailStations} and {@code Ports} can treat the authored station
 * and harbour anchors as authoritative without touching {@code CityLocation}.
 *
 * <p>The shaped decorative ops (roofs, spires, windows, scatter) are approximated by their base
 * course or skipped: only the block primitives matter for transport geometry. A bounds box keeps
 * the map small in production; a test may record the whole plan.
 */
public final class CityPlanProbe implements StructureBuilder {

    /** Terrain reads a plan may make: the baked surface and the landmask. */
    public interface Terrain {
        /** @return the baked surface Y at a column. */
        int surfaceY(int x, int z);

        /** @return true when the column is real land. */
        boolean isLand(int x, int z);
    }

    /** The rectangle a headless probe records; {@code null} means the whole plan. */
    public record Bounds(int minX, int minZ, int maxX, int maxZ) {
        /** @return a box of {@code radius} blocks around a column. */
        public static Bounds around(int x, int z, int radius) {
            return new Bounds(x - radius, z - radius, x + radius, z + radius);
        }

        private boolean contains(int x, int z) {
            return x >= this.minX && x <= this.maxX && z >= this.minZ && z <= this.maxZ;
        }
    }

    /** One authored rail block. */
    public record Rail(int x, int y, int z, BlockState state) {
    }

    /**
     * A water body near a harbour anchor.
     *
     * @param x      a point in open water, clear of the quay
     * @param y      the water surface Y (the air block above the top water course)
     * @param z      the point's Z
     * @param width  the body's X extent in blocks
     * @param length the body's Z extent in blocks
     * @param depth  the deepest vertical water course found in the body
     * @param topY   the top water block's Y
     */
    public record Berth(int x, int y, int z, int width, int length, int depth, int topY) {
    }

    private static final int EDGE_RING = 32;
    private static final int MAX_COMPONENT = 400_000;

    private final Palette palette;
    private final Map<Long, BlockState> blocks = new HashMap<>();
    private final int ground;
    private final @Nullable Terrain terrain;
    private final @Nullable Bounds bounds;
    private final RandomSource rng = RandomSource.create(0L);

    private CityPlanProbe(Palette palette, int ground, @Nullable Terrain terrain,
                          @Nullable Bounds bounds) {
        this.palette = palette;
        this.ground = ground;
        this.terrain = terrain;
        this.bounds = bounds;
    }

    /**
     * @param plan    the authored plan
     * @param palette the palette the plan expects (any palette yields the same geometry)
     * @param cx      authored centre X
     * @param cz      authored centre Z
     * @param ground  flat-heart ground Y
     * @param bounds  region to record, or {@code null} for all of it
     * @return the recorded plan
     */
    public static CityPlanProbe record(CityPlan plan, Palette palette, int cx, int cz, int ground,
                                       @Nullable Terrain terrain, @Nullable Bounds bounds) {
        CityPlanProbe probe = new CityPlanProbe(palette, ground, terrain, bounds);
        int district = Math.clamp(plan.halfExtent(), 300, 400);
        CitySurface surface = CitySurface.of(probe, cx, cz, district, ground, EDGE_RING);
        CityPlanContext ctx = new CityPlanContext(cx, cz, ground, district, surface);
        plan.build(probe, ctx, palette);
        return probe;
    }

    /** @return true when the plan wrote a non-air block at this position. */
    public boolean solid(int x, int y, int z) {
        BlockState state = this.blocks.get(BlockPos.asLong(x, y, z));
        return state != null && !state.isAir();
    }

    /** @return the final state at a position, or {@code null} when the plan wrote nothing. */
    public @Nullable BlockState at(int x, int y, int z) {
        return this.blocks.get(BlockPos.asLong(x, y, z));
    }

    /** @return true when the final state at a position is a rail. */
    public boolean railAt(int x, int y, int z) {
        BlockState state = at(x, y, z);
        return state != null && state.is(Blocks.RAIL);
    }

    /** @return true when the final state at a position is still water. */
    public boolean waterAt(int x, int y, int z) {
        BlockState state = at(x, y, z);
        return state != null && state.is(Blocks.WATER);
    }

    /** @return the highest rail within {@code radius} blocks horizontally, or {@code null}. */
    public @Nullable Rail nearestRail(int x, int z, int radius) {
        Rail best = null;
        long bestDistance = Long.MAX_VALUE;
        int radius2 = radius * radius;
        for (Map.Entry<Long, BlockState> entry : this.blocks.entrySet()) {
            if (!entry.getValue().is(Blocks.RAIL)) {
                continue;
            }
            long packed = entry.getKey();
            int bx = BlockPos.getX(packed);
            int bz = BlockPos.getZ(packed);
            int dx = bx - x;
            int dz = bz - z;
            int distance = dx * dx + dz * dz;
            if (distance > radius2) {
                continue;
            }
            if (distance < bestDistance || (distance == bestDistance && best != null
                    && packed < BlockPos.asLong(best.x(), best.y(), best.z()))) {
                bestDistance = distance;
                best = new Rail(bx, BlockPos.getY(packed), bz, entry.getValue());
            }
        }
        return best;
    }

    /**
     * Finds the water body nearest a quay anchor and describes the berth inside it.
     *
     * @param x      anchor X (normally the authored PORT point)
     * @param z      anchor Z
     * @param radius search radius around the anchor
     * @return the berth, or {@code null} when the plan authored no water there
     */
    public @Nullable Berth waterBerth(int x, int z, int radius) {
        Map<Long, Integer> top = waterTops();
        if (top.isEmpty()) {
            return null;
        }
        int radius2 = radius * radius;
        Map<Integer, Integer> levelCounts = new HashMap<>();
        for (Map.Entry<Long, Integer> entry : top.entrySet()) {
            int wx = BlockPos.getX(entry.getKey());
            int wz = BlockPos.getZ(entry.getKey());
            int dx = wx - x;
            int dz = wz - z;
            if (dx * dx + dz * dz <= radius2) {
                levelCounts.merge(entry.getValue(), 1, Integer::sum);
            }
        }
        if (levelCounts.isEmpty()) {
            return null;
        }
        int seedTop = Integer.MIN_VALUE;
        int bestCount = -1;
        for (Map.Entry<Integer, Integer> level : levelCounts.entrySet()) {
            int count = level.getValue();
            if (count > bestCount || (count == bestCount && level.getKey() > seedTop)) {
                bestCount = count;
                seedTop = level.getKey();
            }
        }
        long seed = Long.MIN_VALUE;
        long bestDistance = Long.MAX_VALUE;
        for (Map.Entry<Long, Integer> entry : top.entrySet()) {
            if (entry.getValue() != seedTop) {
                continue;
            }
            int wx = BlockPos.getX(entry.getKey());
            int wz = BlockPos.getZ(entry.getKey());
            int dx = wx - x;
            int dz = wz - z;
            int distance = dx * dx + dz * dz;
            if (distance <= radius2 && distance < bestDistance) {
                bestDistance = distance;
                seed = entry.getKey();
            }
        }
        if (seed == Long.MIN_VALUE) {
            return null;
        }
        List<Long> body = component(top, seed, seedTop);
        int minX = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int minZ = Integer.MAX_VALUE;
        int maxZ = Integer.MIN_VALUE;
        int deepest = 0;
        for (long column : body) {
            int wx = BlockPos.getX(column);
            int wz = BlockPos.getZ(column);
            minX = Math.min(minX, wx);
            maxX = Math.max(maxX, wx);
            minZ = Math.min(minZ, wz);
            maxZ = Math.max(maxZ, wz);
            deepest = Math.max(deepest, depth(wx, wz, top.get(column)));
        }
        int width = maxX - minX + 1;
        int length = maxZ - minZ + 1;
        int surfaceTop = seedTop;
        for (long column : body) {
            surfaceTop = Math.max(surfaceTop, top.get(column));
        }
        // Spawn clear of the quay at a point with open water on every side and at the body's real
        // surface: a hull is six wide and eighteen long, and a column under a pier or a barge has
        // a lower top (Rome) and would put the deck at the wrong depth.
        long point = berthColumn(top, body, seed, 32, surfaceTop);
        if (point == Long.MIN_VALUE) {
            point = seed;
        }
        int px = BlockPos.getX(point);
        int pz = BlockPos.getZ(point);
        int py = top.get(point) + 1;
        return new Berth(px, py, pz, width, length, depth(px, pz, py - 1), py - 1);
    }

    // ------------------------------------------------------------------ StructureBuilder

    @Override
    public void put(int x, int y, int z, BlockState state) {
        if (this.bounds != null && !this.bounds.contains(x, z)) {
            return;
        }
        this.blocks.put(BlockPos.asLong(x, y, z), state);
    }

    @Override
    public void chest(int x, int y, int z, ResourceKey<LootTable> lootTable) {
        put(x, y, z, Blocks.CHEST.defaultBlockState());
    }

    @Override
    public void fill(int x0, int y0, int z0, int x1, int y1, int z1, BlockState state) {
        int ax = Math.min(x0, x1);
        int bx = Math.max(x0, x1);
        int ay = Math.min(y0, y1);
        int by = Math.max(y0, y1);
        int az = Math.min(z0, z1);
        int bz = Math.max(z0, z1);
        for (int x = ax; x <= bx; x++) {
            for (int z = az; z <= bz; z++) {
                for (int y = ay; y <= by; y++) {
                    put(x, y, z, state);
                }
            }
        }
    }

    @Override
    public void walls(int x0, int y0, int z0, int x1, int y1, int z1, BlockState state) {
        int ax = Math.min(x0, x1);
        int bx = Math.max(x0, x1);
        int ay = Math.min(y0, y1);
        int by = Math.max(y0, y1);
        int az = Math.min(z0, z1);
        int bz = Math.max(z0, z1);
        for (int y = ay; y <= by; y++) {
            for (int x = ax; x <= bx; x++) {
                put(x, y, az, state);
                put(x, y, bz, state);
            }
            for (int z = az; z <= bz; z++) {
                put(ax, y, z, state);
                put(bx, y, z, state);
            }
        }
    }

    @Override
    public void room(int x0, int y0, int z0, int x1, int y1, int z1, Doorway... doorways) {
        int ax = Math.min(x0, x1);
        int bx = Math.max(x0, x1);
        int ay = Math.min(y0, y1);
        int by = Math.max(y0, y1);
        int az = Math.min(z0, z1);
        int bz = Math.max(z0, z1);
        BlockState air = Blocks.AIR.defaultBlockState();
        fill(ax, ay, az, bx, ay, bz, this.palette.ground());
        fill(ax, by, az, bx, by, bz, this.palette.roof());
        if (by - 1 >= ay + 1) {
            walls(ax, ay + 1, az, bx, by - 1, bz, this.palette.wall());
            if (bx - 1 >= ax + 1 && bz - 1 >= az + 1) {
                fill(ax + 1, ay + 1, az + 1, bx - 1, by - 1, bz - 1, air);
            }
        }
        for (Doorway doorway : doorways) {
            if (doorway == null) {
                continue;
            }
            int y = ay + 1;
            switch (doorway.side()) {
                case N -> doorHole(ax, y, az, bx, doorway.offset(), true);
                case S -> doorHole(ax, y, bz, bx, doorway.offset(), true);
                case W -> doorHole(az, y, ax, bz, doorway.offset(), false);
                case E -> doorHole(az, y, bx, bz, doorway.offset(), false);
                default -> {
                }
            }
        }
    }

    @Override
    public void ground(int x0, int z0, int x1, int z1, int y0, int y1, BlockState state) {
        fill(x0, y0, z0, x1, y1, z1, state);
    }

    @Override
    public void pitchedRoof(int x0, int z0, int x1, int z1, int y, int height, int ridgeAxis) {
        if (height >= 1) {
            fill(x0, y, z0, x1, y, z1, this.palette.roof());
        }
    }

    @Override
    public void spire(int cx, int cz, int baseY, int height) {
        // Decorative silhouette: not transport geometry.
    }

    @Override
    public void buttress(int x, int z, int baseY, int height, Side outward) {
        // Decorative silhouette: not transport geometry.
    }

    @Override
    public void window(int x, int y, int z, int height, int width, boolean vertical) {
        // Glazing does not change a platform's clearance in the probe.
    }

    @Override
    public void crenellations(int x0, int z0, int x1, int z1, int y) {
        // Decorative silhouette: not transport geometry.
    }

    @Override
    public void monument(int cx, int cz, int baseY) {
        put(cx, baseY, cz, this.palette.foundation());
    }

    @Override
    public void scatter(int x0, int z0, int x1, int z1, int y0, int y1, BlockState state,
                        float chance) {
        // Deterministic dressing only; it never forms a platform or a water body.
    }

    @Override
    public void ruins(int x0, int y0, int z0, int x1, int y1, int z1, float holeChance) {
        // Decorative decay only.
    }

    @Override
    public RandomSource rng() {
        return this.rng;
    }

    @Override
    public int groundY(int x, int z) {
        return this.terrain == null ? this.ground : this.terrain.surfaceY(x, z);
    }

    @Override
    public boolean isLand(int x, int z) {
        return this.terrain == null || this.terrain.isLand(x, z);
    }

    @Override
    public boolean isReplaceable(int x, int y, int z) {
        return false;
    }

    @Override
    public int chunkMinX() {
        return this.bounds == null ? 0 : this.bounds.minX();
    }

    @Override
    public int chunkMinZ() {
        return this.bounds == null ? 0 : this.bounds.minZ();
    }

    @Override
    public Palette palette() {
        return this.palette;
    }

    @Override
    public void marker(String name, int x, int y, int z) {
        // Jigsaw markers carry no blocks.
    }

    @Override
    public void sign(int x, int y, int z, Direction facing, String... lines) {
        put(x, y, z, Blocks.OAK_SIGN.defaultBlockState());
    }

    // ------------------------------------------------------------------ internals

    private void doorHole(int from, int y, int fixed, int to, int offset, boolean alongX) {
        int span = to - from;
        int along = from + Math.clamp(offset, 1, Math.max(1, span - 1));
        BlockState air = Blocks.AIR.defaultBlockState();
        for (int dy = 0; dy <= 1; dy++) {
            if (alongX) {
                put(along, y + dy, fixed, air);
            } else {
                put(fixed, y + dy, along, air);
            }
        }
    }

    /** @return column key → top water block Y, for every final water column in the probe. */
    private Map<Long, Integer> waterTops() {
        Map<Long, Integer> top = new HashMap<>();
        for (Map.Entry<Long, BlockState> entry : this.blocks.entrySet()) {
            if (!entry.getValue().is(Blocks.WATER)) {
                continue;
            }
            long packed = entry.getKey();
            long column = BlockPos.asLong(BlockPos.getX(packed), 0, BlockPos.getZ(packed));
            int y = BlockPos.getY(packed);
            top.merge(column, y, Math::max);
        }
        return top;
    }

    private static List<Long> component(Map<Long, Integer> top, long seed, int seedTop) {
        List<Long> body = new ArrayList<>();
        Deque<Long> queue = new ArrayDeque<>();
        Set<Long> seen = new HashSet<>();
        queue.add(seed);
        seen.add(seed);
        while (!queue.isEmpty() && body.size() < MAX_COMPONENT) {
            long column = queue.removeFirst();
            body.add(column);
            int x = BlockPos.getX(column);
            int z = BlockPos.getZ(column);
            for (int[] step : new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}}) {
                long next = BlockPos.asLong(x + step[0], 0, z + step[1]);
                Integer nextTop = top.get(next);
                if (nextTop == null || seen.contains(next)
                        || Math.abs(nextTop - seedTop) > 2) {
                    continue;
                }
                seen.add(next);
                queue.addLast(next);
            }
        }
        return body;
    }

    /**
     * Picks the roomiest water point within reach of the anchor: the column with the largest
     * smallest clearance to the nearest edge on all four axes, tie-broken by distance to the
     * quay-side seed.
     */
    private static long berthColumn(Map<Long, Integer> top, List<Long> body, long seed,
                                    int maxDistance, int surfaceTop) {
        long best = Long.MIN_VALUE;
        int bestClear = -1;
        long bestDistance = Long.MAX_VALUE;
        int seedX = BlockPos.getX(seed);
        int seedZ = BlockPos.getZ(seed);
        for (long column : body) {
            if (top.get(column) != surfaceTop) {
                continue;
            }
            int x = BlockPos.getX(column);
            int z = BlockPos.getZ(column);
            int dx = x - seedX;
            int dz = z - seedZ;
            long distance = (long) dx * dx + (long) dz * dz;
            if (distance > (long) maxDistance * maxDistance) {
                continue;
            }
            int clear = Math.min(Math.min(axisClear(top, x, z, 1, 0),
                            axisClear(top, x, z, -1, 0)),
                    Math.min(axisClear(top, x, z, 0, 1), axisClear(top, x, z, 0, -1)));
            if (clear > bestClear || (clear == bestClear && distance < bestDistance)) {
                bestClear = clear;
                bestDistance = distance;
                best = column;
            }
        }
        return best;
    }

    /** @return consecutive water columns from (x,z) in a direction, capped at twelve. */
    private static int axisClear(Map<Long, Integer> top, int x, int z, int dx, int dz) {
        int clear = 0;
        while (clear < 12 && top.containsKey(BlockPos.asLong(x + dx * (clear + 1), 0,
                z + dz * (clear + 1)))) {
            clear++;
        }
        return clear;
    }

    private int depth(int x, int z, int topY) {
        int course = 0;
        while (waterAt(x, topY - course, z)) {
            course++;
        }
        return course;
    }
}
