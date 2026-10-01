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

    public static final ModConfigSpec SPEC = BUILDER.build();

    private ModConfig() {
    }
}
