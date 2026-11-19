package com.sanchous98.eldritchhorror.quest;

import java.util.function.Predicate;

/**
 * The pieces of quest eligibility that are pure arithmetic: the prerequisite gate and the
 * repeatable cooldown. Deliberately Minecraft-free (the {@code ReputationDecayMath} precedent) so
 * {@code QuestRulesTest} can assert on it in a bare JVM — {@link QuestJournal} supplies the live
 * state and this class only decides.
 *
 * <p>Times are wall-clock <b>epoch seconds</b>, matching
 * {@link com.sanchous98.eldritchhorror.registry.ModAttachments#QUEST_COMPLETIONS}. Epoch seconds
 * were chosen over game ticks so a cooldown keeps running across a server restart.
 */
public final class QuestRules {

    /**
     * One Minecraft day in real seconds: 24 000 ticks at 20 ticks per second = 1 200 s (20 real
     * minutes). This is the {@code daily} cooldown.
     */
    public static final long DAY_SECONDS = 1200L;

    /**
     * One Minecraft week in real seconds: seven days = 7 × 1 200 = 8 400 s (2 h 20 min). This is
     * the {@code weekly} cooldown.
     */
    public static final long WEEK_SECONDS = 8400L;

    /**
     * Sentinel stored for a quest that has never been completed. An already-saved world has no
     * {@code QUEST_COMPLETIONS} entry, which reads back as this value, so no existing quest is
     * ever treated as being on cooldown.
     */
    public static final long NEVER_COMPLETED = -1L;

    private QuestRules() {
    }

    /**
     * The prerequisite gate: a quest may only advance when its {@code requires} quest is already
     * complete for this player.
     *
     * @param requires   the prerequisite quest id, or {@code ""} for none
     * @param isComplete tells whether a quest id is complete for this player; it must also report
     *                   {@code false} for an id that is not in the loaded data, so a dangling
     *                   {@code requires} stays unmet
     * @return whether the prerequisite is satisfied
     */
    public static boolean prerequisiteMet(String requires, Predicate<String> isComplete) {
        return requires.isEmpty() || isComplete.test(requires);
    }

    /**
     * The repeatable cooldown: available when the quest has never been completed, when it has no
     * cooldown ({@code periodSeconds <= 0}, i.e. once-only), or when at least one full period has
     * passed since the last completion. Exactly one period later is available (the boundary is
     * inclusive), so a daily reopens on the tick its 1 200 s are up.
     *
     * @param now            current time in epoch seconds
     * @param lastCompleted  last completion in epoch seconds, or {@link #NEVER_COMPLETED}
     * @param periodSeconds  cooldown length in seconds (0 for once-only)
     * @return whether a completion/reward is allowed now
     */
    public static boolean isAvailable(long now, long lastCompleted, long periodSeconds) {
        if (periodSeconds <= 0L || lastCompleted == NEVER_COMPLETED) {
            return true;
        }
        return now - lastCompleted >= periodSeconds;
    }

    /**
     * @param now           current time in epoch seconds
     * @param lastCompleted last completion in epoch seconds, or {@link #NEVER_COMPLETED}
     * @param periodSeconds cooldown length in seconds (0 for once-only)
     * @return seconds left before the quest reopens, or 0 when it is available now
     */
    public static long remainingSeconds(long now, long lastCompleted, long periodSeconds) {
        if (isAvailable(now, lastCompleted, periodSeconds)) {
            return 0L;
        }
        return periodSeconds - (now - lastCompleted);
    }
}
