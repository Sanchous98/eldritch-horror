# World generation

How the Overworld map is made. **Decision:** the map is **deterministic and fixed**
(not seed-derived) on a **16,384 × 16,384** playable map, with **vanilla biomes** placed by
the generator. Two selectable modes:

- **`PROCEDURAL`** — Earth-*like* continents synthesized from noise (fBm + domain warp +
  radial ocean falloff), fixed seed. No assets. (Original plan.)
- **`EARTH`** — the **actual Earth**, from **baked real geodata** (landmask + elevation +
  climate), sampled deterministically. This is what "an Earth map" requires.

> **Important:** no Minecraft *world seed* can generate Earth. Vanilla's generator is a
> noise function with no notion of external geography, so no seed emits the real coastline
> (confirmed by EarthMC, Mojang's feedback forum, and the Minecraft Wiki). An "Earth map"
> must come from **baked geographic data** (or a prebuilt world), not a seed. In `EARTH`
> mode a fixed constant selects the baked dataset — that is the closest thing to an
> "Earth seed", and it is deterministic.

## Goals

- **Deterministic & fixed**: the same map every run; iterate on it, ship it.
- **Earth-like or actual Earth** landmasses with real oceans and coastlines.
- **Vanilla biomes** reused wholesale: cold poles, temperate middle, hot equator.
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

## The map data

Two ways to obtain the map, one interface (`ContinentField`) so the generator is agnostic.

### Mode `PROCEDURAL` — synthesized

A small, fast, deterministic stack (a pure function of a fixed seed):

1. **fBm noise** — several octaves for the base landform.
2. **Domain warp** — offset the sample coordinate so coastlines are irregular.
3. **Radial falloff** — fade land → deep ocean toward the map edge.
4. **Threshold & remap** — `land` value; a narrow band is the coastline (beaches).

### Mode `EARTH` — baked real geodata

Three layers baked at build time (see below), sampled bilinearly at chunk time:

| Layer | Source (public) | Used for |
|---|---|---|
| **Landmask / coastline** | Natural Earth `land` polygons (public domain) | land vs. ocean, beaches |
| **Elevation + bathymetry** | ETOPO / GEBCO, or NASA Visible Earth (`#73934` land, `#73963` bathy) | height spline, ocean depth |
| **Climate** | Köppen–Geiger (CC-BY), or derived from latitude | biome selection |

We do **not** ship the raw global datasets (huge). A small offline bake step downsamples
them to our extent and resolution and commits compact layers:

- **Preferred format:** a small set of **PNG** layers (e.g. 2048² for a 16k map → 8
  blocks/pixel), committed under `src/main/resources/`, read once and cached.
- **Alternative:** pack into a compact binary (e.g. `short[2048][2048]` for elevation) if
  fidelity matters more than reviewability.
- Equirectangular projection (lat/lon → x/z), centred near the prime meridian; the poles
  map to the map edges (or are trimmed).

This is exactly how the prebuilt Earth maps are made (WorldPainter from these datasets) —
we just sample the same data at runtime instead of baking every chunk.

## The `ContinentField` interface

Both modes implement the same sampler, so `ChunkGenerator`/`BiomeSource` don't care:

```
land(x, z)       -> 0..1   (0 = deep ocean, 1 = high land)
elevation(x, z)  -> metres (signed; below 0 = ocean floor)
climate(x, z)    -> {temperature, moisture}
```

- `PROCEDURAL` computes these from noise.
- `EARTH` looks them up from the baked layers.


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

- **Which mode ships first?** `PROCEDURAL` is far cheaper to build (no assets); `EARTH`
  needs the bake step and real data. (Leaning: build `PROCEDURAL` first, add `EARTH` behind
  the same interface.)
- Continent count/size targets (procedural) or Earth extent/trim (Earth mode).
- Whether rivers are procedural overlay or vanilla `river` at drainage lines.
- Whether the constant that selects the baked dataset is independent of the world seed
  (leaning: independent, so the map is stable across worlds and only loot/mobs vary).
- The `EARTH` bake step: PNG vs. binary, and the offline tool that produces it.

## References

- `design/20-map.md` — content that sits on the map.
- Public data: Natural Earth, NASA Visible Earth (elevation/bathymetry), ETOPO/GEBCO,
  Köppen–Geiger. Prior art: WorldPainter scripts (MattiBorchers), Terra 1-to-1, the
  Continents mod, NovoAtlas/TerraScale.
