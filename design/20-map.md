# Map — biomes, dimensions, structures

> **World map:** the Overworld is a **procedural, fixed-seed, Earth-like landmass** with
> **vanilla biomes** (16k × 16k, world-bordered). See [`../docs/WORLDGEN.md`](../docs/WORLDGEN.md).
> The "corrupted biomes" below are **not** worldgen biomes — they are regions the
> **corruption field** converts at runtime (see [03b-corruption.md](03b-corruption.md)).

## Biomes (corruption states)

These are **overlays applied by the corruption field**, not worldgen biomes. Each is a
converted form of the underlying vanilla biome. Each supplies a mob list and a resource list.

| id | Name | Where | Feel | Notable content |
|---|---|---|---|---|
| `blighted_woods` | Blighted Woods | Overworld, spreading | Grey trees, drifting spores | `tainted_fauna`, corruption nodes |
| `drowned_marsh` | Drowned Marsh | Overworld coast | Fog, brackish water, sunken idols | Choir sites, `pearl_reagent` |
| `ashen_waste` | Ashen Waste | Overworld, rift scars | Ash, dead soil, twisted spires | `void_reagent`, rifts |
| `observatory_plateau` | Observatory Plateau | Overworld highlands | Clear skies, strange monoliths | Order outposts, star-iron |

They expand from rifts and altars as the corruption field grows and recede after cleansing.

> The world itself is generated as **continents and oceans with vanilla biomes**
> ([`../docs/WORLDGEN.md`](../docs/WORLDGEN.md)); the above are corruption-field states laid
> over that map, not separate worldgen biomes.

## Dimensions

| id | Name | Access | Rules |
|---|---|---|---|
| `overworld` | Overworld | — | Baseline sanity/corruption |
| `the_veil` | The Veil | `open_rift` / portal rite | Sanity drains constantly; corruption gains doubled; `shoggoth_mass`, `star_spawn` |

### The Veil

- **Fantasy:** the plane behind the veil — where the horror actually lives.
- **Terrain:** non-Euclidean, shifting; landmarks move between visits (data-driven seeds).
- **Rules:** constant sanity drain; ambient corruption; no natural day.
- **Content:** `void_reagent`, star-iron, the deeper rite tiers, `shoggoth_mass`.
- **Travel:** stable and reversible (a return anchor is set on entry).

## Structures

| id | Name | Where | Purpose |
|---|---|---|---|
| `ritual_altar_site` | Ritual Altar Site | Overworld, corrupted | Perform rites; small block pattern |
| `drowned_temple` | Drowned Temple | Coast / `drowned_marsh` | Choir site; quests; Leviathan arena |
| `order_vault` | Order Vault | `observatory_plateau` | Tomes, silver, cleansing services |
| `cult_stronghold` | Cult Stronghold | Overworld, generated | Faction base; worship; shops |
| `rift_scar` | Rift Scar | `ashen_waste` | A stable opening; corruption source |
| `rift_gate` | Veil Gate | Deep rifts | Two-way travel anchor |
| `settlement` | Settlement | Land, temperate/coastal (real city coords) | Civilian hub: trade, inn, quests — see [`21-settlements.md`](21-settlements.md) |

### Ritual Altar Site (pattern, tier 1)

```
. . . . .
. S S S .
. S A S .      A = altar   S = eldritch stone
. S S S .      . = air / rune chalk outline
. . . . .
```

Tier 2/3 patterns add rune rings and required rune blocks; the full pattern set is
per-rite (see `rituals.md`).

## Design notes

- Worldgen is compatibility-sensitive: features and placement are datapack-driven.
- Corruption spreads *into* the world; biomes are symptoms, not fixed regions.
- The Veil is the only dimension planned for the first release; more are backlog.

## Open questions

- Whether the Veil is a single shared plane or per-player instanced.
- Rift network linking rules (see the *Rift network & linking* story).
