# Rome — authored city brief

Footprint `halfExtent = 340`. Coordinates relative to `(cx, ground, cz)`; no layout RNG.

## Identity

Rome between the wars: St Peter's dome and colonnade, the Forum's ruins, Trastevere's lanes,
the catacombs, and the Vatican. Travertine, marble, umbrella pines, and bells.

## Macro layout

| District | X | Z | Character |
|---|---|---|---|
| St Peter's / Vatican | -140..140 | -300..-100 | basilica, colonnade, square, palace |
| Forum | 140..320 | -260..40 | ruins, columns, temples, arches |
| Trastevere | -320..-140 | -240..220 | lanes, trattorias, church, laundry |
| Catacombs | -100..100 | 80..180 | tunnels, tombs, chapel, secret |
| Centro | -120..120 | -80..200 | piazzas, shops, hotels, cafés |
| River Tiber | -320..320 | 200..230 | water, bridges, embankment |

**Arrival gate**: south `(0, 320)`; signpost to St Peter's, Forum, Trastevere, centro.

**Streets**: 14-wide axis; Trastevere lanes 4 wide; Forum paths 6 wide; centro streets 8 wide;
embankment 8 wide.

## Landmark

**St Peter's Basilica** at `(0, -200)`: a great dome on a drum, a long nave, a columned façade,
and a sweeping colonnade around the square.

## Required POIs

| POI | Kind | Position | Content |
|---|---|---|---|
| `centro_quest_board` | QUEST_BOARD | `(0, 60)` | two local quests |
| `albergo` | INN | `(-80, -120)` | inn, keeper, bed |
| `centro_shop` | SHOP | `(-40, 120)` | shop, merchant |
| `apothecary` | HEALER | `(-240, 120)` | healer, remedies |
| `vatican_priest` | NPC | `(0, -120)` | stolen relic |
| `trastevere_priest` | QUEST | `(-240, 0)` | missing priest |
| `catacombs` | SECRET | `(0, 120)` | tunnels, chapel |
| `st_peters` | LANDMARK | `(0, -200)` | basilica |
| `forum` | LANDMARK | `(220, -120)` | ruins, arches |

## NPCs

`hotel_keeper`, `shopkeeper`, `apothecary`, `vatican_priest`, `trastevere_priest`, `guide`,
`carabiniere`.

## Local quests

1. **The Stolen Relic** (`rome_stolen_relic`): a relic vanished from the Vatican. Trace it
   through the Forum and catacombs; return it, keep it, or sell it.
2. **The Missing Priest** (`rome_missing_priest`): a Trastevere priest vanished after a visit
   to the catacombs. Search the tunnels; rescue him or take the chapel's secret.

## Signs

`ST PETER'S`, `FORUM`, `TRASTEVERE`, `CATACOMBS`, `CENTRO`, `CITY NOTICES`,
plus catacomb and river warnings.

## Atmosphere

Travertine, marble, umbrella pines, ruins, laundry lines, Vespas, café tables, church bells,
a few boarded shops, cats, heat.
