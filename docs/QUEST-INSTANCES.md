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

## Constraints

- No permanent block edits in the world from instance content.
- No instance-only NPCs that must persist.
- Instances must be safe to abandon: close on empty, no lost quest items (re-issuable).
- Server cost is bounded: at most one open instance per party, unloaded when empty.
