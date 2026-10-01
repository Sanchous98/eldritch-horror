package com.sanchous98.eldritchhorror.cult;

import net.minecraft.network.chat.Component;

/**
 * A rank-gated cult service: the concrete payoff of reputation, seeded per {@code design/09-cults.md}
 * ("services unlock at rank thresholds"). Plain data on {@link CultDefinition#services()}, resolved
 * server-side by {@link CultServices}.
 *
 * <p>Services are reputation-gated, not paid: performing one consumes no item or coin. {@code design/09}
 * calls the Order's cleansing "costly", so a future payment hook could be added — see
 * {@link CultServices} for the chosen behaviour.
 *
 * @param id      stable snake_case id (localisation suffix {@code service.eldritch_horror.<id>})
 * @param minRank lowest {@link CultRank} that unlocks the service
 * @param kind    what the service does when performed
 * @param target  the rite id taught by {@link Kind#TEACH}, or {@code ""} for other kinds
 */
public record CultService(String id, CultRank minRank, Kind kind, String target) {

    /** The kinds of service the system can resolve today. */
    public enum Kind {
        /** Teach {@link #target} to the caller via {@code RiteKnowledge} (design/26: cult-taught rites). */
        TEACH,
        /** Lower the caller's corruption and the local chunk taint (the Order's cleansing). */
        CLEANSE
    }

    /** @return the localised display name ({@code service.eldritch_horror.<id>}). */
    public Component name() {
        return Component.translatable("service.eldritch_horror." + id);
    }
}
