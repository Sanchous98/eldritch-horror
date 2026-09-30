package com.sanchous98.eldritchhorror.world.loc.site;

import com.sanchous98.eldritchhorror.world.ElevationCurve;
import com.sanchous98.eldritchhorror.world.loc.Location;
import com.sanchous98.eldritchhorror.world.loc.Palette;
import com.sanchous98.eldritchhorror.world.loc.StructureBuilder;
import com.sanchous98.eldritchhorror.world.loc.Tier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A minor tear in the world: a long, narrow canyon meandering through the terrain, its broken lips
 * fringed with shattered stone and jagged spurs rising like broken teeth, and a great leaning
 * monolith marking the rim. The air is wrong here and the scar is still, faintly, widening.
 *
 * <p>Because a site can land on steep ground (this one is reviewed on a mountain), the whole
 * footprint is first levelled to a single datum — exactly as the city paves its district — so the
 * trench is always cut from a flat plateau and reads as a canyon rather than a dent in a hillside.
 */
public final class RiftScar implements Location {

    /** Half-length of the fissure along Z (blocks). */
    private static final int LENGTH = 20;
    /** Radius of the levelled plateau (blocks); the trench stays well inside it. */
    private static final int FIELD = 22;
    /** Maximum centreline wander of the trench (blocks). */
    private static final int MEANDER = 7;

    private final int centerX;
    private final int centerZ;

    /** Constructed at a fixed centre; {@link #build} has no coordinate argument. */
    public RiftScar(int x, int z) {
        this.centerX = x;
        this.centerZ = z;
    }

    @Override
    public String id() {
        return "eldritch_horror:site/rift_scar";
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
        // The built footprint is the FIELD-sized plateau (±18) with the rim monolith at +16; a
        // 60-radius render would shrink it to a speck. Frame just past the levelled edge.
        return FIELD + 12;
    }

    @Override
    public void build(StructureBuilder b) {
        int cx = this.centerX;
        int cz = this.centerZ;
        // One datum for the whole site: the mean surface over the footprint. On a slope this is
        // far cheaper (and calmer) than the centre point, but still a single level to cut/fill to.
        int ground = baseLevel(b, cx, cz, FIELD);
        Palette p = b.palette();
        RandomSource rng = b.rng();
        int y0 = ground + 1;

        // 1. Level the footprint: cut hills down and fill hollows up to the datum, then cap every
        //    column with a ground course. Chunk-clipped, like the city's district paving.
        level(b, cx, cz, ground, p, FIELD);

        // 2. Shattered, tainted debris thrown out across the plateau. Laid before the cut, so the
        //    fissure removes whatever falls in its path and nothing ends up floating over the void.
        b.scatter(cx - FIELD, cz - FIELD, cx + FIELD, cz + FIELD, y0, y0, p.rubble(), 0.22f);
        b.scatter(cx - FIELD, cz - FIELD, cx + FIELD, cz + FIELD, y0 + 1, y0 + 5,
                Palette.cobweb(), 0.04f);
        if (p.overgrowth() != null) {
            b.scatter(cx - FIELD, cz - FIELD, cx + FIELD, cz + FIELD, y0 + 1, y0 + 2,
                    p.overgrowth(), 0.04f);
        }

        // 3. Carve the deep meandering trench through the levelled plateau. Its walls are now the
        //    full trench depth everywhere, so it cannot be mistaken for a fold in the hillside.
        fissure(b, rng, p, cx, cz, ground, LENGTH);

        // 4. Jagged broken stone spurs along both lips of the tear, clear of the trench mouth.
        spurs(b, rng, p, cx + 15, cz + 4, ground);
        spurs(b, rng, p, cx - 15, cz - 10, ground);

        // 5. A leaning monolith anchoring the rim, grounded on the levelled plateau beside the cut.
        monolith(b, p, cx + 16, cz - 6, ground);

        b.marker("rift_scar", cx, y0, cz);
    }

    // ------------------------------------------------------------------ pieces

    /**
     * The levelling pass. For every land column of the footprint: cut the hill down to the datum,
     * fill a hollow up to it, cap with a ground course, and clear a little vegetation above. The
     * plateau that remains is the high ground the trench is cut into — the key to a legible canyon.
     */
    private static void level(StructureBuilder b, int cx, int cz, int ground, Palette p, int field) {
        BlockState air = Blocks.AIR.defaultBlockState();
        // Only the current chunk's columns are visited (O(256)/chunk), like CityLocation.paveDistrict.
        // This pass uses no rng, so chunk-local iteration produces byte-identical writes and keeps
        // the per-chunk cost flat.
        int x0 = b.chunkMinX();
        int z0 = b.chunkMinZ();
        int f2 = field * field;
        for (int x = x0; x < x0 + 16; x++) {
            int dx = x - cx;
            if (dx * dx > f2) {
                continue;
            }
            for (int z = z0; z < z0 + 16; z++) {
                int dz = z - cz;
                if (dx * dx + dz * dz > f2) {
                    continue;
                }
                if (!b.isLand(x, z)) {
                    continue;
                }
                int surface = b.groundY(x, z);
                if (surface > ground) {
                    b.fill(x, ground + 1, z, x, surface, z, air);
                } else if (surface < ground) {
                    b.fill(x, surface + 1, z, x, ground, z, p.foundation());
                }
                b.put(x, ground, z, p.ground());
                // Clear the biome vegetation above the plateau so the canyon is not buried in
                // trees (the city does the same over its district).
                for (int y = ground + 1; y <= ground + 14; y++) {
                    b.put(x, y, z, air);
                }
            }
        }
    }

    /**
     * Mean surface over a 5×5 sample of the footprint: a single datum that sits close to the local
     * ground even on a slope, so levelling neither floats the site nor buries it. Deterministic.
     */
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
     * A deep, wide, meandering canyon cut from the levelled datum: the centreline wanders along Z,
     * the width tapers from a broad 7–11 blocks to a hairline crack at the ends, and the depth
     * runs 24–36 blocks in the heart of the scar so the walls are tall and shadowed. Broken,
     * jagged lips are edged with rubble and taint, and the floor is weathered, tainted debris.
     */
    private static void fissure(StructureBuilder b, RandomSource rng, Palette p,
                                int cx, int cz, int ground, int length) {
        BlockState air = Blocks.AIR.defaultBlockState();
        BlockState taint = Blocks.SCULK.defaultBlockState();
        BlockState vein = Blocks.SCULK_VEIN.defaultBlockState();
        int maxHalf = 3 + rng.nextInt(3);   // 3..5 -> a 7..11 block wide cut
        int maxDepth = 24 + rng.nextInt(13); // 24..36 blocks deep at the heart
        int wander = 0;
        for (int dz = -length; dz <= length; dz++) {
            int z = cz + dz;
            int t = Math.abs(dz);
            // Meander: a bounded random walk of the centreline.
            wander = Math.clamp(wander + rng.nextInt(3) - 1, -MEANDER, MEANDER);
            int mx = cx + wander;
            int halfW = Math.max(1, maxHalf - t / 9);          // taper the width at the ends
            int depth = Math.max(4, maxDepth * (length + 6 - t) / (length + 6));
            // Never cut below the world floor: the generator lays no bedrock, so an unclamped
            // trench on low ground would open a hole into the void.
            depth = Math.min(depth, ground - ElevationCurve.MIN_Y - 2);
            // Broken edges: jitter each wall out independently, deterministically per row.
            int left = mx - halfW - rng.nextInt(2);
            int right = mx + halfW + rng.nextInt(2);
            for (int x = left; x <= right; x++) {
                for (int k = 0; k <= depth; k++) {
                    b.put(x, ground - k, z, air);
                }
                // A tainted, weathered rubble floor.
                b.put(x, ground - depth - 1, z, p.rubble());
                if (rng.nextFloat() < 0.5f) {
                    b.put(x, ground - depth, z, p.weathered());
                }
                if (rng.nextFloat() < 0.18f) {
                    b.put(x, ground - depth, z, taint);
                }
                // Taint creeping up the lower wall.
                if (rng.nextFloat() < 0.10f) {
                    b.put(x, ground - depth + 1, z, vein);
                }
            }
            // Jagged, broken lips: broken stone and taint thrown up along both rims.
            if (rng.nextFloat() < 0.7f) {
                b.put(left - 1, ground, z, p.rubble());
            }
            if (rng.nextFloat() < 0.7f) {
                b.put(right + 1, ground, z, p.weathered());
            }
            if (rng.nextFloat() < 0.25f) {
                b.put(left - 1, ground + 1, z, p.rubble());
            }
            if (rng.nextFloat() < 0.25f) {
                b.put(right + 1, ground + 1, z, p.weathered());
            }
            if (rng.nextFloat() < 0.12f) {
                b.put(left - 1, ground, z, taint);
            }
            if (rng.nextFloat() < 0.12f) {
                b.put(right + 1, ground, z, taint);
            }
        }
    }

    /** Jagged spurs of shattered stone leaning out of a lip, rising and falling along the scar. */
    private static void spurs(StructureBuilder b, RandomSource rng, Palette p, int x, int cz,
                              int ground) {
        int lean = 0;
        for (int z = cz - 16; z <= cz + 16; z += 3) {
            if (rng.nextFloat() < 0.35f) {
                continue;
            }
            if (!b.isLand(x, z)) {
                continue; // never hang a spur over water
            }
            int h = 3 + rng.nextInt(5);
            lean = Math.clamp(lean + rng.nextInt(3) - 1, -2, 2);
            for (int i = 0; i < h; i++) {
                b.put(x + lean + i / 3, ground + 1 + i, z, i == 0 ? p.foundation() : p.rubble());
            }
        }
    }

    /** A tall, leaning monolith of ashlar and rubble, split near the top: the rift anchor. */
    private static void monolith(StructureBuilder b, Palette p, int x, int z, int ground) {
        int y0 = ground + 1;
        int h = 18;
        // The base is a small scorched mound, sunk to the levelled datum.
        b.ground(x - 2, z - 2, x + 2, z + 2, ground, ground, p.rubble());
        for (int i = 0; i < h; i++) {
            int lx = x + i / 5; // lean the shaft as it rises
            b.put(lx, y0 + i, z, i % 4 == 0 ? p.accent() : p.wall());
            if (i < 3) {
                b.put(lx - 1, y0 + i, z, p.foundation());
                b.put(lx + 1, y0 + i, z, p.foundation());
            }
        }
        // A snapped crown with a glowing rent in the stone.
        b.put(x + h / 5, y0 + h, z, p.accent());
        b.put(x + h / 5, y0 + h - 2, z, p.light());
        b.put(x + h / 5 - 1, y0 + h - 4, z, p.weathered());
    }
}
