package com.sanchous98.eldritchhorror.world.city;

import java.util.Map;


/**
 * A curated city: real name, world coordinates and real population (from
 * {@code assets/eldritch_horror/map/settlements.json}, baked by {@code tools/bake_earth.py}).
 *
 * <p>{@link #radius()} scales the in-game footprint with the real city size. The map is about
 * 1:2,440 (1 block ≈ 2.44 km), so the largest real cities span a few chunks — "near as big as
 * real ones" at this scale.
 */
public record City(String id, String name, int x, int z, int population) {

    /**
     * Radii supplied by a map package, keyed by city id. A prebuilt 1:1 city declares its own
     * footprint (kilometres, not a few chunks), so the population-derived default does not apply.
     */
    private static volatile Map<String, Integer> radiusOverrides = Map.of();

    /** Installs the per-city radii of the current map package (empty map restores the default). */
    public static void setRadiusOverrides(Map<String, Integer> radii) {
        radiusOverrides = Map.copyOf(radii);
    }

    /** Footprint radius in blocks: the map package's value, else the population estimate. */
    public int radius() {
        Integer override = radiusOverrides.get(this.id);
        if (override != null) {
            return override;
        }
        int r = (int) Math.round(Math.sqrt(Math.max(population, 1)) / 150.0);
        return Math.clamp(r, 10, 44);
    }
}
