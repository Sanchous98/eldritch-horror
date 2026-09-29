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
 * Beijing — imperial Chinese: grey brick siheyuan walls with vermilion beams and pillars, grey
 * stone paving, and yellow/gold glazed-tile roofs. Colour is culture, not climate, so the shell
 * keeps the region's overgrowth while the imperial reds and golds lead.
 *
 * <p>Landmark: a raised palace hall on a stone terrace with a tiered golden roof, a tall gate
 * tower, and a gold-capped pagoda on the sight line. Street props: stone guardian lions, lantern
 * posts, and a drum/bell tower. Follows {@link TokyoStyle}; shared shapes live in {@link StyleKit}.
 */
public final class BeijingStyle implements CityStyle {

    /** Imperial roof tile: golden glazed terracotta, with matching stairs/slabs for pitch. */
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
        return "beijing";
    }

    @Override
    public Palette palette(int koppenClass, boolean coastal) {
        Palette bio = Palette.fromBiome(koppenClass, coastal);
        return new Palette(
                Blocks.POLISHED_ANDESITE.defaultBlockState(),        // ground: grey stone paving
                Blocks.STONE_BRICKS.defaultBlockState(),             // foundation: grey brick plinth
                Blocks.STONE_BRICKS.defaultBlockState(),             // wall: grey brick (siheyuan)
                Blocks.CRACKED_STONE_BRICKS.defaultBlockState(),     // weathered brick
                Materials.redTerracotta(),                           // accent: imperial red beams
                goldTiles(),                                         // roof: golden glazed tiles
                goldStairs(),                                        // roof stairs
                goldSlab(),                                          // roof slabs / eaves
                Blocks.GLASS_PANE.defaultBlockState(),               // window
                Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState(),    // frame: dark timber lattice
                Blocks.CRIMSON_DOOR.defaultBlockState(),             // door: red gate leaf
                Blocks.CRIMSON_FENCE.defaultBlockState(),            // rail
                Blocks.LANTERN.defaultBlockState(),                  // light: paper/stone lantern
                coastal ? Blocks.KELP.defaultBlockState() : bio.overgrowth(),
                Blocks.GRAVEL.defaultBlockState());                  // rubble
    }

    /** The city palette with every roof field forced to gold — used for the pagoda. */
    private static Palette goldRoofed(Palette p) {
        return new Palette(p.ground(), p.foundation(), p.wall(), p.weathered(),
                Materials.glazed(DyeColor.YELLOW),   // accent: gold glazed
                goldTiles(), goldStairs(), goldSlab(),
                p.window(), p.frame(), p.door(), p.rail(), p.light(),
                p.overgrowth(), p.rubble());
    }

    @Override
    public void landmark(StructureBuilder b, RandomSource rng, int cx, int cz, int ground, Palette p) {
        // The central axis: gate tower (front), palace hall (behind), pagoda (off the shoulder).
        gateTower(b, cx, cz + 10, ground, p);
        palaceHall(b, cx, cz - 16, ground, p);
        StyleKit.pagoda(b, cx + 26, cz - 26, ground + 1, 5, goldRoofed(p));
    }

    /** A tall gate tower with a lower and an upper golden roof — the palace's outer gate. */
    private static void gateTower(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int w = 13;
        int d = 5;
        int h = 12;
        int x0 = cx - w / 2;
        int x1 = x0 + w - 1;
        int z0 = cz - d / 2;
        int z1 = z0 + d - 1;
        int y0 = ground + 1;

        b.ground(x0 - 1, z0 - 1, x1 + 1, z1 + 1, ground, ground, p.foundation());
        b.room(x0, y0, z0, x1, y0 + h, z1, new Doorway(Side.S, w / 2), new Doorway(Side.N, w / 2));

        // Vermilion corner pillars, carried through the roofline.
        for (int y = y0; y <= y0 + h + 6; y++) {
            b.put(x0, y, z0, p.accent());
            b.put(x1, y, z0, p.accent());
            b.put(x0, y, z1, p.accent());
            b.put(x1, y, z1, p.accent());
        }
        // Lower, wide golden roof.
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, y0 + h, 4, 0);
        // Upper pavilion with its own smaller roof.
        int uy = y0 + h + 5;
        b.room(cx - 2, uy, cz - 1, cx + 2, uy + 3, cz + 1);
        b.pitchedRoof(cx - 3, cz - 2, cx + 3, cz + 2, uy + 3, 3, 0);
        b.window(cx - 2, uy + 1, cz - 1, 2, 1, true);
        b.window(cx + 2, uy + 1, cz - 1, 2, 1, true);
        b.put(cx, uy + 7, cz, p.light());
    }

    /** A wide palace hall on a raised stone terrace, with a tiered golden hipped roof. */
    private static void palaceHall(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int w = 17;
        int d = 11;
        int h = 7;
        int x0 = cx - w / 2;
        int x1 = x0 + w - 1;
        int z0 = cz - d / 2;
        int z1 = z0 + d - 1;
        int y0 = ground + 3;

        // Raised stone terrace and its front steps.
        b.ground(x0 - 3, z0 - 3, x1 + 3, z1 + 3, ground, ground + 2, p.foundation());
        for (int s = 0; s < 3; s++) {
            b.fill(x0, ground + s, z1 + 3 - s, x1, ground + s, z1 + 3 - s, p.foundation());
        }

        b.room(x0, y0, z0, x1, y0 + h, z1, new Doorway(Side.S, w / 2));
        // Red front colonnade.
        for (int x = x0; x <= x1; x += 4) {
            b.fill(x, y0, z1, x, y0 + h, z1, p.accent());
        }
        for (int x = x0 + 2; x <= x1 - 2; x += 3) {
            b.window(x, y0 + 3, z0, 3, 1, true);
        }
        // Tiered roof: a broad lower shell, a narrower upper shell, a gold crest.
        b.pitchedRoof(x0 - 2, z0 - 2, x1 + 2, z1 + 2, y0 + h, 4, 0);
        b.pitchedRoof(x0 + 3, z0 + 1, x1 - 3, z1 - 1, y0 + h + 4, 3, 0);
        b.put(cx, y0 + h + 7, cz, p.light());
    }

    /** A grey stone guardian lion on a plinth (siheyuan / gate pair). */
    private static void lion(StructureBuilder b, int x, int z, int ground, Palette p) {
        b.fill(x - 1, ground + 1, z - 1, x + 1, ground + 1, z + 1, p.foundation());
        b.put(x, ground + 2, z, p.wall());
        b.put(x, ground + 3, z, p.wall());
        b.put(x + 1, ground + 2, z, p.wall());
        b.put(x - 1, ground + 3, z, p.wall());
    }

    /** A square drum/bell tower: red posts, a bell under a golden cap. */
    private static void bellTower(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int r = 3;
        int h = 9;
        int y0 = ground + 1;
        for (int y = y0; y <= y0 + h; y++) {
            b.put(cx - r, y, cz - r, p.accent());
            b.put(cx + r, y, cz - r, p.accent());
            b.put(cx - r, y, cz + r, p.accent());
            b.put(cx + r, y, cz + r, p.accent());
        }
        b.ground(cx - r, cz - r, cx + r, cz + r, ground, ground + 1, p.foundation());
        b.room(cx - r + 1, y0, cz - r + 1, cx + r - 1, y0 + h - 3, cz + r - 1);
        b.put(cx, y0 + h - 4, cz, Blocks.BELL.defaultBlockState());
        b.pitchedRoof(cx - r - 1, cz - r - 1, cx + r + 1, cz + r + 1, y0 + h - 2, 3, 0);
        b.put(cx, y0 + h + 2, cz, p.light());
    }

    @Override
    public void streetProps(StructureBuilder b, RandomSource rng, int cx, int cz,
                            int district, int ground, Palette p) {
        // A pair of guardian lions flanking the central gate.
        int gap = 5;
        lion(b, cx - gap, cz + gap, ground, p);
        lion(b, cx + gap, cz + gap, ground, p);
        // The drum/bell tower on the axis.
        bellTower(b, cx, cz + district - 24, ground, p);
        // Lantern posts down the two main axes, jittered by the deterministic RNG.
        int step = 12 + rng.nextInt(3);
        for (int d = 28; d <= district - 12; d += step) {
            StyleKit.lanternPost(b, cx + d, cz - 3, ground, p);
            StyleKit.lanternPost(b, cx - d, cz + 3, ground, p);
        }
    }

    @Override
    public void flourish(StructureBuilder b, RandomSource rng, int x, int y0, int z,
                         int x1, int y1, int z1, Palette p) {
        // Vermilion beam band under the eaves, and a sparse lantern at the street door.
        b.fill(x, y1, z, x1, y1, z1, p.accent());
        if (rng.nextFloat() < 0.5F) {
            b.put((x + x1) / 2, y0 + 1, z, p.light());
        }
    }
}
