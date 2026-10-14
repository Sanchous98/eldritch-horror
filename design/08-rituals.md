# Rituals

Rites are performed at an altar, match a block pattern, consume offerings, are gated by
conditions, and resolve to one or more outcomes. A failed or refused rite **always costs
something** (sanity or corruption) — never a silent no-op.

**Outcome types:** `spawn`, `grant`, `transform`, `curse`, `open_rift`, `close_rift`,
`grant_skill`, `reputation`, `sanity`, `corruption`.

| id | Name | Tier | Conditions | Offerings | Outcomes | Cost on failure |
|---|---|---|---|---|---|---|
| `ward_of_the_eye` | Ward of the Eye | 1 | night, clear weather | 4× `salt_reagent`, 1× `chalk_reagent` | `grant` ward token, `corruption −3` | `sanity −8` |
| `drowned_blessing` | Drowned Blessing | 1 | near water, rain or high tide | 2× `pearl_reagent`, 1× `fish_offering` | `grant_skill` `water_breathing` (temp), `reputation` `drowned_choir +10` | `sanity −10` |
| `call_the_lesser` | Call the Lesser | 2 | midnight, moon full or new | 4× `bone_reagent`, 1× `blood_offering` | `spawn` `lesser_swarm` ×3 | `sanity −15`, `corruption +5` |
| `summon_star_spawn` | Summon Star-Spawn | 3 | midnight, `min_corruption_stage: touched` | 8× `star_reagent`, 2× `blood_offering` | `spawn` `star_spawn` ×1, `corruption +12` | `sanity −20`, `corruption +8` |
| `open_rift` | Open the Way | 3 | `min_corruption_stage: marked`, storm | 6× `void_reagent`, 4× `blood_offering` | `open_rift`, `corruption +15` | `sanity −25`, `corruption +10` |
| `close_rift` | Close the Way | 2 | rift within 8 blocks | 8× `salt_reagent`, 2× `chalk_reagent` | `close_rift`, `sanity +10` | `corruption +5` |
| `rite_of_cleansing` | Rite of Cleansing | 3 | `max_corruption_stage: marked` | 4× `silver_reagent`, 2× `prayer_bead` | `corruption −20` | `sanity −15`, `corruption +5` |
| `respec` | Rite of Unmaking | 3 | `min_corruption_stage: touched` | 4× `mirror_shard`, 4× `silver_reagent` | `grant_skill` reset, refund 80% points | `sanity −20` |
| `blight_the_earth` | Blight the Earth | 2 | none | 4× `bone_reagent`, 1× `blood_offering` | `transform` the ground (taint +0.30), `corruption +4`, `reputation` `hollow_choir +5` | `sanity −12` |
| `take_the_hollow_mark` | Take the Hollow Mark | 2 | none | 2× `blood_offering` | `curse` weakness 60s, `corruption +5`, `reputation` `hollow_choir +10` | `sanity −12` |

The last two are shipped additions: the seed table needed content that exercises the `transform`
and `curse` outcome types. They are taught by the Hollow Choir (`cult/Cults.java`), like the
rest of its branch.

## Conditions vocabulary

`time` (midnight/noon/dusk…), `moon_phase` (full/new/…), `weather` (storm/rain/clear),
`dimension`, `min_corruption_stage`, `max_corruption_stage`, `near_water`, `rift_near`.

## Altar tiers

| Altar | Unlocks |
|---|---|
| Crude Altar | Tier 1 |
| Consecrated Altar | Tier 2 |
| Blasphemous Altar | Tier 3 |
| Choir Altar | Endgame outcomes |

## Design notes

- **Learning:** possessing a definition is not enough; a rite must be *learned* (tome or cult).
- **Backlash:** failure applies the listed cost so experimentation has weight.
- **Atomicity:** offerings are consumed only on a successful validation, all-or-nothing.
- **Pattern:** small for tier 1, growing with tier (see `map.md` structures for the altar layout).
- **Implemented (completion pass):** every outcome type in the vocabulary resolves. A rite that
  takes effect applies its Outcome deltas (san/meter, corruption) and, when it is a cult's rite,
  that cult's reputation; a rite that was started and failed (unmet condition, refused outcome)
  applies the Cost on failure column instead and spends no offerings. A rite that never starts
  (no altar, missing offerings, cooldown) costs nothing.

## Open questions

- Matcher strategy: reuse structure templates or a bespoke pattern matcher (see ARCHITECTURE).
- Whether a failed rite can still consume offerings at high corruption.
