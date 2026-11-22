# Istanbul — authored city brief

Footprint `halfExtent = 340`. Coordinates relative to `(cx, ground, cz)`; no layout RNG.

## Identity

Constantinople/Istanbul between empires: Hagia Sophia's great dome, the Grand Bazaar's vaulted
lanes, Galata's tower and merchants, the Bosphorus ferries, and a cistern under the bazaar.
Stone, water, copper, and smoke.

## Macro layout

| District | X | Z | Character |
|---|---|---|---|
| Hagia Sophia / palace | -140..140 | -300..-80 | dome, minarets, courtyard, palace gate |
| Grand Bazaar | -320..-140 | -240..200 | vaulted lanes, shops, caravanserai, tea |
| Galata | 140..320 | -240..120 | tower, merchants, warehouses, church |
| Bosphorus | 160..320 | 140..300 | water, ferries, quay, fish market |
| Cistern / old city | -120..120 | -60..160 | columns, water, secret cistern entrance |
| Hippodrome | -120..120 | 180..300 | obelisks, square, tea gardens |

**Arrival gate**: south `(0, 320)`; signpost to Hagia Sophia, bazaar, Galata, ferries.

**Streets**: 14-wide axis; bazaar lanes 5 wide; Galata lanes 6 wide; quay 10 wide; hippodrome
square.

## Landmark

**Hagia Sophia** at `(0, -200)`: a great central dome on a square base, four minarets, a long
nave, an arcaded courtyard, and a fountain.

## Required POIs

| POI | Kind | Position | Content |
|---|---|---|---|
| `bazaar_quest_board` | QUEST_BOARD | `(-220, 0)` | two local quests |
| `caravanserai` | INN | `(-240, -80)` | inn, keeper, bed |
| `bazaar_shop` | SHOP | `(-240, 60)` | shop, merchant |
| `hakim` | HEALER | `(-200, 120)` | healer, remedies |
| `icon_dealer` | QUEST | `(-260, -120)` | smuggled icon |
| `dragoman` | NPC | `(220, -120)` | missing dragoman |
| `cistern` | SECRET | `(0, 40)` | underground cistern |
| `galata_tower` | LANDMARK | `(240, -160)` | tower, view |
| `ferry_quay` | LANDMARK | `(240, 220)` | ferries, fish market |

## NPCs

`innkeeper`, `merchant`, `hakim`, `icon_dealer`, `dragoman`, `ferryman`, `guard`.

## Local quests

1. **The Smuggled Icon** (`istanbul_smuggled_icon`): an icon dealer has a stolen icon. Trace it
   through the bazaar and cistern; return it to the church, keep it, or sell it to the Choir.
2. **The Missing Dragoman** (`istanbul_missing_dragoman`): a translator vanished after a deal in
   Galata. Search the tower and the cistern; choose to rescue him or take the merchant's ledger.

## Signs

`HAGIA SOPHIA`, `GRAND BAZAAR`, `GALATA`, `FERRIES`, `HIPPODROME`, `CITY NOTICES`,
plus cistern and quay warnings.

## Atmosphere

Domes, minarets, copper, carpets, lanterns, cats, seagulls, ferries, tea glasses, a few boarded
shops, water in stone channels.
