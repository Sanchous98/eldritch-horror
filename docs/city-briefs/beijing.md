# Beijing — authored city brief

Status: **queued** (after Shanghai). Footprint: `halfExtent = 340` (680×680).
Coordinates relative to `(cx, ground, cz)`: `+x` east, `+z` south, `+y` up. No layout RNG.

## Identity

Imperial Beijing at the end of the dynasty: the Forbidden City's yellow roofs behind red walls,
Tiananmen Square, grey hutong lanes with courtyard houses, the blue-tiled Temple of Heaven, the
silk market, the railway/legation quarter, and the canal. The palace is the skyline; the hutongs
are the maze; the market is the noise.

## Macro layout

| District | X | Z | Character |
|---|---|---|---|
| Forbidden City | -130..130 | -300..-70 | red walls, moat, gates, three courtyards, Hall of Supreme Harmony |
| Tiananmen Square | -120..120 | -50..80 | open paved square, monument, quest board, crowds |
| Hutongs | -330..-150 | -280..220 | grey brick courtyard houses, 4–6 wide lanes, wells, secret |
| Temple of Heaven | -90..90 | 140..290 | circular altar, blue roof, park, priest |
| Silk Market | 140..320 | -90..120 | shops, tea house, inn, pickpockets, awnings |
| Railway / Legation | 140..330 | 140..310 | station, warehouses, foreign legation, canal dock |
| Canal | 120..145 | -300..310 | water strip, two stone bridges |

**Arrival gate**: south edge `(0, 320)`, opening onto the square; signpost points to palace,
temple, hutongs, market and station.

**Streets**: a 14-wide north–south avenue `x=-7..7` from the gate to the palace; a 12-wide east–
west avenue `z=-7..7`; hutong lanes 4–6 wide; market lanes 8 wide; quay road 10 wide.

## Landmark

**Hall of Supreme Harmony** at `(0, -200)`: a long imperial hall on a three-tier stone terrace,
red columns, yellow double roof, flanked by two side halls, inside the walled Forbidden City.
The Meridian Gate at `(0, -300)` is the formal entrance; the moat surrounds the walls.

## Required POIs

| POI | Kind | Relative position | Content |
|---|---|---|---|
| `square_quest_board` | QUEST_BOARD | `(0, 60)` | city board, two local quests |
| `palace_clerk` | NPC | `(0, -90)` | palace clerk, sealed-decree quest |
| `hutong_elder` | NPC | `(-240, -120)` | elder, missing-eunuch quest, well secret |
| `temple_priest` | HEALER | `(0, 220)` | priest/healer, decodes the decree |
| `silk_merchant` | SHOP | `(220, 20)` | shop, silk, rumours |
| `tea_house` | INN | `(220, -40)` | tea house, keeper, bed |
| `railway_station` | LANDMARK | `(220, 250)` | station, stationmaster, cargo |
| `legation` | SECRET | `(300, 200)` | foreign legation, locked archive |
| `hutong_well` | SECRET | `(-240, -120)` | well/tunnel to a hidden archive |
| `canal_bridge` | LANDMARK | `(132, 0)` | stone bridge, moored boats |

## NPCs

`palace_clerk`, `hutong_elder`, `temple_priest`, `silk_merchant`, `stationmaster`, `legate`.

## Local quests

1. **The Missing Eunuch** (`beijing_missing_eunuch`): the hutong elder's neighbour vanished with
   a palace seal. Search the well/tunnel, find the archive, choose to return the seal to the
   palace (Order) or sell it to the legation (Choir).
2. **The Sealed Decree** (`beijing_sealed_decree`): the palace clerk has a coded edict; the
   temple priest can decode it, but the silk merchant wants a copy. Choose who receives it.

## Signs

`FORBIDDEN CITY`, `TEMPLE OF HEAVEN`, `HUTONGS`, `SILK MARKET`, `RAILWAY`, `LEGATION QUARTER`,
`CITY NOTICES`, plus two warning signs at the moat/well.

## Atmosphere

Yellow roof tiles, red walls, grey hutong brick, stone lions, lanterns, bicycles/rickshaws,
market awnings, canal boats, coal smoke, propaganda posters (1920s warlord era), a few boarded
shops, crows on the palace roof.

## Acceptance

- `cityId()` = `beijing`, `halfExtent()` = 340; all POIs inside `[-340,340]`.
- Arrival gate → square → palace axis is walkable; the well secret is reachable from the hutong.
- No building overlaps the avenues, canal or palace walls; every door faces a street/courtyard.
- No RNG, no shared-file edits, registry entry added by the lead.
