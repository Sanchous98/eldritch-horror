package com.sanchous98.eldritchhorror.classes;

import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

/**
 * The three starting archetypes from {@code design/14-classes.md}. The enum is the canonical id
 * vocabulary: {@link ClassAPI} persists {@link #id()}, and {@link Classes} keys the kit/rite data
 * table off the constants.
 *
 * <p>This is deliberately the {@code classes} package, not the contract's literal
 * {@code ...eldritchhorror.class}: {@code class} is a Java reserved keyword and cannot be a package
 * segment. See the handoff note in {@code docs/PROLOGUE-CONTRACT.md}.
 */
public enum ClassId {
    INVESTIGATOR("investigator"),
    OCCULTIST("occultist"),
    CULTIST("cultist");

    private final String id;

    ClassId(String id) {
        this.id = id;
    }

    /** The stable, namespaced-free id persisted on the {@code PLAYER_CLASS} attachment. */
    public String id() {
        return this.id;
    }

    /**
     * @return the class with this id, or {@code null} when {@code id} is not one of the three
     */
    public static @Nullable ClassId byId(String id) {
        for (ClassId value : values()) {
            if (value.id.equals(id)) {
                return value;
            }
        }
        return null;
    }

    /** The localised display name, from lang key {@code class.eldritch_horror.<id>}. */
    public Component displayName() {
        return Component.translatable("class.eldritch_horror." + this.id);
    }
}
