# Bosses

Bosses are gated by progression and solvable through **rites or combat** — not only
brute force. Each has phases and a documented arena.

| id | Name | Gate | Phases | Reward |
|---|---|---|---|---|
| `choir_leviathan` | Choir Leviathan | Drowned Choir questline | 3 | `drowned_lung`, tide reagents |
| `the_horror` | The Horror | Endgame path | 4 | Endgame (banish or summon) |

## Choir Leviathan

- **Where:** a drowned temple offshore, summoned via `drowned_blessing` chain.
- **Fantasy:** a tide-thing the Choir keeps half-asleep.
- **Phases:**
  1. *Stirring* — slow sweeps, summons lesser swarms.
  2. *Tidebreak* — arena floods; drowning zones; must use water breathing.
  3. *Frenzy* — fast, enraged; weak to silver and warding.
- **Mechanics:** flooding arena, summons, a `ward` interaction to open a damage window.
- **Rites:** can be soothed by a Choir rite instead of killed (different reward).
- **Drops:** `drowned_lung`, `pearl_reagent`, `mark_of_favour`.

## The Horror

- **Where:** wherever the endgame path opens it, or in the Veil.
- **Fantasy:** the thing the whole mod points at — a presence, barely embodied.
- **Phases:**
  1. *Attention* — reality distorts; sanity drains hard; no damage yet.
  2. *Approach* — the arena corrupts; adds spawn; the Horror advances.
  3. *Grasp* — direct attacks; requires wards and cleansing to survive.
  4. *Decision* — the endgame fork: **banish** (costly, resets much) or **summon** (Hollow Choir's goal).
- **Mechanics:** corruption field rises during the fight; sanity is the real health bar.
- **Endgame paths:**
  - **Banish:** `rite_of_cleansing` + `close_rift` chain; costs progression and corruption.
  - **Summon:** Hollow Choir rite; ends the run in the cult's favour.
- **Drops:** none conventional — the outcome *is* the reward.

## Design notes

- Each boss must have a non-combat solve.
- Arenas are documented structures (see `map.md`).
- Boss health is not the point; **sanity and corruption** are the real resources.

## Open questions

- Whether The Horror can be fought conventionally at all, or only solved.
- Whether there is a third "seal it away" ending.
