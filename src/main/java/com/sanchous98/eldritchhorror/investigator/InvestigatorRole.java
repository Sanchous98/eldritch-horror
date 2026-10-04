package com.sanchous98.eldritchhorror.investigator;

import net.minecraft.network.chat.Component;

/**
 * The one-word strategy label that hints at an investigator's playstyle, from
 * {@code design/14-classes.md}. It is presentation only — a label, not a choice or a modifier; the
 * roster's actual numbers live in {@link Progression}.
 */
public enum InvestigatorRole {
    RESEARCH("research"),
    COMBAT("combat"),
    GATE_CLOSER("gate_closer"),
    MAGIC("magic"),
    SUPPORT("support"),
    EXPEDITION("expedition"),
    ALL_ROUNDER("all_rounder");

    private final String id;

    InvestigatorRole(String id) {
        this.id = id;
    }

    /** The stable, namespaced-free id used in lang keys and on signs. */
    public String id() {
        return this.id;
    }

    /** The localised display name, from lang key {@code role.eldritch_horror.<id>}. */
    public Component displayName() {
        return Component.translatable("role.eldritch_horror." + this.id);
    }
}
