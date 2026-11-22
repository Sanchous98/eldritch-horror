# Delhi — authored city brief

Footprint `halfExtent = 340`. Coordinates relative to `(cx, ground, cz)`; no layout RNG.

## Identity

Late-colonial Delhi: a domed mausoleum in a charbagh garden, Chandni Chowk's bazaar, the Red
Fort's walls, the railway station, and the stepwell beneath the bazaar. Sandstone, dust,
crowds, and water.

## Macro layout

| District | X | Z | Character |
|---|---|---|---|
| Mausoleum garden | -140..140 | -300..-80 | charbagh, water channels, domed tomb, minarets |
| Chandni Chowk | -320..-140 | -260..200 | bazaar lanes, shops, tea, spice, stepwell |
| Red Fort | 140..320 | -260..-20 | red sandstone walls, gates, courtyards, barracks |
| Railway station | 140..320 | 40..220 | station, platforms, warehouses, telegraph |
| Civil lines | -320..320 | 220..300 | bungalows, club, church, trees |
| Stepwell | -240..-160 | 100..180 | sunken steps, water, secret shrine |

**Arrival gate**: south `(0, 320)`; signpost to mausoleum, bazaar, fort, station.

**Streets**: 14-wide axis to the mausoleum; bazaar lanes 5 wide; fort road 10 wide; station
road 10 wide; civil lines 8 wide.

## Landmark

**Domed mausoleum** at `(0, -200)`: a red sandstone plinth, a great white marble dome, four
corner minarets, an iwan arch, inside a walled charbagh with water channels.

## Required POIs

| POI | Kind | Position | Content |
|---|---|---|---|
| `bazaar_quest_board` | QUEST_BOARD | `(-220, 0)` | two local quests |
| `caravanserai_inn` | INN | `(-240, -80)` | inn, keeper, bed |
| `spice_shop` | SHOP | `(-240, 60)` | shop, merchant |
| `hakim` | HEALER | `(-200, 120)` | healer, remedies |
| `fort_clerk` | NPC | `(240, -120)` | stolen relic |
| `stationmaster` | NPC | `(240, 120)` | railway rumours |
| `stepwell` | SECRET | `(-200, 140)` | stepwell, hidden shrine |
| `tomb_crypt` | SECRET | `(0, -160)` | crypt under the mausoleum |
| `fort_gate` | LANDMARK | `(160, -80)` | Red Fort gate |

## NPCs

`innkeeper`, `spice_merchant`, `hakim`, `fort_clerk`, `stationmaster`, `guide`, `guard`.

## Local quests

1. **The Missing Bride** (`delhi_missing_bride`): a bride vanished from the bazaar before the
   wedding. Search the stepwell and the shrine; choose to rescue her or take the cult's dowry.
2. **The Stolen Relic** (`delhi_stolen_relic`): a relic was taken from the fort. The clerk
   suspects the stationmaster; trace it through the bazaar to the tomb crypt.

## Signs

`MAUSOLEUM`, `CHANDNI CHOWK`, `RED FORT`, `RAILWAY STATION`, `CIVIL LINES`,
`CITY NOTICES`, plus stepwell and tomb warnings.

## Atmosphere

Red sandstone, white marble, dust, spice sacks, tea stalls, rickshaws, peacocks, monkeys,
water channels, a steam train, a few collapsed lanes, heat haze.
