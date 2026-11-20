package com.sanchous98.eldritchhorror.world.town;

import com.sanchous98.eldritchhorror.core.ModConfig;
import com.sanchous98.eldritchhorror.world.loc.Location;
import com.sanchous98.eldritchhorror.world.loc.Palette;
import com.sanchous98.eldritchhorror.world.loc.StructureBuilder;
import com.sanchous98.eldritchhorror.world.loc.StructureBuilder.Doorway;
import com.sanchous98.eldritchhorror.world.loc.StructureBuilder.Side;
import com.sanchous98.eldritchhorror.world.loc.Tier;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

/**
 * A generic settlement: a small lived-in town on the real coordinates of one non-curated entry
 * from {@code settlements.json}. Unlike the curated {@link com.sanchous98.eldritchhorror.world.loc.city.CityLocation}
 * it is deliberately cheap and compact — a few chunks at most — but still reads as a place: a
 * paved plaza with a well and a stone marker, four or more small gothic houses with pitched roofs,
 * doors and windows, lamp posts, a fenced farm plot and a cross of paths.
 *
 * <p>Deterministic: every feature's size/position comes from a position hash seeded by
 * {@link #id()}, never from the shared chunk RNG, so a building that straddles a chunk border is
 * drawn identically (and clipped) by both chunks. All blocks come from {@link Palette}; all
 * terrain work uses {@code groundY}/{@code isLand} and {@link StructureBuilder}'s clipping.
 */
public final class TownLocation implements Location {

    /** Highest {@code townRadiusScale} the config allows. */
    private static final double MAX_RADIUS_SCALE = 1.5;
    /** Largest cull radius any (scaled) town can have. */
    public static final int MAX_CULL_RADIUS = (int) Math.ceil(TownSiting.MAX_RADIUS * MAX_RADIUS_SCALE);

    /** Plaza half-extent in blocks. */
    private static final int PLAZA = 8;
    /** Half-width of the path arms (1 → a 3-wide path). */
    private static final int PATH_HALF = 1;
    /** Shared air state for leveling and door carving. */
    private static final BlockState AIR = Blocks.AIR.defaultBlockState();

    private final Town town;

    /**
     * @param town the baked settlement this location represents
     */
    public TownLocation(Town town) {
        this.town = town;
    }

    /** The settlement behind this location (name and real coordinates). */
    public Town town() {
        return this.town;
    }

    @Override
    public String id() {
        return "eldritch_horror:town/" + this.town.id();
    }

    @Override
    public Tier tier() {
        return Tier.TOWN;
    }

    /**
     * Footprint half-extent: {@link TownSiting#radiusFor} scaled by the config, clamped to
     * [{@link TownSiting#MIN_RADIUS}, {@link #MAX_CULL_RADIUS}] so a town never sprawls.
     */
    @Override
    public int radius() {
        double scale = ModConfig.TOWN_RADIUS_SCALE.get();
        int scaled = (int) Math.round(TownSiting.radiusFor(this.town.population()) * scale);
        return Math.clamp(scaled, TownSiting.MIN_RADIUS, MAX_CULL_RADIUS);
    }

    @Override
    public void build(StructureBuilder b) {
        int cx = this.town.x();
        int cz = this.town.z();
        int radius = radius();
        Palette p = b.palette();
        int ground = plazaLevel(b, cx, cz);

        pavePlaza(b, cx, cz, ground, p);
        pavePaths(b, cx, cz, radius, ground, p);
        well(b, cx, cz, ground, p);
        b.monument(cx + 4, cz + 4, ground + 1);
        lamps(b, cx, cz, p);

        int farmX = farmX(cx, radius);
        int farmZ = farmZ(cz, radius);
        farm(b, farmX, farmZ, cx, cz, ground, p);
        houses(b, cx, cz, radius, farmX, farmZ, ground, p);

        b.marker("town_center", cx, ground + 1, cz);
    }

    // ------------------------------------------------------------------ ground works

    /**
     * The town's reference level: the mean terrain surface over the plaza, sampled on a coarse
     * grid. Pure ({@code groundY}/{@code isLand} only), so every chunk computes the same level.
     */
    private static int plazaLevel(StructureBuilder b, int cx, int cz) {
        long sum = 0;
        int n = 0;
        for (int dx = -PLAZA; dx <= PLAZA; dx += 4) {
            for (int dz = -PLAZA; dz <= PLAZA; dz += 4) {
                int x = cx + dx;
                int z = cz + dz;
                if (b.isLand(x, z)) {
                    sum += b.groundY(x, z);
                    n++;
                }
            }
        }
        return n == 0 ? b.groundY(cx, cz) : (int) Math.round(sum / (double) n);
    }

    /** Paves and levels the plaza square; only this chunk's columns are touched. */
    private static void pavePlaza(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int x0 = Math.max(cx - PLAZA, b.chunkMinX());
        int x1 = Math.min(cx + PLAZA, b.chunkMinX() + 15);
        int z0 = Math.max(cz - PLAZA, b.chunkMinZ());
        int z1 = Math.min(cz + PLAZA, b.chunkMinZ() + 15);
        for (int x = x0; x <= x1; x++) {
            for (int z = z0; z <= z1; z++) {
                levelColumn(b, x, z, ground, p);
            }
        }
    }

    /**
     * A cross of 3-wide paths from the plaza out to the rim. The path follows the natural surface
     * (clamped near the plaza level), so it steps with a slope instead of cutting a trench.
     */
    private static void pavePaths(StructureBuilder b, int cx, int cz, int radius, int ground, Palette p) {
        int reach = radius - 4;
        for (int x = b.chunkMinX(); x <= b.chunkMinX() + 15; x++) {
            int dx = x - cx;
            if (Math.abs(dx) > reach) {
                continue;
            }
            for (int z = b.chunkMinZ(); z <= b.chunkMinZ() + 15; z++) {
                int dz = z - cz;
                boolean arm = (Math.abs(dx) <= PATH_HALF && Math.abs(dz) <= reach)
                        || (Math.abs(dz) <= PATH_HALF && Math.abs(dx) <= reach);
                if (!arm || (Math.abs(dx) <= PLAZA && Math.abs(dz) <= PLAZA)) {
                    continue;
                }
                int y = Math.clamp(b.groundY(x, z), ground - 6, ground + 6);
                levelColumn(b, x, z, y, p);
            }
        }
    }

    /** Levels one column to {@code y} (cut above, fill below) and caps it with paving. */
    private static void levelColumn(StructureBuilder b, int x, int z, int y, Palette p) {
        if (!b.isLand(x, z)) {
            return;
        }
        int surface = b.groundY(x, z);
        if (surface > y) {
            b.fill(x, y + 1, z, x, surface, z, AIR);
        } else if (surface < y) {
            b.fill(x, surface + 1, z, x, y, z, p.foundation());
        }
        b.put(x, y, z, p.ground());
        for (int cy = y + 1; cy <= Math.max(surface, y) + 3; cy++) {
            b.put(x, cy, z, AIR);
        }
    }

    // ------------------------------------------------------------------ plaza furniture

    /** The town well: a foundation curb with a dark mouth and a lantern post. */
    private static void well(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        if (!touches(b, cx - 1, cz - 1, cx + 1, cz + 1)) {
            return;
        }
        b.fill(cx - 1, ground + 1, cz - 1, cx + 1, ground + 1, cz + 1, p.foundation());
        b.put(cx, ground + 1, cz, p.ground());
        b.put(cx, ground + 2, cz - 1, p.rail());
        b.put(cx, ground + 3, cz - 1, p.light());
    }

    /** A lamp post at each plaza corner, standing on the local surface (never floating). */
    private static void lamps(StructureBuilder b, int cx, int cz, Palette p) {
        int offset = PLAZA + 2;
        for (int sx = -1; sx <= 1; sx += 2) {
            for (int sz = -1; sz <= 1; sz += 2) {
                int x = cx + sx * offset;
                int z = cz + sz * offset;
                if (!touches(b, x, z, x, z) || !b.isLand(x, z)) {
                    continue;
                }
                int y = b.groundY(x, z);
                b.put(x, y, z, p.foundation());
                b.put(x, y + 1, z, p.accent());
                b.put(x, y + 2, z, p.accent());
                b.put(x, y + 3, z, p.light());
            }
        }
    }

    // ------------------------------------------------------------------ farm

    /** X of the fenced farm plot: one quadrant chosen deterministically from the town id. */
    private int farmX(int cx, int radius) {
        int distance = radius * 3 / 4;
        return cx + ((this.town.id().hashCode() & 1) == 0 ? distance : -distance);
    }

    /** Z of the fenced farm plot. */
    private int farmZ(int cz, int radius) {
        int distance = radius * 3 / 4;
        return cz + ((this.town.id().hashCode() & 2) == 0 ? distance : -distance);
    }

    /**
     * A fenced farm plot: a levelled pad, a rail fence with a gate facing the plaza, and a
     * deterministic mix of tilled rubble and overgrowth inside. Skipped over water.
     */
    private static void farm(StructureBuilder b, int fx, int fz, int cx, int cz, int ground, Palette p) {
        int halfX = 6;
        int halfZ = 4;
        if (!touches(b, fx - halfX, fz - halfZ, fx + halfX, fz + halfZ)
                || !landBox(b, fx - halfX, fz - halfZ, fx + halfX, fz + halfZ)) {
            return;
        }
        int y = Math.clamp(b.groundY(fx, fz), ground - 4, ground + 4);
        int x0 = fx - halfX;
        int x1 = fx + halfX;
        int z0 = fz - halfZ;
        int z1 = fz + halfZ;
        for (int x = x0; x <= x1; x++) {
            for (int z = z0; z <= z1; z++) {
                levelColumn(b, x, z, y, p);
            }
        }
        b.walls(x0, y + 1, z0, x1, y + 1, z1, p.rail());
        for (int x = x0 + 1; x < x1; x++) {
            for (int z = z0 + 1; z < z1; z++) {
                if ((hash(x, z, 1) & 3) == 0) {
                    b.put(x, y, z, p.rubble());
                }
                if (p.overgrowth() != null && (hash(x, z, 2) & 7) == 0) {
                    b.put(x, y + 1, z, p.overgrowth());
                }
            }
        }
        // Gate: a two-block opening on the wall facing the town centre, carved last.
        int gateX = fx > cx ? x0 : x1;
        int gateZ = Math.clamp(fz, z0 + 1, z1 - 1);
        b.put(gateX, y + 1, gateZ, AIR);
        if (gateZ + 1 < z1) {
            b.put(gateX, y + 1, gateZ + 1, AIR);
        }
    }

    // ------------------------------------------------------------------ houses

    /** Builds 5–8 varied houses on a ring around the plaza, each on its own levelled pad. */
    private void houses(StructureBuilder b, int cx, int cz, int radius,
                        int farmX, int farmZ, int ground, Palette p) {
        int seed = this.town.id().hashCode();
        int count = 5 + (mix(seed, 1) >>> 1) % 4; // 5..8
        for (int i = 0; i < count; i++) {
            int h = mix(seed, i * 31 + 2);
            double angle = (i + 0.5 + ((h >>> 8) & 0x3F) / 128.0) / count * Math.PI * 2;
            int distance = radius * 55 / 100 + (h >>> 16) % 7;
            int hx = cx + (int) Math.round(Math.cos(angle) * distance);
            int hz = cz + (int) Math.round(Math.sin(angle) * distance);
            long fdx = (long) hx - farmX;
            long fdz = (long) hz - farmZ;
            if (fdx * fdx + fdz * fdz < 14L * 14L) {
                continue; // keep the farm plot clear
            }
            int w = 5 + (h >>> 3) % 4;
            int d = 5 + (h >>> 5) % 4;
            if (Math.abs(Math.cos(angle)) < Math.abs(Math.sin(angle))) {
                int swap = w;
                w = d;
                d = swap;
            }
            buildHouse(b, hx, hz, w, d, 3 + (h >>> 7) % 2, cx, cz, ground, p, h);
        }
    }

    /** One small gothic house: shell, pitched roof, windows, rubble skirt and a walkable door. */
    private static void buildHouse(StructureBuilder b, int hx, int hz, int w, int d, int height,
                                   int cx, int cz, int ground, Palette p, int seed) {
        int x0 = hx - w / 2;
        int z0 = hz - d / 2;
        int x1 = x0 + w - 1;
        int z1 = z0 + d - 1;
        int padX = w / 2 + 2;
        int padZ = d / 2 + 2;
        if (!touches(b, x0 - 1, z0 - 1, x1 + 1, z1 + 1)
                || !landBox(b, x0 - 1, z0 - 1, x1 + 1, z1 + 1)) {
            return;
        }
        int y0 = Math.clamp(b.groundY(hx, hz), ground - 4, ground + 4);
        for (int x = hx - padX; x <= hx + padX; x++) {
            for (int z = hz - padZ; z <= hz + padZ; z++) {
                levelColumn(b, x, z, y0, p);
            }
        }

        int y1 = y0 + height;
        Side doorSide = doorSide(hx, hz, cx, cz);
        int along = (doorSide == Side.N || doorSide == Side.S) ? w : d;
        int doorOffset = 1 + Math.floorMod(seed >>> 11, Math.max(1, along - 2));
        b.room(x0, y0, z0, x1, y1, z1, new Doorway(doorSide, doorOffset));
        b.fill(x0, y0, z0, x1, y0, z1, p.foundation());
        for (int y = y0; y <= y1; y++) {
            b.put(x0, y, z0, p.accent());
            b.put(x1, y, z0, p.accent());
            b.put(x0, y, z1, p.accent());
            b.put(x1, y, z1, p.accent());
        }

        int ridge = w >= d ? 0 : 1;
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, y1, 2 + Math.floorMod(seed >>> 9, 2), ridge);

        // One small window on each wall, one course below the eaves.
        int wy = y0 + Math.min(2, height);
        b.window((x0 + x1) / 2, wy, z0, 1, 1, false);
        b.window((x0 + x1) / 2, wy, z1, 1, 1, false);
        b.window(x0, wy, (z0 + z1) / 2, 1, 1, false);
        b.window(x1, wy, (z0 + z1) / 2, 1, 1, false);

        decorate(b, x0, z0, x1, z1, y0, p);
        placeDoor(b, p, x0, z0, x1, z1, y0 + 1, doorSide, doorOffset);
    }

    /** Which wall the door sits on: the one facing the town centre. */
    private static Side doorSide(int hx, int hz, int cx, int cz) {
        if (Math.abs(hx - cx) >= Math.abs(hz - cz)) {
            return hx >= cx ? Side.W : Side.E;
        }
        return hz >= cz ? Side.N : Side.S;
    }

    /**
     * A little wear without reading as a ruin: rubble at the foot of the walls and overgrowth
     * creeping up them. Pure position hashes, so it is identical in every chunk that draws it.
     */
    private static void decorate(StructureBuilder b, int x0, int z0, int x1, int z1, int y, Palette p) {
        for (int x = x0 - 1; x <= x1 + 1; x++) {
            for (int z = z0 - 1; z <= z1 + 1; z++) {
                if ((hash(x, z, 3) & 15) == 0 && b.isReplaceable(x, y + 1, z)) {
                    b.put(x, y + 1, z, p.rubble());
                }
                if (p.overgrowth() != null && (hash(x, z, 4) & 23) == 0
                        && b.isReplaceable(x, y + 2, z)) {
                    b.put(x, y + 2, z, p.overgrowth());
                }
            }
        }
    }

    /** Sets a two-high palette door, carving the opening first so nothing blocks it. */
    private static void placeDoor(StructureBuilder b, Palette p, int x0, int z0, int x1, int z1,
                                  int y, Side side, int offset) {
        int dx;
        int dz;
        Direction facing;
        switch (side) {
            case N -> { dx = x0 + offset; dz = z0; facing = Direction.NORTH; }
            case S -> { dx = x0 + offset; dz = z1; facing = Direction.SOUTH; }
            case W -> { dx = x0; dz = z0 + offset; facing = Direction.WEST; }
            default -> { dx = x1; dz = z0 + offset; facing = Direction.EAST; }
        }
        b.put(dx, y, dz, AIR);
        b.put(dx, y + 1, dz, AIR);
        BlockState lower = p.door()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, facing)
                .setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.LOWER);
        b.put(dx, y, dz, lower);
        b.put(dx, y + 1, dz, lower.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER));
    }

    // ------------------------------------------------------------------ helpers

    /** True when the inclusive box could touch the chunk currently generating. */
    private static boolean touches(StructureBuilder b, int x0, int z0, int x1, int z1) {
        return x1 >= b.chunkMinX() && x0 <= b.chunkMinX() + 15
                && z1 >= b.chunkMinZ() && z0 <= b.chunkMinZ() + 15;
    }

    /** True when every column of the inclusive box is land. */
    private static boolean landBox(StructureBuilder b, int x0, int z0, int x1, int z1) {
        for (int x = x0; x <= x1; x++) {
            for (int z = z0; z <= z1; z++) {
                if (!b.isLand(x, z)) {
                    return false;
                }
            }
        }
        return true;
    }

    /** Stable per-cell hash (no RNG stream), so decoration is chunk-order independent. */
    private static int hash(int x, int z, int salt) {
        int h = x * 0x9E3779B9 ^ z * 0x85EBCA6B ^ salt * 0xC2B2AE35;
        h ^= h >>> 15;
        h *= 0x2C1B3C6D;
        h ^= h >>> 12;
        return h;
    }

    /** Mixes the town seed with a feature index. */
    private static int mix(int seed, int n) {
        int h = seed ^ (n * 0x9E3779B9);
        h ^= h >>> 16;
        h *= 0x7FEB352D;
        h ^= h >>> 15;
        return h;
    }
}
