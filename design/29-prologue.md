# 29 — Prologue: the Threshold

How a new player enters the world. A **one-time** prologue that mirrors the board game:
pick a named **investigator**, take their starting kit, then step through the gate to the
city you want to begin in.

This is the *only* deliberate onboarding: the main world is deliberately hostile and
unexplained, so the prologue must teach the two core choices (who you are, where you start)
without a tutorial.

## Goals

- **Deterministic start.** The player's entry city is chosen by the player, never by the
  vanilla spawn search. Fixes the "spawn is vanilla-chosen" gap in `docs/WORLDGEN.md`.
- **Investigator choice has weight.** Choosing a named investigator is meaningful and permanent
  (barring a debug reset). It is the first decision a player makes, as in the board game.
- **Tone.** Quiet, cold, *before* the horror. A small observatory/waystation at the edge of
  things, not a bustling hub. The last safe room.

## The Threshold (dimension)

A tiny, finite, self-contained dimension: `eldritch_horror:threshold`.

- **Extent:** a single small island, roughly **96 × 96 blocks** of usable floor, surrounded
  by a hard void/ocean edge and an invisible barrier. No terrain generation — it is
  **authored, not generated**: a `StructureBuilder` layout stamped once at dimension load
  (same builder used by cities/sites), so it is fully procedural and version-controlled.
- **Layout:**
  - a central **observatory** (the safe room) with **12 investigator pedestals** on an inner
    ring around the obelisk;
  - a **ring of 24 gates** around it, one per curated city, each labelled with the city's
    real name;
  - a **lectern/wall** with a short "what is happening" placard (codex entry grant).
- **No weather, no day cycle pressure, no mobs, no sanity/corruption drain here.** All the
  tickers (`SanityTicker`, `CorruptionTicker`, `TaintSystem`, `EventTicker`) already gate on
  `Level.OVERWORLD`; the Threshold is excluded from every one because it is not the Overworld.
- **Placeholder art.** Blocks/materials are drawn from existing palettes; the final look is
  the user's modelling/texturing job.

## Flow

1. **First join.** On first join, a per-player `PROLOGUE_DONE` attachment is absent. The server
   teleports the player into the Threshold at its fixed spawn and shows a short message. Every
   subsequent join does **not** re-enter the prologue.
2. **Choose an investigator.** 12 pedestals, each carrying one investigator (name + role on a
   sign). Interacting is **two-step**: the first right-click shows a preview (occupation,
   passive, starter kit, starting rite); a second right-click on the same pedestal commits.
   Committing:
   - sets the permanent per-player `INVESTIGATOR` attachment (synced, `copyOnDeath`);
   - hands the investigator's **starting kit** (items) and **starting rite knowledge**;
   - applies the investigator's passive where the system exists (e.g. the `max_sanity`
     attribute), and records the rest for when their systems land.
3. **Choose a city.** 24 gates. Walking into one teleports the player to that city's curated,
   safe surface point in the Overworld, sets that city as the player's **home** (the `HOME_CITY`
   attachment) and their **respawn point**, and marks the prologue **complete**.
4. **Done.** The prologue is one-time; there is **no return** to the Threshold. A fresh
   player is placed at a city, standing at the start of `design/02-progression.md`. On death the
   player respawns in their home city; `/eh home` (gamemaster debug) teleports there on demand.

## Investigators

The 12 named investigators (occupation, role, passive, active, kit, rite) are defined in
`14-classes.md`; the prologue is where they are *granted*. The data lives in one place in code
(the `investigator` registry), so the prologue, commands and future investigator-gated content
all read the same source.

Each investigator's **active** ability is invoked through their own **signature item** (e.g.
`seers_lens` for Eleanor, `rosary` for Agatha; the item is bound to its owner and given in their
starting kit) — right-click, with a cooldown. Behaviour is dispatched by item identity, not a shared
id.
(given in every kit); it has a 20-second per-player cooldown.

## Rules & invariants

- **Server-authoritative.** Investigator write, kit grant and city teleport all happen on the
  server; the client only sees synced state. No client-side teleport.
- **Permanent.** The `INVESTIGATOR` attachment cannot be changed by gameplay; `/eh investigator
  set <id>` (gamemaster, debug) is the only reset and re-grants that investigator's kit.
- **One-way.** After a gate, the player cannot return; the Threshold may be unloaded after
  the last player leaves it.
- **Protected.** The Threshold hub cannot be mined or blown up (`ThresholdProtection`).
- **Multiplayer-safe.** Many players can be in the Threshold at once; each picks
  independently.
- **Config-gated.** A `Type.SERVER` boolean to disable the prologue entirely (players then
  spawn normally, investigator chosen by command).

## Commands

| Command | Effect |
|---|---|
| `/eh investigator get` | Prints your current investigator. |
| `/eh investigator set <id>` | Gamemaster debug: sets and re-grants an investigator. |
| `/eh home` | Gamemaster debug: teleport to your starting city (respawn does this normally). |

## What is deliberately *not* here

- No lore dump, no quest, no NPC tutorial. The codex placard is the only exposition.
- No investigator skill trees yet (`15-skills.md`); the prologue only **grants** the
  investigator.
- No return hub, no fast travel (that is a later system, if ever).

## Deferred / open

- Which rite each investigator starts with (depends on the rite roster's tier-1 list).
