# Lagos — authored city brief

Footprint `halfExtent = 340`. Coordinates relative to `(cx, ground, cz)`; no layout RNG.

## Identity

Colonial Lagos: the Oba's walled courtyard palace, the marina and lagoon, the market, the
colonial quarter, and the shrines. Red earth, whitewash, corrugated iron, water, and drums.

## Macro layout

| District | X | Z | Character |
|---|---|---|---|
| Oba's palace | -140..140 | -300..-80 | walls, colonnaded hall, courtyards, shrine |
| Market | -320..-140 | -240..180 | stalls, awnings, crafts, food, drums |
| Marina / lagoon | 140..320 | -240..220 | water, quay, canoes, warehouses, fish |
| Colonial quarter | -120..120 | -60..200 | whitewashed offices, church, club, residences |
| Shrine / lagoon | 160..300 | 220..320 | stilt shrine, water, secret |
| Marina road | -320..320 | 200..230 | waterfront road, palms |

**Arrival gate**: south `(0, 320)`; signpost to palace, market, marina, colonial quarter.

**Streets**: 14-wide axis; market lanes 6 wide; marina quay 10 wide; colonial grid 8 wide;
lagoon boardwalk 4 wide.

## Landmark

**Oba's palace** at `(0, -200)`: a walled compound with a colonnaded hall under a pitched metal
roof, carved posts, a central courtyard, and a shrine.

## Required POIs

| POI | Kind | Position | Content |
|---|---|---|---|
| `market_quest_board` | QUEST_BOARD | `(-220, 0)` | two local quests |
| `colonial_hotel` | INN | `(-80, -120)` | inn, keeper, bed |
| `market_shop` | SHOP | `(-240, -60)` | shop, merchant |
| `herbalist` | HEALER | `(-240, 120)` | healer, remedies |
| `oil_office` | QUEST | `(220, -120)` | oil ledger |
| `fisherman` | NPC | `(240, 120)` | missing fisherman |
| `lagoon_shrine` | SECRET | `(240, 260)` | stilt shrine, chest/lore |
| `palace_gate` | LANDMARK | `(0, -80)` | palace gate, guard |
| `marina_quay` | LANDMARK | `(220, 40)` | canoes, fish market |

## NPCs

`hotel_keeper`, `merchant`, `herbalist`, `oil_clerk`, `fisherman`, `palace_guard`, `priest`.

## Local quests

1. **The Oil Ledger** (`lagos_oil_ledger`): an oil company clerk has a ledger of illegal
   shipments. Trace it through the marina to the shrine; expose it, sell it, or give it to the
   Choir.
2. **The Missing Fisherman** (`lagos_missing_fisherman`): a fisherman vanished on the lagoon.
   Search the stilt shrine and the water; choose to rescue him or take the shrine's offering.

## Signs

`OBA'S PALACE`, `MARKET`, `MARINA`, `COLONIAL QUARTER`, `LAGOON`, `CITY NOTICES`,
plus lagoon and shrine warnings.

## Atmosphere

Red earth, whitewash, rusted iron, palm trees, canoes, fish, drums, market awnings, a few
flooded lanes, chickens, heat.
