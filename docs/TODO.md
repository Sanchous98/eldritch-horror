# TODO / backlog

Working list for finishing the mod. Ordered by priority. Each item states the design source, the
concrete gap, and a definition of done. When an item lands, move it to **Done** with its commit.

Status legend: `[ ]` open · `[~]` in progress · `[x]` done.

## Open

### 1. Rite conditions (design/08, design/05)
- [x] **Gap:** `RiteEngine` charged the offerings but never checked the rite's **Conditions** column.
- **Done:** `rite/RiteConditions` (per-rite time/moon/weather/near-water/corruption-stage/rift,
  loaded-chunks only), checked before the outcome; unmet → refuse + message, meter cost charged,
  offerings untouched; `REQUIRE_RITE_CONDITIONS` gates it; the altar shows conditions. Commit
  `d8632f6`.

### 2. Prologue reset command
- [x] **Gap:** only a first-time player entered the Threshold; there was no way to reset
  `PROLOGUE_DONE` for testing.
- **Done:** `/eh prologue reset` (gamemaster) clears `PROLOGUE_DONE` and teleports back to the
  Threshold; documented in `docs/TESTING.md`. Commit `d8632f6`.

### 3. Site chests + loot tables (design/16, design/21)
- [x] **Gap:** no container loot existed anywhere; Aldous only affected mob drops.
- **Done:** `StructureBuilder.chest` + `Builder` place loot-block chests deterministically; `SiteLoot`
  wires a chest or two into all nine fixed sites from four JSON tables (`common`/`order`/`cult`/
  `relic`). Commit `TBD`. (Antiquarian extension to chest loot still open — see below.)

### 4. Map markers: rifts and cult strongholds (design/22)
- [ ] **Gap:** the map shows cities/home/player/discovered sites, but not known rifts or cult
  strongholds.
- **DoD:** discovered rifts (via `observe`/proximity) and known cult strongholds render as distinct
  markers; unknown ones stay hidden.

### 5. Map keybind (design/22)
- [ ] **Gap:** the map opens only by right-clicking `world_atlas`.
- **DoD:** a rebindable key (default `M`) opens the map when the atlas is held (optional) or always;
  registered via `RegisterKeyMappingsEvent` + a client tick handler.

### 6. City-state depth (design/21) — larger
- [ ] **Gap:** `CityState` only scales the shelter source. Not done: fallen-city block conversion,
  map marker change, Hollow Choir takeover, defend quests, price changes.
- **DoD:** at least (a) a map marker reflecting the state and (b) a bounded visual change when a city
  Falls; takeover/quests scoped separately.

### 7. Skill tree & quest journal (design/13) — larger
- [ ] **Gap:** not started. Sanity/corruption HUD and the codex exist; skill spend and quest tracking
  do not.
- **DoD:** scoped in its own design pass before implementation.

### 8. Altar block-pattern matching (design/05) — optional
- [ ] **Gap:** rites are performed only via `/eh rite`; the altar pattern is decorative.
- **DoD:** decide command-only vs a pattern matcher, then implement or explicitly drop.

## Done

- [x] **World map + event notifications** — `770c919` (fixed in `202240b`).
- [x] **Rite offerings consumed; per-investigator signature items; live passives (city recovery,
  mob loot); city state derived from taint; dead API nits removed** — `d3ce502`.
- [x] **Street-level city arrival; Rio halo fix; deps bump** — `08cf25a`.
- [x] **Code-review fixes** — `202240b`.
- [x] **`docs/TESTING.md`** — `abc399d`.
