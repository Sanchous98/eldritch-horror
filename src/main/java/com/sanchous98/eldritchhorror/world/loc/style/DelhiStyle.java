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
        mausoleum(b, cx, cz, ground, p);
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

    /**
     * A domed mausoleum: a square sandstone hall on a marble plinth, a bulbous dome on a
     * cylindrical drum, four corner minarets and four chhatri pavilions.
     */
    private static void mausoleum(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int w = 17;
        int h = 11;
        int x0 = cx - w / 2;
        int x1 = x0 + w - 1;
        int z0 = cz - w / 2;
        int z1 = z0 + w - 1;
        int y0 = ground + 1;
        int y1 = y0 + h;

        b.ground(x0 - 3, z0 - 3, x1 + 3, z1 + 3, ground, ground, p.foundation());
        b.room(x0, y0, z0, x1, y1, z1, new StructureBuilder.Doorway(Side.S, 8));
        // Marble plinth band and a cornice of marble.
        b.walls(x0, y0, z0, x1, y0 + 1, z1, p.accent());
        b.crenellations(x0 - 1, z0 - 1, x1 + 1, z1 + 1, y1 + 1);

        // Pointed-arch iwan recesses on each face (marble frames).
        iwan(b, cx, z0, y0, p, true);
        iwan(b, cx, z1, y0, p, true);
        iwan(b, x0, cz, y0, p, false);
        iwan(b, x1, cz, y0, p, false);

        // Cylindrical drum, then a bulbous dome.
        int drumY = y1 + 2;
        b.fill(cx - 3, y1 + 1, cz - 3, cx + 3, y1 + 1, cz + 3, p.foundation());
        b.fill(cx - 3, drumY, cz - 3, cx + 3, drumY + 2, cz + 3, p.wall());
        for (int i = -3; i <= 3; i++) {
            b.put(cx + i, drumY + 1, cz - 3, p.accent());
            b.put(cx + i, drumY + 1, cz + 3, p.accent());
            b.put(cx - 3, drumY + 1, cz + i, p.accent());
            b.put(cx + 3, drumY + 1, cz + i, p.accent());
        }
        bulbousDome(b, cx, cz, drumY + 3, 4, p);

        // Four corner minarets, standing proud of the plinth.
        StyleKit.minaret(b, x0 - 2, z0 - 2, ground, 24, p);
        StyleKit.minaret(b, x1 + 2, z0 - 2, ground, 24, p);
        StyleKit.minaret(b, x0 - 2, z1 + 2, ground, 24, p);
        StyleKit.minaret(b, x1 + 2, z1 + 2, ground, 24, p);

        // Four chhatri pavilions on the roof corners.
        chhatri(b, x0 + 1, z0 + 1, y1, p);
        chhatri(b, x1 - 1, z0 + 1, y1, p);
        chhatri(b, x0 + 1, z1 - 1, y1, p);
        chhatri(b, x1 - 1, z1 - 1, y1, p);
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
