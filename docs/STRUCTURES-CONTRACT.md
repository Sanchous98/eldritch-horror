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

    /** (Plus the shaped operations in "Architecture vocabulary" below: roof/spire/buttress/
     *  window/crenellations/monument/scatter/ruins.) */

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


## Cultural styles (local colour)

Climate alone cannot make Tokyo look Japanese — Tokyo is Köppen *Cfa*, the same class as
New Orleans. **Local colour is culture, not climate.** So a second axis is added.

A **`CityStyle`** is one file per city style. The shared `CityLocation` layout (jittered
district, alleys, decay, lights) is owned centrally; the style only supplies *materials* and
*flourishes*, so many styles can be built in parallel without touching shared code.

```java
public interface CityStyle {
    /** Stable id, e.g. "japanese". */
    String id();

    /** Materials for this culture, varied by climate + coast. May delegate to
     *  Palette.fromBiome(...) for the parts culture does not change. */
    Palette palette(int koppenClass, boolean coastal);

    /** The skyline landmark (cathedral / temple / mosque / pagoda …). */
    void landmark(StructureBuilder b, RandomSource rng, int cx, int cz, int ground, Palette p);

    /** Optional per-building flourishes (signs, balconies, verandas…). Default: none. */
    default void flourish(StructureBuilder b, RandomSource rng, int x, int y0, int z,
                          int x1, int y1, int z1, Palette p) {}

    /** Optional street furniture (lanterns, torii, neon, statues…). Default: none. */
    default void streetProps(StructureBuilder b, RandomSource rng, int cx, int cz,
                             int district, int ground, Palette p) {}
}
```

- **`CityStyles.forCity(String name)`** is a single registry file, owned centrally, mapping
  each curated city to a `CityStyle` (fallback: the gothic `Palette.fromBiome` when a city's
  style is missing). **Workers never edit the registry.**
- A worker's job for one city = **create exactly one file**
  `world/loc/style/<Name>Style.java` implementing `CityStyle`. Nothing else.
- **Architecture helpers** available to styles (built on the base builder): the shaped ops in
  "Architecture vocabulary" below, plus style-specific helpers that a style may add as local
  `private` methods in its own file (e.g. `torii`, `pagoda`, `mosqueDome`).

### JAPANESE (reference style, Tokyo)

| Element | Blocks |
|---|---|
| plaster wall (shikkui) | white concrete / smooth quartz |
| timber beams, koushi lattice | dark oak / spruce logs, fences, trapdoors |
| kawara tile roof (grey, flared eaves) | deepslate tiles / polished andesite, stairs+slabs |
| oxidized copper roof | oxidized/weathered copper |
| vermilion (torii, pillars) | red concrete / red terracotta / crimson planks |
| paper lantern / chōchin | shroomlight / ochre froglight / redstone lamp |
| neon signage | red/magenta/blue stained glass + glowstone/sea lantern |

The **landmark** is a five-level **pagoda** with stacked flared roofs; **streetProps** add
**torii** gates and stone/paper lanterns along the approaches. This is the reference other
styles copy.

## Palettes

`Palette` is the frozen block vocabulary (locations never hardcode blocks). It is a
**dark-gothic core with regional flavour** — every city reads as brooding architecture, but
materials and overgrowth change with the real climate **and culture**:


```java
public record Palette(
        BlockState ground,       // path / plaza surface
        BlockState foundation,   // building base course
        BlockState wall,         // primary wall block
        BlockState weathered,    // cracked / mossy variant of `wall`
        BlockState accent,       // trim, pillars, quoins
        BlockState roof,         // solid roof block
        BlockState roofStairs,   // pitched-roof stairs
        BlockState roofSlab,     // pitched-roof slabs / eaves
        BlockState window,       // glazing (pane-like)
        BlockState frame,        // window frame / mullions
        BlockState door,         // door / gate
        BlockState rail,         // railing / fence
        BlockState light,        // lantern / candle / torch
        BlockState overgrowth,   // vines / leaves / moss (nullable = none)
        BlockState rubble         // gravel / coarse dirt / debris
) {
    static Palette fromBiome(int koppenClass, boolean coastal);
}
```

Regional mapping (Köppen family → materials; keep the gothic *shape* everywhere):

| Family | wall | accent | roof | overgrowth |
|---|---|---|---|---|
| **C** temperate/oceanic | deepslate + stone bricks | dark oak | dark oak stairs, deepslate tiles | vines, moss |
| **D** continental/boreal | deepslate, spruce | spruce logs | spruce stairs, deepslate | moss, ferns |
| **B** arid | sandstone bricks, cut sandstone | blackstone trim | dark oak/acacia | dead bush |
| **A** tropical | mossy stone bricks | jungle wood | jungle stairs | vines, leaves |
| **E** polar | stone bricks, deepslate | spruce | snow/ice slabs | none (snow drift) |
| **coastal** modifier | + prismarine | + dried kelp | — | kelp |

## Architecture vocabulary

The builder exposes **shaped** operations, not just boxes, so locations compose real
silhouettes. All are deterministic and chunk-clipped:

```java
void pitchedRoof(int x0, int z0, int x1, int z1, int y, int height, int ridgeAxis);
void spire(int cx, int cz, int baseY, int height);
void buttress(int x, int z, int baseY, int height, Side outward);
void window(int x, int y, int z, int height, int width, boolean vertical);
void crenellations(int x0, int z0, int x1, int z1, int y);
void monument(int cx, int cz, int baseY);   // obelisk / statue plinth
void scatter(int x0, int z0, int x1, int z1, int y0, int y1, BlockState state, float chance);
void ruins(int x0, int y0, int z0, int x1, int y1, int z1, float holeChance);
```

## Atmosphere rules (what makes it feel *wrong*)

1. **Silhouette over footprint**: steep pitched roofs, spires, buttresses, tall narrow
   windows — even a small building should look gothic, not like a box.
2. **Decay pass**: after building, scatter cobwebs in corners, moss/overgrowth low on
   north/wet walls, rubble at foundations, punch holes in some walls, break some roofs.
3. **Light is scarce**: windows mostly dark; lit only by lanterns/candles, sparsely.
4. **Density & alleys**: streets 2–3 wide, irregular blocks, no grid straightness.
5. **Landmark contrast**: one over-tall cathedral/spire per city dominates the skyline.


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
