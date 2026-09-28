# Skills

Skill trees group nodes by *focus*. Nodes have tiers, a cost in points, prerequisites,
and one or more effects. A node's `tag` list drives filtering and content gating.

Trees: **investigation**, **occultism**, **corruption**, **divination**, **blood_magic**,
**summoning**. The tables below list the nodes currently defined.

## investigation

| id | Name | Tier | Cost | Prerequisites | Effect |
|---|---|---|---|---|---|
| `iron_will` | Iron Will | 1 | 1 | — | `sanity_drain −15%` |
| `observe` | Observe | 1 | 1 | — | `reveal_radius +16` (rifts, corruption nodes) |
| `warding` | Warding | 2 | 2 | `iron_will` | `corruption_spread_nearby −40%` (place wards) |

## occultism

| id | Name | Tier | Cost | Prerequisites | Effect |
|---|---|---|---|---|---|
| `forbidden_lore` | Forbidden Lore | 1 | 1 | — | `tome_sanity_cost −20%` |
| `rite_affinity` | Rite Affinity | 1 | 1 | — | `ritual_speed +10%` |
| `divination` | Divination | 2 | 2 | `forbidden_lore` | `inspect_eldritch +1` (see effect tags on sight) |
| `lucid_dreams` | Lucid Dreams | 2 | 2 | `forbidden_lore` | `dream_visions +1` (sleep can teach a rite) |

## corruption

| id | Name | Tier | Cost | Prerequisites | Effect |
|---|---|---|---|---|---|
| `devotion` | Devotion | 1 | 1 | — | `cult_reputation_gain +50%` |
| `blood_offering` | Blood Offering | 1 | 1 | — | `allow_blood_cost +1` (pay offerings in health) |
| `touched_mind` | Touched Mind | 2 | 2 | `devotion` | `corruption_gain +25%`, `ritual_power +20%` |
| `summoning` | Summoning | 3 | 3 | `touched_mind`, `rite_affinity` | unlock rites tagged `summoning` |
| `eldritch_pact` | Eldritch Pact | 4 | 5 | `summoning` | `horror_awareness +1`, `corruption_floor = 50` (capstone) |

## Design notes

- **Tiers** gate depth: tier 1 is reachable in the first hour; the capstone is endgame.
- **Costs** rise faster than effects so builds stay narrow.
- **Capstones change the rules**, not just numbers (`eldritch_pact` sets a corruption floor).
- Every effect maps to a `stat`; keep the vocabulary small and documented here.

## Open questions

- Respec cost and frequency (see the *Respec ritual* story).
- Whether skill points are shared with quest rewards or separate.
