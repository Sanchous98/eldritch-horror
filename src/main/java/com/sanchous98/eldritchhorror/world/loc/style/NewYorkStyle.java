package com.sanchous98.eldritchhorror.world.loc.style;

import com.sanchous98.eldritchhorror.world.loc.Palette;
import com.sanchous98.eldritchhorror.world.loc.StructureBuilder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Blocks;

/**
 * New York — cultural city style. Brownstone and red-brick rows with cast-iron trim, iron fire
 * escapes zig-zagging down the facades, gaslit lamp posts, a stone statue on the plaza, and water
 * towers perched on the roofs.
 *
 * <p>Landmark: a stepped Art-Deco skyscraper (Empire-State-like) — stacked {@code room} boxes
 * pulled in as setbacks, a limestone crown of {@code crenellations} and a dominating {@code spire}.
 * Shared helpers live in {@link StyleKit}.
 */
public final class NewYorkStyle implements CityStyle {

    @Override
    public String id() {
        return "newyork";
    }

    @Override
    public Palette palette(int koppenClass, boolean coastal) {
        // Culture leads: brick and brownstone under a limestone Deco crown, dark cast-iron trim.
        // Climate only nudges the overgrowth (wetter → vines); the coast swaps it for kelp.
        Palette bio = Palette.fromBiome(koppenClass, coastal);
        return new Palette(
                Blocks.STONE_BRICKS.defaultBlockState(),            // ground: stone sidewalk
                Blocks.COBBLESTONE.defaultBlockState(),             // foundation: granite base course
                Blocks.BRICKS.defaultBlockState(),                  // wall: red-brick row
                Materials.terracotta(DyeColor.BROWN),               // weathered: brownstone
                Blocks.POLISHED_BLACKSTONE.defaultBlockState(),     // accent: dark cast-iron trim
                Blocks.SMOOTH_QUARTZ.defaultBlockState(),           // roof: limestone Deco crown
                Blocks.SMOOTH_QUARTZ_STAIRS.defaultBlockState(),    // roof stairs
                Blocks.SMOOTH_QUARTZ_SLAB.defaultBlockState(),      // roof slabs / cornices
                Blocks.GLASS_PANE.defaultBlockState(),              // window
                Blocks.IRON_TRAPDOOR.defaultBlockState(),           // frame: iron mullions
                Blocks.DARK_OAK_DOOR.defaultBlockState(),           // door
                Blocks.IRON_BARS.defaultBlockState(),               // rail: fire-escape rails
                Blocks.LANTERN.defaultBlockState(),                 // light: gas lamp
                coastal ? Blocks.KELP.defaultBlockState() : bio.overgrowth(),
                Blocks.GRAVEL.defaultBlockState());                 // rubble
    }

    @Override
    public void landmark(StructureBuilder b, RandomSource rng, int cx, int cz, int ground, Palette p) {
        skyscraper(b, cx, cz, ground, p);
    }

    /** A stepped Deco tower: widening base, three setback tiers, a crenellated crown and a spire. */
    private static void skyscraper(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int y0 = ground + 1;
        // tier: {halfWidth, height}
        int[][] tiers = {{8, 14}, {6, 10}, {4, 9}, {2, 8}};
        int y = y0;
        for (int[] t : tiers) {
            int r = t[0];
            int top = y + t[1] - 1;
            b.room(cx - r, y, cz - r, cx + r, top, cz + r);
            cornerPilasters(b, cx, cz, r, y, top, p);
            windowBands(b, cx, cz, r, y, top, p);
            // Cornice / setback ledge in the limestone crown colour.
            b.fill(cx - r, top, cz - r, cx + r, top, cz + r, p.roofSlab());
            y = top + 1;
        }
        int crown = y;
        b.crenellations(cx - 2, cz - 2, cx + 2, cz + 2, crown);
        b.spire(cx, cz, crown + 2, 12);
        b.fill(cx - 2, crown - 1, cz - 2, cx + 2, crown - 1, cz + 2, p.roof());
    }

    /** Dark-iron vertical piers at the four corners of a tier. */
    private static void cornerPilasters(StructureBuilder b, int cx, int cz, int r, int y0, int y1,
                                        Palette p) {
        for (int y = y0; y <= y1; y++) {
            b.put(cx - r, y, cz - r, p.accent());
            b.put(cx + r, y, cz - r, p.accent());
            b.put(cx - r, y, cz + r, p.accent());
            b.put(cx + r, y, cz + r, p.accent());
        }
    }

    /** Tall narrow glazing every 4 blocks around a tier. */
    private static void windowBands(StructureBuilder b, int cx, int cz, int r, int y0, int y1,
                                    Palette p) {
        for (int y = y0 + 3; y <= y1 - 2; y += 4) {
            for (int d = -r + 2; d <= r - 2; d += 3) {
                b.window(cx + d, y, cz - r, 3, 1, true);
                b.window(cx + d, y, cz + r, 3, 1, true);
                b.window(cx - r, y, cz + d, 3, 1, true);
                b.window(cx + r, y, cz + d, 3, 1, true);
            }
        }
    }

    @Override
    public void flourish(StructureBuilder b, RandomSource rng, int x, int y0, int z,
                         int x1, int y1, int z1, Palette p) {
        // Iron fire escape: a zig-zag of landings and ladders down the min-Z facade.
        fireEscape(b, rng, x, x1, z, y0, y1, p);
        // A wooden water tower on the roof, only on some buildings.
        if (rng.nextInt(3) == 0 && x1 - x >= 3 && z1 - z >= 3) {
            waterTower(b, x + 1, z + 1, y1 + 1, p);
        }
    }

    /** Alternating iron landings joined by vertical ladders, zig-zagging down the facade. */
    private static void fireEscape(StructureBuilder b, RandomSource rng, int x, int x1, int z,
                                   int y0, int y1, Palette p) {
        if (x1 - x < 2) {
            return;
        }
        int len = Math.min(4, x1 - x);
        int side = rng.nextBoolean() ? -1 : 1;
        boolean flip = false;
        for (int y = y0 + 4; y <= y1; y += 4) {
            int lx0 = side < 0 ? x : x1 - len;
            int lx1 = lx0 + len;
            b.fill(lx0, y, z, lx1, y, z, p.rail());          // landing grating
            b.fill(lx0, y + 1, z, lx1, y + 1, z, p.frame()); // railing
            // A short ladder run up to the next landing, alternating ends.
            int ladderX = flip ? lx1 : lx0;
            b.put(ladderX, y + 2, z, Blocks.LADDER.defaultBlockState());
            if (y + 3 <= y1) {
                b.put(ladderX, y + 3, z, Blocks.LADDER.defaultBlockState());
            }
            flip = !flip;
        }
    }

    /** A small plank tank on legs, capped with a conical roof. */
    private static void waterTower(StructureBuilder b, int x, int z, int baseY, Palette p) {
        b.fill(x, baseY, z, x + 2, baseY + 1, z + 2, p.accent());   // legs / frame
        b.fill(x, baseY + 2, z, x + 2, baseY + 4, z + 2, p.wall()); // tank
        b.fill(x, baseY + 5, z, x + 2, baseY + 5, z + 2, p.roofSlab());
        b.put(x + 1, baseY + 6, z + 1, p.roof());
    }

    @Override
    public void streetProps(StructureBuilder b, RandomSource rng, int cx, int cz,
                            int district, int ground, Palette p) {
        // Gaslit lamp posts down the avenues (the grid — NY is not a winding medieval town).
        for (int d = 24; d <= district - 10; d += 12) {
            StyleKit.lanternPost(b, cx + d, cz + 3, ground, p);
            StyleKit.lanternPost(b, cx - d, cz - 3, ground, p);
            StyleKit.lanternPost(b, cx + 3, cz + d, ground, p);
            StyleKit.lanternPost(b, cx - 3, cz - d, ground, p);
        }
        // A stone statue at the plaza and one down the main avenue.
        StyleKit.statue(b, cx, cz + district / 3, ground, p);
        StyleKit.statue(b, cx - district / 3, cz, ground, p);
    }
}
