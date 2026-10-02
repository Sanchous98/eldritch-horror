package com.sanchous98.eldritchhorror.rite;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * The rite registry: the eight seed rites from {@code design/08-rituals.md}, seeded in table order,
 * plus the three Ancient One non-combat solve rites from {@code design/28-ancient-ones.md}.
 *
 * <p>A Java registry for now; a datapack loader can populate {@link #register} later without
 * changing callers (mirrors {@code cult/Cults.java}). Definitions are immutable data and this class
 * holds no per-player state — knowledge lives on the {@code RITE_KNOWLEDGE} attachment and is
 * accessed through {@link RiteKnowledge}.
 *
 * <p>Registration order is deterministic (insertion order); {@link #register} replaces an existing
 * id in place rather than reordering.
 */
public final class Rites {

    private static final CopyOnWriteArrayList<RiteDefinition> ALL = new CopyOnWriteArrayList<>();

    static {
        // Tier / outcomes are from design/08-rituals.md; the sanity/corruption fields are that
        // table's COST column (the price paid on success), not its Outcome column.
        register(new RiteDefinition("ward_of_the_eye", RiteDefinition.nameOf("ward_of_the_eye"),
                1, -8, 0, RiteDefinition.Outcome.GRANT, "ward_token"));
        register(new RiteDefinition("drowned_blessing", RiteDefinition.nameOf("drowned_blessing"),
                1, -10, 0, RiteDefinition.Outcome.GRANT_SKILL, "water_breathing"));
        register(new RiteDefinition("call_the_lesser", RiteDefinition.nameOf("call_the_lesser"),
                2, -15, 5, RiteDefinition.Outcome.SPAWN, ""));
        register(new RiteDefinition("summon_star_spawn", RiteDefinition.nameOf("summon_star_spawn"),
                3, -20, 8, RiteDefinition.Outcome.SPAWN, ""));
        register(new RiteDefinition("open_rift", RiteDefinition.nameOf("open_rift"),
                3, -25, 10, RiteDefinition.Outcome.OPEN_RIFT, ""));
        register(new RiteDefinition("close_rift", RiteDefinition.nameOf("close_rift"),
                2, 0, 5, RiteDefinition.Outcome.CLOSE_RIFT, ""));
        register(new RiteDefinition("rite_of_cleansing", RiteDefinition.nameOf("rite_of_cleansing"),
                3, -15, 5, RiteDefinition.Outcome.CORRUPTION, ""));
        register(new RiteDefinition("respec", RiteDefinition.nameOf("respec"),
                3, -20, 0, RiteDefinition.Outcome.GRANT_SKILL, "respec"));

        // Ancient One non-combat solves (design/28-ancient-ones.md). One shared SOOTHE outcome,
        // parameterised by the entity to find in the `grant` field (mirroring how grant/grantSkill
        // already carry their own key). Reagent/gate: the same rite-knowledge gate used by every
        // rite (a tome), so no new currency is invented.
        register(new RiteDefinition("soothe_cthulhu", RiteDefinition.nameOf("soothe_cthulhu"),
                2, -10, 0, RiteDefinition.Outcome.SOOTHE, "cthulhu"));
        register(new RiteDefinition("draw_away_dunwich", RiteDefinition.nameOf("draw_away_dunwich"),
                2, -10, -5, RiteDefinition.Outcome.SOOTHE, "dunwich_horror"));
        register(new RiteDefinition("still_shub_niggurath", RiteDefinition.nameOf("still_shub_niggurath"),
                3, -15, -8, RiteDefinition.Outcome.SOOTHE, "shub_niggurath"));

        // Ancient One solves, batch 2 (design/28). Same SOOTHE outcome, one per new presence.
        register(new RiteDefinition("still_azathoth", RiteDefinition.nameOf("still_azathoth"),
                2, -12, 0, RiteDefinition.Outcome.SOOTHE, "azathoth"));
        register(new RiteDefinition("seal_the_gate", RiteDefinition.nameOf("seal_the_gate"),
                3, -15, 0, RiteDefinition.Outcome.SOOTHE, "yog_sothoth"));
        register(new RiteDefinition("ward_ithaqua", RiteDefinition.nameOf("ward_ithaqua"),
                2, -10, -5, RiteDefinition.Outcome.SOOTHE, "ithaqua"));
        register(new RiteDefinition("appease_yig", RiteDefinition.nameOf("appease_yig"),
                2, -10, 0, RiteDefinition.Outcome.SOOTHE, "yig"));
        register(new RiteDefinition("bind_atlach_nacha", RiteDefinition.nameOf("bind_atlach_nacha"),
                3, -15, -8, RiteDefinition.Outcome.SOOTHE, "atlach_nacha"));
    }

    private Rites() {
    }

    /**
     * Registers a rite. A later registration with the same id replaces the earlier one in place
     * (keeping deterministic order); otherwise it is appended.
     */
    public static void register(RiteDefinition definition) {
        for (int i = 0; i < ALL.size(); i++) {
            if (ALL.get(i).id().equals(definition.id())) {
                ALL.set(i, definition);
                return;
            }
        }
        ALL.add(definition);
    }

    /** @return the definition for {@code id}, or {@code null} if unknown. */
    public static RiteDefinition byId(String id) {
        for (RiteDefinition definition : ALL) {
            if (definition.id().equals(id)) {
                return definition;
            }
        }
        return null;
    }

    /** @return all registered rites, in registration order. */
    public static List<RiteDefinition> all() {
        return List.copyOf(ALL);
    }
}
