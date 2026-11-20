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

### Ritual cells (pinned 2026-11)

Worship is not scenery: a cell that is left alone advances the cult's goal without a player
choosing that path. This is the Hollow Choir's agency — the endgame can come to you.

- **Where.** A cell gathers at a real altar (the `ritual_altar_site`, a cult stronghold, or a
  player-built `altar_core` ring) inside loaded chunks, with at least **3 cultists** of one cult and
  a player within ~64 blocks. Nothing is force-loaded and no ritual starts out of sight.
- **The ritual.** `GATHER` (the cultists walk to the ring, ~20 s) → `CHANT` (~30 s, particles and
  the rite hum at the core) → completion. A `RiteBinder` leads when one is present; otherwise the
  senior cultist present does.
- **Interruption is the counter-play.** Killing the leader, breaking the altar core, or standing
  inside the ring stops it. An interrupted ritual pays the interrupter (Order standing and a small
  relic), resets the cell's cooldown and makes the leader's death matter.
- **Escalation.** A completed ritual summons the cult's lesser things (star-spawn for the Hollow
  Choir) and taints the ground. Each completion also raises the world's **dread** counter; at the
  configured threshold the Hollow Choir performs the awakening itself (`awaken_the_horror` path,
  `11-endgame.md`), with a global warning — unless the endgame has already been decided.
- **Bounds.** Loaded chunks only, one ritual per site, a cooldown per site, a capped number of
  active cells, and a config master switch (`enableNpcCultRituals`); the awakening itself can be
  disabled separately (`npcCultRitualsCanAwaken`).

## Multiplayer

- Reputation is individual; cults can hold different views of different players.
- Cult events are world-level and affect all nearby players.

## Open questions

- Can a player found/lead a cult, or only join?
- Whether non-opposed cults can be held simultaneously at high rank.
