package com.sanchous98.eldritchhorror.world.city;

/**
 * A curated city: real name, world coordinates and real population (from
 * {@code assets/eldritch_horror/map/settlements.json}, baked by {@code tools/bake_earth.py}).
 *
 * <p>{@link #radius()} scales the in-game footprint with the real city size. The map is about
 * 1:2,440 (1 block ≈ 2.44 km), so the largest real cities span a few chunks — "near as big as
 * real ones" at this scale.
 */
public record City(String id, String name, int x, int z, int population) {

    /** Footprint radius in blocks, from real population (Tokyo ~40, Nairobi ~12). */
    public int radius() {
        int r = (int) Math.round(Math.sqrt(Math.max(population, 1)) / 150.0);
        return Math.max(10, Math.min(44, r));
    }
}
