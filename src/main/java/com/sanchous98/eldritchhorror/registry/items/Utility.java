package com.sanchous98.eldritchhorror.registry.items;

import com.sanchous98.eldritchhorror.registry.ModItems;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;

/** Mundane survival tools and field supplies. */
public final class Utility {

    /** Whetstone — one tempering attempt on a weapon held in the other hand. */
    public static final DeferredItem<Item> WHETSTONE = ModItems.add("whetstone", 16);

    private Utility() {
    }

    public static void init() {
        // Hand lantern; portable light source for dark expeditions.
        ModItems.add("hand_lantern", 1);
        // Oil flask; fuel for the hand lantern.
        ModItems.add("oil_flask", 16);
        // Tinderbox; lights fires and lanterns in the field.
        ModItems.add("tinderbox", 1);
        // Bedroll; a portable sleeping spot.
        ModItems.add("bedroll", 1);
        // Tent kit; deploys a temporary shelter.
        ModItems.add("tent_kit", 1);
        // Water skin; carries drinking water.
        ModItems.add("water_skin", 16);
        // Field journal; records discoveries, lore and sanity notes.
        ModItems.add("field_journal", 1);
        // Rope coil; climbing and traversal aid.
        ModItems.add("rope_coil", 16);
        // Developer tool; dev-only, shows sanity/corruption.
        ModItems.add("debug_tool", 1);
    }
}
