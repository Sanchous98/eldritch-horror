package com.sanchous98.eldritchhorror.rite;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.jspecify.annotations.Nullable;

/**
 * The rite registry: the eight seed rites from {@code design/08-rituals.md} plus two Hollow Choir
 * rites that exercise the {@code TRANSFORM}/{@code CURSE} outcomes, all seeded in table order, then
 * the Ancient One non-combat solve rites from {@code design/28-ancient-ones.md}.
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

    /** O(1) id index over {@link #ALL}, kept in sync by {@link #register}. */
    private static final java.util.Map<String, RiteDefinition> BY_ID =
            new java.util.concurrent.ConcurrentHashMap<>();

    static {
        // Tier / outcomes are from design/08-rituals.md; the sanity/corruption fields are that
        // table's COST column (the price paid on success), not its Outcome column.
        register(new RiteDefinition("ward_of_the_eye", RiteDefinition.nameOf("ward_of_the_eye"),
                1, -8, 0, RiteDefinition.Outcome.GRANT, "ward_token",
                new RiteDefinition.Gains(0, -3, "unblinking_eye", 10)));
        register(new RiteDefinition("drowned_blessing", RiteDefinition.nameOf("drowned_blessing"),
                1, -10, 0, RiteDefinition.Outcome.GRANT_SKILL, "water_breathing",
                new RiteDefinition.Gains(0, 0, "drowned_choir", 10)));
        register(new RiteDefinition("call_the_lesser", RiteDefinition.nameOf("call_the_lesser"),
                2, -15, 5, RiteDefinition.Outcome.SPAWN, ""));
        register(new RiteDefinition("summon_star_spawn", RiteDefinition.nameOf("summon_star_spawn"),
                3, -20, 8, RiteDefinition.Outcome.SPAWN, "",
                new RiteDefinition.Gains(0, 12, "hollow_choir", 10)));
        register(new RiteDefinition("open_rift", RiteDefinition.nameOf("open_rift"),
                3, -25, 10, RiteDefinition.Outcome.OPEN_RIFT, "",
                new RiteDefinition.Gains(0, 15, "", 0)));
        register(new RiteDefinition("close_rift", RiteDefinition.nameOf("close_rift"),
                2, 0, 5, RiteDefinition.Outcome.CLOSE_RIFT, "",
                new RiteDefinition.Gains(10, 0, "", 0)));
        register(new RiteDefinition("rite_of_cleansing", RiteDefinition.nameOf("rite_of_cleansing"),
                3, -15, 5, RiteDefinition.Outcome.CORRUPTION, "",
                new RiteDefinition.Gains(0, -20, "", 0)));
        register(new RiteDefinition("respec", RiteDefinition.nameOf("respec"),
                3, -20, 0, RiteDefinition.Outcome.GRANT_SKILL, "respec"));

        // Hollow Choir additions (design/26: pacts and the corruption axis). These two exercise the
        // TRANSFORM and CURSE outcome types: turning the ground toward the taint, and taking a lasting
        // bane in exchange for standing. Both are taught by the Hollow Choir (cult/Cults.java).
        register(new RiteDefinition("blight_the_earth", RiteDefinition.nameOf("blight_the_earth"),
                2, -12, 0, RiteDefinition.Outcome.TRANSFORM, "",
                new RiteDefinition.Gains(0, 4, "hollow_choir", 5)));
        register(new RiteDefinition("take_the_hollow_mark", RiteDefinition.nameOf("take_the_hollow_mark"),
                2, -12, 0, RiteDefinition.Outcome.CURSE, "minecraft:weakness",
                new RiteDefinition.Gains(0, 5, "hollow_choir", 10)));

        // The endgame fork (design/11-endgame.md). Awaken the Horror at a deep rift, then decide:
        // banish (the Order's costly path) or summon (the Hollow Choir's). World work lives in
        // endgame/Endgame; the rites are taught by the cults (cult/Cults.java).
        register(new RiteDefinition("awaken_the_horror", RiteDefinition.nameOf("awaken_the_horror"),
                3, -25, 10, RiteDefinition.Outcome.AWAKEN_HORROR, "",
                new RiteDefinition.Gains(0, 10, "hollow_choir", 5)));
        register(new RiteDefinition("banish_the_horror", RiteDefinition.nameOf("banish_the_horror"),
                3, -25, 0, RiteDefinition.Outcome.BANISH_HORROR, ""));
        register(new RiteDefinition("summon_the_horror", RiteDefinition.nameOf("summon_the_horror"),
                3, 0, 15, RiteDefinition.Outcome.SUMMON_HORROR, ""));

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

        // Ancient One solves, batch 3 (design/28). Same SOOTHE outcome, one per new presence.
        register(new RiteDefinition("deny_nyarlathotep", RiteDefinition.nameOf("deny_nyarlathotep"),
                2, -12, 0, RiteDefinition.Outcome.SOOTHE, "nyarlathotep"));
        register(new RiteDefinition("quench_cthugha", RiteDefinition.nameOf("quench_cthugha"),
                2, -10, -5, RiteDefinition.Outcome.SOOTHE, "cthugha"));
        register(new RiteDefinition("still_glaaki", RiteDefinition.nameOf("still_glaaki"),
                2, -12, 0, RiteDefinition.Outcome.SOOTHE, "glaaki"));
        register(new RiteDefinition("sever_hydra", RiteDefinition.nameOf("sever_hydra"),
                3, -15, -6, RiteDefinition.Outcome.SOOTHE, "hydra"));
        register(new RiteDefinition("bar_nyogtha", RiteDefinition.nameOf("bar_nyogtha"),
                3, -15, 0, RiteDefinition.Outcome.SOOTHE, "nyogtha"));
        register(new RiteDefinition("topple_idol", RiteDefinition.nameOf("topple_idol"),
                2, -12, 0, RiteDefinition.Outcome.SOOTHE, "rhan_tegoth"));

        // Ancient One solves, batch 4 (design/28). Same SOOTHE outcome, one per new presence.
        register(new RiteDefinition("silence_hastur", RiteDefinition.nameOf("silence_hastur"),
                3, -15, 0, RiteDefinition.Outcome.SOOTHE, "hastur"));
        register(new RiteDefinition("unmake_nephren_ka", RiteDefinition.nameOf("unmake_nephren_ka"),
                2, -12, -8, RiteDefinition.Outcome.SOOTHE, "nephren_ka"));
        register(new RiteDefinition("seal_abhoth", RiteDefinition.nameOf("seal_abhoth"),
                3, -15, -10, RiteDefinition.Outcome.SOOTHE, "abhoth"));
        register(new RiteDefinition("starve_chaugnar_faugn",
                RiteDefinition.nameOf("starve_chaugnar_faugn"),
                2, -12, 0, RiteDefinition.Outcome.SOOTHE, "chaugnar_faugn"));
        register(new RiteDefinition("quench_tulzscha", RiteDefinition.nameOf("quench_tulzscha"),
                3, -18, 0, RiteDefinition.Outcome.SOOTHE, "tulzscha"));
        register(new RiteDefinition("erase_zstylzhemghi",
                RiteDefinition.nameOf("erase_zstylzhemghi"),
                3, -20, -12, RiteDefinition.Outcome.SOOTHE, "zstylzhemghi"));

        // The design/25 named additions (design/25 replacement table, design/18 boss): the Ithaqual
        // Shade's ward and the Choir Leviathan's soak rite. Same shared SOOTHE outcome; the reagents
        // fall to the shared default (their gate is the taught tome, not a new material).
        register(new RiteDefinition("still_the_shade", RiteDefinition.nameOf("still_the_shade"),
                2, -12, 0, RiteDefinition.Outcome.SOOTHE, "ithaqual_shade"));
        register(new RiteDefinition("still_the_leviathan",
                RiteDefinition.nameOf("still_the_leviathan"),
                3, -18, -5, RiteDefinition.Outcome.SOOTHE, "choir_leviathan"));
    }

    private Rites() {
    }

    /**
     * Registers a rite. A later registration with the same id replaces the earlier one in place
     * (keeping deterministic order); otherwise it is appended.
     */
    public static void register(RiteDefinition definition) {
        BY_ID.put(definition.id(), definition);
        for (int i = 0; i < ALL.size(); i++) {
            if (ALL.get(i).id().equals(definition.id())) {
                ALL.set(i, definition);
                return;
            }
        }
        ALL.add(definition);
    }

    /** @return the definition for {@code id}, or {@code null} if unknown. */
    public static @Nullable RiteDefinition byId(@Nullable String id) {
        return id == null ? null : BY_ID.get(id);
    }

    /** @return all registered rites, in registration order. */
    public static List<RiteDefinition> all() {
        return List.copyOf(ALL);
    }
}
