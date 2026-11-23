# Stations and harbours for the large vehicles

This is the transport-side companion to the large train and packet work. The carriage is
`3.0 x 3.8` and the consist is much longer than the first generation of stations allowed, so the
station platform, its head-room, and the harbour berth are all sized from these numbers:

| vehicle | length | width | height |
|---|---|---|---|
| locomotive | 16 | 3.0 | 3.8 |
| wagon | 14 | 3.0 | 3.8 |

`TrainTimetable.SPACING = 16.0`, `trainCars` defaults to **3** (2–4 allowed). A default consist is
therefore **64 blocks** nose-to-tail (16 + 3 × 16); the four-wagon maximum is **80 blocks**.

## Targets

### Station

* **Platform length 86 blocks** (80 back from the station point for the four-wagon consist, plus a
  6-block front apron). The hard minimum is 64; 80 is covered.
* **Five blocks wide**, with the **near edge three blocks from the rail centreline** (columns 3–7
  across; the carriage half-beam is 1.5).
* **Five blocks clear above the rail head** (`rail+1 … rail+5`) everywhere, including the station
  corridor, bridges and tunnels. The platform floor is one course below the rail, so its top is at
  rail level: the carriage floor is 0.5625 above its attachment, so the platform is half a block
  below the floor — riders step up onto the sill. A slab would put the teleport point (rail level)
  inside the slab, so the full-block floor is deliberate.
* **Canopy at rail+6** over the platform, with pillars every 8 blocks on the outer edge and a warm
  `minecraft:lantern` under the canopy.
* A **sign** (`CITY STATION`) at the front of the platform and a **buffer stop** at the rear end.
  The buffer is stamped before the line, so a real line crossing it clears it.
* The platform is built **on the platform side of the station point**, i.e. the side of the
  authored `STATION` anchor, and extends **behind the station point**, away from the partner. A
  train spawns nose-first at the station point and trails its wagons behind it; the reverse
  direction would run the platform through the terminus building.

### Harbour

* A berth of at least **12 wide × 24 long**, with **water four courses deep**. The packet is
  6 × 18 and needs room to turn; authored water bodies are used as-is, so most are much larger.
* A quay edge at least 24 long, bollards every 8 blocks and a lit quay (lanterns / sea lanterns).
* The **berth surface Y** is the hull's own water level, not the world sea level.

## How the authored anchor becomes authoritative

### Rail

`CityPoi.Kind.STATION` is the station site. `RailStations.authoredStation`:

1. reads an explicit **`north_south`** or **`east_west`** token from `CityPoi.data()`. Every
   `STATION` POI in all 24 plans now carries one; the old bearing fallback is only a last resort.
2. runs the plan once, headlessly, into `CityPlanProbe` (bounded to 32 blocks around the anchor).
3. snaps the rail point to the **nearest authored `Blocks.RAIL`** in that box, so the generated
   platform and line sit on the plan's own track even when the anchor is on the platform or apron.
4. takes the base side from the anchor's side of the rail, and the **outbound sign** from the
   partner's direction. The generated platform is then built on that side.

The `CityPlanProbe` is a deterministic recorder: it replays the plan's block primitives into a
`HashMap` without touching chunks, so no `CityLocation` generation behaviour changes and no
random seed is introduced.

### Sea

`Ports.authoredHarbour` is now authoritative for a city with a `PORT`:

1. runs the plan headlessly around the anchor (96-block box);
2. finds the connected water body next to the anchor at its **modal top course**;
3. picks a berth point with open water on all four sides (a hull is six wide and eighteen long, so
   the quay wall itself cannot be the spawn point; this also avoids a column under a pier or barge);
4. records `berth.y()` = top water + 1 in `Port.waterY`.

The 32-direction ring scan over the baked landmask is only the fallback for a city whose plan has
no `PORT` (or whose plan authored no water and whose map has no reachable sea). The old
`MAX_OFFSET_BLOCKS = 320` cap still applies only to that fallback; the authored anchor path reaches
512 blocks, which is what brings Bangkok (344) and Istanbul (326) in.

`ShipService` spawns at `origin.waterY() + 0.85` instead of `level.getSeaLevel() + 0.85`. This is
what puts Mexico City's canal basin at its authored Y (~119, not 63) and keeps a coastal basin that
is dug below sea level at its own surface.

## Per-city numbers

The station columns come from the headless plan probe at the city's authored layout; the worldgen
platform is identical for every city (86 × 5, edge 3, clearance 5, canopy rail+6, sign, lamp,
buffer). The berth is the authored water body near the `PORT` anchor; `Y` is the real authored
water surface, the level the packet now spawns at. The berth point and body size are measured in
the plan; only the ship's 12 × 24 × 4 slot is required.

| city | station anchor (dx,dy,dz) | axis | authored platform | berth L×W×D @ Y |
|---|---|---|---|---|
| jakarta | 3, 1, −300 | north_south | 41 × 6, edge 3, clear 5 | 374 × 78 × 4 @ 70 |
| shanghai | 0, 1, −310 | north_south | 20 × 8, edge 1, clear 5 | 681 × 36 × 4 @ 60 |
| seoul | 220, 1, −199 | north_south | 33 × 8, edge 1, clear 5 | — |
| beijing | 220, 0, 286 | east_west | 49 × 8, edge 1, clear 5 | — |
| tokyo | 260, 2, −30 | east_west | 49 × 8, edge 1, clear 5 | 27 × 201 × 4 @ 66 |
| bangkok | 315, 1, 75 | east_west | 31 × 8, edge 1, clear 5 | 481 × 661 × 4 @ 63 |
| delhi | 262, 0, 120 | north_south | 49 × 6, edge 1, clear 5 | — |
| nairobi | 258, 0, 0 | north_south | 49 × 7, edge 1, clear 5 | — |
| cairo | 136, 0, 324 | east_west | 25 × 8, edge 1, clear 5 | 421 × 79 × 4 @ 67 |
| moscow | 200, 0, 288 | east_west | 49 × 8, edge 1, clear 5 | — |
| paris | 66, 1, 312 | north_south | 31 × 8, edge 1, clear 5 | — |
| rome | 300, 1, 124 | east_west | 45 × 8, edge 1, clear 5 | 31 × 661 × 4 @ 73 |
| lagos | −116, 1, 305 | north_south | 19 × 8, edge 1, clear 5 | 441 × 77 × 4 @ 64 |
| mumbai | 320, 1, −21 | east_west | 38 × 8, edge 1, clear 5 | 87 × 120 × 4 @ 68 |
| istanbul | −134, 1, 275 | north_south | 34 × 8, edge 1, clear 5 | 181 × 81 × 4 @ 67 |
| london | 160, 1, −247 | east_west | 35 × 7, edge 1, clear 5 | 207 × 61 × 5 @ 65 |
| cape_town | 196, 1, 326 | east_west | 33 × 8, edge 1, clear 5 | 429 × 35 × 4 @ 69 |
| sydney | 210, 1, −36 | east_west | 49 × 8, edge 1, clear 5 | 123 × 391 × 4 @ 68 |
| new_york | 172, 2, −225 | east_west | 2 × 7 (see gap) | 181 × 61 × 5 @ 66 |
| los_angeles | 175, 0, −265 | east_west | 49 × 8, edge 1, clear 5 | 28 × 26 × 4 @ 64 |
| rio_de_janeiro | 120, 1, 335 | east_west | 45 × 8, edge 1, clear 5 | 559 × 34 × 4 @ 71 |
| buenos_aires | 228, 1, 155 | east_west | 49 × 8, edge 1, clear 5 | 346 × 81 × 4 @ 63 |
| lima | 230, 1, −210 | east_west | 5 × 8, edge 1, clear 5 | 501 × 33 × 4 @ 111 |
| mexico_city | 120, 0, 305 | east_west | 45 × 8, edge 1, clear 5 | 371 × 68 × 4 @ 124 |

The authored-platform run/width is the headless sanity measure (a five-wide floor with five clear
above at least one column). The ride platform is the generated 86 × 5 platform in every city; the
authored platform is what the city visual keeps. `new_york`'s authored goods platform is physically
present but partly cut by the shed/embankment in the headless probe, so its measured run is short;
the 86-block worldgen platform is the working one. `lima`'s harbour is the plan's cliff-top Callao
basin (Y 111), which is why the authored basin is far above the ocean.

## What changed in code

* `world/rail/RailLines.java`: platform 86 long, five wide, edge 3, canopy rail+6, pillars, lamps,
  sign and buffer; one-side, behind the consist.
* `world/rail/RailStamp.java`: new station kinds (canopy, pillar, sign, buffer, clear) and a sign
  label.
* `world/rail/RailWorldgen.java`: five-clear clearing for track, bridge, tunnel and platform;
  tunnel ceiling at rail+6; station canopy/sign/lamp/buffer writes; sign text queued through
  `CitySigns`.
* `world/rail/RailStations.java`: explicit axis keyword, authored-rail snap, anchor-side boarding
  point, outbound sign.
* `world/rail/RailStation.java`: `outSign`.
* `world/loc/city/plan/CityPlanProbe.java`: deterministic headless plan recorder and berth finder.
* `world/transport/Port.java`: `waterY`.
* `world/transport/Ports.java`: authored berth authoritative, real berth Y, reach 512 for the
  authored fallback, ring scan only for cities without a `PORT`.
* `world/transport/ShipService.java`: spawn at `waterY + 0.85`.
* All 24 `*Plan.java`: axis keyword and anchor placement; platforms widened / canopies raised
  where the old ones were too low or too narrow; basins deepened to four courses; new basins for
  Bangkok, Los Angeles and Cape Town's south platform; New York/London platforms.
* `build.gradle`: enables the NeoForge unit-test fixture (Minecraft classes on the JUnit
  classpath) and keeps the test working directory at the repository root.

## Verification

`src/test/java/.../world/rail/StationHarbourGeometryTest.java` is the gate. It records every plan,
asserts the `STATION` axis, the authored rail within 32 blocks, a five-wide authored platform with
five clear above it, and the generated platform (86, five wide, edge 3, clear 5, canopy +6, sign,
lamp, buffer). For every city with a `PORT` it asserts a berth of at least 12 × 24 × 4 and prints
the per-city numbers to `build/station-harbour-report.txt`.

```
./scripts/safe-build.sh bash -lc 'docker compose build dev >/dev/null && \
  docker compose run --rm dev ./gradlew build --no-daemon --console=plain'
```

The report was generated by that suite. Only newly generated chunks show any of this: existing
worlds keep their old platforms, sheds and basins until the city/harbour chunks regenerate.

## Known gaps

* **Inland/river ports and sea lanes.** `ShippingLines` still plans over the baked landmask, so a
  berth on an authored basin that is land in the map (Mexico City, the Lima cliff, the Rome/Bangkok
  rivers) can have no sea route even though its spawn Y is now correct. Partner selection, fares
  and schedules were out of scope and are unchanged. A coastal basin that overlays map ocean is
  unaffected.
* **The generated platform palette is neutral stone brick.** Authored cities keep their own
  quays, sheds and signs, but the worldgen platform/canopy is a shared transport material, not a
  per-city palette.
* **Authored props inside the station corridor.** The five-clear pass removes low lamps, pillars
  or crates inside the platform/track corridor; the generated canopy and lanterns replace them. The
  authored cranes, warehouses, signage and high roofs are kept.
* **New York's authored goods platform** is a real platform, but the headless probe's run metric is
  short because the goods shed/embankment occupies much of its footprint; the generated 86-block
  platform is the one a rider uses.
* **Multi-partner terminals** share one station point. The platform is built on the platform side
  of the first placed partner; a line to a partner in the opposite direction can have its consist
  trail on the unplatformed side. The locomotive still stops alongside the platform.
