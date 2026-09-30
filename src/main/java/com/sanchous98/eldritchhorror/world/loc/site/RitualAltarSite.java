package com.sanchous98.eldritchhorror.world.loc.site;

import com.sanchous98.eldritchhorror.world.loc.Location;
import com.sanchous98.eldritchhorror.world.loc.Palette;
import com.sanchous98.eldritchhorror.world.loc.StructureBuilder;
import com.sanchous98.eldritchhorror.world.loc.Tier;
import net.minecraft.util.RandomSource;

/**
 * A minor open-air ritual site: a clearly built raised plinth and a marked central altar, ringed
 * by a readable circle of standing stones, with burnt offerings and a broken processional path.
 * Something was summoned here — and answered.
 */
public final class RitualAltarSite implements Location {

    /** Radius of the stone circle (blocks). */
    private static final int RING = 22;
    /** Plinth half-width: the raised stepped dais under the altar. */
    private static final int PLINTH = 7;
    /** Number of standing stones in the ring. */
    private static final int STONES = 12;

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
        return RING + 30; // render the built circle, not the 60 cull radius
    }

    @Override
    public void build(StructureBuilder b) {
        int cx = this.centerX;
        int cz = this.centerZ;
        int ground = b.groundY(cx, cz);
        Palette p = b.palette();
        RandomSource rng = b.rng();
        int y0 = ground + 1;

        // Trampled, ash-streaked clearing.
        b.ground(cx - RING - 6, cz - RING - 6, cx + RING + 6, cz + RING + 6,
                ground, ground, p.ground());

        // The stone circle: evenly spaced standing stones of graded height, each on a small paved
        // base so the ring reads as a deliberate arrangement rather than a random clump.
        for (int i = 0; i < STONES; i++) {
            double a = i * 2.0 * Math.PI / STONES;
            int mx = cx + (int) Math.round(Math.cos(a) * RING);
            int mz = cz + (int) Math.round(Math.sin(a) * RING);
            b.ground(mx - 1, mz - 1, mx + 1, mz + 1, ground, ground, p.foundation());
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

    /** A raised, stepped plinth carrying a marked altar: the site's unmistakable built centre. */
    private static void altar(StructureBuilder b, Palette p, int cx, int cz, int ground) {
        int y0 = ground + 1;
        // Three stepped courses, widest at the base, so the dais reads as architecture.
        b.fill(cx - PLINTH, y0, cz - PLINTH, cx + PLINTH, y0, cz + PLINTH, p.foundation());
        b.fill(cx - PLINTH + 2, y0 + 1, cz - PLINTH + 2, cx + PLINTH - 2, y0 + 1, cz + PLINTH - 2,
                p.wall());
        b.fill(cx - PLINTH + 4, y0 + 2, cz - PLINTH + 4, cx + PLINTH - 4, y0 + 2, cz + PLINTH - 4,
                p.foundation());
        // A ring of accent posts around the top step reads as a marked ritual margin.
        for (int i = 0; i < 8; i++) {
            double a = i * Math.PI / 4.0;
            int px = cx + (int) Math.round(Math.cos(a) * (PLINTH - 4));
            int pz = cz + (int) Math.round(Math.sin(a) * (PLINTH - 4));
            b.put(px, y0 + 3, pz, p.accent());
        }
        // The defiled altar block itself: weathered, deliberately distinct from the plinth.
        b.fill(cx - 1, y0 + 3, cz - 1, cx + 1, y0 + 3, cz + 1, p.weathered());
        b.put(cx + 1, y0 + 4, cz + 1, p.light());
        b.put(cx - 2, y0 + 4, cz + 1, p.frame());
        b.put(cx - 2, y0 + 5, cz + 1, p.accent());
    }

    /** One standing stone: a graded, slightly irregular pillar on its paved base. */
    private static void standingStone(StructureBuilder b, RandomSource rng, int x, int z, int y0,
                                      Palette p) {
        int h = 4 + rng.nextInt(4);
        for (int i = 0; i < h; i++) {
            b.put(x, y0 + i, z, i == 0 ? p.foundation() : p.wall());
        }
        if (rng.nextFloat() < 0.4f) {
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
