package com.sanchous98.eldritchhorror.world.loc.style;

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Colour-safe block access for styles. <b>Minecraft 26.3 changed coloured blocks</b>: there is
 * no longer a {@code Blocks.WHITE_CONCRETE}; dyed blocks are a
 * {@link net.minecraft.world.level.block.ColorCollection} and are picked by
 * {@link DyeColor}. Styles must use these helpers instead of guessing constant names.
 *
 * <p>All return a {@link BlockState}. Central code — do not copy.
 */
public final class Materials {

    private Materials() {
    }

    /** Dyed concrete, e.g. {@code concrete(DyeColor.WHITE)} (shikkui plaster). */
    public static BlockState concrete(DyeColor c) {
        return Blocks.CONCRETE.pick(c).defaultBlockState();
    }

    /** Dyed terracotta (brick / clay walls, roofs). */
    public static BlockState terracotta(DyeColor c) {
        return Blocks.DYED_TERRACOTTA.pick(c).defaultBlockState();
    }

    /** Glazed terracotta (patterned panels). */
    public static BlockState glazed(DyeColor c) {
        return Blocks.GLAZED_TERRACOTTA.pick(c).defaultBlockState();
    }

    /** Wool (fabric, awnings, banners-as-blocks). */
    public static BlockState wool(DyeColor c) {
        return Blocks.WOOL.pick(c).defaultBlockState();
    }

    /** Stained glass (coloured glazing, neon back-panels). */
    public static BlockState stainedGlass(DyeColor c) {
        return Blocks.STAINED_GLASS.pick(c).defaultBlockState();
    }

    /** Stained glass pane (coloured windows). */
    public static BlockState stainedPane(DyeColor c) {
        return Blocks.STAINED_GLASS_PANE.pick(c).defaultBlockState();
    }

    // Convenience shorthands for common colours --------------------------------------------

    public static BlockState whiteConcrete() {
        return concrete(DyeColor.WHITE);
    }

    public static BlockState lightGrayConcrete() {
        return concrete(DyeColor.LIGHT_GRAY);
    }

    public static BlockState redTerracotta() {
        return terracotta(DyeColor.RED);
    }
}
