# Quest instances

Status: design decision, not implemented.

## The split

The game has two layers with different lifetimes and different jobs:

| Layer | Lifetime | Job |
|---|---|---|
| **World** | permanent, the restore point | 1:1 cities, exploration, transport, Ancient Ones, quest routes through real places |
| **Instance** | one server session | quest content: vaults, catacombs, ritual sites — random every session |

The world is what players learn and what a snapshot restores. An instance is content the session
rolls fresh, so a second playthrough is not a walkthrough. **Nothing an instance does may edit the
persistent world** (no city taint, no blocks outside its own dimension, no boss state).

## Why not move the world into instances

Moving ships, cities or bosses into instances would break the point of the 1:1 world: the distances,
the map, the shared city state and the "the Ancient One is somewhere on this Earth" dread only work
if they are the same world for everyone. Instances are therefore additive, quest-only.

## Session seed

- On server start the mod draws a **session seed** (random, not saved).
- Every instance seed is `hash(sessionSeed, questId, stepIndex)`; the same quest step re-opens the
  same instance within a session, even after a player logs out and back in, and a restart rolls
  new layouts.
- The seed is printed once at startup so a session can be reproduced for debugging.

## Entry and exit

- A quest step of kind `enter` (working name) names an instance **type** and a **world anchor**
  (a door, a stair, a ritual pit) chosen from the city package. The anchor is a real place in the
  city; the locator-bar dot points at it, never at coordinates.
- Interacting with the anchor teleports the party into the instance dimension at the generated
  entrance. A return anchor inside the instance teleports back to the world anchor.
- Leaving, completing or emptying the instance closes it; the dimension slice is discarded.
- Instances are never saved: a restart drops them. A quest whose current step needed an instance
  simply re-opens a fresh one (same session semantics do not survive a restart). Quest progress
  must not depend on instance state that cannot be rebuilt.

## Generation

- A small module library per instance type (rooms, halls, stairs, shafts, shrines) assembled from
  the seed: footprint, module graph, enemy mix, loot, and where the quest objective sits.
- One instance lives in its own marked region of a dedicated dimension, so two parties never share
  a layout; the slice is unloaded and released when the instance closes.
- Ancient Ones never spawn in instances. Bosses stay in the world, where distance and the timetable
  matter.

## Quest use

- A chain can mix: talk/visit in the real city → `enter` an instance for the dangerous part →
  return to the city for the conclusion. The exploration layer stays the spine; the instance is the
  set piece.
- Objectives inside an instance are ordinary steps (gather/hunt/discover) scoped to the instance.
- Loot from instances is the reward for the random layer; it must not be required for world
  progression, so a bad roll never soft-locks a chain.

## Dungeons as portals

A dungeon is a **separate dimension**, and its entrance is a **portal built like a Nether portal**:
a frame of themed blocks, ignited once, opening a plane of coloured portal blocks. The Nether
portal is the right mental model — players already know how to read a frame and a lit plane — but
each dungeon family has its own materials, colour and igniter, so the frame itself tells the player
what lies behind it and how bad an idea this is.

| Portal | Frame | Plane | Igniter | Dungeon |
|---|---|---|---|---|
| Catacombs | mossy stone bricks + bone blocks | pale green | funerary candle | crypts, ossuaries; low tier |
| Drowned | prismarine + sea lanterns | cyan | briny pearl | flooded temple; mid tier |
| Tomb | sandstone + chiselled sandstone | amber | sun scarab | desert tomb; low tier |
| Vault | deepslate tiles + amethyst | violet | void shard | sealed vault; high tier |
| Whisper | blackstone + crying obsidian | indigo, dim | whisper sigil | dream archive; high tier, only hinted by whispers |

- Frame shape and ignition reuse the Nether-portal reading: a rectangular frame (4×5 minimum,
  larger frames open a larger plane), lit with the family's igniter, extinguished by breaking the
  frame. No coordinates are ever shown.
- The portal plane is its own block per family, with the family's colour and animation. Standing in
  it for the usual portal delay teleports a player (only players; no mobs, no trains, no ships) to
  the session instance for that family.
- The return plane is placed at the instance's entrance and returns to the frame the player used.
- Some frames exist in the world already (the real catacombs under Paris, a drowned gate on a
  shore); others are craftable from the family's materials plus a rare igniter. Ill-advised, but
  allowed.
- Instances stay session-scoped: the portal is permanent, the dungeon behind it is rerolled every
  session, and closing the instance leaves the frame intact.

### Whispers

The sanity system already plays `AMBIENT_WHISPER` at the MARKED band. That becomes the diegetic
hint channel:

- A player below the FRAYING band starts hearing **actionable** whispers: a place name and a
  direction to a portal ("the black gate behind the church at Les Halles…"), shown as the usual
  locator-bar dot, never coordinates.
- Whispers are **unreliable by design**: a share of them are true, some are stale, some point at a
  hazard. The lower the sanity, the more whispers and the higher the chance one is true — madness
  makes you useful and dangerous at the same time.
- The Whisper-family portal (indigo) is only ever found through whispers; its frame is not placed
  in the world otherwise.
- A sane player can still find the other frames by exploring; whispers are a shortcut with a price,
  not the only path.
- Whisper text is generated from the dungeon seed, so within a session the same whisper always
  names the same portal.

### Risk and reward

Dungeons are the "dubious reward" layer:

| | |
|---|---|
| Danger | generated traps and enemies scaled to the family tier; a local dread that drains sanity inside the instance |
| Reward | rare reagents/artefacts and lore, rolled from a table with a real chance of a poor or empty haul |
| Cost | the trip costs sanity and supplies; leaving early is always allowed |
| Guardrail | nothing required for world/quest progression is dungeon-only, and no Ancient One spawns inside |

The deal the portal offers is deliberately bad on average and excellent occasionally: the player
gambles time, sanity and gear for a chance at something the city cannot give.

## Constraints

- No permanent block edits in the world from instance content.
- No instance-only NPCs that must persist.
- Instances must be safe to abandon: close on empty, no lost quest items (re-issuable).
- Server cost is bounded: at most one open instance per party, unloaded when empty.
