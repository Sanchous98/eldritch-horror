# 03c — Reputation

The social axis. Standing with each cult, per player, ranges `−100…+100`. It gates
services, ranks, dialogue and the strongest rites — and it remembers.

## Model

- **Per player, per cult:** an integer value.
- **Ranks** are thresholds on that value; services unlock by rank.
- **Opposed pairs** apply cross-consequences: a gain with one is a lesser loss with its opposite.

## Ranks (generic)

| Rank | Threshold | Unlocks |
|---|---|---|
| Outsider | `< 0` | Refused; hostile dialogue |
| Neutral | `0–19` | Basic dialogue |
| Initiate | `20–39` | Tier-1 teaching, small trades |
| Member | `40–59` | Tier-2 rites, reagents |
| Devoted | `60–79` | Tier-3 rites, artefacts |
| Inner Circle | `80+` | Signature rites, stronghold access |

Cult-specific rank names are in `09-cults.md`.

## Gain & loss

| Action | Effect |
|---|---|
| Complete a cult quest | `+10…+30` |
| Offer a demanded item | `+5` per offering |
| Perform the cult's signature rite | `+10` |
| Aid an opposed cult | `−10…−20` (opposed pair) |
| Break a taboo | `−25` and a hostile window |
| Kill a cult member | `−40` |

## Opposed pairs

- `drowned_choir` ↔ `unblinking_eye`: aiding one costs the other.
- `hollow_choir`: opposed to *both* in spirit, recruitable late.

## Decay

- Slow drift toward `0` for inactive players (config-gated, off by default on small servers).

## Why it matters

- Rites the player can learn are gated by rank as well as corruption.
- Dialogue branches on standing (see `10-quests.md`).
- The endgame Hollow path requires standing with a cult most players have antagonised.

## Tuning knobs

- Gains/losses per action, rank thresholds, decay rate, opposed-pair factor.

## Open questions

- Whether two non-opposed cults can both be held at high standing.
