package com.sanchous98.eldritchhorror.world.loc.style;

import com.sanchous98.eldritchhorror.world.loc.Palette;
import com.sanchous98.eldritchhorror.world.loc.StructureBuilder;
import com.sanchous98.eldritchhorror.world.loc.StructureBuilder.Doorway;
import com.sanchous98.eldritchhorror.world.loc.StructureBuilder.Side;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

/**
 * Shared building blocks for {@link CityStyle} landmarks and props. This is <b>central code</b>:
 * styles call these, they do not copy them. Only {@link StructureBuilder} + {@link Palette}
 * blocks are used.
 *
 * <p>Add a helper here when two or more cultures need it (pagoda, dome, torii, bell tower,
 * minaret, obelisk, statue, lantern post…).
 */
public final class StyleKit {

    private StyleKit() {
    }

    // ------------------------------------------------------------------ European / gothic

    /** A long buttressed nave with a tall crenellated entrance tower and a dominating spire. */
    public static void cathedral(StructureBuilder b, RandomSource rng, int cx, int cz, int ground, Palette p) {
        int w = 31;
        int l = 61;
        int h = 30;
        int x0 = cx - w / 2;
        int x1 = x0 + w - 1;
        int z0 = cz - l / 2;
        int z1 = z0 + l - 1;
        int y0 = ground + 1;
        int y1 = y0 + h;

        b.ground(x0 - 3, z0 - 5, x1 + 3, z1 + 3, ground, ground, p.foundation());
        b.room(x0, y0, z0, x1, y1, z1, new Doorway(Side.N, w / 2));
        b.pitchedRoof(x0 - 2, z0 - 2, x1 + 2, z1 + 2, y1, 16, 1);

        for (int z = z0 + 3; z <= z1 - 3; z += 7) {
            b.buttress(x0, z, y0, 9, Side.W);
            b.buttress(x1, z, y0, 9, Side.E);
        }
        int wy = y0 + 6;
        for (int z = z0 + 4; z <= z1 - 4; z += 5) {
            b.window(x0, wy, z, 7, 1, true);
            b.window(x1, wy, z, 7, 1, true);
        }

        int tx0 = cx - 9;
        int tx1 = cx + 9;
        int tz0 = z0 - 16;
        int tz1 = z0 - 1;
        int towerTop = y0 + 64;
        b.room(tx0, y0, tz0, tx1, towerTop, tz1, new Doorway(Side.S, 9));
        for (int y = y0; y <= towerTop; y++) {
            b.put(tx0, y, tz0, p.accent());
            b.put(tx1, y, tz0, p.accent());
            b.put(tx0, y, tz1, p.accent());
            b.put(tx1, y, tz1, p.accent());
        }
        for (int y = y0 + 8; y <= towerTop - 6; y += 9) {
            b.window(tx0, y, (tz0 + tz1) / 2, 6, 1, true);
            b.window(tx1, y, (tz0 + tz1) / 2, 6, 1, true);
            b.window((tx0 + tx1) / 2, y, tz0, 6, 1, true);
        }
        b.crenellations(tx0 - 1, tz0 - 1, tx1 + 1, tz1 + 1, towerTop + 1);
        b.spire(cx, (tz0 + tz1) / 2, towerTop + 3, 44);
        twoHighDoor(b, p, cx, tz1, y0 + 1, Direction.SOUTH);
    }

    // ------------------------------------------------------------------ East Asian

    /**
     * A multi-level pagoda: a square shaft with a flared roof at each level, tapering upward,
     * capped with a spire. {@code levels} 3–7. Vermilion/accent frames, kawara-coloured roofs.
     */
    public static void pagoda(StructureBuilder b, int cx, int cz, int baseY, int levels, Palette p) {
        int half = 15;
        int storey = 9;
        for (int lv = 0; lv < levels; lv++) {
            int s = half - lv * 3;             // footprint shrinks toward the top
            if (s < 1) {
                break;
            }
            int y = baseY + lv * storey;
            int x0 = cx - s;
            int x1 = cx + s;
            int z0 = cz - s;
            int z1 = cz + s;
            b.room(x0, y, z0, x1, y + storey - 1, z1);
            // Corner pillars in the accent (vermilion) colour.
            for (int yy = y; yy <= y + storey - 1; yy++) {
                b.put(x0, yy, z0, p.accent());
                b.put(x1, yy, z0, p.accent());
                b.put(x0, yy, z1, p.accent());
                b.put(x1, yy, z1, p.accent());
            }
            // Flared roof: an overhanging pitched shell two blocks wider than the shaft.
            b.pitchedRoof(x0 - 3, z0 - 3, x1 + 3, z1 + 3, y + storey - 1, 6, 0);
            b.window(x0, y + 3, (z0 + z1) / 2, 4, 1, true);
            b.window(x1, y + 3, (z0 + z1) / 2, 4, 1, true);
            b.window((x0 + x1) / 2, y + 3, z0, 4, 1, true);
            b.window((x0 + x1) / 2, y + 3, z1, 4, 1, true);
        }
        b.spire(cx, cz, baseY + levels * storey, 30);
    }

    /** A vermilion torii gate: two pillars, a curved top lintel and a lower tie beam. */
    public static void torii(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int half = 3;
        int h = 5;
        for (int y = ground + 1; y <= ground + h; y++) {
            b.put(cx - half, y, cz, p.accent());
            b.put(cx + half, y, cz, p.accent());
        }
        // Lower tie beam.
        b.fill(cx - half, ground + h - 1, cz, cx + half, ground + h - 1, cz, p.accent());
        // Top lintel, one wider on each side (the kasagi).
        b.fill(cx - half - 1, ground + h + 1, cz, cx + half + 1, ground + h + 1, cz, p.accent());
    }

    // ------------------------------------------------------------------ Islamic

    /**
     * A mosque: a rectangular hall with a large central dome and two minarets at the corners.
     * The dome is built as concentric shrinking rings (no spherical block exists).
     */
    public static void mosque(StructureBuilder b, RandomSource rng, int cx, int cz, int ground, Palette p) {
        int w = 37;
        int d = 31;
        int h = 21;
        int x0 = cx - w / 2;
        int x1 = x0 + w - 1;
        int z0 = cz - d / 2;
        int z1 = z0 + d - 1;
        int y0 = ground + 1;
        int y1 = y0 + h;

        b.ground(x0 - 3, z0 - 3, x1 + 3, z1 + 3, ground, ground, p.foundation());
        b.room(x0, y0, z0, x1, y1, z1, new Doorway(Side.N, w / 2));
        b.crenellations(x0 - 1, z0 - 1, x1 + 1, z1 + 1, y1 + 1);
        for (int z = z0 + 3; z <= z1 - 3; z += 4) {
            b.window(x0, y0 + 6, z, 6, 1, true);
            b.window(x1, y0 + 6, z, 6, 1, true);
        }
        dome(b, cx, cz, y1, 16, p);
        // Two minarets at the north corners.
        minaret(b, x0, z0 - 7, ground, 58, p);
        minaret(b, x1, z0 - 7, ground, 58, p);
        twoHighDoor(b, p, cx, z1, y0 + 1, Direction.SOUTH);
    }

    /** A hemispherical dome drawn as shrinking square rings, capped with a small finial. */
    public static void dome(StructureBuilder b, int cx, int cz, int baseY, int radius, Palette p) {
        for (int r = radius; r >= 0; r--) {
            int y = baseY + (radius - r);
            b.fill(cx - r, y, cz - r, cx + r, y, cz + r, p.roof());
            // Shell: only the perimeter is the wall colour, interior is filled with roof.
            for (int i = -r; i <= r; i++) {
                b.put(cx + i, y, cz - r, p.accent());
                b.put(cx + i, y, cz + r, p.accent());
                b.put(cx - r, y, cz + i, p.accent());
                b.put(cx + r, y, cz + i, p.accent());
            }
        }
        b.put(cx, baseY + radius + 1, cz, p.light());
    }

    /** A slender tower with a balcony ring of crenellations and a small dome-cap. */
    public static void minaret(StructureBuilder b, int cx, int cz, int ground, int height, Palette p) {
        int r = 2;
        int top = ground + 1 + height;
        b.room(cx - r, ground + 1, cz - r, cx + r, top, cz + r);
        for (int y = ground + 1; y <= top; y++) {
            b.put(cx - r, y, cz - r, p.accent());
            b.put(cx + r, y, cz - r, p.accent());
            b.put(cx - r, y, cz + r, p.accent());
            b.put(cx + r, y, cz + r, p.accent());
        }
        b.crenellations(cx - r - 1, cz - r - 1, cx + r + 1, cz + r + 1, top - 4);
        b.spire(cx, cz, top + 1, 8);
    }

    // ------------------------------------------------------------------ South Asian

    /** A stepped temple tower (vimana): nested shrinking tiers rising to a finial. */
    public static void steppedTemple(StructureBuilder b, int cx, int cz, int ground, int tiers, Palette p) {
        int half = 19;
        int y = ground + 1;
        for (int t = 0; t < tiers; t++) {
            int s = half - t * 3;
            if (s < 1) {
                break;
            }
            b.room(cx - s, y, cz - s, cx + s, y + 6, cz + s);
            for (int yy = y; yy <= y + 6; yy++) {
                b.put(cx - s, yy, cz - s, p.accent());
                b.put(cx + s, yy, cz - s, p.accent());
                b.put(cx - s, yy, cz + s, p.accent());
                b.put(cx + s, yy, cz + s, p.accent());
            }
            y += 7;
        }
        b.spire(cx, cz, y, 22);
    }

    // ------------------------------------------------------------------ generic props

    /** A lamp / lantern post beside a street. The district is flat, so `ground` is correct. */
    public static void lanternPost(StructureBuilder b, int x, int z, int ground, Palette p) {
        b.put(x, ground + 1, z, p.rail());
        b.put(x, ground + 2, z, p.rail());
        b.put(x, ground + 3, z, p.light());
    }

    /** A tall obelisk (monument/plinth) at a plaza. */
    public static void obelisk(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        b.fill(cx - 2, ground + 1, cz - 2, cx + 2, ground + 2, cz + 2, p.foundation());
        b.fill(cx - 1, ground + 3, cz - 1, cx + 1, ground + 3, cz + 1, p.accent());
        for (int y = ground + 4; y <= ground + 12; y++) {
            b.put(cx, y, cz, p.accent());
        }
        b.put(cx, ground + 13, cz, p.light());
    }

    /** An equestrian/standing statue: plinth + a rough figure in the accent colour. */
    public static void statue(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        b.fill(cx - 1, ground + 1, cz - 1, cx + 1, ground + 2, cz + 1, p.foundation());
        b.put(cx, ground + 3, cz, p.accent());
        b.put(cx, ground + 4, cz, p.accent());
        b.put(cx - 1, ground + 4, cz, p.accent());
        b.put(cx + 1, ground + 4, cz, p.accent());
        b.put(cx, ground + 5, cz, p.accent());
    }

    /** Places a 1x2 door facing {@code facing} at the given column. */
    public static void twoHighDoor(StructureBuilder b, Palette p, int x, int z, int y, Direction facing) {
        BlockState lower = p.door()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, facing)
                .setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.LOWER);
        b.put(x, y, z, lower);
        b.put(x, y + 1, z, lower.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER));
    }
}
