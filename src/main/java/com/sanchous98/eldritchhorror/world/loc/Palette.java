package com.sanchous98.eldritchhorror.world.loc;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The frozen palette vocabulary: locations never hardcode blocks, they pick blocks from here.
 * A desert city and a harbour city therefore differ without hand-authoring.
 *
 * <p>Derived from the real Köppen–Geiger class at the site plus whether the site is coastal
 * (see {@link #fromBiome(int, boolean)}).
 */
public record Palette(
        BlockState surface,   // ground / paths
        BlockState wall,      // primary wall block
        BlockState accent,    // trim / corners
        BlockState roof,      // roof / upper
        BlockState window,    // glazing (glass-like)
        BlockState light,     // light source
        BlockState door       // door / gate
) {

    /** Shorthand for {@code block.defaultBlockState()}. */
    private static BlockState s(Block block) {
        return block.defaultBlockState();
    }

    /**
     * Palette for the real Köppen class (1..30, 0 = ocean/unclassified) and whether the site is
     * coastal. Köppen families are grouped; coastal sites get prismarine trim.
     */
    public static Palette fromBiome(int koppenClass, boolean coastal) {
        BlockState surface;
        BlockState wall;
        BlockState accent;
        BlockState roof;
        BlockState window = s(Blocks.GLASS);
        BlockState light = s(Blocks.LANTERN);
        BlockState door = s(Blocks.OAK_DOOR);

        switch (koppenClass) {
            case 4, 5, 6, 7 -> {          // arid: hot/cold desert and steppe
                surface = s(Blocks.SAND);
                wall = s(Blocks.SMOOTH_SANDSTONE);
                accent = s(Blocks.CUT_SANDSTONE);
                roof = s(Blocks.SANDSTONE);
            }
            case 1, 2, 3 -> {             // tropical rainforest / monsoon / savannah
                surface = s(Blocks.COBBLESTONE);
                wall = s(Blocks.MUD_BRICKS);
                accent = s(Blocks.STRIPPED_JUNGLE_LOG);
                roof = s(Blocks.DARK_OAK_PLANKS);
                door = s(Blocks.JUNGLE_DOOR);
            }
            case 8, 9, 10 -> {            // Mediterranean
                surface = s(Blocks.COBBLESTONE);
                wall = s(Blocks.TERRACOTTA);
                accent = s(Blocks.STRIPPED_OAK_LOG);
                roof = s(Blocks.BRICKS);
            }
            case 29, 30 -> {              // polar tundra / frost
                surface = s(Blocks.SNOW_BLOCK);
                wall = s(Blocks.SPRUCE_PLANKS);
                accent = s(Blocks.PACKED_ICE);
                roof = s(Blocks.DEEPSLATE_TILES);
                light = s(Blocks.SEA_LANTERN);
                door = s(Blocks.SPRUCE_DOOR);
            }
            default -> {                  // temperate
                surface = s(Blocks.COBBLESTONE);
                wall = s(Blocks.OAK_PLANKS);
                accent = s(Blocks.STRIPPED_OAK_LOG);
                roof = s(Blocks.BRICKS);
            }
        }

        if (coastal) {
            // Harbour sites read differently: stone footing and prismarine trim.
            surface = s(Blocks.STONE_BRICKS);
            accent = s(Blocks.PRISMARINE_BRICKS);
        }
        return new Palette(surface, wall, accent, roof, window, light, door);
    }
}
