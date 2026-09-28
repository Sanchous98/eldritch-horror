# World generation

How the Overworld map is made. **Decision:** a **prebuilt `EARTH` map** — the actual
planet — baked from public geodata into compact layers, sampled deterministically at
runtime. **Whole globe**, **equirectangular**, at **2 blocks/pixel**.

> **Important:** no Minecraft *world seed* can generate Earth. Vanilla's generator is a
> noise function with no notion of external geography, so no seed emits the real coastline
> (confirmed by EarthMC, Mojang's feedback forum, and the Minecraft Wiki). An "Earth map"
> must come from **baked geographic data** (or a prebuilt world), not a seed. Here a fixed
> constant selects the baked dataset — the closest thing to an "Earth seed", deterministic.

## Goals

- **Deterministic & fixed**: the same map every run; iterate on it, ship it.
- **The whole Earth**, with real continents, oceans and coastlines.
- **Vanilla biomes** reused wholesale: cold poles, temperate middle, hot equator.
- **Playable**: compressed climate, a world border, sensible start region.
- **Small footprint**: a few tens of MB of baked layers, not a prebuilt save.


## Non-goals

- **No ores, no caves.** The world is a *surface* world: terrain height and biome only.
  Vanilla ore/cave generation is deliberately absent. This is consistent with the design:
  **nothing in [`design/`](../../design/README.md) requires mining or underground
  resources.** Reagents come from the surface, structures, mobs and cult trade; vanilla
  stone tools/armour aren't part of the progression. (If the design ever does need an
  underground reagent, add a *targeted* feature or a structure/loot source rather than
  re-enabling global ore generation.)
- Custom biomes (vanilla set is enough — see `design/20-map.md`).
- 1:1 Earth or realistic geography.
- Changing the Nether or End (Overworld only).
- Pre-generating chunks on disk up front (the map is *deterministic*, so it is
  effectively pre-generated; we only decide the *layout* ahead of play).

## Architecture

Two custom, deterministic pieces replace vanilla worldgen; **surface features** (trees,
grass, flora) and structures stay vanilla and run on top. Carvers (caves) and ore
features are intentionally not run — see "Non-goals".

```
world (x,z) ──► EarthField ──► LandMask + Elevation + Köppen   (per column)
                   │
                   ├──► EarthChunkGenerator   → terrain height per column
                   └──► EarthBiomeSource      → vanilla biome per column
```

Both consume the **same** `ContinentField` so terrain and biome agree.

### `ContinentChunkGenerator` (`Registries.CHUNK_GENERATOR`)

- Samples `ContinentField` for each `(x,z)`: `elevation` (real metres).
- **Sea level** at Y=63 (elevation 0). Below → ocean floor as deep as the real
  bathymetry; above → land scaled toward the build ceiling.
- **Vertical exaggeration:** a linear map of the real −11,000…+8,800 m onto the build range
  would erase most terrain (the Himalaya would only reach ~Y+96). We apply a **non-linear
  curve** so the extremes feel extreme:

  | Real | Minecraft Y |
  |---|---|
  | Mariana Trench (−11,000 m) | ~−40 (bedrock) |
  | Ocean floor (−4,000 m) | ~25 |
  | Sea level (0) | 63 |
  | Tibetan plateau (+4,500 m) | ~150 |
  | Himalaya / Everest (+8,800 m) | ~250–300 |

  Land is exaggerated more than ocean, so highlands read as *high* and the abyss as *deep*.
- Emits a `NoiseChunk`/`ChunkAccess` surface; delegates caves/carvers to vanilla.

### `EarthBiomeSource` (`Registries.BIOME_SOURCE`)

Uses the **baked real biome layer** (Beck Köppen–Geiger classes), not computed latitude —
so the Sahara is desert and the Amazon is rainforest, which latitude alone cannot know.
A **class → vanilla biome** table maps all 30 Köppen classes onto vanilla biomes (below),
with ocean/land and elevation refinements (peaks, coasts).

| Köppen class | Vanilla biome |
|---|---|
| Af / Am | `jungle` |
| Aw | `savanna` |
| BWh | `desert` |
| BWk | `desert` (cold variant) |
| BSh / BSk | `savanna` / `plains` (steppe) |
| Csa / Csb | `plains` (Mediterranean) |
| Cfa / Cfb / Cfc | `forest` / `birch_forest` |
| Cwa / Cwb / Cwc | `forest` (monsoon) |
| Dfa / Dfb / Dfc | `taiga` / `forest` |
| Dwa–Dwd / Dsa–Dsd | `taiga` / `snowy_taiga` |
| ET | `snowy_plains` / `snowy_taiga` |
| EF | `ice_spikes` / `snowy_plains` |
| Land, high elevation | `windswept_hills` / `frozen_peaks` / `stony_peaks` |
| Coast | `beach` / `stony_shore` |
| Ocean | `ocean` → `deep_ocean` by depth |

## The map data

Real geography can't be derived from a formula, so we bake it. The choices are **where the
data lives**:

| Placement | Shipped | Trade-off |
|---|---|---|
| **Baked layers** (chosen) | tens of MB | self-contained, deterministic, works offline, no live dependency |
| Runtime download | ~0 | needs network + cache + a maintained source; first-use latency |
| Pre-generated chunks | 6–300 GB | huge saves; not shippable as a mod |

**Decision:** biomes must *really match* and mountains/oceans must be extreme, so we bake
all three layers from **real, observed data**:

| Layer | Source | Encoding |
|---|---|---|
| **Landmask / coastline** | Natural Earth `land` | 1-bit |
| **Elevation + bathymetry** | ETOPO (NOAA) | 8-bit, signed range, quantized non-linearly |
| **Climate / biome class** | Beck Köppen–Geiger 1 km (CC-BY) | 5-bit class (0–29) |

This is the difference between *Earth-shaped* and *Earth*: real elevation puts the Himalaya
in Asia and the deep trenches in the Pacific, and real climate puts the Sahara where it is.


### Layer format (at 2 blocks/pixel, 8192×4096)

| Layer | Encoding | Size |
|---|---|---|
| Landmask | 1-bit PNG (bitmap) | **~0.1 MB** (baked) |
| Elevation | 8-bit grayscale PNG, non-linear quantized | **~5–30 MB** |

All committed under `src/main/resources/`, read once and cached. Equirectangular mapping
`lon/lat → x/z` is linear.

This is exactly how the prebuilt Earth maps are made (WorldPainter from these datasets) —
we sample the data at runtime instead of baking every chunk.


## The `EarthField` interface

One sampler, so `ChunkGenerator`/`BiomeSource` don't care where the data comes from:

```
elevation(x, z)   -> metres (signed; below 0 = ocean floor)   [baked ETOPO]
koppen(x, z)      -> 0..29 climate class                       [baked Köppen]
land(x, z)        -> bool                                      [BakedMask]
```

- `elevation` ← the baked **ETOPO** layer (real Himalaya, real trenches).
- `koppen` ← the baked **Beck Köppen–Geiger** layer (real Sahara/Amazon).
- `land` ← the baked **landmask** (used for coasts/validation).




## How big is the data, really?

**Not 300 GB.** The huge numbers people quote (EarthMC 6 GB, 0xBit up to 300 GB) are
**prebuilt world *saves*** — every one of millions of chunks generated and written to disk
up front. We never do that: our map is sampled at runtime, so we ship only the small source
layers above, and each player's save grows only where they actually explore.

### Map extent & resolution (decided)

- **Map:** whole Earth, **equirectangular**, **2:1** — **16,384 × 8,192** blocks (X = 360°
  longitude at 2 blocks/°, Z = 180° latitude at 2 blocks/°).
- **World border:** matches the map (not square). `x ∈ [−8192, 8192)`, `z ∈ [−4096, 4096)`.
- **Layer resolution:** **8192 × 4096** pixels = 2 blocks/pixel.
- **Projection:** longitude `−180…180 → x −8192…8192`; latitude `+90…−90 → z −4096…4096`.

A whole-globe map is inherently **2:1**; forcing it into a square would stretch latitude 2×
(continents look tall), so the map is 2:1 and the border follows.

> **Polar stretch:** in equirectangular, the poles are a single point stretched across the
> full width, so Antarctica becomes a full-width strip at the map edge. That is geometrically
> correct; if it plays badly we can trim/ice-cap it later without changing the rest.

> **Baked:** `tools/bake_landmask.py` rasterizes Natural Earth's 1:50m land polygons to
> `landmask_8192x4096.png` (1-bit, ~0.1 MB on disk; land ≈ 33%).

| Layer | Pixels | Size |
|---|---|---|
| Landmask (1-bit packed) | 8192×4096 (33.5 M) | **~4 MB** |
| Elevation (8-bit) | 8192×4096 | **~32 MB** |
| Climate | computed (lat + distance-to-coast) | 0 |

> At 1:1 Earth scale you'd need ~33.5 M columns just for latitude/longitude, and far more
> for elevation — 300 GB territory. We map **Earth onto a 16k-wide playable strip** at
> 2 blocks/pixel instead: "playable, not a simulation".

### Playability

At 2 blocks/pixel the world is ~8 km across — realistic Earth geography, compressed into a
survival-scale world (continents a day's walk, not a lifetime's). A **sensible start region**
(a temperate, coastal spawn near the map centre) is chosen so players don't spawn mid-ocean.



### Playability rules

- **World border** matching the 2:1 map: `x ∈ [−8192, 8192)`, `z ∈ [−4096, 4096)`.
- **Sensible spawn**: a temperate coastal region near the map centre — never mid-ocean.
- **Bounded oceans**: real Earth oceans are huge; boats/rites make them crossable, and the
  spawn is on land.
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

## Implementation status

| Piece | State |
|---|---|
| Baked landmask / elevation / Köppen layers | ✅ done (`tools/bake_earth.py`) |
| `EarthMap` + `EarthMaps` (server-side sampling) | ✅ done |
| `ElevationCurve`, `BiomeTable` | ✅ done |
| `EarthBiomeSource`, `EarthChunkGenerator` | ✅ done |
| Overworld dimension override | ✅ done (`data/minecraft/dimension/overworld.json`) |
| Runs on a dedicated server | ✅ verified (`Done (…)`, single `minecraft:overworld`) |
| **Surface decoration** (trees, grass, flowers, flora) | ❌ **deferred** — see the *Surface decoration* story |
| Pinned coastal spawn | ❌ deferred (vanilla spawn search currently finds land) |
| Structures (altar sites, temples, vaults) | ❌ later (`design/20-map.md`) |

Because a custom generator **replaces** vanilla biome decoration, the surface is currently
bare (stone/surface block, no trees or grass). That is expected and tracked separately.



1. **Bake step** (offline Python): Natural Earth land polygons → a 8192×4096 1-bit landmask;
   optionally ETOPO/GEBCO → an 8-bit elevation layer. Commit the layers.
2. `ContinentField` (landmask sampler + computed climate) + unit tests (no Minecraft).
3. `ContinentBiomeSource` — ocean/land + latitude/moisture biomes.
4. `ContinentChunkGenerator` — height from the field.
5. Wire the Overworld dimension/noise settings to it; set the 2:1 world border + spawn.
6. Structures/content from `design/20-map.md` layered on afterwards.

## Open questions

- **Elevation: real (bake ETOPO) or procedural shaped by the landmask?** Real gives Earth's
  mountains; procedural is a smaller asset.
- Spawn point: exact temperate coastal region to pin.
- Whether rivers are procedural overlay or vanilla `river` at drainage lines.
- Whether the constant selecting the baked dataset is independent of the world seed
  (leaning: independent — stable map across worlds; only loot/mobs vary).
- Landmask encoding: packed 1-bit vs. 8-bit coverage (anti-aliased coastline).


## References

- `design/20-map.md` — content that sits on the map.
- Public data: Natural Earth, NASA Visible Earth (elevation/bathymetry), ETOPO/GEBCO,
  Köppen–Geiger. Prior art: WorldPainter scripts (MattiBorchers), Terra 1-to-1, the
  Continents mod, NovoAtlas/TerraScale.
