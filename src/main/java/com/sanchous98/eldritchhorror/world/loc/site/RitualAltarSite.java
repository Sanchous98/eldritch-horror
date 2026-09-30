package com.sanchous98.eldritchhorror.world.loc.site;

import com.sanchous98.eldritchhorror.world.loc.Location;
import com.sanchous98.eldritchhorror.world.loc.Palette;
import com.sanchous98.eldritchhorror.world.loc.StructureBuilder;
import com.sanchous98.eldritchhorror.world.loc.Tier;
import net.minecraft.util.RandomSource;

/**
 * A minor open-air ritual site: a ring of monolith stones around a raised, defiled altar dais,
 * with burnt offerings and a broken processional path. Something was summoned here — and
 * answered.
 */
public final class RitualAltarSite implements Location {

    /** Radius of the stone circle (blocks). */
    private static final int RING = 22;

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
        return 60;
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

        // The stone circle: a monument at each of eight stations.
        for (int i = 0; i < 8; i++) {
            double a = i * Math.PI / 4.0;
            int mx = cx + (int) Math.round(Math.cos(a) * RING);
            int mz = cz + (int) Math.round(Math.sin(a) * RING);
            b.ground(mx - 2, mz - 2, mx + 2, mz + 2, ground, ground, p.foundation());
            b.monument(mx, mz, y0);
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

    private static void altar(StructureBuilder b, Palette p, int cx, int cz, int ground) {
        int y0 = ground + 1;
        b.fill(cx - 5, y0, cz - 5, cx + 5, y0, cz + 5, p.foundation());
        b.fill(cx - 3, y0 + 1, cz - 3, cx + 3, y0 + 1, cz + 3, p.foundation());
        b.fill(cx - 2, y0 + 2, cz - 2, cx + 2, y0 + 2, cz + 2, p.accent());
        // The defiled altar block itself.
        b.fill(cx - 1, y0 + 3, cz - 1, cx + 1, y0 + 3, cz + 1, p.weathered());
        // A single candle, and a stone lectern facing the altar.
        b.put(cx + 1, y0 + 4, cz + 1, p.light());
        b.put(cx - 2, y0 + 4, cz + 1, p.frame());
        b.put(cx - 2, y0 + 5, cz + 1, p.accent());
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
