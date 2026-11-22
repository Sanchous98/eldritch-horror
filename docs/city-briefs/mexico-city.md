# Mexico City — authored city brief

Footprint `halfExtent = 340`. Coordinates relative to `(cx, ground, cz)`; no layout RNG.

## Identity

Mexico City over its old lake: the colonial cathedral on the Zócalo, the canals and chinampas,
the market, the ruins, and the volcanoes beyond. Stone, stucco, water, and dust.

## Macro layout

| District | X | Z | Character |
|---|---|---|---|
| Cathedral / Zócalo | -140..140 | -300..-100 | cathedral, plaza, palace, arcades |
| Market | -320..-140 | -240..180 | stalls, awnings, food, crafts |
| Canals | 140..320 | -240..160 | water, chinampas, boats, bridges |
| Ruins | -120..120 | -80..180 | stepped pyramid, ball court, museum |
| Reforma | 140..320 | 180..300 | boulevard, hotels, shops, cafés |
| Aztec tunnel | -60..60 | 60..160 | tunnel, shrine, secret |

**Arrival gate**: south `(0, 320)`; signpost to cathedral, market, canals, ruins.

**Streets**: 14-wide axis; market lanes 6 wide; canal paths 4 wide; boulevard 12 wide.

## Landmark

**Metropolitan Cathedral** at `(0, -200)`: twin bell towers, a long nave, a domed crossing, and
a baroque façade on the Zócalo.

## Required POIs

| POI | Kind | Position | Content |
|---|---|---|---|
| `zocalo_quest_board` | QUEST_BOARD | `(0, -120)` | two local quests |
| `hotel` | INN | `(-80, -60)` | inn, keeper, bed |
| `market_shop` | SHOP | `(-240, -60)` | shop, merchant |
| `curandero` | HEALER | `(-240, 120)` | healer, remedies |
| `museum_curator` | NPC | `(0, 120)` | missing curator |
| `canal_boatman` | QUEST | `(240, 0)` | stolen idol |
| `aztec_tunnel` | SECRET | `(0, 120)` | tunnel, shrine |
| `cathedral` | LANDMARK | `(0, -200)` | cathedral |
| `ruins` | LANDMARK | `(0, 60)` | pyramid, ball court |

## NPCs

`hotel_clerk`, `shopkeeper`, `curandero`, `curator`, `boatman`, `guard`, `guide`.

## Local quests

1. **The Missing Curator** (`mexico_city_missing_curator`): a museum curator vanished after
   opening a tomb. Search the ruins and tunnel; rescue him or take the idol.
2. **The Stolen Idol** (`mexico_city_stolen_idol`): an idol was taken from the museum. Trace it
   through the canals to the tunnel; return it, keep it, or sell it.

## Signs

`CATHEDRAL`, `MARKET`, `CANALS`, `RUINS`, `REFORMA`, `CITY NOTICES`,
plus canal and tunnel warnings.

## Atmosphere

Stone, stucco, awnings, canal boats, marigolds, ruins, church bells, dust, a few collapsed
lanes, vultures.
