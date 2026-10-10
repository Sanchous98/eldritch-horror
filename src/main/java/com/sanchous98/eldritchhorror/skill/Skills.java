package com.sanchous98.eldritchhorror.skill;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.Nullable;

/**
 * The seed skill tree (design/13). Small and data-driven: each node's effect is applied where its
 * system runs, so a node is never a dead entry.
 */
public final class Skills {

    /** Lucid Mind: +10% maximum sanity. */
    public static final String LUCID_MIND = "lucid_mind";
    /** Warded Soul: −10% corruption gain. */
    public static final String WARDED_SOUL = "warded_soul";
    /** Iron Will: a further +15% maximum sanity, stacking with Lucid Mind. */
    public static final String IRON_WILL = "iron_will";
    /** Frugal Rites: a quarter of successful rites consume no offerings. */
    public static final String FRUGAL_RITES = "frugal_rites";

    private static final List<Skill> ALL = List.of(
            new Skill(LUCID_MIND, 1),
            new Skill(WARDED_SOUL, 1),
            new Skill(IRON_WILL, 1),
            new Skill(FRUGAL_RITES, 2));

    private static final Map<String, Skill> BY_ID = index(ALL);

    private Skills() {
    }

    private static Map<String, Skill> index(List<Skill> skills) {
        Map<String, Skill> out = new LinkedHashMap<>();
        for (Skill skill : skills) {
            out.put(skill.id(), skill);
        }
        return Map.copyOf(out);
    }

    /** @return all skills, in presentation order. */
    public static List<Skill> all() {
        return ALL;
    }

    /** @return the skill for {@code id}, or {@code null} if unknown. */
    public static @Nullable Skill byId(@Nullable String id) {
        return id == null ? null : BY_ID.get(id);
    }
}
