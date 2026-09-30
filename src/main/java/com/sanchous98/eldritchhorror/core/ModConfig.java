package com.sanchous98.eldritchhorror.core;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Mod configuration. Common config is registered in {@code EldritchHorror}; split into
 * {@code STARTUP}/{@code SERVER} (or add a client config on the client-only side) as the
 * systems below come online.
 */
public final class ModConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue ENABLE_SANITY =
            BUILDER.comment("Enable the Sanity system. Disable for a pure-content playthrough.")
                    .define("enableSanity", true);

    public static final ModConfigSpec.DoubleValue SANITY_DRAIN_MULTIPLIER =
            BUILDER.comment("Global multiplier applied to all sanity drain.")
                    .defineInRange("sanityDrainMultiplier", 1.0, 0.0, 10.0);

    // --- Sanity sources (demo) -------------------------------------------------------------
    // The two sources below are the milestone's visible demo of the source framework. They are on
    // by default but deliberately gentle, so sanity drifts over minutes instead of seconds.

    /** Demo source: sanity drains in the dark (unlit spots and the overworld night). */
    public static final ModConfigSpec.BooleanValue ENABLE_SANITY_DARKNESS =
            BUILDER.comment("[demo] Enable the 'darkness' sanity source.")
                    .define("enableSanityDarkness", true);

    /** Demo drain rate, points per second; negative drains. */
    public static final ModConfigSpec.DoubleValue SANITY_DARKNESS_RATE =
            BUILDER.comment("[demo] Sanity change per second while dark (negative drains).")
                    .defineInRange("sanityDarknessRate", -0.05, -10.0, 0.0);

    /** Demo source: sanity recovers inside a curated city district. */
    public static final ModConfigSpec.BooleanValue ENABLE_SANITY_CITY =
            BUILDER.comment("[demo] Enable the 'city' sanity source.")
                    .define("enableSanityCity", true);

    /** Demo recovery rate, points per second; positive recovers. */
    public static final ModConfigSpec.DoubleValue SANITY_CITY_RATE =
            BUILDER.comment("[demo] Sanity change per second inside a city (positive recovers).")
                    .defineInRange("sanityCityRate", 0.08, 0.0, 10.0);

    // --- Sanity stage cut-offs (fractions of max sanity) -----------------------------------

    /** Below this fraction → Uneasy. */
    public static final ModConfigSpec.DoubleValue SANITY_UNEASY =
            BUILDER.comment("Sanity fraction below which the state becomes Uneasy.")
                    .defineInRange("sanityUneasy", 0.75, 0.0, 1.0);

    /** Below this fraction → Fraying (madness I). */
    public static final ModConfigSpec.DoubleValue SANITY_FRAYING =
            BUILDER.comment("Sanity fraction below which the state becomes Fraying.")
                    .defineInRange("sanityFraying", 0.5, 0.0, 1.0);

    /** Below this fraction → Breaking (madness II); reaching 0 → Marked. */
    public static final ModConfigSpec.DoubleValue SANITY_BREAKING =
            BUILDER.comment("Sanity fraction below which the state becomes Breaking.")
                    .defineInRange("sanityBreaking", 0.25, 0.0, 1.0);

    public static final ModConfigSpec.BooleanValue ENABLE_CORRUPTION_SPREAD =
            BUILDER.comment("Allow corruption to spread from altars and rifts.")
                    .define("enableCorruptionSpread", true);

    public static final ModConfigSpec.IntValue RITUAL_COOLDOWN_TICKS =
            BUILDER.comment("Minimum ticks between ritual attempts at the same altar.")
                    .defineInRange("ritualCooldownTicks", 40, 0, 72000);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private ModConfig() {
    }
}
