package com.sanchous98.eldritchhorror.cult;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The cult registry: the three seed cults. A Java registry for now; a datapack loader can populate
 * {@link #register} later without changing callers (see {@code design/09-cults.md}).
 *
 * <p>Definitions are immutable data; this class holds no per-player state (that lives on the
 * reputation attachment).
 */
public final class Cults {

    private static final Map<String, CultDefinition> BY_ID = new LinkedHashMap<>();

    static {
        register(new CultDefinition(
                "drowned_choir", "The Drowned Choir",
                "Coastlines, drowned temples, tide pools",
                List.of("Acolyte", "Tidebound", "Deep-Sworn", "Voice of the Choir"),
                List.of("unblinking_eye"),
                "drowned_blessing",
                "Tide-worshippers who trade breath for devotion."));

        register(new CultDefinition(
                "unblinking_eye", "The Order of the Unblinking Eye",
                "Libraries, observatories, warded vaults",
                List.of("Initiate", "Witness", "Archivist", "Keeper"),
                List.of("drowned_choir"),
                "ward_of_the_eye",
                "Scholars who catalogue the horror to survive it."));

        register(new CultDefinition(
                "hollow_choir", "The Hollow Choir",
                "Deep places, rifts, the Veil",
                List.of("Aspirant", "Vessel", "Hollowed", "Chorus-Speaker"),
                List.of(),
                "summon_star_spawn",
                "The cult that wants the horror summoned."));
    }

    private Cults() {
    }

    /** Registers a cult definition. Later registrations with the same id replace the earlier one. */
    public static void register(CultDefinition definition) {
        BY_ID.put(definition.id(), definition);
    }

    /** @return the definition for {@code id}, or {@code null} if unknown. */
    public static CultDefinition byId(String id) {
        return BY_ID.get(id);
    }

    /** @return all registered cult definitions, in registration order. */
    public static List<CultDefinition> all() {
        return List.copyOf(BY_ID.values());
    }
}
