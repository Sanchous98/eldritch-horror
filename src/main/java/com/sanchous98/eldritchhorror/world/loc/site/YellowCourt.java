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
 * The Yellow Court — the seat of Hastur (design/28). A broad, pale tiled court open to the sky,
 * ringed by an irregular colonnade of uneven yellow pillars, with a stepped throne dais at its
 * heart and tattered yellow banners hanging from leaning poles. The name is not written here;
 * the court only waits to hear it.
 *
 * <p>Everything is levelled to one datum first, so the pale pavement and the dais step shading
 * read as a deliberate court from directly above rather than a dent in the terrain.
 */
public final class YellowCourt implements Location {

    /** Half-extent of the paved court (blocks). */
    private static final int COURT = 34;
    /** Radius of the levelled ground (the court sits well inside it). */
    private static final int FIELD = COURT + 6;
    /** Half-width of the throne dais. */
    private static final int DAIS = 8;
    /** Number of colonnade pillars around the court. */
    private static final int PILLARS = 20;

    private final int centerX;
    private final int centerZ;

    /** Constructed at a fixed centre; {@link #build} has no coordinate argument. */
    public YellowCourt(int x, int z) {
        this.centerX = x;
        this.centerZ = z;
    }

    @Override
    public String id() {
        return "eldritch_horror:site/yellow_court";
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
        return FIELD + 4; // the levelled court, not the larger cull radius
    }

    @Override
    public void build(StructureBuilder b) {
        int cx = this.centerX;
        int cz = this.centerZ;
        int ground = baseLevel(b, cx, cz, FIELD);
        Palette p = b.palette();
        RandomSource rng = b.rng();
        int y0 = ground + 1;

        // Level the whole footprint so the pale court is a single flat plane.
        level(b, cx, cz, ground, FIELD, p);

        // The court itself: a radial pale tile field with grid lines, clipped to a circle.
        pave(b, cx, cz, ground);

        // An irregular colonnade of uneven yellow pillars around the rim.
        colonnade(b, rng, cx, cz, ground);

        // The stepped throne dais at the centre.
        dais(b, cx, cz, ground);

        // Tattered yellow banners on leaning poles around the court.
        banners(b, rng, cx, cz, ground);

        // Fringe dressing: rubble and cobwebs on the unpaved ground.
        b.scatter(cx - FIELD, cz - FIELD, cx + FIELD, cz + FIELD, y0, y0, p.rubble(), 0.10f);
        b.scatter(cx - FIELD, cz - FIELD, cx + FIELD, cz + FIELD, y0 + 1, y0 + 4,
                Palette.cobweb(), 0.03f);

        b.marker("yellow_court", cx, y0, cz);
    }

    // ------------------------------------------------------------------ pieces

    /** The pale tile pavement: a grid of smooth-sandstone lines over alternating yellow tiles. */
    private static void pave(StructureBuilder b, int cx, int cz, int ground) {
        BlockState conc = Blocks.CONCRETE.pick(DyeColor.YELLOW).defaultBlockState();
        BlockState terra = Blocks.DYED_TERRACOTTA.pick(DyeColor.YELLOW).defaultBlockState();
        BlockState line = Blocks.SMOOTH_SANDSTONE.defaultBlockState();
        BlockState chisel = Blocks.CHISELED_SANDSTONE.defaultBlockState();
        int x0 = b.chunkMinX();
        int z0 = b.chunkMinZ();
        int c2 = COURT * COURT;
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
                int adx = Math.abs(dx);
                int adz = Math.abs(dz);
                BlockState state;
                if (adx % 8 == 0 || adz % 8 == 0) {
                    state = line;                        // plaza grid
                } else if (((dx + dz) & 1) == 0) {
                    state = conc;
                } else if ((adx + adz) % 5 == 0) {
                    state = chisel;                      // sparse brighter tiles
                } else {
                    state = terra;
                }
                b.put(x, ground, z, state);
            }
        }
    }

    /** Uneven yellow pillars around the court — deliberately not a symmetric ring. */
    private static void colonnade(StructureBuilder b, RandomSource rng, int cx, int cz, int ground) {
        int r = COURT - 3;
        for (int i = 0; i < PILLARS; i++) {
            double a = i * 2.0 * Math.PI / PILLARS;
            int x = cx + (int) Math.round(Math.cos(a) * r);
            int z = cz + (int) Math.round(Math.sin(a) * r);
            if (!b.isLand(x, z)) {
                continue; // never plant a pillar over water
            }
            int h = 4 + rng.nextInt(7); // 4..10, so the skyline is irregular
            if (rng.nextFloat() < 0.25f) {
                h = Math.max(2, h - 3); // some are broken stumps
            }
            pillar(b, x, z, ground + 1, h, i);
        }
    }

    /** One pillar: a sandstone base, a yellow shaft, a glazed cap. */
    private static void pillar(StructureBuilder b, int x, int z, int y0, int h, int seed) {
        b.put(x, y0, z, Blocks.SANDSTONE.defaultBlockState());
        BlockState shaft = ((seed & 1) == 0)
                ? Blocks.DYED_TERRACOTTA.pick(DyeColor.YELLOW).defaultBlockState()
                : Blocks.CHISELED_SANDSTONE.defaultBlockState();
        for (int i = 1; i < h; i++) {
            b.put(x, y0 + i, z, shaft);
        }
        b.put(x, y0 + h, z, Blocks.GLAZED_TERRACOTTA.pick(DyeColor.YELLOW).defaultBlockState());
    }

    /** A three-tier yellow dais carrying a pale throne. */
    private static void dais(StructureBuilder b, int cx, int cz, int ground) {
        int y0 = ground + 1;
        BlockState terra = Blocks.DYED_TERRACOTTA.pick(DyeColor.YELLOW).defaultBlockState();
        BlockState smooth = Blocks.SMOOTH_SANDSTONE.defaultBlockState();
        for (int t = 0; t < 3; t++) {
            int h = DAIS - t * 2;
            if (h < 1) {
                break;
            }
            b.fill(cx - h, y0 + t, cz - h, cx + h, y0 + t, cz + h, ((t & 1) == 0) ? terra : smooth);
        }
        int top = y0 + 2;
        // Throne platform and the tall glazed back wall.
        b.fill(cx - 3, top + 1, cz - 3, cx + 3, top + 1, cz + 3, Blocks.CHISELED_SANDSTONE.defaultBlockState());
        b.fill(cx - 2, top + 2, cz - 3, cx + 2, top + 3, cz - 3,
                Blocks.GLAZED_TERRACOTTA.pick(DyeColor.YELLOW).defaultBlockState());
        // The seat and its armrests, in tattered yellow wool.
        b.fill(cx - 1, top + 2, cz - 2, cx + 1, top + 2, cz + 1, Blocks.WOOL.pick(DyeColor.YELLOW).defaultBlockState());
        b.put(cx - 2, top + 2, cz - 2, Blocks.GLAZED_TERRACOTTA.pick(DyeColor.YELLOW).defaultBlockState());
        b.put(cx + 2, top + 2, cz - 2, Blocks.GLAZED_TERRACOTTA.pick(DyeColor.YELLOW).defaultBlockState());
    }

    /** Tattered yellow banners hanging from leaning sandstone poles. */
    private static void banners(StructureBuilder b, RandomSource rng, int cx, int cz, int ground) {
        int y0 = ground + 1;
        int r = COURT - 9;
        for (int i = 0; i < 8; i++) {
            double a = i * Math.PI / 4.0 + 0.2;
            int x = cx + (int) Math.round(Math.cos(a) * r);
            int z = cz + (int) Math.round(Math.sin(a) * r);
            if (!b.isLand(x, z)) {
                continue;
            }
            int pole = 7 + rng.nextInt(4); // 7..10
            for (int k = 0; k < pole; k++) {
                b.put(x, y0 + k, z, Blocks.SANDSTONE.defaultBlockState());
            }
            // The cloth hangs from just below the top and is ragged: strip length varies and a
            // second, shorter strip tears away on some banners.
            int len = 3 + rng.nextInt(4);
            int start = pole - len - 1;
            int top = y0 + pole - 1;
            // Crossbar: the banner is visibly lashed to the pole, not hanging in the air.
            b.put(x + 1, top, z, Blocks.WOOL.pick(DyeColor.YELLOW).defaultBlockState());
            if (rng.nextFloat() < 0.4f) {
                b.put(x + 2, top, z, Blocks.WOOL.pick(DyeColor.YELLOW).defaultBlockState());
            }
            for (int k = 0; k < len; k++) {
                int yy = y0 + start + k;
                b.put(x + 1, yy, z, Blocks.WOOL.pick(DyeColor.YELLOW).defaultBlockState());
                if (rng.nextFloat() < 0.4f) {
                    b.put(x + 2, yy, z, Blocks.WOOL.pick(DyeColor.YELLOW).defaultBlockState());
                }
            }
        }
    }

    /** Mean surface over a 5x5 sample of the footprint: one datum for the whole court. */
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
                for (int y = ground + 1; y <= ground + 14; y++) {
                    b.put(x, y, z, air);
                }
            }
        }
    }
}
