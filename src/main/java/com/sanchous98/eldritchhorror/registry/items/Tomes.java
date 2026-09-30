package com.sanchous98.eldritchhorror.registry.items;

import com.sanchous98.eldritchhorror.registry.ModItems;

/**
 * Tomes — knowledge items. Reading one pays a rite's price.
 *
 * <p>Each entry records, in its comment, the rite it <b>grants</b> and the price paid
 * (<b>sanity</b> immediately, <b>corruption</b> forever), per {@code design/16-items.md} and
 * {@code design/26-rituals-and-occult.md}. Tomes are knowledge, not consumables: they stack to 1
 * and persist, so their use applies only the sanity/corruption cost and does <b>not</b> consume the
 * book (see {@code ModItems.add(id, maxStack, sanity, corruption, false)}).
 *
 * <p>The <b>grant</b> itself is not wired: there is no rite-knowledge system yet, so reading a tome
 * charges the cost and nothing else. When a rite registry exists, hook the grant here.
 */
public final class Tomes {
    private Tomes() {}

    public static void init() {
        // Grants ward_of_the_eye. Cost: sanity -10, corruption +0. (16-items canon)
        ModItems.add("tome_of_the_eye", 1, -10, 0, false);
        // Grants drowned_blessing. Cost: sanity -10, corruption +0. (16-items canon)
        ModItems.add("tome_of_tides", 1, -10, 0, false);
        // Grants call_the_lesser. Cost: sanity -15, corruption +8. (16-items canon)
        ModItems.add("hollow_text", 1, -15, 8, false);
        // Grants summon_star_spawn. Cost: sanity -20, corruption +10. (16-items canon)
        ModItems.add("star_codex", 1, -20, 10, false);
        // Grants close_rift — the Order's sealing branch. Cost: corruption +5 (08-rituals).
        ModItems.add("codex_of_wards", 1, 0, 5, false);
        // Grants rite_of_cleansing — recovery of a corrupted area. Cost: sanity -15, corruption +5.
        ModItems.add("bone_ledger", 1, -15, 5, false);
        // Grants open_rift — the Veil gate fast-travel branch. Cost: sanity -25, corruption +10.
        ModItems.add("atlas_of_the_veil", 1, -25, 10, false);
        // Grants respec (Rite of Unmaking) — a rewritten self. Cost: sanity -20 (08-rituals).
        ModItems.add("watchers_diary", 1, -20, 0, false);
        // Grants call_the_lesser and open_rift — the Hollow Choir's teachings; charged the
        // higher-tier open_rift price (sanity -25, corruption +10).
        ModItems.add("cult_litanies", 1, -25, 10, false);
        // Grants a single fragment of a ward (ward_of_the_eye). Cost: sanity -5, corruption +0.
        ModItems.add("fragment_page", 1, -5, 0, false);
    }
}
