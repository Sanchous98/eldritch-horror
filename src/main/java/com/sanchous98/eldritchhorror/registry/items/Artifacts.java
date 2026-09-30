package com.sanchous98.eldritchhorror.registry.items;

import com.sanchous98.eldritchhorror.registry.ModItems;

/**
 * Artefacts — unique-effect items with a matching drawback (sidegrades, never pure upgrades).
 *
 * <p>Each entry records its <b>effect</b> and its <b>drawback</b> in its comment, per
 * {@code design/16-items.md} and {@code design/26-rituals-and-occult.md}. Artefacts are
 * signature pieces: they stack to 1. Effects are recorded per item; behaviour not wired yet.
 */
public final class Artifacts {
    private Artifacts() {}

    public static void init() {
        // Effect: see rifts and entity effects through walls. Drawback: sanity drains while equipped.
        ModItems.add("eye_of_the_watcher", 1);
        // Effect: breathe underwater indefinitely. Drawback: corruption + slowly.
        ModItems.add("drowned_lung", 1);
        // Effect: reveals a nearby rite or secret. Drawback: occasional sanity jolt.
        ModItems.add("mirror_shard", 1);
        // Effect: -40% corruption spread within 16 blocks. Drawback: -10% ritual power.
        ModItems.add("warden_sigil", 1);
        // Effect: +25% summoning power. Drawback: villagers flee; sanity floor is lower.
        ModItems.add("prophet_mask", 1);
        // Effect: suppresses darkness/night sanity drain in a radius. Drawback: its light draws eldritch attention.
        ModItems.add("lantern_of_steadfast", 1);
        // Effect: halves rift/Veil sanity drain while worn. Drawback: cleansing rites recover less of your corruption.
        ModItems.add("wardstone_pendant", 1);
        // Effect: points to the nearest ward or safe settlement node. Drawback: spins wildly near rifts and drains sanity.
        ModItems.add("compass_of_quiet", 1);
        // Effect: +20% ritual casting speed and -sanity cost. Drawback: each rite cast adds +1 corruption.
        ModItems.add("ring_of_focus", 1);
        // Effect: stores one rite outcome and re-casts it later as an echo. Drawback: each echo adds corruption.
        ModItems.add("chalice_of_echoes", 1);
        // Effect: see corruption fields, hidden nodes and Veil seams through the Veil. Drawback: prolonged use drains sanity.
        ModItems.add("veil_lens", 1);
        // Effect: absorbs one fatal blow, then breaks. Drawback: absorbing adds corruption and a sanity jolt.
        ModItems.add("bone_charm", 1);
    }
}
