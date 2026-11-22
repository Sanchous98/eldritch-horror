# Seoul — authored city brief

Footprint `halfExtent = 340`. Coordinates relative to `(cx, ground, cz)`; no layout RNG.

## Identity

Late-Joseon Seoul under a modernising edge: Gyeongbokgung's throne hall and stone courtyards,
Bukchon's hanok roofs, Namdaemun market, the new brick quarter, the Han river, and a tomb under
the palace garden.

## Macro layout

| District | X | Z | Character |
|---|---|---|---|
| Gyeongbokgung | -140..140 | -300..-80 | walls, gates, throne hall, courtyards, garden |
| Bukchon hanok | -330..-150 | -260..160 | tiled hanok roofs, narrow lanes, scholar houses |
| Namdaemun market | 140..320 | -180..80 | stalls, awnings, tea houses, inn |
| Modern quarter | 140..320 | 100..300 | brick shops, bank, police station, telegraph |
| Han river | -320..320 | 200..230 | water, bridges, ferry dock |
| Palace garden | -100..100 | -80..40 | pond, pavilion, tomb entrance |

**Arrival gate**: south `(0, 320)`; signpost to palace, hanok village, market, modern quarter.

**Streets**: 14-wide palace avenue `x=-7..7`; market lanes 6 wide; hanok lanes 4 wide;
modern grid 10 wide; river road 8 wide.

## Landmark

**Gyeongbokgung throne hall** at `(0, -200)`: a long hall on a two-tier stone platform, dark
tiled double roof, red columns, painted eaves, flanked by two pavilions, inside the palace walls.

## Required POIs

| POI | Kind | Position | Content |
|---|---|---|---|
| `market_quest_board` | QUEST_BOARD | `(220, 0)` | two local quests |
| `hanok_inn` | INN | `(-240, 80)` | inn, keeper, bed |
| `market_shop` | SHOP | `(220, -80)` | shop, merchant |
| `herbalist` | HEALER | `(-240, -100)` | herbalist, remedies |
| `palace_clerk` | NPC | `(0, -90)` | royal ghost quest |
| `scholar_house` | QUEST | `(-240, 0)` | missing scholar |
| `police_station` | NPC | `(240, 200)` | constable, rumours |
| `palace_tomb` | SECRET | `(60, -20)` | tomb under the garden |
| `river_ferry` | LANDMARK | `(0, 215)` | ferry dock, bridge |

## NPCs

`innkeeper`, `merchant`, `herbalist`, `palace_clerk`, `scholar`, `constable`, `ferryman`.

## Local quests

1. **The Royal Ghost** (`seoul_royal_ghost`): the palace clerk hears a ghost in the throne hall.
   Restore the broken memorial tablet in the garden tomb, or take the ghost's seal to the Choir.
2. **The Missing Scholar** (`seoul_missing_scholar`): a hanok scholar vanished while copying a
   forbidden text. Search the scholar house and the palace tomb; choose to return the text or
   sell it.

## Signs

`GYEONGBOKGUNG`, `BUKCHON`, `NAMDAEMUN`, `MODERN QUARTER`, `HAN RIVER`, `CITY NOTICES`,
plus tomb and river warnings.

## Atmosphere

Tiled hanok roofs, paper doors, stone walls, pine trees, market awnings, brassware, a steam
tram, river mist, a few boarded houses, magpies.
