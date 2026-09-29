# World boundary, Morok, and travel

Status: **decided (design)**; implementation deferred. Supersedes the "no border" note in
`WORLDGEN.md`.

## The problem

The world is a flat, finite rectangle (131 072 × 65 536 blocks), not a sphere. A true globe
is impossible on Minecraft's cubic grid (chunks are square, gravity is one axis, the poles
cannot converge). Rather than fight this, the shape becomes **fiction**: the edge of the
known Earth is an in-world phenomenon, not an invisible wall.

## The boundary (mix: ice + Morok)

Edges are felt differently depending on where you are. There is **no hard wall** the player
bumps into; each edge is a *place*.

| Edge | What it is | Experience |
|---|---|---|
| **South** | Antarctica — an endless ice sheet | cold, white, featureless; the map has run out |
| **North** | Permanent pack ice / Arctic void | the same, mirrored |
| **East / West** | **Morok** (Морок) | see below |

### Morok (Морок)

Past the eastern/western extent the world does not simply end: it **repeats**. The sampler
already clamps beyond the map, so the last column of terrain is tiled on forever. That
artifact becomes the horror:

- The terrain **repeats** — the same coastline, the same hill, again and again.
- **Sanity drains** the longer you stay out there; perception distorts first.
- The **map "smears"**: your position markers drift and lie.
- Crossing far enough triggers a **cast-back** to the last "known" point, as if the world
  refuses to let you leave.

The player who walks into Morok does not hit a box; they **stop trusting the ground**.

## Why the edge is the edge

The boundary is the limit of the **known Earth** — the area we have baked real data for.
Beyond it lies what has not been charted, and uncharted space is where the horror lives.
This also ties the map to the knowledge/sanity systems: the world is only as real as what
the players have established.

## Travel (principle only)

Long distances between cities (thousands of blocks) must be justified by **progression**, not
free teleports. Frozen principle:

- **Every city is a potential network node.** A node is not usable until it has been
  **discovered/opened**; travel is therefore earned by exploration.
- **Early game**: on foot / horse / boat.
- **Mid/late game**: a network linking opened nodes (concrete form TBD — steles, waygates,
  caravan routes), probably with a **cost** (time, coin, or sanity/corruption for occult
  routes).
- Exact mechanics, names, and the occult "Veil gate" branch are **deferred**.

## Open questions

- Does Morok also appear if you sail off the north/south ice, or only east/west?
- Cast-back: always, or only if the player has a "known" anchor?
- Should the repeat be visually seeded (e.g. subtle differences) or exact?
- How does Morok interact with the future **Veil dimension** and Map events
  (see `22-map-and-knowledge.md`)?

## Implementation notes (later)

- Sampler (`EarthMap`): currently `clamp(...)`. Morok reuses that clamp as its mechanism;
  add a "distance past the edge" value so effects scale with depth.
- World border remains for chunk-generation/technical limits, but must **not** read as a
  wall at the ice/Morok edges (may need to move the border out or hide it).
