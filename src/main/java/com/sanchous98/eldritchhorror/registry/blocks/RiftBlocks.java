package com.sanchous98.eldritchhorror.registry.blocks;

import com.sanchous98.eldritchhorror.registry.ModBlocks;
import com.sanchous98.eldritchhorror.registry.ModItems;

/** Blocks forming and anchoring rifts. Stub blocks; no behaviour yet. */
public final class RiftBlocks {
    private RiftBlocks() {}

    public static void init() {
        // Frame surrounding a rift gate portal.
        ModBlocks.add("veil_gate_frame", p -> p.strength(4.0f, 9.0f));
        // Anchor that pins a rift open; glows faintly. The custom class tells the world's rift
        // ledger when the anchor is destroyed, however that happens (see RiftAnchorBlock).
        var anchor = ModBlocks.BLOCKS.registerBlock("rift_anchor", RiftAnchorBlock::new,
                p -> p.strength(6.0f, 12.0f).lightLevel(s -> 7));
        ModBlocks.ALL.add(anchor);
        ModItems.ALL.add(ModItems.ITEMS.registerSimpleBlockItem("rift_anchor", anchor));
        // Massive monolith standing at a rift site.
        ModBlocks.add("black_monolith", p -> p.strength(8.0f, 12.0f));
    }
}
