package com.sanchous98.eldritchhorror.world.loc.site;

import com.sanchous98.eldritchhorror.world.loc.Location;
import com.sanchous98.eldritchhorror.world.loc.Palette;
import com.sanchous98.eldritchhorror.world.loc.StructureBuilder;
import com.sanchous98.eldritchhorror.world.loc.Tier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;

/**
 * A minor tear in the world: a long, narrow canyon meandering through the terrain, its broken lips
 * fringed with shattered stone and jagged spurs rising like broken teeth, and a great leaning
 * monolith marking its northern end. The air is wrong here and the scar is still, faintly,
 * widening.
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
        return 60; // cull radius — unchanged
    }

    @Override
    public int renderRadius() {
        return LENGTH + 30; // render the built fissure, not the 60 cull radius
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

        // The fissure itself: a narrow, meandering trench cut deep into the terrain.
        fissure(b, rng, p, cx, cz, ground, LENGTH);

        // Jagged broken stone spurs along both lips.
        spurs(b, rng, p, cx + 7, cz, ground);
        spurs(b, rng, p, cx - 7, cz - 8, ground);

        // A leaning monolith anchoring the north end of the scar. Kept inside the prepared
        // rubble field so its base sits on the levelled ground rather than a slope.
        monolith(b, p, cx + 2, cz - FIELD + 4, ground);

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

    /**
     * A narrow, deep, meandering canyon: the centreline wanders along Z, the walls are cut one
     * block out with broken, jagged edges, and scorched rubble lines the floor and rim. Deep at the
     * middle and closing to a hairline crack at the ends, so it reads as a real fault, not a valley.
     */
    private static void fissure(StructureBuilder b, RandomSource rng, Palette p,
                                int cx, int cz, int ground, int length) {
        int wander = 0;
        int prevDepth = 0;
        for (int dz = -length; dz <= length; dz++) {
            int z = cz + dz;
            int t = Math.abs(dz);
            // Meander: a bounded random walk of the centreline.
            wander = Math.clamp(wander + rng.nextInt(3) - 1, -8, 8);
            int mx = cx + wander;
            int halfW = Math.max(0, (length - t) / 12); // 1..2, narrow
            int depth = Math.max(1, (length - t) / 4 + 2); // deep, tapering at the ends
            // Broken edges: jitter each wall out by one block, deterministically per row.
            int left = mx - halfW - rng.nextInt(2);
            int right = mx + halfW + rng.nextInt(2);
            for (int x = left; x <= right; x++) {
                for (int k = 0; k <= depth; k++) {
                    b.put(x, ground - k, z, Blocks.AIR.defaultBlockState());
                }
                // Tainted, scorched rubble floor.
                b.put(x, ground - depth - 1, z, p.rubble());
                if (rng.nextFloat() < 0.5f) {
                    b.put(x, ground - depth, z, p.weathered());
                }
            }
            // Scorched rubble and broken stone along the rim, thicker as the canyon deepens.
            if (depth > prevDepth || rng.nextFloat() < 0.5f) {
                b.put(left - 1, ground, z, p.rubble());
                b.put(right + 1, ground, z, p.rubble());
                if (rng.nextFloat() < 0.4f) {
                    b.put(left - 1, ground + 1, z, p.weathered());
                }
                if (rng.nextFloat() < 0.4f) {
                    b.put(right + 1, ground + 1, z, p.weathered());
                }
            }
            prevDepth = depth;
        }
    }

    /** Jagged spurs of shattered stone leaning out of a lip, rising and falling along the scar. */
    private static void spurs(StructureBuilder b, RandomSource rng, Palette p, int x, int cz,
                              int ground) {
        int lean = 0;
        for (int z = cz - 18; z <= cz + 18; z += 3) {
            if (rng.nextFloat() < 0.35f) {
                continue;
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
        int h = 16;
        // The base is a small scorched mound.
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
