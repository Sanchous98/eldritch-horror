# City vegetation: trees inside authored districts

The 24 curated cities are built from hand-authored plans (see
[`CITY-PLAN-CONTRACT.md`](CITY-PLAN-CONTRACT.md)). A plan should read as a city, not as a
clearing in a forest: natural biome trunks and crowns must not stand in its streets or poke through
its roofs, while the plan's own deliberate trees — Cairo's courtyard palms, Beijing's pines,
Bangkok's banyan canopies, and the rest — must survive.

## Why plan cities needed their own pass

`CityLocation.build` handles two kinds of city:

* **Procedural cities** (no plan registered) go through `paveDistrict`, whose per-column clear
  removes everything above the terraced surface for `CLEAR_ABOVE` (40) blocks.
* **Authored cities** return early after grading the district, replaying the recorded plan and
  lighting it. The procedural sweep never ran, so the biome's trees stayed where they were.

The fix keeps that early return but adds the vegetation sweep to the authored branch, plus a
runtime net for chunks that were generated before the fix or that are touched later.

## What is cleared

For every column of the **generating chunk** that is inside the plan's district circle and is land:

* scan from the planned surface (`CitySurface.at(x, z)`) **exclusively above** it, up to 40 blocks
  above the higher of the planned and natural terrain (clipped to the world's build range). Using
  the natural height too matters on a cut slope: the biome's trees stood at the original level, so
  a cap measured only from the lowered street would leave their crowns in the air;
* replace any block in `BlockTags.LOGS` or `BlockTags.LEAVES` with air via
  `StructureBuilder.put`, so the write is chunk-clipped like every other structure write.

Nothing below the planned surface is touched. Buried roots are left alone, which is deliberate:
the surface pass is what the player sees, and digging under a district should still find the
original soil/wood rather than a hollowed-out column.

Only logs and leaves are removed. Grass, flowers, bamboo, mushrooms and other decoration are not
this pass's business, and a plan may place them on purpose.

## How authored vegetation is told apart from natural vegetation

The plan is run once into a `RecordingBuilder`; `RecordingBuilder.writes()` groups its final block
states by chunk (`Map<Long, List<CityWrite>>`, keyed by `ChunkPos.pack`). Before a chunk is
swept, the generator builds the set of positions where **that chunk's recorded write is itself a
log or leaf**:

```java
if (write.state().is(BlockTags.LOGS) || write.state().is(BlockTags.LEAVES)) {
    authored.add(write.pos().asLong());
}
```

A block is removed only when its position is **not** in that set. The recorder keeps only the
final state per position, so a plan that paints a trunk and later carves it away records air (or
whatever replaced it) and is not protected; a plan that paints a palm records the palm and is
protected exactly where it stands.

This is an exact-position test, not a radius test: a natural tree growing two blocks from an
authored palm is removed, while the palm is kept.

## Generation-time sweep

`CityLocation.sweepPlanVegetation` runs immediately after the plan replay and the lighting pass,
in the same generating chunk. It reads only that chunk through the new
`StructureBuilder.getState`; outside the chunk, and outside the vertical range, `getState` returns
air (mirroring `put`'s clipping). The sweep is therefore deterministic, chunk-order independent and
safe on the concurrent worldgen threads.

`Builder.getState` reads the live `WorldGenLevel`; `RecordingBuilder.getState` reads its own write
map (or air); the internal discarding builder delegates so a plan can keep running against it
unchanged.

## Runtime safety net

`CityVegetation` is a small server ticker in the same style as `CityRuins`:

* every 20 ticks, for each player in the overworld, find the nearest curated city;
* only **plan** cities (`CityPlans.forCity(name) != null`) are considered — procedural cities
  already have their own generation sweep;
* inspect loaded chunks within 4 chunks of the player that fall in the district circle;
* skip chunks already marked by the persisted `VEGETATION_SWEPT` chunk attachment;
* scan the same planned-surface-to-`surface + 40` band (measured from the higher of the planned
  and natural terrain, as in the generation sweep) and remove stray logs/leaves, again exempting
  the plan's authored log/leaf positions;
* remove at most `cityVegetationBlocksPerPass` blocks per pass and inspect at most
  `cityVegetationChunksPerTick` chunks per pass; a chunk is marked swept only after a complete
  inspection, so a budget-limited pass resumes later.

`CityLocation.surfaceOf(city)` rebuilds the same graded surface from the baked Earth map, so the
ticker needs no generating chunk and no force-loaded terrain.

### Authored data availability

The authored exemption set comes from the in-memory `PLAN_CACHE` that worldgen fills while
building a city. If a city's plan is not cached — a fresh server before any of its chunks
generate, or an evicted LRU entry — the ticker **does not remove anything for that city** and does
not mark its chunks swept. It cannot honestly distinguish a plan's palm from a biome tree without
that data, and deleting a courtyard palm is worse than leaving a stray trunk for one more visit.
Generating any chunk of the city (or the normal worldgen pass) repopulates the cache, after which
the next visit sweeps the loaded chunks.

### Caveat: player-built trees

The first time a chunk is visited after this feature is installed, a player-planted log or leaf
inside the district above the planned surface looks exactly like a stray biome tree and will be
removed. The sweep is one-shot per chunk (the attachment), so builds made after that visit are not
touched. Servers that want the net off can set `enableCityVegetationCleanup = false`; the
generation-time sweep still applies to newly generated chunks.

## Configuration

| Key | Default | Meaning |
|---|---|---|
| `enableCityVegetationCleanup` | `true` | Master switch for the runtime net. |
| `cityVegetationChunksPerTick` | `1` | Loaded plan-city chunks inspected per pass. |
| `cityVegetationBlocksPerPass` | `64` | Hard cap on blocks removed per pass. |

The generation-time sweep is not configurable: it is part of city generation, like the procedural
`paveDistrict` clear.

## Files

* `world/loc/StructureBuilder.java` — the minimal `getState` read.
* `world/loc/Builder.java`, `world/loc/RecordingBuilder.java` — chunk-clipped live read and
  recorded-write read.
* `world/loc/city/CityLocation.java` — plan sweep, authored-vegetation lookup, map-backed surface.
* `world/loc/city/CitySurface.java` — map-backed surface for the runtime pass.
* `world/city/CityVegetation.java` — the bounded runtime ticker.
* `registry/ModAttachments.java` — `VEGETATION_SWEPT` chunk marker.
* `core/ModConfig.java` — the three cleanup settings.
