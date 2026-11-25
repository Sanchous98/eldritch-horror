# Map package: the mod works without a built-in map

The mod must run on any world: the vanilla-ish Earth map with curated city plans, a
prebuilt Arnis city package, or a plain flat world with no cities at all. Cities are
data, not code.

## Model

A **map package** is a small JSON file that ships *outside* the mod jar (world folder or
config). It lists the cities and the anchors the systems need:

```json
{
  "version": 1,
  "name": "earth-1to1",
  "cities": [
    {
      "id": "paris",
      "name": "Paris",
      "x": 0,
      "z": 0,
      "radius": 5600,
      "population": 2100000,
      "station": {"x": 0, "y": 65, "z": -3100, "axis": "north_south"},
      "port":    {"x": 0, "y": 63, "z": 0, "water_y": 63},
      "landmarks": [
        {"name": "Eiffel Tower", "x": -4226, "z": -199}
      ]
    }
  ]
}
```

- `x`, `z` — the city centre in blocks; `radius` — the built footprint (the systems use it
  for "inside the city" checks: sanity, decay, roaming Ancient One exclusion).
- `station` / `port` — where the transport network puts the platform or the berth. A
  prebuilt city does not get a generated station: the package says where it is.
- `landmarks` — authored names and positions used by contracts and the locator bar.
- Anything missing falls back to the current behaviour (plan scan / ring search).

## Loading

1. On server start, look for `config/eh-map.json`, then `<world>/eh-map.json`.
2. Parse and validate: unique ids, positive radius, coordinates inside the world border.
3. Install the city list into `Cities`; each city's `radius()` returns the package value.
4. If no package is found, keep the baked settlements table — the mod's default map.

## World geometry

The package never contains blocks. The world itself is built offline:

- `arnis` generates each city at 1:1 into its own world;
- `merge_worlds.py` relocates those chunks to the city's `x, z` (see `paris/merge_all.sh`);
- `layout.json` is the source of truth for the positions, and `eh-map.json` is generated
  from it plus the station/port/landmark table.

Resetting the map is therefore "delete the world, rebuild it from the package", not a
regeneration run: 30–40 minutes for all cities, ~10 minutes for one.

## Fallbacks

- No package, no cities: quests, transport and sanity simply have no curated cities to
  attach to; the rest of the mod (items, mobs, bosses by site, rifts) still works.
- A package city whose chunks are missing (not merged yet): the systems still know where
  the city *is*, and the world generator fills the gap with wilderness. Walking there is
  an empty field, not a crash.
