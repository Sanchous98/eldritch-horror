package com.sanchous98.eldritchhorror.registry.items;

import com.sanchous98.eldritchhorror.registry.ModItems;

/**
 * Ritual components consumed by rites.
 *
 * <p>The eight named offerings ({@code salt_reagent} … {@code blood_offering}) stay plain materials:
 * their live use is the rite-offering check in {@code rite/RiteReagents}, which already consumes
 * them. The eleven secondary reagents from the site-loot tables are also consumed on use — each is
 * a {@link ConsumableItem} that applies the sanity/corruption delta its comment records, then
 * shrinks the stack (the {@code ModItems.add(id, max, sanity, corruption)} overload).
 *
 * <p>"over time" is not represented: like {@link Consumables}, no duration system exists yet, so
 * each effect is a single immediate delta.
 */
public final class Reagents {
    private Reagents() {}

    public static void init() {
        // Consecrated Salt — ward/cleansing; corruption −, offering for ward_of_the_eye / close_rift
        ModItems.add("salt_reagent", 16);
        // Ritual Chalk — marks the circle; binds a rite pattern, offering for ward_of_the_eye / close_rift
        ModItems.add("chalk_reagent", 16);
        // Grave Bone — skeletal offering; sanity −1, corruption +1; call_the_lesser
        ModItems.add("bone_reagent", 16);
        // Star-Iron Fragment — meteor/Veil metal; sanity −2, corruption +2; summon_star_spawn
        ModItems.add("star_reagent", 16);
        // Void Residue — rift leavings; sanity −3, corruption +4; open_rift
        ModItems.add("void_reagent", 16);
        // Moonlit Silver — purity metal; corruption −2; rite_of_cleansing / respec
        ModItems.add("silver_reagent", 16);
        // Drowned Pearl — coast/Choir offering; corruption +1, enables water rites; drowned_blessing
        ModItems.add("pearl_reagent", 16);
        // Blood Offering — self-wound; sanity −4, corruption +3; fuels summoning/open rites
        ModItems.add("blood_offering", 16);

        // Grave Dust — ossuary powder; use: sanity −1, corruption +1, consumed; undead rites
        ModItems.add("grave_dust", 16, -1, 1);
        // Black Candle — dark rite focus; use: sanity −2, corruption +2, consumed; extends night rites
        ModItems.add("black_candle", 16, -2, 2);
        // Ritual Ash — spent-rite residue; use: corruption +1, consumed; reagent for re-binding rites
        ModItems.add("ritual_ash", 16, 0, 1);
        // Ichor Vial — bottled eldritch ichor; use: sanity −3, corruption +3, consumed; empowers summoning
        ModItems.add("ichor_vial", 16, -3, 3);
        // Moonwater — moon-touched water; use: corruption −1, sanity +1, consumed; cleanses minor taint
        ModItems.add("moonwater", 16, 1, -1);
        // Mandrake Root — screaming root; use: sanity −1, consumed; herbal rites and tonics
        ModItems.add("mandrake_root", 16, -1, 0);
        // Iron Nail — cold iron binding; use: corruption −1, consumed; pins wards and seals
        ModItems.add("iron_nail", 16, 0, -1);
        // Consecrated Oil — anointing oil; use: corruption −2, sanity +1, consumed; blesses tools and altars
        ModItems.add("consecrated_oil", 16, 1, -2);
        // Veil Dust — dust from the thinning Veil; use: sanity −2, corruption +3, consumed; opens pathways
        ModItems.add("veil_dust", 16, -2, 3);
        // Effigy Doll — sympathetic likeness; use: sanity −2, corruption +2, consumed; redirects rites and curses
        ModItems.add("effigy_doll", 16, -2, 2);
        // Spirit Ash — remnant of a departed spirit; use: sanity −1, corruption +2, consumed; binds spirits
        ModItems.add("spirit_ash", 16, -1, 2);
    }
}
