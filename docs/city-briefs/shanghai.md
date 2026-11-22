# Shanghai — authored city brief

Status: **ready for the city agent** (framework: `docs/CITY-PLAN-CONTRACT.md`).
Footprint: `halfExtent = 340` (680×680 blocks), centre at the curated Shanghai coordinate.
All coordinates below are relative to `(cx, ground, cz)`: `+x` east, `+z` south, `+y` up.
No layout RNG; every block is placed by the plan.

## Identity

Treaty-port Shanghai, 1920s: the Bund's grey stone waterfront, Nanjing Road's neon shops, the
Old City's crooked lanes and tea houses, the French Concession's plane trees and villas, and
Pudong's docks under the Pearl Tower. The city should read as five connected districts, not a
grid of identical houses.

## Macro layout

| District | X | Z | Character |
|---|---|---|---|
| The Bund | 90..150 | -260..260 | waterfront promenade, colonial facades, customs house, hotel |
| Nanjing Road | -320..150 | -12..12 | shopping spine, department store, cinema, tea house |
| People's Square | -90..60 | -70..70 | paved plaza, monument, quest board, street musicians |
| Old City | -280..-90 | 70..280 | temple, Yu Garden, zigzag bridge, herbalist, tea house, cellar |
| French Concession | -280..-70 | -280..-70 | villas, plane trees, café, clinic, Order safehouse |
| Pudong / docks | 190..340 | -280..280 | warehouses, quays, dockworkers, Pearl Tower |

**Huangpu River**: authored water strip `x = 155..190`, `z = -340..340`, with stone quays at
`x=150..155` and `x=190..195`. The Bund faces it; Pudong is the far bank. Bridges at `z=-40`
and `z=140` (stone arch, 8 wide) connect the banks.

**Arrival gate**: west edge at `(-320, 0)`, opening onto Nanjing Road. A signpost immediately
inside the gate points to the five districts.

**Streets** (authored rectangles, paving + kerbs):
- Nanjing Road: `z=-7..7`, `x=-320..150`, width 14.
- Bund promenade: `x=120..150`, `z=-260..260`; facades on `x=90..120`.
- People's Square ring road: a 12-wide loop around the plaza.
- Old City lanes: `x=-260..-110` every 35, `z=90..260` every 35, width 6; one crooked lane
  (`x=-200..-140`, `z=120..200`) deliberately bends around the tea house.
- French Concession avenues: `x=-260..-90` every 45, `z=-260..-90` every 45, width 8;
  plane trees along both kerbs.
- Pudong quay road: `x=210..330`, `z=-250..250`; warehouse aprons every 60 in `z`.

## Landmark

**Pearl Tower** at `(260, 0)`: a 90-block Art-Deco column on a colonnaded base, three spheres
(observation decks) and a spire. A paved plaza `(230..290, -30..30)` with a low wall and two
neon sign pylons. Visible from the Bund across the river.

## Required POIs

| POI | Kind | Relative position | Content |
|---|---|---|---|
| `bund_quest_board` | QUEST_BOARD | `(132, 0)` | city board; two local quests below |
| `astor_house` | INN | `(105, -80)` | hotel, clerk NPC, rentable room (sign + bed) |
| `wing_on` | SHOP | `(20, -20)` | department store, shopkeeper, crates |
| `customs_house` | LANDMARK | `(105, 60)` | customs hall, clock, clerk |
| `herbalist` | HEALER | `(-150, 150)` | herbalist NPC, shelves, cauldron |
| `tea_house` | INN | `(-180, 190)` | Yu Garden tea house, keeper, cellar stairs |
| `order_safehouse` | SECRET | `(-220, -170)` | unmarked door, Order agent, ledger |
| `dock_7` | QUEST | `(250, 120)` | warehouse, crane, cult shrine in the back |
| `french_cafe` | QUEST | `(-160, -150)` | café owner, missing-violinist quest |
| `old_city_cellar` | SECRET | `(-190, 210)` | cellar under the tea house; chest + lore book + cult relic |
| `pearl_tower` | LANDMARK | `(260, 0)` | tower + plaza |
| `gate_signpost` | SIGN | `(-312, 0)` | five-direction signpost |

## NPCs (anchors in `CityPoi`)

- `clerk` — Astor House, nervous, points to the quest board.
- `herbalist` — Old City, healer dialogue, sells/offers a remedy.
- `cafe_owner` — French Concession, gives *The Vanishing Violinist*.
- `order_agent` — safehouse, gives *The Bund Cargo* and a choice.
- `dockworker` — Dock 7, strike talk, hints at the shrine.
- `cult_defector` — cellar, secret dialogue, can be spared or reported.

## Local quests

1. **The Bund Cargo** (`shanghai_bund_cargo`, 2 steps): the customs clerk/Order agent asks you
   to inspect Dock 7. Find the manifest in the warehouse, then either burn the cult shrine
   (Order) or take the relic (Choir). Reward: Order/Choir reputation + a city-specific item.
2. **The Vanishing Violinist** (`shanghai_vanishing`, 3 steps): the café owner's lodger is
   missing. Search the villa, find the cellar under the tea house, and choose to rescue the
   violinist or take the cult's offer. Reward: a unique instrument/lore book + reputation.

Both quests must be offered from the board/NPC, advance on real interactions, and never
require `/eh` cheats.

## Signs (minimum)

- Arrival signpost: `THE BUND ->`, `NANJING ROAD ->`, `OLD CITY v`, `FRENCH CONCESSION ^`,
  `PUDONG DOCKS ->`.
- District entry signs at each street mouth.
- Shop signs: `WING ON`, `ASTOR HOUSE`, `CUSTOMS`, `TEA HOUSE`, `HERBALIST`, `CAFE`.
- Dock warning: `DOCK 7 - NO ENTRY`, `BEWARE THE TIDE`.
- One quest-board header sign: `CITY NOTICES`.

## Atmosphere / set dressing

- Bund: lamp posts, stone lions, moored sampans, crates, a clock.
- Nanjing Road: neon sign bands, awnings, rickshaws (minecarts/boats as props), market stalls.
- Old City: zigzag bridge over a pond, willow trees, incense, hanging lanterns, laundry lines.
- French Concession: plane trees, iron railings, balconies, a fountain, a small clinic.
- Pudong: cranes, coal piles, rope, barrels, a flooded warehouse, strike banners.
- Decay: a few shelled/boarded buildings, rubble, crows; not uniform ruin.

## Acceptance

- Plan compiles with no shared-file edits; `halfExtent()` returns 340; `cityId()` is `shanghai`.
- Every block is written through `StructureBuilder`; no `RandomSource`, no static mutable state.
- All POIs lie inside `[-340,340]`; no building overlaps a street/quay; doors face a street.
- The arrival gate signpost and the quest board are reachable on foot from the gate.
- `CityPlans` registration is added by the lead, not by the city agent.
