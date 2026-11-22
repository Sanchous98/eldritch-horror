# London — authored city brief

Footprint `halfExtent = 340`. Coordinates relative to `(cx, ground, cz)`; no layout RNG.

## Identity

London between the wars: St Paul's dome and Big Ben, the docks and warehouses, the East End's
terraces, the British Museum, and the Underground. Smoke, fog, brick, and iron.

## Macro layout

| District | X | Z | Character |
|---|---|---|---|
| St Paul's / City | -140..140 | -300..-80 | cathedral, clock tower, banks, offices |
| Docks | 140..320 | -260..180 | quay, warehouses, cranes, railway, pubs |
| East End | 140..320 | 180..320 | terraces, market, chapel, alleys |
| British Museum | -320..-140 | -240..80 | museum, squares, townhouses, gardens |
| West End | -120..120 | -60..180 | theatres, shops, hotels, cafés |
| Underground | -100..100 | 80..140 | station, tunnels, secret junction |

**Arrival gate**: south `(0, 320)`; signpost to cathedral, docks, East End, museum, West End.

**Streets**: 14-wide axis; dock quay 12 wide; East End lanes 5 wide; museum squares 10 wide;
West End streets 8 wide.

## Landmark

**St Paul's Cathedral** at `(0, -200)`: a great dome on a colonnaded drum, a west front with
twin towers, and a square clock tower nearby (Big Ben silhouette).

## Required POIs

| POI | Kind | Position | Content |
|---|---|---|---|
| `city_quest_board` | QUEST_BOARD | `(-80, -120)` | two local quests |
| `savoy_hotel` | INN | `(-80, 40)` | inn, keeper, bed |
| `west_end_shop` | SHOP | `(-40, 80)` | shop, merchant |
| `apothecary` | HEALER | `(180, 220)` | healer, remedies |
| `museum_curator` | NPC | `(-240, -60)` | missing expedition |
| `dock_pub` | QUEST | `(240, 60)` | cursed mummy |
| `underground_junction` | SECRET | `(0, 120)` | tunnel junction, hidden room |
| `british_museum` | LANDMARK | `(-240, 0)` | museum, gallery |
| `big_ben` | LANDMARK | `(80, -120)` | clock tower |

## NPCs

`hotel_keeper`, `shopkeeper`, `apothecary`, `curator`, `dockworker`, `bobby`, `theatre_manager`.

## Local quests

1. **The Missing Expedition** (`london_missing_expedition`): a museum expedition vanished in
   the East End. Trace it through the docks to the Underground junction; rescue or take the find.
2. **The Cursed Mummy** (`london_cursed_mummy`): a mummy case in the museum is killing people.
   Follow the clues from the dock pub to the museum and the junction; destroy it or sell it.

## Signs

`ST PAUL'S`, `DOCKS`, `EAST END`, `BRITISH MUSEUM`, `WEST END`, `UNDERGROUND`, `CITY NOTICES`,
plus dock and tunnel warnings.

## Atmosphere

Brick, fog, gas lamps, iron railings, red buses, dock cranes, coal smoke, theatre marquees,
a few bombed/boarded terraces, pigeons.
