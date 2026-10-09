# TODO / backlog

Working list for finishing the mod. Ordered by priority. Each item states the design source, the
concrete gap, and a definition of done. When an item lands, move it to **Done** with its commit.

Status legend: `[ ]` open · `[~]` in progress · `[x]` done.

## Open

### 6. City-state depth (design/21) — larger
- [~] **Gap:** `CityState` only scaled the shelter source.
- (a) **Done:** the map colours city markers by the player's discovered state — `CityStateDiscovery`
  records it to the owner-synced `CITY_STATES` attachment; `WorldMapScreen` reads it. Commit
  `2bdde13`.
- [ ] (b) Fallen-city block conversion / Hollow Choir takeover / defend quests / price changes.

### 7. Skill tree & quest journal (design/13) — larger
- [ ] **Gap:** not started. Sanity/corruption HUD and the codex exist; skill spend and quest tracking
  do not.
- **DoD:** scoped in its own design pass before implementation.

### 8. Altar block-pattern matching (design/05) — optional
- [ ] **Gap:** rites are performed only via `/eh rite`; the altar pattern is decorative.
- **DoD:** decide command-only vs a pattern matcher, then implement or explicitly drop.

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
