# Content packs: the mod is an engine, the map is content

The mod must run on any world. Everything that describes *this* world — which cities exist,
where the station is, what the quests are — belongs to a content pack, not to the jar.

## Split

| Layer | Ships in | Contains |
|---|---|---|
| **Mod (engine)** | the jar | items, blocks, entities, bosses, sanity, corruption, transport *mechanics*, quest *interpreter*, waypoint packets, advancement *hooks*, rendering, configs |
| **Content pack** | `config/eh-map.json` (or `<world>/eh-map.json`) | cities, station/port anchors, landmarks, quest chains, NPC placements, site spawns |
| **World geometry** | the world folder | the generated chunks (Arnis cities + merged wilderness) |
| **Default pack** | the jar's resources | the current 24-city layout and quest lines, used when no pack is installed |

A world with no pack still works: the engine has nothing to attach quests to, but items, mobs,
boss fights by site, rifts, sanity and transport physics all keep running. A pack without
geometry also works: the systems know where things *are*, the chunks are just wilderness.

## Quest packs

The engine keeps the mechanics; the pack keeps the script.

```json
{
  "quests": [
    {
      "id": "paris.relic",
      "city": "paris",
      "title": "The Relic of Saint-Denis",
      "chain": [
        {"type": "talk",   "contact": "curator", "place": "the Louvre"},
        {"type": "travel", "place": "Notre-Dame", "dx": -167, "dz": 398},
        {"type": "gather", "item": "eldritch_horror:relic_shard", "count": 3},
        {"type": "hunt",   "entity": "eldritch_horror:worshipper", "count": 1},
        {"type": "return", "contact": "curator"}
      ],
      "reward": {"obols": 40, "advancement": "eldritch_horror:cities/paris"}
    }
  ]
}
```

- Step types are an enum in the engine (`talk`, `travel`, `gather`, `hunt`, `return`,
  `discover`); unknown types are skipped with a warning, so a newer pack cannot crash an
  older mod.
- `place`, `dx`, `dz` drive the contact dialogue and the locator-bar dot. Coordinates are
  optional: without them the engine resolves the nearest landmark of the pack.
- A pack may override the default chain for a city, add chains to a city, or replace the
  whole quest list with `"replace": true`.

## Why

- A world author (or a server admin) can write a Paris quest line about the Louvre without
  touching Java.
- The 1:1 map can carry its own story, and a different map can carry a different one.
- Updates stay independent: new quests ship as a JSON edit, not a mod release.

## Status

- Cities, station/port anchors and landmarks: implemented (`MapPackage`).
- Quest packs: the schema above is fixed here; the interpreter still builds the default
  chains in `CityQuests`, and the loader is the next step. Until then the engine behaves
  exactly as today when no pack is present.
