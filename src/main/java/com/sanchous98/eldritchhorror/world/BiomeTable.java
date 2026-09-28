package com.sanchous98.eldritchhorror.world;

import java.util.List;

/**
 * Köppen–Geiger class index → vanilla biome id. Class indices and names follow Beck et al.
 * 2018 (1 = Af … 30 = EF); index 0 = ocean/unclassified.
 *
 * <p>{@link #BIOMES} is index-aligned with the class number (entry 0 is a placeholder) so
 * the generator can look a class up directly, and the whole table is serialisable into the
 * biome source codec (the biome-source can then resolve ids without a live registry).
 *
 * <p>Pure and Minecraft-independent; extend freely — this table is the single source of truth.
 */
public final class BiomeTable {

    /** Index-aligned with the Köppen class (0 unused placeholder, 1..30 per Beck 2018). */
    public static final List<String> BIOMES = List.of(
            "minecraft:ocean",         // 0  placeholder (ocean handled separately)
            "minecraft:jungle",        // 1  Af  tropical rainforest
            "minecraft:jungle",        // 2  Am  tropical monsoon
            "minecraft:savanna",       // 3  Aw  tropical savannah
            "minecraft:desert",        // 4  BWh hot desert
            "minecraft:desert",        // 5  BWk cold desert
            "minecraft:savanna",       // 6  BSh hot steppe
            "minecraft:plains",        // 7  BSk cold steppe
            "minecraft:plains",        // 8  Csa Mediterranean hot
            "minecraft:plains",        // 9  Csb Mediterranean warm
            "minecraft:plains",        // 10 Csc Mediterranean cold
            "minecraft:forest",        // 11 Cwa monsoon hot
            "minecraft:forest",        // 12 Cwb monsoon warm
            "minecraft:forest",        // 13 Cwc monsoon cold
            "minecraft:forest",        // 14 Cfa humid subtropical
            "minecraft:birch_forest",  // 15 Cfb oceanic
            "minecraft:birch_forest",  // 16 Cfc subpolar oceanic
            "minecraft:taiga",         // 17 Dsa
            "minecraft:taiga",         // 18 Dsb
            "minecraft:taiga",         // 19 Dsc
            "minecraft:snowy_taiga",   // 20 Dsd
            "minecraft:taiga",         // 21 Dwa
            "minecraft:taiga",         // 22 Dwb
            "minecraft:snowy_taiga",   // 23 Dwc
            "minecraft:snowy_taiga",   // 24 Dwd
            "minecraft:taiga",         // 25 Dfa
            "minecraft:taiga",         // 26 Dfb
            "minecraft:snowy_taiga",   // 27 Dfc
            "minecraft:snowy_taiga",   // 28 Dfd
            "minecraft:snowy_plains",  // 29 ET  tundra
            "minecraft:ice_spikes"     // 30 EF  frost
    );

    /** Fallback for unclassified land. */
    public static final String DEFAULT_LAND = "minecraft:plains";

    /**
     * The distinct biome ids the source may return, in a stable order. This is the set that
     * gets resolved to holders and listed as possible biomes.
     */
    public static final List<String> SOURCE_BIOMES = sourceBiomes();

    private BiomeTable() {
    }

    private static List<String> sourceBiomes() {
        java.util.LinkedHashSet<String> set = new java.util.LinkedHashSet<>(BIOMES.subList(1, BIOMES.size()));
        set.add("minecraft:ocean");
        set.add("minecraft:deep_ocean");
        set.add("minecraft:beach");
        set.add(DEFAULT_LAND);
        return List.copyOf(set);
    }

    /** @return a vanilla biome id for a Köppen class (1..30); never null. */
    public static String land(int koppenClass) {
        if (koppenClass <= 0 || koppenClass >= BIOMES.size()) {
            return DEFAULT_LAND;
        }
        return BIOMES.get(koppenClass);
    }

    /**
     * Biome id for a surface column.
     *
     * @param koppenClass  Köppen class from the baked layer (0 = ocean/unclassified)
     * @param land         whether the column is land
     * @param depthBelowSea how far the surface is below sea level, in blocks (0 for land)
     */
    public static String forColumn(int koppenClass, boolean land, int depthBelowSea) {
        if (!land) {
            if (depthBelowSea <= 1) {
                return "minecraft:beach";
            }
            return depthBelowSea > 30 ? "minecraft:deep_ocean" : "minecraft:ocean";
        }
        return land(koppenClass);
    }
}
