package com.sanchous98.eldritchhorror.world.loc.style;

import com.sanchous98.eldritchhorror.world.loc.Palette;
import com.sanchous98.eldritchhorror.world.loc.StructureBuilder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Buenos Aires — cultural city style. Mediterranean / porteno: pastel rendered plaster (rose, blue,
 * ochre) with white trim, orange terracotta tile roofs, wrought-iron balconies, grand avenues and
 * the colourful painted houses of La Boca.
 *
 * <p>Landmark: a grand beaux-arts palace/congress — a colonnaded portico under a high slate dome
 * (Congress, the Colón). Street props: iron lamp posts, statues and the Obelisk on the avenue.
 * Flourishes: wrought-iron balconies and colourful La Boca shopfronts. Deterministic and
 * cheap; uses only {@link StructureBuilder} + {@link Palette} (via {@link StyleKit}/{@link Materials}).
 */
public final class BuenosAiresStyle implements CityStyle {

    @Override
    public String id() {
        return "buenosaires";
    }

    @Override
    public Palette palette(int koppenClass, boolean coastal) {
        // Culture leads: pastel rendered plaster + terracotta tile, whatever the Köppen class.
        // Climate only varies the damp overgrowth (moss/vines, kelp on the coast).
        Palette bio = Palette.fromBiome(koppenClass, coastal);
        return new Palette(
                Blocks.SMOOTH_STONE.defaultBlockState(),            // ground: paved grand avenue
                Blocks.CUT_SANDSTONE.defaultBlockState(),           // foundation: rendered warm base course
                Materials.terracotta(DyeColor.PINK),                // wall: pastel rose plaster render
                Materials.terracotta(DyeColor.WHITE),               // weathered: sun-faded plaster
                Materials.whiteConcrete(),                          // accent: white trim / quoins
                Materials.terracotta(DyeColor.ORANGE),              // roof: orange terracotta tile
                Blocks.BRICK_STAIRS.defaultBlockState(),            // roof stairs
                Blocks.BRICK_SLAB.defaultBlockState(),              // roof slabs / eaves
                Blocks.GLASS_PANE.defaultBlockState(),              // window: tall French glazing
                Blocks.BIRCH_TRAPDOOR.defaultBlockState(),          // frame: pale timber mullions
                Blocks.DARK_OAK_DOOR.defaultBlockState(),           // door: dark porte-cochère
                Blocks.IRON_BARS.defaultBlockState(),               // rail: wrought-iron balcony
                Blocks.LANTERN.defaultBlockState(),                 // light: gas/electric lamp
                bio.overgrowth(),
                Blocks.GRAVEL.defaultBlockState());                 // rubble
    }

    @Override
    public void landmark(StructureBuilder b, RandomSource rng, int cx, int cz, int ground, Palette p) {
        // El Obelisco de Buenos Aires: a tall, slender, plain white square obelisk rising from a
        // small stone base on a paved plaza. The city's cleanest, most minimal skyline mark.
        b.ground(cx - 8, cz - 8, cx + 8, cz + 8, ground, ground, p.ground());

        // Small two-tier base.
        b.fill(cx - 3, ground + 1, cz - 3, cx + 3, ground + 1, cz + 3, p.foundation());
        b.fill(cx - 2, ground + 2, cz - 2, cx + 2, ground + 2, cz + 2, p.accent());

        // The clean 3x3 white shaft (dominant, ~60 blocks).
        int shaftBase = ground + 3;
        int shaftTop = ground + 62;
        for (int y = shaftBase; y <= shaftTop; y++) {
            b.fill(cx - 1, y, cz - 1, cx + 1, y, cz + 1, p.accent());
        }

        // A shallow cornice, then the small stepped pyramidion.
        b.fill(cx - 2, shaftTop + 1, cz - 2, cx + 2, shaftTop + 1, cz + 2, p.accent());
        b.fill(cx - 1, shaftTop + 2, cz - 1, cx + 1, shaftTop + 2, cz + 1, p.accent());
        b.put(cx, shaftTop + 3, cz, p.accent());
    }



    @Override
    public void flourish(StructureBuilder b, RandomSource rng, int x, int y0, int z,
                         int x1, int y1, int z1, Palette p) {
        // Colourful La Boca painted shopfront on a ground face.
        if (rng.nextFloat() < 0.5f) {
            shopfront(b, rng.nextInt(4), x, y0, z, x1, z1, pastel(rng), p);
        }
        // Wrought-iron balcony on an upper face.
        if (rng.nextFloat() < 0.6f) {
            balcony(b, rng.nextInt(4), y1 - 2, x, z, x1, z1, p);
        }
    }

    @Override
    public void streetProps(StructureBuilder b, RandomSource rng, int cx, int cz,
                            int district, int ground, Palette p) {
        // Iron lamp posts down both grand avenues.
        for (int d = 26; d <= district - 8; d += 12) {
            StyleKit.lanternPost(b, cx + d, cz + 3, ground, p);
            StyleKit.lanternPost(b, cx - d, cz - 3, ground, p);
            StyleKit.lanternPost(b, cx + 3, cz + d, ground, p);
            StyleKit.lanternPost(b, cx - 3, cz - d, ground, p);
        }
        // The Obelisk on the avenue, clear of the palace front.
        StyleKit.obelisk(b, cx, cz + district - 30, ground, p);
        // Statues flanking the avenue approaches.
        StyleKit.statue(b, cx - district / 2, cz, ground, p);
        StyleKit.statue(b, cx + district / 2, cz, ground, p);
    }

    // ------------------------------------------------------------------ helpers

    /** A painted panel and a projecting awning band on one ground-level face. */
    private static void shopfront(StructureBuilder b, int side, int x, int y0, int z,
                                  int x1, int z1, BlockState paint, Palette p) {
        switch (side) {
            case 0 -> {
                b.fill(x + 1, y0 + 1, z, x1 - 1, y0 + 2, z, paint);
                b.fill(x, y0 + 3, z, x1, y0 + 3, z, p.rail());
            }
            case 1 -> {
                b.fill(x + 1, y0 + 1, z1, x1 - 1, y0 + 2, z1, paint);
                b.fill(x, y0 + 3, z1, x1, y0 + 3, z1, p.rail());
            }
            case 2 -> {
                b.fill(x, y0 + 1, z + 1, x, y0 + 2, z1 - 1, paint);
                b.fill(x, y0 + 3, z, x, y0 + 3, z1, p.rail());
            }
            default -> {
                b.fill(x1, y0 + 1, z + 1, x1, y0 + 2, z1 - 1, paint);
                b.fill(x1, y0 + 3, z, x1, y0 + 3, z1, p.rail());
            }
        }
    }

    /** A wrought-iron railing band set into one upper face. */
    private static void balcony(StructureBuilder b, int side, int y, int x, int z, int x1, int z1, Palette p) {
        switch (side) {
            case 0 -> b.fill(x + 1, y, z, x1 - 1, y, z, p.rail());
            case 1 -> b.fill(x + 1, y, z1, x1 - 1, y, z1, p.rail());
            case 2 -> b.fill(x, y, z + 1, x, y, z1 - 1, p.rail());
            default -> b.fill(x1, y, z + 1, x1, y, z1 - 1, p.rail());
        }
    }

    /** A bright pastel paint block for a La Boca facade (colour-safe via {@link Materials}). */
    private static BlockState pastel(RandomSource rng) {
        return switch (rng.nextInt(6)) {
            case 0 -> Materials.terracotta(DyeColor.PINK);
            case 1 -> Materials.glazed(DyeColor.LIGHT_BLUE);   // azulejo blue
            case 2 -> Materials.concrete(DyeColor.YELLOW);
            case 3 -> Materials.terracotta(DyeColor.ORANGE);   // terracotta render
            case 4 -> Materials.concrete(DyeColor.WHITE);
            default -> Materials.terracotta(DyeColor.LIGHT_BLUE);
        };
    }
}
