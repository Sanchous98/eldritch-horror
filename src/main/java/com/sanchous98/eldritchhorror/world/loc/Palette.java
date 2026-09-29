package com.sanchous98.eldritchhorror.world.loc;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The frozen palette vocabulary: locations never hardcode blocks, they pick blocks from here.
 *
 * <p>A <b>dark-gothic core with regional flavour</b>: every city reads as brooding architecture,
 * but the materials and overgrowth shift with the real Köppen–Geiger class at the site (plus a
 * coastal modifier). See {@code docs/STRUCTURES-CONTRACT.md} § Palettes.
 */
public record Palette(
        BlockState ground,       // path / plaza surface
        BlockState foundation,   // building base course
        BlockState wall,         // primary wall block
        BlockState weathered,    // cracked / mossy variant of `wall`
        BlockState accent,       // trim, pillars, quoins
        BlockState roof,         // solid roof block
        BlockState roofStairs,   // pitched-roof stairs
        BlockState roofSlab,     // pitched-roof slabs / eaves
        BlockState window,       // glazing (pane-like)
        BlockState frame,        // window frame / mullions
        BlockState door,         // door / gate
        BlockState rail,         // railing / fence
        BlockState light,        // lantern / candle / torch
        BlockState overgrowth,   // vines / leaves / moss (nullable = none)
        BlockState rubble         // gravel / coarse dirt / debris
) {

    /** Shorthand for {@code block.defaultBlockState()}. */
    private static BlockState s(Block block) {
        return block.defaultBlockState();
    }

    /**
     * The universal decay block — cobwebs in the corners of abandoned rooms. Not region-specific,
     * so it is a helper rather than a palette field (keeps the 15-field record frozen).
     */
    public static BlockState cobweb() {
        return s(Blocks.COBWEB);
    }

    /**
     * Palette for the real Köppen class (1..30, 0 = ocean/unclassified) and whether the site is
     * coastal. Köppen families (A/B/C/D/E) pick the materials; the coastal modifier adds prismarine
     * and kelp on top.
     */
    public static Palette fromBiome(int koppenClass, boolean coastal) {
        BlockState ground;
        BlockState foundation;
        BlockState wall;
        BlockState weathered;
        BlockState accent;
        BlockState roof;
        BlockState roofStairs;
        BlockState roofSlab;
        BlockState window = s(Blocks.GLASS_PANE);
        BlockState frame;
        BlockState door;
        BlockState rail;
        BlockState light = s(Blocks.LANTERN);
        BlockState overgrowth;
        BlockState rubble = s(Blocks.GRAVEL);

        if (koppenClass >= 1 && koppenClass <= 3) {
            // A — tropical rainforest / monsoon / savannah: mossy stone, jungle timber.
            ground = s(Blocks.MOSSY_COBBLESTONE);
            foundation = s(Blocks.MOSSY_STONE_BRICKS);
            wall = s(Blocks.MOSSY_STONE_BRICKS);
            weathered = s(Blocks.MOSSY_COBBLESTONE);
            accent = s(Blocks.STRIPPED_JUNGLE_LOG);
            roof = s(Blocks.JUNGLE_PLANKS);
            roofStairs = s(Blocks.JUNGLE_STAIRS);
            roofSlab = s(Blocks.JUNGLE_SLAB);
            frame = s(Blocks.JUNGLE_PLANKS);
            door = s(Blocks.JUNGLE_DOOR);
            rail = s(Blocks.JUNGLE_FENCE);
            overgrowth = s(Blocks.VINE);
        } else if (koppenClass >= 4 && koppenClass <= 7) {
            // B — arid desert / steppe: pale sandstone shell, blackstone trim, dead bush.
            ground = s(Blocks.SAND);
            foundation = s(Blocks.CUT_SANDSTONE);
            wall = s(Blocks.SANDSTONE);
            weathered = s(Blocks.CUT_SANDSTONE);
            accent = s(Blocks.POLISHED_BLACKSTONE_BRICKS);
            roof = s(Blocks.DARK_OAK_PLANKS);
            roofStairs = s(Blocks.DARK_OAK_STAIRS);
            roofSlab = s(Blocks.DARK_OAK_SLAB);
            frame = s(Blocks.ACACIA_PLANKS);
            door = s(Blocks.ACACIA_DOOR);
            rail = s(Blocks.ACACIA_FENCE);
            overgrowth = s(Blocks.DEAD_BUSH);
            rubble = s(Blocks.COARSE_DIRT);
        } else if (koppenClass >= 17 && koppenClass <= 28) {
            // D — continental / boreal: deepslate, spruce, soul-lit and mouldy.
            ground = s(Blocks.COBBLED_DEEPSLATE);
            foundation = s(Blocks.DEEPSLATE_BRICKS);
            wall = s(Blocks.DEEPSLATE_BRICKS);
            weathered = s(Blocks.CRACKED_DEEPSLATE_BRICKS);
            accent = s(Blocks.SPRUCE_LOG);
            roof = s(Blocks.DEEPSLATE_TILES);
            roofStairs = s(Blocks.SPRUCE_STAIRS);
            roofSlab = s(Blocks.SPRUCE_SLAB);
            frame = s(Blocks.SPRUCE_PLANKS);
            door = s(Blocks.SPRUCE_DOOR);
            rail = s(Blocks.SPRUCE_FENCE);
            light = s(Blocks.SOUL_LANTERN);
            overgrowth = s(Blocks.MOSS_BLOCK);
        } else if (koppenClass >= 29 && koppenClass <= 30) {
            // E — polar tundra / frost: stone and deepslate under snow, spruce timber.
            ground = s(Blocks.SNOW_BLOCK);
            foundation = s(Blocks.DEEPSLATE_BRICKS);
            wall = s(Blocks.STONE_BRICKS);
            weathered = s(Blocks.CRACKED_STONE_BRICKS);
            accent = s(Blocks.STRIPPED_SPRUCE_LOG);
            roof = s(Blocks.PACKED_ICE);
            roofStairs = s(Blocks.SPRUCE_STAIRS);
            roofSlab = s(Blocks.SPRUCE_SLAB);
            frame = s(Blocks.SPRUCE_PLANKS);
            door = s(Blocks.SPRUCE_DOOR);
            rail = s(Blocks.SPRUCE_FENCE);
            light = s(Blocks.SOUL_LANTERN);
            overgrowth = null; // snow drift instead of greenery
        } else {
            // C — temperate / oceanic (7..16 and anything unclassified): deepslate and dark oak.
            ground = s(Blocks.COBBLED_DEEPSLATE);
            foundation = s(Blocks.DEEPSLATE_BRICKS);
            wall = s(Blocks.STONE_BRICKS);
            weathered = s(Blocks.CRACKED_STONE_BRICKS);
            accent = s(Blocks.STRIPPED_DARK_OAK_LOG);
            roof = s(Blocks.DEEPSLATE_TILES);
            roofStairs = s(Blocks.DARK_OAK_STAIRS);
            roofSlab = s(Blocks.DARK_OAK_SLAB);
            frame = s(Blocks.DARK_OAK_PLANKS);
            door = s(Blocks.DARK_OAK_DOOR);
            rail = s(Blocks.DARK_OAK_FENCE);
            overgrowth = s(Blocks.VINE);
        }

        if (coastal) {
            // Harbour sites read differently: prismarine trim and kelp growth on top of the region.
            accent = s(Blocks.PRISMARINE_BRICKS);
            weathered = s(Blocks.DARK_PRISMARINE);
            overgrowth = s(Blocks.KELP);
        }
        return new Palette(ground, foundation, wall, weathered, accent, roof, roofStairs, roofSlab,
                window, frame, door, rail, light, overgrowth, rubble);
    }
}
