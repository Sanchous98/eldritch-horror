package com.sanchous98.eldritchhorror.world.loc.site;

import com.sanchous98.eldritchhorror.world.loc.SiteLoot;

import com.sanchous98.eldritchhorror.world.loc.Location;
import com.sanchous98.eldritchhorror.world.loc.Palette;
import com.sanchous98.eldritchhorror.world.loc.StructureBuilder;
import com.sanchous98.eldritchhorror.world.loc.Tier;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The Black Pyramid — the tomb-seat of Nephren-Ka (design/28). A stepped pyramid of blackstone,
 * its square tiers banded so the concentric silhouette reads from directly above, pierced by a
 * single dark entrance that runs a corridor into a deep burial chamber of gold and weeping stone.
 * A paved causeway approaches from the north.
 *
 * <p>The stepped solid is written per chunk-local column (not as one huge box), so the per-chunk
 * cost stays flat and the whole tomb is chunk-clipped like every other site.
 */
public final class BlackPyramid implements Location {

    /** Half-width of the pyramid base (blocks). */
    private static final int BASE = 30;
    /** Half-width of the flat summit. */
    private static final int TOP = 6;
    /** Inward inset, and one block of rise, per tier. */
    private static final int STEP = 4;
    /** Courses of the vertical base wall below the first set-back. */
    private static final int BASE_COURSES = 4;
    /** Y-index of the summit above the first course. */
    private static final int SUMMIT = 9;
    /** Radius of the levelled apron around the pyramid. */
    private static final int FIELD = BASE + 16;
    /** Half-width of the burial chamber. */
    private static final int CHAMBER = 5;

    private final int centerX;
    private final int centerZ;

    /** Constructed at a fixed centre; {@link #build} has no coordinate argument. */
    public BlackPyramid(int x, int z) {
        this.centerX = x;
        this.centerZ = z;
    }

    @Override
    public String id() {
        return "eldritch_horror:site/black_pyramid";
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
        return FIELD + 4; // the levelled apron, not the larger cull radius
    }

    @Override
    public void build(StructureBuilder b) {
        int cx = this.centerX;
        int cz = this.centerZ;
        int ground = baseLevel(b, cx, cz, FIELD);
        Palette p = b.palette();
        int y0 = ground + 1;

        // Level the apron to a single datum.
        level(b, cx, cz, ground, FIELD, p);

        // The stepped black solid, one chunk-local column at a time.
        solid(b, cx, cz, ground);

        // Cut the entrance, corridor and burial chamber out of the solid.
        carve(b, cx, cz, ground);

        // The burial chamber: floor, sarcophagus, pillars and gilded fittings.
        chamber(b, cx, cz, ground);

        // The approach: a paved causeway with flanking obelisks.
        causeway(b, cx, cz, ground);

        b.marker("black_pyramid", cx, y0, cz);
        // Grave goods beside the sarcophagus (burial-chamber interior).
        SiteLoot.fillRoom(b, cx - 4, cz - 4, cx + 4, cz + 4, y0, SiteLoot.RELIC);
    }

    // ------------------------------------------------------------------ pieces

    /**
     * The stepped solid: for each chunk-local land column inside the base, the tier height is
     * derived from its Chebyshev distance to the centre. Tier edges and corners get a distinct
     * cap so the concentric squares are legible from above.
     */
    private static void solid(StructureBuilder b, int cx, int cz, int ground) {
        BlockState body = Blocks.BLACKSTONE.defaultBlockState();
        BlockState cap = Blocks.POLISHED_BLACKSTONE.defaultBlockState();
        BlockState edge = Blocks.CHISELED_POLISHED_BLACKSTONE.defaultBlockState();
        BlockState corner = Blocks.GILDED_BLACKSTONE.defaultBlockState();
        int x0 = b.chunkMinX();
        int z0 = b.chunkMinZ();
        for (int x = x0; x < x0 + 16; x++) {
            int dx = x - cx;
            int adx = Math.abs(dx);
            if (adx > BASE) {
                continue;
            }
            for (int z = z0; z < z0 + 16; z++) {
                int dz = z - cz;
                int adz = Math.abs(dz);
                int d = Math.max(adx, adz);
                if (d > BASE || !b.isLand(x, z)) {
                    continue;
                }
                int top = tierTop(d, ground);
                for (int y = ground; y <= top; y++) {
                    b.put(x, y, z, y == top ? cap : body);
                }
                // Re-cap the top face for legibility: tier boundary, or a corner quoins.
                if (adx == adz && d >= TOP) {
                    b.put(x, top, z, corner);
                } else if ((d - TOP) % STEP == 0) {
                    b.put(x, top, z, edge);
                }
            }
        }
    }

    /** Height (inclusive) of the stepped solid at Chebyshev distance {@code d}. */
    private static int tierTop(int d, int ground) {
        if (d <= TOP) {
            return ground + 1 + SUMMIT;
        }
        int idx = (d - TOP + STEP - 1) / STEP;
        return ground + 1 + SUMMIT - idx;
    }

    /** Carves the entrance, the corridor and the burial chamber out of the solid. */
    private static void carve(StructureBuilder b, int cx, int cz, int ground) {
        BlockState air = Blocks.AIR.defaultBlockState();
        int y0 = ground + 1;
        int x0 = b.chunkMinX();
        int z0 = b.chunkMinZ();
        int chamberTop = y0 + BASE_COURSES;
        for (int x = x0; x < x0 + 16; x++) {
            int dx = x - cx;
            int adx = Math.abs(dx);
            for (int z = z0; z < z0 + 16; z++) {
                int dz = z - cz;
                int adz = Math.abs(dz);
                // Burial chamber: a tall room under the summit.
                if (adx <= CHAMBER && adz <= CHAMBER) {
                    for (int y = y0; y <= chamberTop; y++) {
                        b.put(x, y, z, air);
                    }
                    continue;
                }
                // Corridor: a 3-wide shaft out of the north face.
                if (adx <= 1 && dz < -CHAMBER && dz >= -BASE) {
                    for (int y = y0; y <= y0 + 2; y++) {
                        b.put(x, y, z, air);
                    }
                }
            }
        }
    }

    /** Fits out the burial chamber: a patterned floor, a central sarcophagus and corner pillars. */
    private static void chamber(StructureBuilder b, int cx, int cz, int ground) {
        int y0 = ground + 1;
        BlockState floor = Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState();
        BlockState gold = Blocks.GOLD_BLOCK.defaultBlockState();
        BlockState gilded = Blocks.GILDED_BLACKSTONE.defaultBlockState();
        BlockState weep = Blocks.CRYING_OBSIDIAN.defaultBlockState();
        BlockState dark = Blocks.CONCRETE.pick(DyeColor.BLACK).defaultBlockState();

        // Patterned floor with a gilded ring.
        for (int dx = -CHAMBER; dx <= CHAMBER; dx++) {
            for (int dz = -CHAMBER; dz <= CHAMBER; dz++) {
                boolean ring = Math.abs(dx) == CHAMBER - 1 || Math.abs(dz) == CHAMBER - 1;
                b.put(cx + dx, ground, cz + dz, ring ? gilded : floor);
            }
        }
        // The sarcophagus: a polished slab with a gold head and gilded trim.
        b.fill(cx - 1, y0, cz - 1, cx + 1, y0, cz + 1, Blocks.POLISHED_BLACKSTONE.defaultBlockState());
        b.fill(cx - 1, y0 + 1, cz - 1, cx + 1, y0 + 1, cz + 1, dark);
        b.put(cx, y0 + 1, cz + 2, gilded);
        b.put(cx, y0 + 1, cz - 2, gold);
        // Four corner pillars, weeping at the crown.
        for (int sx = -1; sx <= 1; sx += 2) {
            for (int sz = -1; sz <= 1; sz += 2) {
                int px = cx + sx * (CHAMBER - 1);
                int pz = cz + sz * (CHAMBER - 1);
                for (int y = y0; y <= y0 + 3; y++) {
                    b.put(px, y, pz, y == y0 + 3 ? weep : Blocks.POLISHED_BLACKSTONE.defaultBlockState());
                }
                b.put(px, y0 + 4, pz, gilded);
            }
        }
        // Gilded reliefs on the north and south walls.
        b.fill(cx - 3, y0 + 2, cz - CHAMBER, cx + 3, y0 + 2, cz - CHAMBER, gilded);
        b.fill(cx - 3, y0 + 2, cz + CHAMBER, cx + 3, y0 + 2, cz + CHAMBER, gilded);
    }

    /** A paved approach from the north entrance, flanked by short gilded obelisks. */
    private static void causeway(StructureBuilder b, int cx, int cz, int ground) {
        int zStart = cz - BASE + 1;
        int zEnd = cz - (BASE + 14);
        BlockState pave = Blocks.POLISHED_BLACKSTONE.defaultBlockState();
        BlockState edge = Blocks.GILDED_BLACKSTONE.defaultBlockState();
        for (int z = zEnd; z <= zStart; z++) {
            for (int x = cx - 2; x <= cx + 2; x++) {
                if (!b.isLand(x, z)) {
                    continue;
                }
                b.put(x, ground, z, (x == cx - 2 || x == cx + 2) ? edge : pave);
            }
        }
        obelisk(b, cx - 4, zEnd + 2, ground);
        obelisk(b, cx + 4, zEnd + 2, ground);
    }

    /** A short gilded blackstone needle on a blackstone plinth. */
    private static void obelisk(StructureBuilder b, int x, int z, int ground) {
        if (!b.isLand(x, z)) {
            return;
        }
        int y0 = ground + 1;
        b.fill(x - 1, ground, z - 1, x + 1, ground, z + 1, Blocks.POLISHED_BLACKSTONE.defaultBlockState());
        for (int i = 0; i < 7; i++) {
            b.put(x, y0 + i, z, i == 0
                    ? Blocks.GILDED_BLACKSTONE.defaultBlockState()
                    : Blocks.POLISHED_BLACKSTONE.defaultBlockState());
        }
        b.put(x, y0 + 7, z, Blocks.GOLD_BLOCK.defaultBlockState());
    }

    /** Mean surface over a 5x5 sample of the apron: one datum for the whole tomb. */
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

    /** Cut hills to the datum, fill hollows, and clear vegetation above. Chunk-local, no rng. */
    private static void level(StructureBuilder b, int cx, int cz, int ground, int field, Palette p) {
        BlockState air = Blocks.AIR.defaultBlockState();
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
                if (dx * dx + dz * dz > f2 || !b.isLand(x, z)) {
                    continue;
                }
                int surface = b.groundY(x, z);
                if (surface > ground) {
                    b.fill(x, ground + 1, z, x, surface, z, air);
                } else if (surface < ground) {
                    b.fill(x, surface + 1, z, x, ground, z, p.foundation());
                }
                b.put(x, ground, z, p.ground());
                for (int y = ground + 1; y <= ground + 16; y++) {
                    b.put(x, y, z, air);
                }
            }
        }
    }
}
