# Buenos Aires — authored city brief

Footprint `halfExtent = 340`. Coordinates relative to `(cx, ground, cz)`; no layout RNG.

## Identity

Buenos Aires between the wars: the beaux-arts palace, Plaza de Mayo, the tango halls, the port,
and the Recoleta cemetery. Stucco, iron, tile, and music.

## Macro layout

| District | X | Z | Character |
|---|---|---|---|
| Palace / Plaza de Mayo | -140..140 | -300..-100 | palace, plaza, cathedral, government house |
| Tango halls | -320..-140 | -240..180 | halls, cafés, theatres, milongas |
| Port | 140..320 | -240..160 | docks, warehouses, customs, rail |
| Recoleta | 140..320 | 180..300 | cemetery, basilica, gardens, mansions |
| Centro | -120..120 | -80..180 | shops, banks, cafés, offices |
| Crypt | 180..260 | 200..280 | cemetery crypt, secret |

**Arrival gate**: south `(0, 320)`; signpost to palace, tango halls, port, Recoleta.

**Streets**: 14-wide axis; tango lanes 6 wide; port road 10 wide; cemetery paths 4 wide.

## Landmark

**Beaux-arts palace** at `(0, -200)`: a colonnaded portico, a high slate dome, and a grand
staircase on the plaza.

## Required POIs

| POI | Kind | Position | Content |
|---|---|---|---|
| `plaza_quest_board` | QUEST_BOARD | `(0, -120)` | two local quests |
| `hotel` | INN | `(-80, -60)` | inn, keeper, bed |
| `centro_shop` | SHOP | `(-40, 120)` | shop, merchant |
| `apothecary` | HEALER | `(-240, 120)` | healer, remedies |
| `tango_hall` | QUEST | `(-240, 0)` | missing heiress |
| `port_customs` | NPC | `(240, 0)` | stolen tango |
| `recoleta_crypt` | SECRET | `(240, 240)` | crypt, chests/lore |
| `palace` | LANDMARK | `(0, -200)` | palace |
| `recoleta` | LANDMARK | `(240, 220)` | cemetery, basilica |

## NPCs

`hotel_clerk`, `shopkeeper`, `apothecary`, `tango_singer`, `customs_officer`, `gardener`,
`constable`.

## Local quests

1. **The Missing Heiress** (`buenos_aires_missing_heiress`): an heiress vanished after a tango
   show. Search the hall and Recoleta crypt; rescue her or take the family secret.
2. **The Stolen Tango** (`buenos_aires_stolen_tango`): a composer's tango was stolen. Trace it
   through the port and crypt; return it, keep it, or sell it.

## Signs

`PALACE`, `TANGO HALLS`, `PORT`, `RECOLETA`, `CENTRO`, `CITY NOTICES`,
plus cemetery and port warnings.

## Atmosphere

Stucco, iron balconies, tile, cafés, tango music, port cranes, cypress trees, marble tombs,
a few boarded shops, pigeons.
