package com.sanchous98.eldritchhorror.investigator;

import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

/**
 * The twelve named investigators from {@code design/14-classes.md}, replacing the old three class
 * archetypes. The enum is the canonical id vocabulary: {@link InvestigatorAPI} persists
 * {@link #id()}, and {@link Investigators} keys the kit/rite data table off the constants.
 *
 * <p>{@link #role()} is a presentation label, not a second choice.
 */
public enum Investigator {
    ELEANOR_VANCE("eleanor_vance", InvestigatorRole.RESEARCH),
    JACK_CORRIGAN("jack_corrigan", InvestigatorRole.RESEARCH),
    TOM_MALLORY("tom_mallory", InvestigatorRole.COMBAT),
    CORMAC_BLACKWOOD("cormac_blackwood", InvestigatorRole.COMBAT),
    SISTER_AGATHA("sister_agatha", InvestigatorRole.GATE_CLOSER),
    MARION_DELACROIX("marion_delacroix", InvestigatorRole.GATE_CLOSER),
    VERA_NIGHTINGALE("vera_nightingale", InvestigatorRole.MAGIC),
    NIKOLAI_VOLKOV("nikolai_volkov", InvestigatorRole.MAGIC),
    DR_AMOS_HARTLEY("dr_amos_hartley", InvestigatorRole.SUPPORT),
    EVELYN_ASHCOMBE("evelyn_ashcombe", InvestigatorRole.SUPPORT),
    ALDOUS_PEMBERTON("aldous_pemberton", InvestigatorRole.EXPEDITION),
    HAZEL_QUINN("hazel_quinn", InvestigatorRole.ALL_ROUNDER);

    private final String id;
    private final InvestigatorRole role;

    Investigator(String id, InvestigatorRole role) {
        this.id = id;
        this.role = role;
    }

    /** The stable, namespaced-free id persisted on the {@code INVESTIGATOR} attachment. */
    public String id() {
        return this.id;
    }

    /** The investigator's strategy label (presentation only). */
    public InvestigatorRole role() {
        return this.role;
    }

    /** The localised display name, from lang key {@code investigator.eldritch_horror.<id>}. */
    public Component displayName() {
        return Component.translatable("investigator.eldritch_horror." + this.id);
    }

    /**
     * @return the investigator with this id, or {@code null} when {@code id} is not one of the twelve
     */
    public static @Nullable Investigator byId(String id) {
        for (Investigator value : values()) {
            if (value.id.equals(id)) {
                return value;
            }
        }
        return null;
    }
}
