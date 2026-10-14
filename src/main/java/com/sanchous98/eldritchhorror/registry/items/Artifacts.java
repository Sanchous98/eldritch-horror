package com.sanchous98.eldritchhorror.registry.items;

import com.sanchous98.eldritchhorror.registry.ModItems;

/**
 * Artefacts — unique-effect items with a matching drawback (sidegrades, never pure upgrades).
 *
 * <p>Each entry records its <b>effect</b> and its <b>drawback</b> in its comment, per
 * {@code design/16-items.md} and {@code design/26-rituals-and-occult.md}. Artefacts are signature
 * pieces: they stack to 1.
 *
 * <p>Four passives are wired now. {@code eye_of_the_watcher} and {@code drowned_lung} are their own
 * item classes; {@code ring_of_focus} and {@code warden_sigil} stay plain items because their hooks
 * live in the systems they shorten — {@code rite/RiteEngine} and {@code corruption/TaintSystem}
 * read them through {@link CarriedItems#carries}, where "carried" means anywhere in the inventory
 * (the mod has no trinket slot; the tooltips say so). The rest are flavour.
 */
public final class Artifacts {
    private Artifacts() {}

    public static void init() {
        // Eye of the Watcher — use: points at the nearest rift you have discovered. Passive:
        // sanity −0.1/s while carried. flavour (design/16): see-through-walls rendering is a client feature.
        ModItems.addItem("eye_of_the_watcher", EyeOfTheWatcherItem::new, 1);
        // Drowned Lung — use: Water Breathing 8:00 and corruption +2; reusable (not consumed).
        ModItems.addItem("drowned_lung", DrownedLungItem::new, 1);
        // Mirror Shard — reveals a nearby rite or secret. flavour (design/16): no "nearest secret" registry to read.
        ModItems.add("mirror_shard", 1);
        // Warden's Sigil — passive: −40% corruption spread within 16 blocks while carried
        // (corruption/TaintSystem.spread). flavour (design/16): "−10% ritual power" has no hook.
        ModItems.add("warden_sigil", 1);
        // Prophet's Mask — +25% summoning power. flavour (design/16): no summon-power hook exists.
        ModItems.add("prophet_mask", 1);
        // Lantern of the Steadfast — suppresses darkness/night sanity drain in a radius.
        // flavour (design/16): the darkness source is private to sanity/SanitySources, which this pass
        // may not touch, so the suppression cannot be expressed in ≤30 lines.
        ModItems.add("lantern_of_steadfast", 1);
        // Wardstone Pendant — halves rift/Veil sanity drain while worn. flavour (design/16): no rift
        // sanity source exists to halve (rift pressure is damage + taint, not sanity).
        ModItems.add("wardstone_pendant", 1);
        // Compass of Quiet — points to the nearest ward or safe settlement. flavour (design/16): no ward registry.
        ModItems.add("compass_of_quiet", 1);
        // Ring of Focus — passive: ritual cooldown ×0.8 while carried (rite/RiteEngine.perform).
        // flavour (design/16): the −sanity cost and +1 corruption per cast are not wired.
        ModItems.add("ring_of_focus", 1);
        // Chalice of Echoes — stores one rite outcome and re-casts it. flavour (design/16): no rite-echo store exists.
        ModItems.add("chalice_of_echoes", 1);
        // Veil Lens — see corruption fields and Veil seams. flavour (design/16): client rendering, no server hook.
        ModItems.add("veil_lens", 1);
        // Bone Charm — absorbs one fatal blow, then breaks. flavour (design/16): no death hook in this pass.
        ModItems.add("bone_charm", 1);
    }
}
