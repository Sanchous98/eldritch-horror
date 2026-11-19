package com.sanchous98.eldritchhorror.skill;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * Guards the Lucid Dreams rite pool ({@code design/15-skills.md}: "sleep can teach a rite",
 * {@code dream_visions +1}): tier ≤ 2, never known, never the endgame fork.
 */
class LucidDreamPoolTest {

    private static final List<LucidDreamPool.Entry> RITES = List.of(
            new LucidDreamPool.Entry("ward_of_the_eye", 1),
            new LucidDreamPool.Entry("call_the_lesser", 2),
            new LucidDreamPool.Entry("summon_star_spawn", 3),
            new LucidDreamPool.Entry("awaken_the_horror", 3),
            new LucidDreamPool.Entry("respec", 3));

    @Test
    void keepsOnlyUnknownRitesOfTierTwoOrLower() {
        assertEquals(List.of("ward_of_the_eye", "call_the_lesser"),
                LucidDreamPool.eligible(RITES, Set.of()));
    }

    @Test
    void neverOffersAnAlreadyKnownRite() {
        assertEquals(List.of("call_the_lesser"),
                LucidDreamPool.eligible(RITES, Set.of("ward_of_the_eye")));
    }

    @Test
    void neverOffersTheEndgameFork() {
        assertTrue(LucidDreamPool.EXCLUDED.contains("awaken_the_horror"));
        assertTrue(LucidDreamPool.EXCLUDED.contains("banish_the_horror"));
        assertTrue(LucidDreamPool.EXCLUDED.contains("summon_the_horror"));
        assertTrue(LucidDreamPool.eligible(RITES, Set.of()).stream()
                .noneMatch(LucidDreamPool.EXCLUDED::contains));
    }

    @Test
    void anEmptyPoolMeansNoDream() {
        assertEquals(List.of(), LucidDreamPool.eligible(List.of(), Set.of()));
        assertEquals(List.of(), LucidDreamPool.eligible(RITES,
                Set.of("ward_of_the_eye", "call_the_lesser")));
    }
}
