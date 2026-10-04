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
| `eldritch_horror:threshold` | The Threshold | one-time prologue | No drain; the class/city prologue ([29-prologue.md](29-prologue.md)) |

There is **no traversable Veil dimension** (decided). "The Veil" stays only as the *idea* of the
barrier between the known world and what is behind it: it manifests as rifts and events in the
Overworld, never as a place to visit. The edge of the charted world is **Morok** (the lethal polar
repeat, [23-boundary-and-travel.md](23-boundary-and-travel.md)); the horror behind the veil is
reached *through* a rift, and nothing ever leaves the Overworld. A playable Veil plane is backlog,
and only if it earns a mechanic no other system already provides.

## Structures

| id | Name | Where | Purpose |
|---|---|---|---|
| `ritual_altar_site` | Ritual Altar Site | Overworld, corrupted | Perform rites; small block pattern |
| `drowned_temple` | Drowned Temple | Coast / `drowned_marsh` | Choir site; quests; Leviathan arena |
| `order_vault` | Order Vault | `observatory_plateau` | Tomes, silver, cleansing services |
| `cult_stronghold` | Cult Stronghold | Overworld, generated | Faction base; worship; shops |
| `rift_scar` | Rift Scar | `ashen_waste` | A stable opening; corruption source |
| `rift_gate` | Rift Gate | Deep rifts | A two-way anchor for the rift network; opens passages within the Overworld |
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
- Only the Overworld is populated; the Threshold is the one added dimension (prologue only). A
  playable Veil plane is backlog.

## Open questions

- Rift network linking rules (see the *Rift network & linking* story).
