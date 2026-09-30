package com.sanchous98.eldritchhorror.registry.items;

import com.sanchous98.eldritchhorror.registry.ModItems;

/** Maps, lenses and navigating instruments for finding rifts, cults and secrets. Stub content; behaviour not wired yet. */
public final class Navigation {
    private Navigation() {
    }

    public static void init() {
        // City map; reveals the layout of the nearest settlement.
        ModItems.add("city_map", 1);
        // World atlas; consolidated view of explored regions and cult territory.
        ModItems.add("world_atlas", 1);
        // Cartographer's Lens; reveals structures and ruins at a distance.
        ModItems.add("cartographers_lens", 1);
        // Compass of Longing; points toward the nearest rift.
        ModItems.add("compass_of_longing", 1);
        // Sextant; fixes position under the Veil's shifting sky.
        ModItems.add("sextant", 1);
        // Veil Compass; points toward active ritual sites.
        ModItems.add("veil_compass", 1);
        // Marked map; stores and reveals player-placed waypoints.
        ModItems.add("marked_map", 1);
        // Route ledger; records safe travel routes between strongholds.
        ModItems.add("route_ledger", 1);
    }
}
