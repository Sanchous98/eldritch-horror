package com.sanchous98.eldritchhorror.skill;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Regression guard for the rite cooldown arithmetic ({@code design/15-skills.md}: Rite Affinity
 * +10% ritual speed, stacking multiplicatively with the carried {@code ring_of_focus}).
 * {@link RitualCooldownMath} is deliberately free of game state, so it can be asserted on without a
 * running game.
 */
class RitualCooldownMathTest {

    /** The configured cooldown — {@code ModConfig.ritualCooldownTicks}'s default. */
    private static final int BASE = 40;

    @Test
    void zeroStillDisablesTheCooldown() {
        assertEquals(0, RitualCooldownMath.cooldownTicks(0, true, true));
    }

    @Test
    void noSkillAndNoRingKeepsTheBase() {
        assertEquals(BASE, RitualCooldownMath.cooldownTicks(BASE, false, false));
    }

    @Test
    void riteAffinityShortensTenPercent() {
        assertEquals(36, RitualCooldownMath.cooldownTicks(BASE, true, false));
    }

    @Test
    void ringOfFocusShortensTwentyPercent() {
        assertEquals(32, RitualCooldownMath.cooldownTicks(BASE, false, true));
    }

    @Test
    void theTwoReductionsStackMultiplicatively() {
        // 40 × 0.90 × 0.80 = 28.8, floored.
        assertEquals(28, RitualCooldownMath.cooldownTicks(BASE, true, true));
    }

    @Test
    void theWaitIsNeverShortenedBelowOneTick() {
        assertEquals(1, RitualCooldownMath.cooldownTicks(1, true, true));
        assertEquals(1, RitualCooldownMath.cooldownTicks(2, true, true));
    }
}
