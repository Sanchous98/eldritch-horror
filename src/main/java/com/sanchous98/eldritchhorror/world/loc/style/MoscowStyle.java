package com.sanchous98.eldritchhorror.world.loc.style;

import com.sanchous98.eldritchhorror.world.loc.Palette;
import com.sanchous98.eldritchhorror.world.loc.StructureBuilder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Moscow — Russian style. Bright red-brick (Kremlin) walls with white-limestone trim, steep
 * pyramidal (tent) towers and bulging onion domes, under snow, lit by warm lanterns.
 *
 * <p>Landmark: a St Basil's-style cluster — a tall red-brick tent tower crowned by a gold onion,
 * ringed by smaller onion domes on drums, each a shrinking stack of {@link StructureBuilder#fill}
 * rings that bulges out before tapering to a point, plus little spires. Street props: lamp posts,
 * stone monuments and a white-stone clock tower. Follows the pattern of {@link TokyoStyle}; the
 * shared helpers live in {@link StyleKit}.
 */
public final class MoscowStyle implements CityStyle {

    /** Glazed shells for the satellite onion domes (St Basil's kaleidoscope of coloured onions). */
    private static final BlockState[] DOME_SHELLS = {
            Materials.glazed(DyeColor.GREEN),
            Materials.glazed(DyeColor.RED),
            Materials.glazed(DyeColor.WHITE),
            Materials.glazed(DyeColor.BLUE),
            Materials.glazed(DyeColor.YELLOW),
            Materials.glazed(DyeColor.PURPLE),
            Materials.glazed(DyeColor.LIGHT_BLUE),
            Materials.glazed(DyeColor.ORANGE),
            Materials.glazed(DyeColor.CYAN)
    };

    @Override
    public String id() {
        return "moscow";
    }

    @Override
    public Palette palette(int koppenClass, boolean coastal) {
        // Culture leads: bright red brick + white stone whatever the climate. Köppen only tints the
        // overgrowth; the coast swaps it for kelp.
        Palette bio = Palette.fromBiome(koppenClass, coastal);
        return new Palette(
                Blocks.SNOW_BLOCK.defaultBlockState(),              // ground: trodden snow plaza
                Blocks.STONE_BRICKS.defaultBlockState(),            // foundation: white-stone footing
                Blocks.BRICKS.defaultBlockState(),                  // wall: bright red brick (Kremlin)
                Materials.terracotta(DyeColor.RED),                 // weathered brick course
                Blocks.SMOOTH_QUARTZ.defaultBlockState(),           // accent: white limestone trim
                Blocks.RED_NETHER_BRICKS.defaultBlockState(),       // roof: vivid red tile, not London slate
                Blocks.RED_NETHER_BRICK_STAIRS.defaultBlockState(), // roof stairs
                Blocks.RED_NETHER_BRICK_SLAB.defaultBlockState(),   // roof slabs / eaves
                Blocks.GLASS_PANE.defaultBlockState(),              // window
                Blocks.SPRUCE_TRAPDOOR.defaultBlockState(),         // frame: timber mullions
                Blocks.SPRUCE_DOOR.defaultBlockState(),             // door
                Blocks.SPRUCE_FENCE.defaultBlockState(),            // rail
                Blocks.LANTERN.defaultBlockState(),                 // light: warm street lantern
                coastal ? Blocks.KELP.defaultBlockState() : bio.overgrowth(),
                Blocks.GRAVEL.defaultBlockState());                 // rubble
    }

    // ------------------------------------------------------------------ landmark

    @Override
    public void landmark(StructureBuilder b, RandomSource rng, int cx, int cz, int ground, Palette p) {
        int y0 = ground + 1;
        int half = 21;

        // Snowy red-brick terrace with a white-stone kerb.
        b.ground(cx - half, cz - half, cx + half, cz + half, ground, ground, p.foundation());
        b.fill(cx - half, y0, cz - half, cx + half, y0, cz + half, p.wall());
        b.walls(cx - half, y0, cz - half, cx + half, y0, cz + half, p.accent());

        // The central tent tower (Ivan the Great), crowned with a gold onion: the tallest spire.
        tentTower(b, cx, cz, y0 + 1, 7, 50, p);

        // A ring of eight coloured onion domes, each on its own brick tower and drum, of varied
        // height and width, radiating from the central tower. The ninth dome is the gold centre.
        int n = DOME_SHELLS.length;
        int ringR = 16;
        for (int k = 0; k < n; k++) {
            double a = (Math.PI * 2 / n) * k + (rng.nextDouble() - 0.5) * 0.30;
            int tx = cx + (int) Math.round(Math.cos(a) * ringR);
            int tz = cz + (int) Math.round(Math.sin(a) * ringR);

            // Its own red-brick tower: a hollow radius-3 shaft with white-stone window slits.
            int towerH = 10 + rng.nextInt(13); // varied: 10..22
            for (int y = y0 + 1; y < y0 + 1 + towerH; y++) {
                for (int dx = -3; dx <= 3; dx++) {
                    for (int dz = -3; dz <= 3; dz++) {
                        int d2 = dx * dx + dz * dz;
                        if (d2 <= 9 && d2 > 4) {
                            b.put(tx + dx, y, tz + dz, p.wall());
                        }
                    }
                }
            }
            b.window(tx - 3, y0 + 4, tz, 3, 1, true);
            b.window(tx + 3, y0 + 4, tz, 3, 1, true);

            // A short white-stone drum, then the coloured onion and its finial.
            int drumH = 3 + rng.nextInt(3);
            int drumBase = y0 + 1 + towerH;
            drum(b, tx, tz, drumBase, 2, drumH, p);
            onion(b, tx, tz, drumBase + drumH, 2 + rng.nextInt(2), DOME_SHELLS[k], p.accent());
        }

        b.marker("moscow_landmark", cx, y0, cz);
    }

    /** A steeply tapering red-brick pyramid with white-stone ribs, crown and a gold onion. */
    private static void tentTower(StructureBuilder b, int cx, int cz, int baseY, int baseR, int height, Palette p) {
        for (int i = 0; i <= height; i++) {
            int s = Math.round(baseR * (1.0f - (float) i / height));
            if (s < 1) {
                s = 1;
            }
            ring(b, cx, cz, baseY + i, s, p.wall(), p.accent());
        }
        int topY = baseY + height + 1;
        // White-stone crown courses.
        b.fill(cx - 2, topY, cz - 2, cx + 2, topY, cz + 2, p.accent());
        b.fill(cx - 1, topY + 1, cz - 1, cx + 1, topY + 1, cz + 1, p.accent());
        onion(b, cx, cz, topY + 2, 2, Blocks.GOLD_BLOCK.defaultBlockState(), p.accent());
    }

    /** A short red-brick drum (cylinder) with white-stone ribs, carrying an onion dome. */
    private static void drum(StructureBuilder b, int cx, int cz, int baseY, int r, int height, Palette p) {
        for (int y = baseY; y < baseY + height; y++) {
            ring(b, cx, cz, y, r, p.wall(), p.accent());
        }
    }

    /**
     * An onion dome: a stack of {@code fill} rings whose radius bulges from 1 up to {@code maxR}
     * and tapers back to a point, capped with a gold finial and a rod. Taller and fuller than
     * {@link StyleKit#dome}.
     */
    private static void onion(StructureBuilder b, int cx, int cz, int baseY, int maxR, BlockState shell, BlockState trim) {
        int h = maxR * 2 + 2;
        for (int i = 0; i <= h; i++) {
            double t = (double) i / h;
            int r = (int) Math.round(1 + (maxR - 1) * Math.sin(Math.PI * t));
            if (r < 1) {
                r = 1;
            }
            ring(b, cx, cz, baseY + i, r, shell, trim);
            if (t >= 0.5 && r <= 1) {
                b.put(cx, baseY + i + 1, cz, Blocks.GOLD_BLOCK.defaultBlockState());
                b.put(cx, baseY + i + 2, cz, trim);
                b.put(cx, baseY + i + 3, cz, Blocks.END_ROD.defaultBlockState());
                return;
            }
        }
    }

    /** One solid horizontal ring: {@code shell} fill with an accent perimeter. */
    private static void ring(StructureBuilder b, int cx, int cz, int y, int r, BlockState shell, BlockState trim) {
        b.fill(cx - r, y, cz - r, cx + r, y, cz + r, shell);
        for (int i = -r; i <= r; i++) {
            b.put(cx + i, y, cz - r, trim);
            b.put(cx + i, y, cz + r, trim);
            b.put(cx - r, y, cz + i, trim);
            b.put(cx + r, y, cz + i, trim);
        }
    }

    // ------------------------------------------------------------------ flourishes & props

    @Override
    public void flourish(StructureBuilder b, RandomSource rng, int x, int y0, int z,
                         int x1, int y1, int z1, Palette p) {
        // White-stone corbel band along the top wall course (kokoshnik-like eave trim).
        b.walls(x, y1, z, x1, y1, z1, p.accent());
    }

    @Override
    public void streetProps(StructureBuilder b, RandomSource rng, int cx, int cz,
                            int district, int ground, Palette p) {
        // Lamp posts down the two main axes.
        for (int d = 26; d <= district - 12; d += 20) {
            StyleKit.lanternPost(b, cx + d, cz + 5, ground, p);
            StyleKit.lanternPost(b, cx - d, cz - 5, ground, p);
        }
        // Stone monuments at two plaza corners.
        StyleKit.obelisk(b, cx - district + 18, cz - district + 18, ground, p);
        StyleKit.statue(b, cx + district - 18, cz + district - 18, ground, p);
        // A white-stone clock tower marking the district.
        clockTower(b, cx + district - 16, cz - 8, ground, p);
    }

    /** A slim white-stone tower with a dial face and a dark spire. */
    private static void clockTower(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int r = 2;
        int base = ground + 1;
        int top = base + 20;
        b.room(cx - r, base, cz - r, cx + r, top, cz + r);
        for (int y = base; y <= top; y++) {
            b.put(cx - r, y, cz - r, p.accent());
            b.put(cx + r, y, cz - r, p.accent());
            b.put(cx - r, y, cz + r, p.accent());
            b.put(cx + r, y, cz + r, p.accent());
        }
        b.window(cx - r, base + 4, cz, 3, 1, true);
        b.window(cx + r, base + 9, cz, 3, 1, true);
        clockFace(b, cx, base + 15, cz + r, p);
        b.crenellations(cx - r, cz - r, cx + r, cz + r, top + 1);
        b.spire(cx, cz, top + 2, 7);
    }

    /** A 3x3 white-stone dial with a gold hub and black hands. */
    private static void clockFace(StructureBuilder b, int x, int y, int z, Palette p) {
        b.fill(x - 1, y - 1, z, x + 1, y + 1, z, p.accent());
        b.put(x, y, z, Blocks.GOLD_BLOCK.defaultBlockState());
        b.put(x, y + 1, z, Materials.concrete(DyeColor.BLACK));
        b.put(x + 1, y, z, Materials.concrete(DyeColor.BLACK));
    }
}
