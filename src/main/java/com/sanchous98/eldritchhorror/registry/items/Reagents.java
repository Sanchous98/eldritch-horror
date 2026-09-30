package com.sanchous98.eldritchhorror.registry.items;

import com.sanchous98.eldritchhorror.registry.ModItems;

/** Ritual components consumed by rites. Intended effects noted per item; behaviour not wired yet. */
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
        // Grave Dust — ossuary powder; sanity −1, corruption +1; undead rites
        ModItems.add("grave_dust", 16);
        // Black Candle — dark rite focus; sanity −2, corruption +2; extends night rites
        ModItems.add("black_candle", 16);
        // Ritual Ash — spent-rite residue; corruption +1; reagent for re-binding rites
        ModItems.add("ritual_ash", 16);
        // Ichor Vial — bottled eldritch ichor; sanity −3, corruption +3; empowers summoning
        ModItems.add("ichor_vial", 16);
        // Moonwater — moon-touched water; corruption −1, sanity +1; cleanses minor taint
        ModItems.add("moonwater", 16);
        // Mandrake Root — screaming root; sanity −1; herbal rites and tonics
        ModItems.add("mandrake_root", 16);
        // Iron Nail — cold iron binding; corruption −1; pins wards and seals
        ModItems.add("iron_nail", 16);
        // Consecrated Oil — anointing oil; corruption −2, sanity +1; blesses tools and altars
        ModItems.add("consecrated_oil", 16);
        // Veil Dust — dust from the thinning Veil; sanity −2, corruption +3; opens pathways
        ModItems.add("veil_dust", 16);
        // Effigy Doll — sympathetic likeness; sanity −2, corruption +2; redirects rites and curses
        ModItems.add("effigy_doll", 16);
        // Spirit Ash — remnant of a departed spirit; sanity −1, corruption +2; binds spirits
        ModItems.add("spirit_ash", 16);
    }
}
