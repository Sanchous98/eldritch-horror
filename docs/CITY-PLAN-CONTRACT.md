# Authored city-plan contract

Frozen rules for the 24 curated cities. Each city is **one hand-authored plan file** that places
its own blocks deliberately — there is no procedural city layout and no shared prefab library.
See also `docs/STRUCTURES-CONTRACT.md` and the per-city briefs in `docs/city-briefs/`.

## One file per city

`src/main/java/com/sanchous98/eldritchhorror/world/loc/city/plan/<City>Plan.java`

The class implements `CityPlan`. Your plan must compile with **no edits to any shared file** —
no changes to `CityPlans`, `CityLocation`, `StructureBuilder`, `Builder`, palettes, styles or
another city.

## Interface

```java
public interface CityPlan {
    String cityId();                    // lowercase; spaces/underscores equivalent, e.g. "shanghai"
    int halfExtent();                                       // 300..400 (600..800 across)
    void build(StructureBuilder b, int cx, int cz, int ground, Palette p); // legacy flat form
default void build(StructureBuilder b, CityPlanContext ctx, Palette p); // terrain-aware
    default List<CityPoi> pois() { return List.of(); }
}
```

- `build` is called **once per generating chunk**. Write the whole city on every call; the
  builder keeps only the slice that falls inside the chunk currently generating. Absolute world
  coordinates: the city centre is `(cx, ground, cz)`; `ground` is its levelled surface Y.
- `pois` returns authored anchors with coordinates **relative to `(cx, ground, cz)`**.

## Hard rules

1. **Every block goes through `StructureBuilder`** (`put`/`fill`/`walls`/`room`/`ground`/…, the
   shaped ops, or `CityPlanSupport`). Never touch a `Level`, chunk or block entity directly.
2. **No layout RNG**: no `b.rng()`, `RandomSource`, `Math.random()` or any other randomness.
   Authored arrays, tables and loops are fine; every run must produce the same city.
3. **No static mutable state** and no state that depends on chunk generation order.
4. **Chunk-independent**: `build` runs again for every chunk the city touches, so every decision
   must be identical in every chunk. Do not read the generating chunk
   (`b.isReplaceable`, `b.chunkMinX`/`chunkMinZ`); `b.groundY`/`b.isLand` are global terrain
   reads and are allowed.
5. **Graded authored core, never a cut plate**: the shared pass keeps a flat authored heart at
   `ground` and grades it into the natural terrain over the rim, so the city never looks like a
   piece of land cut out with a knife. Use `ground` inside the heart; use
   `ctx.surface().at(x, z)` for anything on the rim/outskirts, and add authored retaining walls or
   steps where the design needs them. Do not flatten beyond the core or reimplement terraces.
   `b.groundY` is for authored outskirts/water edges only. Prefer `p.*` for the shared palette
   vocabulary; extra authored blocks are allowed where the palette has no slot.
6. No client-only code, no `@OnlyIn`, no registry or config reads.

## POI anchors

```java
new CityPoi("tea_house", CityPoi.Kind.INN, -180, 0, 190, "keeper");
```

`Kind`: `NPC`, `QUEST_BOARD`, `SHOP`, `INN`, `HEALER`, `LANDMARK`, `SECRET`, `SIGN`.
`data` is a free-form hint (profession, quest id, sign text key); use `""` for none. Quest givers
are `NPC` with the quest id in `data`; the notice board is `QUEST_BOARD`. POIs are authored data
only — the runtime hook comes later.

## Helpers (`CityPlanSupport`)

```java
CityPlanSupport.sign(b, x, y, z, Direction.NORTH, "THE BUND ->", "NANJING ROAD ->");
CityPlanSupport.path(b, x0, z0, x1, z1, width, y, state);   // level street, centre line + width
CityPlanSupport.path(b, x0, z0, x1, z1, width, state);      // same, follows b.groundY
CityPlanSupport.plaza(b, x0, z0, x1, z1, y, state);         // one course at y
```

A sign needs a solid block underneath it. Prefer the explicit-`y` street form inside the city.

## Required content per city

- an **arrival gate** with signposts pointing at the districts;
- **3 or more districts** with their own character;
- the city's **landmark** (the one that dominates the skyline);
- **6 or more `CityPoi` anchors**, including **3+ NPC anchors**;
- **2 local quests**, authored as `QUEST_BOARD`/`NPC` anchors plus readable signs; the runtime
  quest hook is a later integration item;
- **4 or more readable signs** (arrival, district entries, shops, warnings);
- **1 secret** (cellar, tunnel, crypt, shrine) reachable on foot;
- a **`STATION` anchor** for every city: the rail yard/station building at the district edge,
  with a straight, level approach for the track; put `north_south` or `east_west` in `data` to
  fix the platform orientation, otherwise the partner bearing decides. The rail network connects
  to this point;
- a **`PORT` anchor** for every coastal city: the harbour/dock at the water edge; the ship
  network will use this point (the transport pass reads the plan anchors, not a terrain guess);
- all POIs inside `[-halfExtent, halfExtent]`; streets and doors connected; no building
  overlapping a street, quay or landmark.

## Quest chains (next phase)

Each city gets a short starting chain (3–5 steps) after the layouts land. The chain is allowed to
vary by city and to adjust the plan: add a door, a cellar, a witness, an item spawn or a second
room where the quest needs one. City plans must expose the anchors the chain will use:
`QUEST_BOARD` (the board), `NPC` (givers/witnesses, quest id in `data`), `SECRET` (the final
scene), and `SIGN` (directions/clues). Do not implement the quest runtime in a city plan.

## Registry

The lead adds one line to `CityPlans` after the plan lands:

```java
register(new ShanghaiPlan());
```

A city agent never edits `CityPlans`. Until a plan is registered, the city keeps the procedural
fallback in `CityLocation`.

## Before handing back

The lead runs the build after the batch; the agent must not run gradle/docker. Only your plan
file was added; the determinism rules above were re-read against the code (no RNG, no chunk
reads, no static mutable state).
