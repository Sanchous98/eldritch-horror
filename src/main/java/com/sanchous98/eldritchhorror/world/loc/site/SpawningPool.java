package com.sanchous98.eldritchhorror.world.loc.site;

import com.sanchous98.eldritchhorror.world.loc.Location;
import com.sanchous98.eldritchhorror.world.loc.Palette;
import com.sanchous98.eldritchhorror.world.loc.StructureBuilder;
import com.sanchous98.eldritchhorror.world.loc.Tier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The Spawning Pool — the filth-pool of Abhoth (design/28). A reeking, sunken basin of green
 * fluid ringed by a raised mud rim, dotted with lumpy organic growths and a central mass of
 * slime that is never quite still. The pool is sealed: water is placed only inside the bowl, one
 * course below the levelled rim, so it cannot leak into the surrounding terrain.
 *
 * <p>Depth is a deterministic function of distance to the centre, and the fluid is laid column by
 * column only down to the bowl floor, so the whole thing is a genuinely contained basin.
 */
public final class SpawningPool implements Location {

    /** Radius of the sunken basin (blocks). */
    private static final int POOL = 20;
    /** Radius of the raised rim just outside the basin. */
    private static final int RIM = 22;
    /** Radius of the levelled ground. */
    private static final int FIELD = POOL + 10;
    /** Depth of the basin at its centre (blocks). */
    private static final int DEPTH = 9;

    private final int centerX;
    private final int centerZ;

    /** Constructed at a fixed centre; {@link #build} has no coordinate argument. */
    public SpawningPool(int x, int z) {
        this.centerX = x;
        this.centerZ = z;
    }

    @Override
    public String id() {
        return "eldritch_horror:site/spawning_pool";
    }

    @Override
    public Tier tier() {
        return Tier.MINOR;
    }

    @Override
    public int radius() {
        return 60; // cull radius — unchanged
    }

    @Override
    public int renderRadius() {
        return FIELD + 4; // the levelled basin, not the larger cull radius
    }

    @Override
    public void build(StructureBuilder b) {
        int cx = this.centerX;
        int cz = this.centerZ;
        int ground = baseLevel(b, cx, cz, FIELD);
        Palette p = b.palette();
        RandomSource rng = b.rng();

        // Level the muddy apron to a single datum.
        level(b, cx, cz, ground, p);

        // Sink the sealed basin and fill it with fluid.
        basin(b, cx, cz, ground);

        // A raised, reeking mud rim around the basin.
        rim(b, cx, cz, ground);

        // Lumpy organic growths on the shoreline and a seething mass at the centre.
        lumps(b, rng, cx, cz, ground);

        // Fringe dressing.
        int y0 = ground + 1;
        b.scatter(cx - FIELD, cz - FIELD, cx + FIELD, cz + FIELD, y0, y0, p.rubble(), 0.14f);
        if (p.overgrowth() != null) {
            b.scatter(cx - FIELD, cz - FIELD, cx + FIELD, cz + FIELD, y0, y0 + 1,
                    p.overgrowth(), 0.06f);
        }

        b.marker("spawning_pool", cx, y0, cz);
    }

    // ------------------------------------------------------------------ pieces

    /**
     * Sinks the basin: each column's depth falls off linearly from the centre, the bowl is carved
     * and floored with mud/clay/moss, then water is placed from the floor up to one course below
     * the rim. Columns too shallow to carve are left solid, so no water touches the open terrain.
     */
    private static void basin(StructureBuilder b, int cx, int cz, int ground) {
        BlockState water = Blocks.WATER.defaultBlockState();
        int waterTop = ground - 1;
        int x0 = b.chunkMinX();
        int z0 = b.chunkMinZ();
        for (int x = x0; x < x0 + 16; x++) {
            int dx = x - cx;
            if (dx * dx > POOL * POOL) {
                continue;
            }
            for (int z = z0; z < z0 + 16; z++) {
                int dz = z - cz;
                int d2 = dx * dx + dz * dz;
                if (d2 > POOL * POOL || !b.isLand(x, z)) {
                    continue;
                }
                int r = (int) Math.sqrt(d2);
                int depth = (POOL - r) * DEPTH / POOL;
                if (depth < 2) {
                    continue; // rim column: leave it solid so the pool is sealed
                }
                int floor = ground - depth;
                for (int y = floor + 1; y <= ground; y++) {
                    b.put(x, y, z, y <= waterTop ? water : Blocks.AIR.defaultBlockState());
                }
                b.put(x, floor, z, floorBlock(x, z));
            }
        }
    }

    /** Deterministic floor material: mud, with clay and moss patches. No rng (chunk-safe). */
    private static BlockState floorBlock(int x, int z) {
        int h = Math.floorMod(x * 31 + z * 17, 15);
        if (h == 0) {
            return Blocks.MOSS_BLOCK.defaultBlockState();
        }
        if (h < 4) {
            return Blocks.CLAY.defaultBlockState();
        }
        return Blocks.MUD.defaultBlockState();
    }

    /** A raised green-mud rim ring outside the basin, deliberately uneven. */
    private static void rim(StructureBuilder b, int cx, int cz, int ground) {
        BlockState terra = Blocks.DYED_TERRACOTTA.pick(DyeColor.GREEN).defaultBlockState();
        BlockState moss = Blocks.MOSS_BLOCK.defaultBlockState();
        BlockState conc = Blocks.CONCRETE.pick(DyeColor.GREEN).defaultBlockState();
        for (int i = 0; i < 96; i++) {
            double a = i * 2.0 * Math.PI / 96.0;
            int x = cx + (int) Math.round(Math.cos(a) * RIM);
            int z = cz + (int) Math.round(Math.sin(a) * RIM);
            if (!b.isLand(x, z)) {
                continue;
            }
            b.put(x, ground + 1, z, ((i & 1) == 0) ? terra : moss);
            if (i % 3 == 0) {
                b.put(x, ground + 2, z, conc);
            }
        }
    }

    /** Organic lumps: slime/honey mounds on the shoreline and a central seething mass. */
    private static void lumps(StructureBuilder b, RandomSource rng, int cx, int cz, int ground) {
        // A central mass rising from the fluid.
        mass(b, cx, cz, ground - 1);
        // Shoreline lumps, at absolute positions, so rng is chunk-consistent.
        for (int i = 0; i < 26; i++) {
            double a = i * 2.0 * Math.PI / 26.0 + (rng.nextFloat() - 0.5f) * 0.15f;
            int r = RIM - 2 - rng.nextInt(5);
            int x = cx + (int) Math.round(Math.cos(a) * r);
            int z = cz + (int) Math.round(Math.sin(a) * r);
            if (!b.isLand(x, z)) {
                continue;
            }
            int h = 1 + rng.nextInt(3);
            for (int y = 1; y <= h; y++) {
                b.put(x, ground + y, z, lumpBlock(rng, x, z, y));
            }
        }
    }

    /** The Abhoth mass: a lumpy green mound over the centre of the pool. */
    private static void mass(StructureBuilder b, int cx, int cz, int waterTop) {
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                int d = Math.max(Math.abs(dx), Math.abs(dz));
                if (d > 3) {
                    continue;
                }
                b.put(cx + dx, waterTop + 1, cz + dz, d <= 1
                        ? Blocks.SLIME_BLOCK.defaultBlockState()
                        : Blocks.CONCRETE.pick(DyeColor.GREEN).defaultBlockState());
            }
        }
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                b.put(cx + dx, waterTop + 2, cz + dz, Blocks.HONEY_BLOCK.defaultBlockState());
            }
        }
        b.put(cx, waterTop + 3, cz, Blocks.SLIME_BLOCK.defaultBlockState());
        b.put(cx + 1, waterTop + 3, cz, Blocks.DYED_TERRACOTTA.pick(DyeColor.GREEN).defaultBlockState());
        b.put(cx, waterTop + 3, cz + 1, Blocks.DYED_TERRACOTTA.pick(DyeColor.GREEN).defaultBlockState());
    }

    private static BlockState lumpBlock(RandomSource rng, int x, int z, int y) {
        if (rng.nextFloat() < 0.35f) {
            return Blocks.HONEY_BLOCK.defaultBlockState();
        }
        if ((x + z + y) % 2 == 0) {
            return Blocks.SLIME_BLOCK.defaultBlockState();
        }
        return Blocks.MOSS_BLOCK.defaultBlockState();
    }

    /** Mean surface over a 5x5 sample of the footprint: one datum for the whole basin. */
    private static int baseLevel(StructureBuilder b, int cx, int cz, int field) {
        long sum = 0;
        int n = 0;
        for (int i = -2; i <= 2; i++) {
            for (int j = -2; j <= 2; j++) {
                int x = cx + i * field / 2;
                int z = cz + j * field / 2;
                if (b.isLand(x, z)) {
                    sum += b.groundY(x, z);
                    n++;
                }
            }
        }
        return n == 0 ? b.groundY(cx, cz) : (int) (sum / n);
    }

    /**
     * Cut hills to the datum, fill hollows, and clear vegetation. The apron is capped with dirt in
     * the pool's reach and the biome ground outside it, so the basin always sinks into soft filth.
     */
    private static void level(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        BlockState air = Blocks.AIR.defaultBlockState();
        BlockState dirt = Blocks.DIRT.defaultBlockState();
        int x0 = b.chunkMinX();
        int z0 = b.chunkMinZ();
        int f2 = FIELD * FIELD;
        int pool2 = POOL * POOL;
        for (int x = x0; x < x0 + 16; x++) {
            int dx = x - cx;
            if (dx * dx > f2) {
                continue;
            }
            for (int z = z0; z < z0 + 16; z++) {
                int dz = z - cz;
                int d2 = dx * dx + dz * dz;
                if (d2 > f2 || !b.isLand(x, z)) {
                    continue;
                }
                int surface = b.groundY(x, z);
                if (surface > ground) {
                    b.fill(x, ground + 1, z, x, surface, z, air);
                } else if (surface < ground) {
                    b.fill(x, surface + 1, z, x, ground, z, p.foundation());
                }
                b.put(x, ground, z, d2 <= pool2 ? dirt : p.ground());
                for (int y = ground + 1; y <= ground + 14; y++) {
                    b.put(x, y, z, air);
                }
            }
        }
    }
}
