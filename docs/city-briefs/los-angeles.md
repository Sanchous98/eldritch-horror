# Los Angeles — authored city brief

Footprint `halfExtent = 340`. Coordinates relative to `(cx, ground, cz)`; no layout RNG.

## Identity

1930s Los Angeles: the Spanish mission church, Hollywood's studios and boulevards, downtown's
offices, the oil fields, and the beach. Stucco, palm trees, neon, dust, and sun.

## Macro layout

| District | X | Z | Character |
|---|---|---|---|
| Mission | -140..140 | -300..-100 | church, plaza, gardens, cloister |
| Hollywood | -320..-140 | -240..160 | studios, boulevard, theatres, cafés |
| Downtown | -120..120 | -80..180 | offices, banks, shops, hotel |
| Oil fields | 140..320 | -260..120 | derricks, tanks, sheds, rail |
| Beach | 140..320 | 160..320 | pier, pavilion, beach huts, promenade |
| Studio basement | -240..-160 | 40..120 | secret set, cellar |

**Arrival gate**: south `(0, 320)`; signpost to mission, Hollywood, downtown, oil, beach.

**Streets**: 14-wide boulevard; Hollywood streets 10 wide; downtown grid 8 wide; oil roads 6;
beach promenade 8.

## Landmark

**Spanish mission church** at `(0, -200)`: white stucco nave, terracotta pitched roof, bell
tower, arcaded cloister, and a palm courtyard.

## Required POIs

| POI | Kind | Position | Content |
|---|---|---|---|
| `hollywood_quest_board` | QUEST_BOARD | `(-220, 0)` | two local quests |
| `hotel` | INN | `(-80, -60)` | inn, keeper, bed |
| `hollywood_shop` | SHOP | `(-240, -60)` | shop, merchant |
| `apothecary` | HEALER | `(-200, 120)` | healer, remedies |
| `studio` | QUEST | `(-240, 80)` | missing starlet |
| `oil_office` | NPC | `(240, 0)` | cult studio |
| `studio_basement` | SECRET | `(-200, 80)` | cellar, hidden set |
| `mission` | LANDMARK | `(0, -200)` | church |
| `beach_pier` | LANDMARK | `(240, 240)` | pier, pavilion |

## NPCs

`hotel_clerk`, `shopkeeper`, `apothecary`, `director`, `oilman`, `lifeguard`, `cop`.

## Local quests

1. **The Missing Starlet** (`los_angeles_missing_starlet`): a starlet vanished from a studio.
   Search the lot and basement; rescue her or take the director's film.
2. **The Cult Studio** (`los_angeles_cult_studio`): an oilman funds a studio making strange
   films. Follow the money from the oil fields to the basement; expose it or take the reel.

## Signs

`MISSION`, `HOLLYWOOD`, `DOWNTOWN`, `OIL FIELDS`, `BEACH`, `CITY NOTICES`,
plus studio and beach warnings.

## Atmosphere

Stucco, palm trees, neon, film lights, oil derricks, beach umbrellas, cars, dust, a few boarded
studios, gulls, heat.
