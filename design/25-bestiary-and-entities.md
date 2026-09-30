# 25 — Bestiary and Entities

Status: **decided (design)**; implementation deferred. Consolidates `17-mobs.md` and
`18-bosses.md` into a roster **framework**; individual entities are content.

## Design principles

1. **Silhouette over stats.** You should know a thing by its outline, its gait and its
   *sound* before you read a health bar. Every entity has one readable shape.
2. **Dread over damage.** The scariest entities do little damage and drain **sanity**; the
   dangerous ones are the *quiet* ones. Damage is the fallback, not the hook
   (`04-pillars.md`: the horror is a presence).
3. **Sound is a weapon.** Every mob ships at least one vocalisation; some (the Watcher) exist
   almost entirely as sound and absence.
4. **No content for content's sake.** Every entity must serve a system: a sanity source, a
   corruption vector, a ritual outcome, a boss gate, or a faction.
5. **Spawns attach to the world.** Entities appear because of *where you are* (biome, site,
   corruption) or *what happened* (event, rite) — never uniformly everywhere.
6. **Non-combat solves exist.** At least one answer to every major threat is a rite, ward or
   negotiation (`18-bosses.md`).

## Entity families

Ordered by the progression the player meets them in:

| Family | What it is | Examples | Threat |
|---|---|---|---|
| **Mundane wildlife** | baseline, uncorrupted animals | deer, wolves, fish | none (not food; no hunger system) |
| **Tainted fauna** | corrupted versions of the mundane | `tainted_fauna`, corrupted birds | low, corruption vector |
| **Cultists** | human faction agents | `worshipper`, `cult_zealot` | social / low combat |
| **Lesser horrors** | the first truly wrong things | `lesser_swarm`, `watcher` | sanity / ambush |
| **Minions** | summons and elite horrors | `star_spawn`, `shoggoth_mass` | combat + aura |
| **The named ones** | bosses, gated by progression | `choir_leviathan`, `the_horror` | endgame |

## Example roster (≈10 entries)

Each entry is a one-line role. Ids match `17-mobs.md` / `18-bosses.md` where they exist.

| id | Name | Family | One-line role |
|---|---|---|---|
| `deer` | Deer | mundane | the ordinary world, so the wrong things can contrast against it |
| `tainted_fauna` | Tainted Fauna | tainted | a familiar animal made wrong; touching it taints you |
| `worshipper` | Worshipper | cultist | faction NPC that trades, teaches rites, guards altars |
| `cult_zealot` | Cult Zealot | cultist | cult combatant that scales with the owning cult's rank |
| `lesser_swarm` | Lesser Swarm | lesser horror | weak, numerous, seeks you in the dark; drains sanity in numbers |
| `watcher` | The Watcher | lesser horror | a presence, not a fight — follows at distance, cannot be killed early |
| `star_spawn` | Star-Spawn | minion | hunts by sound; resists mundane weapons; heavy line-of-sight drain |
| `shoggoth_mass` | Shoggoth Mass | minion | slow amorphous elite; absorbs damage; carries a corruption aura |
| `choir_leviathan` | Choir Leviathan | named | drowned-temple boss; soothable by a Choir rite instead of killed |
| `the_horror` | The Horror | named | the endgame stage; sanity is the real health bar, not HP |

## How spawns attach to the world

Spawns are **context-driven**, not a global spawn table. Three attachment modes:

- **Biome-based** — the corruption-field biomes (`blighted_woods`, `drowned_marsh`,
  `ashen_waste`, `observatory_plateau`; see `20-map.md`) each carry a mob list. The list
  becomes eligible as the field converts the chunk; vanilla biomes spawn vanilla things.
- **Site-based** — the second-echelon locations own their populations (see
  `docs/STRUCTURES-CONTRACT.md`): `cult_stronghold` → cultists; `drowned_temple` → tide
  things; `rift_scar` → swarm/star-spawn; `order_vault` → Order guards; `ritual_altar_site`
  → what the rite calls. A site declares inhabitants via spawn overrides.
- **Event-based** — `19-events.md` and rite outcomes spawn entities as *consequences*
  (`call_the_lesser`, `summon_star_spawn`, `rift_bloom`, `veil_thin`). These are the only
  spawns that are not tied to a place.

**Scaling:** spawn weight scales with the local corruption field and the session phase
(`docs/MULTIPLAYER.md`), not with raw difficulty. Throttle to near players; cap concurrent
spawns per chunk.

## Boss arenas are rooms inside locations

There is **no separate boss-arena structure**. An arena is a **room inside an existing
location** (`docs/STRUCTURES-CONTRACT.md`): the Leviathan's flooded hall is a room in the
`drowned_temple`; the Horror's stage is wherever the endgame path opens it (`11-endgame.md`).

- The location's `Location.build` carves the arena as part of the site; combat is out of
  scope until the bestiary epic (contract's "Out of scope").
- The arena should expose one **environmental interaction** (a ward to strike, a flood to
  manage, a rift to close) so the fight has a non-stat solve.
- Boss phases read the world (corruption field, sanity), not only HP; the Horror's final
  phase **is** the banish/summon decision.

## Ties to sanity and corruption (`24-sanity-and-corruption.md`)

- **Sanity:** witnessing/line-of-sight is the primary drain; the Watcher and swarm exist to
  make being watched cost something. Entities are sanity *sources* (`SanitySource`).
- **Corruption:** tainted fauna and shoggoth auras are corruption *vectors*; killing near
  innocents can spread taint. High corruption unlocks/attracts the worse roster.
- **Feedback both ways:** low sanity makes you *see* things that are not there (hallucination
  mobs are client-side, no server entity) — the bestiary has a **real** and a **perceived**
  layer, and the player can rarely tell which is which.

## Open questions

- Whether hallucination entities ever become real (a perceived spawn that persists).
- Per-player instanced bosses in multiplayer, or one shared world boss.
- How many entities one chunk may hold before spawn weights scale down.
- Whether the Watcher can ever be killed, or only avoided/warded away.

## Implementation notes (later)

- **Shared AI goals library** so entity behaviour composes (the contract's "Entity AI goals
  library" story); no per-mob subclass for common behaviour.
- Every entity: a loot table, at least one sound event, and a spawn declaration.
- Spawn declarations are **data** (biome lists, site overrides, event/rite outcome `spawn`),
  not hardcoded; the rite outcome `spawn` type already exists (`05-ritual-engine.md`).
- **Client half:** hallucination rendering and non-combat ambience only; all real entities
  and spawns are server-side (`MULTIPLAYER.md`).
