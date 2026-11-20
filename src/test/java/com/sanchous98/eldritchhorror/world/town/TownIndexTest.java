package com.sanchous98.eldritchhorror.world.town;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * Guards the game-free spatial index that keeps 3,065 towns out of the per-chunk hot path: bucket
 * lookup across neighbouring cells, exact radius/boundary behaviour, empty buckets, the chunk-box
 * query, and a many-town set.
 */
class TownIndexTest {

    private static Town town(String id, int x, int z) {
        return new Town(id, id, x, z, 100_000);
    }

    @Test
    void nearFindsTownsAcrossNeighbouringBuckets() {
        TownIndex index = new TownIndex(512);
        index.add(town("a", 100, 100));
        index.add(town("b", 700, 100));   // adjacent bucket in X
        index.add(town("c", 100, 700));   // adjacent bucket in Z
        index.add(town("d", 700, 700));   // diagonal bucket
        index.add(town("far", 5000, 5000));
        index.seal();

        Set<String> ids = ids(index.near(100, 100, 900));
        assertEquals(Set.of("a", "b", "c", "d"), ids);
    }

    @Test
    void nearIsExactAndInclusiveAtTheRadius() {
        TownIndex index = new TownIndex();
        index.add(town("on", 13, 0));    // exactly 13 blocks away
        index.add(town("out", 14, 0));   // 14 blocks away
        index.add(town("corner", 3, 4)); // exactly 5 blocks away
        index.seal();

        assertEquals(Set.of("on", "corner"), ids(index.near(0, 0, 13)));
        assertEquals(Set.of("on", "out", "corner"), ids(index.near(0, 0, 14)));
        assertEquals(Set.of("on", "out", "corner"), ids(index.near(0, 0, 13 + 1)));
        assertTrue(index.near(0, 0, -1).isEmpty(), "a negative radius yields nothing");
    }

    @Test
    void bucketBoundariesAndNegativeCoordinatesAreHandled() {
        TownIndex index = new TownIndex(16);
        // Straddles the x = 0 and x = 16 bucket boundaries, and lives in negative cells.
        index.add(town("west", -1, 0));
        index.add(town("east", 16, 0));
        index.add(town("south", 0, -17));
        index.add(town("corner", -16, -16));
        index.seal();

        assertEquals(Set.of("west"), ids(index.near(0, 0, 1)));
        assertEquals(Set.of("west", "east"), ids(index.near(0, 0, 16)));
        assertEquals(Set.of("west", "east", "south"), ids(index.near(0, 0, 17)));
        assertEquals(Set.of("corner"), ids(index.near(-16, -15, 1)));
        assertEquals(4, index.near(0, 0, 200).size());
    }

    @Test
    void emptyBucketsAndEmptyIndexesReturnEmptyLists() {
        TownIndex empty = new TownIndex();
        empty.seal();
        assertTrue(empty.near(0, 0, 10_000).isEmpty());
        assertTrue(empty.forChunk(0, 0, 10_000).isEmpty());

        TownIndex sparse = new TownIndex(512);
        sparse.add(town("only", 0, 0));
        sparse.seal();
        assertTrue(sparse.near(20_000, 20_000, 512).isEmpty());
        assertTrue(sparse.forChunk(-500, -500, 64).isEmpty());
    }

    @Test
    void forChunkMeasuresToTheChunkBoxNotItsCentre() {
        TownIndex index = new TownIndex(512);
        // Chunk (0,0) covers blocks 0..15. This town is 20 blocks past the far edge.
        index.add(town("east", 35, 0));
        // 21 blocks past the edge: just out of a 20-block reach.
        index.add(town("far", 36, 0));
        index.seal();

        assertEquals(Set.of("east"), ids(index.forChunk(0, 0, 20)));
        assertEquals(Set.of("east", "far"), ids(index.forChunk(0, 0, 21)));
        // Both towns are inside chunk (2,0) (blocks 32..47), so reach 0 finds them.
        assertEquals(Set.of("east", "far"), ids(index.forChunk(2, 0, 0)));
        // Chunk (1,0) covers 16..31, four blocks short of the nearest town.
        assertTrue(index.forChunk(1, 0, 3).isEmpty());
        // Chunk (-1,0) covers -16..-1: the town is 36 blocks past its edge.
        assertTrue(index.forChunk(-1, 0, 0).isEmpty());
        // Chunk (3,0) starts at 48, so "far" is 12 blocks away and "east" 13.
        assertTrue(index.forChunk(3, 0, 11).isEmpty());
        assertEquals(Set.of("east", "far"), ids(index.forChunk(3, 0, 13)));
    }

    @Test
    void manyTownsAreFilteredToTheExactRadius() {
        TownIndex index = new TownIndex(1024);
        Set<String> nearIds = new HashSet<>();
        for (int i = 0; i < 5000; i++) {
            int x = i * 7919 % 40_000 - 20_000;
            int z = i * 104_729 % 40_000 - 20_000;
            index.add(town("t" + i, x, z));
            long dx = x - 1000L;
            long dz = z - -2000L;
            if (dx * dx + dz * dz <= 300L * 300L) {
                nearIds.add("t" + i);
            }
        }
        index.seal();

        assertEquals(5000, index.size());
        List<Town> found = index.near(1000, -2000, 300);
        assertEquals(nearIds, ids(found));
        for (Town town : found) {
            long dx = town.x() - 1000L;
            long dz = town.z() - -2000L;
            assertTrue(dx * dx + dz * dz <= 300L * 300L);
        }
    }

    @Test
    void sealingFreezesTheIndex() {
        TownIndex index = new TownIndex();
        index.add(town("a", 0, 0));
        index.seal();
        index.seal(); // idempotent
        assertEquals(1, index.size());
        assertThrows(IllegalStateException.class, () -> index.add(town("b", 1, 1)));
        assertThrows(IllegalArgumentException.class, () -> new TownIndex(0));
    }

    private static Set<String> ids(List<Town> towns) {
        Set<String> out = new HashSet<>();
        for (Town town : towns) {
            out.add(town.id());
        }
        return out;
    }
}
