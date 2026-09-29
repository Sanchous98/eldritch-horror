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

    /** Fills an inclusive box. */
    void fill(int x0, int y0, int z0, int x1, int y1, int z1, BlockState state);

    /** Hollow box (walls only), inclusive, with the floor/ceiling left as-is. */
    void walls(int x0, int y0, int z0, int x1, int y1, int z1, BlockState state);

    /** A hollow room: floor, ceiling and walls, optionally with a 1x2 doorway on `side`. */
    void room(int x0, int y0, int z0, int x1, int y1, int z1, Doorway... doorways);

    /** Replaces whatever is at (x,z) in [y0,y1] with the surface block. */
    void ground(int x0, int z0, int x1, int z1, int y0, int y1, BlockState state);

    /** Deterministic RNG for this location (seeded by the location id). */
    RandomSource rng();

    /** Terrain surface Y at (x,z), clamped to the buildable range. */
    int groundY(int x, int z);

    /** The palette chosen from the real biome/terrain at this location. */
    Palette palette();

    /** A jigsaw marker: prefers a hand-authored .nbt named `name` if present. */
    void marker(String name, int x, int y, int z);
}
```

### `Location`

```java
public interface Location {
    /** Stable id, used to seed the RNG. Must never change once shipped. */
    String id();

    /** Metropolis / Town / Minor — see the tiers table. */
    Tier tier();

    /** Build the location through the builder. Called once per generating chunk. */
    void build(StructureBuilder b);

    /** Half-extent in blocks: chunks farther than this are skipped. */
    int radius();
}
```

### `Locations` (registry)

```java
public final class Locations {
    /** All fixed-coordinate locations, indexed by the chunk they touch. */
    static void register(Location loc, int x, int z);
    /** Called from applyBiomeDecoration; dispatches to overlapping locations. */
    public static void place(WorldGenLevel level, ChunkAccess chunk);
}
```

Rules:
- **Chunk-clipped**: a builder is handed the current chunk; writes outside it are dropped
  (so a large structure is built incrementally as its chunks generate).
- **Deterministic**: same id → same layout, always. No `Math.random()`.
- **No global state**: nothing static that a parallel agent could race on.

### Placement (frozen decision)

- **Fixed locations** (cities) are dispatched from
  `EarthChunkGenerator.applyBiomeDecoration` via `Locations.place(...)`. This already
  chunk-clips and is deterministic; it is good enough to ship cities.
- **Random-in-biome** locations (sites, ruins) will later be a real vanilla `Structure` +
  `StructureSet` (`RandomSpreadStructurePlacement`) restricted to `BiomeTable` biomes.
  Rationale: a `Structure` gives `/locate`, bounding boxes and spawn overrides, but is
  more boilerplate than fixed cities need. Do not build this until a site needs it.


## Palettes

`Palette` is a small data interface (frozen vocabulary — locations never hardcode blocks):

```java
public record Palette(
        BlockState surface,   // ground/paths
        BlockState wall,      // primary wall block
        BlockState accent,    // trim / corners
        BlockState roof,      // roof / upper
        BlockState window,    // glazing (glass-like)
        BlockState light,     // light source
        BlockState door        // door / gate
) {
    /** Palette for the real Köppen class + whether the site is coastal. */
    static Palette fromBiome(int koppenClass, boolean coastal);
}
```

A desert city and a harbour city therefore differ without hand-authoring, and a location
picks its palette via `builder.palette()`.


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
   builder or another location. A location is one file: `world/loc/<type>/<Name>.java`.
2. Implement `Location`; register it in `Locations` **only for fixed-coordinate** places.
   Naming: id `eldritch_horror:<type>/<name>`, Java class `PascalCase`.
3. **Run the integration build** before finishing: `./gradlew compileJava` must pass.
4. Add a GameTest or a `/place`-able check for your location.
5. Do not change registry ids that exist.

## Out of scope (do not attempt in location work)

- Combat/damage/boss AI (needs the bestiary epic).
- Loot inside chests beyond placeholder tables.
- Anything client-only.

## Open items

- `tools/export_nbt.py` / `import` helpers (to be written with the toolkit).
- Structure-set JSON for random-placed locations (see Placement above).
