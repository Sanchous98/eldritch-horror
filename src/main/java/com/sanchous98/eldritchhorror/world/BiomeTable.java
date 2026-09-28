package com.sanchous98.eldritchhorror.world;

import java.util.Map;

/**
 * Maps a Köppen–Geiger class index (as baked in {@code koppen_<W>x<H>.png}) to a vanilla
 * biome id. Class indices and names follow Beck et al. 2018 (1 = Af … 30 = EF); 0 = ocean.
 *
 * <p>Pure and Minecraft-independent; the generator resolves the returned id against the
 * biome registry. Extend freely — this table is the single source of truth for the mapping.
 */
public final class BiomeTable {
    private static final Map<Integer, String> LAND = Map.ofEntries(
            Map.entry(1, "minecraft:jungle"),          // Af  tropical rainforest
            Map.entry(2, "minecraft:jungle"),          // Am  tropical monsoon
            Map.entry(3, "minecraft:savanna"),         // Aw  tropical savannah
            Map.entry(4, "minecraft:desert"),          // BWh hot desert
            Map.entry(5, "minecraft:desert"),          // BWk cold desert
            Map.entry(6, "minecraft:savanna"),         // BSh hot steppe
            Map.entry(7, "minecraft:plains"),          // BSk cold steppe
            Map.entry(8, "minecraft:plains"),          // Csa Mediterranean hot
            Map.entry(9, "minecraft:plains"),          // Csb Mediterranean warm
            Map.entry(10, "minecraft:plains"),         // Csc Mediterranean cold
            Map.entry(11, "minecraft:forest"),         // Cwa monsoon hot
            Map.entry(12, "minecraft:forest"),         // Cwb monsoon warm
            Map.entry(13, "minecraft:forest"),         // Cwc monsoon cold
            Map.entry(14, "minecraft:forest"),         // Cfa humid subtropical
            Map.entry(15, "minecraft:birch_forest"),   // Cfb oceanic
            Map.entry(16, "minecraft:birch_forest"),   // Cfc subpolar oceanic
            Map.entry(17, "minecraft:taiga"),          // Dsa
            Map.entry(18, "minecraft:taiga"),          // Dsb
            Map.entry(19, "minecraft:taiga"),          // Dsc
            Map.entry(20, "minecraft:snowy_taiga"),    // Dsd
            Map.entry(21, "minecraft:taiga"),          // Dwa
            Map.entry(22, "minecraft:taiga"),          // Dwb
            Map.entry(23, "minecraft:snowy_taiga"),    // Dwc
            Map.entry(24, "minecraft:snowy_taiga"),    // Dwd
            Map.entry(25, "minecraft:taiga"),          // Dfa
            Map.entry(26, "minecraft:taiga"),          // Dfb
            Map.entry(27, "minecraft:snowy_taiga"),    // Dfc
            Map.entry(28, "minecraft:snowy_taiga"),    // Dfd
            Map.entry(29, "minecraft:snowy_plains"),   // ET  tundra
            Map.entry(30, "minecraft:ice_spikes")      // EF  frost
    );

    private BiomeTable() {
    }

    /** @return a vanilla biome id for the class, or {@code null} for ocean/unknown. */
    public static String land(int koppenClass) {
        return LAND.get(koppenClass);
    }

    /** Biome id for a column: ocean by depth, else the Köppen mapping with a coast override. */
    public static String forColumn(int koppenClass, boolean land, int depthBelowSea) {
        if (!land) {
            return depthBelowSea > 30 ? "minecraft:deep_ocean" : "minecraft:ocean";
        }
        if (depthBelowSea == 0 && koppenClass == 0) {
            return "minecraft:beach"; // coast not classified
        }
        String biome = land(koppenClass);
        return biome != null ? biome : "minecraft:plains";
    }
}
