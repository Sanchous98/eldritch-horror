# Tokyo — authored city brief

Footprint `halfExtent = 340`. Coordinates relative to `(cx, ground, cz)`; no layout RNG.

## Identity

Taisho-era Tokyo: Asakusa's temple and five-level pagoda, Nakamise lantern street, Ginza's
brick department stores and neon cafés, Yanaka's wooden houses and cemetery, the Sumida docks,
and a shrine hill above it all. Dense, low, wooden, with one tall pagoda.

## Macro layout

| District | X | Z | Character |
|---|---|---|---|
| Asakusa | 80..300 | -260..-20 | temple, pagoda, Nakamise shopping street, lanterns |
| Ginza | -120..120 | -80..160 | brick shops, cafés, cinema, neon, department store |
| Yanaka | -320..-140 | -260..220 | wooden houses, cemetery, cats, narrow lanes |
| Docks | 120..320 | 20..300 | warehouses, canal, fishing boats, rope |
| Shrine hill | -100..80 | -300..-140 | torii path, small shrine, crypt |

**Arrival gate**: south `(0, 320)`; signpost to Asakusa, Ginza, Yanaka, docks, shrine.

**Streets**: 12-wide main avenue `x=-6..6` north–south; 10-wide Ginza street `z=40..50`;
Nakamise 6-wide `x=150..156`; Yanaka lanes 4 wide; dock quay 8 wide.

## Landmark

**Five-level pagoda** at `(190, -160)`: stone base, five tapering wooden storeys with dark tiled
roofs and bronze bells, inside the Asakusa temple yard. The temple hall and a great lantern
gate stand beside it.

## Required POIs

| POI | Kind | Position | Content |
|---|---|---|---|
| `ginza_quest_board` | QUEST_BOARD | `(0, 70)` | two local quests |
| `ryokan` | INN | `(-80, 40)` | inn, keeper, bed |
| `department_store` | SHOP | `(40, 100)` | shop, clerk, crates |
| `herbalist` | HEALER | `(-220, 60)` | herbalist, remedies |
| `cinema` | QUEST | `(60, 20)` | cursed film reel, projectionist |
| `shrine` | QUEST | `(0, -220)` | missing miko, priest |
| `docks_warehouse` | SECRET | `(260, 180)` | warehouse, smuggled cargo |
| `pagoda_crypt` | SECRET | `(190, -160)` | crypt under the pagoda |
| `police_box` | NPC | `(0, 120)` | police box, constable |

## NPCs

`innkeeper`, `shop_clerk`, `herbalist`, `projectionist`, `shrine_priest`, `dockworker`,
`constable`.

## Local quests

1. **The Cursed Film Reel** (`tokyo_cursed_reel`): the Ginza projectionist has a reel showing a
   death that has not happened yet. Trace the film to the docks warehouse; choose to destroy it
   or deliver it to the Choir.
2. **The Missing Miko** (`tokyo_missing_miko`): the shrine maiden vanished from the hill. Follow
   the torii path, find the crypt under the pagoda, and choose to rescue her or take the relic.

## Signs

`ASAKUSA`, `GINZA`, `YANAKA`, `DOCKS`, `SHRINE`, `NAKAMISE`, `CITY NOTICES`, plus two warning
signs at the cemetery and the crypt.

## Atmosphere

Paper lanterns, wooden eaves, noren, stone foxes, cherry trees, telegraph poles, rickshaws,
canal boats, a cinema marquee, a few boarded shops, crows, incense smoke.
