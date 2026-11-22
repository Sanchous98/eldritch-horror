# Moscow — authored city brief

Footprint `halfExtent = 340`. Coordinates relative to `(cx, ground, cz)`; no layout RNG.

## Identity

Moscow between revolutions: St Basil's coloured onion domes on Red Square, the Kremlin wall,
the metro's marble halls, Arbat's apartments and cafés, the industrial district's chimneys, and
snow. Red brick, gold, iron, and cold.

## Macro layout

| District | X | Z | Character |
|---|---|---|---|
| Red Square / St Basil's | -140..140 | -300..-100 | cathedral, square, Kremlin wall, mausoleum |
| Kremlin | 140..320 | -300..-80 | wall, towers, palace, armory, garden |
| Arbat | -320..-140 | -240..200 | apartments, cafés, shops, courtyards |
| Metro / station | -120..120 | -60..120 | station, marble hall, escalators, tunnel |
| Industrial district | 140..320 | 120..300 | factories, chimneys, workers' housing, rail |
| River | -320..320 | 160..190 | embankment, bridges, barges |

**Arrival gate**: south `(0, 320)`; signpost to Red Square, Kremlin, Arbat, metro, factories.

**Streets**: 16-wide axis; Arbat lanes 6 wide; industrial grid 10 wide; embankment 8 wide;
metro entrance plaza.

## Landmark

**St Basil's Cathedral** at `(0, -200)`: a central tent tower with a gold onion dome, eight
smaller coloured onion domes on drum bases, red-brick walls and a low arcade.

## Required POIs

| POI | Kind | Position | Content |
|---|---|---|---|
| `square_quest_board` | QUEST_BOARD | `(0, -120)` | two local quests |
| `metropol_hotel` | INN | `(-80, -120)` | inn, keeper, bed |
| `arbat_shop` | SHOP | `(-240, -60)` | shop, merchant |
| `herbalist` | HEALER | `(-240, 120)` | healer, remedies |
| `icon_chapel` | QUEST | `(-260, -120)` | stolen icon |
| `commissar_office` | NPC | `(240, -160)` | missing commissar |
| `metro_tunnel` | SECRET | `(0, 40)` | tunnel, hidden room |
| `kremlin_gate` | LANDMARK | `(160, -160)` | gate, guards |
| `factory` | LANDMARK | `(240, 240)` | factory, chimneys, rail |

## NPCs

`hotel_keeper`, `merchant`, `herbalist`, `priest`, `commissar`, `station_chief`, `worker`.

## Local quests

1. **The Stolen Icon** (`moscow_stolen_icon`): a chapel icon vanished. Trace it through Arbat
   to the metro tunnel; return it, keep it, or sell it to the Choir.
2. **The Missing Commissar** (`moscow_missing_commissar`): a commissar disappeared after a
   factory meeting. Search the factory and metro; choose to rescue him or take his papers.

## Signs

`RED SQUARE`, `KREMLIN`, `ARBAT`, `METRO`, `FACTORIES`, `CITY NOTICES`,
plus metro and factory warnings.

## Atmosphere

Red brick, gold domes, snow, iron railings, samovars, propaganda posters, steam, factory smoke,
a few boarded shops, crows.
