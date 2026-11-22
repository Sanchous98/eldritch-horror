package com.sanchous98.eldritchhorror.registry.items;

import java.util.Map;

/**
 * The quality ladder of every weapon ({@code design/16-items.md}).
 *
 * <p>A grade is <b>presentation and provenance</b>, not a second balance axis: damage, durability
 * and on-hit effects stay the numbers each item registers. The grade sets the item name's colour
 * (vanilla rarity) and adds one tooltip line, and it mirrors where the item comes from - crude
 * improvised gear, the mundane field kit, crafted/issued quality, rare-material fine work, or a
 * unique relic.
 *
 * <p>Deliberately Minecraft-free: {@code WeaponGradeTest} checks that every combat item is graded
 * and that the lookup defaults sensibly without bootstrapping the game registries. {@code Weapons}
 * maps the grade to {@code Rarity}.
 */
public enum WeaponGrade {

    /** Improvised: bone and a knife, the first thing a survivor can make. */
    CRUDE("crude"),
    /** Mundane 1920s field kit: the axe, the whip, the sidearm, the throwables. */
    COMMON("common"),
    /** Crafted or issued gear with real materials behind it. */
    QUALITY("quality"),
    /** Rare-material or elite-issue weapons. */
    FINE("fine"),
    /** Unique occult relics; one of a kind. */
    RELIC("relic");

    /** The grade of every weapon and combat throwable, by item id. */
    private static final Map<String, WeaponGrade> BY_ITEM = Map.ofEntries(
            // crude
            Map.entry("bone_club", CRUDE),
            Map.entry("ritual_knife", CRUDE),
            // common: the mundane tier
            Map.entry("occult_tool", COMMON),
            Map.entry("woodsman_axe", COMMON),
            Map.entry("bull_whip", COMMON),
            Map.entry("revolver_38", COMMON),
            Map.entry("dynamite", COMMON),
            Map.entry("tear_gas", COMMON),
            // quality: crafted or issued
            Map.entry("silver_blade", QUALITY),
            Map.entry("automatic_45", QUALITY),
            Map.entry("shotgun_double", QUALITY),
            Map.entry("sacrificial_dagger", QUALITY),
            Map.entry("censer_mace", QUALITY),
            // fine: rare materials, elite issue
            Map.entry("star_iron_sword", FINE),
            Map.entry("tidal_trident", FINE),
            Map.entry("wardens_maul", FINE),
            Map.entry("carbine_rifle", FINE),
            // relics
            Map.entry("hollow_pact_blade", RELIC),
            Map.entry("eye_scepter", RELIC),
            Map.entry("veil_bow", RELIC));

    private final String id;

    WeaponGrade(String id) {
        this.id = id;
    }

    /** The snake_case id used in the lang key. */
    public String id() {
        return this.id;
    }

    /** The full tooltip line for this grade, e.g. {@code grade.eldritch_horror.quality}. */
    public String lineKey() {
        return "grade.eldritch_horror." + this.id;
    }

    /** The grade of an item id; an unlisted item reads as {@link #COMMON}. */
    public static WeaponGrade of(String itemId) {
        return BY_ITEM.getOrDefault(itemId, COMMON);
    }

    /** The complete id → grade table (immutable), for tests and documentation. */
    public static Map<String, WeaponGrade> byItem() {
        return BY_ITEM;
    }
}
