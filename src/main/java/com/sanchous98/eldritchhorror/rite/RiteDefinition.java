package com.sanchous98.eldritchhorror.rite;

import net.minecraft.network.chat.Component;

/**
 * A data-driven rite: one ceremony the player can perform at an altar once they <b>know</b> it.
 *
 * <p>This is plain, immutable data — the Java-registry seed layer for what {@code design/26-rituals-and-occult.md}
 * describes as a datapack later. A rite is never free of both axes: {@code sanity} and
 * {@code corruption} are the price paid on success, and the {@link Outcome} is what the ceremony
 * produces (resolution itself is deferred; see {@code design/05-ritual-engine.md}).
 *
 * <p>See {@code design/08-rituals.md} for the canonical rite table and outcome vocabulary.
 *
 * @param id         stable snake_case id (also the knowledge key and the localisation suffix)
 * @param name       translatable display name ({@code rite.eldritch_horror.<id>})
 * @param tier       altar tier that gates the rite (1…3)
 * @param sanity     sanity delta applied on success (may be negative)
 * @param corruption corruption delta applied on success (may be negative)
 * @param outcome    the kind of effect the rite resolves to
 * @param grant      token/skill id this rite grants, or {@code ""} when it grants nothing
 */
public record RiteDefinition(
        String id,
        Component name,
        int tier,
        int sanity,
        int corruption,
        Outcome outcome,
        String grant) {

    /**
     * The outcome vocabulary from {@code design/08-rituals.md} / {@code design/26-rituals-and-occult.md}.
     *
     * <p>{@code ward_of_the_eye} resolves through {@link #GRANT} (a ward token) and {@code respec}
     * through {@link #GRANT_SKILL} (a skill reset), so no separate {@code WARD}/{@code RESET}
     * constants exist: the engine's registered outcome types are the source of truth.
     */
    public enum Outcome {
        /** {@code grant} — a token/permanent boon. */
        GRANT,
        /** {@code grant_skill} — a skill or a skill reset. */
        GRANT_SKILL,
        /** {@code spawn} — summon an entity or swarm. */
        SPAWN,
        /** {@code transform} — change the performer. */
        TRANSFORM,
        /** {@code curse} — apply a lasting bane. */
        CURSE,
        /** {@code open_rift} — open a Veil gate / rift. */
        OPEN_RIFT,
        /** {@code close_rift} — seal a rift. */
        CLOSE_RIFT,
        /** {@code reputation} — shift cult standing. */
        REPUTATION,
        /** {@code sanity} — a sanity-only resolution. */
        SANITY,
        /** {@code corruption} — a corruption-only resolution (e.g. cleansing). */
        CORRUPTION
    }

    /** Builds the translatable name for a rite id. */
    public static Component nameOf(String id) {
        return Component.translatable("rite.eldritch_horror." + id);
    }
}
