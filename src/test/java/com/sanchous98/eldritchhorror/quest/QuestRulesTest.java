package com.sanchous98.eldritchhorror.quest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * Guards the two pure decisions behind the quest journal: the {@code requires} gate and the
 * repeatable cooldown ({@code design/10-quests.md}). {@link QuestRules} deliberately imports no
 * Minecraft types, so it loads in a bare JVM.
 */
class QuestRulesTest {

    /** An arbitrary "now" for the cooldown assertions; the rules only compare differences. */
    private static final long NOW = 1_700_000_000L;

    // --- prerequisite gate -------------------------------------------------------------------

    @Test
    void noPrerequisiteIsAlwaysMet() {
        Set<String> completed = Set.of();
        assertTrue(QuestRules.prerequisiteMet("", completed::contains));
    }

    @Test
    void aCompletedPrerequisiteIsMet() {
        Set<String> completed = Set.of("first_rite", "first_steps");
        assertTrue(QuestRules.prerequisiteMet("first_rite", completed::contains));
    }

    @Test
    void anAbsentPrerequisiteIsUnmet() {
        Set<String> completed = Set.of("first_steps");
        assertFalse(QuestRules.prerequisiteMet("first_rite", completed::contains));
    }

    @Test
    void anUnknownPrerequisiteIdIsUnmet() {
        // A `requires` naming a quest missing from the loaded data must not open the gate, even when
        // every loaded quest is complete.
        Set<String> completed = Set.of("first_steps", "first_rite", "lorekeeper");
        assertFalse(QuestRules.prerequisiteMet("quest_not_in_data", completed::contains));
    }

    // --- repeatable cooldown -----------------------------------------------------------------

    @Test
    void neverCompletedIsAvailable() {
        assertTrue(QuestRules.isAvailable(NOW, QuestRules.NEVER_COMPLETED, QuestRules.DAY_SECONDS));
    }

    @Test
    void justCompletedIsNotAvailable() {
        assertFalse(QuestRules.isAvailable(NOW, NOW, QuestRules.DAY_SECONDS));
        assertEquals(QuestRules.DAY_SECONDS, QuestRules.remainingSeconds(NOW, NOW,
                QuestRules.DAY_SECONDS));
    }

    @Test
    void exactlyOnePeriodLaterIsAvailable() {
        assertTrue(QuestRules.isAvailable(NOW + QuestRules.DAY_SECONDS, NOW,
                QuestRules.DAY_SECONDS));
        assertTrue(QuestRules.isAvailable(NOW + QuestRules.WEEK_SECONDS, NOW,
                QuestRules.WEEK_SECONDS));
        assertEquals(0L, QuestRules.remainingSeconds(NOW + QuestRules.DAY_SECONDS, NOW,
                QuestRules.DAY_SECONDS));
    }

    @Test
    void oneSecondShortOfThePeriodIsStillCoolingDown() {
        assertFalse(QuestRules.isAvailable(NOW + QuestRules.WEEK_SECONDS - 1L, NOW,
                QuestRules.WEEK_SECONDS));
        assertEquals(1L, QuestRules.remainingSeconds(NOW + QuestRules.WEEK_SECONDS - 1L, NOW,
                QuestRules.WEEK_SECONDS));
    }

    @Test
    void aOnceOnlyQuestIgnoresTimeAndNeverHasACooldown() {
        assertTrue(QuestRules.isAvailable(NOW, NOW, 0L));
        assertTrue(QuestRules.isAvailable(NOW, QuestRules.NEVER_COMPLETED, 0L));
        assertEquals(0L, QuestRules.remainingSeconds(NOW, NOW, 0L));
        assertFalse(Repeat.ONCE.repeatable());
    }

    // --- data constants ----------------------------------------------------------------------

    @Test
    void repeatPeriodsMatchMinecraftDayAndWeekLengths() {
        // 24 000 ticks / 20 tps = 1 200 s; a week is seven of those.
        assertEquals(1200L, QuestRules.DAY_SECONDS);
        assertEquals(8400L, QuestRules.WEEK_SECONDS);
        assertEquals(QuestRules.DAY_SECONDS, Repeat.DAILY.periodSeconds());
        assertEquals(QuestRules.WEEK_SECONDS, Repeat.WEEKLY.periodSeconds());
        assertTrue(Repeat.DAILY.repeatable());
        assertTrue(Repeat.WEEKLY.repeatable());
    }
}
