# 11 — Endgame

The endgame is a **decision**, not a boss health bar. When corruption reaches `Claimed`
and the rifts are open, two paths diverge — and they are not symmetric.

## Prerequisites

- Total corruption high (stage `Claimed`).
- Several rifts open (`open_rift` performed, or found in `ashen_waste`).
- The relevant quest chain advanced (`10-quests.md`).
- Rites learned: `close_rift` / `rite_of_cleansing` (banish) or `open_rift` + Hollow rank (summon).

## Path A — Banish

**Fantasy:** pay everything to put it back. The resistance ending.

- **Steps:** a chained rite (`close_rift` → `rite_of_cleansing`) at a `rift_gate`, requiring
  rare reagents and the Order's aid.
- **Cost:** a large part of your **corruption resets**, along with a portion of progression
  and reputation; rifts close; corrupted biomes recede.
- **Reward:** a cleansed world state, the Order's highest standing, a unique token.
- **Tone:** costly, sober, arguably a loss.

## Path B — Summon

**Fantasy:** finish what the Hollow Choir started. The horror ending.

- **Steps:** the Hollow Choir's signature rite at a Choir Altar, requiring `summoning`,
  blood offerings, and standing with `hollow_choir`.
- **Cost:** corruption `Claimed` becomes permanent; sanity floors; the world tips.
- **Reward:** the strongest powers, the Choir's inner circle, a changed world.
- **Tone:** triumphant and terrible.

## The fork

The choice is legible before it is made (see `04-pillars.md`: no unwarned dead ends).
Both paths are reachable; neither is strictly "better".

## The Horror encounter

The Horror (`18-bosses.md`) is the stage on which the fork plays out:

- It is felt (events, distortion) long before combat.
- Its "phases" are really *escalations*; sanity is the real health bar.
- The final phase **is** the decision — banish or summon.

## World state after

| | Banish | Summon |
|---|---|---|
| Rifts | Close | Multiply |
| Corrupted biomes | Recede | Spread |
| Cult standing | Order at max | Hollow at max |
| Player corruption | Largely reset | Permanent `Claimed` |
| Tone | Loss/lucidity | Power/doom |

## Implemented (completion pass)

- **The world's decision is saved**: `endgame/EndgameState` is a per-overworld `SavedData`;
  `/eh endgame` reads it, `/eh endgame set <none|banished|summoned>` repairs it.
- **The Horror** (`entity/TheHorror`, `18-bosses.md`) is an invulnerable presence with four stages
  (attention → approach → grasp → decision), a sanity aura that deepens with the stage, taint and
  capped answers; it is awakened by its own rite at a rift. The escalation clock lives in `tick()`
  because attention sets `NoAI`, which stops the AI step.
- **Three rites** (`rite/Rites.java`): `awaken_the_horror` (needs a rift), `banish_the_horror`
  (Order-taught) and `summon_the_horror` (Hollow-taught, Devoted).
- **Banish**: closes the loaded rifts, recedes the taint, lifts any permanent floor, resets a large
  part of corruption, costs a skill point and a quarter of every cult's standing, grants
  `banishment_focus`.
- **Summon**: pins the corruption floor at the Claimed band — a floor on the meter itself, so every
  cleanse path (rite, service, event) is refused by the system — raises Hollow standing, tears open
  rifts, grants `hollow_crown`.
- **Journal**: `awaken_the_horror`, `the_decision` and `the_seal` (data files under
  `data/eldritch_horror/quest`).
- **The third outcome — sealed** (the open question below, now implemented): `endgame/RiftLedger`
  is a world `SavedData` that records every rift anchor placed (the `open_rift` rite, the summon
  path) and forgets one when it goes: the close rite, a banish, or the anchor block itself breaking —
  `RiftAnchorBlock` tells the ledger, so an altar ring built over a rift cannot leave a ghost behind —
  and `prune` drops entries whose loaded chunk says the anchor is gone. When The Horror reaches the
  decision and the ledger is empty, `Endgame.trySeal` records `Outcome.SEALED`, the presence recedes,
  the `endgame.seal` sound plays and the journal's `the_seal` completes. Nothing was banished, so no
  cost is paid; the fork rites then refuse ("the decision has already been made"). A world that never
  had a rift is never "sealed" (`wasEverOpen`).

## Open questions

- Whether the endgame is per-player or per-world in multiplayer (it is per-world today).
