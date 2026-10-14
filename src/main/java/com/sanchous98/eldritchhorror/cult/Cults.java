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
                        CultService.offer("offer_pearls", CultRank.NEUTRAL, "pearl_reagent", 1, 5),
                        new CultService("teach_drowned_blessing", CultRank.INITIATE,
                                CultService.Kind.TEACH, "drowned_blessing"),
                        CultService.trade("buy_salt", CultRank.INITIATE,
                                "black_obol", 1, "salt_reagent", 2))));

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
                        CultService.offer("offer_pages", CultRank.NEUTRAL, "fragment_page", 1, 5),
                        new CultService("teach_ward_of_the_eye", CultRank.INITIATE,
                                CultService.Kind.TEACH, "ward_of_the_eye"),
                        new CultService("cleansing", CultRank.DEVOTED,
                                CultService.Kind.CLEANSE, ""),
                        CultService.trade("buy_chalk", CultRank.INITIATE,
                                "black_obol", 1, "chalk_reagent", 2))));

        register(new CultDefinition(
                "hollow_choir", "The Hollow Choir",
                "Deep places, rifts, the space behind them",
                List.of(Component.translatable("rank.eldritch_horror.hollow_choir.0"),
                        Component.translatable("rank.eldritch_horror.hollow_choir.1"),
                        Component.translatable("rank.eldritch_horror.hollow_choir.2"),
                        Component.translatable("rank.eldritch_horror.hollow_choir.3")),
                List.of("drowned_choir", "unblinking_eye"),
                "summon_star_spawn",
                "The cult that wants the horror summoned.",
                List.of(
                        CultService.offer("offer_blood", CultRank.NEUTRAL, "blood_offering", 1, 5),
                        new CultService("teach_call_the_lesser", CultRank.INITIATE,
                                CultService.Kind.TEACH, "call_the_lesser"),
                        new CultService("teach_blight_the_earth", CultRank.INITIATE,
                                CultService.Kind.TEACH, "blight_the_earth"),
                        new CultService("teach_take_the_hollow_mark", CultRank.MEMBER,
                                CultService.Kind.TEACH, "take_the_hollow_mark"),
                        new CultService("teach_summon_star_spawn", CultRank.DEVOTED,
                                CultService.Kind.TEACH, "summon_star_spawn"),
                        new CultService("teach_open_rift", CultRank.INNER_CIRCLE,
                                CultService.Kind.TEACH, "open_rift"),
                        CultService.trade("buy_bone", CultRank.INITIATE,
                                "black_obol", 1, "bone_reagent", 2),
                        CultService.trade("buy_litanies", CultRank.DEVOTED,
                                "black_obol", 4, "cult_litanies", 1))));
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
