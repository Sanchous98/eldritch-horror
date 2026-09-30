# Status & handoff

Read this together with `AGENTS.md` after any context compaction. It is the short, current

## How to resume after compaction

1. Read `AGENTS.md` (working rules — subagents; **host safety, no OOM**).
2. Read this file.
3. See the continuation plan below (chat).
4. Check the board: <https://github.com/users/Sanchous98/projects/2>.

## Project

Eldritch Horror — an RPG-flavoured horror mod for **Minecraft 26.3 / NeoForge / Java 25**.
Repo: <https://github.com/Sanchous98/eldritch-horror> (branch `main`, all pushed).

## Done (verified)

- **Earth world generator**: real baked layers (landmask, ETOPO elevation, Köppen), real
  terrain + biomes, Overworld replaced. `Earth world generator ready` on server start.
- **World ×8**: extent **131 072 × 65 536** blocks, layers **16 384 × 8 192** (8 blocks/px),
  elevation **8-bit** (`round(m/75)+127`, ~21 MB). `settlements.json` at 364 blocks/°.
  Explicit **world border** (set on `ServerStarted`).
- **Location frame** (`world/loc/`): `StructureBuilder`/`Builder`, `Location`, `Locations`
  registry, shaped ops (pitchedRoof/spire/buttress/window/crenellations/monument/scatter/
  ruins), `Tier`, `Palette`, `Materials` (26.3 dyed blocks via `ColorCollection.pick`).
- **24 cultural city styles** (`world/loc/style/*Style.java`, one file per city) + `StyleKit`
  shared landmarks (cathedral/mosque/dome/minaret/pagoda/torii/steppedTemple/obelisk/statue/
  lantern). Tokyo is the reference (shikkui + kawara + vermilion + neon + cherry blossom).
- **Cities at contract size**: district 380 (~760 across), bigger landmarks, decay pass.
- **City renderer** (`core/CityRenderer`, dev-only `-Deh.renderCities`): PNGs to `run/render/`,
  tick-sliced. Used to review without a client.
- **Design**: `design/23-boundary-and-travel.md` (cylinder + Morok);
  `docs/STRUCTURES-CONTRACT.md`; `AGENTS.md`; `docs/HOST-SAFETY.md`.
- **Safety**: hard caps in `docker-compose.yml`, `scripts/guarded-run.sh`.

## Fixed (notable)

- Palette for a city's style was bypassed in `Locations.place` (grey cities) — fixed.
- World border crashed startup (`ServerAboutToStart` → `ServerStarted`).
- Renderer used the legacy city radius (captured only the centre) — fixed.

## Known limitations / deferred

- Terrain is **surface-only** and **undecorated** (no grass/trees/flowers) — Phase 3.
- Cities have **no services/state** yet (deferred until those features exist).
- **No combat, mobs, bosses, rituals, sanity, corruption** implemented — design only.
- **Landmark duplicates** not yet separated (Paris/Mexico City; Istanbul/Cairo) — Phase 2.
- Only ~4–10 of 24 cities visually reviewed so far — Phase 1.
- Spawn is vanilla-chosen; a pinned coastal spawn is a tracked task.

## Open incident

The host OOM (dockerd killed, all containers stopped) was caused by the dev container having
the host `docker.sock` (rw): spawned containers were **siblings**, unbounded by the container
cgroup. Remedy in progress: no host socket; build inside the container cgroup. See
`docs/HOST-SAFETY.md`. **Do not use the host socket. Run through `scripts/guarded-run.sh`.**

## Where things live

| Concern | File |
|---|---|
| Host safety (no OOM) | `docs/HOST-SAFETY.md` |
| Working rules (subagents) | `AGENTS.md` |
| Design, source of truth | `design/` |
| World generation | `docs/WORLDGEN.md` |
| Locations / parallel authoring | `docs/STRUCTURES-CONTRACT.md` |
| Multiplayer & sessions | `docs/MULTIPLAYER.md` |
| Windows/Docker pitfalls | `docs/WINDOWS.md` |
