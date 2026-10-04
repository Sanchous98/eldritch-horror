# World boundary, Morok, and travel

Status: **decided (design)**; implementation deferred. Supersedes the "no border" note in
`WORLDGEN.md`.

## The shape: a cylinder

The world is a flat, finite rectangle (131 072 × 65 536 blocks), not a sphere. A true globe
is impossible on Minecraft's cubic grid. But **longitude is a circle** — 180°E *is* 180°W —
so east↔west genuinely closes. Latitude is **not** a circle: the poles are points.

- **East–west (X): closed.** The terrain wraps; you can circumnavigate the Earth.
- **North–south (Z): bounded.** The poles are the hard limits.

This matches the tabletop precedent: you cannot travel *across the poles*; the polar axis is
a boundary, while the longitudes loop.

## East–west: the seam (hidden in open ocean)

There is no barrier, and the seam is placed so it is **never seen**:

- The wrap meridian is shifted to a chosen **ocean longitude** via a longitude-origin offset
  (`LON_ORIGIN`) — no re-bake, just a sampling shift. Chosen: **≈ 26°W (central Atlantic)**,
  where the baked landmask has **0% land within |lat| < 60°**.
- Terrain is then **periodic** across the seam, so a continent crossing the antimeridian no
  longer matters — the join is deep water.
- Physically crossing is handled by a **seam warp**: an entity leaving the east edge is placed
  at the west edge (same Z, same facing, momentum kept), and vice versa. From the player's
  point of view nothing happens — the ocean just continues. No debuff, no wall; a planet you
  can sail around.

## North–south: the limits and Morok

To the **south**, Antarctica — an endless ice sheet. To the **north**, pack ice / Arctic
void. These ends are **bounded**, the map runs out, and the sampler clamps beyond the last
row so the terrain **repeats forever**. That repetition is **Morok** (Морок): the frozen,
featureless repeat at the end of the charted world.

### Morok is lethal, not a safety net

Morok does **not** cast the player back. It **applies an escalating debuff that kills fairly
quickly**:

- The longer and deeper past the edge you go, the faster it stacks.
- **Sanity drains**, then **health** — perception distorts first (map "smears", markers lie).
- It is survivable only by **turning back early**. Push too far and you die out there.

So the poles are not a fence; they are a **place that kills you** if you refuse to accept that
the world has ended.

## Why the edge is the edge

The boundary is the limit of the **known Earth** — the area we have baked real data for.
Beyond it lies what has not been charted, and uncharted space is where the horror lives. This
ties the map to the knowledge/sanity systems: the world is only as real as what the players
have established.

## Travel (principle only)

Long distances between cities (thousands of blocks) must be justified by **progression**, not
free teleports. Frozen principle:

- **Every city is a potential network node**, usable only once **discovered/opened**.
- **Early game**: on foot / horse / boat.
- **Mid/late game**: a network linking opened nodes (form TBD), probably with a **cost**
  (time, coin, or sanity/corruption for occult routes).
- Exact mechanics and the occult "rift gate" branch are **deferred**.

## Open questions

- Polar repeat: **exact** (surreal sameness) or **seeded** (99% same + rare "wrong" details,
  the imitation)? Leaning to the seeded hybrid — it hides the technical join *and* unnerves.
- How does Morok interact with Map events (see `22-map-and-knowledge.md`)? (There is no Veil
  dimension: the horror behind the veil is reached *through* rifts inside the Overworld.)
- Does the Morok debuff also apply off the polar ice, or only at the map edge proper?

## Implementation notes (later)

- **Sampler** (`EarthMap`): today it `clamp(...)`s both axes. Change X to **wrap**
  (`((px - origin) mod W + W) mod W + origin`) using `LON_ORIGIN`; keep Z clamped. Then the
  seam is continuous and sits in mid-Atlantic open water, and Morok lives only at the poles.
- **Seam warp**: a server-side check teleports entities crossing `|x| >= HALF_WIDTH` by
  `∓ 2*HALF_WIDTH`, preserving Z and facing. Must run before the world border can stop them.
- **World border**: must **not** block the X seam — keep Z only (or rely on the warp for X).
  The polar border must not read as a wall; the Morok debuff handles "going too far".
- **Morok debuff**: needs a "distance past the edge" value from the sampler so the effect
  scales with depth; drives the sanity/health escalation on a server tick.
- Chunk generation, biomes and structures all sample through `EarthMap`, so wrapping there
  makes the whole world consistent automatically.
