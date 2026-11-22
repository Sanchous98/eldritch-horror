package com.sanchous98.eldritchhorror.registry.items;

/**
 * Per-instance quality of a weapon ({@code design/16-items.md}), separate from its
 * {@link WeaponGrade grade}.
 *
 * <p><b>Grade</b> is fixed by the item: it says what the thing is (Crude … Relic) and is
 * guaranteed by the recipe or the loot entry. <b>Quality</b> is rolled per stack and is the only
 * thing that colours the name: a player tempers a weapon with a whetstone, and each attempt can
 * improve it, drop it one step, or shatter it. The first two steps are safe (100%); from the third
 * on the risk of a downgrade or a break grows with the quality, so a Peerless blade is a real
 * gamble.
 *
 * <p>Minecraft-free by design: the odds and the roll take a plain {@code 0..99} value, so
 * {@code WeaponQualityTest} can pin every boundary without a game bootstrap. The stack/durability
 * plumbing lives in {@link WeaponQualityStacks}.
 */
public final class WeaponQuality {

    /** Lowest quality (a fresh weapon). */
    public static final int MIN = 0;

    /** Highest quality; no further tempering is possible. */
    public static final int MAX = 5;

    /** Max-durability bonus per quality step (+12% each, +60% at {@link #MAX}). */
    public static final float DURABILITY_PER_LEVEL = 0.12F;

    /** One step's outcome weights, in percent; the three must sum to 100. */
    public record Odds(int upgrade, int downgrade, int breakChance) {
        public Odds {
            if (upgrade < 0 || downgrade < 0 || breakChance < 0) {
                throw new IllegalArgumentException("odds must not be negative");
            }
            if (upgrade + downgrade + breakChance != 100) {
                throw new IllegalArgumentException("odds must sum to 100");
            }
        }
    }

    /** What one tempering attempt did. */
    public enum Outcome {
        UPGRADE,
        DOWNGRADE,
        BREAK
    }

    /**
     * Odds by current quality: the first two steps are guaranteed, then the failure window opens and
     * both the downgrade and the break chance climb. Deliberately generous - a session is short, so
     * a top-tier blade should be a reasonable evening's work, not a grind: the expected attempts are
     * 1.05 / 1.14 / 1.25 and the cumulative break risk about 1% / 3% / 7%. The last entry is never
     * rolled (the caller refuses at {@link #MAX}); it is 0/0/100 so the table stays total.
     */
    private static final Odds[] ODDS = {
            new Odds(100, 0, 0),
            new Odds(100, 0, 0),
            new Odds(95, 4, 1),
            new Odds(88, 9, 3),
            new Odds(80, 14, 6),
            new Odds(0, 0, 100)
    };

    private WeaponQuality() {
    }

    /** The odds for the given current quality, clamped into the ladder. */
    public static Odds odds(int quality) {
        return ODDS[clamp(quality)];
    }

    /** Whether this is the top of the ladder. */
    public static boolean isMax(int quality) {
        return quality >= MAX;
    }

    /** Clamps any stored value into {@code [MIN, MAX]}. */
    public static int clamp(int quality) {
        return Math.max(MIN, Math.min(MAX, quality));
    }

    /**
     * Resolves one attempt from a {@code 0..99} roll.
     *
     * @param quality current quality
     * @param percent a uniform value in {@code [0, 100)}
     */
    public static Outcome roll(int quality, int percent) {
        Odds odds = odds(quality);
        int roll = Math.max(0, Math.min(99, percent));
        if (roll < odds.upgrade()) {
            return Outcome.UPGRADE;
        }
        if (roll < odds.upgrade() + odds.downgrade()) {
            return Outcome.DOWNGRADE;
        }
        return Outcome.BREAK;
    }

    /** The localized quality name, e.g. {@code quality.eldritch_horror.2}. */
    public static String nameKey(int quality) {
        return "quality.eldritch_horror." + clamp(quality);
    }

    /** The tooltip line key, taking the quality name as {@code %s}. */
    public static String lineKey() {
        return "quality.eldritch_horror.line";
    }

    /** Colour bucket: 0 common, 1 uncommon, 2 rare, 3 epic (two lowest share white). */
    public static int rarityBucket(int quality) {
        int q = clamp(quality);
        if (q <= 1) {
            return 0;
        }
        if (q == 2) {
            return 1;
        }
        return q == 3 ? 2 : 3;
    }
}
