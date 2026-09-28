# Mobs

The bestiary. Each entry lists role, habitat, behaviour, and notable effects. Bosses are
in [bosses.md](bosses.md).

| id | Name | Role | Habitat | Hostility |
|---|---|---|---|---|
| `worshipper` | Worshipper | Cult NPC | Cult sites | Neutral (faction) |
| `cult_zealot` | Cult Zealot | Cult combatant | Cult sites, territory | Hostile to rivals |
| `lesser_swarm` | Lesser Swarm | Pack creature | Corrupted areas, rifts | Hostile |
| `star_spawn` | Star-Spawn | Summoned horror-adjacent | Rifts, the Veil | Hostile |
| `shoggoth_mass` | Shoggoth Mass | Amorphous horror | The Veil, deep rifts | Hostile |
| `watcher` | The Watcher | Ambient stalker | Near the corrupted | Passive (drains) |
| `tainted_fauna` | Tainted Fauna | Corrupted animal | Corrupted biomes | Varies |

## Worshipper

- **Role:** faction NPC that trades, teaches rites, and guards altars.
- **Behaviour:** driven by its `CultDefinition`, not a per-cult subclass.
- **Interaction:** reputation-gated dialogue and services (see `cults.md`).

## Cult Zealot

- **Role:** hostile cult combatant that escalates with rank.
- **Behaviour:** patrols cult territory; retaliates against taboo-breakers.
- **Scaling:** health/damage tied to the owning cult's rank tier.

## Lesser Swarm

- **Role:** weak, numerous, unsettling.
- **Behaviour:** spawns in packs of 3–5; seeks the player in the dark.
- **Effect:** small sanity drain in numbers.

## Star-Spawn

- **Role:** summoned horror-adjacent mob; a mid-game threat.
- **Behaviour:** hunts by sound; strong melee; resistant to mundane weapons.
- **Effect:** heavy sanity drain within line of sight.
- **Summon:** `summon_star_spawn` rite, or `summoning` skill.

## Shoggoth Mass

- **Role:** late-game amorphous horror.
- **Behaviour:** slow, absorbs damage types; splits when damaged below threshold.
- **Effect:** corruption aura; sanity drain.

## The Watcher

- **Role:** ambient stalker — a *presence*, not a fight.
- **Behaviour:** follows at distance, disappears when approached; cannot be killed early.
- **Effect:** drains sanity while in line of sight; `hides` when watched directly.

## Tainted Fauna

- **Role:** corrupted variant of ordinary animals.
- **Behaviour:** skittish or aggressive by species; spreads taint when killed nearby.
- **Effect:** touching it adds small corruption.

## Design notes

- Share AI goals via a library (see the *Entity AI goals library* story).
- Spawns scale with local corruption and player stage, not just difficulty.
- Every mob has a loot table and at least one vocalization.
