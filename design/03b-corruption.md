# 03b — Corruption

The long-term resource. A durable exposure value that grows from forbidden knowledge,
ritual use, and proximity to rifts. It **barely recedes**, and it gates the dark paths.

## Model

- **Per player:** a value `0–100` with stages; persisted, server-authoritative.
- **Per chunk:** a corruption *field* driving block conversion and ambient spawns.
- **Sources are registered** (`CorruptionSource`) so content declares its taint.

## Stages

| Stage | Range | Effects |
|---|---|---|
| **Dormant** | 0–9 | Clean; cults ignore you |
| **Touched** | 10–39 | See rifts; some fauna react; tier-3 rites open |
| **Marked** | 40–69 | Villagers fear you; sleep restores less sanity; dark rites; summoning |
| **Claimed** | 70–100 | Locked out of the resistance path; endgame; the horror knows your name |

## Gain & loss

| Source | Effect |
|---|---|
| Reading a tome | `+5…+10` |
| Performing a rite | per-rite `+3…+15` |
| Touching tainted fauna / carrying cursed items | small, ongoing |
| Standing in a corrupted chunk | very small, ongoing |
| **Cleansing rite** | `−20` (rare, costly, capped) |
| **Banish ending** | resets a large part of the meter |

Corruption **cannot be bought back** in general — only rare rites lower it, and they
carry their own cost. High corruption is a *gate*, not a pure penalty.

## The per-chunk field

- Grows from rifts/altars, spreads to neighbours over time.
- Drives block conversion (a small block set), spore/fog particles, and ambient spawns.
- **Performance:** tick only near players; bound memory; persist with the chunk.

## Visibility

- The player's own corruption is shown as a stage, not necessarily a number.
- The world shows it: converted blocks, fog, tainted fauna, ambient events.

## Resistance

- Skills/gear can slow **gain**, never remove the meter.
- The Order sells cleansing; the Hollow Choir rewards the opposite.

## Interaction with sanity

- High corruption lowers sanity regen and worsens nightmares.
- Some effects (`Marked`) require both low sanity and high corruption.

## Tuning knobs

- `enableCorruptionSpread`, per-source gains, cleansing amount, stage thresholds.

## Open questions

- Whether corruption ever fully clears (for a "clean" ending).
- Per-dimension rule differences (see `12-multiplayer.md` / `20-map.md`).
