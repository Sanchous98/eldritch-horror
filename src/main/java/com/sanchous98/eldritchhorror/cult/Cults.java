package com.sanchous98.eldritchhorror.cult;

import net.minecraft.network.chat.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.Nullable;

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
                List.of(Component.translatable("rank.eldritch_horror.drowned_choir.0"),
                        Component.translatable("rank.eldritch_horror.drowned_choir.1"),
                        Component.translatable("rank.eldritch_horror.drowned_choir.2"),
                        Component.translatable("rank.eldritch_horror.drowned_choir.3")),
                List.of("unblinking_eye"),
                "drowned_blessing",
                "Tide-worshippers who trade breath for devotion.",
                List.of(
                        new CultService("teach_drowned_blessing", CultRank.INITIATE,
                                CultService.Kind.TEACH, "drowned_blessing"))));

        register(new CultDefinition(
                "unblinking_eye", "The Order of the Unblinking Eye",
                "Libraries, observatories, warded vaults",
                List.of(Component.translatable("rank.eldritch_horror.unblinking_eye.0"),
                        Component.translatable("rank.eldritch_horror.unblinking_eye.1"),
                        Component.translatable("rank.eldritch_horror.unblinking_eye.2"),
                        Component.translatable("rank.eldritch_horror.unblinking_eye.3")),
                List.of("drowned_choir"),
                "ward_of_the_eye",
                "Scholars who catalogue the horror to survive it.",
                List.of(
                        new CultService("teach_ward_of_the_eye", CultRank.INITIATE,
                                CultService.Kind.TEACH, "ward_of_the_eye"),
                        new CultService("cleansing", CultRank.DEVOTED,
                                CultService.Kind.CLEANSE, ""))));

        register(new CultDefinition(
                "hollow_choir", "The Hollow Choir",
                "Deep places, rifts, the Veil",
                List.of(Component.translatable("rank.eldritch_horror.hollow_choir.0"),
                        Component.translatable("rank.eldritch_horror.hollow_choir.1"),
                        Component.translatable("rank.eldritch_horror.hollow_choir.2"),
                        Component.translatable("rank.eldritch_horror.hollow_choir.3")),
                List.of("drowned_choir", "unblinking_eye"),
                "summon_star_spawn",
                "The cult that wants the horror summoned.",
                List.of(
                        new CultService("teach_call_the_lesser", CultRank.INITIATE,
                                CultService.Kind.TEACH, "call_the_lesser"),
                        new CultService("teach_summon_star_spawn", CultRank.DEVOTED,
                                CultService.Kind.TEACH, "summon_star_spawn"),
                        new CultService("teach_open_rift", CultRank.INNER_CIRCLE,
                                CultService.Kind.TEACH, "open_rift"))));
    }

    private Cults() {
    }

    /** Registers a cult definition. Later registrations with the same id replace the earlier one. */
    public static void register(CultDefinition definition) {
        BY_ID.put(definition.id(), definition);
    }

    /** @return the definition for {@code id}, or {@code null} if unknown. */
    public static @Nullable CultDefinition byId(String id) {
        return BY_ID.get(id);
    }

    /** @return all registered cult definitions, in registration order. */
    public static List<CultDefinition> all() {
        return List.copyOf(BY_ID.values());
    }
}
