package com.sanchous98.eldritchhorror.world.town;

/**
 * One non-curated settlement from {@code assets/eldritch_horror/map/settlements.json}: real
 * name, world coordinates and real population. Deliberately data-only (no Minecraft types), so
 * the town index and its siting rules load in a bare JVM.
 *
 * <p>Curated cities stay in {@link com.sanchous98.eldritchhorror.world.city.Cities}; this is the
 * generic tier built by {@link TownLocation}.
 *
 * @param id stable baked id, e.g. {@code subotica_7165_-16774}; seeds the layout
 * @param name real place name (display only)
 * @param x world block X of the settlement centre
 * @param z world block Z of the settlement centre
 * @param population real population, used to scale the footprint
 */
public record Town(String id, String name, int x, int z, int population) {
}
