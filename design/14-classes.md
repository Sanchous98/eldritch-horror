# 14 — Investigators (named characters)

Replaces the three archetypes. As in the board game, you choose a **named investigator**, not a
class. Each has an **occupation** (flavour), a **role** (a one-word strategy label — Research,
Combat, Gate Closer, Magic, Support, Expedition, All-Rounder), a **starting kit**, a starting
**rite**, and **two abilities**: one **passive** (always on) and one **active** (a signature item,
right-click, with a cooldown).

The names and abilities are **ours** (a spirit-analogue, not a port). We copy no FFG investigator,
stat block, card or art — same rule as the Ancient Ones (`28-ancient-ones.md`).

## The roster (12)

| id | Name | Occupation | Role | Passive | Active |
|---|---|---|---|---|---|
| `eleanor_vance` | Eleanor Vance | Parapsychologist | Research | `max_sanity +15%` | **Read the Signs** — reveal nearby rifts and codex-worthy landmarks |
| `jack_corrigan` | Jack Corrigan | Private Eye | Research | `corruption_gain −20%` | **Stake Out** — mark the nearest rift; brief `Resistance` |
| `tom_mallory` | Tom Mallory | Ex-Soldier | Combat | `sanity_drain −25%` | **Hold the Line** — `Resistance` + `Regeneration` for 12s |
| `cormac_blackwood` | Cormac Blackwood | Smuggler | Combat | `corruption_gain +25%`, `sanity_drain −15%` | **Dirty Trick** — brief `Invisibility` + `Speed` to break away |
| `sister_agatha` | Sister Agatha | Nun | Gate Closer | `corruption_gain −25%` | **Benediction** — cleanse the local taint patch; small sanity |
| `marion_delacroix` | Marion Delacroix | Medium | Gate Closer | `max_sanity +10%` | **Commune** — sense the nearest Ancient One (direction + distance) |
| `vera_nightingale` | Vera Nightingale | Occultist | Magic | `corruption_gain +25%`, one extra starting rite | **Blood Offering** — trade a burst of sanity for a burst of corruption |
| `nikolai_volkov` | Nikolai Volkov | Scholar | Magic | `max_sanity −10%`, `sanity_drain +15%` | **Forbidden Insight** — learn a random unknown rite |
| `dr_amos_hartley` | Dr. Amos Hartley | Alienist | Support | city sanity recovery `+50%` | **Sedate** — clear `Madness`/`Marked`, restore sanity |
| `evelyn_ashcombe` | Evelyn Ashcombe | Heiress | Support | starts with extra currency | **Buy Time** — `Resistance` for 20s |
| `aldous_pemberton` | Aldous Pemberton | Antiquarian | Expedition | better site/ruin loot | **Survey** — reveal the nearest site/city coordinates || `hazel_quinn` | Hazel Quinn | Journalist | All-Rounder | small spread of the above | **Exposé** — reduce own corruption a little; brief `Speed` |

## Ability mechanics (how they map to our systems)

- **Passives** are read through `Progression` (the same seam the old classes used): multipliers for
  `max_sanity`, `sanity_drain`, `corruption_gain`, city recovery, loot, etc. Bonuses whose systems
  exist are live (max sanity, sanity drain, corruption gain, city recovery, Aldous's better loot on
  mob drops and site chests); the rest are recorded for when their systems land.
- **Actives** are the investigator's **signature item** (a per-investigator item given in the
  starting kit, e.g. `seers_lens` for Eleanor, `rosary` for Agatha; the item is bound to its owner
  and dispatching is by item identity). Right-click triggers it with a per-player cooldown.
  Server-authoritative, bounded, built from effects + the public
  `SanityAPI`/`CorruptionAPI`/`TaintAPI`/`RiteKnowledge` facades. No GUI.
- **Choice is permanent** except the debug reset, exactly as before: a preview-then-confirm
  interaction, now on a pedestal per investigator.

## Starting kits — earned, not issued

Each investigator has a small thematic kit plus the signature item; the Magic investigators also get
a tome/reagents. Exact item lists live in the registry data table (`Investigators`, one place), but
**a fresh player is issued nothing at the pedestal**. The kit is earned through two data-driven field
tasks (journal, `J`) that are unique to the investigator:

1. **The gear** — a first task using the investigator's trade and ordinary world materials (paper,
   iron, string, bone, wool, wheat, feathers, books, gold, or crafting chalk). It pays the kit minus
   the signature item.
2. **The calling** — a second task that uses the gear: a hunt (the restless dead, spiders), a codex
   discovery, or — for an occultist — the first rite. It pays the signature item.

The tasks are definitions in `data/eldritch_horror/quest/` with `investigator`, `trigger`/`param`,
`reward_kit` and `reward_signature`; the effects and thresholds are content, not code. The journal
shows only the chosen investigator's two tasks. A stripped datapack that defines none of them falls
back to the old hand-over, so nobody can start empty-handed. The admin reset
(`/eh investigator set`) still grants the kit directly for testing.

## How the roster is presented

- 12 pedestals in the Threshold (an inner ring, or two arcs), each with a sign: the investigator's
  name and role. Preview-then-confirm on right-click.
- The prologue flow is unchanged: choose an investigator at a pedestal, then walk through a city
  arch. The chosen investigator is the permanent `INVESTIGATOR` attachment (was `PLAYER_CLASS`).

## Notes

- Role is a **label**, not a separate choice — a hint for the player, as in the board game.
- Cross-investigator synergies (`14-classes.md`'s old "synergies") are dropped for now; they were
  speculative and had no systems.
