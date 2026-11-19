package com.sanchous98.eldritchhorror.registry.items;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Guards the widely-shared weapon arithmetic ({@code design/16-items.md}): the sacrificial dagger's
 * corruption curve, the tidal trident's water multiplier and the veil bow's eldritch bonus, plus the
 * floor/clamp rules that stop a broken event value turning into negative damage. {@link WeaponMath}
 * is game-free by design, so no server is needed.
 */
class WeaponMathTest {

    /** Float comparison margin; the values are simple decimals. */
    private static final float EPSILON = 1.0E-4F;

    @Test
    void daggerAtNoCorruptionIsUnchanged() {
        assertEquals(1.0, WeaponMath.daggerMultiplier(0.0));
    }

    @Test
    void daggerScalesLinearlyWithCorruption() {
        assertEquals(1.25, WeaponMath.daggerMultiplier(50.0), 1.0E-9);
        assertEquals(1.5, WeaponMath.daggerMultiplier(100.0), 1.0E-9);
    }

    @Test
    void daggerClampsCorruptionOutsideTheMeter() {
        assertEquals(1.0, WeaponMath.daggerMultiplier(-40.0));
        assertEquals(1.5, WeaponMath.daggerMultiplier(400.0), 1.0E-9);
    }

    @Test
    void tridentIsStrongerOnlyUnderwater() {
        assertEquals(1.25, WeaponMath.waterMultiplier(true));
        assertEquals(1.0, WeaponMath.waterMultiplier(false));
    }

    @Test
    void veilBowAddsItsBonusOnlyToEldritchTargets() {
        assertEquals(6.0F, WeaponMath.bonusDamage(4.0F, WeaponMath.VEIL_BOW_ELDRITCH_BONUS, true),
                EPSILON);
        assertEquals(4.0F, WeaponMath.bonusDamage(4.0F, WeaponMath.VEIL_BOW_ELDRITCH_BONUS, false),
                EPSILON);
    }

    @Test
    void scalingFloorsAtZeroInsteadOfHealing() {
        assertEquals(0.0F, WeaponMath.scaled(-3.0F, 1.5), EPSILON);
        assertEquals(0.0F, WeaponMath.scaled(4.0F, 0.0), EPSILON);
        assertEquals(0.0F, WeaponMath.scaled(4.0F, -1.0), EPSILON);
    }

    @Test
    void aZeroBaseStaysZeroEvenWithABonus() {
        assertEquals(0.0F, WeaponMath.bonusDamage(0.0F, 2.0F, true), EPSILON);
        assertEquals(0.0F, WeaponMath.bonusDamage(-1.0F, 2.0F, true), EPSILON);
    }

    @Test
    void scalingAppliesTheMultiplierToAPositiveHit() {
        assertEquals(6.0F, WeaponMath.scaled(4.0F, 1.5), EPSILON);
        assertEquals(5.0F, WeaponMath.scaled(4.0F, 1.25), EPSILON);
    }
}
