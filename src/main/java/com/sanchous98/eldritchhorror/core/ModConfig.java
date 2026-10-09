package com.sanchous98.eldritchhorror.core;

import java.util.List;
import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Mod configuration. Registered as a single {@code SERVER} config in {@code EldritchHorror}
 * (one {@link ModConfigSpec.Builder}); a client config can be split out later if needed.
 */
public final class ModConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue ENABLE_SANITY =
            BUILDER.comment("Enable the Sanity system. Disable for a pure-content playthrough.")
                    .define("enableSanity", true);

    /** One-time prologue: new players start in the Threshold and pick an investigator + city. */
    public static final ModConfigSpec.BooleanValue ENABLE_PROLOGUE =
            BUILDER.comment("Enable the one-time Threshold prologue (investigator choice + city gate).")
                    .define("enablePrologue", true);

    /** Whether the /eh investigator command is available. */
    public static final ModConfigSpec.BooleanValue ENABLE_INVESTIGATOR_COMMANDS =
            BUILDER.comment("Enable the /eh investigator command (get / set).")
                    .define("enableInvestigatorCommands", true);

    /** Whether players get a chat line when a world event starts or ends. */
    public static final ModConfigSpec.BooleanValue ENABLE_EVENT_NOTIFICATIONS =
            BUILDER.comment("Tell players in chat when a world event starts or ends.")
                    .define("enableEventNotifications", true);

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

    // --- City state (design/21): a city's condition tracks the corruption on its district, and
    // scales how much shelter it gives (Thriving → Uneasy → Besieged → Fallen). Derived from the
    // per-chunk taint field, never stored; see world/city/CityStates.

    /** District-average taint at/above which a city is Uneasy. */
    public static final ModConfigSpec.DoubleValue CITY_STATE_UNEASY_TAINT =
            BUILDER.comment("District taint at which a city becomes Uneasy (thinner shelter).")
                    .defineInRange("cityStateUneasyTaint", 0.10, 0.0, 1.0);

    /** District-average taint at/above which a city is Besieged. */
    public static final ModConfigSpec.DoubleValue CITY_STATE_BESIEGED_TAINT =
            BUILDER.comment("District taint at which a city becomes Besieged.")
                    .defineInRange("cityStateBesiegedTaint", 0.30, 0.0, 1.0);

    /** District-average taint at/above which a city is Fallen (no shelter). */
    public static final ModConfigSpec.DoubleValue CITY_STATE_FALLEN_TAINT =
            BUILDER.comment("District taint at which a city becomes Fallen (no shelter).")
                    .defineInRange("cityStateFallenTaint", 0.55, 0.0, 1.0);

    /** Source: the lethal polar end of the world (Morok) drains sanity with depth. */
    public static final ModConfigSpec.BooleanValue ENABLE_SANITY_MOROK =
            BUILDER.comment("Enable the 'morok' sanity source (polar drain past the charted edge).")
                    .define("enableSanityMorok", true);

    // --- Bestiary sanity source (risen husk) ------------------------------------------------

    /** Source: standing near one or more risen husks drains sanity (a presence, not a fight). */
    public static final ModConfigSpec.BooleanValue ENABLE_SANITY_RISEN_HUSK =
            BUILDER.comment("Enable the 'risen_husk' sanity source (nearby husks drain sanity).")
                    .define("enableSanityRisenHusk", true);

    /** Drain per counted husk per second; negative drains. */
    public static final ModConfigSpec.DoubleValue SANITY_RISEN_HUSK_RATE =
            BUILDER.comment("Sanity change per second per nearby risen husk (negative drains).")
                    .defineInRange("sanityRisenHuskRate", -0.08, -10.0, 0.0);

    /** Radius (blocks) within which a risen husk counts; loaded chunks only. */
    public static final ModConfigSpec.IntValue SANITY_RISEN_HUSK_RADIUS =
            BUILDER.comment("Radius (blocks) in which risen husks contribute to the sanity drain.")
                    .defineInRange("sanityRisenHuskRadius", 8, 1, 32);

    /** Most husks counted at once; keeps the drain bounded when a horde is present. */
    public static final ModConfigSpec.IntValue SANITY_RISEN_HUSK_MAX =
            BUILDER.comment("Maximum risen husks counted toward the sanity drain at once.")
                    .defineInRange("sanityRisenHuskMax", 4, 1, 32);

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

    // --- Bestiary spawns (risen husk) -------------------------------------------------------

    /**
     * Enable the bounded risen-husk spawner near players. Vanilla mobs are suppressed, so our
     * creatures need their own pass; it spawns only in already-loaded chunks, in the dark or in
     * tainted terrain, capped per pass and per player.
     */
    public static final ModConfigSpec.BooleanValue ENABLE_RISEN_HUSK_SPAWNS =
            BUILDER.comment("Allow risen husks to spawn near players in dark/tainted loaded terrain.")
                    .define("enableRisenHuskSpawns", true);

    /** Spawner pass interval, in ticks. */
    public static final ModConfigSpec.IntValue RISEN_HUSK_SPAWN_INTERVAL_TICKS =
            BUILDER.comment("Ticks between risen-husk spawn passes (minimum 20).")
                    .defineInRange("risenHuskSpawnIntervalTicks", 200, 20, 24000);

    /** Max husks spawned per player per pass. */
    public static final ModConfigSpec.IntValue RISEN_HUSK_SPAWN_COUNT =
            BUILDER.comment("Max risen husks spawned per player per pass.")
                    .defineInRange("risenHuskSpawnCount", 2, 0, 16);

    /** Radius (blocks) around a player in which spawns are attempted. */
    public static final ModConfigSpec.IntValue RISEN_HUSK_SPAWN_RADIUS =
            BUILDER.comment("Radius (blocks) around each player in which husks may spawn.")
                    .defineInRange("risenHuskSpawnRadius", 32, 8, 96);

    /** Per-player cap: no spawn pass runs while a player already has this many husks nearby. */
    public static final ModConfigSpec.IntValue RISEN_HUSK_SPAWN_CAP =
            BUILDER.comment("Max risen husks allowed near one player before spawning pauses.")
                    .defineInRange("risenHuskSpawnCap", 8, 1, 64);

    /** Real population per target villager; target = clamp(population / this, 8, 60). */
    public static final ModConfigSpec.IntValue CITY_POPULATION_DENSITY =
            BUILDER.comment("Real population per villager; target = clamp(population / this, 8, 60).")
                    .defineInRange("cityPopulationDensity", 250000, 1000, 10000000);

    // --- Bestiary dread auras (tainted_fauna / lesser_swarm / watcher) -----------------------

    /** Corruption vector: standing near tainted fauna adds corruption (mirrors the husk drain). */
    public static final ModConfigSpec.BooleanValue ENABLE_CORRUPTION_TAINTED_FAUNA =
            BUILDER.comment("Enable the 'tainted_fauna' corruption vector (nearby fauna add corruption).")
                    .define("enableCorruptionTaintedFauna", true);

    /** Corruption per second per nearby tainted fauna; positive taints. */
    public static final ModConfigSpec.DoubleValue CORRUPTION_TAINTED_FAUNA_RATE =
            BUILDER.comment("Corruption per second per nearby tainted fauna (positive taints).")
                    .defineInRange("corruptionTaintedFaunaRate", 0.03, 0.0, 10.0);

    /** Radius (blocks) within which tainted fauna add corruption; loaded chunks only. */
    public static final ModConfigSpec.IntValue CORRUPTION_TAINTED_FAUNA_RADIUS =
            BUILDER.comment("Radius (blocks) in which tainted fauna contribute corruption.")
                    .defineInRange("corruptionTaintedFaunaRadius", 6, 1, 32);

    /** Most tainted fauna counted at once, bounding a herd. */
    public static final ModConfigSpec.IntValue CORRUPTION_TAINTED_FAUNA_MAX =
            BUILDER.comment("Maximum tainted fauna counted toward the corruption vector at once.")
                    .defineInRange("corruptionTaintedFaunaMax", 4, 1, 32);

    /** Sanity source: a swarm of lesser horrors drains sanity faster the more of them there are. */
    public static final ModConfigSpec.BooleanValue ENABLE_SANITY_LESSER_SWARM =
            BUILDER.comment("Enable the 'lesser_swarm' sanity source (nearby swarms drain sanity in numbers).")
                    .define("enableSanityLesserSwarm", true);

    /** Drain per counted swarm member per second; negative drains. */
    public static final ModConfigSpec.DoubleValue SANITY_LESSER_SWARM_RATE =
            BUILDER.comment("Sanity change per second per nearby lesser swarm (negative drains).")
                    .defineInRange("sanityLesserSwarmRate", -0.10, -10.0, 0.0);

    /** Radius (blocks) within which lesser swarms count; loaded chunks only. */
    public static final ModConfigSpec.IntValue SANITY_LESSER_SWARM_RADIUS =
            BUILDER.comment("Radius (blocks) in which lesser swarms contribute to the sanity drain.")
                    .defineInRange("sanityLesserSwarmRadius", 8, 1, 32);

    /** Most swarm members counted at once; keeps the pack drain bounded. */
    public static final ModConfigSpec.IntValue SANITY_LESSER_SWARM_MAX =
            BUILDER.comment("Maximum lesser swarm members counted toward the sanity drain at once.")
                    .defineInRange("sanityLesserSwarmMax", 8, 1, 32);

    /** Sanity source: the Watcher is a presence — a steady, strong, single-target drain. */
    public static final ModConfigSpec.BooleanValue ENABLE_SANITY_WATCHER =
            BUILDER.comment("Enable the 'watcher' sanity source (the presence drains sanity).")
                    .define("enableSanityWatcher", true);

    /** Drain per second while a Watcher is near; negative drains. */
    public static final ModConfigSpec.DoubleValue SANITY_WATCHER_RATE =
            BUILDER.comment("Sanity change per second per nearby Watcher (negative drains).")
                    .defineInRange("sanityWatcherRate", -0.25, -10.0, 0.0);

    /** Radius (blocks) within which a Watcher counts; loaded chunks only. */
    public static final ModConfigSpec.IntValue SANITY_WATCHER_RADIUS =
            BUILDER.comment("Radius (blocks) in which a Watcher contributes to the sanity drain.")
                    .defineInRange("sanityWatcherRadius", 16, 1, 48);

    /** Most Watchers counted at once (a presence is meant to be singular). */
    public static final ModConfigSpec.IntValue SANITY_WATCHER_MAX =
            BUILDER.comment("Maximum Watchers counted toward the sanity drain at once.")
                    .defineInRange("sanityWatcherMax", 1, 1, 8);

    // --- Bestiary spawns (tainted_fauna / lesser_swarm / watcher) ----------------------------

    /** Enable the bounded tainted-fauna spawner (tainted terrain, or darkness as a wrong wanderer). */
    public static final ModConfigSpec.BooleanValue ENABLE_TAINTED_FAUNA_SPAWNS =
            BUILDER.comment("Allow tainted fauna to spawn near players in tainted/dark loaded terrain.")
                    .define("enableTaintedFaunaSpawns", true);

    public static final ModConfigSpec.IntValue TAINTED_FAUNA_SPAWN_INTERVAL_TICKS =
            BUILDER.comment("Ticks between tainted-fauna spawn passes (minimum 20).")
                    .defineInRange("taintedFaunaSpawnIntervalTicks", 200, 20, 24000);

    public static final ModConfigSpec.IntValue TAINTED_FAUNA_SPAWN_COUNT =
            BUILDER.comment("Max tainted fauna spawned per player per pass.")
                    .defineInRange("taintedFaunaSpawnCount", 2, 0, 16);

    public static final ModConfigSpec.IntValue TAINTED_FAUNA_SPAWN_RADIUS =
            BUILDER.comment("Radius (blocks) around each player in which tainted fauna may spawn.")
                    .defineInRange("taintedFaunaSpawnRadius", 32, 8, 96);

    public static final ModConfigSpec.IntValue TAINTED_FAUNA_SPAWN_CAP =
            BUILDER.comment("Max tainted fauna allowed near one player before spawning pauses.")
                    .defineInRange("taintedFaunaSpawnCap", 6, 1, 64);

    /** Enable the bounded lesser-swarm spawner (dark/tainted; numerous by design). */
    public static final ModConfigSpec.BooleanValue ENABLE_LESSER_SWARM_SPAWNS =
            BUILDER.comment("Allow lesser swarms to spawn near players in dark/tainted loaded terrain.")
                    .define("enableLesserSwarmSpawns", true);

    public static final ModConfigSpec.IntValue LESSER_SWARM_SPAWN_INTERVAL_TICKS =
            BUILDER.comment("Ticks between lesser-swarm spawn passes (minimum 20).")
                    .defineInRange("lesserSwarmSpawnIntervalTicks", 200, 20, 24000);

    public static final ModConfigSpec.IntValue LESSER_SWARM_SPAWN_COUNT =
            BUILDER.comment("Max lesser swarms spawned per player per pass.")
                    .defineInRange("lesserSwarmSpawnCount", 4, 0, 24);

    public static final ModConfigSpec.IntValue LESSER_SWARM_SPAWN_RADIUS =
            BUILDER.comment("Radius (blocks) around each player in which lesser swarms may spawn.")
                    .defineInRange("lesserSwarmSpawnRadius", 32, 8, 96);

    public static final ModConfigSpec.IntValue LESSER_SWARM_SPAWN_CAP =
            BUILDER.comment("Max lesser swarms allowed near one player before spawning pauses.")
                    .defineInRange("lesserSwarmSpawnCap", 12, 1, 64);

    /** Enable the bounded Watcher spawner (rare; one presence at a time by default). */
    public static final ModConfigSpec.BooleanValue ENABLE_WATCHER_SPAWNS =
            BUILDER.comment("Allow the Watcher to appear near players in tainted/dark loaded terrain (rare).")
                    .define("enableWatcherSpawns", true);

    public static final ModConfigSpec.IntValue WATCHER_SPAWN_INTERVAL_TICKS =
            BUILDER.comment("Ticks between Watcher spawn passes (minimum 20; long, it is rare).")
                    .defineInRange("watcherSpawnIntervalTicks", 1200, 20, 72000);

    public static final ModConfigSpec.IntValue WATCHER_SPAWN_COUNT =
            BUILDER.comment("Max Watchers spawned per player per pass (keep this at 1).")
                    .defineInRange("watcherSpawnCount", 1, 0, 4);

    public static final ModConfigSpec.IntValue WATCHER_SPAWN_RADIUS =
            BUILDER.comment("Radius (blocks) around each player in which the Watcher may appear.")
                    .defineInRange("watcherSpawnRadius", 48, 8, 128);

    public static final ModConfigSpec.IntValue WATCHER_SPAWN_CAP =
            BUILDER.comment("Max Watchers allowed near one player before spawning pauses.")
                    .defineInRange("watcherSpawnCap", 1, 1, 8);

    /**
     * Whether the Watcher can be killed at all. False (default) makes it permanently invulnerable —
     * "very hard/impossible to kill early". Set true to make it a very tough fight (40 HP, no loot).
     */
    public static final ModConfigSpec.BooleanValue WATCHER_KILLABLE =
            BUILDER.comment("Allow the Watcher to take damage. False = permanently invulnerable.")
                    .define("watcherKillable", false);

    // --- Bestiary: bone_choir (skeleton-role melee undead) -----------------------------------

    /** The bone choir's low hymn: a weak, low-rate sanity drain. */
    public static final ModConfigSpec.BooleanValue ENABLE_SANITY_BONE_CHOIR =
            BUILDER.comment("Enable the 'bone_choir' sanity source (nearby choirs hum a low hymn).")
                    .define("enableSanityBoneChoir", true);

    public static final ModConfigSpec.DoubleValue SANITY_BONE_CHOIR_RATE =
            BUILDER.comment("Sanity change per second per nearby bone choir (negative drains).")
                    .defineInRange("sanityBoneChoirRate", -0.06, -10.0, 0.0);

    public static final ModConfigSpec.IntValue SANITY_BONE_CHOIR_RADIUS =
            BUILDER.comment("Radius (blocks) in which bone choirs contribute to the sanity drain.")
                    .defineInRange("sanityBoneChoirRadius", 8, 1, 32);

    public static final ModConfigSpec.IntValue SANITY_BONE_CHOIR_MAX =
            BUILDER.comment("Maximum bone choirs counted toward the sanity drain at once.")
                    .defineInRange("sanityBoneChoirMax", 4, 1, 32);

    public static final ModConfigSpec.BooleanValue ENABLE_BONE_CHOIR_SPAWNS =
            BUILDER.comment("Allow bone choirs to spawn near players in dark/tainted loaded terrain.")
                    .define("enableBoneChoirSpawns", true);

    public static final ModConfigSpec.IntValue BONE_CHOIR_SPAWN_INTERVAL_TICKS =
            BUILDER.comment("Ticks between bone-choir spawn passes (minimum 20).")
                    .defineInRange("boneChoirSpawnIntervalTicks", 240, 20, 24000);

    public static final ModConfigSpec.IntValue BONE_CHOIR_SPAWN_COUNT =
            BUILDER.comment("Max bone choirs spawned per player per pass.")
                    .defineInRange("boneChoirSpawnCount", 2, 0, 16);

    public static final ModConfigSpec.IntValue BONE_CHOIR_SPAWN_RADIUS =
            BUILDER.comment("Radius (blocks) around each player in which bone choirs may spawn.")
                    .defineInRange("boneChoirSpawnRadius", 32, 8, 96);

    public static final ModConfigSpec.IntValue BONE_CHOIR_SPAWN_CAP =
            BUILDER.comment("Max bone choirs allowed near one player before spawning pauses.")
                    .defineInRange("boneChoirSpawnCap", 6, 1, 64);

    // --- Bestiary: drowned_thrall (coastal / drowned-role corruption vector) -----------------

    /** The drowned thrall's corruption vector: standing near one taints you slowly. */
    public static final ModConfigSpec.BooleanValue ENABLE_CORRUPTION_DROWNED_THRALL =
            BUILDER.comment("Enable the 'drowned_thrall' corruption vector (nearby thralls taint).")
                    .define("enableCorruptionDrownedThrall", true);

    public static final ModConfigSpec.DoubleValue CORRUPTION_DROWNED_THRALL_RATE =
            BUILDER.comment("Corruption per second per nearby drowned thrall (positive taints).")
                    .defineInRange("corruptionDrownedThrallRate", 0.04, 0.0, 10.0);

    public static final ModConfigSpec.IntValue CORRUPTION_DROWNED_THRALL_RADIUS =
            BUILDER.comment("Radius (blocks) in which drowned thralls contribute corruption.")
                    .defineInRange("corruptionDrownedThrallRadius", 6, 1, 32);

    public static final ModConfigSpec.IntValue CORRUPTION_DROWNED_THRALL_MAX =
            BUILDER.comment("Maximum drowned thralls counted toward the corruption vector at once.")
                    .defineInRange("corruptionDrownedThrallMax", 4, 1, 32);

    public static final ModConfigSpec.BooleanValue ENABLE_DROWNED_THRALL_SPAWNS =
            BUILDER.comment("Allow drowned thralls to spawn near players in/near water (coastal).")
                    .define("enableDrownedThrallSpawns", true);

    public static final ModConfigSpec.IntValue DROWNED_THRALL_SPAWN_INTERVAL_TICKS =
            BUILDER.comment("Ticks between drowned-thrall spawn passes (minimum 20).")
                    .defineInRange("drownedThrallSpawnIntervalTicks", 240, 20, 24000);

    public static final ModConfigSpec.IntValue DROWNED_THRALL_SPAWN_COUNT =
            BUILDER.comment("Max drowned thralls spawned per player per pass.")
                    .defineInRange("drownedThrallSpawnCount", 2, 0, 16);

    public static final ModConfigSpec.IntValue DROWNED_THRALL_SPAWN_RADIUS =
            BUILDER.comment("Radius (blocks) around each player in which drowned thralls may spawn.")
                    .defineInRange("drownedThrallSpawnRadius", 32, 8, 96);

    public static final ModConfigSpec.IntValue DROWNED_THRALL_SPAWN_CAP =
            BUILDER.comment("Max drowned thralls allowed near one player before spawning pauses.")
                    .defineInRange("drownedThrallSpawnCap", 6, 1, 64);

    // --- Bestiary: veil_stalker (ambusher / spider-role sanity whisper) ----------------------

    /** The veil stalker's whisper: a small, quiet sanity drain. */
    public static final ModConfigSpec.BooleanValue ENABLE_SANITY_VEIL_STALKER =
            BUILDER.comment("Enable the 'veil_stalker' sanity source (a whisper from above drains sanity).")
                    .define("enableSanityVeilStalker", true);

    public static final ModConfigSpec.DoubleValue SANITY_VEIL_STALKER_RATE =
            BUILDER.comment("Sanity change per second per nearby veil stalker (negative drains).")
                    .defineInRange("sanityVeilStalkerRate", -0.10, -10.0, 0.0);

    public static final ModConfigSpec.IntValue SANITY_VEIL_STALKER_RADIUS =
            BUILDER.comment("Radius (blocks) in which veil stalkers contribute to the sanity drain.")
                    .defineInRange("sanityVeilStalkerRadius", 6, 1, 32);

    public static final ModConfigSpec.IntValue SANITY_VEIL_STALKER_MAX =
            BUILDER.comment("Maximum veil stalkers counted toward the sanity drain at once.")
                    .defineInRange("sanityVeilStalkerMax", 2, 1, 32);

    /** Whether the veil stalker climbs walls (its ambush hook). */
    public static final ModConfigSpec.BooleanValue ENABLE_VEIL_STALKER_CLIMBING =
            BUILDER.comment("Allow the veil stalker to climb walls (its ambush hook).")
                    .define("enableVeilStalkerClimbing", true);

    public static final ModConfigSpec.BooleanValue ENABLE_VEIL_STALKER_SPAWNS =
            BUILDER.comment("Allow veil stalkers to spawn near players in dark/tainted loaded terrain.")
                    .define("enableVeilStalkerSpawns", true);

    public static final ModConfigSpec.IntValue VEIL_STALKER_SPAWN_INTERVAL_TICKS =
            BUILDER.comment("Ticks between veil-stalker spawn passes (minimum 20).")
                    .defineInRange("veilStalkerSpawnIntervalTicks", 300, 20, 24000);

    public static final ModConfigSpec.IntValue VEIL_STALKER_SPAWN_COUNT =
            BUILDER.comment("Max veil stalkers spawned per player per pass.")
                    .defineInRange("veilStalkerSpawnCount", 2, 0, 16);

    public static final ModConfigSpec.IntValue VEIL_STALKER_SPAWN_RADIUS =
            BUILDER.comment("Radius (blocks) around each player in which veil stalkers may spawn.")
                    .defineInRange("veilStalkerSpawnRadius", 32, 8, 96);

    public static final ModConfigSpec.IntValue VEIL_STALKER_SPAWN_CAP =
            BUILDER.comment("Max veil stalkers allowed near one player before spawning pauses.")
                    .defineInRange("veilStalkerSpawnCap", 4, 1, 64);

    // --- Ancient Ones (boss framework, design/28-ancient-ones.md) -----------------------------

    /** Health fraction at or below which any Ancient One enters its middle phase. */
    public static final ModConfigSpec.DoubleValue BOSS_PHASE_TWO_HEALTH =
            BUILDER.comment("Health fraction at or below which an Ancient One enters phase two.")
                    .defineInRange("bossPhaseTwoHealth", 0.5, 0.05, 1.0);

    /** Health fraction at or below which any Ancient One enters its enraged phase. */
    public static final ModConfigSpec.DoubleValue BOSS_ENRAGE_HEALTH =
            BUILDER.comment("Health fraction at or below which an Ancient One becomes enraged.")
                    .defineInRange("bossEnrageHealth", 0.25, 0.01, 1.0);

    /** Global difficulty knob: multiplies every Ancient One's max health at spawn. */
    public static final ModConfigSpec.DoubleValue BOSS_HEALTH_MULTIPLIER =
            BUILDER.comment("Multiplies every Ancient One's max health (difficulty knob; 1.0 = base).")
                    .defineInRange("bossHealthMultiplier", 1.0, 0.25, 4.0);

    /** Master switch for the bounded, once-per-site boss triggers (Cthulhu at drowned_temple). */
    public static final ModConfigSpec.BooleanValue ENABLE_BOSS_SITE_TRIGGERS =
            BUILDER.comment("Allow a site boss to appear once when a player nears its fixed site.")
                    .define("enableBossSiteTriggers", true);

    /** Player proximity (blocks) that arms a site boss trigger; loaded chunks only. */
    public static final ModConfigSpec.IntValue BOSS_SITE_TRIGGER_RADIUS =
            BUILDER.comment("Radius (blocks) around a site within which its boss may once appear.")
                    .defineInRange("bossSiteTriggerRadius", 64, 16, 160);

    /** Cthulhu: the drowned-temple Ancient One. */
    public static final ModConfigSpec.BooleanValue CTHULHU_KILLABLE =
            BUILDER.comment("Allow Cthulhu to be killed. False = permanently invulnerable.")
                    .define("cthulhuKillable", true);

    public static final ModConfigSpec.BooleanValue ENABLE_SANITY_CTHULHU =
            BUILDER.comment("Enable the Cthulhu sanity source (the dream-leak drains sanity nearby).")
                    .define("enableSanityCthulhu", true);

    public static final ModConfigSpec.DoubleValue SANITY_CTHULHU_RATE =
            BUILDER.comment("Sanity change per second while near Cthulhu (negative drains).")
                    .defineInRange("sanityCthulhuRate", -0.35, -10.0, 0.0);

    public static final ModConfigSpec.IntValue SANITY_CTHULHU_RADIUS =
            BUILDER.comment("Radius (blocks) in which Cthulhu contributes to the sanity drain.")
                    .defineInRange("sanityCthulhuRadius", 32, 1, 96);

    /** The Dunwich Horror: a mobile settlement threat near tainted woods. */
    public static final ModConfigSpec.BooleanValue DUNWICH_HORROR_KILLABLE =
            BUILDER.comment("Allow the Dunwich Horror to be killed. False = invulnerable.")
                    .define("dunwichHorrorKillable", true);

    public static final ModConfigSpec.BooleanValue ENABLE_DUNWICH_HORROR_SPAWNS =
            BUILDER.comment("Allow the Dunwich Horror to stalk players in dark/tainted loaded terrain.")
                    .define("enableDunwichHorrorSpawns", true);

    public static final ModConfigSpec.IntValue DUNWICH_HORROR_SPAWN_INTERVAL_TICKS =
            BUILDER.comment("Ticks between Dunwich Horror spawn passes (minimum 20).")
                    .defineInRange("dunwichHorrorSpawnIntervalTicks", 2400, 20, 72000);

    public static final ModConfigSpec.IntValue DUNWICH_HORROR_SPAWN_RADIUS =
            BUILDER.comment("Radius (blocks) around each player in which the Horror may appear.")
                    .defineInRange("dunwichHorrorSpawnRadius", 48, 8, 128);

    public static final ModConfigSpec.IntValue DUNWICH_HORROR_SPAWN_CAP =
            BUILDER.comment("Max Dunwich Horrors allowed near one player before spawning pauses.")
                    .defineInRange("dunwichHorrorSpawnCap", 1, 1, 4);

    public static final ModConfigSpec.BooleanValue ENABLE_CORRUPTION_DUNWICH_HORROR =
            BUILDER.comment("Enable the Dunwich Horror corruption vector (nearby Horror taints).")
                    .define("enableCorruptionDunwichHorror", true);

    public static final ModConfigSpec.DoubleValue CORRUPTION_DUNWICH_HORROR_RATE =
            BUILDER.comment("Corruption per second while near the Dunwich Horror (positive taints).")
                    .defineInRange("corruptionDunwichHorrorRate", 0.08, 0.0, 10.0);

    public static final ModConfigSpec.IntValue CORRUPTION_DUNWICH_HORROR_RADIUS =
            BUILDER.comment("Radius (blocks) in which the Dunwich Horror contributes corruption.")
                    .defineInRange("corruptionDunwichHorrorRadius", 20, 1, 64);

    /** Shub-Niggurath: a slow, massive biome presence. */
    public static final ModConfigSpec.BooleanValue SHUB_NIGGURATH_KILLABLE =
            BUILDER.comment("Allow Shub-Niggurath to be killed. False = invulnerable.")
                    .define("shubNiggurathKillable", true);

    public static final ModConfigSpec.BooleanValue ENABLE_SHUB_NIGGURATH_SPAWNS =
            BUILDER.comment("Allow Shub-Niggurath to appear in tainted loaded terrain (rare).")
                    .define("enableShubNiggurathSpawns", true);

    public static final ModConfigSpec.IntValue SHUB_NIGGURATH_SPAWN_INTERVAL_TICKS =
            BUILDER.comment("Ticks between Shub-Niggurath spawn passes (minimum 20; long, it is rare).")
                    .defineInRange("shubNiggurathSpawnIntervalTicks", 3600, 20, 144000);

    public static final ModConfigSpec.IntValue SHUB_NIGGURATH_SPAWN_RADIUS =
            BUILDER.comment("Radius (blocks) around each player in which Shub-Niggurath may appear.")
                    .defineInRange("shubNiggurathSpawnRadius", 48, 8, 128);

    public static final ModConfigSpec.IntValue SHUB_NIGGURATH_SPAWN_CAP =
            BUILDER.comment("Max Shub-Nigguraths allowed near one player before spawning pauses.")
                    .defineInRange("shubNiggurathSpawnCap", 1, 1, 4);

    public static final ModConfigSpec.BooleanValue ENABLE_CORRUPTION_SHUB_NIGGURATH =
            BUILDER.comment("Enable the Shub-Niggurath corruption aura (the woods breathe taint).")
                    .define("enableCorruptionShubNiggurath", true);

    public static final ModConfigSpec.DoubleValue CORRUPTION_SHUB_NIGGURATH_RATE =
            BUILDER.comment("Corruption per second while within Shub-Niggurath's aura (positive taints).")
                    .defineInRange("corruptionShubNiggurathRate", 0.15, 0.0, 10.0);

    public static final ModConfigSpec.DoubleValue CORRUPTION_SHUB_NIGGURATH_SPRINT_RATE =
            BUILDER.comment("Extra corruption per second while sprinting near Shub-Niggurath.")
                    .defineInRange("corruptionShubNiggurathSprintRate", 0.20, 0.0, 10.0);

    public static final ModConfigSpec.IntValue CORRUPTION_SHUB_NIGGURATH_RADIUS =
            BUILDER.comment("Radius (blocks) of Shub-Niggurath's corruption aura; wide by design.")
                    .defineInRange("corruptionShubNiggurathRadius", 40, 4, 128);

    /** Sanity drain while standing still near Shub-Niggurath (running costs corruption instead). */
    public static final ModConfigSpec.DoubleValue SANITY_SHUB_NIGGURATH_STILL_RATE =
            BUILDER.comment("Sanity change per second while standing still near Shub-Niggurath.")
                    .defineInRange("sanityShubNiggurathStillRate", -0.12, -10.0, 0.0);

    // --- Ancient Ones, batch 2 (design/28): Azathoth / Yog-Sothoth / Ithaqua / Yig / Atlach-Nacha --

    /** Azathoth: the rift_scar presence that replaces the Wither and has no melee. */
    public static final ModConfigSpec.BooleanValue AZATHOTH_KILLABLE =
            BUILDER.comment("Allow Azathoth to be killed. False = permanently invulnerable (it is no fight).")
                    .define("azathothKillable", true);
    public static final ModConfigSpec.BooleanValue ENABLE_SANITY_AZATHOTH =
            BUILDER.comment("Enable the Azathoth sanity source (the music unmakes the will to act).")
                    .define("enableSanityAzathoth", true);
    public static final ModConfigSpec.DoubleValue SANITY_AZATHOTH_RATE =
            BUILDER.comment("Sanity change per second while near Azathoth (negative drains).")
                    .defineInRange("sanityAzathothRate", -0.55, -10.0, 0.0);
    public static final ModConfigSpec.IntValue SANITY_AZATHOTH_RADIUS =
            BUILDER.comment("Radius (blocks) of Azathoth's hard sanity drain.")
                    .defineInRange("sanityAzathothRadius", 40, 1, 128);
    public static final ModConfigSpec.BooleanValue ENABLE_SITE_AZATHOTH =
            BUILDER.comment("Allow Azathoth to hold the rift_scar (one loaded-only presence per player).")
                    .define("enableSiteAzathoth", true);
    public static final ModConfigSpec.IntValue SITE_AZATHOTH_COUNT =
            BUILDER.comment("Max Azathoths spawned per rift_scar pass.")
                    .defineInRange("siteAzathothCount", 1, 0, 2);
    public static final ModConfigSpec.IntValue SITE_AZATHOTH_CAP =
            BUILDER.comment("Max Azathoths near one player at a rift_scar before pausing.")
                    .defineInRange("siteAzathothCap", 1, 1, 4);

    /** Yog-Sothoth: the gate on the observatory plateau, bound to the existing order_vault site. */
    public static final ModConfigSpec.BooleanValue YOG_SOTHOTH_KILLABLE =
            BUILDER.comment("Allow Yog-Sothoth to be killed. False = permanently invulnerable.")
                    .define("yogSothothKillable", true);
    public static final ModConfigSpec.BooleanValue ENABLE_SANITY_YOG_SOTHOTH =
            BUILDER.comment("Enable the Yog-Sothoth sanity source (the gate draws the eye).")
                    .define("enableSanityYogSothoth", true);
    public static final ModConfigSpec.DoubleValue SANITY_YOG_SOTHOTH_RATE =
            BUILDER.comment("Sanity change per second while near Yog-Sothoth (negative drains).")
                    .defineInRange("sanityYogSothothRate", -0.30, -10.0, 0.0);
    public static final ModConfigSpec.IntValue SANITY_YOG_SOTHOTH_RADIUS =
            BUILDER.comment("Radius (blocks) of Yog-Sothoth's large, patient sanity drain.")
                    .defineInRange("sanityYogSothothRadius", 48, 1, 160);
    public static final ModConfigSpec.BooleanValue ENABLE_SITE_YOG_SOTHOTH =
            BUILDER.comment("Allow Yog-Sothoth to stand at the order_vault (observatory plateau), loaded-only.")
                    .define("enableSiteYogSothoth", true);
    public static final ModConfigSpec.IntValue SITE_YOG_SOTHOTH_COUNT =
            BUILDER.comment("Max Yog-Sothoths spawned per order_vault pass.")
                    .defineInRange("siteYogSothothCount", 1, 0, 2);
    public static final ModConfigSpec.IntValue SITE_YOG_SOTHOTH_CAP =
            BUILDER.comment("Max Yog-Sothoths near one player at the vault before pausing.")
                    .defineInRange("siteYogSothothCap", 1, 1, 4);

    /** Ithaqua: the walking wind of the cold edge, replacing the Warden. */
    public static final ModConfigSpec.BooleanValue ITHAQUA_KILLABLE =
            BUILDER.comment("Allow Ithaqua to be killed. False = permanently invulnerable.")
                    .define("ithaquaKillable", true);
    public static final ModConfigSpec.BooleanValue ENABLE_ITHAQUA_SPAWNS =
            BUILDER.comment("Allow Ithaqua to stalk players on cold, loaded ground (the polar edge).")
                    .define("enableIthaquaSpawns", true);
    public static final ModConfigSpec.IntValue ITHAQUA_SPAWN_INTERVAL_TICKS =
            BUILDER.comment("Ticks between Ithaqua spawn passes (minimum 20).")
                    .defineInRange("ithaquaSpawnIntervalTicks", 2400, 20, 72000);
    public static final ModConfigSpec.IntValue ITHAQUA_SPAWN_RADIUS =
            BUILDER.comment("Radius (blocks) around each player in which Ithaqua may appear.")
                    .defineInRange("ithaquaSpawnRadius", 48, 8, 128);
    public static final ModConfigSpec.IntValue ITHAQUA_SPAWN_CAP =
            BUILDER.comment("Max Ithaquas allowed near one player before spawning pauses.")
                    .defineInRange("ithaquaSpawnCap", 1, 1, 4);
    public static final ModConfigSpec.BooleanValue ENABLE_SANITY_ITHAQUA =
            BUILDER.comment("Enable the Ithaqua sanity source (the wind drags the will away).")
                    .define("enableSanityIthaqua", true);
    public static final ModConfigSpec.DoubleValue SANITY_ITHAQUA_RATE =
            BUILDER.comment("Sanity change per second while near Ithaqua (negative drains).")
                    .defineInRange("sanityIthaquaRate", -0.30, -10.0, 0.0);
    public static final ModConfigSpec.IntValue SANITY_ITHAQUA_RADIUS =
            BUILDER.comment("Radius (blocks) of Ithaqua's sanity drain.")
                    .defineInRange("sanityIthaquaRadius", 28, 1, 96);

    /** Yig: the ashen_waste presence that escalates when struck. */
    public static final ModConfigSpec.BooleanValue YIG_KILLABLE =
            BUILDER.comment("Allow Yig to be killed. False = permanently invulnerable.")
                    .define("yigKillable", true);
    public static final ModConfigSpec.BooleanValue ENABLE_YIG_SPAWNS =
            BUILDER.comment("Allow Yig to appear in tainted loaded terrain (the ashen waste).")
                    .define("enableYigSpawns", true);
    public static final ModConfigSpec.IntValue YIG_SPAWN_INTERVAL_TICKS =
            BUILDER.comment("Ticks between Yig spawn passes (minimum 20).")
                    .defineInRange("yigSpawnIntervalTicks", 2400, 20, 72000);
    public static final ModConfigSpec.IntValue YIG_SPAWN_RADIUS =
            BUILDER.comment("Radius (blocks) around each player in which Yig may appear.")
                    .defineInRange("yigSpawnRadius", 48, 8, 128);
    public static final ModConfigSpec.IntValue YIG_SPAWN_CAP =
            BUILDER.comment("Max Yigs allowed near one player before spawning pauses.")
                    .defineInRange("yigSpawnCap", 1, 1, 4);
    public static final ModConfigSpec.BooleanValue ENABLE_YIG_ESCALATION =
            BUILDER.comment("Enable Yig's escalation: each hit may draw one more of the dead to it.")
                    .define("enableYigEscalation", true);
    public static final ModConfigSpec.IntValue YIG_ESCALATION_CAP =
            BUILDER.comment("Max drawn dead within 24 blocks of Yig before the escalation pauses.")
                    .defineInRange("yigEscalationCap", 6, 0, 24);
    public static final ModConfigSpec.BooleanValue ENABLE_CORRUPTION_YIG =
            BUILDER.comment("Enable the Yig corruption aura (the deaths taint the ground).")
                    .define("enableCorruptionYig", true);
    public static final ModConfigSpec.DoubleValue CORRUPTION_YIG_RATE =
            BUILDER.comment("Corruption per second while near Yig (positive taints).")
                    .defineInRange("corruptionYigRate", 0.10, 0.0, 10.0);
    public static final ModConfigSpec.IntValue CORRUPTION_YIG_RADIUS =
            BUILDER.comment("Radius (blocks) of Yig's corruption aura.")
                    .defineInRange("corruptionYigRadius", 24, 1, 96);

    /** Atlach-Nacha: the rift_scar weaver. */
    public static final ModConfigSpec.BooleanValue ATLACH_NACHA_KILLABLE =
            BUILDER.comment("Allow Atlach-Nacha to be killed. False = permanently invulnerable.")
                    .define("atlachNachaKillable", true);
    public static final ModConfigSpec.BooleanValue ENABLE_SITE_ATLACH_NACHA =
            BUILDER.comment("Allow Atlach-Nacha to weave at the rift_scar (loaded-only).")
                    .define("enableSiteAtlachNacha", true);
    public static final ModConfigSpec.IntValue SITE_ATLACH_NACHA_COUNT =
            BUILDER.comment("Max Atlach-Nachas spawned per rift_scar pass.")
                    .defineInRange("siteAtlachNachaCount", 1, 0, 2);
    public static final ModConfigSpec.IntValue SITE_ATLACH_NACHA_CAP =
            BUILDER.comment("Max Atlach-Nachas near one player at a rift_scar before pausing.")
                    .defineInRange("siteAtlachNachaCap", 1, 1, 4);
    public static final ModConfigSpec.BooleanValue ENABLE_CORRUPTION_ATLACH_NACHA =
            BUILDER.comment("Enable the Atlach-Nacha corruption aura (the web widens the tear).")
                    .define("enableCorruptionAtlachNacha", true);
    public static final ModConfigSpec.DoubleValue CORRUPTION_ATLACH_NACHA_RATE =
            BUILDER.comment("Corruption per second while near Atlach-Nacha (positive taints).")
                    .defineInRange("corruptionAtlachNachaRate", 0.18, 0.0, 10.0);
    public static final ModConfigSpec.IntValue CORRUPTION_ATLACH_NACHA_RADIUS =
            BUILDER.comment("Radius (blocks) of Atlach-Nacha's corruption aura.")
                    .defineInRange("corruptionAtlachNachaRadius", 28, 1, 96);

    // --- Ancient Ones, batch 3 (design/28): Nyarlathotep / Cthugha / Glaaki / Hydra / Nyogtha /
    // Rhan-Tegoth. Same shapes as batch 2; every one attaches to an existing site or biome.

    /** Nyarlathotep: the trusted face at the cult_stronghold. */
    public static final ModConfigSpec.BooleanValue NYARLATHOTEP_KILLABLE =
            BUILDER.comment("Allow Nyarlathotep to be killed. False = permanently invulnerable.")
                    .define("nyarlathotepKillable", true);
    public static final ModConfigSpec.BooleanValue ENABLE_SITE_NYARLATHOTEP =
            BUILDER.comment("Allow Nyarlathotep to wear a trusted face in the cult_stronghold (loaded-only).")
                    .define("enableSiteNyarlathotep", true);
    public static final ModConfigSpec.IntValue SITE_NYARLATHOTEP_COUNT =
            BUILDER.comment("Max Nyarlathoteps spawned per cult_stronghold pass.")
                    .defineInRange("siteNyarlathotepCount", 1, 0, 2);
    public static final ModConfigSpec.IntValue SITE_NYARLATHOTEP_CAP =
            BUILDER.comment("Max Nyarlathoteps near one player at a stronghold before pausing.")
                    .defineInRange("siteNyarlathotepCap", 1, 1, 4);
    public static final ModConfigSpec.BooleanValue ENABLE_SANITY_NYARLATHOTEP =
            BUILDER.comment("Enable the Nyarlathotep sanity source (the betrayal drains when you look away).")
                    .define("enableSanityNyarlathotep", true);
    public static final ModConfigSpec.DoubleValue SANITY_NYARLATHOTEP_RATE =
            BUILDER.comment("Sanity change per second while near Nyarlathotep (negative drains).")
                    .defineInRange("sanityNyarlathotepRate", -0.32, -10.0, 0.0);
    public static final ModConfigSpec.IntValue SANITY_NYARLATHOTEP_RADIUS =
            BUILDER.comment("Radius (blocks) of Nyarlathotep's sanity drain.")
                    .defineInRange("sanityNyarlathotepRadius", 30, 1, 96);

    /** Cthugha: the watching flame of the ashen_waste. */
    public static final ModConfigSpec.BooleanValue CTHUGHA_KILLABLE =
            BUILDER.comment("Allow Cthugha to be killed. False = permanently invulnerable.")
                    .define("cthughaKillable", true);
    public static final ModConfigSpec.BooleanValue ENABLE_CTHUGHA_SPAWNS =
            BUILDER.comment("Allow Cthugha to appear on tainted loaded ground (the ashen waste).")
                    .define("enableCthughaSpawns", true);
    public static final ModConfigSpec.IntValue CTHUGHA_SPAWN_INTERVAL_TICKS =
            BUILDER.comment("Ticks between Cthugha spawn passes (minimum 20).")
                    .defineInRange("cthughaSpawnIntervalTicks", 2400, 20, 72000);
    public static final ModConfigSpec.IntValue CTHUGHA_SPAWN_RADIUS =
            BUILDER.comment("Radius (blocks) around each player in which Cthugha may appear.")
                    .defineInRange("cthughaSpawnRadius", 48, 8, 128);
    public static final ModConfigSpec.IntValue CTHUGHA_SPAWN_CAP =
            BUILDER.comment("Max Cthughas allowed near one player before spawning pauses.")
                    .defineInRange("cthughaSpawnCap", 1, 1, 4);
    public static final ModConfigSpec.BooleanValue ENABLE_CORRUPTION_CTHUGHA =
            BUILDER.comment("Enable the Cthugha corruption aura (the watching flame taints; worse in light).")
                    .define("enableCorruptionCthugha", true);
    public static final ModConfigSpec.DoubleValue CORRUPTION_CTHUGHA_RATE =
            BUILDER.comment("Corruption per second while near Cthugha (positive taints).")
                    .defineInRange("corruptionCthughaRate", 0.14, 0.0, 10.0);
    public static final ModConfigSpec.IntValue CORRUPTION_CTHUGHA_RADIUS =
            BUILDER.comment("Radius (blocks) of Cthugha's corruption aura.")
                    .defineInRange("corruptionCthughaRadius", 26, 1, 96);

    /** Glaaki: the lake-dream of the drowned_marsh. */
    public static final ModConfigSpec.BooleanValue GLAAKI_KILLABLE =
            BUILDER.comment("Allow Glaaki to be killed. False = permanently invulnerable.")
                    .define("glaakiKillable", true);
    public static final ModConfigSpec.BooleanValue ENABLE_GLAAKI_SPAWNS =
            BUILDER.comment("Allow Glaaki to appear on water-adjacent loaded ground (the drowned marsh).")
                    .define("enableGlaakiSpawns", true);
    public static final ModConfigSpec.IntValue GLAAKI_SPAWN_INTERVAL_TICKS =
            BUILDER.comment("Ticks between Glaaki spawn passes (minimum 20).")
                    .defineInRange("glaakiSpawnIntervalTicks", 2600, 20, 72000);
    public static final ModConfigSpec.IntValue GLAAKI_SPAWN_RADIUS =
            BUILDER.comment("Radius (blocks) around each player in which Glaaki may appear.")
                    .defineInRange("glaakiSpawnRadius", 48, 8, 128);
    public static final ModConfigSpec.IntValue GLAAKI_SPAWN_CAP =
            BUILDER.comment("Max Glaakis allowed near one player before spawning pauses.")
                    .defineInRange("glaakiSpawnCap", 1, 1, 4);
    public static final ModConfigSpec.BooleanValue ENABLE_SANITY_GLAAKI =
            BUILDER.comment("Enable the Glaaki sanity source (the servitors' call).")
                    .define("enableSanityGlaaki", true);
    public static final ModConfigSpec.DoubleValue SANITY_GLAAKI_RATE =
            BUILDER.comment("Sanity change per second while near Glaaki (negative drains).")
                    .defineInRange("sanityGlaakiRate", -0.30, -10.0, 0.0);
    public static final ModConfigSpec.IntValue SANITY_GLAAKI_RADIUS =
            BUILDER.comment("Radius (blocks) of Glaaki's sanity drain.")
                    .defineInRange("sanityGlaakiRadius", 28, 1, 96);

    /** The Hydra: the head-budding presence of the drowned_marsh. */
    public static final ModConfigSpec.BooleanValue HYDRA_KILLABLE =
            BUILDER.comment("Allow the Hydra to be killed. False = permanently invulnerable.")
                    .define("hydraKillable", true);
    public static final ModConfigSpec.BooleanValue ENABLE_HYDRA_SPAWNS =
            BUILDER.comment("Allow the Hydra to appear on water-adjacent loaded ground (the drowned marsh).")
                    .define("enableHydraSpawns", true);
    public static final ModConfigSpec.IntValue HYDRA_SPAWN_INTERVAL_TICKS =
            BUILDER.comment("Ticks between Hydra spawn passes (minimum 20).")
                    .defineInRange("hydraSpawnIntervalTicks", 2800, 20, 72000);
    public static final ModConfigSpec.IntValue HYDRA_SPAWN_RADIUS =
            BUILDER.comment("Radius (blocks) around each player in which the Hydra may appear.")
                    .defineInRange("hydraSpawnRadius", 48, 8, 128);
    public static final ModConfigSpec.IntValue HYDRA_SPAWN_CAP =
            BUILDER.comment("Max Hydras allowed near one player before spawning pauses.")
                    .defineInRange("hydraSpawnCap", 1, 1, 4);
    public static final ModConfigSpec.BooleanValue ENABLE_HYDRA_ESCALATION =
            BUILDER.comment("Enable the Hydra's escalation: each hit may bud one more head.")
                    .define("enableHydraEscalation", true);
    public static final ModConfigSpec.IntValue HYDRA_ESCALATION_CAP =
            BUILDER.comment("Max budded heads within 24 blocks of the Hydra before escalation pauses.")
                    .defineInRange("hydraEscalationCap", 6, 0, 24);
    public static final ModConfigSpec.BooleanValue ENABLE_SANITY_HYDRA =
            BUILDER.comment("Enable the Hydra sanity source (the heads' gaze).")
                    .define("enableSanityHydra", true);
    public static final ModConfigSpec.DoubleValue SANITY_HYDRA_RATE =
            BUILDER.comment("Sanity change per second while near the Hydra (negative drains).")
                    .defineInRange("sanityHydraRate", -0.28, -10.0, 0.0);
    public static final ModConfigSpec.IntValue SANITY_HYDRA_RADIUS =
            BUILDER.comment("Radius (blocks) of the Hydra's sanity drain.")
                    .defineInRange("sanityHydraRadius", 26, 1, 96);

    /** Nyogtha: the thing under the floor in the deep caves. */
    public static final ModConfigSpec.BooleanValue NYOGTHA_KILLABLE =
            BUILDER.comment("Allow Nyogtha to be killed. False = permanently invulnerable.")
                    .define("nyogthaKillable", true);
    public static final ModConfigSpec.BooleanValue ENABLE_NYOGTHA_SPAWNS =
            BUILDER.comment("Allow Nyogtha to be heard in deep loaded caves.")
                    .define("enableNyogthaSpawns", true);
    public static final ModConfigSpec.IntValue NYOGTHA_SPAWN_INTERVAL_TICKS =
            BUILDER.comment("Ticks between Nyogtha spawn passes (minimum 20).")
                    .defineInRange("nyogthaSpawnIntervalTicks", 3000, 20, 72000);
    public static final ModConfigSpec.IntValue NYOGTHA_SPAWN_RADIUS =
            BUILDER.comment("Radius (blocks) around each player in which Nyogtha may appear.")
                    .defineInRange("nyogthaSpawnRadius", 40, 8, 128);
    public static final ModConfigSpec.IntValue NYOGTHA_SPAWN_CAP =
            BUILDER.comment("Max Nyogthas allowed near one player before spawning pauses.")
                    .defineInRange("nyogthaSpawnCap", 1, 1, 4);
    public static final ModConfigSpec.IntValue NYOGTHA_SPAWN_MAX_Y =
            BUILDER.comment("Highest Y at which Nyogtha may appear (deep caves; it is under the floor).")
                    .defineInRange("nyogthaSpawnMaxY", 0, -64, 64);
    public static final ModConfigSpec.BooleanValue ENABLE_SANITY_NYOGTHA =
            BUILDER.comment("Enable the Nyogtha sanity source (a sound heard through the stone).")
                    .define("enableSanityNyogtha", true);
    public static final ModConfigSpec.DoubleValue SANITY_NYOGTHA_RATE =
            BUILDER.comment("Sanity change per second while near Nyogtha (negative drains).")
                    .defineInRange("sanityNyogthaRate", -0.30, -10.0, 0.0);
    public static final ModConfigSpec.IntValue SANITY_NYOGTHA_RADIUS =
            BUILDER.comment("Radius (blocks) of Nyogtha's sanity drain.")
                    .defineInRange("sanityNyogthaRadius", 24, 1, 96);

    /** Rhan-Tegoth: the idol at the polar/cold edge. */
    public static final ModConfigSpec.BooleanValue RHAN_TEGOTH_KILLABLE =
            BUILDER.comment("Allow Rhan-Tegoth to be killed. False = permanently invulnerable.")
                    .define("rhanTegothKillable", true);
    public static final ModConfigSpec.BooleanValue ENABLE_RHAN_TEGOTH_SPAWNS =
            BUILDER.comment("Allow Rhan-Tegoth to stand on cold, loaded ground (the polar edge).")
                    .define("enableRhanTegothSpawns", true);
    public static final ModConfigSpec.IntValue RHAN_TEGOTH_SPAWN_INTERVAL_TICKS =
            BUILDER.comment("Ticks between Rhan-Tegoth spawn passes (minimum 20).")
                    .defineInRange("rhanTegothSpawnIntervalTicks", 3200, 20, 72000);
    public static final ModConfigSpec.IntValue RHAN_TEGOTH_SPAWN_RADIUS =
            BUILDER.comment("Radius (blocks) around each player in which Rhan-Tegoth may appear.")
                    .defineInRange("rhanTegothSpawnRadius", 48, 8, 128);
    public static final ModConfigSpec.IntValue RHAN_TEGOTH_SPAWN_CAP =
            BUILDER.comment("Max Rhan-Tegoths allowed near one player before spawning pauses.")
                    .defineInRange("rhanTegothSpawnCap", 1, 1, 4);
    public static final ModConfigSpec.BooleanValue ENABLE_SANITY_RHAN_TEGOTH =
            BUILDER.comment("Enable the Rhan-Tegoth sanity source (the pull to worship).")
                    .define("enableSanityRhanTegoth", true);
    public static final ModConfigSpec.DoubleValue SANITY_RHAN_TEGOTH_RATE =
            BUILDER.comment("Sanity change per second while near Rhan-Tegoth (negative drains).")
                    .defineInRange("sanityRhanTegothRate", -0.25, -10.0, 0.0);
    public static final ModConfigSpec.IntValue SANITY_RHAN_TEGOTH_RADIUS =
            BUILDER.comment("Radius (blocks) of Rhan-Tegoth's sanity drain.")
                    .defineInRange("sanityRhanTegothRadius", 32, 1, 96);

    // --- Rite framework ---------------------------------------------------------------------

    /**
     * Minimum ticks between one player's rite performances. Prevents rite spam (design/05-rituals).
     * Applies per player; a rejected attempt still pays no cost and changes nothing.
     */
    public static final ModConfigSpec.IntValue RITUAL_COOLDOWN_TICKS =
            BUILDER.comment("Minimum ticks between a player's rite performances (0 disables).")
                    .defineInRange("ritualCooldownTicks", 40, 0, 72000);

    /** Whether performing a rite consumes its listed offerings (design/08). Off = creative testing. */
    public static final ModConfigSpec.BooleanValue REQUIRE_RITE_REAGENTS =
            BUILDER.comment("Require and consume a rite's offerings. Disable for costless testing.")
                    .define("requireRiteReagents", true);

    /** Whether a rite's Conditions (time/moon/weather/corruption/rift) are enforced (design/08). */
    public static final ModConfigSpec.BooleanValue REQUIRE_RITE_CONDITIONS =
            BUILDER.comment("Enforce a rite's world conditions (night, moon, weather, stage, rift).")
                    .define("requireRiteConditions", true);

    /** Whether a rite must be performed at a matching altar pattern (design/05). */
    public static final ModConfigSpec.BooleanValue REQUIRE_RITE_ALTAR =
            BUILDER.comment("Require a rite to be performed at an altar_core marked with runes/chalk.")
                    .define("requireRiteAltar", true);

    /** Horizontal radius searched for the altar core and its marks. */
    public static final ModConfigSpec.IntValue RITE_ALTAR_RADIUS =
            BUILDER.comment("Radius searched for an altar core and its rune/chalk marks.")
                    .defineInRange("riteAltarRadius", 6, 2, 16);

    /** How many rune_stone / ritual_chalk marks the altar needs around its core. */
    public static final ModConfigSpec.IntValue RITE_ALTAR_MARKS =
            BUILDER.comment("Rune/chalk marks required around the altar core.")
                    .defineInRange("riteAltarMarks", 4, 0, 24);

    // --- Rift discovery for the world map (design/22) ------------------------------------------

    /** Whether players discover nearby rifts and mark them on the world map. */
    public static final ModConfigSpec.BooleanValue ENABLE_RIFT_DISCOVERY =
            BUILDER.comment("Discover nearby rifts and show them on the world map.")
                    .define("enableRiftDiscovery", true);

    /** Chunk radius scanned around a player for discovered rifts (loaded chunks only). */
    public static final ModConfigSpec.IntValue RIFT_DISCOVERY_CHUNK_RADIUS =
            BUILDER.comment("Chunk radius scanned around a player for rift discovery.")
                    .defineInRange("riftDiscoveryChunkRadius", 2, 0, 8);

    /** Cap on rifts collected per scan (so a rift field cannot blow up the player's marker set). */
    public static final ModConfigSpec.IntValue RIFT_DISCOVERY_MAX =
            BUILDER.comment("Max rifts recorded per player per discovery pass.")
                    .defineInRange("riftDiscoveryMax", 32, 1, 256);

    // --- Ancient One non-combat solves (design/28: "always a non-combat solve") ---------------
    // Three solve rites (rite/Rites.java) resolve through the shared RiteEngine SOOTHE outcome,
    // which finds the nearest compatible AncientOne within riteSolveRadius and asks it to solve.
    // Each solve has its own availability toggle; disabling one makes its rite refuse, never
    // silently no-op (the rite's cost is still paid, per design/08).

    /** Cthulhu: {@code soothe_cthulhu} puts the drowned-temple presence to sleep instead of killing it. */
    public static final ModConfigSpec.BooleanValue ENABLE_RITE_SOOTHE_CTHULHU =
            BUILDER.comment("Allow soothe_cthulhu to put Cthulhu to sleep (a non-combat solve).")
                    .define("enableRiteSootheCthulhu", true);

    /** Dunwich Horror: {@code draw_away_dunwich} lures it off the settlement and clears local taint. */
    public static final ModConfigSpec.BooleanValue ENABLE_RITE_CLEANSE_DUNWICH =
            BUILDER.comment("Allow draw_away_dunwich to banish the Dunwich Horror and cleanse local taint.")
                    .define("enableRiteCleanseDunwich", true);

    /** Shub-Niggurath: {@code still_shub_niggurath} stills the woods' aura for a bounded duration. */
    public static final ModConfigSpec.BooleanValue ENABLE_RITE_STILL_SHUB_NIGGURATH =
            BUILDER.comment("Allow still_shub_niggurath to suppress Shub-Niggurath's aura for a while.")
                    .define("enableRiteStillShubNiggurath", true);

    /** Radius (blocks) within which a solve rite may find its Ancient One; loaded chunks only. */
    public static final ModConfigSpec.IntValue RITE_SOLVE_RADIUS =
            BUILDER.comment("Radius (blocks) a solve rite searches for its Ancient One.")
                    .defineInRange("riteSolveRadius", 8, 2, 32);

    /** How long {@code still_shub_niggurath} stills the biome presence (ticks). */
    public static final ModConfigSpec.IntValue RITE_STILL_SHUB_NIGGURATH_TICKS =
            BUILDER.comment("Ticks Shub-Niggurath's aura stays stilled after still_shub_niggurath.")
                    .defineInRange("riteStillShubNiggurathTicks", 6000, 200, 72000);

    // Ancient One non-combat solves, batch 2 (design/28). Same shared SOOTHE outcome; each toggle
    // gates one target. A disabled solve makes its rite refuse (cost still paid).
    public static final ModConfigSpec.BooleanValue ENABLE_RITE_STILL_AZATHOTH =
            BUILDER.comment("Allow still_azathoth to still the music at the centre of the rift_scar.")
                    .define("enableRiteStillAzathoth", true);
    public static final ModConfigSpec.BooleanValue ENABLE_RITE_SEAL_YOG_SOTHOTH =
            BUILDER.comment("Allow seal_the_gate to close Yog-Sothoth's gate on the plateau.")
                    .define("enableRiteSealYogSothoth", true);
    public static final ModConfigSpec.BooleanValue ENABLE_RITE_WARD_ITHAQUA =
            BUILDER.comment("Allow ward_ithaqua to turn the walking wind aside for a time.")
                    .define("enableRiteWardIthaqua", true);
    public static final ModConfigSpec.BooleanValue ENABLE_RITE_APPEASE_YIG =
            BUILDER.comment("Allow appease_yig to stop Yig drinking the deaths.")
                    .define("enableRiteAppeaseYig", true);
    public static final ModConfigSpec.BooleanValue ENABLE_RITE_BIND_ATLACH_NACHA =
            BUILDER.comment("Allow bind_atlach_nacha to undo the web and let the scar close.")
                    .define("enableRiteBindAtlachNacha", true);

    /** How long {@code ward_ithaqua} stills the wind (ticks). */
    public static final ModConfigSpec.IntValue RITE_WARD_ITHAQUA_TICKS =
            BUILDER.comment("Ticks Ithaqua's aura stays stilled after ward_ithaqua.")
                    .defineInRange("riteWardIthaquaTicks", 6000, 200, 72000);

    /** How long {@code bind_atlach_nacha} binds the weaver (ticks). */
    public static final ModConfigSpec.IntValue RITE_BIND_ATLACH_NACHA_TICKS =
            BUILDER.comment("Ticks Atlach-Nacha stays bound after bind_atlach_nacha.")
                    .defineInRange("riteBindAtlachNachaTicks", 6000, 200, 72000);

    // Ancient One non-combat solves, batch 3 (design/28): Nyarlathotep / Cthugha / Glaaki / Hydra /
    // Nyogtha / Rhan-Tegoth. Same shared SOOTHE outcome; each toggle gates one target.
    public static final ModConfigSpec.BooleanValue ENABLE_RITE_DENY_NYARLATHOTEP =
            BUILDER.comment("Allow deny_nyarlathotep to name and set aside the trusted face.")
                    .define("enableRiteDenyNyarlathotep", true);
    public static final ModConfigSpec.BooleanValue ENABLE_RITE_QUENCH_CTHUGHA =
            BUILDER.comment("Allow quench_cthugha to dim the watching flame for a time.")
                    .define("enableRiteQuenchCthugha", true);
    public static final ModConfigSpec.BooleanValue ENABLE_RITE_STILL_GLAAKI =
            BUILDER.comment("Allow still_glaaki to still the same lake and free the servitors.")
                    .define("enableRiteStillGlaaki", true);
    public static final ModConfigSpec.BooleanValue ENABLE_RITE_SEVER_HYDRA =
            BUILDER.comment("Allow sever_hydra to stop the heads budding for a time.")
                    .define("enableRiteSeverHydra", true);
    public static final ModConfigSpec.BooleanValue ENABLE_RITE_BAR_NYOGTHA =
            BUILDER.comment("Allow bar_nyogtha to shut the way below and stop the hearing.")
                    .define("enableRiteBarNyogtha", true);
    public static final ModConfigSpec.BooleanValue ENABLE_RITE_TOPPLE_IDOL =
            BUILDER.comment("Allow topple_idol to topple Rhan-Tegoth and end the worship.")
                    .define("enableRiteToppleIdol", true);

    /** How long {@code quench_cthugha} dims the watching flame (ticks). */
    public static final ModConfigSpec.IntValue RITE_QUENCH_CTHUGHA_TICKS =
            BUILDER.comment("Ticks Cthugha's aura stays quenched after quench_cthugha.")
                    .defineInRange("riteQuenchCthughaTicks", 6000, 200, 72000);

    /** How long {@code sever_hydra} stops the heads budding (ticks). */
    public static final ModConfigSpec.IntValue RITE_SEVER_HYDRA_TICKS =
            BUILDER.comment("Ticks the Hydra stays severed after sever_hydra.")
                    .defineInRange("riteSeverHydraTicks", 6000, 200, 72000);

    /** How far (blocks) {@code draw_away_dunwich} draws the Horror from the settlement. */
    public static final ModConfigSpec.IntValue DUNWICH_RETREAT_DISTANCE =
            BUILDER.comment("Distance (blocks) draw_away_dunwich draws the Dunwich Horror away.")
                    .defineInRange("dunwichRetreatDistance", 64, 8, 192);

    // --- Ancient Ones, batch 4 (design/28): the last six presences ----------------------------
    // Four new minor sites own one each (yellow_court / black_pyramid / spawning_pool /
    // temple_of_the_feaster); Tulzscha and Zstylzhemghi share the existing rift_scar. Same
    // site-bound spawn shape and same shared SOOTHE solve outcome as the earlier batches.

    /** Hastur: the unspoken name in the yellow_court. */
    public static final ModConfigSpec.BooleanValue HASTUR_KILLABLE =
            BUILDER.comment("Allow Hastur to be killed. False = permanently invulnerable.")
                    .define("hasturKillable", true);
    public static final ModConfigSpec.BooleanValue ENABLE_SITE_HASTUR =
            BUILDER.comment("Allow Hastur to hold the yellow_court (loaded-only).")
                    .define("enableSiteHastur", true);
    public static final ModConfigSpec.IntValue SITE_HASTUR_COUNT =
            BUILDER.comment("Max Hasturs spawned per yellow_court pass.")
                    .defineInRange("siteHasturCount", 1, 0, 2);
    public static final ModConfigSpec.IntValue SITE_HASTUR_CAP =
            BUILDER.comment("Max Hasturs near one player at the court before pausing.")
                    .defineInRange("siteHasturCap", 1, 1, 4);
    public static final ModConfigSpec.BooleanValue ENABLE_SANITY_HASTUR =
            BUILDER.comment("Enable the Hastur sanity source (hearing the name makes you want to say it).")
                    .define("enableSanityHastur", true);
    public static final ModConfigSpec.DoubleValue SANITY_HASTUR_RATE =
            BUILDER.comment("Sanity change per second while near Hastur (negative drains).")
                    .defineInRange("sanityHasturRate", -0.42, -10.0, 0.0);
    public static final ModConfigSpec.IntValue SANITY_HASTUR_RADIUS =
            BUILDER.comment("Radius (blocks) of Hastur's sanity drain.")
                    .defineInRange("sanityHasturRadius", 34, 1, 96);

    /** Nephren-Ka: the king who remembers you, in the black_pyramid. */
    public static final ModConfigSpec.BooleanValue NEPHREN_KA_KILLABLE =
            BUILDER.comment("Allow Nephren-Ka to be killed. False = permanently invulnerable.")
                    .define("nephrenKaKillable", true);
    public static final ModConfigSpec.BooleanValue ENABLE_SITE_NEPHREN_KA =
            BUILDER.comment("Allow Nephren-Ka to hold the black_pyramid (loaded-only).")
                    .define("enableSiteNephrenKa", true);
    public static final ModConfigSpec.IntValue SITE_NEPHREN_KA_COUNT =
            BUILDER.comment("Max Nephren-Kas spawned per black_pyramid pass.")
                    .defineInRange("siteNephrenKaCount", 1, 0, 2);
    public static final ModConfigSpec.IntValue SITE_NEPHREN_KA_CAP =
            BUILDER.comment("Max Nephren-Kas near one player at the pyramid before pausing.")
                    .defineInRange("siteNephrenKaCap", 1, 1, 4);
    public static final ModConfigSpec.BooleanValue ENABLE_CORRUPTION_NEPHREN_KA =
            BUILDER.comment("Enable the Nephren-Ka corruption aura (his gaze turns your cult against you).")
                    .define("enableCorruptionNephrenKa", true);
    public static final ModConfigSpec.DoubleValue CORRUPTION_NEPHREN_KA_RATE =
            BUILDER.comment("Corruption per second while near Nephren-Ka (positive taints).")
                    .defineInRange("corruptionNephrenKaRate", 0.16, 0.0, 10.0);
    public static final ModConfigSpec.IntValue CORRUPTION_NEPHREN_KA_RADIUS =
            BUILDER.comment("Radius (blocks) of Nephren-Ka's corruption aura.")
                    .defineInRange("corruptionNephrenKaRadius", 30, 1, 96);

    /** Abhoth: the spawning pool — killing is the trap. */
    public static final ModConfigSpec.BooleanValue ABHOTH_KILLABLE =
            BUILDER.comment("Allow Abhoth to be killed. False = permanently invulnerable.")
                    .define("abhothKillable", true);
    public static final ModConfigSpec.BooleanValue ENABLE_SITE_ABHOTH =
            BUILDER.comment("Allow Abhoth to fill the spawning_pool (loaded-only).")
                    .define("enableSiteAbhoth", true);
    public static final ModConfigSpec.IntValue SITE_ABHOTH_COUNT =
            BUILDER.comment("Max Abhoths spawned per spawning_pool pass.")
                    .defineInRange("siteAbhothCount", 1, 0, 2);
    public static final ModConfigSpec.IntValue SITE_ABHOTH_CAP =
            BUILDER.comment("Max Abhoths near one player at the pool before pausing.")
                    .defineInRange("siteAbhothCap", 1, 1, 4);
    public static final ModConfigSpec.BooleanValue ENABLE_CORRUPTION_ABHOTH =
            BUILDER.comment("Enable the Abhoth corruption aura (the filth it spawns taints the ground).")
                    .define("enableCorruptionAbhoth", true);
    public static final ModConfigSpec.DoubleValue CORRUPTION_ABHOTH_RATE =
            BUILDER.comment("Corruption per second while near Abhoth (positive taints).")
                    .defineInRange("corruptionAbhothRate", 0.18, 0.0, 10.0);
    public static final ModConfigSpec.IntValue CORRUPTION_ABHOTH_RADIUS =
            BUILDER.comment("Radius (blocks) of Abhoth's corruption aura.")
                    .defineInRange("corruptionAbhothRadius", 28, 1, 96);

    /** Chaugnar Faugn: the hunger you can hear, in the temple_of_the_feaster. */
    public static final ModConfigSpec.BooleanValue CHAUGNAR_FAUGN_KILLABLE =
            BUILDER.comment("Allow Chaugnar Faugn to be killed. False = permanently invulnerable.")
                    .define("chaugnarFaugnKillable", true);
    public static final ModConfigSpec.BooleanValue ENABLE_SITE_CHAUGNAR_FAUGN =
            BUILDER.comment("Allow Chaugnar Faugn to hold the temple_of_the_feaster (loaded-only).")
                    .define("enableSiteChaugnarFaugn", true);
    public static final ModConfigSpec.IntValue SITE_CHAUGNAR_FAUGN_COUNT =
            BUILDER.comment("Max Chaugnar Faugns spawned per temple pass.")
                    .defineInRange("siteChaugnarFaugnCount", 1, 0, 2);
    public static final ModConfigSpec.IntValue SITE_CHAUGNAR_FAUGN_CAP =
            BUILDER.comment("Max Chaugnar Faugns near one player at the temple before pausing.")
                    .defineInRange("siteChaugnarFaugnCap", 1, 1, 4);
    public static final ModConfigSpec.BooleanValue ENABLE_SANITY_CHAUGNAR_FAUGN =
            BUILDER.comment("Enable the Chaugnar Faugn sanity source (it feeds on your courage).")
                    .define("enableSanityChaugnarFaugn", true);
    public static final ModConfigSpec.DoubleValue SANITY_CHAUGNAR_FAUGN_RATE =
            BUILDER.comment("Sanity change per second while near Chaugnar Faugn (negative drains).")
                    .defineInRange("sanityChaugnarFaugnRate", -0.38, -10.0, 0.0);
    public static final ModConfigSpec.IntValue SANITY_CHAUGNAR_FAUGN_RADIUS =
            BUILDER.comment("Radius (blocks) of Chaugnar Faugn's sanity drain.")
                    .defineInRange("sanityChaugnarFaugnRadius", 32, 1, 96);

    /** Tulzscha: the green flame at the centre of the rift_scar. */
    public static final ModConfigSpec.BooleanValue TULZSCHA_KILLABLE =
            BUILDER.comment("Allow Tulzscha to be killed. False = permanently invulnerable.")
                    .define("tulzschaKillable", true);
    public static final ModConfigSpec.BooleanValue ENABLE_SITE_TULZSCHA =
            BUILDER.comment("Allow Tulzscha to burn at the rift_scar centre (loaded-only).")
                    .define("enableSiteTulzscha", true);
    public static final ModConfigSpec.IntValue SITE_TULZSCHA_COUNT =
            BUILDER.comment("Max Tulzschas spawned per rift_scar pass.")
                    .defineInRange("siteTulzschaCount", 1, 0, 2);
    public static final ModConfigSpec.IntValue SITE_TULZSCHA_CAP =
            BUILDER.comment("Max Tulzschas near one player at the scar before pausing.")
                    .defineInRange("siteTulzschaCap", 1, 1, 4);
    public static final ModConfigSpec.BooleanValue ENABLE_SANITY_TULZSCHA =
            BUILDER.comment("Enable the Tulzscha sanity source (looking into the flame costs memory).")
                    .define("enableSanityTulzscha", true);
    public static final ModConfigSpec.DoubleValue SANITY_TULZSCHA_RATE =
            BUILDER.comment("Sanity change per second while near Tulzscha (negative drains).")
                    .defineInRange("sanityTulzschaRate", -0.50, -10.0, 0.0);
    public static final ModConfigSpec.IntValue SANITY_TULZSCHA_RADIUS =
            BUILDER.comment("Radius (blocks) of Tulzscha's sanity drain.")
                    .defineInRange("sanityTulzschaRadius", 36, 1, 96);

    /** Zstylzhemghi: the name forgotten on purpose, eroding the court at the scar. */
    public static final ModConfigSpec.BooleanValue ZSTYLZHEMGHI_KILLABLE =
            BUILDER.comment("Allow Zstylzhemghi to be killed. False = permanently invulnerable.")
                    .define("zstylzhemghiKillable", true);
    public static final ModConfigSpec.BooleanValue ENABLE_SITE_ZSTYLZHEMGHI =
            BUILDER.comment("Allow Zstylzhemghi to erode the rift_scar court (loaded-only).")
                    .define("enableSiteZstylzhemghi", true);
    public static final ModConfigSpec.IntValue SITE_ZSTYLZHEMGHI_COUNT =
            BUILDER.comment("Max Zstylzhemghis spawned per rift_scar pass.")
                    .defineInRange("siteZstylzhemghiCount", 1, 0, 2);
    public static final ModConfigSpec.IntValue SITE_ZSTYLZHEMGHI_CAP =
            BUILDER.comment("Max Zstylzhemghis near one player at the scar before pausing.")
                    .defineInRange("siteZstylzhemghiCap", 1, 1, 4);
    public static final ModConfigSpec.BooleanValue ENABLE_CORRUPTION_ZSTYLZHEMGHI =
            BUILDER.comment("Enable the Zstylzhemghi corruption aura (the world erodes around the name).")
                    .define("enableCorruptionZstylzhemghi", true);
    public static final ModConfigSpec.DoubleValue CORRUPTION_ZSTYLZHEMGHI_RATE =
            BUILDER.comment("Corruption per second while near Zstylzhemghi (positive taints).")
                    .defineInRange("corruptionZstylzhemghiRate", 0.20, 0.0, 10.0);
    public static final ModConfigSpec.IntValue CORRUPTION_ZSTYLZHEMGHI_RADIUS =
            BUILDER.comment("Radius (blocks) of Zstylzhemghi's corruption aura.")
                    .defineInRange("corruptionZstylzhemghiRadius", 34, 1, 96);

    // Ancient One non-combat solves, batch 4 (design/28). Same shared SOOTHE outcome.
    public static final ModConfigSpec.BooleanValue ENABLE_RITE_SILENCE_HASTUR =
            BUILDER.comment("Allow silence_hastur to still the yellow court for a time.")
                    .define("enableRiteSilenceHastur", true);
    public static final ModConfigSpec.BooleanValue ENABLE_RITE_UNMAKE_NEPHREN_KA =
            BUILDER.comment("Allow unmake_nephren_ka to make the king forget you.")
                    .define("enableRiteUnmakeNephrenKa", true);
    public static final ModConfigSpec.BooleanValue ENABLE_RITE_SEAL_ABHOTH =
            BUILDER.comment("Allow seal_abhoth to seal the spawning pool without killing it.")
                    .define("enableRiteSealAbhoth", true);
    public static final ModConfigSpec.BooleanValue ENABLE_RITE_STARVE_CHAUGNAR_FAUGN =
            BUILDER.comment("Allow starve_chaugnar_faugn to feed the hunger nothing.")
                    .define("enableRiteStarveChaugnarFaugn", true);
    public static final ModConfigSpec.BooleanValue ENABLE_RITE_QUENCH_TULZSCHA =
            BUILDER.comment("Allow quench_tulzscha to close the green flame's eye.")
                    .define("enableRiteQuenchTulzscha", true);
    public static final ModConfigSpec.BooleanValue ENABLE_RITE_ERASE_ZSTYLZHEMGHI =
            BUILDER.comment("Allow erase_zstylzhemghi to forget the forgotten name again.")
                    .define("enableRiteEraseZstylzhemghi", true);

    /** How long {@code silence_hastur} stills the court (ticks). */
    public static final ModConfigSpec.IntValue RITE_SILENCE_HASTUR_TICKS =
            BUILDER.comment("Ticks the yellow court stays stilled after silence_hastur.")
                    .defineInRange("riteSilenceHasturTicks", 6000, 200, 72000);

    /** How long {@code erase_zstylzhemghi} pauses the erosion (ticks). */
    public static final ModConfigSpec.IntValue RITE_ERASE_ZSTYLZHEMGHI_TICKS =
            BUILDER.comment("Ticks the erosion stays paused after erase_zstylzhemghi.")
                    .defineInRange("riteEraseZstylzhemghiTicks", 6000, 200, 72000);

    // --- Bestiary: blight_pod (creeper-role corruption vector) --------------------------------

    /** The blight pod's spore aura: standing near one taints you slowly. */
    public static final ModConfigSpec.BooleanValue ENABLE_CORRUPTION_BLIGHT_POD =
            BUILDER.comment("Enable the 'blight_pod' corruption vector (nearby pods taint).")
                    .define("enableCorruptionBlightPod", true);

    public static final ModConfigSpec.DoubleValue CORRUPTION_BLIGHT_POD_RATE =
            BUILDER.comment("Corruption per second per nearby blight pod (positive taints).")
                    .defineInRange("corruptionBlightPodRate", 0.05, 0.0, 10.0);

    public static final ModConfigSpec.IntValue CORRUPTION_BLIGHT_POD_RADIUS =
            BUILDER.comment("Radius (blocks) in which blight pods contribute corruption.")
                    .defineInRange("corruptionBlightPodRadius", 6, 1, 32);

    public static final ModConfigSpec.IntValue CORRUPTION_BLIGHT_POD_MAX =
            BUILDER.comment("Maximum blight pods counted toward the corruption vector at once.")
                    .defineInRange("corruptionBlightPodMax", 3, 1, 32);

    public static final ModConfigSpec.BooleanValue ENABLE_BLIGHT_POD_SPAWNS =
            BUILDER.comment("Allow blight pods to spawn near players in dark/tainted loaded terrain.")
                    .define("enableBlightPodSpawns", true);

    public static final ModConfigSpec.IntValue BLIGHT_POD_SPAWN_INTERVAL_TICKS =
            BUILDER.comment("Ticks between blight-pod spawn passes (minimum 20).")
                    .defineInRange("blightPodSpawnIntervalTicks", 260, 20, 24000);

    public static final ModConfigSpec.IntValue BLIGHT_POD_SPAWN_COUNT =
            BUILDER.comment("Max blight pods spawned per player per pass.")
                    .defineInRange("blightPodSpawnCount", 2, 0, 16);

    public static final ModConfigSpec.IntValue BLIGHT_POD_SPAWN_RADIUS =
            BUILDER.comment("Radius (blocks) around each player in which blight pods may spawn.")
                    .defineInRange("blightPodSpawnRadius", 32, 8, 96);

    public static final ModConfigSpec.IntValue BLIGHT_POD_SPAWN_CAP =
            BUILDER.comment("Max blight pods allowed near one player before spawning pauses.")
                    .defineInRange("blightPodSpawnCap", 4, 1, 64);

    /** Taint added to each loaded chunk in the burst radius when a blight pod dies. */
    public static final ModConfigSpec.DoubleValue BLIGHT_POD_BURST_TAINT =
            BUILDER.comment("Taint added per chunk in a blight pod's death burst (bounded).")
                    .defineInRange("blightPodBurstTaint", 0.12, 0.0, 1.0);

    /** Chunk half-width of the blight pod's death burst (1 = a 3x3 chunk patch). */
    public static final ModConfigSpec.IntValue BLIGHT_POD_BURST_RADIUS =
            BUILDER.comment("Chunk radius of a blight pod's death burst (1 = 3x3 chunks).")
                    .defineInRange("blightPodBurstRadius", 1, 0, 4);

    /** Hard cap on blocks placed per burst, so the terrain edit stays tiny. */
    public static final ModConfigSpec.IntValue BLIGHT_POD_BURST_MAX_BLOCKS =
            BUILDER.comment("Maximum sculk/tainted-soil blocks a single blight pod burst may place.")
                    .defineInRange("blightPodBurstMaxBlocks", 6, 0, 24);

    // --- Bestiary: byakhee (flying ambusher / phantom-role) -----------------------------------

    /** The byakhee's dive: approaching it drains sanity. */
    public static final ModConfigSpec.BooleanValue ENABLE_SANITY_BYAKHEE =
            BUILDER.comment("Enable the 'byakhee' sanity source (a nearby flier drains sanity).")
                    .define("enableSanityByakhee", true);

    public static final ModConfigSpec.DoubleValue SANITY_BYAKHEE_RATE =
            BUILDER.comment("Sanity change per second per nearby byakhee (negative drains).")
                    .defineInRange("sanityByakheeRate", -0.12, -10.0, 0.0);

    public static final ModConfigSpec.IntValue SANITY_BYAKHEE_RADIUS =
            BUILDER.comment("Radius (blocks) in which byakhees contribute to the sanity drain.")
                    .defineInRange("sanityByakheeRadius", 12, 1, 32);

    public static final ModConfigSpec.IntValue SANITY_BYAKHEE_MAX =
            BUILDER.comment("Maximum byakhees counted toward the sanity drain at once.")
                    .defineInRange("sanityByakheeMax", 2, 1, 32);

    public static final ModConfigSpec.BooleanValue ENABLE_BYAKHEE_SPAWNS =
            BUILDER.comment("Allow byakhees to spawn above players at night (dark sky).")
                    .define("enableByakheeSpawns", true);

    public static final ModConfigSpec.IntValue BYAKHEE_SPAWN_INTERVAL_TICKS =
            BUILDER.comment("Ticks between byakhee spawn passes (minimum 20).")
                    .defineInRange("byakheeSpawnIntervalTicks", 600, 20, 24000);

    public static final ModConfigSpec.IntValue BYAKHEE_SPAWN_COUNT =
            BUILDER.comment("Max byakhees spawned per player per pass.")
                    .defineInRange("byakheeSpawnCount", 2, 0, 8);

    public static final ModConfigSpec.IntValue BYAKHEE_SPAWN_RADIUS =
            BUILDER.comment("Radius (blocks) around each player in which byakhees may appear.")
                    .defineInRange("byakheeSpawnRadius", 40, 8, 128);

    public static final ModConfigSpec.IntValue BYAKHEE_SPAWN_CAP =
            BUILDER.comment("Max byakhees allowed near one player before spawning pauses.")
                    .defineInRange("byakheeSpawnCap", 3, 1, 16);

    /** Lowest Y a byakhee may spawn at; keeps them above the deep dark below the surface. */
    public static final ModConfigSpec.IntValue BYAKHEE_SPAWN_MIN_Y =
            BUILDER.comment("Minimum Y at which a byakhee may spawn (fliers stay high).")
                    .defineInRange("byakheeSpawnMinY", 50, -64, 320);

    // --- Bestiary: star_spawn (minion, sound-hunter, resists mundane weapons) -----------------

    /** The star-spawn's heavy line-of-sight drain. */
    public static final ModConfigSpec.BooleanValue ENABLE_SANITY_STAR_SPAWN =
            BUILDER.comment("Enable the 'star_spawn' sanity source (in-sight drain, heavy).")
                    .define("enableSanityStarSpawn", true);

    public static final ModConfigSpec.DoubleValue SANITY_STAR_SPAWN_RATE =
            BUILDER.comment("Sanity change per second per visible star-spawn (negative drains).")
                    .defineInRange("sanityStarSpawnRate", -0.45, -10.0, 0.0);

    public static final ModConfigSpec.IntValue SANITY_STAR_SPAWN_RADIUS =
            BUILDER.comment("Radius (blocks) in which star-spawns contribute to the sanity drain.")
                    .defineInRange("sanityStarSpawnRadius", 24, 1, 64);

    public static final ModConfigSpec.IntValue SANITY_STAR_SPAWN_MAX =
            BUILDER.comment("Maximum star-spawns counted toward the sanity drain at once.")
                    .defineInRange("sanityStarSpawnMax", 1, 1, 8);

    /** Whether the star-spawn resists mundane weapons (only ward-breaker items wound it). */
    public static final ModConfigSpec.BooleanValue STAR_SPAWN_RESISTS_MUNDANE =
            BUILDER.comment("Star-spawn resists mundane weapons; only #eldritch_horror:ward_breakers wound it.")
                    .define("starSpawnResistsMundane", true);

    /** Ambient spawns are OFF by default: star-spawns are summoned by rites, not wandered into. */
    public static final ModConfigSpec.BooleanValue ENABLE_STAR_SPAWN_SPAWNS =
            BUILDER.comment("Allow ambient star-spawn spawns (default OFF; rites/eggs still work).")
                    .define("enableStarSpawnSpawns", false);

    public static final ModConfigSpec.IntValue STAR_SPAWN_SPAWN_INTERVAL_TICKS =
            BUILDER.comment("Ticks between ambient star-spawn passes (minimum 20).")
                    .defineInRange("starSpawnSpawnIntervalTicks", 2400, 20, 72000);

    public static final ModConfigSpec.IntValue STAR_SPAWN_SPAWN_COUNT =
            BUILDER.comment("Max star-spawns spawned per player per pass.")
                    .defineInRange("starSpawnSpawnCount", 1, 0, 4);

    public static final ModConfigSpec.IntValue STAR_SPAWN_SPAWN_RADIUS =
            BUILDER.comment("Radius (blocks) around each player in which star-spawns may appear.")
                    .defineInRange("starSpawnSpawnRadius", 40, 8, 128);

    public static final ModConfigSpec.IntValue STAR_SPAWN_SPAWN_CAP =
            BUILDER.comment("Max star-spawns allowed near one player before spawning pauses.")
                    .defineInRange("starSpawnSpawnCap", 2, 1, 8);

    // --- Bestiary: shoggoth_mass (minion, slow elite, wide corruption aura) -------------------

    /** The shoggoth mass's wide corruption aura. */
    public static final ModConfigSpec.BooleanValue ENABLE_CORRUPTION_SHOGGOTH =
            BUILDER.comment("Enable the 'shoggoth_mass' corruption aura (a wide, slow taint).")
                    .define("enableCorruptionShoggoth", true);

    public static final ModConfigSpec.DoubleValue CORRUPTION_SHOGGOTH_RATE =
            BUILDER.comment("Corruption per second per nearby shoggoth mass (positive taints).")
                    .defineInRange("corruptionShoggothRate", 0.10, 0.0, 10.0);

    public static final ModConfigSpec.IntValue CORRUPTION_SHOGGOTH_RADIUS =
            BUILDER.comment("Radius (blocks) of the shoggoth mass's corruption aura; wide by design.")
                    .defineInRange("corruptionShoggothRadius", 20, 1, 64);

    public static final ModConfigSpec.IntValue CORRUPTION_SHOGGOTH_MAX =
            BUILDER.comment("Maximum shoggoth masses counted toward the corruption aura at once.")
                    .defineInRange("corruptionShoggothMax", 1, 1, 8);

    /** Ambient spawns are OFF by default: shoggoth masses are summoned by rites/events. */
    public static final ModConfigSpec.BooleanValue ENABLE_SHOGGOTH_SPAWNS =
            BUILDER.comment("Allow ambient shoggoth-mass spawns (default OFF; rites/eggs still work).")
                    .define("enableShoggothSpawns", false);

    public static final ModConfigSpec.IntValue SHOGGOTH_SPAWN_INTERVAL_TICKS =
            BUILDER.comment("Ticks between ambient shoggoth-mass passes (minimum 20).")
                    .defineInRange("shoggothSpawnIntervalTicks", 2400, 20, 72000);

    public static final ModConfigSpec.IntValue SHOGGOTH_SPAWN_COUNT =
            BUILDER.comment("Max shoggoth masses spawned per player per pass.")
                    .defineInRange("shoggothSpawnCount", 1, 0, 4);

    public static final ModConfigSpec.IntValue SHOGGOTH_SPAWN_RADIUS =
            BUILDER.comment("Radius (blocks) around each player in which shoggoth masses may appear.")
                    .defineInRange("shoggothSpawnRadius", 40, 8, 128);

    public static final ModConfigSpec.IntValue SHOGGOTH_SPAWN_CAP =
            BUILDER.comment("Max shoggoth masses allowed near one player before spawning pauses.")
                    .defineInRange("shoggothSpawnCap", 1, 1, 4);

    // --- Bestiary: weaver_spawn (spider-role nest-guard that webs a room shut) ----------------

    /** The weaver's skittering dread: a small sanity drain while it is near. */
    public static final ModConfigSpec.BooleanValue ENABLE_SANITY_WEAVER_SPAWN =
            BUILDER.comment("Enable the 'weaver_spawn' sanity source (a nest-guard drains sanity).")
                    .define("enableSanityWeaverSpawn", true);

    public static final ModConfigSpec.DoubleValue SANITY_WEAVER_SPAWN_RATE =
            BUILDER.comment("Sanity change per second per nearby weaver (negative drains).")
                    .defineInRange("sanityWeaverSpawnRate", -0.09, -10.0, 0.0);

    public static final ModConfigSpec.IntValue SANITY_WEAVER_SPAWN_RADIUS =
            BUILDER.comment("Radius (blocks) in which weavers contribute to the sanity drain.")
                    .defineInRange("sanityWeaverSpawnRadius", 8, 1, 32);

    public static final ModConfigSpec.IntValue SANITY_WEAVER_SPAWN_MAX =
            BUILDER.comment("Maximum weavers counted toward the sanity drain at once.")
                    .defineInRange("sanityWeaverSpawnMax", 3, 1, 32);

    public static final ModConfigSpec.BooleanValue ENABLE_WEAVER_SPAWN_SPAWNS =
            BUILDER.comment("Allow weavers to spawn near players in dark/tainted loaded terrain.")
                    .define("enableWeaverSpawnSpawns", true);

    public static final ModConfigSpec.IntValue WEAVER_SPAWN_SPAWN_INTERVAL_TICKS =
            BUILDER.comment("Ticks between weaver spawn passes (minimum 20).")
                    .defineInRange("weaverSpawnSpawnIntervalTicks", 300, 20, 24000);

    public static final ModConfigSpec.IntValue WEAVER_SPAWN_SPAWN_COUNT =
            BUILDER.comment("Max weavers spawned per player per pass.")
                    .defineInRange("weaverSpawnSpawnCount", 2, 0, 16);

    public static final ModConfigSpec.IntValue WEAVER_SPAWN_SPAWN_RADIUS =
            BUILDER.comment("Radius (blocks) around each player in which weavers may spawn.")
                    .defineInRange("weaverSpawnSpawnRadius", 32, 8, 96);

    public static final ModConfigSpec.IntValue WEAVER_SPAWN_SPAWN_CAP =
            BUILDER.comment("Max weavers allowed near one player before spawning pauses.")
                    .defineInRange("weaverSpawnSpawnCap", 4, 1, 64);

    /** Whether the weaver climbs walls (its nest-guard mobility; distinct from the veil stalker). */
    public static final ModConfigSpec.BooleanValue ENABLE_WEAVER_CLIMBING =
            BUILDER.comment("Allow the weaver spawn to climb walls (its nest-guard mobility).")
                    .define("enableWeaverClimbing", true);

    /** Master switch for the weaver's bounded web placement (its signature hook). */
    public static final ModConfigSpec.BooleanValue ENABLE_WEAVER_WEBS =
            BUILDER.comment("Allow weavers to place cobwebs near their nest (bounded, loaded chunks only).")
                    .define("enableWeaverWebs", true);

    /** Hard cap on web blocks a single weaver may place, ever (the placement is one-shot). */
    public static final ModConfigSpec.IntValue WEAVER_WEB_MAX_BLOCKS =
            BUILDER.comment("Maximum cobwebs one weaver places near its spawn point (0 disables).")
                    .defineInRange("weaverWebMaxBlocks", 8, 0, 32);

    /** Whether the weaver's bite slows the target (the cave-spider shape). */
    public static final ModConfigSpec.BooleanValue ENABLE_WEAVER_SLOW_BITE =
            BUILDER.comment("Weaver bites apply slowness (the web in the wound).")
                    .define("enableWeaverSlowBite", true);

    // --- Bestiary: rift_mite (silverfish/endermite-role tainted-chunk vermin) ----------------

    /** The rift mite's swarm dread: it only bites in numbers, so the aura stacks a little. */
    public static final ModConfigSpec.BooleanValue ENABLE_CORRUPTION_RIFT_MITE =
            BUILDER.comment("Enable the 'rift_mite' corruption vector (a mite swarm taints).")
                    .define("enableCorruptionRiftMite", true);

    public static final ModConfigSpec.DoubleValue CORRUPTION_RIFT_MITE_RATE =
            BUILDER.comment("Corruption per second per nearby rift mite (positive taints).")
                    .defineInRange("corruptionRiftMiteRate", 0.03, 0.0, 10.0);

    public static final ModConfigSpec.IntValue CORRUPTION_RIFT_MITE_RADIUS =
            BUILDER.comment("Radius (blocks) in which rift mites contribute corruption.")
                    .defineInRange("corruptionRiftMiteRadius", 6, 1, 32);

    public static final ModConfigSpec.IntValue CORRUPTION_RIFT_MITE_MAX =
            BUILDER.comment("Maximum rift mites counted toward the corruption vector at once.")
                    .defineInRange("corruptionRiftMiteMax", 6, 1, 32);

    public static final ModConfigSpec.BooleanValue ENABLE_RIFT_MITE_SPAWNS =
            BUILDER.comment("Allow rift mites to spawn in tainted loaded terrain (pack vermin).")
                    .define("enableRiftMiteSpawns", true);

    public static final ModConfigSpec.IntValue RIFT_MITE_SPAWN_INTERVAL_TICKS =
            BUILDER.comment("Ticks between rift-mite spawn passes (minimum 20).")
                    .defineInRange("riftMiteSpawnIntervalTicks", 200, 20, 24000);

    public static final ModConfigSpec.IntValue RIFT_MITE_SPAWN_COUNT =
            BUILDER.comment("Max rift mites spawned per player per pass (they arrive in a group).")
                    .defineInRange("riftMiteSpawnCount", 5, 0, 24);

    public static final ModConfigSpec.IntValue RIFT_MITE_SPAWN_RADIUS =
            BUILDER.comment("Radius (blocks) around each player in which rift mites may spawn.")
                    .defineInRange("riftMiteSpawnRadius", 24, 8, 96);

    public static final ModConfigSpec.IntValue RIFT_MITE_SPAWN_CAP =
            BUILDER.comment("Max rift mites allowed near one player before spawning pauses.")
                    .defineInRange("riftMiteSpawnCap", 12, 1, 64);

    // --- Bestiary: shambler_ooze (slime-role splitter, tainted residue) -----------------------

    /** The shambler's residue: standing near one taints you slowly. */
    public static final ModConfigSpec.BooleanValue ENABLE_CORRUPTION_SHAMBLER_OOZE =
            BUILDER.comment("Enable the 'shambler_ooze' corruption vector (nearby oozes taint).")
                    .define("enableCorruptionShamblerOoze", true);

    public static final ModConfigSpec.DoubleValue CORRUPTION_SHAMBLER_OOZE_RATE =
            BUILDER.comment("Corruption per second per nearby shambler ooze (positive taints).")
                    .defineInRange("corruptionShamblerOozeRate", 0.04, 0.0, 10.0);

    public static final ModConfigSpec.IntValue CORRUPTION_SHAMBLER_OOZE_RADIUS =
            BUILDER.comment("Radius (blocks) in which shambler oozes contribute corruption.")
                    .defineInRange("corruptionShamblerOozeRadius", 6, 1, 32);

    public static final ModConfigSpec.IntValue CORRUPTION_SHAMBLER_OOZE_MAX =
            BUILDER.comment("Maximum shambler oozes counted toward the corruption vector at once.")
                    .defineInRange("corruptionShamblerOozeMax", 4, 1, 32);

    public static final ModConfigSpec.BooleanValue ENABLE_SHAMBLER_OOZE_SPAWNS =
            BUILDER.comment("Allow shambler oozes to spawn near players in dark/tainted loaded terrain.")
                    .define("enableShamblerOozeSpawns", true);

    public static final ModConfigSpec.IntValue SHAMBLER_OOZE_SPAWN_INTERVAL_TICKS =
            BUILDER.comment("Ticks between shambler-ooze spawn passes (minimum 20).")
                    .defineInRange("shamblerOozeSpawnIntervalTicks", 260, 20, 24000);

    public static final ModConfigSpec.IntValue SHAMBLER_OOZE_SPAWN_COUNT =
            BUILDER.comment("Max shambler oozes spawned per player per pass.")
                    .defineInRange("shamblerOozeSpawnCount", 2, 0, 16);

    public static final ModConfigSpec.IntValue SHAMBLER_OOZE_SPAWN_RADIUS =
            BUILDER.comment("Radius (blocks) around each player in which shambler oozes may spawn.")
                    .defineInRange("shamblerOozeSpawnRadius", 32, 8, 96);

    public static final ModConfigSpec.IntValue SHAMBLER_OOZE_SPAWN_CAP =
            BUILDER.comment("Max shambler oozes allowed near one player before spawning pauses.")
                    .defineInRange("shamblerOozeSpawnCap", 6, 1, 64);

    /** How many smaller copies a struck shambler splits into on death (0 disables the split). */
    public static final ModConfigSpec.IntValue SHAMBLER_OOZE_SPLIT_COUNT =
            BUILDER.comment("Copies spawned when a shambler ooze dies (bounded; 0 = no split).")
                    .defineInRange("shamblerOozeSplitCount", 2, 0, 4);

    /** The smallest size a split copy may be; at or below this it dies without splitting. */
    public static final ModConfigSpec.IntValue SHAMBLER_OOZE_MIN_SPLIT_SIZE =
            BUILDER.comment("Smallest size a shambler copy may have; below it the split stops.")
                    .defineInRange("shamblerOozeMinSplitSize", 1, 0, 3);

    /** Whether a shambler death also leaves a little taint behind (its residue). */
    public static final ModConfigSpec.BooleanValue ENABLE_SHAMBLER_OOZE_RESIDUE =
            BUILDER.comment("A shambler's death leaves a small taint residue in its chunk.")
                    .define("enableShamblerOozeResidue", true);

    public static final ModConfigSpec.DoubleValue SHAMBLER_OOZE_RESIDUE_TAINT =
            BUILDER.comment("Taint added to the shambler's chunk on death (bounded).")
                    .defineInRange("shamblerOozeResidueTaint", 0.06, 0.0, 1.0);

    // --- Bestiary: choir_spite (vex-role wall-passing focus drain) ----------------------------

    /** The spite's focus drain: it passes walls and sips sanity while it lasts. */
    public static final ModConfigSpec.BooleanValue ENABLE_SANITY_CHOIR_SPITE =
            BUILDER.comment("Enable the 'choir_spite' sanity source (a wall-passing mote drains focus).")
                    .define("enableSanityChoirSpite", true);

    public static final ModConfigSpec.DoubleValue SANITY_CHOIR_SPITE_RATE =
            BUILDER.comment("Sanity change per second per nearby choir spite (negative drains).")
                    .defineInRange("sanityChoirSpiteRate", -0.14, -10.0, 0.0);

    public static final ModConfigSpec.IntValue SANITY_CHOIR_SPITE_RADIUS =
            BUILDER.comment("Radius (blocks) in which choir spites contribute to the sanity drain.")
                    .defineInRange("sanityChoirSpiteRadius", 10, 1, 32);

    public static final ModConfigSpec.IntValue SANITY_CHOIR_SPITE_MAX =
            BUILDER.comment("Maximum choir spites counted toward the sanity drain at once.")
                    .defineInRange("sanityChoirSpiteMax", 3, 1, 32);

    public static final ModConfigSpec.BooleanValue ENABLE_CHOIR_SPITE_SPAWNS =
            BUILDER.comment("Allow choir spites to spawn near players in dark/tainted loaded terrain.")
                    .define("enableChoirSpiteSpawns", true);

    public static final ModConfigSpec.IntValue CHOIR_SPITE_SPAWN_INTERVAL_TICKS =
            BUILDER.comment("Ticks between choir-spite spawn passes (minimum 20).")
                    .defineInRange("choirSpiteSpawnIntervalTicks", 400, 20, 24000);

    public static final ModConfigSpec.IntValue CHOIR_SPITE_SPAWN_COUNT =
            BUILDER.comment("Max choir spites spawned per player per pass.")
                    .defineInRange("choirSpiteSpawnCount", 2, 0, 8);

    public static final ModConfigSpec.IntValue CHOIR_SPITE_SPAWN_RADIUS =
            BUILDER.comment("Radius (blocks) around each player in which choir spites may appear.")
                    .defineInRange("choirSpiteSpawnRadius", 36, 8, 128);

    public static final ModConfigSpec.IntValue CHOIR_SPITE_SPAWN_CAP =
            BUILDER.comment("Max choir spites allowed near one player before spawning pauses.")
                    .defineInRange("choirSpiteSpawnCap", 3, 1, 16);

    public static final ModConfigSpec.IntValue CHOIR_SPITE_SPAWN_MIN_Y =
            BUILDER.comment("Minimum Y at which a choir spite may spawn (fliers stay above the deep dark).")
                    .defineInRange("choirSpiteSpawnMinY", 50, -64, 320);

    /** Whether the spite passes through walls (its defining movement). */
    public static final ModConfigSpec.BooleanValue ENABLE_CHOIR_SPITE_PHASING =
            BUILDER.comment("Allow the choir spite to pass through walls (no physics).")
                    .define("enableChoirSpitePhasing", true);

    /** How long (seconds) a spite lasts before it gutters out; bounded so it never accumulates. */
    public static final ModConfigSpec.IntValue CHOIR_SPITE_LIFETIME_SECONDS =
            BUILDER.comment("Seconds a choir spite exists before it gutters out (bounded).")
                    .defineInRange("choirSpiteLifetimeSeconds", 60, 5, 600);

    // --- Bestiary: night_hag (phantom-role, gated on LOW SANITY not low sleep) ----------------

    /** The night hag feeds on the frayed: approaching it drains sanity. */
    public static final ModConfigSpec.BooleanValue ENABLE_SANITY_NIGHT_HAG =
            BUILDER.comment("Enable the 'night_hag' sanity source (a swooping hag drains sanity).")
                    .define("enableSanityNightHag", true);

    public static final ModConfigSpec.DoubleValue SANITY_NIGHT_HAG_RATE =
            BUILDER.comment("Sanity change per second per nearby night hag (negative drains).")
                    .defineInRange("sanityNightHagRate", -0.20, -10.0, 0.0);

    public static final ModConfigSpec.IntValue SANITY_NIGHT_HAG_RADIUS =
            BUILDER.comment("Radius (blocks) in which night hags contribute to the sanity drain.")
                    .defineInRange("sanityNightHagRadius", 12, 1, 32);

    public static final ModConfigSpec.IntValue SANITY_NIGHT_HAG_MAX =
            BUILDER.comment("Maximum night hags counted toward the sanity drain at once.")
                    .defineInRange("sanityNightHagMax", 2, 1, 16);

    public static final ModConfigSpec.BooleanValue ENABLE_NIGHT_HAG_SPAWNS =
            BUILDER.comment("Allow night hags to spawn above players whose sanity is low (the real gate).")
                    .define("enableNightHagSpawns", true);

    /**
     * The night hag's gate: it only appears when a nearby player's sanity is at or below this
     * fraction of their maximum. This is the design's "swoops at low sanity, not low sleep".
     */
    public static final ModConfigSpec.DoubleValue NIGHT_HAG_SANITY_THRESHOLD =
            BUILDER.comment("Sanity fraction (0..1) at or below which a night hag may spawn near a player.")
                    .defineInRange("nightHagSanityThreshold", 0.5, 0.0, 1.0);

    public static final ModConfigSpec.IntValue NIGHT_HAG_SPAWN_INTERVAL_TICKS =
            BUILDER.comment("Ticks between night-hag spawn passes (minimum 20).")
                    .defineInRange("nightHagSpawnIntervalTicks", 600, 20, 24000);

    public static final ModConfigSpec.IntValue NIGHT_HAG_SPAWN_COUNT =
            BUILDER.comment("Max night hags spawned per player per pass.")
                    .defineInRange("nightHagSpawnCount", 2, 0, 8);

    public static final ModConfigSpec.IntValue NIGHT_HAG_SPAWN_RADIUS =
            BUILDER.comment("Radius (blocks) around each player in which night hags may appear.")
                    .defineInRange("nightHagSpawnRadius", 40, 8, 128);

    public static final ModConfigSpec.IntValue NIGHT_HAG_SPAWN_CAP =
            BUILDER.comment("Max night hags allowed near one player before spawning pauses.")
                    .defineInRange("nightHagSpawnCap", 2, 1, 8);

    public static final ModConfigSpec.IntValue NIGHT_HAG_SPAWN_MIN_Y =
            BUILDER.comment("Minimum Y at which a night hag may spawn (fliers stay above the deep dark).")
                    .defineInRange("nightHagSpawnMinY", 50, -64, 320);

    // --- Cult faction (design/09-cults.md, design/25 cultist family) --------------------------

    /**
     * Reputation below which a cult mob treats a player as hostile. Default {@code 0} is the
     * {@code OUTSIDER} band — a player with zero or positive standing is passed by; a negative one
     * is attacked/refused. Applies to every cult mob via the shared reputation read.
     */
    public static final ModConfigSpec.IntValue CULT_HOSTILE_BELOW =
            BUILDER.comment("Reputation below which a cult mob is hostile to a player (0 = the Outsider band).")
                    .defineInRange("cultHostileBelow", 0, -100, 100);

    /** Whether a cultist's health/attack scale with its owning cult's rank band at spawn. */
    public static final ModConfigSpec.BooleanValue ENABLE_CULT_RANK_SCALING =
            BUILDER.comment("Scale cult_zealot health/attack with its rank band (design/09 rank ladder).")
                    .define("enableCultRankScaling", true);

    /** Per-band multiplier added to health/attack: {@code 1 + rank.ordinal() * this}. */
    public static final ModConfigSpec.DoubleValue CULT_RANK_SCALE_PER_BAND =
            BUILDER.comment("Extra health/attack per rank band above Neutral (0.10 = +10% per band).")
                    .defineInRange("cultRankScalePerBand", 0.10, 0.0, 1.0);

    /** Radius (blocks) a worshipper keeps to its spawn point (guards a location). */
    public static final ModConfigSpec.IntValue WORSHIPPER_HOME_RADIUS =
            BUILDER.comment("Radius (blocks) a worshipper guards around its spawn point.")
                    .defineInRange("worshipperHomeRadius", 16, 1, 64);

    // --- Bestiary: cult_zealot / cult_raider (cult combatants) -------------------------------

    public static final ModConfigSpec.BooleanValue ENABLE_SANITY_CULT_ZEALOT =
            BUILDER.comment("Enable the 'cult_zealot' sanity source (a cult soldier drains sanity).")
                    .define("enableSanityCultZealot", true);

    public static final ModConfigSpec.DoubleValue SANITY_CULT_ZEALOT_RATE =
            BUILDER.comment("Sanity change per second per nearby cult zealot (negative drains).")
                    .defineInRange("sanityCultZealotRate", -0.08, -10.0, 0.0);

    public static final ModConfigSpec.IntValue SANITY_CULT_ZEALOT_RADIUS =
            BUILDER.comment("Radius (blocks) in which cult zealots contribute to the sanity drain.")
                    .defineInRange("sanityCultZealotRadius", 8, 1, 32);

    public static final ModConfigSpec.IntValue SANITY_CULT_ZEALOT_MAX =
            BUILDER.comment("Maximum cult zealots counted toward the sanity drain at once.")
                    .defineInRange("sanityCultZealotMax", 3, 1, 32);

    public static final ModConfigSpec.BooleanValue ENABLE_SANITY_CULT_RAIDER =
            BUILDER.comment("Enable the 'cult_raider' sanity source (a war-band skirmisher drains sanity).")
                    .define("enableSanityCultRaider", true);

    public static final ModConfigSpec.DoubleValue SANITY_CULT_RAIDER_RATE =
            BUILDER.comment("Sanity change per second per nearby cult raider (negative drains).")
                    .defineInRange("sanityCultRaiderRate", -0.07, -10.0, 0.0);

    public static final ModConfigSpec.IntValue SANITY_CULT_RAIDER_RADIUS =
            BUILDER.comment("Radius (blocks) in which cult raiders contribute to the sanity drain.")
                    .defineInRange("sanityCultRaiderRadius", 8, 1, 32);

    public static final ModConfigSpec.IntValue SANITY_CULT_RAIDER_MAX =
            BUILDER.comment("Maximum cult raiders counted toward the sanity drain at once.")
                    .defineInRange("sanityCultRaiderMax", 3, 1, 32);

    // --- Bestiary: rite_binder (raises the dead) ---------------------------------------------

    public static final ModConfigSpec.BooleanValue ENABLE_CORRUPTION_RITE_BINDER =
            BUILDER.comment("Enable the 'rite_binder' corruption vector (nearby binder taints).")
                    .define("enableCorruptionRiteBinder", true);

    public static final ModConfigSpec.DoubleValue CORRUPTION_RITE_BINDER_RATE =
            BUILDER.comment("Corruption per second per nearby rite binder (positive taints).")
                    .defineInRange("corruptionRiteBinderRate", 0.06, 0.0, 10.0);

    public static final ModConfigSpec.IntValue CORRUPTION_RITE_BINDER_RADIUS =
            BUILDER.comment("Radius (blocks) in which rite binders contribute corruption.")
                    .defineInRange("corruptionRiteBinderRadius", 8, 1, 32);

    public static final ModConfigSpec.IntValue CORRUPTION_RITE_BINDER_MAX =
            BUILDER.comment("Maximum rite binders counted toward the corruption vector at once.")
                    .defineInRange("corruptionRiteBinderMax", 2, 1, 16);

    /** Master switch for the binder's raise-the-dead rite (its signature hook). */
    public static final ModConfigSpec.BooleanValue ENABLE_RITE_BINDER_RAISING =
            BUILDER.comment("Allow a rite binder to raise risen husks on a cooldown (bounded).")
                    .define("enableRiteBinderRaising", true);

    public static final ModConfigSpec.IntValue RITE_BINDER_COOLDOWN_TICKS =
            BUILDER.comment("Ticks between a rite binder's raise-the-dead rites (minimum 20).")
                    .defineInRange("riteBinderCooldownTicks", 200, 20, 24000);

    /** Hard cap of husks a single rite may raise, before the standing cap is applied. */
    public static final ModConfigSpec.IntValue RITE_BINDER_RAISE_COUNT =
            BUILDER.comment("Max risen husks raised by one rite (0 disables the rite).")
                    .defineInRange("riteBinderRaiseCount", 2, 0, 4);

    /** No rite raises more than this many husks standing near the binder at once. */
    public static final ModConfigSpec.IntValue RITE_BINDER_RAISE_CAP =
            BUILDER.comment("Max risen husks allowed standing near a binder before it stops raising.")
                    .defineInRange("riteBinderRaiseCap", 4, 0, 16);

    public static final ModConfigSpec.IntValue RITE_BINDER_RAISE_RADIUS =
            BUILDER.comment("Radius (blocks) around a binder in which raised husks appear and are counted.")
                    .defineInRange("riteBinderRaiseRadius", 10, 2, 32);

    // --- Bestiary: plague_crone (hag-alchemist debuffer) -------------------------------------

    public static final ModConfigSpec.BooleanValue ENABLE_CORRUPTION_PLAGUE_CRONE =
            BUILDER.comment("Enable the 'plague_crone' corruption vector (nearby crone taints).")
                    .define("enableCorruptionPlagueCrone", true);

    public static final ModConfigSpec.DoubleValue CORRUPTION_PLAGUE_CRONE_RATE =
            BUILDER.comment("Corruption per second per nearby plague crone (positive taints).")
                    .defineInRange("corruptionPlagueCroneRate", 0.05, 0.0, 10.0);

    public static final ModConfigSpec.IntValue CORRUPTION_PLAGUE_CRONE_RADIUS =
            BUILDER.comment("Radius (blocks) in which plague crones contribute corruption.")
                    .defineInRange("corruptionPlagueCroneRadius", 6, 1, 32);

    public static final ModConfigSpec.IntValue CORRUPTION_PLAGUE_CRONE_MAX =
            BUILDER.comment("Maximum plague crones counted toward the corruption vector at once.")
                    .defineInRange("corruptionPlagueCroneMax", 3, 1, 16);

    /** Master switch for the crone's curse (its signature debuff). */
    public static final ModConfigSpec.BooleanValue ENABLE_PLAGUE_CRONE_CURSE =
            BUILDER.comment("Allow a plague crone to curse a hostile player on a cooldown (bounded).")
                    .define("enablePlagueCroneCurse", true);

    public static final ModConfigSpec.IntValue PLAGUE_CRONE_CURSE_COOLDOWN_TICKS =
            BUILDER.comment("Ticks between a plague crone's curses (minimum 20).")
                    .defineInRange("plagueCroneCurseCooldownTicks", 120, 20, 24000);

    public static final ModConfigSpec.IntValue PLAGUE_CRONE_CURSE_RADIUS =
            BUILDER.comment("Radius (blocks) within which a plague crone may curse a hostile player.")
                    .defineInRange("plagueCroneCurseRadius", 8, 2, 24);

    public static final ModConfigSpec.IntValue PLAGUE_CRONE_CURSE_DURATION_TICKS =
            BUILDER.comment("Duration (ticks) of each curse effect the crone applies (fixed, short).")
                    .defineInRange("plagueCroneCurseDurationTicks", 100, 20, 1200);

    // --- Bestiary: passive (mundane) fauna (design/25 passive role table) ----------------------

    /**
     * The single bounded "turns tainted" conversion used by {@code mire_sow}, {@code hill_hound}
     * and {@code bog_bear}: when the animal stands in a sufficiently tainted loaded chunk it may be
     * replaced in place by one {@code tainted_fauna}. Per-entity cooldown + per-check chance keep a
     * herd from flipping wholesale.
     */
    public static final ModConfigSpec.BooleanValue ENABLE_TAINT_CONVERSION =
            BUILDER.comment("Allow taintable passives (mire_sow/hill_hound/bog_bear) to turn tainted_fauna.")
                    .define("enableTaintConversion", true);

    public static final ModConfigSpec.DoubleValue TAINT_CONVERSION_CHANCE =
            BUILDER.comment("Chance per eligible check that a taintable passive converts (0..1).")
                    .defineInRange("taintConversionChance", 0.08, 0.0, 1.0);

    public static final ModConfigSpec.IntValue TAINT_CONVERSION_COOLDOWN_TICKS =
            BUILDER.comment("Ticks before the same animal may be checked for taint conversion again.")
                    .defineInRange("taintConversionCooldownTicks", 600, 20, 24000);

    /** Shared ambient pass for the passive roster. */
    public static final ModConfigSpec.IntValue PASSIVE_SPAWN_INTERVAL_TICKS =
            BUILDER.comment("Ticks between mundane-fauna spawn passes (minimum 20).")
                    .defineInRange("passiveSpawnIntervalTicks", 300, 20, 24000);

    public static final ModConfigSpec.IntValue PASSIVE_SPAWN_RADIUS =
            BUILDER.comment("Radius (blocks) around each player in which passive fauna may spawn.")
                    .defineInRange("passiveSpawnRadius", 40, 8, 96);

    public static final ModConfigSpec.IntValue PASSIVE_SPAWN_COUNT =
            BUILDER.comment("Max passive fauna spawned per player per pass.")
                    .defineInRange("passiveSpawnCount", 1, 0, 8);

    public static final ModConfigSpec.BooleanValue ENABLE_DEER_SPAWNS =
            BUILDER.comment("Allow deer to spawn on clean ground near players.").define("enableDeerSpawns", true);
    public static final ModConfigSpec.IntValue DEER_SPAWN_CAP =
            BUILDER.comment("Max deer allowed near one player before spawning pauses.")
                    .defineInRange("deerSpawnCap", 6, 1, 32);

    public static final ModConfigSpec.BooleanValue ENABLE_WOOL_HARE_SPAWNS =
            BUILDER.comment("Allow wool hares to spawn on clean ground near players.").define("enableWoolHareSpawns", true);
    public static final ModConfigSpec.IntValue WOOL_HARE_SPAWN_CAP =
            BUILDER.comment("Max wool hares allowed near one player before spawning pauses.")
                    .defineInRange("woolHareSpawnCap", 6, 1, 32);

    public static final ModConfigSpec.BooleanValue ENABLE_MIRE_SOW_SPAWNS =
            BUILDER.comment("Allow mire sows to spawn on clean ground near players.").define("enableMireSowSpawns", true);
    public static final ModConfigSpec.IntValue MIRE_SOW_SPAWN_CAP =
            BUILDER.comment("Max mire sows allowed near one player before spawning pauses.")
                    .defineInRange("mireSowSpawnCap", 4, 1, 32);

    public static final ModConfigSpec.BooleanValue ENABLE_ASH_FOWL_SPAWNS =
            BUILDER.comment("Allow ash fowl to spawn on clean ground near players.").define("enableAshFowlSpawns", true);
    public static final ModConfigSpec.IntValue ASH_FOWL_SPAWN_CAP =
            BUILDER.comment("Max ash fowl allowed near one player before spawning pauses.")
                    .defineInRange("ashFowlSpawnCap", 6, 1, 32);

    public static final ModConfigSpec.BooleanValue ENABLE_BURROWLING_SPAWNS =
            BUILDER.comment("Allow burrowlings to spawn on clean ground near players.").define("enableBurrowlingSpawns", true);
    public static final ModConfigSpec.IntValue BURROWLING_SPAWN_CAP =
            BUILDER.comment("Max burrowlings allowed near one player before spawning pauses.")
                    .defineInRange("burrowlingSpawnCap", 5, 1, 32);

    public static final ModConfigSpec.BooleanValue ENABLE_PACK_BEAST_SPAWNS =
            BUILDER.comment("Allow pack beasts to spawn on clean ground near players.").define("enablePackBeastSpawns", true);
    public static final ModConfigSpec.IntValue PACK_BEAST_SPAWN_CAP =
            BUILDER.comment("Max pack beasts allowed near one player before spawning pauses.")
                    .defineInRange("packBeastSpawnCap", 3, 1, 32);

    public static final ModConfigSpec.BooleanValue ENABLE_GREY_FOX_SPAWNS =
            BUILDER.comment("Allow grey foxes to spawn on clean ground near players.").define("enableGreyFoxSpawns", true);
    public static final ModConfigSpec.IntValue GREY_FOX_SPAWN_CAP =
            BUILDER.comment("Max grey foxes allowed near one player before spawning pauses.")
                    .defineInRange("greyFoxSpawnCap", 3, 1, 32);

    public static final ModConfigSpec.BooleanValue ENABLE_HILL_HOUND_SPAWNS =
            BUILDER.comment("Allow hill hounds to spawn on clean ground near players.").define("enableHillHoundSpawns", true);
    public static final ModConfigSpec.IntValue HILL_HOUND_SPAWN_CAP =
            BUILDER.comment("Max hill hounds allowed near one player before spawning pauses.")
                    .defineInRange("hillHoundSpawnCap", 4, 1, 32);

    public static final ModConfigSpec.BooleanValue ENABLE_HEARTH_CAT_SPAWNS =
            BUILDER.comment("Allow hearth cats to spawn on clean ground near players.").define("enableHearthCatSpawns", true);
    public static final ModConfigSpec.IntValue HEARTH_CAT_SPAWN_CAP =
            BUILDER.comment("Max hearth cats allowed near one player before spawning pauses.")
                    .defineInRange("hearthCatSpawnCap", 4, 1, 32);

    public static final ModConfigSpec.BooleanValue ENABLE_WOOL_BEAST_SPAWNS =
            BUILDER.comment("Allow wool beasts to spawn on clean ground near players.").define("enableWoolBeastSpawns", true);
    public static final ModConfigSpec.IntValue WOOL_BEAST_SPAWN_CAP =
            BUILDER.comment("Max wool beasts allowed near one player before spawning pauses.")
                    .defineInRange("woolBeastSpawnCap", 3, 1, 32);

    public static final ModConfigSpec.BooleanValue ENABLE_BOG_BEAR_SPAWNS =
            BUILDER.comment("Allow bog bears to spawn on clean ground near water (rare).").define("enableBogBearSpawns", true);
    public static final ModConfigSpec.IntValue BOG_BEAR_SPAWN_CAP =
            BUILDER.comment("Max bog bears allowed near one player before spawning pauses.")
                    .defineInRange("bogBearSpawnCap", 2, 1, 16);

    public static final ModConfigSpec.BooleanValue ENABLE_TIDE_GRAZER_SPAWNS =
            BUILDER.comment("Allow tide grazers to spawn on clean ground near water (coastal).").define("enableTideGrazerSpawns", true);
    public static final ModConfigSpec.IntValue TIDE_GRAZER_SPAWN_CAP =
            BUILDER.comment("Max tide grazers allowed near one player before spawning pauses.")
                    .defineInRange("tideGrazerSpawnCap", 4, 1, 32);

    public static final ModConfigSpec.BooleanValue ENABLE_SPORE_BEE_SPAWNS =
            BUILDER.comment("Allow tainted spore bees to spawn in tainted loaded terrain.").define("enableSporeBeeSpawns", true);
    public static final ModConfigSpec.IntValue SPORE_BEE_SPAWN_CAP =
            BUILDER.comment("Max spore bees allowed near one player before spawning pauses.")
                    .defineInRange("sporeBeeSpawnCap", 6, 1, 32);

    /**
     * Stone sentinel ambient spawns are OFF by default: it is an Order-built vault guard, populated
     * by sites/commands later; the egg works now.
     */
    public static final ModConfigSpec.BooleanValue ENABLE_STONE_SENTINEL_SPAWNS =
            BUILDER.comment("Allow ambient stone-sentinel spawns (default OFF; vaults and eggs work).")
                    .define("enableStoneSentinelSpawns", false);
    public static final ModConfigSpec.IntValue STONE_SENTINEL_SPAWN_CAP =
            BUILDER.comment("Max stone sentinels allowed near one player before spawning pauses.")
                    .defineInRange("stoneSentinelSpawnCap", 1, 1, 8);

    /** The spore bee's sting carries a small, bounded corruption add to a struck player. */
    public static final ModConfigSpec.BooleanValue ENABLE_CORRUPTION_SPORE_BEE_STING =
            BUILDER.comment("Enable the spore bee sting's corruption add (server-side, per hit).")
                    .define("enableCorruptionSporeBeeSting", true);
    public static final ModConfigSpec.DoubleValue CORRUPTION_SPORE_BEE_STING =
            BUILDER.comment("Corruption added to a player struck by a spore bee (positive taints).")
                    .defineInRange("corruptionSporeBeeSting", 0.5, 0.0, 10.0);

    /** Ash fowl warning hush: its silence-before-a-presence is a very small sanity drain. */
    public static final ModConfigSpec.BooleanValue ENABLE_SANITY_ASH_FOWL =
            BUILDER.comment("Enable the 'ash_fowl' sanity source (its hush before a presence unsettles).")
                    .define("enableSanityAshFowl", true);
    public static final ModConfigSpec.DoubleValue SANITY_ASH_FOWL_RATE =
            BUILDER.comment("Sanity change per second per nearby ash fowl (negative drains).")
                    .defineInRange("sanityAshFowlRate", -0.02, -10.0, 0.0);
    public static final ModConfigSpec.IntValue SANITY_ASH_FOWL_RADIUS =
            BUILDER.comment("Radius (blocks) in which ash fowl contribute to the sanity drain.")
                    .defineInRange("sanityAshFowlRadius", 6, 1, 32);
    public static final ModConfigSpec.IntValue SANITY_ASH_FOWL_MAX =
            BUILDER.comment("Maximum ash fowl counted toward the sanity drain at once.")
                    .defineInRange("sanityAshFowlMax", 2, 1, 32);

    /** Hearth cat: unsettled by the wrong, not the dark. */
    public static final ModConfigSpec.BooleanValue ENABLE_SANITY_HEARTH_CAT =
            BUILDER.comment("Enable the 'hearth_cat' sanity source (the cat's unease unsettles).")
                    .define("enableSanityHearthCat", true);
    public static final ModConfigSpec.DoubleValue SANITY_HEARTH_CAT_RATE =
            BUILDER.comment("Sanity change per second per nearby hearth cat (negative drains).")
                    .defineInRange("sanityHearthCatRate", -0.03, -10.0, 0.0);
    public static final ModConfigSpec.IntValue SANITY_HEARTH_CAT_RADIUS =
            BUILDER.comment("Radius (blocks) in which hearth cats contribute to the sanity drain.")
                    .defineInRange("sanityHearthCatRadius", 6, 1, 32);
    public static final ModConfigSpec.IntValue SANITY_HEARTH_CAT_MAX =
            BUILDER.comment("Maximum hearth cats counted toward the sanity drain at once.")
                    .defineInRange("sanityHearthCatMax", 2, 1, 32);

    /** Stone sentinel warding deterrent. */
    public static final ModConfigSpec.BooleanValue ENABLE_STONE_SENTINEL_WARD =
            BUILDER.comment("Allow the stone sentinel to ward nearby lesser spawns (bounded).")
                    .define("enableStoneSentinelWard", true);
    public static final ModConfigSpec.IntValue STONE_SENTINEL_WARD_RADIUS =
            BUILDER.comment("Radius (blocks) within which the sentinel wards lesser spawns.")
                    .defineInRange("stoneSentinelWardRadius", 12, 2, 32);
    public static final ModConfigSpec.IntValue STONE_SENTINEL_WARD_MAX =
            BUILDER.comment("Max lesser mobs warded per pass (keeps a horde bounded).")
                    .defineInRange("stoneSentinelWardMax", 4, 0, 16);
    public static final ModConfigSpec.DoubleValue STONE_SENTINEL_WARD_PUSH =
            BUILDER.comment("Outward nudge strength the sentinel applies to a warded mob.")
                    .defineInRange("stoneSentinelWardPush", 0.25, 0.0, 2.0);

    // --- Ambient (ambience) fauna (design/25 ambient role table) ----------------------------
    // Six non-combat drifters carried by the single AmbientSpawner pass. The interval/radius/count
    // below mirror the mundane pass, so both reuse BestiarySupport without a second scan loop.

    public static final ModConfigSpec.IntValue AMBIENT_SPAWN_INTERVAL_TICKS =
            BUILDER.comment("Ticks between ambient-drifer spawn passes (minimum 20).")
                    .defineInRange("ambientSpawnIntervalTicks", 600, 20, 24000);

    public static final ModConfigSpec.IntValue AMBIENT_SPAWN_RADIUS =
            BUILDER.comment("Radius (blocks) around each player in which ambient drifters may spawn.")
                    .defineInRange("ambientSpawnRadius", 40, 8, 96);

    public static final ModConfigSpec.IntValue AMBIENT_SPAWN_COUNT =
            BUILDER.comment("Max ambient drifters spawned per player per pass.")
                    .defineInRange("ambientSpawnCount", 1, 0, 8);

    public static final ModConfigSpec.BooleanValue ENABLE_CAVE_DRIFTER_SPAWNS =
            BUILDER.comment("Allow cave drifters to spawn in dark loaded terrain (the bat role).")
                    .define("enableCaveDrifterSpawns", true);
    public static final ModConfigSpec.IntValue CAVE_DRIFTER_SPAWN_CAP =
            BUILDER.comment("Max cave drifters allowed near one player before spawning pauses.")
                    .defineInRange("caveDrifterSpawnCap", 4, 1, 32);

    public static final ModConfigSpec.BooleanValue ENABLE_PALE_DRIFTER_SPAWNS =
            BUILDER.comment("Allow pale drifters to spawn in still water (the squid role).")
                    .define("enablePaleDrifterSpawns", true);
    public static final ModConfigSpec.IntValue PALE_DRIFTER_SPAWN_CAP =
            BUILDER.comment("Max pale drifters allowed near one player before spawning pauses.")
                    .defineInRange("paleDrifterSpawnCap", 4, 1, 32);

    public static final ModConfigSpec.BooleanValue ENABLE_LANTERN_JELLY_SPAWNS =
            BUILDER.comment("Allow lantern jellies to spawn in still water (the glow squid role).")
                    .define("enableLanternJellySpawns", true);
    public static final ModConfigSpec.IntValue LANTERN_JELLY_SPAWN_CAP =
            BUILDER.comment("Max lantern jellies allowed near one player before spawning pauses.")
                    .defineInRange("lanternJellySpawnCap", 4, 1, 32);

    public static final ModConfigSpec.BooleanValue ENABLE_MARSH_MOTE_SPAWNS =
            BUILDER.comment("Allow marsh motes to spawn on wet loaded ground (the marsh drift-life role).")
                    .define("enableMarshMoteSpawns", true);
    public static final ModConfigSpec.IntValue MARSH_MOTE_SPAWN_CAP =
            BUILDER.comment("Max marsh motes allowed near one player before spawning pauses.")
                    .defineInRange("marshMoteSpawnCap", 5, 1, 32);

    public static final ModConfigSpec.BooleanValue ENABLE_DROWNED_MINNOW_SPAWNS =
            BUILDER.comment("Allow drowned minnows to spawn in still water (the fish-shoal role).")
                    .define("enableDrownedMinnowSpawns", true);
    public static final ModConfigSpec.IntValue DROWNED_MINNOW_SPAWN_CAP =
            BUILDER.comment("Max drowned minnows allowed near one player before spawning pauses.")
                    .defineInRange("drownedMinnowSpawnCap", 6, 1, 32);

    public static final ModConfigSpec.BooleanValue ENABLE_FROST_WISP_SPAWNS =
            BUILDER.comment("Allow frost wisps to spawn in cold loaded terrain (the polar-mote role).")
                    .define("enableFrostWispSpawns", true);
    public static final ModConfigSpec.IntValue FROST_WISP_SPAWN_CAP =
            BUILDER.comment("Max frost wisps allowed near one player before spawning pauses.")
                    .defineInRange("frostWispSpawnCap", 4, 1, 32);

    /** Lantern jelly: a faint light in the deep whose false comfort unsettles. */
    public static final ModConfigSpec.BooleanValue ENABLE_SANITY_LANTERN_JELLY =
            BUILDER.comment("Enable the 'lantern_jelly' sanity source (its false comfort unsettles).")
                    .define("enableSanityLanternJelly", true);
    public static final ModConfigSpec.DoubleValue SANITY_LANTERN_JELLY_RATE =
            BUILDER.comment("Sanity change per second per nearby lantern jelly (negative drains).")
                    .defineInRange("sanityLanternJellyRate", -0.02, -10.0, 0.0);
    public static final ModConfigSpec.IntValue SANITY_LANTERN_JELLY_RADIUS =
            BUILDER.comment("Radius (blocks) in which lantern jellies contribute to the sanity drain.")
                    .defineInRange("sanityLanternJellyRadius", 7, 1, 32);
    public static final ModConfigSpec.IntValue SANITY_LANTERN_JELLY_MAX =
            BUILDER.comment("Maximum lantern jellies counted toward the sanity drain at once.")
                    .defineInRange("sanityLanternJellyMax", 2, 1, 32);

    // --- Bestiary polish (step 3): goals / cadence ----------------------------------------------
    // Every key below defaults to ON, so the polish ships active; turn one off to fall back to the
    // previous standing behaviour for that single feature.

    /** Watcher: approach only while unobserved, hold/withdraw while the player looks at it. */
    public static final ModConfigSpec.BooleanValue WATCHER_STALK_ENABLED =
            BUILDER.comment("Watcher stalks (approaches unseen, hides when watched); off = old follow-at-range.")
                    .define("watcherStalkEnabled", true);

    /** Veil stalker: a short leap at the target in addition to its wall climb. */
    public static final ModConfigSpec.BooleanValue VEIL_STALKER_LEAP_ENABLED =
            BUILDER.comment("Allow the veil stalker to leap at its target (ambush) in addition to climbing.")
                    .define("enableVeilStalkerLeap", true);

    /** Gregarious mobs (lesser swarm, rift mite): one that spots a player alerts nearby kin. */
    public static final ModConfigSpec.BooleanValue BESTIARY_PACK_ALERT_ENABLED =
            BUILDER.comment("Gregarious lesser mobs alert nearby kin when one of them spots a player.")
                    .define("enableBestiaryPackAlert", true);

    /** Squishy mobs (ash fowl, ambient drifters) flee once wounded below a health fraction. */
    public static final ModConfigSpec.BooleanValue BESTIARY_RETREAT_ENABLED =
            BUILDER.comment("Squishy mobs flee when hurt and below a health fraction rather than stand.")
                    .define("enableBestiaryRetreat", true);

    // --- Site populations (design/25 "site-based" spawns) --------------------------------------
    // ONE shared, bounded pass (entity/SitePopulationSpawner) keeps the five fixed second-echelon
    // sites inhabited while a player is near them. It reuses the shared BestiarySupport primitives
    // (loaded chunks only, deterministic RandomSource, per-player caps) and confines every spawn to
    // the site's built footprint. Numbers below are the site tables; ritual_altar_site deliberately
    // has no ambient population (the rite engine owns what appears there).

    /** Master switch for the single site-population pass. Off = no site populations at all. */
    public static final ModConfigSpec.BooleanValue ENABLE_SITE_POPULATIONS =
            BUILDER.comment("Keep the fixed sites (stronghold/temple/vault/rift) inhabited near players.")
                    .define("enableSitePopulations", true);

    public static final ModConfigSpec.IntValue SITE_POPULATION_INTERVAL_TICKS =
            BUILDER.comment("Ticks between shared site-population passes (minimum 20).")
                    .defineInRange("sitePopulationIntervalTicks", 100, 20, 24000);

    public static final ModConfigSpec.IntValue SITE_POPULATION_SPAWN_RADIUS =
            BUILDER.comment("Radius (blocks) around a player in which site inhabitants may spawn.")
                    .defineInRange("sitePopulationSpawnRadius", 32, 8, 96);

    // cult_stronghold: cultists (zealot/raider, a few) + worshippers guarding the altar.
    public static final ModConfigSpec.IntValue SITE_CULT_ZEALOT_COUNT =
            BUILDER.comment("Max cult zealots spawned per site pass (cult_stronghold).")
                    .defineInRange("siteCultZealotCount", 1, 0, 8);
    public static final ModConfigSpec.IntValue SITE_CULT_ZEALOT_CAP =
            BUILDER.comment("Max cult zealots near one player at a cult_stronghold before pausing.")
                    .defineInRange("siteCultZealotCap", 2, 1, 32);
    public static final ModConfigSpec.IntValue SITE_CULT_RAIDER_COUNT =
            BUILDER.comment("Max cult raiders spawned per site pass (cult_stronghold).")
                    .defineInRange("siteCultRaiderCount", 2, 0, 8);
    public static final ModConfigSpec.IntValue SITE_CULT_RAIDER_CAP =
            BUILDER.comment("Max cult raiders near one player at a cult_stronghold before pausing.")
                    .defineInRange("siteCultRaiderCap", 3, 1, 32);
    public static final ModConfigSpec.IntValue SITE_WORSHIPPER_COUNT =
            BUILDER.comment("Max worshippers spawned per site pass (cult_stronghold).")
                    .defineInRange("siteWorshipperCount", 1, 0, 8);
    public static final ModConfigSpec.IntValue SITE_WORSHIPPER_CAP =
            BUILDER.comment("Max worshippers near one player at a cult_stronghold before pausing.")
                    .defineInRange("siteWorshipperCap", 2, 1, 16);

    // drowned_temple: tide-thralls (the Cthulhu boss trigger is separate and untouched).
    public static final ModConfigSpec.IntValue SITE_DROWNED_THRALL_COUNT =
            BUILDER.comment("Max drowned thralls spawned per site pass (drowned_temple).")
                    .defineInRange("siteDrownedThrallCount", 2, 0, 8);
    public static final ModConfigSpec.IntValue SITE_DROWNED_THRALL_CAP =
            BUILDER.comment("Max drowned thralls near one player at a drowned_temple before pausing.")
                    .defineInRange("siteDrownedThrallCap", 4, 1, 32);

    // order_vault: stone sentinels (the Order guards; their ambient pass stays off).
    public static final ModConfigSpec.IntValue SITE_STONE_SENTINEL_COUNT =
            BUILDER.comment("Max stone sentinels spawned per site pass (order_vault).")
                    .defineInRange("siteStoneSentinelCount", 1, 0, 8);
    public static final ModConfigSpec.IntValue SITE_STONE_SENTINEL_CAP =
            BUILDER.comment("Max stone sentinels near one player at an order_vault before pausing.")
                    .defineInRange("siteStoneSentinelCap", 2, 1, 16);

    // rift_scar: rift vermin + swarm; a star-spawn only on the long inner cadence.
    public static final ModConfigSpec.IntValue SITE_LESSER_SWARM_COUNT =
            BUILDER.comment("Max lesser swarms spawned per site pass (rift_scar).")
                    .defineInRange("siteLesserSwarmCount", 3, 0, 16);
    public static final ModConfigSpec.IntValue SITE_LESSER_SWARM_CAP =
            BUILDER.comment("Max lesser swarms near one player at a rift_scar before pausing.")
                    .defineInRange("siteLesserSwarmCap", 6, 1, 32);
    public static final ModConfigSpec.IntValue SITE_RIFT_MITE_COUNT =
            BUILDER.comment("Max rift mites spawned per site pass (rift_scar; they arrive in a group).")
                    .defineInRange("siteRiftMiteCount", 4, 0, 24);
    public static final ModConfigSpec.IntValue SITE_RIFT_MITE_CAP =
            BUILDER.comment("Max rift mites near one player at a rift_scar before pausing.")
                    .defineInRange("siteRiftMiteCap", 8, 1, 64);
    public static final ModConfigSpec.IntValue SITE_STAR_SPAWN_COUNT =
            BUILDER.comment("Max star-spawns spawned per rift-scar star pass.")
                    .defineInRange("siteStarSpawnCount", 1, 0, 4);
    public static final ModConfigSpec.IntValue SITE_STAR_SPAWN_CAP =
            BUILDER.comment("Max star-spawns near one player at a rift_scar before pausing.")
                    .defineInRange("siteStarSpawnCap", 1, 1, 8);
    public static final ModConfigSpec.IntValue SITE_STAR_SPAWN_INTERVAL_TICKS =
            BUILDER.comment("Ticks between the rift_scar's occasional star-spawn attempts (minimum 20).")
                    .defineInRange("siteStarSpawnIntervalTicks", 2400, 20, 72000);

    // --- Events (design/19-events.md) -------------------------------------------------------
    // Ambient/world events and the single shared EventTicker. Data-driven: each event carries a
    // trigger, weight, duration and cooldown; the ticker applies global/per-player caps and
    // cooldowns so events never stack into noise. Safe defaults: events are on, but every event
    // is individually toggleable and can also be driven by hand with "/eh event <id>".

    /** Master switch for the events framework. Off = no auto events and no effect ticking. */
    public static final ModConfigSpec.BooleanValue ENABLE_EVENTS =
            BUILDER.comment("Enable the ambient/world events framework.")
                    .define("enableEvents", true);

    /** Whether the manual {@code /eh event} / {@code /eh events} test commands are registered. */
    public static final ModConfigSpec.BooleanValue ENABLE_EVENT_COMMANDS =
            BUILDER.comment("Enable the manual /eh event and /eh events test commands.")
                    .define("enableEventCommands", true);

    /** Trigger evaluation cadence, in ticks (200 = every 10s). */
    public static final ModConfigSpec.IntValue EVENT_INTERVAL_TICKS =
            BUILDER.comment("Ticks between event trigger evaluations (200 = 10s).")
                    .defineInRange("eventIntervalTicks", 200, 20, 24000);

    /** Global cap on events running at once across all players. */
    public static final ModConfigSpec.IntValue EVENT_MAX_ACTIVE =
            BUILDER.comment("Maximum events running at once across the server.")
                    .defineInRange("eventMaxActive", 8, 1, 64);

    /** Per-player cap on events running at once. */
    public static final ModConfigSpec.IntValue EVENT_MAX_PER_PLAYER =
            BUILDER.comment("Maximum events running at once for one player.")
                    .defineInRange("eventMaxPerPlayer", 2, 1, 16);

    /** Chunk radius of the bounded nearby-rift scan (only when a rift trigger is enabled). */
    public static final ModConfigSpec.IntValue EVENT_RIFT_SCAN_CHUNK_RADIUS =
            BUILDER.comment("Chunk radius searched for rift anchors during trigger evaluation.")
                    .defineInRange("eventRiftScanChunkRadius", 2, 1, 6);

    /** Cap on rift anchors collected per player per scan. */
    public static final ModConfigSpec.IntValue EVENT_RIFT_SCAN_MAX =
            BUILDER.comment("Maximum rift anchors collected per player during trigger evaluation.")
                    .defineInRange("eventRiftScanMax", 8, 1, 64);

    /** whisper: low sanity anywhere. */
    public static final ModConfigSpec.BooleanValue ENABLE_EVENT_WHISPER =
            BUILDER.comment("Enable the 'whisper' event.")
                    .define("enableEventWhisper", true);
    public static final ModConfigSpec.DoubleValue EVENT_WHISPER_SANITY_THRESHOLD =
            BUILDER.comment("whisper: sanity below which it may trigger.")
                    .defineInRange("eventWhisperSanityThreshold", 40.0, 0.0, 100.0);
    public static final ModConfigSpec.IntValue EVENT_WHISPER_DURATION =
            BUILDER.comment("whisper: duration in ticks (400 = 20s).")
                    .defineInRange("eventWhisperDuration", 400, 20, 24000);
    public static final ModConfigSpec.IntValue EVENT_WHISPER_COOLDOWN =
            BUILDER.comment("whisper: cooldown in ticks after it ends (6000 = 5m).")
                    .defineInRange("eventWhisperCooldown", 6000, 0, 240000);

    /** darkness_pulse: night + a nearby open rift. */
    public static final ModConfigSpec.BooleanValue ENABLE_EVENT_DARKNESS_PULSE =
            BUILDER.comment("Enable the 'darkness_pulse' event.")
                    .define("enableEventDarknessPulse", true);
    public static final ModConfigSpec.IntValue EVENT_DARKNESS_DURATION =
            BUILDER.comment("darkness_pulse: duration in ticks (400 = 20s).")
                    .defineInRange("eventDarknessDuration", 400, 20, 24000);
    public static final ModConfigSpec.IntValue EVENT_DARKNESS_COOLDOWN =
            BUILDER.comment("darkness_pulse: cooldown in ticks after it ends (6000 = 5m).")
                    .defineInRange("eventDarknessCooldown", 6000, 0, 240000);
    public static final ModConfigSpec.DoubleValue EVENT_DARKNESS_SANITY_RATE =
            BUILDER.comment("darkness_pulse: extra sanity per second for its duration (negative drains).")
                    .defineInRange("eventDarknessSanityRate", -0.2, -10.0, 0.0);

    /** rift_bloom: an open rift ages (high local taint). */
    public static final ModConfigSpec.BooleanValue ENABLE_EVENT_RIFT_BLOOM =
            BUILDER.comment("Enable the 'rift_bloom' event.")
                    .define("enableEventRiftBloom", true);
    public static final ModConfigSpec.IntValue EVENT_RIFT_BLOOM_DURATION =
            BUILDER.comment("rift_bloom: duration in ticks while the rift stays open (2400 = 2m).")
                    .defineInRange("eventRiftBloomDuration", 2400, 20, 24000);
    public static final ModConfigSpec.IntValue EVENT_RIFT_BLOOM_COOLDOWN =
            BUILDER.comment("rift_bloom: cooldown in ticks after it ends.")
                    .defineInRange("eventRiftBloomCooldown", 2400, 0, 240000);
    public static final ModConfigSpec.DoubleValue EVENT_RIFT_BLOOM_TAINT_THRESHOLD =
            BUILDER.comment("rift_bloom: local chunk taint at or above which the rift counts as aging.")
                    .defineInRange("eventRiftBloomTaintThreshold", 0.5, 0.0, 1.0);
    public static final ModConfigSpec.DoubleValue EVENT_RIFT_BLOOM_TAINT_RATE =
            BUILDER.comment("rift_bloom: extra taint per second added to the loaded patch around the rift.")
                    .defineInRange("eventRiftBloomTaintRate", 0.01, 0.0, 1.0);

    /** veil_thin: storm plus many rifts (or high local taint). */
    public static final ModConfigSpec.BooleanValue ENABLE_EVENT_VEIL_THIN =
            BUILDER.comment("Enable the 'veil_thin' event.")
                    .define("enableEventVeilThin", true);
    public static final ModConfigSpec.IntValue EVENT_VEIL_THIN_DURATION =
            BUILDER.comment("veil_thin: duration in ticks (2400 = 2m).")
                    .defineInRange("eventVeilThinDuration", 2400, 20, 24000);
    public static final ModConfigSpec.IntValue EVENT_VEIL_THIN_COOLDOWN =
            BUILDER.comment("veil_thin: cooldown in ticks after it ends (6000 = 5m).")
                    .defineInRange("eventVeilThinCooldown", 6000, 0, 240000);
    public static final ModConfigSpec.IntValue EVENT_VEIL_THIN_MIN_RIFTS =
            BUILDER.comment("veil_thin: nearby rift anchors needed (with a storm) to trigger.")
                    .defineInRange("eventVeilThinMinRifts", 2, 1, 16);
    public static final ModConfigSpec.DoubleValue EVENT_VEIL_THIN_TAINT_THRESHOLD =
            BUILDER.comment("veil_thin: local taint that substitutes for the rift count.")
                    .defineInRange("eventVeilThinTaintThreshold", 0.6, 0.0, 1.0);
    public static final ModConfigSpec.IntValue EVENT_VEIL_THIN_SPAWN_RADIUS =
            BUILDER.comment("veil_thin: radius (blocks) of the surge around a player.")
                    .defineInRange("eventVeilThinSpawnRadius", 32, 8, 64);
    public static final ModConfigSpec.IntValue EVENT_VEIL_THIN_SURGE_CAP =
            BUILDER.comment("veil_thin: cap of each surged hostile near one player.")
                    .defineInRange("eventVeilThinSurgeCap", 8, 1, 32);
    public static final ModConfigSpec.IntValue EVENT_VEIL_THIN_SURGE_PER_PASS =
            BUILDER.comment("veil_thin: hostiles of each type added per surge pass.")
                    .defineInRange("eventVeilThinSurgePerPass", 2, 1, 8);
    public static final ModConfigSpec.IntValue EVENT_VEIL_THIN_SPAWN_INTERVAL =
            BUILDER.comment("veil_thin: ticks between surge passes (60 = 3s).")
                    .defineInRange("eventVeilThinSpawnInterval", 60, 20, 600);

    /** hallucination_wave: sanity below the threshold; fake cues only, no real mobs. */
    public static final ModConfigSpec.BooleanValue ENABLE_EVENT_HALLUCINATION_WAVE =
            BUILDER.comment("Enable the 'hallucination_wave' event.")
                    .define("enableEventHallucinationWave", true);
    public static final ModConfigSpec.IntValue EVENT_HALLUCINATION_DURATION =
            BUILDER.comment("hallucination_wave: duration in ticks (600 = 30s).")
                    .defineInRange("eventHallucinationDuration", 600, 20, 24000);
    public static final ModConfigSpec.IntValue EVENT_HALLUCINATION_COOLDOWN =
            BUILDER.comment("hallucination_wave: cooldown in ticks after it ends.")
                    .defineInRange("eventHallucinationCooldown", 4800, 0, 240000);
    public static final ModConfigSpec.DoubleValue EVENT_HALLUCINATION_SANITY_THRESHOLD =
            BUILDER.comment("hallucination_wave: sanity below which it may trigger.")
                    .defineInRange("eventHallucinationSanityThreshold", 20.0, 0.0, 100.0);

    /** cleansing_dawn: after a close_rift; corruption recedes and sanity recovers. */
    public static final ModConfigSpec.BooleanValue ENABLE_EVENT_CLEANSING_DAWN =
            BUILDER.comment("Enable the 'cleansing_dawn' event.")
                    .define("enableEventCleansingDawn", true);
    public static final ModConfigSpec.IntValue EVENT_CLEANSING_DAWN_DURATION =
            BUILDER.comment("cleansing_dawn: duration in ticks (2400 = 2m).")
                    .defineInRange("eventCleansingDawnDuration", 2400, 20, 24000);
    public static final ModConfigSpec.IntValue EVENT_CLEANSING_DAWN_COOLDOWN =
            BUILDER.comment("cleansing_dawn: cooldown in ticks after it ends.")
                    .defineInRange("eventCleansingDawnCooldown", 0, 0, 240000);
    public static final ModConfigSpec.DoubleValue EVENT_CLEANSING_SANITY_RATE =
            BUILDER.comment("cleansing_dawn: sanity per second recovered for its duration.")
                    .defineInRange("eventCleansingSanityRate", 0.1, 0.0, 10.0);
    public static final ModConfigSpec.DoubleValue EVENT_CLEANSING_CORRUPTION_RATE =
            BUILDER.comment("cleansing_dawn: player corruption per second removed (negative).")
                    .defineInRange("eventCleansingCorruptionRate", -0.05, -10.0, 0.0);
    public static final ModConfigSpec.DoubleValue EVENT_CLEANSING_TAINT_RATE =
            BUILDER.comment("cleansing_dawn: taint per second removed from the loaded local patch (negative).")
                    .defineInRange("eventCleansingTaintRate", -0.01, -1.0, 0.0);

    // --- World events, batch 2 (design/19): the four remaining bespoke events ------------------
    // cult_procession / blood_moon_rite / star_fall / hollow_call.

    /** Radius (blocks) around the fixed cult_stronghold that counts as "at the cult site". */
    public static final ModConfigSpec.IntValue EVENT_CULT_SITE_RADIUS =
            BUILDER.comment("Radius (blocks) around the cult_stronghold centre for site events.")
                    .defineInRange("eventCultSiteRadius", 160, 16, 512);

    /** cult_procession: a scheduled march and chant at the cult stronghold. */
    public static final ModConfigSpec.BooleanValue ENABLE_EVENT_CULT_PROCESSION =
            BUILDER.comment("Enable the 'cult_procession' event.")
                    .define("enableEventCultProcession", true);
    public static final ModConfigSpec.IntValue EVENT_CULT_PROCESSION_DURATION =
            BUILDER.comment("cult_procession: duration in ticks (default one in-game day).")
                    .defineInRange("eventCultProcessionDuration", 24000, 200, 72000);
    public static final ModConfigSpec.IntValue EVENT_CULT_PROCESSION_COOLDOWN =
            BUILDER.comment("cult_procession: cooldown in ticks after it ends.")
                    .defineInRange("eventCultProcessionCooldown", 24000, 0, 240000);
    public static final ModConfigSpec.IntValue EVENT_CULT_PROCESSION_SPAWN_INTERVAL =
            BUILDER.comment("cult_procession: ticks between procession top-up passes.")
                    .defineInRange("eventCultProcessionSpawnInterval", 200, 20, 24000);
    public static final ModConfigSpec.IntValue EVENT_CULT_PROCESSION_SPAWN_RADIUS =
            BUILDER.comment("cult_procession: radius (blocks) in which the procession may gather.")
                    .defineInRange("eventCultProcessionSpawnRadius", 48, 8, 160);
    public static final ModConfigSpec.IntValue EVENT_CULT_PROCESSION_SPAWN_CAP =
            BUILDER.comment("cult_procession: max zealots present near one player.")
                    .defineInRange("eventCultProcessionSpawnCap", 3, 1, 16);
    public static final ModConfigSpec.IntValue EVENT_CULT_PROCESSION_WORSHIPPER_CAP =
            BUILDER.comment("cult_procession: max worshippers present near one player.")
                    .defineInRange("eventCultProcessionWorshipperCap", 4, 1, 16);
    public static final ModConfigSpec.DoubleValue EVENT_CULT_PROCESSION_SANITY_RATE =
            BUILDER.comment("cult_procession: sanity per second drained by the chant (negative).")
                    .defineInRange("eventCultProcessionSanityRate", -0.05, -10.0, 0.0);
    public static final ModConfigSpec.IntValue EVENT_CULT_PROCESSION_REP =
            BUILDER.comment("cult_procession: hollow_choir reputation granted at the event's start.")
                    .defineInRange("eventCultProcessionRep", 3, 0, 50);

    /** blood_moon_rite: a full-moon rite at high corruption; rifts may open. */
    public static final ModConfigSpec.BooleanValue ENABLE_EVENT_BLOOD_MOON_RITE =
            BUILDER.comment("Enable the 'blood_moon_rite' event.")
                    .define("enableEventBloodMoonRite", true);
    public static final ModConfigSpec.IntValue EVENT_BLOOD_MOON_DURATION =
            BUILDER.comment("blood_moon_rite: duration in ticks (one night).")
                    .defineInRange("eventBloodMoonDuration", 12000, 200, 72000);
    public static final ModConfigSpec.IntValue EVENT_BLOOD_MOON_COOLDOWN =
            BUILDER.comment("blood_moon_rite: cooldown in ticks after it ends.")
                    .defineInRange("eventBloodMoonCooldown", 24000, 0, 240000);
    public static final ModConfigSpec.DoubleValue EVENT_BLOOD_MOON_TAINT_RATE =
            BUILDER.comment("blood_moon_rite: taint added per pass to the loaded local patch.")
                    .defineInRange("eventBloodMoonTaintRate", 0.02, 0.0, 1.0);
    public static final ModConfigSpec.IntValue EVENT_BLOOD_MOON_SPAWN_INTERVAL =
            BUILDER.comment("blood_moon_rite: ticks between cult top-up passes.")
                    .defineInRange("eventBloodMoonSpawnInterval", 300, 20, 24000);
    public static final ModConfigSpec.IntValue EVENT_BLOOD_MOON_CULT_CAP =
            BUILDER.comment("blood_moon_rite: max zealots present near one player.")
                    .defineInRange("eventBloodMoonCultCap", 3, 1, 16);
    public static final ModConfigSpec.IntValue EVENT_BLOOD_MOON_HAG_CAP =
            BUILDER.comment("blood_moon_rite: max night hags present near one player.")
                    .defineInRange("eventBloodMoonHagCap", 2, 1, 16);
    public static final ModConfigSpec.DoubleValue EVENT_BLOOD_MOON_SANITY_RATE =
            BUILDER.comment("blood_moon_rite: sanity per second drained (negative).")
                    .defineInRange("eventBloodMoonSanityRate", -0.08, -10.0, 0.0);

    /** star_fall: a meteorite of star_reagent; the only event that edits blocks (a bounded crater). */
    public static final ModConfigSpec.BooleanValue ENABLE_EVENT_STAR_FALL =
            BUILDER.comment("Enable the 'star_fall' event.")
                    .define("enableEventStarFall", true);
    public static final ModConfigSpec.IntValue EVENT_STAR_FALL_DURATION =
            BUILDER.comment("star_fall: duration in ticks (a brief after-shock).")
                    .defineInRange("eventStarFallDuration", 120, 20, 2400);
    public static final ModConfigSpec.IntValue EVENT_STAR_FALL_COOLDOWN =
            BUILDER.comment("star_fall: cooldown in ticks after it ends.")
                    .defineInRange("eventStarFallCooldown", 6000, 0, 240000);
    public static final ModConfigSpec.IntValue EVENT_STAR_FALL_RADIUS =
            BUILDER.comment("star_fall: radius (blocks) around the player searched for the impact site.")
                    .defineInRange("eventStarFallRadius", 40, 8, 128);
    public static final ModConfigSpec.IntValue EVENT_STAR_FALL_CRATER_RADIUS =
            BUILDER.comment("star_fall: crater radius (blocks) of the impact.")
                    .defineInRange("eventStarFallCraterRadius", 4, 1, 12);
    public static final ModConfigSpec.IntValue EVENT_STAR_FALL_CRATER_BLOCKS =
            BUILDER.comment("star_fall: hard cap on blocks changed by the crater.")
                    .defineInRange("eventStarFallCraterBlocks", 96, 8, 512);
    public static final ModConfigSpec.DoubleValue EVENT_STAR_FALL_SANITY_RATE =
            BUILDER.comment("star_fall: sanity per second drained during the after-shock (negative).")
                    .defineInRange("eventStarFallSanityRate", -0.15, -10.0, 0.0);

    /** hollow_call: the Claimed-corruption "call"; a forced-night horror. */
    public static final ModConfigSpec.BooleanValue ENABLE_EVENT_HOLLOW_CALL =
            BUILDER.comment("Enable the 'hollow_call' event.")
                    .define("enableEventHollowCall", true);
    public static final ModConfigSpec.IntValue EVENT_HOLLOW_CALL_DURATION =
            BUILDER.comment("hollow_call: duration in ticks (one night).")
                    .defineInRange("eventHollowCallDuration", 12000, 200, 72000);
    public static final ModConfigSpec.IntValue EVENT_HOLLOW_CALL_COOLDOWN =
            BUILDER.comment("hollow_call: cooldown in ticks after it ends.")
                    .defineInRange("eventHollowCallCooldown", 36000, 0, 240000);
    public static final ModConfigSpec.DoubleValue EVENT_HOLLOW_CALL_SANITY_RATE =
            BUILDER.comment("hollow_call: sanity per second drained by the call (negative).")
                    .defineInRange("eventHollowCallSanityRate", -0.20, -10.0, 0.0);
    public static final ModConfigSpec.IntValue EVENT_HOLLOW_CALL_SPAWN_INTERVAL =
            BUILDER.comment("hollow_call: ticks between spawn top-up passes.")
                    .defineInRange("eventHollowCallSpawnInterval", 400, 20, 24000);
    public static final ModConfigSpec.IntValue EVENT_HOLLOW_CALL_HAG_CAP =
            BUILDER.comment("hollow_call: max night hags present near one player.")
                    .defineInRange("eventHollowCallHagCap", 2, 1, 16);
    public static final ModConfigSpec.IntValue EVENT_HOLLOW_CALL_SPITE_CAP =
            BUILDER.comment("hollow_call: max choir spites present near one player.")
                    .defineInRange("eventHollowCallSpiteCap", 3, 1, 16);

    // --- Codex / world knowledge (design/22-map-and-knowledge.md) ---------------------------
    // The server-authoritative discovery layer. Knowledge is the progression: proximity reveals
    // sites/cities, seeing or killing a mob reveals its bestiary entry, seeing an Ancient One
    // reveals its boss entry, and a world event starting nearby reveals it.

    /** Master switch for the codex discovery ticker and the kill hook. */
    public static final ModConfigSpec.BooleanValue ENABLE_CODEX =
            BUILDER.comment("Enable server-authoritative codex discovery.")
                    .define("enableCodex", true);

    /** Whether the manual {@code /eh codex} / {@code /eh codex learn} commands are registered. */
    public static final ModConfigSpec.BooleanValue ENABLE_CODEX_COMMANDS =
            BUILDER.comment("Enable the /eh codex and /eh codex learn test commands.")
                    .define("enableCodexCommands", true);

    /** Discovery cadence, in ticks (20 = once a second), matching the sanity ticker. */
    public static final ModConfigSpec.IntValue CODEX_INTERVAL_TICKS =
            BUILDER.comment("Ticks between codex proximity discovery passes (20 = 1s).")
                    .defineInRange("codexIntervalTicks", 20, 5, 1200);

    /** Radius (blocks) at which a fixed site/city is discovered. */
    public static final ModConfigSpec.IntValue CODEX_SITE_RADIUS =
            BUILDER.comment("Blocks from a fixed site/city centre at which it is discovered.")
                    .defineInRange("codexSiteRadius", 96, 8, 512);

    /** Radius (blocks) at which an Ancient One is discovered. */
    public static final ModConfigSpec.IntValue CODEX_BOSS_RADIUS =
            BUILDER.comment("Blocks from an Ancient One at which its boss entry is discovered.")
                    .defineInRange("codexBossRadius", 48, 8, 256);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private ModConfig() {
    }
}
