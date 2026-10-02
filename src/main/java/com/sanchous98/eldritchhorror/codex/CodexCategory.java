package com.sanchous98.eldritchhorror.codex;

import java.util.List;

/**
 * The codex sections (design/22-map-and-knowledge.md): knowledge of the <i>horror</i> is
 * progression, so each kind of discovery lists under its own heading. Iteration order is the
 * display order used by {@code /eh codex}.
 */
public enum CodexCategory {
    /** Fixed sites: the second-echelon ruins, vaults and scars. */
    SITE("site"),
    /** The curated settlements. */
    CITY("city"),
    /** The Ancient Ones. */
    BOSS("boss"),
    /** The bestiary (our mobs). */
    BESTIARY("bestiary"),
    /** Rites; derived from {@code RITE_KNOWLEDGE}, never stored separately. */
    RITE("rite"),
    /** Data-driven world events. */
    EVENT("event");

    private final String id;

    CodexCategory(String id) {
        this.id = id;
    }

    /** Stable category id, used in the attachment and by the command. */
    public String id() {
        return this.id;
    }

    /** All categories, in display order. */
    public static List<CodexCategory> ordered() {
        return List.of(values());
    }
}
