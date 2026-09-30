package com.sanchous98.eldritchhorror.world.loc.style;

import com.sanchous98.eldritchhorror.world.loc.Palette;
import com.sanchous98.eldritchhorror.world.loc.StructureBuilder;
import com.sanchous98.eldritchhorror.world.loc.StructureBuilder.Side;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Blocks;

/**
 * Los Angeles — cultural city style. Spanish-colonial / Mission Revival and pastel Art-Deco:
 * sun-bleached tan/cream stucco bungalows under terracotta tile, white revival bell tower,
 * arcaded colonnades, palm-lined boulevards, glassy modern towers.
 *
 * <p>Landmark: a Spanish mission church — white stucco nave with a terracotta pitched roof,
 * an arched arcade along its front and a red-tile bell tower. Street props: palm posts, lamp
 * posts, statues and a big HOLLYWOOD-style sign/arch. Deterministic; uses only
 * {@link StructureBuilder} + {@link Palette} (via {@link StyleKit}/{@link Materials}).
 */
public final class LosAngelesStyle implements CityStyle {

    @Override
    public String id() {
        return "losangeles";
    }

    @Override
    public Palette palette(int koppenClass, boolean coastal) {
        // Culture leads: sun-bleached stucco + terracotta tile, whatever the Köppen class. Climate
        // only supplies the overgrowth; the coast swaps it for kelp.
        Palette bio = Palette.fromBiome(koppenClass, coastal);
        return new Palette(
                Blocks.SMOOTH_SANDSTONE.defaultBlockState(),        // ground: pale boulevard paving
                Blocks.CUT_SANDSTONE.defaultBlockState(),           // foundation: stone base course
                Materials.terracotta(DyeColor.WHITE),               // wall: sun-bleached tan/cream stucco
                Blocks.SMOOTH_SANDSTONE.defaultBlockState(),        // weathered: bleached stucco
                Materials.terracotta(DyeColor.ORANGE),              // accent: terracotta / mission trim
                Blocks.TERRACOTTA.defaultBlockState(),              // roof: terracotta tile bed
                Blocks.BRICK_STAIRS.defaultBlockState(),            // roof stairs
                Blocks.BRICK_SLAB.defaultBlockState(),              // roof slabs / eaves
                Blocks.GLASS_PANE.defaultBlockState(),              // window: mission glazing
                Blocks.ACACIA_TRAPDOOR.defaultBlockState(),         // frame: timber shutters
                Blocks.ACACIA_DOOR.defaultBlockState(),             // door
                Blocks.ACACIA_FENCE.defaultBlockState(),            // rail
                Blocks.LANTERN.defaultBlockState(),                 // light: patio lantern
                coastal ? Blocks.KELP.defaultBlockState() : bio.overgrowth(),
                Blocks.GRAVEL.defaultBlockState());                 // rubble: dry debris
    }

    @Override
    public void landmark(StructureBuilder b, RandomSource rng, int cx, int cz, int ground, Palette p) {
        // Capitol Records round tower: a cylindrical stack of horizontal record-bands on a low
        // circular podium, topped by a glazed penthouse and a slender needle. The stacked,
        // projecting accent bands are the silhouette cue — a pile of records.
        int g = ground;

        // Boulevard plaza.
        b.ground(cx - 14, cz - 14, cx + 14, cz + 14, g, g, p.ground());

        // Circular podium drum: radius 10, five high.
        int podR = 10;
        for (int y = g + 1; y <= g + 5; y++) {
            for (int dx = -podR; dx <= podR; dx++) {
                for (int dz = -podR; dz <= podR; dz++) {
                    int d2 = dx * dx + dz * dz;
                    if (d2 > podR * podR) {
                        continue;
                    }
                    boolean rim = d2 > (podR - 2) * (podR - 2);
                    b.put(cx + dx, y, cz + dz, rim ? p.foundation() : p.wall());
                }
            }
        }

        // The tower shaft: stacked bands, each floor capped by a projecting record-groove ring.
        int shaftBase = g + 6;
        int shaftTop = shaftBase + 58;
        int R = 7;
        for (int y = shaftBase; y <= shaftTop; y++) {
            boolean groove = ((y - shaftBase) % 5) == 4;
            int r = groove ? R + 1 : R;
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    if (dx * dx + dz * dz > r * r) {
                        continue;
                    }
                    b.put(cx + dx, y, cz + dz, groove ? p.accent() : p.wall());
                }
            }
            // Window band on the shell, four faces, every third floor.
            if (!groove && ((y - shaftBase) % 3) == 0) {
                b.put(cx + R, y, cz, p.window());
                b.put(cx - R, y, cz, p.window());
                b.put(cx, y, cz + R, p.window());
                b.put(cx, y, cz - R, p.window());
            }
        }

        // Cornice ring at the top, one block wider than the shaft.
        for (int dx = -R - 1; dx <= R + 1; dx++) {
            for (int dz = -R - 1; dz <= R + 1; dz++) {
                if (dx * dx + dz * dz <= (R + 1) * (R + 1)) {
                    b.put(cx + dx, shaftTop + 1, cz + dz, p.accent());
                }
            }
        }

        // Glazed penthouse (the machine room under the spire).
        int phBase = shaftTop + 2;
        int phR = 4;
        for (int y = phBase; y <= phBase + 4; y++) {
            for (int dx = -phR; dx <= phR; dx++) {
                for (int dz = -phR; dz <= phR; dz++) {
                    int d2 = dx * dx + dz * dz;
                    if (d2 > phR * phR) {
                        continue;
                    }
                    boolean shell = d2 > (phR - 1) * (phR - 1);
                    boolean win = shell && ((y - phBase) % 2 == 1);
                    b.put(cx + dx, y, cz + dz, win ? p.window() : p.wall());
                }
            }
        }
        b.fill(cx - phR, phBase + 5, cz - phR, cx + phR, phBase + 5, cz + phR, p.roof());

        // The needle: a small base ring, a tapering mast, and a lit tip.
        int nBase = phBase + 6;
        b.fill(cx - 1, nBase, cz - 1, cx + 1, nBase, cz + 1, p.accent());
        for (int y = nBase + 1; y <= nBase + 9; y++) {
            b.put(cx, y, cz, p.accent());
        }
        b.put(cx, nBase + 10, cz, p.light());
    }

    @Override
    public void flourish(StructureBuilder b, RandomSource rng, int x, int y0, int z,
                         int x1, int y1, int z1, Palette p) {
        // A pastel Deco band or a timber balcony on some upper walls.
        int roll = rng.nextInt(3);
        if (roll == 0) {
            return;
        }
        int y = y1 - 2;
        if (roll == 1) {
            switch (rng.nextInt(4)) {
                case 0 -> b.fill(x + 1, y, z, x1 - 1, y, z, p.accent());
                case 1 -> b.fill(x + 1, y, z1, x1 - 1, y, z1, p.accent());
                case 2 -> b.fill(x, y, z + 1, x, y, z1 - 1, p.accent());
                default -> b.fill(x1, y, z + 1, x1, y, z1 - 1, p.accent());
            }
        } else {
            switch (rng.nextInt(4)) {
                case 0 -> b.fill(x + 1, y, z - 1, x1 - 1, y, z - 1, p.rail());
                case 1 -> b.fill(x + 1, y, z1 + 1, x1 - 1, y, z1 + 1, p.rail());
                case 2 -> b.fill(x - 1, y, z + 1, x - 1, y, z1 - 1, p.rail());
                default -> b.fill(x1 + 1, y, z + 1, x1 + 1, y, z1 - 1, p.rail());
            }
        }
    }

    @Override
    public void streetProps(StructureBuilder b, RandomSource rng, int cx, int cz,
                            int district, int ground, Palette p) {
        // Palm-lined boulevards: fronded posts down both main axes.
        for (int d = 28; d <= district - 8; d += 16) {
            palm(b, cx + d, cz + 5, ground, p);
            palm(b, cx - d, cz - 5, ground, p);
            palm(b, cx + 5, cz + d, ground, p);
            palm(b, cx - 5, cz - d, ground, p);
        }
        // Lamp posts flank the plaza.
        for (int d = 30; d <= district - 10; d += 20) {
            StyleKit.lanternPost(b, cx + d, cz - 3, ground, p);
            StyleKit.lanternPost(b, cx - d, cz + 3, ground, p);
        }
        // Statues and the big HOLLYWOOD sign/arch on the approaches.
        StyleKit.statue(b, cx - 8, cz + district - 34, ground, p);
        StyleKit.statue(b, cx + 8, cz + district - 34, ground, p);
        bigSign(b, cx, cz + district - 44, ground, p);
    }

    // ------------------------------------------------------------------ local helpers

    /**
     * The mission landmark: a stucco nave under a terracotta pitched roof, an arched arcade
     * colonnade along the south front, and a red-tile bell tower at the north-west corner.
     * Small and cheap — safe to call per chunk.
     */
    private static void mission(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int w = 11;
        int l = 19;
        int h = 8;
        int x0 = cx - w / 2;
        int x1 = x0 + w - 1;
        int z0 = cz - l / 2;
        int z1 = z0 + l - 1;
        int y0 = ground + 1;
        int y1 = y0 + h;

        b.ground(x0 - 2, z0 - 2, x1 + 2, z1 + 2, ground, ground, p.foundation());
        b.room(x0, y0, z0, x1, y1, z1, new StructureBuilder.Doorway(Side.N, 5));
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, y1, 5, 1);
        for (int z = z0 + 3; z <= z1 - 3; z += 4) {
            b.window(x0, y0 + 3, z, 3, 1, true);
            b.window(x1, y0 + 3, z, 3, 1, true);
        }
        arcade(b, x0, x1, z1 + 3, ground, p);
        bellTower(b, x0 - 3, z0 + 3, ground, p);
    }

    /**
     * An arched arcade colonnade along X at fixed Z: terracotta piers two apart under a tile
     * lintel, capped with stair heads suggesting round mission arches.
     */
    private static void arcade(StructureBuilder b, int x0, int x1, int z, int ground, Palette p) {
        int y = ground + 1;
        int top = y + 3;
        for (int x = x0; x <= x1; x += 2) {
            b.fill(x, y, z, x, top, z, p.accent());
        }
        b.fill(x0 - 1, top + 1, z, x1 + 1, top + 1, z, p.roof());
        for (int x = x0 + 1; x <= x1 - 1; x += 2) {
            b.put(x, top, z, p.roofStairs());
        }
    }

    /**
     * A Mission bell tower: a slender stucco shaft with a red-tile cap and a tile bell canopy
     * carried on four corner posts, finished with a light.
     */
    private static void bellTower(StructureBuilder b, int x, int z, int ground, Palette p) {
        int y0 = ground + 1;
        int y1 = y0 + 18;
        b.room(x - 1, y0, z - 1, x + 1, y1, z + 1);
        // White revival stucco shaft (contrasts with the tan nave).
        b.walls(x - 1, y0, z - 1, x + 1, y1, z + 1, Materials.terracotta(DyeColor.WHITE));
        for (int y = y0; y <= y1; y++) {
            b.put(x - 1, y, z - 1, p.accent());
            b.put(x + 1, y, z - 1, p.accent());
            b.put(x - 1, y, z + 1, p.accent());
            b.put(x + 1, y, z + 1, p.accent());
        }
        for (int y = y0 + 4; y <= y1 - 3; y += 4) {
            b.window(x, y, z - 1, 2, 1, true);
            b.window(x, y, z + 1, 2, 1, true);
        }
        // Tile cap.
        b.pitchedRoof(x - 2, z - 2, x + 2, z + 2, y1, 3, 0);
        // Bell canopy: four posts, a lintel and a small tile roof, lit from within.
        int by = y1 + 4;
        for (int y = y1 + 1; y <= by; y++) {
            b.put(x - 1, y, z - 1, p.accent());
            b.put(x + 1, y, z - 1, p.accent());
            b.put(x - 1, y, z + 1, p.accent());
            b.put(x + 1, y, z + 1, p.accent());
        }
        b.put(x, by, z, p.light());
        b.pitchedRoof(x - 2, z - 2, x + 2, z + 2, by + 1, 3, 0);
    }

    /** A palm: a slim timber trunk with a mop of leaves. */
    private static void palm(StructureBuilder b, int x, int z, int ground, Palette p) {
        int h = 6;
        for (int y = ground + 1; y <= ground + h; y++) {
            b.put(x, y, z, Blocks.JUNGLE_LOG.defaultBlockState());
        }
        b.fill(x - 1, ground + h, z, x + 1, ground + h, z, Blocks.JUNGLE_LEAVES.defaultBlockState());
        b.fill(x, ground + h, z - 1, x, ground + h, z + 1, Blocks.JUNGLE_LEAVES.defaultBlockState());
        b.put(x, ground + h, z, Blocks.JUNGLE_LEAVES.defaultBlockState());
        b.put(x, ground + h + 1, z, Blocks.JUNGLE_LEAVES.defaultBlockState());
    }

    /**
     * A big hillside sign: two terracotta end piers carrying a band of pale letters on a stepped
     * frame — an unmistakable Tinseltown marker without spelling anything.
     */
    private static void bigSign(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int y = ground + 1;
        int half = 8;
        // End piers.
        b.fill(cx - half, y, cz, cx - half, y + 3, cz, p.accent());
        b.fill(cx + half, y, cz, cx + half, y + 3, cz, p.accent());
        // Letter band: pale panels with terracotta dividers.
        for (int i = -half + 1; i <= half - 1; i++) {
            b.put(cx + i, y + 1, cz, p.wall());
            b.put(cx + i, y + 2, cz, (i % 5 == 0) ? p.accent() : p.wall());
        }
        // Stepped frame along the top.
        for (int i = -half; i <= half; i++) {
            b.put(cx + i, y + 3, cz, p.accent());
        }
    }
}
