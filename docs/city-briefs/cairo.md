# Cairo — authored city brief

Footprint `halfExtent = 340`. Coordinates relative to `(cx, ground, cz)`; no layout RNG.

## Identity

Colonial-era Cairo: a great mosque with dome and minarets, Khan el-Khalili's covered bazaar,
the Nile corniche, the Citadel's walls, and the City of the Dead's tombs. Sand, stone, water,
and dust.

## Macro layout

| District | X | Z | Character |
|---|---|---|---|
| Great mosque | -140..140 | -300..-80 | dome, two minarets, courtyard, fountain |
| Khan el-Khalili | -320..-140 | -240..200 | covered bazaar lanes, shops, tea, papyrus |
| Nile corniche | 140..320 | -240..160 | river, quay, feluccas, hotels, customs |
| Citadel | 140..320 | 180..300 | walls, gate, barracks, well |
| City of the Dead | -320..320 | 220..320 | tombs, mausolea, lanes, secret tomb |
| European quarter | -120..120 | -60..180 | hotels, banks, church, cafés |

**Arrival gate**: south `(0, 320)`; signpost to mosque, bazaar, Nile, Citadel, City of the Dead.

**Streets**: 14-wide axis; bazaar lanes 5 wide; corniche 12 wide; citadel road 10 wide; tomb
lanes 4 wide.

## Landmark

**Great mosque** at `(0, -200)`: a large central dome, two slender minarets, an arcaded
courtyard with a fountain, and a tall portal.

## Required POIs

| POI | Kind | Position | Content |
|---|---|---|---|
| `bazaar_quest_board` | QUEST_BOARD | `(-220, 0)` | two local quests |
| `oriental_hotel` | INN | `(-80, -120)` | inn, keeper, bed |
| `papyrus_shop` | SHOP | `(-240, -60)` | shop, merchant |
| `hakim` | HEALER | `(-240, 120)` | healer, remedies |
| `papyrus_dealer` | QUEST | `(-260, -120)` | stolen papyrus |
| `museum_curator` | NPC | `(220, -120)` | missing archaeologist |
| `city_of_the_dead` | SECRET | `(-200, 260)` | tomb, shrine |
| `citadel_gate` | LANDMARK | `(160, 220)` | Citadel gate, guard |
| `nile_quay` | LANDMARK | `(220, 60)` | feluccas, customs |

## NPCs

`hotel_keeper`, `merchant`, `hakim`, `papyrus_dealer`, `curator`, `guard`, `ferryman`.

## Local quests

1. **The Stolen Papyrus** (`cairo_stolen_papyrus`): a papyrus dealer's scroll was stolen. Trace
   it through the bazaar to the City of the Dead; return it to the museum, keep it, or sell it.
2. **The Missing Archaeologist** (`cairo_missing_archaeologist`): the curator's colleague
   vanished at the Citadel. Search the well and the tomb; choose to rescue him or take the find.

## Signs

`GREAT MOSQUE`, `KHAN EL-KHALILI`, `NILE`, `CITADEL`, `CITY OF THE DEAD`, `CITY NOTICES`,
plus tomb and quay warnings.

## Atmosphere

Sandstone, domes, minarets, awnings, water pipes, feluccas, palm trees, dust, a few collapsed
tombs, cats, heat haze.
