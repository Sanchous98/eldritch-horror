# Sydney — authored city brief

Footprint `halfExtent = 340`. Coordinates relative to `(cx, ground, cz)`; no layout RNG.

## Identity

Sydney between the wars: the harbour and its ferries, the Opera House's white sails, The Rocks'
sandstone lanes, Bondi's beach, the rail yards, and the bush beyond. Sandstone, eucalyptus,
salt, and sun.

## Macro layout

| District | X | Z | Character |
|---|---|---|---|
| Harbour / Opera House | -140..140 | -260..-80 | sails, promenade, ferries, quay |
| The Rocks | -320..-140 | -240..160 | sandstone lanes, pubs, terraces, market |
| Rail yards | 140..320 | -240..120 | station, sheds, coal, workshops |
| Bondi | 140..320 | 160..320 | beach, surf sheds, pavilion, cliffs |
| Botanic / domain | -120..120 | -60..180 | gardens, avenue, museum, fountain |
| Sea cave | 180..260 | 220..300 | cliff path, cave, secret |

**Arrival gate**: south `(0, 320)`; signpost to harbour, Rocks, rail yards, Bondi.

**Streets**: 14-wide axis; Rocks lanes 5 wide; quay 12 wide; rail road 10 wide; beach road 8 wide.

## Landmark

**Opera House** at `(0, -200)`: stacked white shell roofs on a sandstone podium, facing the
harbour, with a broad promenade and ferry wharf.

## Required POIs

| POI | Kind | Position | Content |
|---|---|---|---|
| `harbour_quest_board` | QUEST_BOARD | `(0, -120)` | two local quests |
| `rocks_pub` | INN | `(-240, -60)` | inn, keeper, bed |
| `rocks_shop` | SHOP | `(-240, 60)` | shop, merchant |
| `apothecary` | HEALER | `(-200, 120)` | healer, remedies |
| `ferry_master` | NPC | `(220, -120)` | missing sailor |
| `customs` | QUEST | `(240, 0)` | smuggled cargo |
| `sea_cave` | SECRET | `(220, 260)` | cave, chests/lore |
| `opera_house` | LANDMARK | `(0, -200)` | sails |
| `bondi_pavilion` | LANDMARK | `(240, 220)` | beach pavilion |

## NPCs

`publican`, `shopkeeper`, `apothecary`, `ferry_master`, `customs_officer`, `surf_lifesaver`,
`constable`.

## Local quests

1. **The Missing Sailor** (`sydney_missing_sailor`): a ferry hand vanished after a night run.
   Trace him through the Rocks to the sea cave; rescue him or take the smuggler's cargo.
2. **The Smuggled Cargo** (`sydney_smuggled_cargo`): customs suspects a crate landed at the
   quay. Follow it through the rail yards and the cave; expose it or sell it.

## Signs

`OPERA HOUSE`, `THE ROCKS`, `RAIL YARDS`, `BONDI`, `BOTANIC GARDEN`, `CITY NOTICES`,
plus harbour and cliff warnings.

## Atmosphere

Sandstone, white sails, ferries, eucalyptus, surf, pubs, coal smoke, beach umbrellas, a few
boarded terraces, gulls, heat.
