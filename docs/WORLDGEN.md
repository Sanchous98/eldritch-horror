# World generation

How the Overworld map is made. **Decision:** Earth-like continents and oceans from a
**procedural, fixed seed** — no authored image — on a **16,384 × 16,384** playable map,
with **vanilla biomes** placed by the generator. It is deterministic: the same world seed
always yields the same map.

> This is the map slice. It is intentionally the first buildable, testable piece.

## Goals

- **Earth-like landmasses**: a few continents, real oceans, coastlines — not vanilla's
  scattered noise islands.
- **Vanilla biomes** reused wholesale: cold north/south, temperate middle, hot equator.
- **Deterministic & fixed**: iterate on the generator, get the same map.
- **Playable, not a simulation**: compressed climate, bounded oceans, a world border.

## Non-goals

- Custom biomes (vanilla set is enough — see `design/20-map.md`).
- 1:1 Earth or realistic geography.
- Changing the Nether or End (Overworld only).
- Pre-generating chunks on disk up front (the map is *deterministic*, so it is
  effectively pre-generated; we only decide the *layout* ahead of play).

## Architecture

Two custom, deterministic pieces replace vanilla worldgen; everything else (carvers,
features, structures, mobs) stays vanilla and runs on top.

```
world seed ──► ContinentField ──► LandMask + Elevation      (per x,z)
                    │
                    ├──► ContinentChunkGenerator   → terrain height per column
                    └──► ContinentBiomeSource      → vanilla biome per column
```

Both consume the **same** `ContinentField` so terrain and biome agree.

### `ContinentChunkGenerator` (`Registries.CHUNK_GENERATOR`)

- Samples `ContinentField` for each `(x,z)`: `land` (0–1) and `elevation`.
- **Sea level** at Y=63. Below → ocean floor; above → land.
- **Height spline**: lowlands near the coast, hills inland, mountains where elevation is
  high; continental shelf so oceans are shallow near coasts and deep offshore.
- Emits a `NoiseChunk`/`ChunkAccess` surface; delegates caves/carvers to vanilla.

### `ContinentBiomeSource` (`Registries.BIOME_SOURCE`)

Maps **latitude + elevation + moisture** to vanilla biomes:

| Condition | Biomes |
|---|---|
| Below sea level, shallow | `river`/`beach` at coast, `ocean` → `deep_ocean` by depth |
| High elevation | `windswept_hills`, `frozen_peaks`, `stony_peaks` |
| Hot (equator) | `desert`, `savanna`, `jungle`, `badlands` by moisture |
| Temperate | `plains`, `forest`, `birch_forest`, `swamp` by moisture |
| Cold (poles) | `taiga`, `snowy_taiga`, `snowy_plains`, `ice_spikes` |

## The continent function (fixed seed)

A small, fast, deterministic stack:

1. **fBm noise** — several octaves of value/simplex noise for the base landform.
2. **Domain warp** — offset the sample coordinate by another noise field so coastlines are
   irregular, not blobby.
3. **Radial falloff** — fade land → deep ocean toward the map edge, so the world is bounded
   by sea rather than a hard cliff.
4. **Threshold & remap** — turn the field into a `land` value; a narrow band around the
   threshold is the coastline (beaches).

All of it is a pure function of `(seed, x, z)`, so it is reproducible and testable without
Minecraft.

## Playability rules

- **World border** at ±8192 (`/worldborder set 16384 center 0 0`), matching the extent.
- **Bounded oceans**: cap the falloff so no ocean exceeds a crossable width.
- **Compressed climate**: latitude bands are squeezed so several biomes occur within a few
  thousand blocks, not one per continent.
- **Sea level** Y=63 keeps vanilla carvers/features valid.
- **Vanilla Nether/End** untouched.

## Performance

- Sample `ContinentField` **per column**, not per block, and cache per chunk.
- Precompute a small **gradient/spline** for height; no expensive noise in the inner loop.
- The generator must be **stateless and side-safe** (server runs it; clients never do).

## API surface to build

| Piece | Registry | Notes |
|---|---|---|
| `ContinentChunkGenerator` + codec | `Registries.CHUNK_GENERATOR` | custom generator type |
| `ContinentBiomeSource` + codec | `Registries.BIOME_SOURCE` | custom biome source type |
| `ContinentField` | — | pure logic; unit-testable |
| Noise settings / dimension JSON | `data/…/worldgen/noise_settings`, `dimension` | points the Overworld at our generator |

> 26.3 uses `Identifier` (the `ResourceLocation` rename) and `Codec`-based registration;
> verify signatures against the decompiled jar before writing.

## Verifying (testable-first)

- Spectator flight at high Y to eyeball continents.
- `/locate biome minecraft:ocean` and `minecraft:plains` → large coherent regions.
- F3 biome/height at a spot.
- A debug command `/eldritch map info` → land/sea + elevation for the current column.
- `ContinentField` unit tests (pure function): determinism, ocean at edges, land fraction.

## Implementation order

1. `ContinentField` + unit tests (no Minecraft — fast to iterate).
2. `ContinentBiomeSource` — land/ocean + latitude biomes.
3. `ContinentChunkGenerator` — height from the field.
4. Wire the Overworld dimension/noise settings to it.
5. World border + tuning pass (ocean width, climate compression).
6. Structures/biomes from `design/20-map.md` layered on afterwards.

## Open questions

- Continent count/size targets (how many, how big) — tune against the spectate view.
- Whether rivers are procedural overlay or vanilla `river` biome at drainage lines.
- Whether the fixed seed is the *world* seed or an independent **map seed** (leaning:
  independent, so the map is stable across worlds and only the world seed varies loot).
