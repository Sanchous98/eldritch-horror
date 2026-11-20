package com.sanchous98.eldritchhorror.world;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** The Köppen table's lookup rules: class bounds, the land fallback and the water bands. */
class BiomeTableTest {

    @Test
    void knownClassesMapToTheirBiome() {
        assertEquals("minecraft:jungle", BiomeTable.land(1));
        assertEquals("minecraft:desert", BiomeTable.land(4));
        assertEquals("minecraft:snowy_plains", BiomeTable.land(29));
        assertEquals("minecraft:ice_spikes", BiomeTable.land(30));
    }

    @Test
    void outOfRangeClassesFallBackToLand() {
        assertEquals(BiomeTable.DEFAULT_LAND, BiomeTable.land(0));
        assertEquals(BiomeTable.DEFAULT_LAND, BiomeTable.land(-1));
        assertEquals(BiomeTable.DEFAULT_LAND, BiomeTable.land(31));
    }

    @Test
    void waterColumnsSplitIntoBeachShelfAndDeepOcean() {
        assertEquals("minecraft:beach", BiomeTable.forColumn(0, false, 0));
        assertEquals("minecraft:beach", BiomeTable.forColumn(0, false, 1));
        assertEquals("minecraft:ocean", BiomeTable.forColumn(0, false, 2));
        assertEquals("minecraft:ocean", BiomeTable.forColumn(0, false, 30));
        assertEquals("minecraft:deep_ocean", BiomeTable.forColumn(0, false, 31));
    }

    @Test
    void landColumnsUseTheirKoppenClass() {
        assertEquals("minecraft:jungle", BiomeTable.forColumn(1, true, 0));
    }

    @Test
    void everySourceBiomeIsADistinctNamespacedId() {
        assertTrue(BiomeTable.SOURCE_BIOMES.contains("minecraft:ocean"));
        assertTrue(BiomeTable.SOURCE_BIOMES.contains("minecraft:deep_ocean"));
        assertTrue(BiomeTable.SOURCE_BIOMES.contains("minecraft:beach"));
        assertTrue(BiomeTable.SOURCE_BIOMES.contains(BiomeTable.DEFAULT_LAND));
        for (String id : BiomeTable.SOURCE_BIOMES) {
            assertTrue(id.startsWith("minecraft:"), "unexpected biome id " + id);
        }
        assertEquals(BiomeTable.SOURCE_BIOMES.size(), BiomeTable.SOURCE_BIOMES.stream().distinct().count());
    }
}
