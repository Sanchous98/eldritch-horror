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
                bio.overgrowth(),
                Blocks.GRAVEL.defaultBlockState());                 // rubble
    }

    @Override
    public void landmark(StructureBuilder b, RandomSource rng, int cx, int cz, int ground, Palette p) {
        // The Statue of Liberty: a broad stone plaza, an arcaded pedestal, then a tapering
        // robed figure ~90 blocks tall with a raised torch arm and a spiked crown.
        int y0 = ground + 1;

        // --- Plaza: a wide limestone apron ringed by a low kerb. ---
        b.ground(cx - 16, cz - 16, cx + 16, cz + 16, ground, ground, p.ground());
        b.walls(cx - 16, y0, cz - 16, cx + 16, y0, cz + 16, p.foundation());

        // --- Pedestal tier 1: a broad stepped plinth (21 wide). ---
        b.fill(cx - 10, y0, cz - 10, cx + 10, y0 + 6, cz + 10, p.foundation());
        b.fill(cx - 9, y0 + 7, cz - 9, cx + 9, y0 + 7, cz + 9, p.foundation());

        // --- Pedestal tier 2: the main shaft, iron corner piers and tall glazing (15 wide). ---
        int t2 = y0 + 8;
        b.fill(cx - 7, t2, cz - 7, cx + 7, t2 + 9, cz + 7, p.wall());
        cornerPilasters(b, cx, cz, 7, t2, t2 + 9, p);
        windowBands(b, cx, cz, 7, t2, t2 + 9, p);
        b.fill(cx - 8, t2 + 10, cz - 8, cx + 8, t2 + 10, cz + 8, p.roofSlab());

        // --- Pedestal tier 3: an open colonnade under a heavy cap (11 wide). ---
        int t3 = t2 + 11;
        b.room(cx - 5, t3, cz - 5, cx + 5, t3 + 8, cz + 5);
        for (int y = t3; y <= t3 + 8; y++) {
            for (int d = -5; d <= 5; d += 2) {
                b.put(cx + d, y, cz - 5, p.accent());
                b.put(cx + d, y, cz + 5, p.accent());
                b.put(cx - 5, y, cz + d, p.accent());
                b.put(cx + 5, y, cz + d, p.accent());
            }
        }
        b.fill(cx - 6, t3 + 9, cz - 6, cx + 6, t3 + 9, cz + 6, p.foundation());
        b.fill(cx - 4, t3 + 10, cz - 4, cx + 4, t3 + 10, cz + 4, p.foundation());
        int figY = t3 + 11;                                    // the figure's feet

        // --- The robe: an 11-wide hem tapering to 5-wide shoulders. ---
        int robeH = 34;
        for (int i = 0; i < robeH; i++) {
            int y = figY + i;
            int r = 5 - (i * 3) / (robeH - 1);                 // 5 -> 2 half-width
            for (int dx = -r; dx <= r; dx++) {
                int lim = r - Math.abs(dx);
                for (int dz = -lim; dz <= lim; dz++) {
                    boolean edge = Math.abs(dx) + Math.abs(dz) == r;
                    boolean fold = (((dx + dz) % 3) == 0);
                    b.put(cx + dx, y, cz + dz, edge && fold ? p.weathered() : p.wall());
                }
            }
        }

        // --- Torso and shoulders (7 wide, then 5). ---
        int torsoY = figY + robeH;
        for (int i = 0; i <= 6; i++) {
            int y = torsoY + i;
            int r = (i < 4) ? 3 : 2;
            for (int dx = -r; dx <= r; dx++) {
                int lim = r - Math.abs(dx);
                for (int dz = -lim; dz <= lim; dz++) {
                    b.put(cx + dx, y, cz + dz, p.wall());
                }
            }
        }

        // --- Neck and head. ---
        int neckY = torsoY + 7;
        b.fill(cx, neckY, cz, cx, neckY + 1, cz, p.wall());
        int headY = neckY + 2;
        for (int i = 0; i <= 4; i++) {
            int y = headY + i;
            int r = (i == 4) ? 1 : 2;
            for (int dx = -r; dx <= r; dx++) {
                int lim = r - Math.abs(dx);
                for (int dz = -lim; dz <= lim; dz++) {
                    b.put(cx + dx, y, cz + dz, p.wall());
                }
            }
        }

        // --- The diadem: a 7-point spiked crown. ---
        int crownY = headY + 5;
        for (int dx = -3; dx <= 3; dx++) {
            int lim = 3 - Math.abs(dx);
            for (int dz = -lim; dz <= lim; dz++) {
                if (Math.abs(dx) + Math.abs(dz) == 3) {
                    b.put(cx + dx, crownY, cz + dz, p.accent());
                }
            }
        }
        int[][] spikes = {{0, 0}, {3, 0}, {-3, 0}, {0, 3}, {0, -3}, {2, 2}, {-2, -2}, {2, -2}, {-2, 2}};
        for (int[] s : spikes) {
            b.put(cx + s[0], crownY + 1, cz + s[1], p.accent());
            b.put(cx + s[0], crownY + 2, cz + s[1], p.roofSlab());
        }
        b.put(cx, crownY + 3, cz, p.accent());                 // the tallest central ray

        // --- The raised torch arm (on the east side, well above the head). ---
        int armX = cx + 3;
        for (int y = torsoY + 2; y <= torsoY + 22; y++) {
            b.put(armX, y, cz, p.wall());
        }
        b.put(armX, torsoY + 23, cz, p.accent());              // hand / wrist
        b.fill(armX - 1, torsoY + 24, cz - 1, armX + 1, torsoY + 24, cz + 1, p.roofSlab());
        b.fill(armX - 1, torsoY + 25, cz - 1, armX + 1, torsoY + 25, cz + 1, p.accent());
        b.put(armX, torsoY + 26, cz, p.light());               // the flame
        b.put(armX, torsoY + 27, cz, p.light());

        // --- The other arm, cradling the tablet. ---
        int tabX = cx - 3;
        for (int y = torsoY + 2; y <= torsoY + 8; y++) {
            b.put(tabX, y, cz, p.wall());
        }
        b.fill(tabX - 2, torsoY + 9, cz - 1, tabX, torsoY + 9, cz + 1, p.roofSlab());
        b.fill(tabX - 2, torsoY + 10, cz - 1, tabX, torsoY + 10, cz + 1, p.roof());
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
