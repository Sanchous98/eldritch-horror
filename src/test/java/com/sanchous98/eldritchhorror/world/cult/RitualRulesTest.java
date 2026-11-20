package com.sanchous98.eldritchhorror.world.cult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sanchous98.eldritchhorror.world.cult.RitualRules.Completion;
import com.sanchous98.eldritchhorror.world.cult.RitualRules.Interruption;
import org.junit.jupiter.api.Test;

/**
 * Guards the game-free ritual-cell rules ({@code design/06-factions.md}, "Ritual cells", pinned
 * 2026-11): GATHER → CHANT timing, the ordered interruption precedence and the completion tier.
 * {@link RitualRules} touches no Minecraft type, so no server is needed.
 */
class RitualRulesTest {

    @Test
    void gatherEndsAfterItsTimer() {
        assertFalse(RitualRules.gatherDone(399, 400, 0));
        assertTrue(RitualRules.gatherDone(400, 400, 0));
    }

    @Test
    void gatherEndsEarlyWhenTwoCultistsArrive() {
        assertFalse(RitualRules.gatherDone(0, 400, 1));
        assertTrue(RitualRules.gatherDone(0, 400, 2));
        assertTrue(RitualRules.gatherDone(0, 400, 3));
    }

    @Test
    void chantEndsAfterItsFullLength() {
        assertFalse(RitualRules.chantDone(599, 600));
        assertTrue(RitualRules.chantDone(600, 600));
        assertTrue(RitualRules.chantDone(900, 600));
    }

    @Test
    void aRunningCellIsNotInterrupted() {
        assertEquals(Interruption.NONE, RitualRules.interruption(true, true, false, 3, 3));
    }

    @Test
    void leaderDeathOutranksEverything() {
        assertEquals(Interruption.LEADER,
                RitualRules.interruption(false, false, true, 0, 3));
    }

    @Test
    void aBrokenCoreOutranksThePlayerAndTheCount() {
        assertEquals(Interruption.CORE,
                RitualRules.interruption(true, false, true, 0, 3));
    }

    @Test
    void aPlayerInTheRingOutranksTheCount() {
        assertEquals(Interruption.PLAYER,
                RitualRules.interruption(true, true, true, 0, 3));
    }

    @Test
    void threeLossesCollapseTheCell() {
        assertFalse(RitualRules.tooFew(2, 4));
        assertTrue(RitualRules.tooFew(1, 4));
        assertEquals(Interruption.TOO_FEW, RitualRules.interruption(true, true, false, 1, 4));
    }

    @Test
    void aCellThatStartedWithFiveCollapsesAfterThreeLosses() {
        assertFalse(RitualRules.tooFew(3, 5));
        assertTrue(RitualRules.tooFew(2, 5));
    }

    @Test
    void onlyTheHollowChoirPerformsTheSummon() {
        assertEquals(Completion.SUMMON, RitualRules.completionFor("hollow_choir"));
        assertEquals(Completion.LESSER, RitualRules.completionFor("drowned_choir"));
        assertEquals(Completion.LESSER, RitualRules.completionFor("unblinking_eye"));
        assertEquals(Completion.LESSER, RitualRules.completionFor(""));
    }
}
