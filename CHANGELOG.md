# Changelog

All notable changes to this project. This file starts at the current development snapshot;
the full history lives in `git log`.

The format is loosely [Keep a Changelog](https://keepachangelog.com/en/1.1.0/); the mod follows
the version in `gradle.properties` (currently `0.1.0`, unreleased).

## [Unreleased] — 0.1.0 "playable alpha"

### Added — world & systems

- Earth-scale Overworld map: continents and oceans from a real heightmap, vanilla biomes
  (see `docs/WORLDGEN.md`), plus the **Threshold** — the mod's own dimension.
- Curated **starting cities** and a one-time prologue that places the player.
- **Sanity** meter with HUD, drains near the eldritch, rest/light recovery, madness effects.
- **Corruption** as durable exposure; a per-chunk **taint field** that converts chunks
  (tainted soil/stone), spreads and can be purified locally.
- **Cult reputation** with ranks, services and offices per faction; the Fallen takeover,
  boundary travel to the Morok edge, settlements and second-echelon **sites** (cult stronghold,
  drowned temple, rift scar, order vault, ritual altar site, …).
- **Vanilla mobs are replaced** by the mod's own bestiary (lesser, ambient, passive families);
  twenty **Ancient Ones** exist as site-spawned, unkillable-by-default presences with
  non-combat **solve** rites.

### Added — rituals, quests, endgame

- The **rite engine**: altar block pattern (a core *ringed* by rune/chalk marks), offerings,
  conditions, cooldowns, and four outcome families (reputation / transform / curse / sanity);
  a failed rite costs its `Cost on failure`, an unfinished one costs nothing; `respec` refunds
  80 %. Fourteen seeded rites, including the endgame fork (`awaken_the_horror`,
  `banish_the_horror`, `summon_the_horror`).
- **Codex**: learning lore (tomes, cult scripture, the prophet, the starting rite) is the same
  as finding a codex entry — so the lorekeeper path is reachable on a fresh world.
- **Quests** as datapack content (`data/eldritch_horror/quest/*.json`), a 19-quest chain and a
  journal screen synced server → client. Prerequisites are enforced server-side, and the design's
  daily/weekly side quests (`star_hunt`, `tide_offering`, `cleansing_contract`) pay again after
  their cooldown.
- **Fallen cities can be reclaimed** (`design/21`): the Order's `reclaim_city` rite clears a
  district's taint and drives out the Hollow Choir, but only where a city has actually Fallen.
- **Rift-gate travel** (`design/26`, `design/23`): anchors link two-way with no UI; a trip costs
  corruption and sanity and can tear a new rift open on arrival.
- **Wards shelter from auras too** (`design/26`): a presence's sanity aura and the husk crowd no
  longer drain a player standing in a haven.
- **Wards are places** (`design/26`, `design/18`): the warding rite raises a persisted haven that
  suppresses sanity drain, keeps freshly spawned lesser horrors out and opens a +25% damage window
  on the Ancient Ones; `/eh ward` reports it.
- **Skill tree** (`design/15`): eight nodes with real effects and an optional prerequisite chain —
  the occultism four (`forbidden_lore`, `rite_affinity`, `divination`, `lucid_dreams`) join the
  existing four, and a locked node answers in chat instead of silently refusing.
- **Endgame** (per world, `SavedData`): the Horror awakens, walks through four timed stages,
  and the world **banishes** it (rift sealing, corruption collapse) or **summons** it
  (a corruption floor on the world, the "hollow crown" path) — plus `/eh endgame` for state.

### Added — client UI

- Map, quest **journal** (`J`), **codex** (`C`) and **skill tree** (`K`) screens with their own
  keybinds; HUD readouts and tooltips. Server-authoritative state, client-side rendering only.

### Added — tests

- **Headless GameTest suite** (`design/13`): the altar ring, ward ledger, gate links and the seal,
  run with `./gradlew runGameTestServer` (26.3's data-driven test instances).
- **JUnit coverage for the pure logic** (weapon, gate, ward, city-reclaim, cooldown, reputation
  decay, quest rules, lucid-dream pool, the elevation curve and biome table) plus a
  **`LangParityTest`** that fails the build on a missing key, a duplicate key or a placeholder
  mismatch between `en_us.json` and `ru_ru.json`.
- **PMD is a build gate**: `pmdMain` and `pmdTest` run from `check`, and a violation fails
  `./gradlew build`.

### Added — the cult's own clock

- **NPC cult rituals**: a cell of cultists gathers at an altar, chants and summons star-spawn on its
  own; breaking the circle — kill the leader, break the altar, stand in the ring — pays Order
  standing and buys the world time.
- **The doom clock** (`design/11`): a soft 0–100 world meter that ends in the Choir's summon after
  ~150 unopposed minutes (faster with open rifts). At 70 % the Horror awakens; banish or seal still
  wins. `/eh doom` reads it.

### Added — scheduled transport

- **Ships on a timetable** (`design/30`): real multi-seat packets sail cached sea lanes between
  harbour cities, depart on the in-game clock, charge a fare, stream only the water around the hull
  and leave empty services at the dock. A full voyage is verified; the hull model is a placeholder
  until the art pass.

### Added — towns

- **Generic towns** (`design/21`): the 3,065 non-curated settlements now generate as small
  deterministic places — plaza and well, houses, lamps, a farm and paths — with a bucketed spatial
  index so worldgen never scans the whole settlement list.
- **Towns are inhabited**: villagers with vanilla trades, a taint-abandon gate, and a small night
  shelter/sanity recovery inside the footprint.

### Tooling & CI

- **All three endings bot-verified live**: banish, summon and the seal each resolved through the
  real rites and recorded their journal outcome.
- **Bots join the level's player list**: world-wide events (endgame quests/broadcasts, doom warnings,
  boss bars) now reach the headless test players, and a full bot run can finish the game (`the_seal`).

- **GitHub Actions**: CI builds + runs JUnit, PMD and the GameTest server on every push/PR;
  tagged `v*` releases publish the jar with a SHA-256 checksum. `docs/CI.md` documents both
  workflows.

### Added — content & tools

- Bestiary sounds, item/reagent catalogue, tomes and class/skill unlocks.
- **Bestiary completed** (`design/25`, `design/18`): the five vanilla replacements with their own
  aura axes, the roaming named `ithaqual_shade` and the drowned-temple Choir **Leviathan** (3
  phases) — each with a loot table and a generated skin.
- **Items wired** (`design/16`): real weapon/tool stats, the reagent row's deltas, carried-artefact
  passives (rite cooldown, taint suppression, the watcher's drain) and five navigation items.
- **Weapons bite** (`design/16`): the recorded on-hit effects are real — target bonuses from data
  tags (`#eldritch`, `#star_spawn`), corruption-scaled dagger damage, the trident's underwater cut,
  the censer's paid ward, the pact blade's kill price and the veil bow's cost, eldritch bonus and
  pierce.
- **Audio wired**: thirteen procedural ogg through `ModSounds`/`sounds.json` — the rites, the
  endgame fork (banish/summon/**seal**), the Horror's own voice (it no longer sounds like a wither),
  whispers and dread.
- **The third ending** (`design/11`, was an open question): a world-level **rift ledger** — when the
  Horror comes for the decision and every rift it knew is already closed, nothing holds it and the
  world stands **sealed** (no cost, no banishment; the journal's `the_seal` completes).
- **Russian locale** (`ru_ru`, 853 keys incl. subtitles), independently reviewed; parity is now
  enforced by `LangParityTest` in the JUnit suite.
- **Armour and the last artefacts** (`design/16`, slice 2): the eight worn pieces are real armour
  (equipment assets, generated worn sheets, per-set stats); the bone charm absorbs one fatal blow;
  the lantern stills the darkness drain; the mask summons more star-spawn, panics villagers and
  lowers the sanity ceiling; the chalice echoes the last rite for corruption; the compass reads the
  safest city; the lens reads the local taint; the maps remember waypoints.
- **Bots can be hurt again** (26.3): `ServerPlayer#isInvulnerableTo` treats a player without a
  loaded client — and one mid-dimension-change — as invulnerable, so headless bots were untouchable
  and every combat probe silently did nothing; the harness now stands in for a player that confirms.
- **Bot harness**: a headless server-side test player under `/eh bot …` (spawn, perceive,
  travel, craft, mine, place altars, perform rites, fight) — used to prove the arc end to end.
- **Procedural asset generator** `tools/gen_assets.py`: logo/pack icon, every entity skin,
  item icons — deterministic, palette in one place; hero art stays the artist's job.
- **Procedural sound generator** `tools/gen_sounds.py`: rites, endgame, the Horror and
  ambience as reproducible `.ogg` (rendered with ffmpeg).

### Fixed

- Payload type ids built with `createType` crashed on startup at resource load: ids must be
  `Identifier.fromNamespaceAndPath(MODID, path)` — recorded as a rule in `AGENTS.md`.
- Flying drifters missed a `FLYING_SPEED` attribute and crashed the server.
- The Horror's stage clock was frozen in `ATTENTION` (NoAI skips `customServerAiStep`);
  the clock now runs in `tick()`.
- Codex/`lorekeeper` could not be reached on a fresh world (learning sources were unreachable).
- Missing spawn egg (`the_horror_spawn_egg`) icon/model — the asset audit now covers all items.

### Docs

- `design/` updated where implementation chose a concrete shape (rites, endgame, altar pattern).
- `docs/STATUS.md` kept as the live handoff; `docs/TODO.md` as the backlog.
- README rewritten for the playable-alpha state; `AGENTS.md` collects build/verification rules.
