# Structures & locations — authoring contract

This is the frozen interface that **all** location work (human or agent) must follow.
It exists so that many locations can be built **in parallel** without breaking each other.
Change it only deliberately, in one place, with the integration build green.

## Scope

A "location" is a place the player can find, explore, and later fight in: cities, temples,
vaults, strongholds, altar sites, ruins, rifts. Boss **arenas** are rooms inside locations;
the combat itself is out of scope until the bestiary/endgame systems exist.

## Tiers & sizes (gameplay scale, not map scale)

Real proportions are deliberately abandoned so locations are playable. Sized in blocks:

| Tier | Size (blocks) | Examples | Notes |
|---|---|---|---|
| **Metropolis** | 600–800 across | the 24 curated cities | districts, cathedral, arena |
| **Town / site** | 200–300 | `drowned_temple`, `order_vault`, `cult_stronghold` | a courtyard + 3–8 rooms |
| **Minor** | 60–120 | `ritual_altar_site`, `rift_scar`, ruins | one feature + a fight space |

## The one interface: `StructureBuilder`

Every location is placed via the shared builder. **No location writes blocks directly.**

```java
public interface StructureBuilder {
    /** Writes one block, clipped to the chunk currently generating. */
    void put(int x, int y, int z, BlockState state);

    /** Fills a box. */
    void fill(int x0, int y0, int z0, int x1, int y1, int z1, BlockState state);

    /** Hollow box with a wall block and optional openings. */
    void walls(int x0, int y0, int z0, int x1, int y1, int z1, BlockState state);

    /** Deterministic RNG for this location (seeded by the location id). */
    RandomSource rng();

    /** Terrain surface Y from the Earth layers at (x,z). */
    int groundY(int x, int z);

    /** The palette chosen from the real biome/terrain at this location. */
    Palette palette();

    /** Reserves a jigsaw marker for a hand-authored .nbt, if one exists for `name`. */
    void marker(String name, int x, int y, int z);
}
```

Rules:
- **Chunk-clipped**: a builder is handed the current chunk; writes outside it are dropped
  (so a large structure is built incrementally as its chunks generate).
- **Deterministic**: same id → same layout, always. No `Math.random()`.
- **No global state**: nothing static that a parallel agent could race on.

## Registration

A location registers once and is placed by the generator:
- **Fixed-coordinate** (cities) — pinned to baked `settlements.json` coords.
- **Random-in-biome** (temples, ruins) — via a structure set with a spacing/separation,
  restricted to the biomes from `BiomeTable`.

## Palettes

Palettes are derived from real geography (`Palette.fromBiome(koppen, coastal)`), so a
desert city and a harbour city differ without hand-authoring. Locations pick a palette via
the builder — they never hardcode blocks like `Blocks.SANDSTONE` at the top level.

## Hand-authored `.nbt` (jigsaw) — the seam for artistry

The generator emits **valid placeholder `.nbt`** for every piece. To improve a piece by
hand:

1. Run `python3 tools/export_nbt.py <location>` to write its pieces under
   `src/main/resources/data/eldritch_horror/structure/…` (or in `run/schematics/`).
2. Open the world, `/place template …` or use a structure block to load it, edit it to
   taste, and **re-save** it to the same path.
3. The generator prefers the hand-authored `.nbt` when present and falls back to the
   procedural placeholder otherwise.

This means **any** piece can be swapped for a hand-built one **without touching code** —
the seam between "generated" and "artistic".

## What each worker (human or agent) must do

1. Touch **only your own files** (your location(s), your data). Never edit the shared
   builder or another location.
2. Name things per the convention: `world/loc/<type>/<name>.java`, data at
   `data/eldritch_horror/…`.
3. **Run the integration build** before finishing: `./gradlew compileJava` must pass.
4. Add a GameTest or a `/place`-able check for your location.
5. Do not change registry ids that exist.

## Out of scope (do not attempt in location work)

- Combat/damage/boss AI (needs the bestiary epic).
- Loot inside chests beyond placeholder tables.
- Anything client-only.

## Open items

- `tools/export_nbt.py` / `import` helpers (to be written with the toolkit).
- Structure-set JSON for random-placed locations.
- The exact `Palette` block vocabulary.
