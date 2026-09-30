package com.sanchous98.eldritchhorror.registry.items;

import com.sanchous98.eldritchhorror.registry.ModItems;

/**
 * Weapons and worn gear (FFG traits: <em>Weapon</em>, <em>Magical Weapon</em>,
 * <em>Relic Weapon</em>). Intended behaviour noted per item; not wired yet.
 *
 * <p>All entries are content stubs: single-item stacks so they read as distinct gear. Damage,
 * sanity and corruption effects below are design intent only and live nowhere but this javadoc
 * until the combat and sanity systems are wired (see {@code design/27-systems-framework.md}).
 */
public final class Weapons {
    private Weapons() {
    }

    public static void init() {
        // Occultist's Implement — mainhand ritual focus. +ritual speed; no melee bonus.
        ModItems.add("occult_tool", 1);
        // Silver Blade — consecrated silver; bonus damage vs. eldritch entities. Weapon.
        ModItems.add("silver_blade", 1);
        // Hollow Pact Blade — relic weapon: massive damage, drains sanity on kill, +1 corruption/kill.
        ModItems.add("hollow_pact_blade", 1);
        // Ritual Knife — cheap magical weapon; small damage, enables blood rites when held.
        ModItems.add("ritual_knife", 1);
        // Warden's Maul — heavy Order weapon; high damage, slows the wielder while equipped.
        ModItems.add("wardens_maul", 1);
        // Bone Club — crude grave-bone weapon; low damage, no special effect.
        ModItems.add("bone_club", 1);
        // Star-Iron Sword — meteoric weapon; bonus damage vs. star-spawn, faint sanity drain.
        ModItems.add("star_iron_sword", 1);
        // Tidal Trident — Choir weapon; bonus damage in/under water, corrosion on hit.
        ModItems.add("tidal_trident", 1);
        // Sacrificial Dagger — magical weapon; damage scales with the wielder's corruption.
        ModItems.add("sacrificial_dagger", 1);
        // Censer Mace — burning incense head; applies a ward aura, costs sanity to swing.
        ModItems.add("censer_mace", 1);
        // Eye Scepter — relic weapon; reveals nearby rifts, weak melee, drains sanity while held.
        ModItems.add("eye_scepter", 1);
        // Veil Bow — relic weapon; arrows pierce the Veil, +damage vs. eldritch, sanity cost per shot.
        ModItems.add("veil_bow", 1);

        // Investigator's Coat — chest gear; −sanity drain in darkness. Armour.
        ModItems.add("investigator_coat", 1);
        // Choir Robe — chest gear; +reputation gain, +corruption gain. Armour.
        ModItems.add("choir_robe", 1);
        // Warden's Plate — chest gear; heavy armour, resists corruption spread, slows movement.
        ModItems.add("wardens_plate", 1);
        // Drowned Wetsuit — chest gear; water breathing and swim speed, +corruption over time.
        ModItems.add("drowned_wetsuit", 1);
        // Cultist's Habit — chest gear; blends with cults (+reputation), −sanity in daylight.
        ModItems.add("cultist_habit", 1);
        // Occultist's Hat — head gear; −sanity drain while reading tomes, no armour value.
        ModItems.add("occultists_hat", 1);
        // Boots of Silence — feet gear; muffles footsteps, reduces pursuit/sanity from noise.
        ModItems.add("boots_of_silence", 1);
        // Gloves of Precision — hands gear; +ritual accuracy and lockpicking, no combat bonus.
        ModItems.add("gloves_of_precision", 1);
    }
}
