# Nairobi — authored city brief

Footprint `halfExtent = 340`. Coordinates relative to `(cx, ground, cz)`; no layout RNG.

## Identity

Colonial-era Nairobi at the railway: the KICC-style tower, the station and yards, the Indian
bazaar, the colonial club and bungalows, and the dusty outskirts. Iron, red earth, acacia,
and steam.

## Macro layout

| District | X | Z | Character |
|---|---|---|---|
| Tower / civic centre | -140..140 | -260..-80 | tower, square, offices, hotel |
| Railway station | 140..320 | -240..140 | station, platforms, yards, warehouses |
| Bazaar | -320..-140 | -240..200 | shops, awnings, tea, spices, crafts |
| Colonial club | 140..320 | 160..300 | club, bungalows, church, lawns |
| Outskirts | -320..320 | 220..320 | acacia, dirt lanes, market gardens, ruins |
| Station undercroft | 180..260 | 20..100 | tunnels, cellar, secret |

**Arrival gate**: south `(0, 320)`; signpost to tower, station, bazaar, club.

**Streets**: 14-wide axis; bazaar lanes 6 wide; station road 12 wide; club avenues 8 wide;
outskirt tracks 4 wide.

## Landmark

**KICC-style tower** at `(0, -180)`: a tall concrete shaft with a stepped, crenellated crown
and a spire, on a paved plaza with flagpoles.

## Required POIs

| POI | Kind | Position | Content |
|---|---|---|---|
| `bazaar_quest_board` | QUEST_BOARD | `(-220, 0)` | two local quests |
| `railway_hotel` | INN | `(-80, -120)` | inn, keeper, bed |
| `bazaar_shop` | SHOP | `(-240, -60)` | shop, merchant |
| `herbalist` | HEALER | `(-240, 120)` | healer, remedies |
| `survey_office` | QUEST | `(220, -120)` | missing surveyor |
| `club_steward` | NPC | `(240, 220)` | poached relic |
| `station_undercroft` | SECRET | `(220, 60)` | tunnel, cellar |
| `station` | LANDMARK | `(220, 0)` | station, platforms |
| `club` | LANDMARK | `(240, 240)` | colonial club, lawns |

## NPCs

`hotel_keeper`, `merchant`, `herbalist`, `surveyor`, `steward`, `stationmaster`, `askari`.

## Local quests

1. **The Missing Surveyor** (`nairobi_missing_surveyor`): a surveyor vanished while mapping the
   railway. Search the station undercroft; choose to rescue him or take the survey.
2. **The Poached Relic** (`nairobi_poached_relic`): a relic was poached from a dig and passed
   through the club. Trace it to the undercroft; return it, keep it, or sell it.

## Signs

`CIVIC CENTRE`, `RAILWAY STATION`, `BAZAAR`, `COLONIAL CLUB`, `OUTSKIRTS`, `CITY NOTICES`,
plus station and undercroft warnings.

## Atmosphere

Red earth, corrugated iron, acacia, steam, coal, awnings, club lawns, askari uniforms, a few
abandoned wagons, heat haze.
