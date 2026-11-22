package com.sanchous98.eldritchhorror.registry.items;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Guards the four firearms' stat ladder and the record's validation ({@code design/16-items.md}).
 *
 * <p>Pure Java: no game bootstrap, so it runs in the same fast {@code ./gradlew test} pass as
 * {@code WeaponMathTest}. The point is to catch a typo in a spec (zero pellets, negative pierce)
 * before a client session is needed to notice it.
 */
class FirearmSpecTest {

    @Test
    void theLadderIsMonotonicWhereItShouldBe() {
        assertTrue(FirearmLadder.REVOLVER.damage() < FirearmLadder.AUTOMATIC.damage(),
                "the automatic hits harder than the revolver");
        assertTrue(FirearmLadder.REVOLVER.cooldownTicks() > FirearmLadder.AUTOMATIC.cooldownTicks(),
                "the revolver cycles slower than the automatic");
        assertTrue(FirearmLadder.CARBINE.damage() > FirearmLadder.AUTOMATIC.damage(),
                "the rifle is the hardest single shot");
        assertTrue(FirearmLadder.CARBINE.velocity() > FirearmLadder.REVOLVER.velocity(),
                "the rifle's round is faster");
    }

    @Test
    void theShotgunSpendsTwoCartridgesForOneHeavyCharge() {
        assertEquals(1, FirearmLadder.SHOTGUN.pellets());
        assertEquals(2, FirearmLadder.SHOTGUN.ammoPerShot());
        assertTrue(FirearmLadder.SHOTGUN.damage() > FirearmLadder.CARBINE.damage(),
                "both barrels at once hit hardest");
        assertEquals(1, FirearmLadder.REVOLVER.ammoPerShot());
        assertEquals(1, FirearmLadder.CARBINE.ammoPerShot());
    }

    @Test
    void onlyTheRiflePierces() {
        assertEquals(1, FirearmLadder.CARBINE.pierce());
        assertEquals(0, FirearmLadder.REVOLVER.pierce());
        assertEquals(0, FirearmLadder.AUTOMATIC.pierce());
        assertEquals(0, FirearmLadder.SHOTGUN.pierce());
    }

    @Test
    void rejectsImpossibleSpecs() {
        assertThrows(IllegalArgumentException.class,
                () -> new FirearmSpec(0.0F, 1.0F, 0.0F, 0, 1, 1, 0));
        assertThrows(IllegalArgumentException.class,
                () -> new FirearmSpec(1.0F, 0.0F, 0.0F, 0, 1, 1, 0));
        assertThrows(IllegalArgumentException.class,
                () -> new FirearmSpec(1.0F, 1.0F, -1.0F, 0, 1, 1, 0));
        assertThrows(IllegalArgumentException.class,
                () -> new FirearmSpec(1.0F, 1.0F, 0.0F, 0, 0, 1, 0));
        assertThrows(IllegalArgumentException.class,
                () -> new FirearmSpec(1.0F, 1.0F, 0.0F, 0, 1, 0, 0));
        assertThrows(IllegalArgumentException.class,
                () -> new FirearmSpec(1.0F, 1.0F, 0.0F, 0, 1, 1, -1));
    }
}
