package com.sanchous98.eldritchhorror.world.loc.style;

import com.sanchous98.eldritchhorror.world.loc.Palette;
import com.sanchous98.eldritchhorror.world.loc.StructureBuilder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Sydney — cultural city style. Australian colonial: warm <b>sandstone</b> walls, Victorian
 * terraces with dark slate roofs and <b>iron-lace</b> balconies, a working <b>harbour</b> of
 * timber posts.</p>
 *
 * <p>Landmark: the harbour <b>sails</b> — stacked, tapering shells in white concrete (
 * {@link Materials}) leaning out over a sandstone concourse. Street props: iron lamp posts,
 * statues, and a boardwalk of mooring posts. Flourish: a forged balcony on every terrace.
 *
 * <p>Culture leads; climate only varies the overgrowth. See
 * {@code docs/STRUCTURES-CONTRACT.md} § Cultural styles.
 */
public final class SydneyStyle implements CityStyle {

    @Override
    public String id() {
        return "sydney";
    }

    @Override
    public Palette palette(int koppenClass, boolean coastal) {
        Palette bio = Palette.fromBiome(koppenClass, coastal);
        return new Palette(
                Blocks.SMOOTH_SANDSTONE.defaultBlockState(),        // ground: paved sandstone street
                Blocks.CHISELED_SANDSTONE.defaultBlockState(),      // foundation: dressed base course
                Blocks.SANDSTONE.defaultBlockState(),               // wall: sandstone
                Blocks.CUT_SANDSTONE.defaultBlockState(),           // weathered sandstone
                Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState(),   // accent: dark timber trim
                Blocks.DEEPSLATE_TILES.defaultBlockState(),         // roof: dark slate
                Blocks.DEEPSLATE_TILE_STAIRS.defaultBlockState(),   // roof stairs
                Blocks.DEEPSLATE_TILE_SLAB.defaultBlockState(),     // roof slabs / eaves
                Blocks.GLASS_PANE.defaultBlockState(),              // window
                Blocks.SPRUCE_TRAPDOOR.defaultBlockState(),         // frame: timber mullions
                Blocks.DARK_OAK_DOOR.defaultBlockState(),           // door
                Blocks.IRON_BARS.defaultBlockState(),               // rail: wrought-iron lace
                Blocks.LANTERN.defaultBlockState(),                 // light: gas lamp
                coastal ? Blocks.KELP.defaultBlockState() : bio.overgrowth(),
                Blocks.GRAVEL.defaultBlockState());                 // rubble: harbour ballast
    }

    @Override
    public void landmark(StructureBuilder b, RandomSource rng, int cx, int cz, int ground, Palette p) {
        int by = ground + 1;
        BlockState shell = Materials.whiteConcrete();
        BlockState shade = Materials.lightGrayConcrete();

        // Broad stepped harbour podium / concourse.
        b.ground(cx - 30, cz - 20, cx + 30, cz + 26, ground, ground, p.foundation());
        b.ground(cx - 25, cz - 15, cx + 25, cz + 21, ground, ground, p.ground());
        for (int t = 0; t < 3; t++) {
            int s = 25 - t * 3;
            b.fill(cx - s, by + t, cz - 15 + t * 2, cx + s, by + t, cz + 21 - t * 2, p.foundation());
        }
        // Low harbour parapet along the water side (+Z), punctuated by gas lamps.
        for (int x = cx - 30; x <= cx + 30; x++) {
            b.put(x, by + 3, cz + 26, p.foundation());
            b.put(x, by + 4, cz + 26, p.wall());
        }
        for (int x = cx - 27; x <= cx + 27; x += 9) {
            b.put(x, by + 5, cz + 26, p.accent());
            b.put(x, by + 6, cz + 26, p.light());
        }

        // The sails: a stepped row of white shells, tallest near the centre. Each shell is a
        // leaning, curved sheet — vertical ribs whose height follows an arc and whose tops lean
        // back over the podium, giving the Opera House's stacked-arc silhouette.
        int[] sxc = {cx - 21, cx - 7, cx + 8, cx + 22};
        int[] shw = {8, 12, 10, 6};
        int[] sht = {44, 76, 60, 32};
        int[] sz0 = {cz + 6, cz + 11, cz + 9, cz + 5};
        for (int s = 0; s < sxc.length; s++) {
            int halfW = shw[s];
            int height = sht[s];
            int zBase = sz0[s];
            int lean = height / 7;                       // taller shells lean further back
            for (int dx = -halfW; dx <= halfW; dx++) {
                double u = dx / (double) halfW;          // -1 .. 1
                double frac = 1.0 - u * u;               // curved shell profile
                int hEdge = (int) Math.round(height * frac);
                if (hEdge < 1) {
                    hEdge = 1;
                }
                for (int y = 0; y <= hEdge; y++) {
                    int zz = zBase + (int) Math.round((double) y * lean / height);
                    b.put(sxc[s] + dx, by + 2 + y, zz, shell);
                    b.put(sxc[s] + dx, by + 2 + y, zz - 1, y == 0 ? shade : shell);
                }
            }
            // A short finial mast and beacon at each peak.
            int topY = by + 3 + height;
            for (int k = 0; k < 2; k++) {
                b.put(sxc[s], topY + k, zBase + lean, p.accent());
            }
            b.put(sxc[s], topY + 2, zBase + lean, p.light());
        }
    }

    /** One curved shell: horizontal rows of tapering width, leaning as they rise. */
    private static void sail(StructureBuilder b, int cx, int cz, int baseY, int r, int h, Palette p) {
        BlockState shell = Materials.whiteConcrete();
        for (int i = 0; i < h; i++) {
            int xr = r - (i * r) / h;            // the arc: widest at the base
            int z = cz + (i * r) / (h + 3);      // the lean, back from the concourse
            b.fill(cx - xr, baseY + i, z, cx + xr, baseY + i, z, shell);
            b.put(cx - xr, baseY + i, z + 1, p.roofSlab());   // dark tile lip
            b.put(cx + xr, baseY + i, z + 1, p.roofSlab());
        }
        for (int k = 0; k < 3; k++) {            // mast
            b.put(cx, baseY + h + k, cz + r / 2, p.accent());
        }
        b.put(cx, baseY + h + 3, cz + r / 2, p.light());
    }

    @Override
    public void flourish(StructureBuilder b, RandomSource rng, int x, int y0, int z,
                         int x1, int y1, int z1, Palette p) {
        if (x1 - x < 4 || y1 - y0 < 4) {
            return;                              // too small for a terrace front
        }
        int by = y0 + 2;
        // Iron-lace balcony across the street (south) face.
        b.fill(x, by, z1, x1, by, z1, p.roofSlab());
        b.fill(x, by + 1, z1, x1, by + 1, z1, p.rail());
        for (int xx = x; xx <= x1; xx += 3) {
            b.put(xx, by, z1, p.accent());
            b.put(xx, by + 1, z1, p.accent());
        }
        // Wrought-iron lamp bracket at the corner.
        b.put(x, by + 2, z1, p.rail());
        b.put(x, by + 3, z1, p.light());
    }

    @Override
    public void streetProps(StructureBuilder b, RandomSource rng, int cx, int cz,
                            int district, int ground, Palette p) {
        // Harbour boardwalk: a jetty of mooring posts along the water edge.
        int zEdge = cz + district - 12;
        for (int dx = -district + 20; dx <= district - 20; dx += 4) {
            int px = cx + dx;
            b.ground(px, zEdge, px + 3, zEdge + 3, ground, ground, p.ground());
            b.put(px, ground + 1, zEdge, p.accent());
            b.put(px, ground + 2, zEdge, p.rail());
        }
        // Gas lamps down the two main axes.
        for (int d = 30; d <= district - 10; d += 16) {
            StyleKit.lanternPost(b, cx + d, cz + 3, ground, p);
            StyleKit.lanternPost(b, cx - d, cz - 3, ground, p);
        }
        // Two statues on the approaches.
        StyleKit.statue(b, cx + 12, cz - 8, ground, p);
        StyleKit.statue(b, cx - 12, cz + 8, ground, p);
    }
}
