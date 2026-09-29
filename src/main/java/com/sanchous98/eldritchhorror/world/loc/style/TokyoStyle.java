package com.sanchous98.eldritchhorror.world.loc.style;

import com.sanchous98.eldritchhorror.world.loc.Palette;
import com.sanchous98.eldritchhorror.world.loc.StructureBuilder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;

/**
 * Tokyo — the reference cultural style. Japanese: white shikkui plaster with dark timber, grey
 * kawara tile roofs with flared eaves, vermilion gates, paper lanterns and neon signage.
 *
 * <p>Landmark: a five-level pagoda. Street props: torii gates and lantern posts. This is the
 * pattern every other style copies.
 */
public final class TokyoStyle implements CityStyle {

    @Override
    public String id() {
        return "japanese";
    }

    @Override
    public Palette palette(int koppenClass, boolean coastal) {
        // Culture leads: shikkui + timber + kawara, regardless of Köppen. Climate only nudges
        // the overgrowth (wetter → vines); the coast swaps the overgrowth for kelp.
        Palette bio = Palette.fromBiome(koppenClass, coastal);
        return new Palette(
                Blocks.GRAVEL.defaultBlockState(),                  // ground: raked gravel street
                Blocks.POLISHED_ANDESITE.defaultBlockState(),       // foundation
                Materials.whiteConcrete(),                          // wall: shikkui plaster
                Blocks.CALCITE.defaultBlockState(),                 // weathered plaster
                Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState(),   // accent: dark timber beams
                Blocks.DEEPSLATE_TILES.defaultBlockState(),         // roof: kawara tile
                Blocks.DEEPSLATE_TILE_STAIRS.defaultBlockState(),   // roof stairs
                Blocks.DEEPSLATE_TILE_SLAB.defaultBlockState(),     // roof slabs / eaves
                Blocks.GLASS_PANE.defaultBlockState(),              // window
                Blocks.SPRUCE_TRAPDOOR.defaultBlockState(),         // frame: koushi lattice
                Blocks.DARK_OAK_DOOR.defaultBlockState(),           // door
                Blocks.DARK_OAK_FENCE.defaultBlockState(),          // rail
                Blocks.SHROOMLIGHT.defaultBlockState(),             // light: paper lantern glow
                coastal ? Blocks.KELP.defaultBlockState() : bio.overgrowth(),
                Blocks.GRAVEL.defaultBlockState());                 // rubble: raked gravel
    }

    @Override
    public void flourish(StructureBuilder b, RandomSource rng, int x, int y0, int z,
                         int x1, int y1, int z1, Palette p) {
        // The defining Japanese look: dark timber beams framing pale shikkui walls.
        // Horizontal sill and lintel courses all round, plus a vertical post at one corner.
        for (int y = y0; y <= y0 + 1 && y <= y1; y++) {
            b.fill(x, y, z, x1, y, z, p.accent());
            b.fill(x, y, z1, x1, y, z1, p.accent());
            b.fill(x, y, z, x, y, z1, p.accent());
            b.fill(x1, y, z, x1, y, z1, p.accent());
        }
        for (int y = y0; y < y1; y += 2) {
            b.fill(x, y, z, x1, y, z, p.accent());
            b.fill(x, y, z1, x1, y, z1, p.accent());
        }
        // A cherry-blossom accent tree beside some buildings.
        if (rng.nextFloat() < 0.3f) {
            int tx = x - 2;
            int tz = z - 2;
            int gy = b.groundY(tx, tz);
            b.put(tx, gy + 1, tz, Blocks.CHERRY_LOG.defaultBlockState());
            b.put(tx, gy + 2, tz, Blocks.CHERRY_LOG.defaultBlockState());
            b.fill(tx - 1, gy + 3, tz - 1, tx + 1, gy + 3, tz + 1,
                    Blocks.CHERRY_LEAVES.defaultBlockState());
            b.put(tx, gy + 4, tz, Blocks.CHERRY_LEAVES.defaultBlockState());
        }
    }

    @Override
    public void landmark(StructureBuilder b, RandomSource rng, int cx, int cz, int ground, Palette p) {
        StyleKit.pagoda(b, cx, cz, ground + 1, 5, p);
    }

    @Override
    public void streetProps(StructureBuilder b, RandomSource rng, int cx, int cz,
                            int district, int ground, Palette p) {
        // Torii on the four approaches to the plaza.
        StyleKit.torii(b, cx, cz - district + 20, ground, p);
        StyleKit.torii(b, cx, cz + district - 20, ground, p);
        StyleKit.torii(b, cx - district + 20, cz, ground, p);
        StyleKit.torii(b, cx + district - 20, cz, ground, p);
        // Lantern posts down the two main axes.
        for (int d = 30; d <= district - 10; d += 14) {
            StyleKit.lanternPost(b, cx + d, cz + 3, ground, p);
            StyleKit.lanternPost(b, cx - d, cz - 3, ground, p);
        }
    }
}
