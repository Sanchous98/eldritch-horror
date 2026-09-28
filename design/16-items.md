# Items

Categories: **reagents**, **artefacts**, **tomes**, **gear**, **currency**, **cursed**.
IDs are `snake_case`. Every item needs a lang entry and a tooltip.

## Reagents

Material components consumed by rites.

| id | Name | Source | Used by |
|---|---|---|---|
| `salt_reagent` | Consecrated Salt | Order vaults, warding | `ward_of_the_eye`, `close_rift` |
| `chalk_reagent` | Ritual Chalk | Crafted | `ward_of_the_eye`, `close_rift` |
| `bone_reagent` | Grave Bone | Skeletons, ossuaries | `call_the_lesser` |
| `star_reagent` | Star-Iron Fragment | Meteors, the Veil | `summon_star_spawn` |
| `void_reagent` | Void Residue | Rifts, corrupted chunks | `open_rift` |
| `silver_reagent` | Moonlit Silver | Order trade, ruined vaults | `rite_of_cleansing`, `respec` |
| `pearl_reagent` | Drowned Pearl | Coast, Choir trade | `drowned_blessing` |
| `blood_offering` | Blood Offering | Player (costs health) or mobs | summoning/open rites |

## Artefacts

Unique-effect items (sidegrades with a cost).

| id | Name | Effect | Drawback |
|---|---|---|---|
| `eye_of_the_watcher` | Eye of the Watcher | See rifts and entity effects through walls | Sanity drains while equipped |
| `drowned_lung` | Drowned Lung | Breathe underwater indefinitely | Corruption +slowly |
| `mirror_shard` | Mirror Shard | Reveals a nearby rite/secret | Occasional sanity jolt |
| `warden_sigil` | Warden's Sigil | −40% corruption spread within 16 blocks | −10% ritual power |
| `prophet_mask` | Prophet's Mask | +25% summoning power | Villagers flee; sanity floor lower |

## Tomes

Knowledge items. Reading grants a known rite or lore and applies a cost.

| id | Name | Grants | Cost |
|---|---|---|---|
| `tome_of_the_eye` | Tome of the Unblinking Eye | `ward_of_the_eye` | `sanity −10` |
| `tome_of_tides` | Tome of Tides | `drowned_blessing` | `sanity −10` |
| `hollow_text` | Hollow Text | `call_the_lesser` | `sanity −15`, `corruption +8` |
| `star_codex` | Star Codex | `summon_star_spawn` | `sanity −20`, `corruption +10` |

## Gear

| id | Name | Slot | Effect |
|---|---|---|---|
| `occult_tool` | Occultist's Implement | mainhand | +ritual speed |
| `silver_blade` | Silver Blade | mainhand | +damage vs. eldritch |
| `investigator_coat` | Investigator's Coat | chest | −sanity drain in darkness |
| `choir_robe` | Choir Robe | chest | +reputation gain, +corruption gain |

## Currency

| id | Name | Source |
|---|---|---|
| `mark_of_favour` | Mark of Favour | Cult rewards; spend with cults |
| `relic_coin` | Relic Coin | Ruins, structures |

## Cursed items

| id | Name | Boon | Curse |
|---|---|---|---|
| `hollow_pact_blade` | Hollow Pact Blade | Massive damage | Sanity drain on kill; corruption +1/kill |
| `grasping_idol` | Grasping Idol | Attracts loot | Attracts hostile eldritch entities |
