# Cape Town — authored city brief

Footprint `halfExtent = 340`. Coordinates relative to `(cx, ground, cz)`; no layout RNG.

## Identity

Cape Town under the Company: the Cape Dutch manor and its gable, the harbour and Table Bay,
Bo-Kaap's brightly painted slopes, the Company's Garden, District Six's lanes, and the mountain
behind everything. Whitewash, green shutters, wine, wind, and sea.

## Macro layout

| District | X | Z | Character |
|---|---|---|---|
| Cape Dutch manor | -140..140 | -300..-100 | manor, gable, werf, oaks, slave lodge |
| Harbour | 140..320 | -240..180 | quay, warehouses, cranes, ships, fish |
| Bo-Kaap | -320..-140 | -240..180 | painted slopes, mosques, narrow lanes |
| Company's Garden | -120..120 | -80..140 | formal garden, avenues, fountain, library |
| District Six | -320..320 | 180..300 | brick lanes, terraces, church, market |
| Mountain foot | -320..320 | -340..-300 | slopes, paths, signal station |

**Arrival gate**: south `(0, 320)`; signpost to manor, harbour, Bo-Kaap, garden, District Six.

**Streets**: 14-wide axis; harbour quay 12 wide; Bo-Kaap lanes 5 wide; garden avenues 8 wide;
District Six lanes 6 wide.

## Landmark

**Cape Dutch manor** at `(0, -200)`: a long whitewashed house with a stepped ornamental gable,
green shutters, a broad stoep, oak avenue, and a walled werf.

## Required POIs

| POI | Kind | Position | Content |
|---|---|---|---|
| `harbour_quest_board` | QUEST_BOARD | `(220, 0)` | two local quests |
| `cape_hotel` | INN | `(-80, -120)` | inn, keeper, bed |
| `bo_kaap_shop` | SHOP | `(-240, -60)` | shop, merchant |
| `herbalist` | HEALER | `(-240, 120)` | healer, remedies |
| `signal_station` | QUEST | `(0, -300)` | missing mountaineer |
| `manor_steward` | NPC | `(0, -160)` | sealed letter |
| `wine_cellar` | SECRET | `(40, -180)` | cellar, chests/lore |
| `harbour_quay` | LANDMARK | `(220, 60)` | ships, cranes, fish |
| `company_garden` | LANDMARK | `(0, 40)` | fountain, library, avenues |

## NPCs

`hotel_keeper`, `merchant`, `herbalist`, `signal_keeper`, `steward`, `dockworker`, `dominee`.

## Local quests

1. **The Missing Mountaineer** (`cape_town_missing_mountaineer`): a climber vanished on the
   mountain path. Search the signal station and slopes; choose to rescue him or take his find.
2. **The Sealed Letter** (`cape_town_sealed_letter`): the manor steward has a sealed letter that
   implicates the Company. Open it, deliver it, or sell it to the Choir.

## Signs

`CAPE MANOR`, `HARBOUR`, `BO-KAAP`, `COMPANY'S GARDEN`, `DISTRICT SIX`, `CITY NOTICES`,
plus mountain and cellar warnings.

## Atmosphere

Whitewash, green shutters, oak trees, cobbles, painted Bo-Kaap walls, mosques, ships, gulls,
wine barrels, wind, a few boarded lanes, mountain mist.
