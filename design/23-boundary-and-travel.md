# World boundary, Morok, and travel

Status: **decided (design)**; implementation deferred. Supersedes the "no border" note in
`WORLDGEN.md`.

## The shape: a cylinder

The world is a flat, finite rectangle (131 072 × 65 536 blocks), not a sphere. A true globe
is impossible on Minecraft's cubic grid. But **longitude is a circle** — 180°E *is* 180°W —
so east↔west can genuinely close. Latitude is **not** a circle: the poles are points.

Therefore the world is a **cylinder**:

- **East–west (X): closed.** The terrain wraps; you can circumnavigate the Earth.
- **North–south (Z): bounded.** The poles are the hard limits.

This matches the tabletop precedent: you cannot travel *across the poles*; the polar axis is
a boundary, while the longitudes loop.

## East–west: the seam

There is no barrier. Terrain is **periodic** across the seam, so a continent that crosses the
antimeridian continues seamlessly. Physically crossing is handled by a **seam warp**: an
entity leaving the east edge is placed at the west edge (same Z, same facing, momentum kept),
and vice versa. From the player's point of view nothing happens — the ocean just continues.
No Morok, no wall; just a planet you can sail around.

## North–south: the limits

To the **south**, Antarctica — an endless ice sheet. To the **north**, pack ice / Arctic
void. These ends are **bounded**, and the map runs out there. The sampler clamps beyond the
last row, so the terrain **repeats forever** — and that repetition is **Morok** (Морок): the
frozen, featureless repeat at the end of the charted world.

- The terrain **repeats** — the same ice, the same ridge, again and again.
- **Sanity drains** the longer you stay; perception distorts first.
- The **map "smears"**: position markers drift and lie.
- Cross far enough and a **cast-back** returns you to the last "known" point — as if the
  world refuses to let you leave the poles.

The player who pushes past the ice does not hit a box; they **stop trusting the ground**.

## Why the edge is the edge

The boundary is the limit of the **known Earth** — the area we have baked real data for.
Beyond it lies what has not been charted, and uncharted space is where the horror lives.
This ties the map to the knowledge/sanity systems: the world is only as real as what the
players have established.

## Travel (principle only)

Long distances between cities (thousands of blocks) must be justified by **progression**, not
free teleports. Frozen principle:

- **Every city is a potential network node.** A node is not usable until it has been
  **discovered/opened**; travel is earned by exploration.
- **Early game**: on foot / horse / boat.
- **Mid/late game**: a network linking opened nodes (concrete form TBD — steles, waygates,
  caravan routes), probably with a **cost** (time, coin, or sanity/corruption for occult
  routes).
- Exact mechanics, names, and the occult "Veil gate" branch are **deferred**.

## Open questions

- Seam warp: only at the exact edge, or a soft band that nudges you back?
- Cast-back at the poles: always, or only if the player has a "known" anchor?
- Should the polar repeat be visually seeded (subtle differences) or exact?
- How does Morok interact with the future **Veil dimension** and Map events
  (see `22-map-and-knowledge.md`)?

## Implementation notes (later)

- **Sampler** (`EarthMap`): today it `clamp(...)`s both axes. Change X to **wrap**
  (`((px % W) + W) % W`); keep Z clamped. Then the seam is continuous and Morok lives only
  at the poles.
- **Seam warp**: a server-side check teleports entities crossing `|x| >= HALF_WIDTH` by
  `∓ 2*HALF_WIDTH`, preserving Z and facing. Must run before the world border can stop them.
- **World border**: must **not** block the X seam. Either drop X from the border and keep Z,
  or rely solely on the border for Z and the warp for X. The polar border must not read as a
  wall (move it out / hide it).
- Chunk generation, biomes and structures all sample through `EarthMap`, so wrapping there
  makes the whole world consistent automatically.
