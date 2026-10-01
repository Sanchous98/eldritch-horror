package com.sanchous98.eldritchhorror.registry.blocks;

import com.sanchous98.eldritchhorror.registry.ModBlocks;
import com.sanchous98.eldritchhorror.registry.ModItems;

/** Ritual blocks used to build altars and chalk circles. The altar core is interactive. */
public final class RitualBlocks {
    private RitualBlocks() {}

    public static void init() {
        // Chalk circle marker drawn on the ground.
        ModBlocks.add("ritual_chalk", p -> p.strength(0.5f));
        // Carved stone bearing a rune.
        ModBlocks.add("rune_stone", p -> p.strength(3.0f, 6.0f));
        // Altar centerpiece; emits a soft glow and reports the rites the user knows on use.
        var altar = ModBlocks.BLOCKS.registerBlock("altar_core", RitualAltarBlock::new,
                p -> p.strength(5.0f, 9.0f).lightLevel(s -> 6));
        ModBlocks.ALL.add(altar);
        ModItems.ALL.add(ModItems.ITEMS.registerSimpleBlockItem("altar_core", altar));
        // Offering table for sacrifices.
        ModBlocks.add("sacrificial_altar", p -> p.strength(5.0f, 9.0f));
        // Small bowl that holds offerings.
        ModBlocks.add("offering_bowl", p -> p.strength(1.5f));
        // Burning brazier providing light.
        ModBlocks.add("incense_brazier", p -> p.strength(3.0f).lightLevel(s -> 12));
        // Cluster of lit candles.
        ModBlocks.add("candle_cluster", p -> p.strength(0.5f).lightLevel(s -> 12));
    }
}
