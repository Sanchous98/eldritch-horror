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

## Replacement contract

This mod is a **spirit-analogue of the board game Eldritch Horror, not a port.** We borrow the
*feel* of the mythos and the culture around it; our mechanics, sites and numbers are our own. We
do **not** copy FFG's stat blocks, card text, or art, and we do not reproduce the board game's
rules. The names of the Ancient Ones below are public-domain literary names (Lovecraft et al.),
which is why they are safe to use; everything attached to them here is invented for this mod.

The contract has two halves, both already true in code (`world/MobSuppressor`):

1. **There are no vanilla bosses.** Wither, Ender Dragon, Elder Guardian, Warden, Ravager and
   every other boss-like vanilla entity are removed. Each role is replaced by a named
   **Ancient One encounter** (see `28-ancient-ones.md`) — a *presence* with a sanity-axis hook,
   usually solvable without combat.
2. **There are no vanilla mobs.** Every vanilla mob is suppressed and every ecological role it
   filled is taken by a monster from our own roster (the replacement table below). The world is
   populated by *our* creatures, so it stays coherent.

**Kept as-is: villagers.** `MobSuppressor` allow-lists `minecraft:villager` by default
(`ModConfig.SUPPRESSED_MOB_ALLOWLIST`). Villagers are the **mundane humans** — the ordinary
world, settlements and trade (`20-map.md`, `21-settlements.md`). They are not a monster and not
a boss. They *can* be preyed on: coastal `drowned_thrall` raids and cult abductions threaten
settlements, and rising corruption converts them; that risk is what makes a city's sanity-haven
aura worth protecting.

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

## Monster roster — vanilla replacement table

Every vanilla mob family is **suppressed** (`world/MobSuppressor`) and the role it played is
re-taken by one of ours, so the world is inhabited end to end without a single vanilla mob.
Ids that already exist in this document (`deer`, `watcher`, `cult_zealot`, `tainted_fauna`, the
minions and the named ones) are reused; the rest are proposed. "Serves" is the system the entity
exists for (`25` principle 4): sanity source / corruption vector / rite outcome / faction /
boss gate.

### Hostile role

| Vanilla role | Our replacement | Family | One-line role | Serves |
|---|---|---|---|---|
| zombie / drowned / husk | `risen_husk` | lesser | slow shambling dead; the low bar of the wrong | sanity |
| skeleton / stray / bogged | `bone_choir` | lesser | rattling archer that hums a hymn; its shots announce it | sanity |
| creeper | `blight_pod` | tainted | creeping spore-thing that bursts into taint, not fire | corruption vector |
| spider / cave spider | `weaver_spawn` | lesser | skittering nest-guard; webs a room shut | sanity / site |
| silverfish / endermite | `rift_mite` | lesser | tiny rift vermin; swarms when a chunk is tainted | corruption vector |
| enderman | `watcher` | lesser horror | a presence, not a fight; follows at distance, hides when seen | sanity (star) |
| witch | `plague_crone` | cultist | hag-alchemist of the Hollow Choir; trades curses for taint | faction / corruption |
| pillager / vindicator / evoker | `cult_raider` / `cult_zealot` / `rite_binder` | cultist | cult war-band, rank-scaled; the binder raises the dead | faction / rite outcome |
| vex | `choir_spite` | lesser | a spiteful mote of the Choir; passes walls, drains focus | sanity |
| slime / magma cube | `shambler_ooze` | lesser | splits when struck; drops tainted residue | corruption vector |
| phantom | `night_hag` | lesser | swoops at low sanity, not low sleep; feeds on the frayed | sanity |
| guardian | `drowned_thrall` | lesser | tide-guard of the Choir; prey on coastal villagers | sanity / faction |
| ravager | `dunwich_horror` | named (Ancient One) | the lumbering thing eating a settlement from uphill | boss gate (`28`) |
| wither skeleton | `ashen_revenant` | lesser | burning dead of the ashen waste; leaves scorch-taint | corruption vector |
| zombified piglin / hoglin / piglin | `veil_swine` | lesser | a parody of a herd from behind the veil; aggressive, territorial | sanity |
| ghast | `choir_leviathan` spawn / `wail_husk` | lesser | floating mourner whose wail is a line-of-sight drain | boss gate / sanity |
| blaze | `ashen_wisp` | lesser | fire that watches (Cthugha's kin); light no longer comforts | corruption vector |
| warden | `ithaqual_shade` | named (Ancient One) | wind-that-walks near the cold edge and deep places | boss gate (`28`) |
| elder guardian | `cthulhu` | named (Ancient One) | dream-presence of the flooded hall | boss gate (`28`) |
| ender dragon | `yog_sothoth` | named (Ancient One) | the gate that is the boss | boss gate (`28`) |
| wither | `azathoth` | named (Ancient One) | no fight; the will to act is unmade | boss gate (`28`) |

The remaining roster fills no single vanilla slot but populates rifts and sites:
`lesser_swarm`, `star_spawn`, `shoggoth_mass`, `tainted_fauna`, plus site-owned things. These
stay as defined above; the table only guarantees nothing vanilla is left uncovered.

### Passive role (mundane world)

| Vanilla role | Our replacement | Family | One-line role | Serves |
|---|---|---|---|---|
| cow | `deer` | mundane | the ordinary world, so the wrong things contrast | none (baseline) |
| sheep | `wool_hare` | mundane | skittish herd animal; flees corruption | none |
| pig | `mire_sow` | mundane | settlement livestock; can turn `tainted_fauna` | corruption vector |
| chicken / parrot | `ash_fowl` | mundane | common bird; its silence before a presence is a tell | sanity (warning) |
| rabbit | `burrowling` | mundane | burrows and scatters near taint | none |
| horse / donkey / mule | `pack_beast` | mundane | travel animal for settlements (non-combat) | travel/settlement |
| fox | `grey_fox` | mundane | shy; seen only in clean biomes | sanity (contrast) |
| wolf | `hill_hound` | mundane | settlement guard-dog; can be tainted into a foe | sanity |
| cat / ocelot | `hearth_cat` | mundane | settlement cat; unsettled by the wrong, not the dark | sanity (warning) |
| llama / trader llama | `wool_beast` | mundane | caravan animal near settlements | travel/settlement |
| panda | `bog_bear` | mundane | rare marsh grazer; gentle until tainted | none |
| turtle | `tide_grazer` | mundane | coastal grazer near Choir sites | faction (Choir) |
| bee | `spore_bee` | tainted | pollinates taint; stings add corruption | corruption vector |
| villager | *(kept)* | mundane | the mundane humans; see the Replacement contract | faction / trade |
| iron/snow golem | `stone_sentinel` | order | Order-built guard of vaults; wards lesser spawns | faction (Order) |
| wandering trader | *(kept as villager variant)* | mundane | roaming mundane trader | faction / trade |

### Ambient role

| Vanilla role | Our replacement | Family | One-line role | Serves |
|---|---|---|---|---|
| bat | `cave_drifter` | ambient | harmless flier; its absence is the warning | sanity (warning) |
| squid | `pale_drifter` | ambient | dead-water flier near drowned sites | ambience |
| glow squid | `lantern_jelly` | ambient | faint light in the deep; a false comfort | ambience / sanity |
| allay / tadpole / axolotl | `marsh_mote` | ambient | drift-life of the drowned marsh | ambience |
| (fish shoals) | `drowned_minnow` | ambient | the only thing left in dead water | ambience |
| snow golem (ambient) | `frost_wisp` | ambient | cold motes near the polar edge | ambience |

**Coverage:** the tables above cover **all 60+ suppressible vanilla entity ids** (every
LivingEntity that is not the player), grouped where roles are identical — hostile, passive and
ambient families are each mapped, and **villager is the one deliberate exception** (kept, not
replaced). Mod entities are always allowed by `MobSuppressor`.

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
- Which Ancient Ones (`28-ancient-ones.md`) are canonical for the first release, and whether
  "none"-replaces entries are content for launch or backlog.

## Implementation notes (later)

- **Shared AI goals library** so entity behaviour composes (the contract's "Entity AI goals
  library" story); no per-mob subclass for common behaviour.
- Every entity: a loot table, at least one sound event, and a spawn declaration.
- Spawn declarations are **data** (biome lists, site overrides, event/rite outcome `spawn`),
  not hardcoded; the rite outcome `spawn` type already exists (`05-ritual-engine.md`).
- **Client half:** hallucination rendering and non-combat ambience only; all real entities
  and spawns are server-side (`MULTIPLAYER.md`).

## Build order

Implement **five mobs first**, each proving one slice of the framework. Every one needs: a base
class + shared AI goals, the listed traits, a **spawn declaration**, a loot table, at least one
sound event, and a placeholder model (art comes later).

1. **`risen_husk`** — proves the whole pipeline with the simplest behaviour (walk → hit → die).
   Base `Monster`; traits: `SanitySource` on line of sight; spawn: `ashen_waste`/
   `blighted_woods` at taint > 0; a low sanity rate; placeholder zombie-like model.
2. **`tainted_fauna`** — proves the **corruption vector** + transformation. Base animal; trait:
   touch applies `CorruptionSource`; spawn: converts from mundane fauna when chunk taint rises;
   sound: a distorted animal call.
3. **`watcher`** — proves the **presence** shape (sanctity of "not a fight"): no HP bar early,
   hides when directly watched, spawn: near the corrupted; trait: pure `SanitySource`, no combat
   loot; sound-only, near-absent model.
4. **`lesser_swarm`** — proves **pack spawning + event/rite outcome** spawns. Base `Monster`;
   trait: group sanity multiplier; spawn via `call_the_lesser` outcome and rift proximity;
   the first mobile that stresses the AI goals library.
5. **`drowned_thrall`** (or `cthulhu`, if the `drowned_temple` arena lands in the same epic) —
   proves a **site-owned + faction** mob and the coastal-villager threat; spawn declaration as a
   `drowned_temple` override; a `ward` interaction hook that the Cthulhu encounter later reuses.

Then, in order: `cult_raider`/`rite_binder` (faction combat + rite outcome), `star_spawn` /
`shoggoth_mass` (minions, combat + aura), and the first Ancient One encounter (`28`:
**Cthulhu → Dunwich Horror → Shub-Niggurath**). The full roster arrives as content once the
five above are green.
