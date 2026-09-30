package com.sanchous98.eldritchhorror.registry.items;

import com.sanchous98.eldritchhorror.registry.ModItems;

/**
 * Tonics and restoratives that move sanity/corruption.
 *
 * <p>Behaviour is now wired: each item is a {@link ConsumableItem} whose right-click applies the
 * documented sanity/corruption delta (server-side) and consumes the stack. "over time" and
 * "area" effects are applied as a single immediate delta for now, since no duration/area system
 * exists yet.
 */
public final class Consumables {
    private Consumables() {}

    public static void init() {
        // Sanity Tincture — sanity +8, corruption 0; bottled, stacks 1
        ModItems.add("sanity_tincture", 1, 8, 0);
        // Soothing Tea — sanity +4, corruption −1; calm and warming
        ModItems.add("soothing_tea", 1, 4, -1);
        // Dream Syrup — sanity +6, corruption +2; lucid dreams at a price
        ModItems.add("dream_syrup", 1, 6, 2);
        // Valerian Tonic — sanity +10 over time, corruption 0; slow sedative calm
        ModItems.add("valerian_tonic", 1, 10, 0);
        // Lucid Draught — sanity +5, corruption −2; steels the mind against madness
        ModItems.add("lucid_draught", 1, 5, -2);
        // Salt Purge — corruption −5, sanity −3; purging is agony
        ModItems.add("salt_purge", 16, -3, -5);
        // Blessed Water — corruption −6, sanity +2; a splash of the sacred
        ModItems.add("blessed_water", 1, 2, -6);
        // Moonwater Flask — corruption −4, sanity +4; moon-blessed, gentle cleanse
        ModItems.add("moonwater_flask", 1, 4, -4);
        // Penitent's Ash — corruption −8, sanity −5; self-mortification for absolution
        ModItems.add("penitents_ash", 16, -5, -8);
        // Sight Salve — sanity −4, corruption +2; opens the eye to the unseen
        ModItems.add("sight_salve", 1, -4, 2);
        // Wardsalt Bomb — corruption −3 in an area, sanity +1; thrown cleansing burst
        // (area not implemented yet: applied to the user only)
        ModItems.add("wardsalt_bomb", 16, 1, -3);
        // Incense Stick — sanity +2, corruption −1 over time; smoke that soothes
        ModItems.add("incense_stick", 16, 2, -1);
        // Herbal Bundle — sanity +3, corruption −1; folk remedy for frayed nerves
        ModItems.add("herbal_bundle", 16, 3, -1);
        // Fortifying Stew — sanity +6, corruption 0; a hot meal against the dark
        ModItems.add("fortifying_stew", 16, 6, 0);
        // Iron Broth — sanity +4, corruption +1; bitter, grounding, faintly wrong
        ModItems.add("iron_broth", 16, 4, 1);
    }
}
