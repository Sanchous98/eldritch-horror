package com.sanchous98.eldritchhorror.world.loc.style;

import com.sanchous98.eldritchhorror.world.loc.Palette;
import com.sanchous98.eldritchhorror.world.loc.StructureBuilder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Istanbul — Byzantine + Ottoman. Warm red brick laid on grey limestone, lead-grey domes,
 * slender minarets, arcaded bazaars and arabesque tilework.
 *
 * <p>Landmark: a great central dome flanked by minarets ({@link StyleKit#mosque}). Street props:
 * corner minarets, lantern posts, a bazaar colonnade and a monumental obelisk.
 */
public final class IstanbulStyle implements CityStyle {

    @Override
    public String id() {
        return "istanbul";
    }

    @Override
    public Palette palette(int koppenClass, boolean coastal) {
        // Culture leads: stone base + red brick, lead-grey dome roofs, whatever the climate.
        // Climate only supplies the overgrowth (coast: kelp instead).
        Palette bio = Palette.fromBiome(koppenClass, coastal);
        return new Palette(
                Blocks.STONE_BRICKS.defaultBlockState(),            // ground: paved plaza stone
                Blocks.POLISHED_ANDESITE.defaultBlockState(),       // foundation: grey base course
                Blocks.BRICKS.defaultBlockState(),                  // wall: Ottoman red brick
                Blocks.MUD_BRICKS.defaultBlockState(),              // weathered brick
                Blocks.CHISELED_STONE_BRICKS.defaultBlockState(),   // accent: carved stone trim
                Blocks.DEEPSLATE_TILES.defaultBlockState(),         // roof: lead-grey dome/tile
                Blocks.DEEPSLATE_TILE_STAIRS.defaultBlockState(),   // roof stairs
                Blocks.DEEPSLATE_TILE_SLAB.defaultBlockState(),     // roof slabs / eaves
                Blocks.GLASS_PANE.defaultBlockState(),              // window
                Blocks.POLISHED_ANDESITE.defaultBlockState(),       // frame / mullions
                Blocks.DARK_OAK_DOOR.defaultBlockState(),           // door
                Blocks.IRON_BARS.defaultBlockState(),               // rail / grille
                Blocks.LANTERN.defaultBlockState(),                 // light: oil lantern
                coastal ? Blocks.KELP.defaultBlockState() : bio.overgrowth(),
                Blocks.GRAVEL.defaultBlockState());                 // rubble
    }

    @Override
    public void landmark(StructureBuilder b, RandomSource rng, int cx, int cz, int ground, Palette p) {
        // Hagia Sophia / Blue Mosque silhouette: one huge dome between minarets…
        StyleKit.mosque(b, rng, cx, cz, ground, p);
        // …flanked by two more, echoing the four-minaret Ottoman imperial mosques.
        StyleKit.minaret(b, cx - 11, cz, ground, 18, p);
        StyleKit.minaret(b, cx + 11, cz, ground, 18, p);
    }

    @Override
    public void flourish(StructureBuilder b, RandomSource rng, int x, int y0, int z,
                         int x1, int y1, int z1, Palette p) {
        // Arabesque frieze: a band of glazed tile just below the eaves.
        int band = y1 - 1;
        if (band <= y0) {
            return;
        }
        BlockState tile = Materials.glazed(DyeColor.LIGHT_BLUE);
        for (int i = x; i <= x1; i += 3) {
            b.put(i, band, z, tile);
            b.put(i, band, z1, tile);
        }
        for (int i = z; i <= z1; i += 3) {
            b.put(x, band, i, tile);
            b.put(x1, band, i, tile);
        }
    }

    @Override
    public void streetProps(StructureBuilder b, RandomSource rng, int cx, int cz,
                            int district, int ground, Palette p) {
        // Slender minarets mark the four quarters of the plaza.
        int r = district - 14;
        if (r > 8) {
            StyleKit.minaret(b, cx - r, cz - r, ground, 16, p);
            StyleKit.minaret(b, cx + r, cz - r, ground, 16, p);
            StyleKit.minaret(b, cx - r, cz + r, ground, 16, p);
            StyleKit.minaret(b, cx + r, cz + r, ground, 16, p);
        }
        // Lantern posts down the two main axes.
        for (int d = 24; d <= district - 12; d += 16) {
            StyleKit.lanternPost(b, cx + d, cz + 3, ground, p);
            StyleKit.lanternPost(b, cx - d, cz - 3, ground, p);
        }
        // A covered bazaar arcade along one street; a monument across the plaza.
        colonnade(b, cx, cz + 16, ground, p);
        StyleKit.obelisk(b, cx - 18, cz - 18, ground, p);
    }

    /** A long arcade: paired stone piers carrying a flat roof of slabs. */
    private static void colonnade(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int half = 9;
        for (int x = cx - half; x <= cx + half; x += 3) {
            pier(b, x, cz - 2, ground, p);
            pier(b, x, cz + 2, ground, p);
            b.fill(x - 1, ground + 4, cz - 2, x + 1, ground + 4, cz + 2, p.roofSlab());
        }
    }

    /** One colonnade pier: a stone plinth under a carved accent shaft. */
    private static void pier(StructureBuilder b, int x, int z, int ground, Palette p) {
        b.put(x, ground + 1, z, p.foundation());
        b.put(x, ground + 2, z, p.accent());
        b.put(x, ground + 3, z, p.accent());
    }
}
