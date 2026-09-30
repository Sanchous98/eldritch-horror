package com.sanchous98.eldritchhorror.registry.blocks;

import com.sanchous98.eldritchhorror.registry.ModBlocks;

/** Blocks forming and anchoring rifts in the veil. Stub blocks; no behaviour yet. */
public final class RiftBlocks {
    private RiftBlocks() {}

    public static void init() {
        // Frame surrounding a veil gate portal.
        ModBlocks.add("veil_gate_frame", p -> p.strength(4.0f, 9.0f));
        // Anchor that pins a rift open; glows faintly.
        ModBlocks.add("rift_anchor", p -> p.strength(6.0f, 12.0f).lightLevel(s -> 7));
        // Massive monolith standing at a rift site.
        ModBlocks.add("black_monolith", p -> p.strength(8.0f, 12.0f));
    }
}
