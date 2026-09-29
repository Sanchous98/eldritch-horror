# Status & handoff

Read this together with `AGENTS.md` after any context compaction. It is the short, current
snapshot; the durable truth is in `design/` and `docs/`.

## How to resume after compaction

1. Read `AGENTS.md` (working rules — especially: **delegate bulk work to subagents**).
2. Read this file.
3. Read the doc relevant to the task (table at the bottom).
4. Check the board for tasks: <https://github.com/users/Sanchous98/projects/2>.

## Project

Eldritch Horror — an RPG-flavoured horror mod for **Minecraft 26.3 / NeoForge / Java 25**.
Repo: <https://github.com/Sanchous98/eldritch-horror> (branch `main`).
A `@Mod` skeleton plus a working **Earth world generator** with **24 curated real cities**.

## What works (verified)

- **Build**: `./gradlew build` passes on 26.3 (in Docker: `docker compose build dev`).
- **Earth map**: custom `EarthChunkGenerator` + `EarthBiomeSource` sample baked real layers
  (landmask, ETOPO elevation, Köppen climate) and generate real terrain + real biomes.
  Overworld replaced via `data/minecraft/dimension/overworld.json`.
- **Cities**: 24 curated cities at real coordinates, abstracted builds (palette from biome,
  harbour if coastal), footprint scaled to real population. Verified generating.
- **Dev server**: `docker compose --profile server up server`; auto-op on join (`EH_DEV`).
- **Docker**: no host bind mount (source baked into image); named volumes; entrypoint fixes
  volume ownership and CRLF. Works around Docker Desktop on Windows. See `docs/WINDOWS.md`.

## Done recently

- **World scale ×8**: world is now **131 072 × 65 536** blocks, layers re-baked to
  **16 384 × 8192** (= 8 blocks/pixel). Constants (`HALF_WIDTH=65536`, `HALF_HEIGHT=32768`,
  `BLOCKS_PER_PIXEL=8`, `PIXEL_WIDTH=16384`), `settlements.json` (blocks-per-degree 364.09;
  Tokyo x=50881 z=−12993), explicit **world border** (131072² centred on 0,0) and the preview
  are all in place. Elevation is stored **8-bit** (`round(m/75)+127`, ~21 MB) to stay under
  hosting size limits. `compileJava` green; pushed to `main`.
- **Location frame** (`world/loc/`): `Tier`, `Palette`, `StructureBuilder` + `Builder`,
  `Location`, `Locations` registry, and a reference metropolis `loc/city/CityLocation`.
  `EarthChunkGenerator.applyBiomeDecoration` now dispatches via `Locations.place`; the old
  `CityBuilder`/`CityStamper` are gone. See `docs/STRUCTURES-CONTRACT.md`.
- **Gothic atmosphere**: `Palette` expanded to 15 fields (dark-gothic core + Köppen regional
  materials); builder gained shaped ops (`pitchedRoof`, `spire`, `buttress`, `window`,
  `crenellations`, `monument`, `scatter`, `ruins`); `CityLocation` rebuilt with varied
  silhouettes, a cathedral landmark and a decay pass.

## In flight / current

- Next: **parallel location content** — temples, vaults, strongholds, altar sites — each a
  single `Location` file, built by one worker against the frozen contract. Random-in-biome
  sites will need a real `Structure`/`StructureSet` when the first one lands.

## Known limitations / deferred

- Terrain is **surface-only** — no ores, no caves, and **no surface decoration** (no trees/
  grass) yet. Tracked as a story.
- Cities have **no services and no state** yet (trade/inn/quests; Thriving→Fallen) — deferred
  until those features exist.
- **No combat, mobs, bosses, rituals, sanity, corruption** implemented yet — design only.
- Spawn is vanilla-chosen (lands near the Atlantic); a pinned coastal spawn is a tracked task.
- `PlayerList`/server APIs differ subtly in 26.3 — verify against the decompiled sources
  (extract from the NeoForm cache if needed).

## Next steps (suggested order)

1. Parallel location content against the frame (per `docs/STRUCTURES-CONTRACT.md`).
2. Random-in-biome placement (real `Structure`/`StructureSet`) when the first site lands.
3. Parallel location work via **subagents**, one location/area each.
4. A gameplay system (sanity is self-contained and testable) when locations are good enough.

## Where things live

| Concern | File |
|---|---|
| Working rules (subagents!) | `AGENTS.md` |
| Design, the source of truth | `design/` |
| Code architecture | `docs/ARCHITECTURE.md` |
| World generation | `docs/WORLDGEN.md` |
| Locations / parallel authoring | `docs/STRUCTURES-CONTRACT.md` |
| Multiplayer & sessions | `docs/MULTIPLAYER.md` |
| Windows/Docker pitfalls | `docs/WINDOWS.md` |
| Where to start coding | `docs/DEVELOPMENT-PLAN.md` |
