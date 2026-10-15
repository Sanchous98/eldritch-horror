package com.sanchous98.eldritchhorror.registry.items;

import com.sanchous98.eldritchhorror.registry.ModItems;

/**
 * Maps, lenses and navigating instruments for finding rifts, cults and secrets.
 *
 * <p>All report a real bearing through {@link NavigationItem}, each reading an existing server-side
 * registry (cities, fixed locations, discovered rifts): the compass of quiet reads the derived city
 * states, and the marked map / route ledger read the in-memory waypoint list ({@link Waypoints}).
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
        // Marked map; sneak-use marks where the holder stands, use points at the nearest mark.
        // The marks are an in-memory per-player list (Waypoints, capped); the map-screen rendering
        // of them stays client-only (design/16/22).
        ModItems.addItem("marked_map", MarkedMapItem::new, 1);
        // Route ledger; use lists every mark the holder has set, nearest-first with bearings.
        ModItems.addItem("route_ledger", RouteLedgerItem::new, 1);
    }
}
