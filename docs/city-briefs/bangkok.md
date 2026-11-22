# Bangkok — authored city brief

Footprint `halfExtent = 340`. Coordinates relative to `(cx, ground, cz)`; no layout RNG.

## Identity

1920s Bangkok: the Grand Palace and a Thai temple with a steep stacked roof, the klong market
on the water, Chinatown's shophouses, the backpacker street, and the river. Gold, teak, canals,
heat.

## Macro layout

| District | X | Z | Character |
|---|---|---|---|
| Grand Palace / temple | -140..140 | -300..-80 | walls, ordination hall, stupas, courtyards |
| Klong market | -320..-140 | -260..200 | water market, boats, stilt houses, bridges |
| Chinatown | 140..320 | -220..60 | shophouses, gold shops, tea, opium den |
| Backpacker street | 140..320 | 80..260 | guesthouses, bars, cafés, lanterns |
| River | -320..320 | 200..230 | water, ferry, warehouses |
| Temple garden | -100..100 | -80..60 | bodhi tree, shrine, crypt |

**Arrival gate**: south `(0, 320)`; signpost to palace, klong, Chinatown, guesthouses.

**Streets**: 12-wide palace avenue; klong water lanes 8 wide with footbridges; Chinatown lanes
6 wide; guesthouse street 10 wide; river road 8 wide.

## Landmark

**Thai ordination hall** at `(0, -200)`: tiered base, steep stacked roof with chofah finials,
golden door panels, flanked by stupas, inside the temple walls.

## Required POIs

| POI | Kind | Position | Content |
|---|---|---|---|
| `klong_quest_board` | QUEST_BOARD | `(-220, 0)` | two local quests |
| `guesthouse` | INN | `(220, 160)` | inn, keeper, bed |
| `gold_shop` | SHOP | `(220, -80)` | shop, gold, rumours |
| `monk_healer` | HEALER | `(-60, -120)` | monk/healer, remedies |
| `amulet_stall` | QUEST | `(-240, -120)` | stolen amulet |
| `river_shrine` | QUEST | `(0, 215)` | river spirit, boatman |
| `warehouse` | SECRET | `(260, 40)` | smuggled cargo |
| `temple_crypt` | SECRET | `(60, -20)` | crypt behind the hall |
| `palace_gate` | LANDMARK | `(0, -80)` | palace gate, guards |

## NPCs

`guesthouse_keeper`, `gold_merchant`, `monk`, `amulet_seller`, `boatman`, `dockworker`,
`palace_guard`.

## Local quests

1. **The Stolen Amulet** (`bangkok_stolen_amulet`): a market amulet that wards off a river
   spirit was stolen. Trace it through Chinatown to the warehouse; return it, keep it, or give
   it to the Choir.
2. **The River Spirit** (`bangkok_river_spirit`): the boatman's route is haunted; a shrine on
   the river has been desecrated. Restore the shrine or take the spirit's offering.

## Signs

`GRAND PALACE`, `KLONG MARKET`, `CHINATOWN`, `GUESTHOUSES`, `RIVER`, `CITY NOTICES`,
plus temple and river warnings.

## Atmosphere

Gold leaf, teak, orange monk robes, lotus, canals, long-tail boats, spirit houses, neon
guesthouse signs, a few flooded lanes, geckos, incense.
