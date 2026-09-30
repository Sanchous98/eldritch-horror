package com.sanchous98.eldritchhorror.registry.items;

import com.sanchous98.eldritchhorror.registry.ModItems;

/**
 * Tomes — knowledge items. Reading one is intended to teach a rite at a cost.
 *
 * <p>Each entry records, in its comment, the rite it <b>grants</b> and the price paid
 * (<b>sanity</b> immediately, <b>corruption</b> forever), per {@code design/16-items.md} and
 * {@code design/26-rituals-and-occult.md}. Tomes are knowledge, not consumables: they stack
 * to 1 and persist. Grants are recorded per item; reading/behaviour not wired yet.
 */
public final class Tomes {
    private Tomes() {}

    public static void init() {
        // Grants ward_of_the_eye. Cost: sanity -10, corruption +0. (16-items canon)
        ModItems.add("tome_of_the_eye", 1);
        // Grants drowned_blessing. Cost: sanity -10, corruption +0. (16-items canon)
        ModItems.add("tome_of_tides", 1);
        // Grants call_the_lesser. Cost: sanity -15, corruption +8. (16-items canon)
        ModItems.add("hollow_text", 1);
        // Grants summon_star_spawn. Cost: sanity -20, corruption +10. (16-items canon)
        ModItems.add("star_codex", 1);
        // Grants close_rift — the Order's sealing branch. Cost: sanity -18, corruption +5.
        ModItems.add("codex_of_wards", 1);
        // Grants rite_of_cleansing — recovery of a corrupted area. Cost: sanity -15, corruption +5.
        ModItems.add("bone_ledger", 1);
        // Grants open_rift — the Veil gate fast-travel branch. Cost: sanity -25, corruption +15.
        ModItems.add("atlas_of_the_veil", 1);
        // Grants respec (Rite of Unmaking) — a rewritten self. Cost: sanity -20, corruption +8.
        ModItems.add("watchers_diary", 1);
        // Grants call_the_lesser and open_rift — the Hollow Choir's teachings. Cost: sanity -25, corruption +15.
        ModItems.add("cult_litanies", 1);
        // Grants a single fragment of a ward (ward_of_the_eye). Cost: sanity -5, corruption +0.
        ModItems.add("fragment_page", 1);
    }
}
