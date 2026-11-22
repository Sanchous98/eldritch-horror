# Paris — authored city brief

Footprint `halfExtent = 340`. Coordinates relative to `(cx, ground, cz)`; no layout RNG.

## Identity

Paris between the wars: Notre-Dame on the Île de la Cité, the Louvre, Montmartre's cafés and
dance halls, the Seine's quays and bridges, and the catacombs beneath. Limestone, zinc, art,
and smoke.

## Macro layout

| District | X | Z | Character |
|---|---|---|---|
| Notre-Dame / island | -100..100 | -260..-80 | cathedral, parvis, bridges, quays |
| Louvre | -320..-140 | -240..80 | palace, galleries, gardens, arcades |
| Montmartre | -320..-140 | 120..320 | hill, cafés, dance hall, studios, church |
| Seine | -320..320 | -60..-20 | water, quays, bridges, booksellers |
| Grands boulevards | 140..320 | -240..160 | shops, theatres, hotels, cafés |
| Catacombs | -60..60 | 80..180 | tunnels, ossuary, secret chamber |

**Arrival gate**: south `(0, 320)`; signpost to cathedral, Louvre, Montmartre, boulevards.

**Streets**: 14-wide axis; quays 10 wide; Montmartre lanes 5 wide; boulevards 12 wide;
catacomb tunnels 3 wide.

## Landmark

**Notre-Dame** at `(0, -200)`: a gothic west front with twin towers, a rose window, a long nave
with flying buttresses, and a spire.

## Required POIs

| POI | Kind | Position | Content |
|---|---|---|---|
| `boulevard_quest_board` | QUEST_BOARD | `(220, 0)` | two local quests |
| `hotel_de_ville` | INN | `(220, -120)` | inn, keeper, bed |
| `louvre_shop` | SHOP | `(-240, -60)` | shop, merchant |
| `apothecary` | HEALER | `(-240, 120)` | healer, remedies |
| `louvre_curator` | NPC | `(-240, 0)` | stolen painting |
| `dance_hall` | QUEST | `(-240, 220)` | missing dancer |
| `catacombs` | SECRET | `(0, 120)` | ossuary, hidden chamber |
| `notre_dame` | LANDMARK | `(0, -200)` | cathedral |
| `seine_quay` | LANDMARK | `(0, -40)` | quay, booksellers, bridges |

## NPCs

`hotel_keeper`, `shopkeeper`, `apothecary`, `curator`, `dance_hall_owner`, `gendarme`,
`bookseller`.

## Local quests

1. **The Stolen Painting** (`paris_stolen_painting`): a painting vanished from the Louvre.
   Trace it through the quays and catacombs; return it, keep it, or sell it.
2. **The Missing Dancer** (`paris_missing_dancer`): a dancer vanished after a Montmartre show.
   Search the dance hall and catacombs; rescue her or take the patron's offer.

## Signs

`NOTRE-DAME`, `LOUVRE`, `MONTMARTRE`, `GRANDS BOULEVARDS`, `SEINE`, `CATACOMBS`, `CITY NOTICES`,
plus quay and catacomb warnings.

## Atmosphere

Limestone, zinc roofs, café terraces, book stalls, bridges, gas lamps, dance-hall posters,
a few boarded studios, pigeons, river mist.
