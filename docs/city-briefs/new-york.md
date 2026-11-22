# New York — authored city brief

Footprint `halfExtent = 340`. Coordinates relative to `(cx, ground, cz)`; no layout RNG.

## Identity

Prohibition-era New York: the Empire State's stepped tower, the Manhattan grid, the docks and
warehouses, Harlem's clubs, and the subway. Steel, brick, steam, and jazz.

## Macro layout

| District | X | Z | Character |
|---|---|---|---|
| Midtown | -140..140 | -300..-100 | Empire State, offices, theatres, hotels |
| Docks | 140..320 | -260..160 | piers, warehouses, cranes, rail |
| Harlem | -320..-140 | -240..180 | brownstones, clubs, churches, markets |
| Downtown | -120..120 | -80..180 | banks, city hall, shops, subway |
| Subway | -100..100 | 80..160 | station, tunnels, secret junction |
| Central Park | -320..320 | -340..-300 | trees, paths, lake edge |

**Arrival gate**: south `(0, 320)`; signpost to Midtown, docks, Harlem, downtown.

**Streets**: 14-wide avenues; cross streets 8 wide; dock road 12 wide; Harlem lanes 6 wide.

## Landmark

**Empire State** at `(0, -200)`: a stepped Art-Deco tower with a mast and observation deck, on a
stone base with a lobby.

## Required POIs

| POI | Kind | Position | Content |
|---|---|---|---|
| `midtown_quest_board` | QUEST_BOARD | `(0, -120)` | two local quests |
| `hotel` | INN | `(-80, -60)` | inn, keeper, bed |
| `midtown_shop` | SHOP | `(40, -60)` | shop, merchant |
| `apothecary` | HEALER | `(-240, 120)` | healer, remedies |
| `newsroom` | NPC | `(-240, -120)` | missing reporter |
| `speakeasy` | QUEST | `(240, 40)` | bootlegger |
| `subway_junction` | SECRET | `(0, 120)` | tunnel, hidden room |
| `empire_state` | LANDMARK | `(0, -200)` | tower |
| `docks` | LANDMARK | `(240, -120)` | piers, warehouses |

## NPCs

`hotel_clerk`, `shopkeeper`, `apothecary`, `editor`, `bootlegger`, `cop`, `jazz_singer`.

## Local quests

1. **The Missing Reporter** (`new_york_missing_reporter`): a reporter vanished chasing a dock
   story. Trace him through the speakeasy and subway; rescue him or take the story.
2. **The Bootlegger** (`new_york_bootlegger`): a bootlegger wants a shipment moved. Follow it
   from the docks to the subway; help him, expose him, or take the cargo.

## Signs

`EMPIRE STATE`, `DOCKS`, `HARLEM`, `DOWNTOWN`, `SUBWAY`, `CENTRAL PARK`, `CITY NOTICES`,
plus dock and tunnel warnings.

## Atmosphere

Steel, brick, steam, neon, jazz clubs, brownstones, piers, taxis, fire escapes, a few boarded
shops, pigeons.
