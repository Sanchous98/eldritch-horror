# Multiplayer & sessions

**Decision:** the mod supports a **dedicated server with many players** (as many as vanilla
allows) and is designed around **game sessions** — a world is played in timed sessions, and
session state is server-authoritative. Nothing gameplay-relevant runs on the client.

## Server-authoritative

The server owns every piece of game state:

| State | Where it lives | Client's role |
|---|---|---|
| Terrain / map | Server (custom generator) | renders chunks the server sends |
| Sanity / corruption | Server (per-player) | renders the synced value |
| Reputation | Server (per-player) | renders dialogue/HUD |
| Rifts / corruption field | Server (per-chunk/world) | renders the world |
| Session / phase | Server | shows timers/notifications |

The client never generates terrain or decides outcomes. In singleplayer the integrated
server plays the same role. **This is why the map layers are a server-side concern** — see
below.

### Consequences

- Map layers (landmask/elevation/Köppen) are read by the **server** at startup. They are
  packaged as **common mod resources** (in the mod jar), so a dedicated server loads them
  like any other asset — not as client-only assets.
- `EarthMap` must load through a class loader that sees the mod jar (the server thread's),
  **not** `Minecraft.getInstance()`. No client classes are referenced anywhere in
  `world/`.
- Every gameplay system gets a **server half** and at most a thin **client half** (HUD,
  overlay, particles). See `docs/ARCHITECTURE.md` for the side-safety rule.

## Sessions

A **session** is a bounded run of a world: players join, play, and the session advances
through phases. It gives a multiplayer server a *shape* — a reason to gather — which also
suits the eldritch "the world ends unless you act" fantasy.

### Session phases

| Phase | Meaning | Player pressure |
|---|---|---|
| **Gathering** | Normal play; corruption low; rifts rare | Explore, learn rites, build standing |
| **Stirring** | Corruption above a threshold; ambient events rise | Prepare; stock reagents; choose a side |
| **Breaching** | Rifts open; the horror's presence is felt | Perform rites; defend; decide the path |
| **Resolution** | The endgame fork resolves (banish or summon) | The session's outcome |
| **Intermission / reset** | World is archived or reset for the next session | Persist progress; reset world state |

Phases are **advanced by world corruption total + rites performed + time**, configurable.

### Session state

- Stored per world (server save / `SavedData`), not per client.
- Drives: which ambient events are eligible, difficulty scaling, endgame availability.
- Survives restarts; can be inspected/administered via `/eldritch session`.

## Scaling & performance (many players)

- Map sampling is **per column**, cached per chunk; the layers are loaded once and shared.
- The corruption field ticks **near players only** (throttled), never globally.
- Events have **frequency caps** so a busy server isn't spammed.
- Keep the generator **stateless** so chunk generation parallelises like vanilla.

## Config (server / SERVER type)

Multiplayer balance lives in **server config** (authoritative, not client-editable):

- Session phase thresholds and timers.
- Sanity/corruption difficulty scaling by player count (opt-in).
- Event frequency caps.
- Rift limits / concurrent rift cap.

## Open questions

- Whether session phases are wall-clock based (real time) or in-game-time based.
- What is preserved across a session reset (reputation? codex? a persistent "account"?).
- Whether the world is one long-lived map with sessions layered on, or regenerated per
  session.
