# 29 — Prologue: the Threshold

How a new player enters the world. A **one-time** prologue that mirrors the board game:
pick an investigator (class), take their starting kit, then step through the gate to the
city you want to begin in.

This is the *only* deliberate onboarding: the main world is deliberately hostile and
unexplained, so the prologue must teach the two core choices (who you are, where you start)
without a tutorial.

## Goals

- **Deterministic start.** The player's entry city is chosen by the player, never by the
  vanilla spawn search. Fixes the "spawn is vanilla-chosen" gap in `docs/WORLDGEN.md`.
- **Class choice has weight.** Choosing a class is meaningful and permanent (barring a debug
  reset). It is the first decision a player makes, as in the board game.
- **Tone.** Quiet, cold, *before* the horror. A small observatory/waystation at the edge of
  things, not a bustling hub. The last safe room.

## The Threshold (dimension)

A tiny, finite, self-contained dimension: `eldritch_horror:threshold`.

- **Extent:** a single small island, roughly **96 × 96 blocks** of usable floor, surrounded
  by a hard void/ocean edge and an invisible barrier. No terrain generation — it is
  **authored, not generated**: a `StructureBuilder` layout stamped once at dimension load
  (same builder used by cities/sites), so it is fully procedural and version-controlled.
- **Layout:**
  - a central **observatory** (the safe room) with the three class pedestals;
  - a **ring of 24 gates** around it, one per curated city, each labelled with the city's
    real name;
  - a **lectern/wall** with a short "what is happening" placard (codex entry grant).
- **No weather, no day cycle pressure, no mobs, no sanity/corruption drain here.** All the
  tickers (`SanityTicker`, `CorruptionTicker`, `TaintSystem`, `EventTicker`) already gate on
  `Level.OVERWORLD`; the Threshold must be excluded from *every* one (audit them; a shared
  `Dimensions.isEarth(level)` predicate is the intended seam).
- **Placeholder art.** Blocks/materials are drawn from existing palettes; the final look is
  the user's modelling/texturing job.

## Flow

1. **First join.** On first join, a per-player `STARTED` (or `PROLOGUE`) attachment is
   absent. The server teleports the player into the Threshold at its fixed spawn and shows a
   short subtitle. Every subsequent join does **not** re-enter the prologue.
2. **Choose a class.** Three pedestals (`investigator`, `occultist`, `cultist`). Using one:
   - sets the permanent per-player `CLASS` attachment (synced, `copyOnDeath`);
   - hands the archetype's **starting kit** (items) and **starting rite knowledge**;
   - applies the archetype's bonuses where the system exists (e.g. the `max_sanity`
     attribute), and records the rest for when their systems land;
   - marks the class chosen.
3. **Choose a city.** 24 gates. Entering one teleports the player to that city's curated,
   safe surface point in the Overworld and marks the prologue **complete**.
4. **Done.** The prologue is one-time; there is **no return** to the Threshold. A fresh
   player is placed at a city, standing at the start of `design/02-progression.md`.

## Classes

The archetypes are defined in `14-classes.md`; the prologue is where they are *granted*.

| id | Pedestal | Starter kit | Starting rites |
|---|---|---|---|
| `investigator` | a brass instrument / notebook | field journal, warded charm, a torch stack, bread | one ward rite |
| `occultist` | a lectern of tomes | a tome, a focusing crystal, ink | one divination/learning rite |
| `cultist` | a black altar | a ritual blade, an offering token, cult sigil | one blood/offering rite |

The kit and rite tables live in one data table in code (a `Classes` registry), so the
prologue, commands and future class-gated content all read the same source.

## Rules & invariants

- **Server-authoritative.** Class write, kit grant and city teleport all happen on the
  server; the client only sees synced state. No client-side teleport.
- **Permanent.** `CLASS` cannot be changed by gameplay; `/eh class set <id>` (gamemaster,
  debug) is the only reset and re-grants that class's kit.
- **One-way.** After a gate, the player cannot return; the Threshold may be unloaded after
  the last player leaves it.
- **Multiplayer-safe.** Many players can be in the Threshold at once; each picks
  independently. A player who disconnects mid-prologue resumes in the Threshold (the
  `STARTED` flag is only set when a gate is used).
- **Config-gated.** A `Type.SERVER` boolean to disable the prologue entirely (players then
  spawn normally, class chosen by command).

## Commands

| Command | Effect |
|---|---|
| `/eh class get` | Prints your current class. |
| `/eh class set <id>` | Gamemaster debug: sets and re-grants a class. |

## What is deliberately *not* here

- No lore dump, no quest, no NPC tutorial. The codex placard is the only exposition.
- No class skill trees yet (`15-skills.md`); the prologue only **grants** the archetype.
- No return hub, no fast travel (that is a later system, if ever).

## Deferred / open

- Exact coordinates/size of the Threshold island and gate ring.
- Which rite each class starts with (depends on the rite roster's tier-1 list).
- Whether the starting city is remembered as the player's "home" for later systems
  (respawn, city reputation) — likely yes, but out of scope here.
