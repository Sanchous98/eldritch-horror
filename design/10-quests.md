# Quests

Quest chains tie the factions, rites and map together. Objectives are typed
(`talk`, `gather`, `kill`, `rite`, `reach`) and rewards grant items, reputation and XP.

## Main chain — "The Unblinking Path" (Order / resistance)

| Step | Objective | Giver | Reward |
|---|---|---|---|
| 1 | `talk` to an Order Initiate | `unblinking_eye` | XP, `tome_of_the_eye` |
| 2 | `gather` 4× `chalk_reagent` | `unblinking_eye` | `mark_of_favour`, reputation +10 |
| 3 | `rite` perform `ward_of_the_eye` | `unblinking_eye` | unlock `warding` skill branch |
| 4 | `reach` an `order_vault` | `unblinking_eye` | `silver_blade`, reputation +15 |
| 5 | `kill` a `star_spawn` | `unblinking_eye` | `warden_sigil`, reputation +20 |
| 6 | `rite` perform `close_rift` at a `rift_scar` | `unblinking_eye` | `rite_of_cleansing` unlocked |

## Choir chain — "What the Tide Took"

| Step | Objective | Giver | Reward |
|---|---|---|---|
| 1 | `talk` to a Tidebound | `drowned_choir` | `tome_of_tides` |
| 2 | `gather` 2× `pearl_reagent` | `drowned_choir` | `drowned_lung`, reputation +10 |
| 3 | `rite` perform `drowned_blessing` | `drowned_choir` | reputation +15 |
| 4 | `kill` the `choir_leviathan` **or** soothe it | `drowned_choir` | reputation +30, `mark_of_favour` |
| 5 | `reach` a `drowned_temple` altar | `drowned_choir` | deep-rite access |

## Hollow chain — "The Hollowing" (endgame)

| Step | Objective | Giver | Reward |
|---|---|---|---|
| 1 | `talk` to an Aspirant at a `cult_stronghold` | `hollow_choir` | `hollow_text` |
| 2 | *Requires* corruption stage `marked` | `hollow_choir` | unlock `summoning` skill |
| 3 | `rite` perform `summon_star_spawn` | `hollow_choir` | reputation +20, `star_codex` |
| 4 | `rite` perform `open_rift` in a storm | `hollow_choir` | reputation +25 |
| 5 | `reach` a `rift_gate` | `hollow_choir` | endgame access |

## Side / repeatable quests

| id | Name | Giver | Objective | Repeat |
|---|---|---|---|---|
| `cleansing_contract` | Cleansing Contract | `unblinking_eye` | `rite` `rite_of_cleansing` | weekly |
| `tide_offering` | Tide Offering | `drowned_choir` | `gather` fish/pearls | daily |
| `star_hunt` | Star Hunt | `unblinking_eye` | `kill` star-spawn | daily |
| `rift_scout` | Rift Scout | any | `reach` a `rift_scar` and return | once per rift |

## Investigator field-kit chains

Every investigator earns their starting kit instead of receiving it at the pedestal (design/14).
Two tasks per investigator: the first pays the kit **minus** the signature item, the second pays the
signature item. The second is gated on the first; the first can be worked on as soon as the investigator is chosen. Only the chosen investigator's chain appears in the journal and advances.

| Investigator | First task (gear) | Second task (calling) |
|---|---|---|
| `eleanor_vance` | pick up 4 ink sacs | discover 2 codex entries |
| `jack_corrigan` | pick up 4 ink sacs | kill 3 zombies |
| `tom_mallory` | pick up 6 raw iron | kill 5 zombies |
| `cormac_blackwood` | pick up 6 string | kill 3 spiders |
| `sister_agatha` | pick up 4 bone | discover 1 codex entry |
| `marion_delacroix` | pick up 4 white wool | discover 2 codex entries |
| `vera_nightingale` | craft 6 ritual chalk | perform `ward_of_the_eye` |
| `nikolai_volkov` | pick up 3 leather | discover 2 codex entries |
| `dr_amos_hartley` | pick up 6 wheat | kill 2 zombies |
| `evelyn_ashcombe` | pick up 2 raw gold | discover 1 codex entry |
| `aldous_pemberton` | pick up 6 clay balls | discover 2 codex entries |
| `hazel_quinn` | pick up 4 feathers | discover 1 codex entry |

The materials are deliberately ordinary drops rather than crafted results, so a pickup counts
without asking the player to throw the item away (no kit is required); the codex steps accept any
new entry, including a bestiary kill or a site seen by proximity. The occultist's rite step is reachable because
her gear grants chalk and salt comes from smelting sugar — the first lesson of the ritual loop.

### Schema additions

A quest file may now carry, besides the original fields:

- `investigator` — restrict the quest to one investigator (empty = everyone).
- `trigger` — `pickup`, `kill`, `craft`, `rite` or `codex`; empty means the code advances it by id.
- `param` — the item / entity / rite id the event must carry; `""` for any codex entry.
- `reward_kit` — investigator id: pays that kit from the `Investigators` table minus the signature.
- `reward_signature` — pays the investigator's signature item (its owner is `investigator`).

`quest/QuestTriggers` matches data-driven quests on every pickup, player kill, craft, rite and codex
discovery and routes them through `QuestJournal.add`, so prerequisites, cooldowns, the investigator
gate and rewards stay in one place.

## Design notes

- Quest definitions are data (see the *Quest data format* story); objectives register by type.
- **Opposed cults:** advancing an Order quest can lower Choir standing, and vice versa.
- Rewards feed progression (XP, skills), reputation, and the item economy.
- All dialogue and objective text is localization-ready.

## Open questions

- Whether the main/Choir chains are mutually exclusive after a point.
- How repeatables scale with player level and corruption stage.
