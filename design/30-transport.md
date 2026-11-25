# 30 — Scheduled transport (ships, trains)

The world is a cylinder 131 072 blocks around; walking or sailing across an ocean is hours. Fast
travel must exist, but it must be **a real journey**, not a teleport: the player boards a large
vehicle, waits for its scheduled departure, and rides while the world moves past.

This supersedes the "network form TBD" note in `23-boundary-and-travel.md` for the mundane branch.
The occult branch stays the rift gate (`26-rituals-and-occult.md`) — fast, tainted, personal.

## Principles

- **Real vehicles.** A ship is a large floating entity several blocks long with several seats; a
  train is a locomotive with coupled wagons on real rails. No screen-fade teleports.
- **Timetable, in game time.** Each line has a list of departure times (e.g. six a day for trains,
  four for ships) and a period (daily / every N days); neighbouring lines are staggered so stations
  are not synchronised. Boarding opens one in-game hour before departure and closes when the
  vehicle leaves. Missing it means waiting for the next service. Implementation and config:
  [`docs/TRANSPORT.md`](../docs/TRANSPORT.md).
- **Fares and capacity.** A ticket costs coin (the existing currencies); seats are limited. A line
  can be free for a cult/quest that owns it (later).
- **Real time on the water.** The voyage takes real time proportional to the distance and the
  vehicle's speed; the player can walk the deck, fish, be attacked, or log off (the latter ends the
  ride at the next port).
- **Bounded streaming.** The vehicle holds chunk tickets only around itself (a few chunks), never
  the whole route. Chunks behind unload. Empty services do not sail (no riders → the departure is
  skipped) so an idle server never streams oceans.
- **Danger is part of the trip.** Weather and rifts can delay a ship; a transit can be interrupted.
  The mundane route is cheap but slow and exposed; the rift gate is fast but marks you.

## Ships (stage 1)

- **Ports** are the curated coastal cities (`EarthMap.isCoastal`); a port is a point on the water
  side of the city.
- **Sea lane**: an A* path over the baked landmask at a coarse resolution, kept off the coast by a
  safety margin, smoothed into waypoints. Cached per route; no per-tick pathfinding.
- **The ship** is a 91-block ocean liner: one plain entity (no boat buoyancy, no item drop) whose
  hull AABB is the deck floor at 7.25 blocks above the sea, with an invisible collision part per
  deckhouse chunk and one bridge part above it. The main deck (and the promenade roof) is walkable
  while under way, and standing players are carried with the hull. 32 deck seats, a stepped gangway
  on the starboard side, three funnels, two masts and a large custom model/texture. Server-driven
  movement along the lane at a configured speed; it turns to face the next waypoint.
- **Boarding**: right-click the ship (or the gangway) while it is docked in the boarding window.
  Pay the fare, take a seat. At departure the ship sails; at arrival riders disembark at the
  destination port and the ship despawns.
- **Schedule**: each line departs daily at configured times; a line exists only between two ports
  with a valid sea lane.

## Trains (stage 2)

- **Stations** at inland cities; a station is a platform beside the line, with a boarding point.
- **Rail lines** are generated along a smoothed spline between stations: ballast, sleepers, rails;
  bridges across water and short tunnels through hills. Generation is deterministic from the two
  station ids and bounded to the chunks being generated (no long force-loads). The line is real
  blocks: players can walk it, build beside it, or block it.
- **Consist**: a locomotive plus 2–4 wagons, as **coupled entities**. The locomotive owns the path;
  each wagon samples the leader's recent trail at a fixed spacing (so the consist bends around
  curves instead of clipping through them). Each car is a full carriage (14 blocks for a wagon,
  16 for the locomotive, 3 wide and 3.8 tall) with 12 window-facing seats; players board at a
  station and ride; the train keeps to the line, slows into a station, dwells for a boarding
  window, and departs on the timetable.
- **Same timetable/fare stack as ships**: in-game departure times, boarding window, tickets, and a
  real-time journey proportional to the line length. No teleport.
- **Safety and interruption**: the line can be blocked (a placed block stops the train until
  cleared/removed by the crew), and the same transit dangers as the sea route apply. A derailed or
  stopped train lets riders disembark where it stands.
- **Look**: custom locomotive and carriage models at real scale, with walk-in interiors, window
  bands, benches and doors; see `docs/TRANSPORT-VEHICLES.md`.

## Open questions / defaults

- Ship speed (default 1.5 blocks/tick ≈ 30 b/s): a 5 000-block leg ≈ 3 min, a full ocean crossing is
  a long voyage by design.
- Fares: 2 `black_obol` per leg (default).
- Do ships sail with no riders? No (streaming cost); the timetable is advertised at the port.
- Trains need a rail-line generator; that is a separate content project after the ship proves the
  schedule/boarding/streaming stack.
