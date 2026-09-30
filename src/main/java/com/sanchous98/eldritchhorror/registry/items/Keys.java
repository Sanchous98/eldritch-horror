package com.sanchous98.eldritchhorror.registry.items;

import com.sanchous98.eldritchhorror.registry.ModItems;

/** Progression and endgame key items; non-stacking. Stub content; behaviour not wired yet. */
public final class Keys {
    private Keys() {
    }

    public static void init() {
        // Veil key; opens passages sealed behind the Veil.
        ModItems.add("veil_key", 1);
        // Gate fragment; one piece of the endgame gate assembly.
        ModItems.add("gate_fragment", 1);
        // Banishment focus; catalyses rites that send entities back.
        ModItems.add("banishment_focus", 1);
        // Summoning focus; catalyses rites that call entities through.
        ModItems.add("summoning_focus", 1);
        // Ninth sigil; the final seal in the endgame sequence.
        ModItems.add("ninth_sigil", 1);
        // Hollow crown; endgame regalia of the Hollow path.
        ModItems.add("hollow_crown", 1);
    }
}
