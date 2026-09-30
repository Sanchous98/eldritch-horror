package com.sanchous98.eldritchhorror.world.loc.site;

import com.sanchous98.eldritchhorror.world.loc.Location;
import com.sanchous98.eldritchhorror.world.loc.Palette;
import com.sanchous98.eldritchhorror.world.loc.StructureBuilder;
import com.sanchous98.eldritchhorror.world.loc.Tier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;

/**
 * A minor open-air ritual site: a clearly built raised plinth and a marked central altar, ringed
 * by a readable circle of tall standing stones, with burnt offerings and a broken processional
 * path. Something was summoned here — and answered.
 *
 * <p>The whole site is built at roughly double the earlier scale so the concentric steps, the
 * marked altar and the stone circle are legible from directly above, not just at ground level.
 */
public final class RitualAltarSite implements Location {

    /** Radius of the stone circle (blocks). */
    private static final int RING = 30;
    /** Plinth half-width: the raised stepped dais under the altar. */
    private static final int PLINTH = 13;
    /** Number of standing stones in the ring. */
    private static final int STONES = 16;
    /** Number of stepped plinth tiers, one course apart. */
    private static final int TIERS = 4;

    private final int centerX;
    private final int centerZ;

    /** Constructed at a fixed centre; {@link #build} has no coordinate argument. */
    public RitualAltarSite(int x, int z) {
        this.centerX = x;
        this.centerZ = z;
    }

    @Override
    public String id() {
        return "eldritch_horror:site/ritual_altar_site";
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
        return RING + 8; // the built circle (RING+6), not the 60 cull radius
    }

    @Override
    public void build(StructureBuilder b) {
        int cx = this.centerX;
        int cz = this.centerZ;
        int ground = b.groundY(cx, cz);
        Palette p = b.palette();
        RandomSource rng = b.rng();
        int y0 = ground + 1;

        // Trampled, ash-streaked clearing. Land-gated and chunk-local (no rng), so a coastal edge
        // is never paved as a floating slab and the per-chunk cost stays flat.
        int x0 = b.chunkMinX();
        int z0 = b.chunkMinZ();
        int clear = RING + 6;
        int c2 = clear * clear;
        for (int x = x0; x < x0 + 16; x++) {
            int dx = x - cx;
            if (dx * dx > c2) {
                continue;
            }
            for (int z = z0; z < z0 + 16; z++) {
                int dz = z - cz;
                if (dx * dx + dz * dz > c2 || !b.isLand(x, z)) {
                    continue;
                }
                b.ground(x, z, x, z, ground, ground, p.ground());
                // Clear biome vegetation above the clearing so the ring is not buried in trees.
                for (int y = ground + 1; y <= ground + 12; y++) {
                    b.put(x, y, z, Blocks.AIR.defaultBlockState());
                }
            }
        }

        // The stone circle: evenly spaced TALL monoliths of graded height, each on a wider paved
        // base, so the ring reads as a deliberate arrangement from the top view.
        for (int i = 0; i < STONES; i++) {
            double a = i * 2.0 * Math.PI / STONES;
            int mx = cx + (int) Math.round(Math.cos(a) * RING);
            int mz = cz + (int) Math.round(Math.sin(a) * RING);
            if (!b.isLand(mx, mz)) {
                continue; // never plant a stone over water
            }
            b.ground(mx - 2, mz - 2, mx + 2, mz + 2, ground, ground, p.foundation());
            standingStone(b, rng, mx, mz, y0, p);
        }

        // The altar dais at the centre.
        altar(b, p, cx, cz, ground);

        // A broken processional path along Z.
        path(b, rng, cx, cz - RING, cz + RING + 6, y0, p);

        // Ash, burnt debris, cobwebs, and the marks of something dragged away.
        b.scatter(cx - RING, cz - RING, cx + RING, cz + RING, y0, y0 + 1, p.rubble(), 0.18f);
        b.scatter(cx - RING, cz - RING, cx + RING, cz + RING, y0 + 1, y0 + 4,
                Palette.cobweb(), 0.03f);
        if (p.overgrowth() != null) {
            b.scatter(cx - RING, cz - RING, cx + RING, cz + RING, y0 + 1, y0 + 2,
                    p.overgrowth(), 0.05f);
        }

        b.marker("ritual_altar_site", cx, y0, cz);
    }

    // ------------------------------------------------------------------ pieces

    /**
     * A raised, four-tier stepped plinth carrying a marked altar. Each tier is visibly inset by
     * two blocks and exactly one course higher than the last, so the height shading reads clearly
     * from above; the top step carries accent posts, a distinct altar block and a lit brazier.
     */
    private static void altar(StructureBuilder b, Palette p,
                              int cx, int cz, int ground) {
        int y0 = ground + 1;
        for (int t = 0; t < TIERS; t++) {
            int inset = t * 2;
            int h = PLINTH - inset;
            if (h < 1) {
                break;
            }
            // Alternating foundation / wall courses make the step edges legible in height shading.
            b.fill(cx - h, y0 + t, cz - h, cx + h, y0 + t, cz + h,
                    (t & 1) == 0 ? p.foundation() : p.wall());
        }
        int top = y0 + TIERS - 1;
        int topHalf = Math.max(1, PLINTH - (TIERS - 1) * 2);
        // Accent posts around the top step mark the ritual margin.
        for (int i = 0; i < 8; i++) {
            double a = i * Math.PI / 4.0;
            int px = cx + (int) Math.round(Math.cos(a) * topHalf);
            int pz = cz + (int) Math.round(Math.sin(a) * topHalf);
            b.put(px, top + 1, pz, p.accent());
        }
        // The defiled altar block itself: weathered, deliberately distinct from the plinth.
        b.fill(cx - 1, top + 1, cz - 1, cx + 1, top + 1, cz + 1, p.weathered());
        b.put(cx, top + 2, cz, p.frame());
        b.put(cx + 1, top + 1, cz + 1, p.light());
        // A lit brazier beside the altar, with a fuel course beneath the flame.
        b.put(cx - 2, top + 1, cz, p.foundation());
        b.put(cx - 2, top + 2, cz, p.light());
        b.put(cx - 1, top + 2, cz + 1, p.accent());
    }

    /** One standing stone: a TALL, slightly irregular pillar on its paved base. */
    private static void standingStone(StructureBuilder b, RandomSource rng, int x, int z, int y0,
                                      Palette p) {
        int h = 6 + rng.nextInt(5); // 6..10 tall, so the circle carries from above
        for (int i = 0; i < h; i++) {
            b.put(x, y0 + i, z, i == 0 ? p.foundation() : p.wall());
        }
        if (rng.nextFloat() < 0.5f) {
            b.put(x, y0 + h, z, p.accent());
        }
    }

    private static void path(StructureBuilder b, RandomSource rng, int cx, int z0, int z1,
                             int y0, Palette p) {
        for (int z = z0; z <= z1; z++) {
            int half = 2;
            b.ground(cx - half, z, cx + half, z, y0 - 1, y0 - 1, p.ground());
            if (rng.nextFloat() < 0.35f) {
                b.put(cx + rng.nextInt(2 * half + 1) - half, y0, z, p.rubble());
            }
        }
    }
}
