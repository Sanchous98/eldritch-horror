package com.sanchous98.eldritchhorror.codex;

import net.minecraft.network.chat.Component;

/**
 * One immutable codex entry: a discovered id, the {@link CodexCategory} it groups under, and its
 * display name. This is pure data — the registry of entries is built once from the existing
 * location/entity/rite/event registries (see {@link CodexRegistry}); no per-entry Java class is
 * needed, only this record.
 *
 * @param id       stable entry id (the knowledge key)
 * @param category the section the entry lists under
 * @param name     display name (already localised by the owning registry where one exists)
 */
public record CodexEntry(String id, CodexCategory category, Component name) {

    /** Builds a translatable codex name, with a human-readable fallback when the key is absent. */
    public static Component nameOf(String key, String fallback) {
        return Component.translatableWithFallback("codex.eldritch_horror." + key, fallback);
    }
}
