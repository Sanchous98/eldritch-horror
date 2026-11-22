# Rio de Janeiro — authored city brief

Footprint `halfExtent = 340`. Coordinates relative to `(cx, ground, cz)`; no layout RNG.

## Identity

Rio between the wars: Christ the Redeemer on the hill, Copacabana's beach, the favela's lanes,
the port, and the botanical garden. Granite, palm, tile, and sea.

## Macro layout

| District | X | Z | Character |
|---|---|---|---|
| Redeemer hill | -140..140 | -300..-100 | statue, path, chapel, viewpoint |
| Copacabana | 140..320 | -240..160 | beach, promenade, hotels, cafés |
| Favela | -320..-140 | -240..180 | lanes, stilt houses, stairs, murals |
| Port | 140..320 | 180..320 | docks, warehouses, market, rail |
| Centro | -120..120 | -80..180 | offices, shops, theatres, cathedral |
| Botanical garden | -320..-140 | 180..300 | palms, greenhouse, paths, secret cave |

**Arrival gate**: south `(0, 320)`; signpost to Redeemer, Copacabana, favela, port.

**Streets**: 14-wide axis; favela lanes 4 wide; promenade 10 wide; port road 10 wide.

## Landmark

**Christ the Redeemer** at `(0, -200)`: a tall stone pedestal with an outstretched Art-Deco
figure, on a hilltop terrace with a chapel and a viewpoint.

## Required POIs

| POI | Kind | Position | Content |
|---|---|---|---|
| `centro_quest_board` | QUEST_BOARD | `(0, 60)` | two local quests |
| `hotel` | INN | `(180, -60)` | inn, keeper, bed |
| `centro_shop` | SHOP | `(-40, 120)` | shop, merchant |
| `herbalist` | HEALER | `(-240, 120)` | healer, remedies |
| `dance_hall` | QUEST | `(-240, 0)` | missing dancer |
| `port_customs` | NPC | `(240, 240)` | smuggled relic |
| `hillside_cave` | SECRET | `(-240, 220)` | cave, chests/lore |
| `redeemer` | LANDMARK | `(0, -200)` | statue |
| `copacabana` | LANDMARK | `(240, 40)` | beach, promenade |

## NPCs

`hotel_clerk`, `shopkeeper`, `herbalist`, `dance_teacher`, `customs_officer`, `fisherman`,
`guide`.

## Local quests

1. **The Missing Dancer** (`rio_missing_dancer`): a dancer vanished after a favela show. Search
   the dance hall and hillside cave; rescue her or take the patron's offer.
2. **The Smuggled Relic** (`rio_smuggled_relic`): customs suspects a relic in a port crate.
   Follow it through the port and cave; expose it or sell it.

## Signs

`REDEEMER`, `COPACABANA`, `FAVELA`, `PORT`, `CENTRO`, `BOTANICAL GARDEN`, `CITY NOTICES`,
plus hill and port warnings.

## Atmosphere

Granite, palm trees, mosaic promenade, beach umbrellas, favela stairs, murals, port cranes,
church bells, a few boarded houses, gulls, heat.
