package com.sanchous98.eldritchhorror.quest;

import com.mojang.serialization.Codec;

/**
 * How often a quest can be completed, from the optional {@code "repeat"} field of a
 * {@code quest/*.json} file (design/13, the "Side / repeatable quests" table of
 * {@code design/10-quests.md}).
 *
 * <p>Absent or {@code "once"} means the ordinary, one-shot journal quest. {@code "daily"} and
 * {@code "weekly"} use Minecraft's own day and week lengths expressed in real seconds (see
 * {@link QuestRules#DAY_SECONDS} / {@link QuestRules#WEEK_SECONDS}); the cooldown counts from the
 * <b>last completion</b>, so it does not drift with when the player logs in.
 *
 * <p>An unrecognised value fails the codec, so a typo is reported and that one file is skipped
 * rather than silently becoming a once-only quest.
 */
public enum Repeat {

    /** One-shot: the default; a completed quest never reopens. */
    ONCE("once", 0L),

    /** Reopens 1 200 real seconds (one Minecraft day) after each completion. */
    DAILY("daily", QuestRules.DAY_SECONDS),

    /** Reopens 8 400 real seconds (one Minecraft week) after each completion. */
    WEEKLY("weekly", QuestRules.WEEK_SECONDS);

    private final String key;
    private final long periodSeconds;

    Repeat(String key, long periodSeconds) {
        this.key = key;
        this.periodSeconds = periodSeconds;
    }

    /**
     * Datapack codec: the JSON/NBT string key of the field. A method rather than a constant so the
     * enum itself stays loadable without the serialization library (the unit test only needs the
     * periods, not the codec).
     *
     * @return the codec for the {@code "repeat"} field
     */
    public static Codec<Repeat> codec() {
        return Codec.STRING.xmap(Repeat::fromKey, Repeat::key);
    }

    /** @return the data-file key ({@code "once"}, {@code "daily"} or {@code "weekly"}). */
    public String key() {
        return this.key;
    }

    /** @return seconds that must pass after a completion before this quest reopens (0 for once). */
    public long periodSeconds() {
        return this.periodSeconds;
    }

    /** @return whether this quest can be completed more than once. */
    public boolean repeatable() {
        return this.periodSeconds > 0L;
    }

    private static Repeat fromKey(String key) {
        return switch (key) {
            case "once" -> ONCE;
            case "daily" -> DAILY;
            case "weekly" -> WEEKLY;
            default -> throw new IllegalArgumentException("Unknown quest repeat: " + key);
        };
    }
}
