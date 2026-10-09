# TODO / backlog

Working list for finishing the mod. Ordered by priority. Each item states the design source, the
concrete gap, and a definition of done. When an item lands, move it to **Done** with its commit.

Status legend: `[ ]` open · `[~]` in progress · `[x]` done.

## Open

### 6. City-state depth (design/21) — larger
- [x] **Done:** (a) the map colours city markers by the player's discovered state (`CityStateDiscovery`
  + synced `CITY_STATES`); a Fallen city is taken over by a bounded Hollow Choir presence
  (`CityFall`). Commit `PENDING`.

### 7. Skill tree & quest journal (design/13)
- [x] **Done:** `quest/` journal (seed quests advanced by existing systems; synced `QUESTS`;
  `/eh quests`) and `skill/` tree (points from quests; `lucid_mind` +10% max sanity, `warded_soul`
  −10% corruption gain; `/eh skill`, `/eh skill unlock`). No client screen yet. Commit `PENDING`.

### 8. Altar block-pattern matching (design/05)
- [x] **Done:** `rite/RitualAltar` requires an `altar_core` marked with `rune_stone`/`ritual_chalk`
  near the performer (`requireRiteAltar`, radius/marks config); `ritual_altar_site` now places a real
  core + eight rune stones. Commit `PENDING`.

## Done

- [x] **Antiquarian bonus on site chests (design/16, design/14)** — the passive now applies to our
  `chests/*` tables too (global loot modifier) — `b8b773e`.
- [x] **Map markers: rifts (design/22)** — discovered rifts recorded and drawn; cult strongholds
  already showed as discovered sites — `da2882b`.
- [x] **Rite conditions (design/08)** — `d8632f6`.
- [x] **Prologue reset command (`/eh prologue reset`)** — `d8632f6`.
- [x] **Site chests + loot tables (design/16)** — `a6daaad`.
- [x] **Map keybind (design/22)** — `M` opens the map while the atlas is held — `b40cd01`.
- [x] **World map + event notifications** — `770c919` (fixed in `202240b`).
- [x] **Rite offerings consumed; per-investigator signature items; live passives (city recovery,
  mob loot); city state derived from taint; dead API nits removed** — `d3ce502`.
- [x] **Street-level city arrival; Rio halo fix; deps bump** — `08cf25a`.
- [x] **Code-review fixes** — `202240b`.
- [x] **`docs/TESTING.md`** — `abc399d`.
