package com.sanchous98.eldritchhorror.registry.items;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Pins the quality ladder ({@code design/16-items.md}): the first two steps are free, the risk of a
 * downgrade or a break grows with quality, and every roll boundary is exact. The odds are generous
 * on purpose - short sessions should not be strangled by the roll.
 */
class WeaponQualityTest {

    @Test
    void theFirstTwoStepsAreGuaranteed() {
        assertEquals(new WeaponQuality.Odds(100, 0, 0), WeaponQuality.odds(0));
        assertEquals(new WeaponQuality.Odds(100, 0, 0), WeaponQuality.odds(1));
        for (int percent = 0; percent < 100; percent++) {
            assertEquals(WeaponQuality.Outcome.UPGRADE, WeaponQuality.roll(0, percent));
            assertEquals(WeaponQuality.Outcome.UPGRADE, WeaponQuality.roll(1, percent));
        }
    }

    @Test
    void riskGrowsWithQuality() {
        int previousDowngrade = -1;
        int previousBreak = -1;
        // The top row is a never-rolled sentinel (the caller refuses at MAX), so the climb is
        // asserted up to MAX - 1.
        for (int quality = 0; quality < WeaponQuality.MAX; quality++) {
            WeaponQuality.Odds odds = WeaponQuality.odds(quality);
            assertTrue(odds.downgrade() >= previousDowngrade, "downgrade must not shrink");
            assertTrue(odds.breakChance() >= previousBreak, "break must not shrink");
            previousDowngrade = odds.downgrade();
            previousBreak = odds.breakChance();
        }
    }

    @Test
    void everyOddsRowSumsTo100() {
        for (int quality = 0; quality <= WeaponQuality.MAX; quality++) {
            WeaponQuality.Odds odds = WeaponQuality.odds(quality);
            assertEquals(100, odds.upgrade() + odds.downgrade() + odds.breakChance());
        }
        assertThrows(IllegalArgumentException.class,
                () -> new WeaponQuality.Odds(90, 5, 4));
    }

    @Test
    void theRollBoundariesAreExact() {
        // 2 -> 3 is 95 / 4 / 1.
        assertEquals(WeaponQuality.Outcome.UPGRADE, WeaponQuality.roll(2, 0));
        assertEquals(WeaponQuality.Outcome.UPGRADE, WeaponQuality.roll(2, 94));
        assertEquals(WeaponQuality.Outcome.DOWNGRADE, WeaponQuality.roll(2, 95));
        assertEquals(WeaponQuality.Outcome.DOWNGRADE, WeaponQuality.roll(2, 98));
        assertEquals(WeaponQuality.Outcome.BREAK, WeaponQuality.roll(2, 99));

        // 4 -> 5 is 80 / 14 / 6.
        assertEquals(WeaponQuality.Outcome.UPGRADE, WeaponQuality.roll(4, 79));
        assertEquals(WeaponQuality.Outcome.DOWNGRADE, WeaponQuality.roll(4, 80));
        assertEquals(WeaponQuality.Outcome.DOWNGRADE, WeaponQuality.roll(4, 93));
        assertEquals(WeaponQuality.Outcome.BREAK, WeaponQuality.roll(4, 94));
        assertEquals(WeaponQuality.Outcome.BREAK, WeaponQuality.roll(4, 99));
    }

    @Test
    void theTopOfTheLadderIsFinal() {
        assertTrue(WeaponQuality.isMax(WeaponQuality.MAX));
        assertFalse(WeaponQuality.isMax(WeaponQuality.MAX - 1));
        assertEquals(WeaponQuality.MAX, WeaponQuality.clamp(99));
        assertEquals(WeaponQuality.MIN, WeaponQuality.clamp(-5));
    }

    @Test
    void coloursFollowTheFourVanillaBuckets() {
        assertEquals(0, WeaponQuality.rarityBucket(0));
        assertEquals(0, WeaponQuality.rarityBucket(1));
        assertEquals(1, WeaponQuality.rarityBucket(2));
        assertEquals(2, WeaponQuality.rarityBucket(3));
        assertEquals(3, WeaponQuality.rarityBucket(4));
        assertEquals(3, WeaponQuality.rarityBucket(5));
    }

    @Test
    void theLangKeysAreStable() {
        assertEquals("quality.eldritch_horror.3", WeaponQuality.nameKey(3));
        assertEquals("quality.eldritch_horror.line", WeaponQuality.lineKey());
    }
}
