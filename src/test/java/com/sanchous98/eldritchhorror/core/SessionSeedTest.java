package com.sanchous98.eldritchhorror.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

/** The session seed is stable within a process and splits cleanly by key. */
class SessionSeedTest {

    @Test
    void deriveIsStableForTheSameKey() {
        assertEquals(SessionSeed.derive("dungeon:paris:cellar-3"),
                SessionSeed.derive("dungeon:paris:cellar-3"));
        assertEquals(SessionSeed.derive("dungeon:paris:cellar-3"),
                SessionSeed.derive("dungeon:paris:cellar-3"));
    }

    @Test
    void differentKeysGetDifferentSeeds() {
        assertNotEquals(SessionSeed.derive("dungeon:paris:cellar-3"),
                SessionSeed.derive("dungeon:paris:cellar-4"));
        assertNotEquals(SessionSeed.derive("dungeon:paris"),
                SessionSeed.derive("dungeon:london"));
    }

    @Test
    void sessionSeedIsFixedForTheProcess() {
        assertEquals(SessionSeed.value(), SessionSeed.value());
    }
}
