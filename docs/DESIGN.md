# Design — Eldritch Horror

A design bible for the mod. Numbers here are first-pass and will move; the **shape** of the
systems is the point.

## Pillars

1. **Power has a price.** Every shortcut to power (rituals, tomes, cult patronage) costs sanity,
   corruption, or both. There is no clean build.
2. **Knowledge is progression, not just gear.** Reading, translating, and performing rites
   advances you. The player earns access, not just items.
3. **The horror is a presence, not a spawn.** It acts through weather, ambient events, whispers,
   corrupted terrain, and cult activity before it is ever fightable.
4. **Factions remember.** Cults track you; helping one closes another door.

## The three axes

### Sanity
A regenerating meter (default 0–100). It is the *short-term* resource.

| Source | Effect (per second, baseline) |
|---|---|
| Daylight / well-lit area | +0.5 |
| Sleeping | +4 (once per night) |
| Near a cult altar | −0.3 |
| In darkness (light < 4) | −0.2, doubled underground |
| Rift within 32 blocks | −1.0, scaled by proximity |
| Witnessing an eldritch entity | −3 burst, then −0.5 while in line of sight |
| Reading a forbidden tome | −10 burst, +corruption |

**Thresholds:** below 40 → soft whispers + minor screen pulse; below 20 → periodic blindness/
hallucination spawns and the `Madness` effect; at 0 → `Marked`, and the horror can reach you
wherever you are until sanity recovers above 20.

### Corruption
A slow, **largely permanent** meter (0–100) with stages. It is the *long-term* resource.

| Stage | Range | Meaning |
|---|---|---|
| Dormant | 0–9 | clean; cults ignore you |
| Touched | 10–39 | you can see rifts; some fauna react |
| Marked | 40–69 | you can perform higher rites; villagers fear you; sleep restores less sanity |
| Claimed | 70–100 | locked out of the "resistance" questline; the horror knows your name |

Corruption **cannot be bought back**, only slowly bled by rare, expensive rites — and those rites
have their own cost. High corruption unlocks the darker cults' best content: the cost is
the gate.

### Cult reputation
`-100 … +100` per cult. Reputation gates services (ritual training, reagents, safe passage)
and unlocks rites. Cults have opposed pairs, so reputation with one can cost another.

First-pass cults:

- **The Drowned Choir** — coastal; offers water-breathing, sunken-ruin access; opposed to the
  Order.
- **The Order of the Unblinking Eye** — scholars; offers translations/identify; the "research"
  faction; opposed to the Choir.
- **The Hollow Choir (unnamed)** — the endgame cult that wants the horror summoned; offers the
  most power for the most corruption.

## Progression

A rough intended arc:

1. **Survive the whisper.** Early game: sanity drains in the dark; discover your first tome and
   the first altar. Learn that reading costs you.
2. **Find a faction.** Choose (or be chosen by) a cult. Reputation unlocks the first ritual tier.
3. **Work the rites.** Gather reagents, perform tier-1 rituals, open the first minor rift.
   Corruption begins to matter.
4. **Go deeper.** Tier-2/3 rituals require `Marked`+ corruption, which means committing. The two
   "resistance" cults and the summoning cult diverge here.
5. **The horror stirs.** World events scale with total corruption and rifts open. The endgame is
   either **banishing** the horror (high cost, resets a lot) or **summoning** it (the Hollow
   Choir's path).

## Rituals

A ritual is defined as data:

```jsonc
{
  "altar": "eldritch_horror:eldritch_stone",       // center block
  "pattern": ["...", "..."],                        // relative block pattern
  "offerings": [{ "item": "eldritch_horror:forbidden_reagent", "count": 4 }],
  "conditions": {
    "time": "MIDNIGHT",
    "moon_phase": "FULL",
    "min_corruption_stage": "TOUCHED",
    "dimension": "minecraft:overworld"
  },
  "on_success": [
    { "type": "spawn", "entity": "eldritch_horror:star_spawn", "count": 1 },
    { "type": "grant", "item": "eldritch_horror:forbidden_reagent", "count": 2 },
    { "type": "corruption", "amount": 8 }
  ],
  "on_failure": [{ "type": "sanity", "amount": -15 }]
}
```

Design rules:

- **Failed rituals hurt.** A wrong or refused rite costs sanity or corruption; the engine never
  silently no-ops.
- **Outcomes are registered types**, so content/other mods can add them.
- **Rites are teachable.** Knowing a rite requires learning it from a cult or tome; the recipe
  existing in data is not enough.

## Bestiary (sketch)

| Entity | Role |
|---|---|
| Cultist (per cult) | Faction NPC; trades, teaches, guards. |
| Worshipper / Zealot | Hostile cult combatant, escalates by cult rank. |
| Star-spawn | Summoned horror-adjacent mob; tied to corruption stage. |
| The Watcher | Passive, follows at distance, drains sanity; cannot be killed early. |
| The Horror | Endgame entity; appears only through the endgame path. |

## Open design questions

1. **Is sanity visible to the player always, or only below a threshold?** (Horror works better
   with partial information, but RPG players expect a meter.)
2. **Does corruption ever go down for a "clean" ending**, or is banishing the only redemption
   path?
3. **Multiplayer**: are sanity/corruption shared, averaged, or individual? Cults certainly need
   per-player reputation.
4. **Combat or avoidance**: can the Horror be fought conventionally, or must it be solved through
   ritual/quest mechanics?
