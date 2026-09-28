# 06 — Factions

Factions (cults) are the social engine. They teach rites, sell reagents, gate content,
and oppose one another. Content lives in [`09-cults.md`](09-cults.md); this is the model.

## Structure

- A cult is **data-driven** (`CultDefinition`): domain, ranks, services, demands, taboos,
  signature rite.
- **No subclass per cult** — one cultist NPC type, behaviour driven by the definition.
- Reputation is per player per cult (see `03c-reputation.md`).

## What a cult provides

| Surface | Details |
|---|---|
| Teaching | Rites unlocked as a rank service |
| Trading | Reagents, artefacts, tomes (reputation-gated) |
| Quests | Chains that raise standing and unlock content (see `10-quests.md`) |
| Territory | Cult sites, strongholds, patrols |
| Conflict | Opposed pairs and inter-cult events |

## Cult lifecycle in play

1. **Encounter** — discover a site/stronghold (`20-map.md`).
2. **Approach** — neutral dialogue; taboos stated.
3. **Prove** — quests/offerings raise standing.
4. **Rank up** — services unlock.
5. **Commit** — signature rites; opposed-cult cost.
6. **Endgame** — the Hollow Choir pulls toward summoning; the Order toward banishing.

## The three seed cults

| id | Role | Opposed |
|---|---|---|
| `drowned_choir` | Coastal power-for-breath | `unblinking_eye` |
| `unblinking_eye` | Order/resistance | `drowned_choir` |
| `hollow_choir` | Endgame summoners | *all, in spirit* |

## AI & worship

- Cultists follow schedules: worship at altars, patrol territory, trade, teach.
- Sacrifice and processions are events (`19-events.md`) that adjust standing.
- Inter-cult conflict triggers on reputation thresholds.

## Multiplayer

- Reputation is individual; cults can hold different views of different players.
- Cult events are world-level and affect all nearby players.

## Open questions

- Can a player found/lead a cult, or only join?
- Whether non-opposed cults can be held simultaneously at high rank.
