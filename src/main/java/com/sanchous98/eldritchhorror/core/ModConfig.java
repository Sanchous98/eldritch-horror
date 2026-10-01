package com.sanchous98.eldritchhorror.core;

import java.util.List;
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

    /** Source: the lethal polar end of the world (Morok) drains sanity with depth. */
    public static final ModConfigSpec.BooleanValue ENABLE_SANITY_MOROK =
            BUILDER.comment("Enable the 'morok' sanity source (polar drain past the charted edge).")
                    .define("enableSanityMorok", true);

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
            BUILDER.comment("Allow the per-chunk taint field to bleed into neighbouring chunks.")
                    .define("enableCorruptionSpread", true);

    // --- Corruption system ------------------------------------------------------------------

    /** Master switch for the per-player corruption meter and its threshold effects. */
    public static final ModConfigSpec.BooleanValue ENABLE_CORRUPTION =
            BUILDER.comment("Enable the Corruption system (meter ticking and stage effects).")
                    .define("enableCorruption", true);

    /** Global multiplier applied to all corruption gain/loss. */
    public static final ModConfigSpec.DoubleValue CORRUPTION_MULTIPLIER =
            BUILDER.comment("Global multiplier applied to all per-player corruption change.")
                    .defineInRange("corruptionMultiplier", 1.0, 0.0, 10.0);

    // --- Corruption sources -----------------------------------------------------------------

    /** Source: corruption creeps up while the player stands in a tainted chunk. */
    public static final ModConfigSpec.DoubleValue CORRUPTION_TAINT_RATE =
            BUILDER.comment("Corruption per second per unit of taint in the player's chunk.")
                    .defineInRange("corruptionTaintRate", 0.15, 0.0, 10.0);

    /** Taint spread: fraction of the excess above the threshold bled to each 4-neighbour per second. */
    public static final ModConfigSpec.DoubleValue TAINT_SPREAD_RATE =
            BUILDER.comment("Per-second fraction of a chunk's taint above the threshold bled to neighbours.")
                    .defineInRange("taintSpreadRate", 0.02, 0.0, 1.0);

    /** A chunk must exceed this taint before it bleeds into its neighbours. */
    public static final ModConfigSpec.DoubleValue TAINT_SPREAD_THRESHOLD =
            BUILDER.comment("Taint a chunk must exceed before it spreads to neighbouring chunks.")
                    .defineInRange("taintSpreadThreshold", 0.1, 0.0, 1.0);

    // --- Taint world effect (terrain conversion) --------------------------------------------

    /** Master switch for the visible world effect of the taint field (surface block conversion). */
    public static final ModConfigSpec.BooleanValue ENABLE_TAINT_WORLD =
            BUILDER.comment("Allow tainted chunks to convert a few natural surface blocks per second.")
                    .define("enableTaintWorld", true);

    /** Maximum natural surface blocks converted per loaded tainted chunk per second. Kept gentle. */
    public static final ModConfigSpec.IntValue TAINT_WORLD_RATE =
            BUILDER.comment("Max natural surface blocks converted per tainted chunk per second.")
                    .defineInRange("taintWorldRate", 3, 0, 64);

    /** A chunk must reach this taint before its terrain starts to convert. */
    public static final ModConfigSpec.DoubleValue TAINT_WORLD_THRESHOLD =
            BUILDER.comment("Taint a chunk must reach before its surface blocks convert.")
                    .defineInRange("taintWorldThreshold", 0.25, 0.0, 1.0);

    // --- Corruption stage cut-offs (fractions of DEFAULT_MAX) --------------------------------

    /** At or above this fraction → Touched. */
    public static final ModConfigSpec.DoubleValue CORRUPTION_TOUCHED =
            BUILDER.comment("Corruption fraction at or above which the state becomes Touched.")
                    .defineInRange("corruptionTouched", 0.25, 0.0, 1.0);

    /** At or above this fraction → Marked (corrupted effect). */
    public static final ModConfigSpec.DoubleValue CORRUPTION_MARKED =
            BUILDER.comment("Corruption fraction at or above which the state becomes Marked.")
                    .defineInRange("corruptionMarked", 0.5, 0.0, 1.0);

    /** At or above this fraction → Claimed (stronger corrupted effect). */
    public static final ModConfigSpec.DoubleValue CORRUPTION_CLAIMED =
            BUILDER.comment("Corruption fraction at or above which the state becomes Claimed.")
                    .defineInRange("corruptionClaimed", 0.85, 0.0, 1.0);

    public static final ModConfigSpec.IntValue RITUAL_COOLDOWN_TICKS =
            BUILDER.comment("Minimum ticks between ritual attempts at the same altar.")
                    .defineInRange("ritualCooldownTicks", 40, 0, 72000);

    // --- Mob suppression --------------------------------------------------------------------

    /**
     * Master switch that removes every vanilla mob from the world — natural spawns, monster
     * spawners, structure spawns, spawn eggs and {@code /summon}. Only mod entities and the
     * player remain. See {@code world/MobSuppressor}.
     */
    public static final ModConfigSpec.BooleanValue SUPPRESS_VANILLA_MOBS =
            BUILDER.comment("Remove all vanilla mobs from the world (spawns, spawners, eggs, /summon).")
                    .define("suppressVanillaMobs", true);

    /**
     * Vanilla entity ids exempt from suppression, e.g. {@code ["minecraft:villager"]}. Defaults to
     * {@code minecraft:villager} (humans stay); mod entities are always allowed regardless.
     */
    public static final ModConfigSpec.ConfigValue<List<? extends String>> SUPPRESSED_MOB_ALLOWLIST =
            BUILDER.comment("Vanilla entity ids exempt from mob suppression (default: villagers only).")
                    .defineListAllowEmpty("suppressedMobAllowlist", List.of("minecraft:villager"),
                            () -> "minecraft:villager", String.class::isInstance);

    // --- City population --------------------------------------------------------------------

    /**
     * Master switch for {@code world/CityPopulation}: actively spawns persistent villagers on the
     * paved streets of curated city districts so the streets feel busy. Vanilla natural village
     * spawning does not apply to these custom structures, so they would otherwise be empty.
     */
    public static final ModConfigSpec.BooleanValue ENABLE_CITY_POPULATION =
            BUILDER.comment("Populate curated city districts with villagers so the streets feel busy.")
                    .define("enableCityPopulation", true);

    /** Maximum villagers spawned per city per population pass (a pass runs about every 5 seconds). */
    public static final ModConfigSpec.IntValue CITY_POPULATION_PER_TICK =
            BUILDER.comment("Max villagers spawned per city per population pass (pass ~ every 5 s).")
                    .defineInRange("cityPopulationPerTick", 4, 0, 64);

    /** Real population per target villager; target = clamp(population / this, 8, 60). */
    public static final ModConfigSpec.IntValue CITY_POPULATION_DENSITY =
            BUILDER.comment("Real population per villager; target = clamp(population / this, 8, 60).")
                    .defineInRange("cityPopulationDensity", 250000, 1000, 10000000);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private ModConfig() {
    }
}
