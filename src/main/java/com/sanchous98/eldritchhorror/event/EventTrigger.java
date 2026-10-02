package com.sanchous98.eldritchhorror.event;

/**
 * The kind of trigger an {@link EldritchEvent} reacts to (design/19-events.md). The enum is the
 * readable label for {@code /eh events} and tells the ticker which cheap context facts to gather —
 * in particular whether a rift scan is needed at all — so evaluating a trigger that does not care
 * about rifts pays nothing for one.
 */
public enum EventTrigger {
    /** Low sanity anywhere in the overworld. */
    LOW_SANITY_ANYWHERE(false),
    /** Night (or equivalent darkness) with an open rift nearby. */
    NIGHT_NEAR_RIFT(true),
    /** An open rift has been present long enough to have aged (local taint is high). */
    RIFT_AGING(true),
    /** A storm, with either several rifts nearby or a heavily tainted local chunk. */
    STORM_MANY_RIFTS(true),
    /** Sanity below the hallucination threshold. */
    SANITY_BELOW_20(false),
    /** A rift that was near the player has closed (detected by the ticker). */
    RIFT_CLOSED(true);

    private final boolean usesRifts;

    EventTrigger(boolean usesRifts) {
        this.usesRifts = usesRifts;
    }

    /** Whether evaluating this trigger needs the bounded nearby-rift scan. */
    public boolean usesRifts() {
        return usesRifts;
    }
}
