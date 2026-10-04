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

## Design notes

- Quest definitions are data (see the *Quest data format* story); objectives register by type.
- **Opposed cults:** advancing an Order quest can lower Choir standing, and vice versa.
- Rewards feed progression (XP, skills), reputation, and the item economy.
- All dialogue and objective text is localization-ready.

## Open questions

- Whether the main/Choir chains are mutually exclusive after a point.
- How repeatables scale with player level and corruption stage.
