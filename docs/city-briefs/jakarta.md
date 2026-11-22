# Jakarta — authored city brief

Footprint `halfExtent = 340`. Coordinates relative to `(cx, ground, cz)`; no layout RNG.

## Identity

Colonial Batavia: the whitewashed town hall on its square, the old harbour with Dutch
warehouses, Glodok's Chinese shophouses, the kampung lanes, and the canal. Heat, rot, trade,
and water.

## Macro layout

| District | X | Z | Character |
|---|---|---|---|
| Town hall square | -140..140 | -260..-80 | town hall, church, parade ground, cafés |
| Old harbour | 140..320 | -260..120 | quay, warehouses, cranes, customs |
| Glodok | -320..-140 | -240..200 | shophouses, market, temples, tea |
| Kampung | -320..320 | 120..300 | stilt houses, lanes, wells, mosque |
| Canal | -320..320 | 80..110 | water, bridges, barges |
| Warehouse row | 160..300 | 140..280 | smuggled cargo, cellar |

**Arrival gate**: south `(0, 320)`; signpost to town hall, harbour, Glodok, kampung.

**Streets**: 14-wide axis to the town hall; harbour quay 10 wide; Glodok lanes 6 wide; kampung
lanes 4 wide; canal road 8 wide.

## Landmark

**Colonial town hall** at `(0, -180)`: long whitewashed block, red tile roof, central clock
tower, arcaded ground floor, on a paved square with a flagpole.

## Required POIs

| POI | Kind | Position | Content |
|---|---|---|---|
| `harbour_quest_board` | QUEST_BOARD | `(220, 0)` | two local quests |
| `batavia_hotel` | INN | `(-80, -120)` | inn, keeper, bed |
| `glodok_shop` | SHOP | `(-240, -60)` | shop, merchant |
| `herbalist` | HEALER | `(-240, 120)` | herbalist, remedies |
| `customs_house` | QUEST | `(240, -120)` | drowned ledger |
| `kampung_well` | QUEST | `(-200, 220)` | missing sailor |
| `warehouse_cellar` | SECRET | `(240, 200)` | cellar, smuggled cargo |
| `canal_bridge` | LANDMARK | `(0, 95)` | stone bridge, barges |
| `mosque` | LANDMARK | `(120, 220)` | small mosque, imam |

## NPCs

`hotel_keeper`, `shop_merchant`, `herbalist`, `customs_clerk`, `fisherman`, `imam`,
`dockworker`.

## Local quests

1. **The Drowned Ledger** (`jakarta_drowned_ledger`): a customs clerk found a Dutch ledger in a
   flooded warehouse. Decode it at the hotel; choose to expose the smugglers or sell the route.
2. **The Missing Sailor** (`jakarta_missing_sailor`): a fisherman vanished from the kampung.
   Search the well and the canal; find the shrine under the warehouse.

## Signs

`TOWN HALL`, `OLD HARBOUR`, `GLODOK`, `KAMPUNG`, `CANAL`, `CITY NOTICES`,
plus harbour and canal warnings.

## Atmosphere

Whitewash, red tiles, green shutters, canals, barges, palm trees, market awnings, tobacco,
rain, a few flooded houses, cats, mosques and incense.
