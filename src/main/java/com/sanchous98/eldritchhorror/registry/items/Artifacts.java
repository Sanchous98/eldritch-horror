package com.sanchous98.eldritchhorror.registry.items;

import com.sanchous98.eldritchhorror.registry.ModItems;

/**
 * Artefacts — unique-effect items with a matching drawback (sidegrades, never pure upgrades).
 *
 * <p>Each entry records its <b>effect</b> and its <b>drawback</b> in its comment, per
 * {@code design/16-items.md} and {@code design/26-rituals-and-occult.md}. Artefacts are signature
 * pieces: they stack to 1.
 *
 * <p>The artefact hooks live where their system lives: {@code eye_of_the_watcher} and
 * {@code drowned_lung} are their own item classes; {@code ring_of_focus}, {@code warden_sigil} and
 * {@code lantern_of_steadfast} stay plain items because their hooks live in the systems they
 * shorten — {@code rite/RiteEngine}, {@code corruption/TaintSystem} and the darkness source read
 * them through {@link CarriedItems#carries}, where "carried" means anywhere in the inventory (the
 * mod has no trinket slot; the tooltips say so). The rest have their own classes or events.
 */
public final class Artifacts {
    private Artifacts() {}

    public static void init() {
        // Eye of the Watcher — use: points at the nearest rift you have discovered. Passive:
        // sanity −0.1/s while carried. flavour (design/16): see-through-walls rendering is a client feature.
        ModItems.addItem("eye_of_the_watcher", EyeOfTheWatcherItem::new, 1);
        // Drowned Lung — use: Water Breathing 8:00 and corruption +2; reusable (not consumed).
        ModItems.addItem("drowned_lung", DrownedLungItem::new, 1);
        // Mirror Shard — use: bearing to the nearest registered site the holder has NOT discovered.
        // Passive: a rare sanity jolt while carried (inventoryTick). flavour (design/16): the
        // "nearby rite" reveal has no registry to read.
        ModItems.addItem("mirror_shard", MirrorShardItem::new, 1);
        // Warden's Sigil — passive: −40% corruption spread within 16 blocks while carried
        // (corruption/TaintSystem.spread). flavour (design/16): "−10% ritual power" has no hook.
        ModItems.add("warden_sigil", 1);
        // Prophet's Mask — carried: +25% summon_star_spawn count (see ProphetMaskItem's javadoc),
        // villagers panic, and a small negative max_sanity modifier. Drawback per design/16.
        ModItems.addItem("prophet_mask", ProphetMaskItem::new, 1);
        // Lantern of the Steadfast — carried: the darkness sanity source does not drain
        // (sanity/SanitySources.DarknessSource reads it). "Its light draws attention" stays flavour.
        ModItems.add("lantern_of_steadfast", 1);
        // Wardstone Pendant — halves rift/Veil sanity drain while worn. flavour (design/16): no rift
        // sanity source exists to halve (rift pressure is damage + taint, not sanity).
        ModItems.add("wardstone_pendant", 1);
        // Compass of Quiet — use: bearing to the nearest curated city in the best (safe) state;
        // spins wildly when a rift is within ~16 blocks (NavigationItem.Target.SAFE_SETTLEMENT).
        ModItems.addItem("compass_of_quiet",
                p -> new NavigationItem(p, NavigationItem.Target.SAFE_SETTLEMENT), 1);
        // Ring of Focus — passive: ritual cooldown ×0.8 while carried (rite/RiteEngine.perform).
        // flavour (design/16): the −sanity cost and +1 corruption per cast are not wired.
        ModItems.add("ring_of_focus", 1);
        // Chalice of Echoes — use: re-casts the holder's last successful rite at an altar for
        // corruption +5 instead of its offerings; each echo adds corruption (ChaliceOfEchoesItem
        // + RiteEchoes, recorded by RiteEngine on success).
        ModItems.addItem("chalice_of_echoes", ChaliceOfEchoesItem::new, 1);
        // Veil Lens — use: action-bar readout of the local taint field and the holder's corruption,
        // then a short sustained sanity drain (VeilLensItem). client-only (design/16): the
        // corruption-field / Veil-seam overlay is a client renderer.
        ModItems.addItem("veil_lens", VeilLensItem::new, 1);
        // Bone Charm — absorbs one fatal blow on the player damage path, then breaks:
        // message, corruption +5, sanity −10 (BoneCharmEvents).
        ModItems.add("bone_charm", 1);
    }
}
