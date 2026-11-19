package com.sanchous98.eldritchhorror.skill;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * The rite pool a {@link Skills#LUCID_DREAMS} dream may teach: unknown rites of tier ≤ 2, never the
 * endgame fork. Kept free of game state so the selection rule is unit-testable (the definition
 * registry is passed in as plain {@link Entry} values).
 */
public final class LucidDreamPool {

    /** Deepest rite a dream may teach (design/15: {@code dream_visions +1}). */
    public static final int MAX_TIER = 2;

    /** The endgame fork rites are never dream-taught; the decision must be reached deliberately. */
    public static final Set<String> EXCLUDED = Set.of(
            "awaken_the_horror", "banish_the_horror", "summon_the_horror");

    private LucidDreamPool() {
    }

    /** One rite as the pool sees it: its id and altar tier. */
    public record Entry(String id, int tier) {
    }

    /**
     * @param all   every registered rite, in registry order
     * @param known the rites the player already knows
     * @return the ids a dream may teach, in registry order (empty when nothing is left)
     */
    public static List<String> eligible(List<Entry> all, Set<String> known) {
        List<String> out = new ArrayList<>();
        for (Entry entry : all) {
            if (entry.tier() <= MAX_TIER && !known.contains(entry.id())
                    && !EXCLUDED.contains(entry.id())) {
                out.add(entry.id());
            }
        }
        return out;
    }
}
