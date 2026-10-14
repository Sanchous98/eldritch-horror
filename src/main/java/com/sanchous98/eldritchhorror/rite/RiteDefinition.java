package com.sanchous98.eldritchhorror.rite;

import net.minecraft.network.chat.Component;

/**
 * A data-driven rite: one ceremony the player can perform at an altar once they <b>know</b> it.
 *
 * <p>This is plain, immutable data — the Java-registry seed layer for what {@code design/26-rituals-and-occult.md}
 * describes as a datapack later. The two meter fields are the rite's <b>on_failure</b> price from
 * {@code design/08-rituals.md}'s "Cost on failure" column: paid when the conditions do not hold or
 * the outcome refuses, never by a rite that takes effect. A successful rite instead applies its
 * {@link Gains} — the table's Outcome deltas (corruption for dark rites, sanity for wards) and the
 * signature-rite reputation of {@code design/09-cults.md}.
 *
 * <p>See {@code design/08-rituals.md} for the canonical rite table and outcome vocabulary.
 *
 * @param id         stable snake_case id (also the knowledge key and the localisation suffix)
 * @param name       translatable display name ({@code rite.eldritch_horror.<id>})
 * @param tier       altar tier that gates the rite (1…3)
 * @param sanity     sanity price paid when the rite fails (may be 0)
 * @param corruption corruption price paid when the rite fails (may be 0)
 * @param outcome    the kind of effect the rite resolves to
 * @param grant      token/skill/effect/entity id this rite uses, or {@code ""} when it needs none
 * @param gains      the Outcome deltas + reputation a successful rite applies
 */
public record RiteDefinition(
        String id,
        Component name,
        int tier,
        int sanity,
        int corruption,
        Outcome outcome,
        String grant,
        Gains gains) {

    /**
     * A successful rite's extra effects, from the Outcome column of {@code design/08-rituals.md}:
     * meter deltas that are not the {@link Outcome} itself, plus the reputation a cult's rite pays
     * ({@code design/09-cults.md}: "performing their signature rite").
     *
     * @param sanity     sanity delta applied on success (may be negative)
     * @param corruption corruption delta applied on success (negative cleanses)
     * @param cult       cult id that gains standing on success ({@code ""} for none)
     * @param amount     reputation gained with {@link #cult()} on success
     */
    public record Gains(int sanity, int corruption, String cult, int amount) {

        /** No extra effects — the shape of a plain rite. */
        public static final Gains NONE = new Gains(0, 0, "", 0);

        /** @return whether this pays nothing beyond the outcome itself. */
        public boolean silent() {
            return this.sanity == 0 && this.corruption == 0 && this.amount == 0;
        }
    }

    /** A rite with no extra Outcome deltas and no reputation (the common shape). */
    public RiteDefinition(String id, Component name, int tier, int sanity, int corruption,
                          Outcome outcome, String grant) {
        this(id, name, tier, sanity, corruption, outcome, grant, Gains.NONE);
    }

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
        /** {@code transform} — convert blocks/entities toward the taint (design/05). */
        TRANSFORM,
        /** {@code curse} — lay a lasting bane on the performer (design/05). */
        CURSE,
        /** {@code open_rift} — open a rift gate / rift. */
        OPEN_RIFT,
        /** {@code close_rift} — seal a rift. */
        CLOSE_RIFT,
        /** {@code reputation} — standing itself is the outcome. */
        REPUTATION,
        /** {@code sanity} — a sanity-only resolution. */
        SANITY,
        /** {@code corruption} — a corruption-only resolution (e.g. cleansing). */
        CORRUPTION,
        /**
         * The endgame awakening (design/11): places {@code The Horror} next to the performer at a
         * rift. Refuses while one is already awake nearby.
         */
        AWAKEN_HORROR,
        /** Path A of the endgame fork (design/11): banish The Horror — costly, the world recedes. */
        BANISH_HORROR,
        /** Path B of the endgame fork (design/11): summon The Horror — the Choir's ending. */
        SUMMON_HORROR,
        /**
         * A non-combat solve of an Ancient One present in the world (design/28: "always a non-combat
         * solve"). The rite's {@code grant} names the entity family to find (e.g. {@code cthulhu});
         * {@link RiteEngine} resolves the nearest one within {@code riteSolveRadius} and asks it to
         * solve. Added additively — no existing outcome changes.
         */
        SOOTHE
    }

    /** Builds the translatable name for a rite id. */
    public static Component nameOf(String id) {
        return Component.translatable("rite.eldritch_horror." + id);
    }
}
