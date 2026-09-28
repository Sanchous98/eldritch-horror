# 21 — Settlements

Civilian hubs: the *normal* world the horror is taking away. Unlike cult structures,
settlements are places to trade, rest, learn rumours and find quests — and, as corruption
rises, places that **fall**.

**Placement is real.** Each settlement sits at the **real Earth coordinates** of a real
place (baked from Natural Earth into `settlements.json`): London is where London is, Cairo
where Cairo is. The Earth map means the world is legible and the stakes are geographic.

## Tiers

Only a **curated set of 24 real cities** exists as featured settlements — one or two per
region of the world. Each is a real destination the game builds content for. All other real
places in the baked data (≈3,000) are **not settlements yet**; whether they become generic
towns, ruins, or nothing is an open question, and is left for later.

| Tier | Count | What it is | Services |
|---|---|---|---|
| **City** (curated) | **24** | A featured hub at a real city's coordinates | Trade, inn, healer, quests, rumours, guards |
| Town | — | *deferred*: generic minor settlements, if we add them | — |

The 24 curated cities:

| Region | Cities |
|---|---|
| Europe | London, Paris, Rome, Istanbul, Moscow |
| Africa & Middle East | Cairo, Lagos, Nairobi, Cape Town |
| South & East Asia | Delhi, Mumbai, Shanghai, Beijing, Tokyo, Seoul, Bangkok, Jakarta |
| Oceania | Sydney |
| Americas | New York, Los Angeles, Mexico City, Rio de Janeiro, Buenos Aires, Lima |

Only cities within the playable map are included; the roster is the **source of truth** in
`tools/bake_earth.py` (`CURATED_CITIES`) and is baked into `settlements.json`.

## Real data

| Field | Source |
|---|---|
| Name, latitude, longitude, population | Natural Earth `ne_10m_populated_places` |
| World `x`, `z` | `x = lon × 45.51`, `z = −lat × 45.51` (2 blocks/pixel, 16,384 × 8,192) |
| Tier | population rank |

Baked by `tools/bake_earth.py --layers places` → `settlements.json`.

> **The map is a compressed Earth.** At 2 blocks/pixel a real city spans a few blocks, so a
> "city" is an *abstraction* — a compact hub of buildings, not a 1:1 reproduction. That is
> the same playability-over-fidelity call as the rest of the map.

## Services

| Service | City | Town | Notes |
|---|---|---|---|
| **Trade** | ✔ | ✔ | Reagents, artefacts, tomes (reputation-gated with cults) |
| **Inn / rest** | ✔ | ✔ | Sleep → sanity recovery (reduced at high corruption) |
| **Healer** | ✔ | — | Partial sanity restoration; not corruption |
| **Quest board / rumours** | ✔ | ✔ | Points at rites, structures, rifts, other settlements |
| **Guards** | ✔ | — | Keep monsters out (until they don't) |

Settlements have no innate power over corruption — they are **victims** of it, which is the
point. The player protects them or doesn't.

## Corruption response (and why cities matter)

A settlement's state tracks the local corruption:

| Corruption | State | Effect |
|---|---|---|
| Low | **Thriving** | Full services; NPCs calm |
| Rising | **Uneasy** | Rumours of the horror; some NPCs leave; prices rise |
| High | **Besieged** | Monsters press in; services limited; quests to defend |
| Claimed | **Fallen** | Abandoned/corrupted; marker changes on the map; a cult may move in |

A **Fallen** city is a loss — a hub gone, a quest line cut, a marker on the world map turned
black. This is the horror made *geographic*, and it gives the session its stakes.

## Relation to factions

- Cults **recruit** in settlements (a preacher, a cellar meeting) — the first contact.
- The **Order** keeps vaults and agents here; the **Choir** works the coastal towns.
- Settlements are **neutral ground** until the player's reputation makes them otherwise.
- A city that **Falls** can be **taken over by the Hollow Choir** — trading one hub for a
  cult stronghold.

## Anatomy of a city (it is more than a coordinate)

`settlements.json` is only the **placement index** ("city X at (x,z)"). A city in-game is
four layers:

| Layer | What | Where |
|---|---|---|
| **Location** | name + real `(x,z)` | `settlements.json` (baked) |
| **Buildings** | the actual blocks: streets, walls, a landmark | **structure templates** (`.nbt`) or code |
| **State** | Thriving → Uneasy → Besieged → Fallen | **server world state**, keyed by settlement id |
| **Services** | trade, inn, healer, quests | entities / interactions in the structure |

### Placement is not vanilla

Vanilla places structures by **biome + random spacing**; it cannot pin a structure to exact
coordinates. Cities must therefore be placed explicitly:

- a **custom `Structure`** whose position is the curated coordinate (with a structure set
  that does not random-spread), or
- a **decorator step** in the Earth generator that stamps each curated city during chunk
  generation.

Either way the Earth generator must first **wire the structure system**
(`createState` / `ChunkGeneratorStructureState`) — it currently does not, so *no*
structures generate yet.

### Scale

At 2 blocks/pixel a real 50 km city is ~25 blocks across, so a city is an **abstracted
district** — a compact cluster with a landmark, walls/gates and service NPCs — not a
reproduction. Terrain (coast, river, desert) picks the flavour: harbour, river, inland,
oasis.

### State storage

Settlement state is **world state** keyed by settlement id, not per-player and not in the
world file's blocks: a `SavedData` map `{settlementId → state}`. Map markers read it; the
structure itself does not need to change unless it Falls (then it is corrupted/abandoned).

## Multiplayer & sessions

- Settlement state is **world state** (shared), not per-player.
- A session's phase changes how settlements behave (more unrest as it advances).
- On a session reset, settlement states can persist (a "scarred world") or reset — open
  question.

## Open questions

- Whether the ~3,000 non-curated real places become **generic towns, ruins, or nothing**.
- Whether a **city can be reclaimed** after it Falls (an expensive rite / quest).
- Whether towns far from a player's travel ever simulate, or only when visited.
