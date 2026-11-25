# Scheduled transport

How the mundane travel network is built and when its vehicles run. The design intent is in
[`design/30-transport.md`](../design/30-transport.md); this page covers the implementation and the
config you can change.

Two services share one timetable stack:

- **Ships** (stage 1) — coastal packets on A* sea lanes between authored harbours.
- **Trains** (stage 2) — locomotive + wagon consists on real rail blocks between curated cities.

Both are **real journeys**, not teleports. Fares, speeds, capacities, boarding windows and the
empty-service despawn rule are unchanged by the network-density work.

The train's carriages, seat layout and custom models are described in
[`TRANSPORT-VEHICLES.md`](TRANSPORT-VEHICLES.md).

## Line selection

Routes are computed once on first use and cached for the server's lifetime; a tick never
pathfinds.

### Rail

`world/rail/RailLines.java` walks the curated cities. For each city it sorts every other city by
distance (id breaks ties), then keeps the nearest candidates whose straight-line distance fits
inside `trainMaxLineBlocks` — up to **`maxPartnersPerCity`** partners per city; each partner
becomes one real line and one direction pair. Pairs are deduplicated, so A→B and B→A do not
produce two lines. Selection itself never runs A*: the route is planned once when the line is
built, so a path budget cannot silently drop a pair.

Distances and block paths follow the installed city table. Cities that fit inside the baked Earth
layers keep the old **cylinder** geometry (longitude wraps at ±65,536). A 1:1 map package reaches
past those layers (Sydney is at `z = -181,682`), so its coordinates are treated as a **plane**: the
short way round the antimeridian is not assumed, and station offsets are measured in the same
frame.

#### Two-level route planner

`world/rail/RailPlanner.java` plans every line in two levels:

| Board | Cell | Extent | Purpose |
|---|---|---|---|
| intercity | 512 blocks | all curated cities plus a 16 km margin | the long middle of a 16–280 km route |
| station approach | 64 blocks | a 6 km square around each station | the last three kilometres into the platform |

The coarse board keeps a 180 km route at a few hundred A* cells instead of thousands, and its
expansion budget scales with the route length (`max(600k, route cells × 4,000)`), not with the old
fixed six-hundred-thousand that was tuned for one-kilometre hops. A route first tries a strict
search (short bridges only); if that fails, it retries with the water-run limit raised to the whole
route. The fine board is sampled only near the endpoints, so the whole world is never held at
64-block resolution.

#### Corridor budget

A line longer than **`trainMaxCorridorBlocks`** (default 40,000 blocks) is still a line — stations,
timetable and consist — but its worldgen corridor is **not** stamped. This is deliberate: a 1:1 map
cannot pre-generate every intercity corridor. A 180 km corridor crosses roughly 11,000 chunks, and
the chunk tickets of a single train ride would generate them all, which is exactly what made the
earlier pregen untenable. The consist travels the planned polyline and the ground is whatever the
world generator produced; the locomotive does not demand a rail block under such a line (it still
stops for a player-placed wall). Stamped corridors remain real rails, ballast, bridges and tunnels,
and are still worldgen-only: they appear in newly generated chunks.

The result is a sparse graph rather than every city pair. A city gets one station per partner (a
major city's authored terminal can serve several directions); a city with no partner still gets a
(dormant) station platform. Stations are still placed by `RailStations.place`, one per partner,
facing the partner's bearing. A city with an authored `STATION` anchor reuses that terminal for
every direction; a city without one gets a platform per partner on its station ring. Station map
keys are `cityId|partnerId`, so every line endpoint resolves to a unique station entry (identical
sites share one physical platform).

### Sea

`world/transport/ShippingLines.java` first builds the authored `PAIRS` (including the trans-Pacific
bridges), then adds **nearest reachable neighbours** for every harbour, up to the same
`maxPartnersPerCity` cap. Authored lanes count toward the cap. A lane that A\* cannot find, or that
still touches land, is absent — "no lane, no line".

Both selectors are deterministic: curated input order, distance sorted with a stable id tiebreak,
and the same A* grid code as before.

## Schedules

A direction's timetable is a list of **24-hour clock hours**, not a single pair of hours:

| Config | Default | Meaning |
|---|---|---|
| `shipDepartureHours` | `00,06,12,18` | four ship departures per direction per service day |
| `trainDepartureHours` | `04,08,12,16,20,00` | six train departures per direction per service day |
| `shipPeriodDays` / `trainPeriodDays` | `1` | days between service days |
| `shipBoardingMinutes` / `trainBoardingMinutes` | `60` | boarding opens this many in-game minutes before departure |

The clock is the vanilla day clock: raw `0` is 06:00, so the timetable moves with `/time set`.
Voyages run on real ticks once started.

### Consists and chunk tickets

A consist is spawned only when a player is within **256 blocks** of its origin station. Previously
every line spawned an empty consist twice a service day: with 49 lines that filled `maxTrains`
with cities nobody was in, and a station that did have a waiting player got no train at all. The
gate is only for spawning; a consist already under way is unaffected if its riders leave the area.

Trains rely on vanilla chunk tickets, and vanilla **deduplicates identical tickets instead of
reference-counting them**. Two consequences shaped this code:

- The station hold is counted per chunk (`TrainService.HELD`). Several lines can leave from one
  terminal, and the first line to release its hold must not unload the chunk for the others.
- The station hold and the consist's own hold use different ticket radii (5 and 4), so they are
  distinct tickets. The locomotive keeps a 5×5 ticket on the middle carriage for the whole voyage;
  a chunk is entity-ticking only when its ticket level is at most 31, and a radius-2 ticket ticked
  the middle chunk alone, leaving the locomotive and wagons frozen in the neighbouring chunks.

### Stagger

Each line **and direction** shifts its hour list by a stable phase derived from
`<lineId>:<direction>` (`String.hashCode`, which is specified by the Java language). The phase is
bounded by one service interval — up to 3 h for a six-a-day train, up to 5 h for a four-a-day ship —
so the configured frequency is preserved while neighbouring stations no longer open their boarding
windows together. The same line always keeps the same phase after a restart.

Boarding calls and `/eh train` / `/eh ship` show the real departure time, not the configured hour.

### Adding or changing hours

Edit the server config (`config/eldritch_horror-server.toml`), for example:

```toml
trainDepartureHours = "02,06,10,14,18,22"
shipDepartureHours = "03,09,15,21"
```

- Values are `0..23`, comma separated. Spaces are fine.
- Duplicates and out-of-range or malformed entries are ignored.
- If nothing usable remains, the built-in default list is used.
- The phase is applied per line, so a list of six hours still yields six departures a day per
  direction.

## Seeing a new network

The rail network is **worldgen**: `RailWorldgen` stamps rails, ballast and station platforms into
chunks as they generate. Adding partners therefore only creates the new corridors in **newly
generated chunks**. In an existing world the new lines exist in the network and timetable, but
their blocks are absent until those chunks are generated for the first time.

Only lines at most `trainMaxCorridorBlocks` long have a stamped corridor. A long intercity line is
planned and run but never stamped, so there is nothing to generate for it beyond the chunks a
train actually visits.

To see the short corridors in an existing world either:

- reset the world (delete the `run/world` volume / save and let chunks regenerate), or
- pre-generate the rail corridors: `/eh pregen rail` (or `-Deh.pregen=rail` at boot). Pregen uses
  the same chunk index, so it skips the unstamped long lines automatically.

Sea lanes are pure runtime paths and need no worldgen step.

## Coverage gate

`RailNetworkCoverageTest` builds the network headlessly over the real 1:1 package and layout
(`src/test/resources/rail/eh-map.json` and `layout.json`, copied from `paris/worlds`), asserts at
least 30 lines and no isolated city, and writes the per-city partner table to
`build/rail-network-report.txt`. It also asserts that the package coordinates still agree with the
layout, so the gate cannot drift onto a miniature synthetic map.

## Debug probes

- `/eh train` — nearest station, each line's next departure, live consists, watchdog state.
- `/eh ship` — nearest port, each lane's next departure, live hulls.
- Startup logs: `rail: N lines from P pairs, S stations in T ms` and
  `transport: N sea lines in T ms`.
