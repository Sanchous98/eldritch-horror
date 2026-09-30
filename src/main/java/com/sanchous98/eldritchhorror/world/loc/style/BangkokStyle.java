package com.sanchous98.eldritchhorror.world.loc.style;

import com.sanchous98.eldritchhorror.world.loc.Palette;
import com.sanchous98.eldritchhorror.world.loc.StructureBuilder;
import com.sanchous98.eldritchhorror.world.loc.StructureBuilder.Doorway;
import com.sanchous98.eldritchhorror.world.loc.StructureBuilder.Side;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Bangkok — Thai: white stucco walls, gold/vermilion temple roofs with steep chofah spires,
 * mosaic trim, canals and spirit houses. Colour is culture, not climate, so the stucco and the
 * gold lead while the region only supplies overgrowth (jungle vines, or kelp on the delta).
 *
 * <p>Landmark: a Thai temple — an ordination hall on a tiered base, a steep stacked roof and a
 * tall golden spire. Street props: spirit houses, lantern posts and guardian statues. Follows
 * {@link TokyoStyle}; shared shapes live in {@link StyleKit}.
 */
public final class BangkokStyle implements CityStyle {

    /** Temple roof tile: golden glazed terracotta, with matching stairs/slabs for pitch. */
    private static BlockState goldTiles() {
        return Materials.glazed(DyeColor.YELLOW);
    }

    private static BlockState goldStairs() {
        return Blocks.CONCRETE_STAIRS.pick(DyeColor.YELLOW).defaultBlockState();
    }

    private static BlockState goldSlab() {
        return Blocks.CONCRETE_SLAB.pick(DyeColor.YELLOW).defaultBlockState();
    }

    @Override
    public String id() {
        return "bangkok";
    }

    @Override
    public Palette palette(int koppenClass, boolean coastal) {
        Palette bio = Palette.fromBiome(koppenClass, coastal);
        return new Palette(
                Blocks.STONE_BRICKS.defaultBlockState(),             // ground: paved temple court
                Blocks.POLISHED_DIORITE.defaultBlockState(),         // foundation: pale plinth
                Materials.whiteConcrete(),                           // wall: white stucco
                Materials.lightGrayConcrete(),                       // weathered stucco
                Blocks.POLISHED_BLACKSTONE.defaultBlockState(),      // accent: lacquer pillars
                goldTiles(),                                         // roof: gold temple tile
                goldStairs(),                                        // roof stairs
                goldSlab(),                                          // roof slabs / eaves
                Materials.stainedPane(DyeColor.LIGHT_BLUE),          // window: mosaic glazing
                Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState(),    // frame: dark timber
                Blocks.CRIMSON_DOOR.defaultBlockState(),             // door: vermilion gate leaf
                Blocks.CRIMSON_FENCE.defaultBlockState(),            // rail
                Blocks.LANTERN.defaultBlockState(),                  // light: hanging lantern
                bio.overgrowth(),
                Blocks.GRAVEL.defaultBlockState());                  // rubble
    }

    @Override
    public void landmark(StructureBuilder b, RandomSource rng, int cx, int cz, int ground, Palette p) {
        // Wat Arun: a very tall, steeply stepped central prang flanked by two smaller prangs,
        // white porcelain stucco banded with gold, rising over a wide paved temple terrace.
        int y0 = ground + 1;
        b.ground(cx - 30, cz - 16, cx + 30, cz + 16, ground, ground, p.foundation());

        // --- central prang ---------------------------------------------------------
        int half = 14;                 // 29 blocks wide at the base
        int y = y0;
        for (int t = 0; t < 5; t++) {
            int h = 12;
            b.room(cx - half, y, cz - half, cx + half, y + h - 1, cz + half);
            // Lacquered corner pillars and gold tier cornice.
            for (int yy = y; yy <= y + h - 1; yy++) {
                b.put(cx - half, yy, cz - half, p.accent());
                b.put(cx + half, yy, cz - half, p.accent());
                b.put(cx - half, yy, cz + half, p.accent());
                b.put(cx + half, yy, cz + half, p.accent());
            }
            // Overhanging gold tier ledge, then the solid tier cap.
            b.fill(cx - half - 1, y + h, cz - half - 1, cx + half + 1, y + h, cz + half + 1, p.roofSlab());
            b.fill(cx - half, y + h, cz - half, cx + half, y + h, cz + half, p.roof());
            // Mosaic niches on every face.
            b.window(cx - half, y + 4, cz, 5, 1, true);
            b.window(cx + half, y + 4, cz, 5, 1, true);
            b.window(cx, y + 4, cz - half, 5, 1, true);
            b.window(cx, y + 4, cz + half, 5, 1, true);
            y += h;
            half -= 2;
        }
        // Steep corn-cob spire: shrinking gold-nibbed rings tapering to a needle.
        int sh = 8;
        int yy = y;
        while (sh > 0) {
            for (int k = 0; k < 4 && sh > 0; k++) {
                b.fill(cx - sh, yy, cz - sh, cx + sh, yy, cz + sh, p.wall());
                b.put(cx - sh, yy, cz - sh, p.roof());
                b.put(cx + sh, yy, cz - sh, p.roof());
                b.put(cx - sh, yy, cz + sh, p.roof());
                b.put(cx + sh, yy, cz + sh, p.roof());
                yy++;
            }
            sh--;
        }
        for (int k = 0; k < 5; k++) {
            b.put(cx, yy, cz, p.wall());
            yy++;
        }
        b.fill(cx - 1, yy, cz - 1, cx + 1, yy, cz + 1, p.roof());
        b.put(cx, yy + 1, cz, p.light());

        // --- two smaller flanking prangs ------------------------------------------
        int[] fxs = {cx - 22, cx + 22};
        for (int s = 0; s < 2; s++) {
            int qx = fxs[s];
            int qh = 6;
            int qy = y0;
            for (int t = 0; t < 3; t++) {
                b.room(qx - qh, qy, cz - qh, qx + qh, qy + 11, cz + qh);
                for (int k = qy; k <= qy + 11; k++) {
                    b.put(qx - qh, k, cz - qh, p.accent());
                    b.put(qx + qh, k, cz - qh, p.accent());
                    b.put(qx - qh, k, cz + qh, p.accent());
                    b.put(qx + qh, k, cz + qh, p.accent());
                }
                b.fill(qx - qh - 1, qy + 12, cz - qh - 1, qx + qh + 1, qy + 12, cz + qh + 1, p.roofSlab());
                b.fill(qx - qh, qy + 12, cz - qh, qx + qh, qy + 12, cz + qh, p.roof());
                qy += 12;
                qh -= 1;
            }
            int qs = 4;
            while (qs > 0) {
                for (int k = 0; k < 3; k++) {
                    b.fill(qx - qs, qy, cz - qs, qx + qs, qy, cz + qs, p.wall());
                    b.put(qx - qs, qy, cz - qs, p.roof());
                    b.put(qx + qs, qy, cz - qs, p.roof());
                    b.put(qx - qs, qy, cz + qs, p.roof());
                    b.put(qx + qs, qy, cz + qs, p.roof());
                    qy++;
                }
                qs--;
            }
            b.put(qx, qy, cz, p.light());
        }
    }

    /**
     * A Thai temple: a 17x11 hall raised on a three-step tiered base, with a vermilion colonnade,
     * mosaic window bands and a steep two-tier golden roof capped by a tall chofah spire.
     */
    private static void temple(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int w = 17;
        int d = 11;
        int x0 = cx - w / 2;
        int x1 = x0 + w - 1;
        int z0 = cz - d / 2;
        int z1 = z0 + d - 1;
        int y0 = ground + 3;   // the hall sits on top of the tiers
        int h = 7;

        // Tiered base: three shrinking slabs of pale stone.
        b.ground(x0 - 3, z0 - 3, x1 + 3, z1 + 3, ground, ground + 2, p.foundation());
        b.ground(x0 - 1, z0 - 1, x1 + 1, z1 + 1, ground, ground + 2, p.wall());

        b.room(x0, y0, z0, x1, y0 + h, z1, new Doorway(Side.S, w / 2), new Doorway(Side.N, w / 2));

        // Vermilion columns down both long sides, carried one course past the eave.
        for (int x = x0 + 1; x <= x1 - 1; x += 3) {
            b.fill(x, y0, z0, x, y0 + h + 1, z0, p.accent());
            b.fill(x, y0, z1, x, y0 + h + 1, z1, p.accent());
        }
        // Mosaic window bands on the flanks.
        for (int x = x0 + 2; x <= x1 - 2; x += 3) {
            b.window(x, y0 + 3, z0, 3, 1, true);
            b.window(x, y0 + 3, z1, 3, 1, true);
        }

        // Steep stacked roof: a broad lower shell, a narrower upper shell, then the golden spire.
        b.pitchedRoof(x0 - 2, z0 - 2, x1 + 2, z1 + 2, y0 + h, 5, 0);
        b.pitchedRoof(x0 + 3, z0 + 1, x1 - 3, z1 - 1, y0 + h + 5, 4, 0);
        b.spire(cx, cz, y0 + h + 9, 16);
    }

    /**
     * A spirit house: a tiny gilded shrine raised on a post so the household spirit sits above the
     * street, with a small pitched roof — the everyday Thai street shrine.
     */
    private static void spiritHouse(StructureBuilder b, int x, int z, int ground, Palette p) {
        b.put(x, ground + 1, z, p.accent());
        b.put(x, ground + 2, z, p.accent());
        BlockState gold = Materials.glazed(DyeColor.YELLOW);
        b.fill(x - 1, ground + 3, z - 1, x + 1, ground + 3, z + 1, gold);
        b.put(x, ground + 4, z, p.wall());
        b.put(x, ground + 5, z, p.wall());
        b.pitchedRoof(x - 1, z - 1, x + 1, z + 1, ground + 5, 2, 0);
        b.put(x, ground + 8, z, p.light());
    }

    @Override
    public void streetProps(StructureBuilder b, RandomSource rng, int cx, int cz,
                            int district, int ground, Palette p) {
        // Guardian statues flanking the temple approach.
        StyleKit.statue(b, cx - 9, cz + 4, ground, p);
        StyleKit.statue(b, cx + 9, cz + 4, ground, p);
        // Spirit houses on the corners of the central district.
        spiritHouse(b, cx - district + 12, cz - district + 12, ground, p);
        spiritHouse(b, cx + district - 12, cz + district - 12, ground, p);
        // Lantern posts down the two main axes, spaced by the deterministic RNG.
        int step = 12 + rng.nextInt(3);
        for (int d = 26; d <= district - 12; d += step) {
            StyleKit.lanternPost(b, cx + d, cz - 3, ground, p);
            StyleKit.lanternPost(b, cx - d, cz + 3, ground, p);
        }
    }

    @Override
    public void flourish(StructureBuilder b, RandomSource rng, int x, int y0, int z,
                         int x1, int y1, int z1, Palette p) {
        // A vermilion eave band and a gold trim course, with a sparse hanging lantern.
        b.fill(x, y1, z, x1, y1, z1, p.accent());
        if (rng.nextFloat() < 0.4F) {
            b.put((x + x1) / 2, y0 + 1, z, p.light());
        }
    }
}
