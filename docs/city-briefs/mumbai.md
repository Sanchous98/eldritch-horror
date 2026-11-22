# Mumbai — authored city brief

Footprint `halfExtent = 340`. Coordinates relative to `(cx, ground, cz)`; no layout RNG.

## Identity

Bombay as a port city: the Gateway of India on the harbour, the docks and warehouses, Crawford
Market's awnings, Malabar Hill's bungalows and Towers of Silence, the mill district's brick
chimneys, and the sea. Heat, cotton, cargo, and crowds.

## Macro layout

| District | X | Z | Character |
|---|---|---|---|
| Harbour / Gateway | -140..140 | 120..300 | Gateway arch, quay, ferries, customs, crowd |
| Docks | 140..320 | -260..180 | warehouses, cranes, coal, railway sidings |
| Crawford Market | -320..-140 | -260..80 | market halls, awnings, shops, tea |
| Malabar Hill | -320..-100 | 80..300 | bungalows, gardens, sea view, quiet lanes |
| Mill district | 140..320 | 180..320 | brick mills, chimneys, workers' housing |
| Fort / offices | -120..120 | -260..80 | colonial offices, banks, church, hotel |

**Arrival gate**: south `(0, 320)` or harbour? Use the harbour gate at `(0, 300)`; signpost to
Gateway, docks, market, Malabar Hill, mills.

**Streets**: 14-wide north–south axis; 10-wide market lanes; dock quay 12 wide; mill road 10;
hill lanes 5.

## Landmark

**Gateway of India** at `(0, 220)`: two massive basalt piers, a central arch, corner turrets,
facing the harbour water; a paved plaza with steps and mooring posts.

## Required POIs

| POI | Kind | Position | Content |
|---|---|---|---|
| `harbour_quest_board` | QUEST_BOARD | `(0, 180)` | two local quests |
| `taj_hotel` | INN | `(-80, 120)` | hotel, keeper, bed |
| `crawford_shop` | SHOP | `(-240, -80)` | shop, merchant |
| `hakim` | HEALER | `(-240, 120)` | healer, remedies |
| `dock_office` | QUEST | `(220, 0)` | dock strike |
| `theatre` | QUEST | `(-60, -120)` | missing actor |
| `mill_cellar` | SECRET | `(240, 240)` | mill cellar, shrine |
| `malabar_bungalow` | SECRET | `(-240, 220)` | bungalow, locked study |
| `ferry_dock` | LANDMARK | `(60, 260)` | ferries, boats |

## NPCs

`hotel_keeper`, `merchant`, `hakim`, `dock_master`, `theatre_manager`, `mill_worker`,
`bungalow_servant`.

## Local quests

1. **The Dock Strike** (`mumbai_dock_strike`): the dock master's ledgers vanished during a
   strike. Find them in the mill cellar; choose to expose the owners or sell the ledgers.
2. **The Missing Actor** (`mumbai_missing_actor`): a Parsi theatre actor vanished after a
   performance. Search the theatre and Malabar bungalow; choose to rescue or take the mask.

## Signs

`GATEWAY`, `DOCKS`, `CRAWFORD MARKET`, `MALABAR HILL`, `MILLS`, `CITY NOTICES`,
plus dock and mill warnings.

## Atmosphere

Basalt, ochre, cotton bales, coal smoke, cranes, gulls, awnings, tram wires, bungalow gardens,
a few boarded mills, monsoon puddles.
