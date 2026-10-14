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
- **Quests** as datapack content (`data/eldritch_horror/quest/*.json`), a 14-quest chain and a
  journal screen synced server → client.
- **Endgame** (per world, `SavedData`): the Horror awakens, walks through four timed stages,
  and the world **banishes** it (rift sealing, corruption collapse) or **summons** it
  (a corruption floor on the world, the "hollow crown" path) — plus `/eh endgame` for state.

### Added — client UI

- Map, quest **journal** (`J`), **codex** (`C`) and **skill tree** (`K`) screens with their own
  keybinds; HUD readouts and tooltips. Server-authoritative state, client-side rendering only.

### Added — content & tools

- Bestiary sounds, item/reagent catalogue, tomes and class/skill unlocks.
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
