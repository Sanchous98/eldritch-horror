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
        // Sandstone concourse with a low, heavy pitched entry roof.
        b.ground(cx - 18, cz - 14, cx + 18, cz + 16, ground, ground, p.foundation());
        b.room(cx - 14, by, cz - 10, cx + 14, by + 3, cz + 10);
        b.pitchedRoof(cx - 14, cz - 10, cx + 14, cz + 10, by + 3, 2, 0);
        // Three leaning shells, the tallest at the centre, out toward the water.
        sail(b, cx - 9, cz + 2, by + 4, 7, 20, p);
        sail(b, cx + 9, cz + 2, by + 4, 7, 20, p);
        sail(b, cx, cz + 4, by + 4, 9, 26, p);
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
