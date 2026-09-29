# Night work plan — "bring the world to a good state"

Autonomous overnight run. Goal: make the generated world read correctly, verify by
rendering (no GPU client). Every workstream ends with a green build + a commit; a morning
report summarises what was seen and done.

## The method: render, look, fix

We cannot open a client. Instead a dev-only **server-side renderer** (`core/CityRenderer`)
draws generated chunks to PNG using the game's own map colours. We then **look at the
PNGs** and judge style conformance honestly, fix, and re-render.

Two views per city: **top-down** (roofs/streets/landmark footprints) and **isometric**
(silhouette: roofs, spires, tiers).

## Workstreams (in order)

1. **Renderer** → PNGs for all 24 cities. *(in progress)*
2. **Style conformance pass**: look at every city, list what reads and what does not
   (e.g. "Tokyo still grey", "Paris==Mexico City"). Fix palettes/silhouettes. Re-render.
3. **De-duplicate landmarks** flagged by the audit:
   - Paris vs Mexico City (both `StyleKit.cathedral`) — differentiate.
   - Istanbul vs Cairo (both `StyleKit.mosque`) — differentiate.
   - Also similar palettes: Paris/Buenos Aires, Jakarta/Los Angeles, London/Moscow.
4. **Surface decoration**: the terrain is bare (no trees/grass/flowers). Add a light,
   biome-appropriate decoration pass so the world is not "bald". Keep it cheap.
5. **Second-echelon locations** (one file each, parallel agents):
   `cult_stronghold`, `drowned_temple`, `order_vault`, `ritual_altar_site`, `rift_scar`.
   Random-in-biome placement for these may need a real `Structure`/`StructureSet` — decide
   then.
6. **World boundary — the cylinder**: longitude wraps (`EarthMap` X modular), a seam warp
   for entities crossing the X edge, poles bounded with the **Morok** debuff. See
   `design/23-boundary-and-travel.md`.
7. **Tests**: a GameTest/check that a generated city actually places blocks (regression guard).
8. **Docs + morning report**: update `docs/STATUS.md`, attach the best renders.

## Discipline

- One Gradle build at a time; integration builds serialised by the main session.
- Parallel agents touch **disjoint files only** (one style/location per file).
- Everything committed; `main` stays green.
- Honest reporting: if something looks wrong in the render, say so.
