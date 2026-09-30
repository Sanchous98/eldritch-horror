package com.sanchous98.eldritchhorror.world.loc.site;

import com.sanchous98.eldritchhorror.world.loc.Location;
import com.sanchous98.eldritchhorror.world.loc.Palette;
import com.sanchous98.eldritchhorror.world.loc.StructureBuilder;
import com.sanchous98.eldritchhorror.world.loc.Tier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;

/**
 * A minor tear in the world: a long, ragged fissure in the ground fringed with shattered stone,
 * jagged spurs rising like broken teeth and a snapped arch standing at its lip. The air is wrong
 * here and the scar is still, faintly, widening.
 */
public final class RiftScar implements Location {

    /** Half-length of the fissure along Z (blocks). */
    private static final int LENGTH = 26;
    /** Half-width of the rubble field around the fissure (blocks). */
    private static final int FIELD = 18;

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

        // Rubble-strewn ground around the tear.
        b.ground(cx - FIELD, cz - FIELD, cx + FIELD, cz + FIELD, ground, ground, p.ground());

        // The fissure itself: a tapering wedge opened down the middle.
        fissure(b, p, cx, cz, ground, LENGTH);

        // Broken stone spurs along the eastern lip.
        spurs(b, rng, p, cx + 5, cz, ground);

        // A fractured arch spanning the middle of the scar.
        arch(b, p, cx, cz, ground);

        // Shattered debris thrown out from the rift.
        b.scatter(cx - FIELD, cz - FIELD, cx + FIELD, cz + FIELD, y0, y0 + 1, p.rubble(), 0.22f);
        b.scatter(cx - FIELD, cz - FIELD, cx + FIELD, cz + FIELD, y0 + 1, y0 + 5,
                Palette.cobweb(), 0.04f);
        if (p.overgrowth() != null) {
            b.scatter(cx - FIELD, cz - FIELD, cx + FIELD, cz + FIELD, y0 + 1, y0 + 2,
                    p.overgrowth(), 0.04f);
        }

        b.marker("rift_scar", cx, y0, cz);
    }

    // ------------------------------------------------------------------ pieces

    /** A tapering trench: deep at the centre, closing to a crack at the ends. */
    private static void fissure(StructureBuilder b, Palette p, int cx, int cz, int ground, int length) {
        for (int dz = -length; dz <= length; dz++) {
            int z = cz + dz;
            int t = Math.abs(dz);
            int halfW = Math.max(0, (length - t) / 6);
            int depth = Math.max(1, (length - t) / 5);
            for (int dx = -halfW; dx <= halfW; dx++) {
                for (int k = 0; k <= depth; k++) {
                    b.put(cx + dx, ground - k, z, Blocks.AIR.defaultBlockState());
                }
                // A dark floor of rubble in the bottom.
                b.put(cx + dx, ground - depth - 1, z, p.rubble());
            }
        }
    }

    /** Jagged spurs of shattered stone leaning out of the eastern lip. */
    private static void spurs(StructureBuilder b, RandomSource rng, Palette p, int x, int cz,
                              int ground) {
        for (int z = cz - 18; z <= cz + 18; z += 5) {
            if (rng.nextFloat() < 0.25f) {
                continue;
            }
            int h = 3 + rng.nextInt(4);
            for (int i = 0; i < h; i++) {
                b.put(x + i / 3, ground + 1 + i, z, i == 0 ? p.foundation() : p.rubble());
            }
        }
    }

    private static void arch(StructureBuilder b, Palette p, int cx, int cz, int ground) {
        int y0 = ground + 1;
        int legH = 6;
        // Two leaning legs of ashlar, snapped at the crown.
        for (int i = 0; i < legH; i++) {
            b.put(cx - 3, y0 + i, cz, p.foundation());
            b.put(cx - 2, y0 + i, cz, p.wall());
            b.put(cx + 2, y0 + i, cz, p.wall());
            b.put(cx + 3, y0 + i, cz, p.foundation());
        }
        // An incomplete span: only one shoulder survives.
        b.fill(cx - 3, y0 + legH, cz, cx - 1, y0 + legH, cz, p.accent());
        b.put(cx - 1, y0 + legH + 1, cz, p.accent());
        b.put(cx, y0 + legH + 1, cz, p.accent());
        b.put(cx + 2, y0 + legH, cz, p.light());
    }
}
