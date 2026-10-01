package com.sanchous98.eldritchhorror.cult;

import net.minecraft.network.chat.Component;

import java.util.Locale;

/**
 * The six-step reputation ladder shared by every cult, from {@code Outsider} (hostile) to
 * {@code Inner Circle} (the cult's summit). Cult-specific rank <em>names</em> (Acolyte, Witness,
 * …) live on {@link CultDefinition#ranks()}; this enum is the cult-independent band gameplay can
 * branch on without string matching. See {@code design/09-cults.md} and
 * {@code design/03c-reputation.md}.
 *
 * <p>Ladder order matters: {@link #ordinal()} increases with standing, so
 * {@link #atLeast(CultRank)} is the rank gate.
 */
public enum CultRank {
    /** Hostile standing: reputation below {@code 0}. */
    OUTSIDER,
    /** Unproven: reputation {@code 0…19}. */
    NEUTRAL,
    /** Reputation {@code ≥ 20}. */
    INITIATE,
    /** Reputation {@code ≥ 40}. */
    MEMBER,
    /** Reputation {@code ≥ 60}. */
    DEVOTED,
    /** Reputation {@code ≥ 80}. */
    INNER_CIRCLE;

    /** Reputation thresholds, low → high: {@code value ≥ threshold} ⇒ at least the matching band. */
    private static final int[] THRESHOLDS = {20, 40, 60, 80};

    /**
     * The rank for a (clamped) reputation value.
     *
     * @param reputation a value in {@code −100…+100}; negative gives {@link #OUTSIDER}
     */
    public static CultRank of(int reputation) {
        if (reputation < 0) {
            return OUTSIDER;
        }
        int bands = 0;
        while (bands < THRESHOLDS.length && reputation >= THRESHOLDS[bands]) {
            bands++;
        }
        // 0 bands ⇒ NEUTRAL; each band climbs one rung of the shared ladder.
        return values()[NEUTRAL.ordinal() + bands];
    }

    /** @return whether this rank is at least {@code other} on the shared ladder (low → high). */
    public boolean atLeast(CultRank other) {
        return ordinal() >= other.ordinal();
    }

    /** @return the localised generic rank name ({@code rank.eldritch_horror.<lowercase id>}). */
    public Component display() {
        return Component.translatable("rank.eldritch_horror." + name().toLowerCase(Locale.ROOT));
    }
}
