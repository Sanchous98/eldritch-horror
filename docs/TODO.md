# TODO / backlog

Working list for finishing the mod. Ordered by priority. Each item states the design source, the
concrete gap, and a definition of done. When an item lands, move it to **Done** with its commit.

Status legend: `[ ]` open · `[~]` in progress · `[x]` done.

## Open

### 4. Map markers: rifts and cult strongholds (design/22)
- [x] **Gap:** the map showed cities/home/player/discovered sites, but not rifts.
- **Done:** `KNOWN_RIFTS` attachment + `RiftKnowledge`; `RiftDiscovery` records nearby rifts
  (loaded chunks only) and the one from `open_rift`, forgets sealed ones; synced to the owner so
  `WorldMapScreen` draws them. Cult strongholds are a site id, already shown among discovered sites.
  Commit `da2882b`.

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

### 9. Antiquarian bonus on site chests (design/16, design/14)
- [x] **Gap:** Aldous's loot passive only affected mob drops, not the site chests.
- **Done:** a global loot modifier (`loot/AntiquarianLootModifier`, serializer `LootModifiers`,
  datapack `loot_modifiers/antiquarian.json`) adds one bonus item when Aldous kills a mob or opens
  one of our `chests/*` tables (NeoForge `modifyLoot` sets the queried table id and the opener is
  `THIS_ENTITY`). Replaces the old `LivingDropsEvent` handler. Commit `PENDING`.

## Done

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
