# Lima — authored city brief

Footprint `halfExtent = 340`. Coordinates relative to `(cx, ground, cz)`; no layout RNG.

## Identity

Lima between the wars: the colonial cathedral on the Plaza Mayor, the port of Callao, the
barrios, and the pre-Columbian ruins. Stucco, fog, dust, and sea.

## Macro layout

| District | X | Z | Character |
|---|---|---|---|
| Cathedral / Plaza Mayor | -140..140 | -300..-100 | cathedral, plaza, palace, arcades |
| Port / Callao | 140..320 | -240..160 | docks, warehouses, customs, rail |
| Barrios | -320..-140 | -240..180 | adobe lanes, markets, churches, courtyards |
| Ruins | -120..120 | -80..180 | pyramid, walls, museum, dusty paths |
| Coastal tomb | 180..260 | 200..300 | tomb, cliffs, secret |
| Center | -120..120 | -60..160 | shops, cafés, offices, hotel |

**Arrival gate**: south `(0, 320)`; signpost to cathedral, port, barrios, ruins.

**Streets**: 14-wide axis; barrio lanes 5 wide; port road 10 wide; ruin paths 6 wide.

## Landmark

**Colonial cathedral** at `(0, -200)`: twin bell towers, a long nave, a domed crossing, and a
baroque façade on the plaza.

## Required POIs

| POI | Kind | Position | Content |
|---|---|---|---|
| `plaza_quest_board` | QUEST_BOARD | `(0, -120)` | two local quests |
| `hotel` | INN | `(-80, -60)` | inn, keeper, bed |
| `barrio_shop` | SHOP | `(-240, -60)` | shop, merchant |
| `curandero` | HEALER | `(-240, 120)` | healer, remedies |
| `museum_curator` | NPC | `(0, 120)` | missing archaeologist |
| `port_customs` | QUEST | `(240, 0)` | stolen quipu |
| `coastal_tomb` | SECRET | `(240, 260)` | tomb, chests/lore |
| `cathedral` | LANDMARK | `(0, -200)` | cathedral |
| `ruins` | LANDMARK | `(0, 60)` | pyramid, walls |

## NPCs

`hotel_clerk`, `shopkeeper`, `curandero`, `curator`, `customs_officer`, `fisherman`, `guide`.

## Local quests

1. **The Missing Archaeologist** (`lima_missing_archaeologist`): an archaeologist vanished at
   the ruins. Search the pyramid and coastal tomb; rescue him or take the find.
2. **The Stolen Quipu** (`lima_stolen_quipu`): a quipu was stolen from the museum. Trace it
   through the port to the tomb; return it, keep it, or sell it.

## Signs

`CATHEDRAL`, `PORT`, `BARRIOS`, `RUINS`, `CENTER`, `CITY NOTICES`,
plus ruin and tomb warnings.

## Atmosphere

Stucco, adobe, fog, dust, market awnings, church bells, port cranes, pre-Columbian walls,
a few boarded shops, vultures, sea mist.
