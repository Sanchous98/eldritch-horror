package com.sanchous98.eldritchhorror.world.loc.style;

import com.sanchous98.eldritchhorror.world.loc.Palette;
import com.sanchous98.eldritchhorror.world.loc.StructureBuilder;
import com.sanchous98.eldritchhorror.world.loc.StructureBuilder.Side;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Blocks;

/**
 * Delhi — Mughal cultural style. Red sandstone and white marble, bulbous domes, slender
 * minarets, chhatri pavilions, lapis-and-gold inlay, and formal water gardens.
 *
 * <p>Landmark: a domed mausoleum with four corner minarets (Humayun's Tomb / Taj silhouette).
 * Street props: a central fountain and chhatri pavilions, plus obelisks, statues and lantern
 * posts along the axes.
 *
 * <p>Deterministic and cheap: only {@link StructureBuilder} + {@link Palette} blocks (and
 * {@link StyleKit}/{@link Materials}); all randomness comes from the passed {@link RandomSource}.
 */
public final class DelhiStyle implements CityStyle {

    @Override
    public String id() {
        return "delhi";
    }

    @Override
    public Palette palette(int koppenClass, boolean coastal) {
        // Culture leads: red sandstone shell, white marble accents and inlay, bulbous pale domes.
        return new Palette(
                Blocks.RED_SANDSTONE.defaultBlockState(),            // ground: paved red plaza
                Blocks.CUT_RED_SANDSTONE.defaultBlockState(),        // foundation
                Blocks.RED_SANDSTONE.defaultBlockState(),            // wall: red sandstone
                Blocks.CHISELED_RED_SANDSTONE.defaultBlockState(),   // weathered
                Blocks.SMOOTH_QUARTZ.defaultBlockState(),            // accent: white marble
                Blocks.SMOOTH_QUARTZ.defaultBlockState(),            // roof: marble dome
                Blocks.RED_SANDSTONE_STAIRS.defaultBlockState(),     // roof stairs
                Blocks.SMOOTH_RED_SANDSTONE_SLAB.defaultBlockState(),// roof slabs / eaves
                Blocks.GLASS_PANE.defaultBlockState(),               // window
                Blocks.SMOOTH_QUARTZ.defaultBlockState(),            // frame: marble mullions
                Blocks.ACACIA_DOOR.defaultBlockState(),              // door
                Blocks.RED_SANDSTONE_WALL.defaultBlockState(),       // rail: pierced sandstone rail
                Blocks.LANTERN.defaultBlockState(),                  // light
                Materials.terracotta(DyeColor.BLUE), // inlay / garden blue
                Blocks.GRAVEL.defaultBlockState());                  // rubble
    }

    @Override
    public void landmark(StructureBuilder b, RandomSource rng, int cx, int cz, int ground, Palette p) {
        // The Taj: a white marble mausoleum on a raised podium — a great onion dome on a drum,
        // four slender corner minarets, and a reflecting-pool axis running south.
        int ph = 31;   // 63-block marble podium
        int py = ground + 1;
        b.ground(cx - ph, cz - ph, cx + ph, cz + ph, ground, ground, p.accent());
        b.fill(cx - ph, py, cz - ph, cx + ph, py + 1, cz + ph, p.accent());            // podium slab
        b.fill(cx - ph + 4, py + 2, cz - ph + 4, cx + ph - 4, py + 2, cz + ph - 4, p.foundation());

        // Main mausoleum: a tall marble cube with a pishtaq recess on each face.
        int w = 15;
        int y0 = py + 3;
        int x0 = cx - w;
        int x1 = cx + w;
        int z0 = cz - w;
        int z1 = cz + w;
        int y1 = y0 + 34;
        b.room(x0, y0, z0, x1, y1, z1, new StructureBuilder.Doorway(Side.S, w));
        b.walls(x0, y0, z0, x1, y1, z1, p.accent());   // white-marble skin over the sandstone core
        b.put(cx, y0 + 1, z1, Blocks.AIR.defaultBlockState());  // re-open the south entrance
        b.put(cx, y0 + 2, z1, Blocks.AIR.defaultBlockState());
        iwan(b, cx, z0, y0, p, true);
        iwan(b, cx, z1, y0, p, true);
        iwan(b, x0, cz, y0, p, false);
        iwan(b, x1, cz, y0, p, false);
        b.fill(x0 - 1, y1 + 1, z0 - 1, x1 + 1, y1 + 1, z1 + 1, p.accent());   // chhajja cornice
        for (int z = z0 + 3; z <= z1 - 3; z += 6) {
            b.window(x0, y0 + 8, z, 8, 1, true);
            b.window(x1, y0 + 8, z, 8, 1, true);
        }

        // Four small chhatri pavilions on the roof corners, flanking the great dome.
        chhatri(b, x0 + 2, z0 + 2, y1 + 2, p);
        chhatri(b, x1 - 2, z0 + 2, y1 + 2, p);
        chhatri(b, x0 + 2, z1 - 2, y1 + 2, p);
        chhatri(b, x1 - 2, z1 - 2, y1 + 2, p);

        // Drum, then the great onion dome: a bulb that swells past the drum, then tapers to a finial.
        int drumR = 9;
        b.fill(cx - drumR, y1 + 2, cz - drumR, cx + drumR, y1 + 2, cz + drumR, p.foundation());
        b.fill(cx - drumR + 1, y1 + 2, cz - drumR + 1, cx + drumR - 1, y1 + 7, cz + drumR - 1, p.roof());
        for (int i = -drumR + 1; i <= drumR - 1; i++) {
            b.put(cx + i, y1 + 5, cz - drumR + 1, p.accent());
            b.put(cx + i, y1 + 5, cz + drumR - 1, p.accent());
            b.put(cx - drumR + 1, y1 + 5, cz + i, p.accent());
            b.put(cx + drumR - 1, y1 + 5, cz + i, p.accent());
        }
        int[] rings = {9, 10, 10, 9, 8, 7, 5, 3, 1};
        for (int i = 0; i < rings.length; i++) {
            int r = rings[i];
            int yy = y1 + 8 + i;
            b.fill(cx - r, yy, cz - r, cx + r, yy, cz + r, p.roof());
            for (int j = -r; j <= r; j++) {
                b.put(cx + j, yy, cz - r, p.accent());
                b.put(cx + j, yy, cz + r, p.accent());
                b.put(cx - r, yy, cz + j, p.accent());
                b.put(cx + r, yy, cz + j, p.accent());
            }
        }
        b.spire(cx, cz, y1 + 8 + rings.length, 12);   // gilt finial above the dome

        // Four slender corner minarets, marble-skinned so the whole monument reads white.
        int m = ph - 5;
        int mh = 54;
        int mtop = ground + 1 + mh;
        StyleKit.minaret(b, cx - m, cz - m, ground, mh, p);
        StyleKit.minaret(b, cx + m, cz - m, ground, mh, p);
        StyleKit.minaret(b, cx - m, cz + m, ground, mh, p);
        StyleKit.minaret(b, cx + m, cz + m, ground, mh, p);
        b.walls(cx - m - 2, ground + 1, cz - m - 2, cx - m + 2, mtop, cz - m + 2, p.accent());
        b.walls(cx + m - 2, ground + 1, cz - m - 2, cx + m + 2, mtop, cz - m + 2, p.accent());
        b.walls(cx - m - 2, ground + 1, cz + m - 2, cx - m + 2, mtop, cz + m + 2, p.accent());
        b.walls(cx + m - 2, ground + 1, cz + m - 2, cx + m + 2, mtop, cz + m + 2, p.accent());

        // Reflecting-pool axis: a marble-rimmed channel running south from the podium.
        int qx0 = cx - 4;
        int qx1 = cx + 4;
        int qz0 = cz + 33;
        int qz1 = cz + 48;
        b.ground(qx0 - 3, qz0 - 3, qx1 + 3, qz1 + 3, ground, ground, p.accent());
        b.fill(qx0, ground + 1, qz0, qx1, ground + 1, qz1, p.foundation());
        b.fill(qx0 + 1, ground + 2, qz0, qx1 - 1, ground + 2, qz1, Materials.terracotta(DyeColor.BLUE));
        for (int z = qz0; z <= qz1; z++) {
            b.put(qx0, ground + 2, z, p.accent());
            b.put(qx1, ground + 2, z, p.accent());
        }
        // Marble lantern posts line the water axis.
        for (int z = qz0; z <= qz1; z += 5) {
            StyleKit.lanternPost(b, qx0 - 2, z, ground, p);
            StyleKit.lanternPost(b, qx1 + 2, z, ground, p);
        }
    }

    @Override
    public void flourish(StructureBuilder b, RandomSource rng, int x, int y0, int z,
                         int x1, int y1, int z1, Palette p) {
        // A shallow marble chhajja (eaves cornice) on the long south facade.
        if (x1 - x < 3) {
            return;
        }
        b.fill(x + 1, y1 + 1, z, x1 - 1, y1 + 1, z, p.accent());
        b.put(x + 1, y1 + 1, z, p.foundation());
        b.put(x1 - 1, y1 + 1, z, p.foundation());
    }

    @Override
    public void streetProps(StructureBuilder b, RandomSource rng, int cx, int cz,
                            int district, int ground, Palette p) {
        // Central water garden: a square marble-edged fountain.
        fountain(b, cx, cz, ground, p);
        // A chhatri pavilion on each approach to the plaza.
        int off = Math.max(10, district - 16);
        chhatri(b, cx, cz - off, ground, p);
        chhatri(b, cx, cz + off, ground, p);
        chhatri(b, cx - off, cz, ground, p);
        chhatri(b, cx + off, cz, ground, p);
        // Obelisks at the four corners of the plaza.
        int c = Math.max(14, district - 8);
        StyleKit.obelisk(b, cx - c, cz - c, ground, p);
        StyleKit.obelisk(b, cx + c, cz - c, ground, p);
        StyleKit.obelisk(b, cx - c, cz + c, ground, p);
        StyleKit.obelisk(b, cx + c, cz + c, ground, p);
        // A standing statue on the north / south axes.
        StyleKit.statue(b, cx, cz - Math.max(20, district - 4), ground, p);
        StyleKit.statue(b, cx, cz + Math.max(20, district - 4), ground, p);
        // Lantern posts down the main axes.
        for (int d = 26; d <= district - 10; d += 14) {
            StyleKit.lanternPost(b, cx + d, cz + 3, ground, p);
            StyleKit.lanternPost(b, cx - d, cz - 3, ground, p);
        }
    }

    // ------------------------------------------------------------------ Mughal forms

    /** A central courtyard fountain: a marble rim around a lapis water square. */
    private static void fountain(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        b.fill(cx - 4, ground, cz - 4, cx + 4, ground + 1, cz + 4, p.foundation());
        b.fill(cx - 3, ground + 1, cz - 3, cx + 3, ground + 1, cz + 3,
                Materials.terracotta(DyeColor.BLUE));
        for (int i = -3; i <= 3; i++) {
            b.put(cx + i, ground + 2, cz - 3, p.accent());
            b.put(cx + i, ground + 2, cz + 3, p.accent());
            b.put(cx - 3, ground + 2, cz + i, p.accent());
            b.put(cx + 3, ground + 2, cz + i, p.accent());
        }
        b.put(cx, ground + 2, cz, p.light());
    }


    /** A shallow iwan: a marble-framed arched recess centred on one hall face. */
    private static void iwan(StructureBuilder b, int fx, int fz, int y0, Palette p, boolean xFace) {
        int half = 3;
        for (int i = -half; i <= half; i++) {
            int px = xFace ? fx + i : fx;
            int pz = xFace ? fz : fz + i;
            b.put(px, y0 + 1, pz, p.accent());
            b.put(px, y0 + 2, pz, p.accent());
            b.put(px, y0 + 5, pz, p.accent());
            b.put(px, y0 + 6, pz, p.accent());
        }
        for (int y = y0 + 1; y <= y0 + 7; y++) {
            int edge = xFace ? fx + (y > y0 + 4 ? half - (y - y0 - 5) : half) : fx;
            int edgeZ = xFace ? fz : fz + (y > y0 + 4 ? half - (y - y0 - 5) : half);
            b.put(edge, y, edgeZ, p.accent());
        }
    }

    /** A bulbous onion dome: shrinking rings that widen before closing, with a finial. */
    private static void bulbousDome(StructureBuilder b, int cx, int cz, int baseY, int radius, Palette p) {
        int[] rings = {radius, radius, radius - 1, radius - 2, radius - 3, radius - 4};
        for (int y = 0; y < rings.length; y++) {
            int r = Math.max(0, rings[y]);
            int yy = baseY + y;
            b.fill(cx - r, yy, cz - r, cx + r, yy, cz + r, p.roof());
            for (int i = -r; i <= r; i++) {
                b.put(cx + i, yy, cz - r, p.accent());
                b.put(cx + i, yy, cz + r, p.accent());
                b.put(cx - r, yy, cz + i, p.accent());
                b.put(cx + r, yy, cz + i, p.accent());
            }
        }
        b.put(cx, baseY + rings.length, cz, p.light());
    }

    /** A small open chhatri: four pillars, a lintel ring and a marble dome cap. */
    private static void chhatri(StructureBuilder b, int cx, int cz, int baseY, Palette p) {
        int r = 2;
        for (int y = baseY + 1; y <= baseY + 4; y++) {
            b.put(cx - r, y, cz - r, p.accent());
            b.put(cx + r, y, cz - r, p.accent());
            b.put(cx - r, y, cz + r, p.accent());
            b.put(cx + r, y, cz + r, p.accent());
        }
        // Lintel ring and dome cap.
        b.fill(cx - r, baseY + 5, cz - r, cx + r, baseY + 5, cz + r, p.foundation());
        bulbousDome(b, cx, cz, baseY + 6, 2, p);
    }
}
