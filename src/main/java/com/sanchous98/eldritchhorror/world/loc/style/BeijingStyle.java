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
 * <p>Landmark: the Hall of Supreme Harmony — a long, low imperial hall under a wide, overhanging
 * double-eaved golden roof on a stepped white terrace — fronted by a red gate tower. Street props:
 * stone guardian lions, lantern posts, and a drum/bell tower. Follows {@link TokyoStyle}; shared
 * shapes live in {@link StyleKit}.
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
                bio.overgrowth(),
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
        // The imperial axis: the broad Hall of Supreme Harmony behind (north), fronted by a tall
        // red gate tower (south). Broad double golden eaves over a raised stepped white terrace,
        // scaled up so the hall crest and the tower clearly dominate the skyline.
        BlockState air = Blocks.AIR.defaultBlockState();

        // ---- Hall of Supreme Harmony ----------------------------------------------------------
        int czHall = cz - 8;
        int w = 57;
        int d = 31;
        int h = 26;
        int x0 = cx - w / 2;
        int x1 = x0 + w - 1;
        int z0 = czHall - d / 2;
        int z1 = z0 + d - 1;

        // White marble terrace: four shrinking courses rising to the hall floor.
        for (int s = 0; s < 4; s++) {
            int m = 8 - s * 2;
            b.ground(x0 - m, z0 - m, x1 + m, z1 + m,
                    ground + s * 4, ground + s * 4 + 3, p.foundation());
        }
        int y0 = ground + 16;

        // Hall body, hollowed, with doors on the south (front) and north (rear) faces.
        b.room(x0, y0, z0, x1, y0 + h, z1, new Doorway(Side.S, w / 2), new Doorway(Side.N, w / 2));

        // Vermilion walls: red on every face, leaving the gold eave course showing.
        for (int y = y0 + 1; y <= y0 + h - 1; y++) {
            for (int x = x0; x <= x1; x++) {
                b.put(x, y, z0, p.accent());
                b.put(x, y, z1, p.accent());
            }
            for (int z = z0 + 1; z <= z1 - 1; z++) {
                b.put(x0, y, z, p.accent());
                b.put(x1, y, z, p.accent());
            }
        }
        // Re-open the doors through the new red skin.
        b.put(cx, y0 + 1, z0, air);
        b.put(cx, y0 + 2, z0, air);
        b.put(cx, y0 + 1, z1, air);
        b.put(cx, y0 + 2, z1, air);

        // Tall dark-latticed window bays along the flanks and the short ends, in two bands.
        for (int x = x0 + 6; x <= x1 - 6; x += 5) {
            b.window(x, y0 + 5, z0, 9, 1, true);
            b.window(x, y0 + 5, z1, 9, 1, true);
            b.window(x, y0 + 17, z0, 6, 1, true);
            b.window(x, y0 + 17, z1, 6, 1, true);
        }
        for (int z = z0 + 6; z <= z1 - 6; z += 5) {
            b.window(x0, y0 + 5, z, 9, 1, true);
            b.window(x1, y0 + 5, z, 9, 1, true);
            b.window(x0, y0 + 17, z, 6, 1, true);
            b.window(x1, y0 + 17, z, 6, 1, true);
        }

        // Wide lower eave: a broad golden hip roof overhanging four blocks, ridge along X.
        b.pitchedRoof(x0 - 4, z0 - 4, x1 + 4, z1 + 4, y0 + h, 6, 0);

        // Clerestory band standing on the lower eave — the "double" of the double eave.
        int ux0 = x0 + 10;
        int ux1 = x1 - 10;
        int uz0 = z0 + 4;
        int uz1 = z1 - 4;
        for (int y = y0 + h + 7; y <= y0 + h + 9; y++) {
            for (int x = ux0; x <= ux1; x++) {
                b.put(x, y, uz0, p.accent());
                b.put(x, y, uz1, p.accent());
            }
            for (int z = uz0 + 1; z <= uz1 - 1; z++) {
                b.put(ux0, y, z, p.accent());
                b.put(ux1, y, z, p.accent());
            }
        }
        for (int x = ux0 + 4; x <= ux1 - 4; x += 5) {
            b.window(x, y0 + h + 8, uz0, 2, 1, true);
            b.window(x, y0 + h + 8, uz1, 2, 1, true);
        }

        // Upper eave: a narrower golden hip roof, capped by a gold ridge crest and a lantern.
        b.pitchedRoof(ux0 - 5, uz0 - 5, ux1 + 5, uz1 + 5, y0 + h + 10, 6, 0);
        b.fill(cx - 3, y0 + h + 17, czHall, cx + 3, y0 + h + 17, czHall, p.roof());
        b.put(cx, y0 + h + 18, czHall, p.light());

        // ---- Red gate tower: a tall, tiered watchtower with three golden roofs giving the
        // composition a strong vertical on the palace axis. ------------------------------------
        int gt = cz + 24;
        int gy0 = ground + 2;
        b.ground(cx - 10, gt - 7, cx + 10, gt + 7, ground, ground + 1, p.foundation());

        // Tier 1 — wide base storey.
        int t1h = 16;
        b.room(cx - 8, gy0, gt - 4, cx + 8, gy0 + t1h, gt + 4,
                new Doorway(Side.S, 17), new Doorway(Side.N, 17));
        for (int y = gy0; y <= gy0 + t1h; y++) {
            b.put(cx - 8, y, gt - 4, p.accent());
            b.put(cx + 8, y, gt - 4, p.accent());
            b.put(cx - 8, y, gt + 4, p.accent());
            b.put(cx + 8, y, gt + 4, p.accent());
        }
        for (int x = cx - 6; x <= cx + 6; x += 4) {
            b.window(x, gy0 + 4, gt - 4, 9, 1, true);
            b.window(x, gy0 + 4, gt + 4, 9, 1, true);
        }
        b.pitchedRoof(cx - 10, gt - 6, cx + 10, gt + 6, gy0 + t1h, 5, 0);

        // Tier 2.
        int ty2 = gy0 + t1h + 6;
        int t2h = 15;
        // Carry the gate axis up: an upper doorway on each gallery so the tower is not sealed.
        b.room(cx - 6, ty2, gt - 3, cx + 6, ty2 + t2h, gt + 3, new Doorway(Side.S, 6));
        for (int y = ty2; y <= ty2 + t2h; y++) {
            b.put(cx - 6, y, gt - 3, p.accent());
            b.put(cx + 6, y, gt - 3, p.accent());
            b.put(cx - 6, y, gt + 3, p.accent());
            b.put(cx + 6, y, gt + 3, p.accent());
        }
        for (int x = cx - 4; x <= cx + 4; x += 4) {
            b.window(x, ty2 + 4, gt - 3, 8, 1, true);
            b.window(x, ty2 + 4, gt + 3, 8, 1, true);
        }
        b.pitchedRoof(cx - 8, gt - 5, cx + 8, gt + 5, ty2 + t2h, 5, 0);

        // Tier 3 — the crowning pavilion.
        int ty3 = ty2 + t2h + 5;
        int t3h = 14;
        b.room(cx - 4, ty3, gt - 2, cx + 4, ty3 + t3h, gt + 2, new Doorway(Side.S, 4));
        for (int y = ty3; y <= ty3 + t3h; y++) {
            b.put(cx - 4, y, gt - 2, p.accent());
            b.put(cx + 4, y, gt - 2, p.accent());
            b.put(cx - 4, y, gt + 2, p.accent());
            b.put(cx + 4, y, gt + 2, p.accent());
        }
        b.pitchedRoof(cx - 6, gt - 4, cx + 6, gt + 4, ty3 + t3h, 5, 0);
        b.put(cx, ty3 + t3h + 6, gt, p.light());
    }

    /**
     * The Hall of Supreme Harmony: a long, low imperial hall — vermilion walls under a wide,
     * overhanging double-eaved golden roof — raised on a stepped white terrace. Reads broad on the
     * skyline (about 65 blocks wide, 33 tall) rather than tall and narrow, as the imperial axis
     * demands.
     */
    private static void imperialHall(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int w = 57;
        int d = 31;
        int h = 14;
        int x0 = cx - w / 2;          // cx - 28
        int x1 = x0 + w - 1;          // cx + 28
        int z0 = cz - d / 2;          // cz - 15
        int z1 = z0 + d - 1;          // cz + 15
        int y0 = ground + 3;
        BlockState air = Blocks.AIR.defaultBlockState();

        // White marble terrace: three solid courses plus a stepped south approach.
        b.ground(x0 - 4, z0 - 4, x1 + 4, z1 + 4, ground, ground + 2, p.foundation());
        for (int s = 0; s < 3; s++) {
            b.fill(x0 + 10, ground, z1 + 5 + s, x1 - 10, ground + 2 - s, z1 + 5 + s,
                    p.foundation());
        }

        // Hall body, hollowed, with doors on the south (front) and north (rear) faces.
        b.room(x0, y0, z0, x1, y0 + h, z1, new Doorway(Side.S, w / 2), new Doorway(Side.N, w / 2));

        // Vermilion walls: red on every face, leaving the gold eave course showing.
        for (int y = y0 + 1; y <= y0 + h - 1; y++) {
            for (int x = x0; x <= x1; x++) {
                b.put(x, y, z0, p.accent());
                b.put(x, y, z1, p.accent());
            }
            for (int z = z0 + 1; z <= z1 - 1; z++) {
                b.put(x0, y, z, p.accent());
                b.put(x1, y, z, p.accent());
            }
        }
        // Re-open the doors through the new red skin.
        b.put(cx, y0 + 1, z0, air);
        b.put(cx, y0 + 2, z0, air);
        b.put(cx, y0 + 1, z1, air);
        b.put(cx, y0 + 2, z1, air);

        // Tall dark-latticed window bays along the flanks and the short ends.
        for (int x = x0 + 6; x <= x1 - 6; x += 5) {
            b.window(x, y0 + 4, z0, 7, 1, true);
            b.window(x, y0 + 4, z1, 7, 1, true);
        }
        for (int z = z0 + 6; z <= z1 - 6; z += 5) {
            b.window(x0, y0 + 4, z, 7, 1, true);
            b.window(x1, y0 + 4, z, 7, 1, true);
        }

        // Wide lower eave: a broad golden hip roof overhanging four blocks, ridge along X.
        b.pitchedRoof(x0 - 4, z0 - 4, x1 + 4, z1 + 4, y0 + h, 5, 0);

        // Clerestory band standing on the lower eave — the "double" of the double eave.
        int ux0 = x0 + 10;
        int ux1 = x1 - 10;
        int uz0 = z0 + 4;
        int uz1 = z1 - 4;
        for (int y = y0 + h + 6; y <= y0 + h + 8; y++) {
            for (int x = ux0; x <= ux1; x++) {
                b.put(x, y, uz0, p.accent());
                b.put(x, y, uz1, p.accent());
            }
            for (int z = uz0 + 1; z <= uz1 - 1; z++) {
                b.put(ux0, y, z, p.accent());
                b.put(ux1, y, z, p.accent());
            }
        }
        for (int x = ux0 + 4; x <= ux1 - 4; x += 5) {
            b.window(x, y0 + h + 7, uz0, 2, 1, true);
            b.window(x, y0 + h + 7, uz1, 2, 1, true);
        }

        // Upper eave: a narrower golden hip roof, capped by a gold ridge crest and a lantern.
        b.pitchedRoof(ux0 - 5, uz0 - 5, ux1 + 5, uz1 + 5, y0 + h + 9, 5, 0);
        b.fill(cx - 3, y0 + h + 15, cz, cx + 3, y0 + h + 15, cz, p.roof());
        b.put(cx, y0 + h + 16, cz, p.light());
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
        b.put(cx, uy + 6, cz, p.light());   // on the roof apex, not floating above it
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
