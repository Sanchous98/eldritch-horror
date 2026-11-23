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
great-circle distance (id breaks ties), then keeps the nearest candidates whose **coarse A\***
route fits inside `trainMaxLineBlocks`. Up to **`maxPartnersPerCity`** partners are kept per city;
each partner becomes one real, block-level line and one direction pair. Pairs are deduplicated, so
A→B and B→A do not produce two lines.

A pair's reachability is checked once and reused when the second city considers the same
candidate. Candidate distances are sorted, so the search stops at the first candidate beyond
`trainMaxLineBlocks` instead of continuing through the list.

Stations are still terrain-sampled by `RailStations.place`, one per partner, facing the partner's
bearing. A city with an authored `STATION` anchor reuses that terminal for every direction; a city
without one gets a platform per partner on its station ring. A city with no reachable partner still
gets a dormant station, exactly as before. Station map keys are `cityId|partnerId`, so every
line endpoint resolves to a unique station entry (identical sites share one physical platform).

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

To see them in an existing world either:

- reset the world (delete the `run/world` volume / save and let chunks regenerate), or
- pre-generate the rail corridors: `/eh pregen rail` (or `-Deh.pregen=rail` at boot).

Sea lanes are pure runtime paths and need no worldgen step.

## Debug probes

- `/eh train` — nearest station, each line's next departure, live consists, watchdog state.
- `/eh ship` — nearest port, each lane's next departure, live hulls.
- Startup logs: `rail: N lines from P pairs, S stations in T ms` and
  `transport: N sea lines in T ms`.
