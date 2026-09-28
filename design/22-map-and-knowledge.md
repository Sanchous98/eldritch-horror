# 22 — Map & World Knowledge

The player's picture of the world: a **fully visible world map**, its markers, and the
**events that change it**. This is the navigation and information layer of the RPG.

## Principle: the map is known, the world is not safe

The *map* is complete from the start — you are playing on **Earth**, and you know where
Earth is. What you do not know is **what is happening there**. The map is not hidden by fog;
it is consumed by events. Knowledge of the land is free; knowledge of the *horror* is
progression (see [`04-pillars.md`](04-pillars.md)).

This follows from the Earth design: a player who has been shown a map of Earth already has
the geography. Hiding it would be dishonest and annoying, not scary.

## What the map shows

| Layer | Always? | Notes |
|---|---|---|
| Landmass / oceans (the Earth) | ✔ | from the baked landmask |
| Your position & waypoint | ✔ | |
| **Settlements** | ✔ | city/town markers (see [`21-settlements.md`](21-settlements.md)) |
| **Structures** (altars, temples, vaults) | discovered | revealed by discovery, quests, `observe` |
| **Rifts** | discovered | revealed by `observe`/`divination`, or when they open near you |
| **Cult strongholds** | discovered | revealed by reputation rank or quest |
| **Corruption** | optional overlay | a heatmap of the chunk corruption field (client-side) |

The map opens **fullscreen** (keybind); a **minimap** is optional/accessibility.

## Map-change events

The map is live. Events **add, move, or corrupt** markers — and this is where the horror
becomes visible over time.

| id | Name | Trigger | Map effect |
|---|---|---|---|
| `rift_opened` | A rift tears open | `open_rift` rite, or a natural rift | Adds a **rift** marker (pulsing) |
| `rift_closed` | The way is sealed | `close_rift` rite | Removes the rift marker |
| `settlement_uneasy` | Unrest | Local corruption rising | Settlement marker gains an **unrest** state |
| `settlement_fallen` | A settlement falls | Corruption reaches `Claimed` locally | Marker turns **black**; services gone |
| `settlement_reclaimed` | Reclaimed | Cleansing rite / quest | Marker restored |
| `cult_claimed` | The cult moves in | A Fallen settlement + Hollow Choir | Marker becomes a **cult stronghold** |
| `star_fall` | Star-Fall | Random at `Marked`+ | Adds a **meteor/reagent** marker |
| `veil_thin` | The Veil thins | Storm, many rifts | **Corruption overlay** intensifies map-wide |
| `horror_stirs` | The horror stirs | Session phase → Stirring/Breaching | A **dread** tint spreads from rifts |

Events are **data-driven** (see [`19-events.md`](19-events.md)) and carry an optional map
effect. Because marker changes are per-player-visible but world-state-driven, the map is
server-authoritative with a client-side render (see [`../docs/MULTIPLAYER.md`](../docs/MULTIPLAYER.md)).

## Discovery

- **Settlements** are on the map from the start (they are real and known).
- **Structures, rifts and cult sites** are *not*: they appear when discovered — by being
  sighted, by `observe`/`divination`, by a quest, or by an event that names them.
- Discovery is **per player** (knowledge is personal — see the pillars).

## Implementation sketch

- A **client map screen** rendering the baked **landmask** as the base image (already in
  `assets/…/map/landmask_…png`), scaled to the world extent.
- A server-sent **marker set** (`Settlements`, discovered structures, rifts) with per-marker
  state, updated by the map-change events above.
- Optional **corruption overlay** from the per-chunk field when the client is near.
- Markers are **world coordinates**, so they line up with the map image directly (the same
  `x = lon × 45.51` mapping the generator uses).

## Open questions

- Whether the minimap shows discovered markers or only the fullscreen map does.
- How much of the corruption overlay is revealed (all, or only explored areas).
- Whether players can **place custom waypoints** (expected; likely yes).
- Whether the "dread" tint is a client effect or a data layer.
