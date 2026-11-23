# Authored-city lighting (world generation)

The 24 curated cities used to light only their landmarks and a couple of authored axes, so the
600–800-block districts went fully dark at night. City lighting is now produced by the **city
generator itself**, in the same pass that stamps the plan's blocks, for both the cached plan replay
and the direct `plan.build(...)` path.

## Where it lives

`CityLocation` (plan branch, after the plan writes) calls `lightAuthoredCity(b, ctx, p)` before
the `city_center` marker. That single call covers both paths:

- cached: `plan.build(recorder, …)` is recorded once per city, replayed per chunk with
  `builder.applyChunk(writes, chunkX, chunkZ)`, then the lighting pass runs;
- direct: `plan.build(b, ctx, p)`, then the same lighting pass.

The pass reads the **current chunk's real block states after the plan writes** and writes through
`StructureBuilder.put`, which drops anything outside the generating chunk. No `ServerTickEvent`,
block ticker or runtime repair pass is involved.

## What it places

1. **Street lamps** on a fixed 24-block grid aligned to the city centre
   (`floorMod(x - cx, 24) == 0 && floorMod(z - cz, 24) == 0`), at the planned surface level
   `CitySurface.at(x, z)`. A lamp is only placed when
   - the column is inside the authored district radius,
   - the column is on land,
   - the cell at the surface is solid (a paved floor), and
   - the two cells above it are open (air/replaceable) after the plan's writes.

   The lamp is `StyleKit.lanternPost`: `Palette.rail()` at surface+1 and +2, `Palette.light()` at
   surface+3. Columns within 24 blocks of the city centre are skipped so the plaza stays clear.

2. **Interior lights.** For one column in eight (chosen by a pure position hash of
   `(x, surfaceY, z)` — no rng), the column is scanned from surface+1 up to surface+48 for open
   cells with a solid floor within 3 blocks below and a solid ceiling within 4 blocks above —
   i.e. inside rooms, storeys and covered alleys. At most one lamp per covered space per column is
   placed in the topmost open cell under that ceiling (ceiling−1), so the floor stays walkable.
   `Palette.light()` is used with `hanging=true` when the block supports it (lanterns do), so it
   reads as a hanging fixture.

## Why generation-time

- **Deterministic and chunk-order independent.** Every decision is a pure function of the
  column's absolute coordinates and the chunk's own final blocks; the position hash replaces the
  layout RNG. The same chunk produces the same lamps no matter which chunk generated first.
- **No cross-chunk reads.** Only the generating chunk's 16×16 columns are read and written, so the
  pass never depends on a neighbour that may not exist yet and never writes outside the city
  radius.
- **Free lighting.** Blocks placed during worldgen are included in the chunk's normal lighting
  initialisation, so the lamps are lit correctly on first load. A runtime ticker would have to
  find and edit already-generated chunks, cause block/light updates, and be chunk-timing
  dependent.
- **Cost.** One lighting pass per city chunk: a 24-grid candidate (at most one per chunk) plus a
  hash-gated column scan. City chunks are a small fraction of the world, and the pass is skipped
  entirely outside the district.

## Expected blocks

- Street lamp: rail block (`Palette.rail()` — a fence in most styles) at surface+1/+2 and
  `Palette.light()` at surface+3, i.e. a regular `minecraft:lantern`, or `minecraft:soul_lantern`
  in cold (Köppen D/E) palettes, at `CitySurface.at(x, z) + 3`.
- Interior light: `minecraft:lantern` / `minecraft:soul_lantern` with `hanging=true` in the cell
  directly under a room ceiling (within 4 blocks of the first open cell in the column).

## How to verify

1. Rebuild the dev image (the image bakes the source) and compile:
   `docker compose build dev`, then
   `scripts/safe-build.sh bash -lc 'docker compose run --rm dev ./gradlew compileJava --rerun-tasks --no-daemon --console=plain'`.
2. **Regenerate the world** — chunk generation is cached in `run-data`; an existing city will keep
   its old blocks. Drop it first: `docker compose run --rm dev bash -lc 'rm -rf /workspace/run/world'`.
3. In a generated city, walk the streets: lamps repeat every 24 blocks from the centre
   (`/tp` to `city.x + 24k, surface+4, city.z + 24m`), with no lamps inside the central 24-block
   plaza radius. Lantern light should also appear indoors, one layer under ceilings.
4. Determinism check: regenerate the same city (fresh world or `/forceload` the same chunks after
   deleting the region) and compare lamp coordinates — they must be identical.
