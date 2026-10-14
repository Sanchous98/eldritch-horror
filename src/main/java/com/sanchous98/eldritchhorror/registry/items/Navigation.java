package com.sanchous98.eldritchhorror.registry.items;

import com.sanchous98.eldritchhorror.registry.ModItems;

/**
 * Maps, lenses and navigating instruments for finding rifts, cults and secrets.
 *
 * <p>Five now report a real bearing through {@link NavigationItem}, each reading an existing
 * server-side registry (cities, fixed locations, discovered rifts). The remaining two stay flavour:
 * they need a store this mod does not have — player-placed waypoints (the map only draws the
 * player, cities, discovered sites and rifts) and recorded travel routes.
 */
public final class Navigation {
    private Navigation() {
    }

    public static void init() {
        // City map; use: names the nearest curated settlement and its bearing.
        ModItems.addItem("city_map", p -> new NavigationItem(p, NavigationItem.Target.SETTLEMENT), 1);
        // World atlas; opens the fullscreen world map (client-side, via a dist-safe hook).
        ModItems.addItem("world_atlas", WorldAtlasItem::new, 1);
        // Cartographer's Lens; use: names the nearest registered structure/ruin and its bearing.
        ModItems.addItem("cartographers_lens",
                p -> new NavigationItem(p, NavigationItem.Target.SITE), 1);
        // Compass of Longing; use: points toward the nearest rift the holder has discovered.
        ModItems.addItem("compass_of_longing",
                p -> new NavigationItem(p, NavigationItem.Target.RIFT), 1);
        // Sextant; use: fixes the holder's position (block coordinates).
        ModItems.addItem("sextant", p -> new NavigationItem(p, NavigationItem.Target.POSITION), 1);
        // Veil Compass; use: points toward the nearest fixed ritual-altar site.
        ModItems.addItem("veil_compass", p -> new NavigationItem(p, NavigationItem.Target.ALTAR), 1);
        // Marked map; stores and reveals player-placed waypoints.
        // flavour (design/16): the map has no waypoint store, only the synced player/home/site/rift markers.
        ModItems.add("marked_map", 1);
        // Route ledger; records safe travel routes between strongholds.
        // flavour (design/16): no route store exists.
        ModItems.add("route_ledger", 1);
    }
}
