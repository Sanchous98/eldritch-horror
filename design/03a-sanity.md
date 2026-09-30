# 03a — Sanity

The short-term resource. A regenerating meter that drains near the eldritch and recovers
with rest and light. At the bottom, it lies to you.

## Model

- **Attribute:** `max_sanity` (default 100, range 0–100) — modifiable by skills/gear.
- **State:** current sanity, per player, server-authoritative, synced to the client.
- **Composable:** every drain/regen is a **`SanitySource`** (`tick(player, state) -> delta`),
  so content adds its own without touching core.

## Thresholds

| Sanity | State | Effects |
|---|---|---|
| 70–100 | **Composed** | — |
| 40–69 | **Uneasy** | Occasional whispers; subtle screen pulse |
| 20–39 | **Fraying** | Directional whispers; brief distortions |
| 1–19 | **Breaking** | `Madness` effect; periodic hallucinations; blindness flashes |
| 0 | **Marked** | The horror can reach you anywhere; hallucinations hostile |

Recovering above a threshold clears its effects.

## Drain & regen (first-pass rates, per second)

| Source | Effect |
|---|---|
| Daylight / well-lit area | `+0.5` |
| Sleeping | `+4` (once per night, reduced at high corruption) |
| Near a cult altar | `−0.3` |
| In darkness (light < 4) | `−0.2` (doubled underground) |
| Rift within 32 blocks | `−1.0`, scaled by proximity |
| Witnessing an eldritch entity | `−3` burst, then `−0.5` while in line of sight |
| Reading a forbidden tome | `−10` burst (see items) |

All rates are multiplied by `sanityDrainMultiplier` (config) and difficulty scaling.

## Effects when breaking

- **Hallucinations:** client-side fake mobs/sounds that vanish — no server entities.
- **`Madness`:** debuff applied at the threshold, removed on recovery.
- **`Marked` (at 0):** the horror's attention; ambient events escalate toward you.

## Rest & nightmares

- Sleeping restores sanity — *unless* corruption is high, when it can trigger nightmares
  (a dream vision teaching a rite, or a sanity hit).
- Nightmares are an occultist's (`lucid_dreams`) opportunity and a cultist's tax.

## Multiplayer

- Sanity is **individual** by default (see `12-multiplayer.md` for the open question).
- Nearby players' low sanity does not directly drain yours (no shared pool).

## Items that move sanity

See `16-items.md`: restoratives (tonics/reagents — **not food**; see below) and tomes (cost).
The `investigator_coat` reduces darkness drain.

**There is no hunger system.** Food is not a survival meter: it is removed as a mechanic
(vanilla hunger is disabled) and the **sanity meter takes its place** on the HUD. Crops,
animals and fish still exist only as **offerings/reagents** for rites, never as a food bar.
This matches the tabletop, which has no food track. See `24-sanity-and-corruption.md`.

## Tuning knobs

- `enableSanity`, `sanityDrainMultiplier`, per-source rates (config/data).
- Threshold values live in config so servers can soften the curve.

## Open questions

- Always-visible meter vs. only below a threshold (horror favours partial information).
