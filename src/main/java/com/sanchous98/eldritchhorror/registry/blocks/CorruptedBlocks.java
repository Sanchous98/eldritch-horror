package com.sanchous98.eldritchhorror.registry.blocks;

import com.sanchous98.eldritchhorror.registry.ModBlocks;

/** Corrupted terrain and seal blocks spread by the eldritch influence. Stub blocks; no behaviour yet. */
public final class CorruptedBlocks {
    private CorruptedBlocks() {}

    public static void init() {
        // Corrupt variant of stone; the base corrupted material.
        ModBlocks.add("eldritch_stone", p -> p.strength(3.0f, 6.0f));
        // Cracked, corrupted stone.
        ModBlocks.add("corrupt_stone", p -> p.strength(3.0f, 6.0f));
        // Soil turned foul by the corruption.
        ModBlocks.add("tainted_soil", p -> p.strength(0.6f));
        // Tile etched with an eldritch glyph.
        ModBlocks.add("glyph_tile", p -> p.strength(3.0f, 6.0f));
        // Bone-laden block of an ossuary.
        ModBlocks.add("ossuary_block", p -> p.strength(2.5f, 6.0f));
        // Stone bearing a warding seal.
        ModBlocks.add("seal_stone", p -> p.strength(4.0f, 9.0f));
    }
}
