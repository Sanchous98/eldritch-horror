# 28 — Ancient Ones

Status: **decided (design)**; implementation deferred. This is the **boss layer** of the
bestiary: the named presences that replace the removed vanilla bosses (see the *Replacement
contract* in `25-bestiary-and-entities.md`). It is a spirit-analogue of the board game
Eldritch Horror, **not a port** — the names are public-domain literary names, the mechanics,
sites and numbers are ours. We copy no FFG stat block, card or art.

## The rule

An Ancient One is **not a health bar.** It is a *presence* with a **sanity-axis hook**: the
real loss is attention, memory, will or perception (`11-endgame.md`, `24-sanity-and-corruption.md`).
Where a fight exists, sanity is the real health bar and there is always a **non-combat solve**
(a rite, a ward, a negotiation). Boss arenas are rooms inside existing locations, never separate
structures (`25-bestiary-and-entities.md`).

## The roster

"Replaces" is the vanilla boss whose role it takes; **none** means it is a new presence with no
vanilla analogue. "Site" names an existing second-echelon location (`20-map.md`) or a new minor
site. ★ marks the **first three to implement** (smallest scope — see below).

| Ancient One | Replaces | Site | The horror is a presence — hook (sanity-axis) | First |
|---|---|---|---|---|
| **Cthulhu** | Elder Guardian | `drowned_temple` | Dreams leak from the flooded hall; sleeping near it is worse than being awake | ★ |
| **The Dunwich Horror** | Ravager | `blighted_woods` village edge | A giant you hear and never see; it is always uphill from a settlement it is eating | ★ |
| **Shub-Niggurath** | none | `blighted_woods` (biome) | The woods breathe; standing still costs sanity, running costs corruption | ★ |
| **Azathoth** | Wither | `rift_scar` / the Veil | No fight — the music at the centre unmakes the will to act | |
| **Yog-Sothoth** | Ender Dragon | `rift_gate` / `observatory_plateau` | The gate *is* the boss; every step toward it, it takes one toward you | |
| **Ithaqua** | Warden | polar edge / Morok (`23-boundary-and-travel.md`) | Wind that walks; each gust drains and drags you toward the cold edge | |
| **Nyarlathotep** | none | `cult_stronghold` | Wears a face you trust; the drain is betrayal, not damage | |
| **Hastur** | none | new minor site `yellow_court` (`observatory_plateau`) | A name you must not say; hearing or reading it makes you want to say it | |
| **Yig** | none | `ashen_waste` | Small bites; the more you kill, the more are drawn to the death | |
| **Nephren-Ka** | none | new minor site `black_pyramid` | A king who remembers you; his gaze makes your own cult doubt you | |
| **Abhoth** | none | new minor site `spawning_pool` (`blighted_woods`) | A pool that spawns filth; kill one, two replace it — so killing is the trap | |
| **Atlach-Nacha** | none | `rift_scar` | Weaves the rift wider; every rift you close makes it angrier | |
| **Chaugnar Faugn** | none | new minor site `temple_of_the_feaster` | A hunger you can hear; it feeds on the living and on your courage | |
| **Cthugha** | none | `ashen_waste` / new `fire_temple` | Flame that watches; heat and light no longer restore sanity here | |
| **Glaaki** | none | `drowned_marsh` | Green servitors drag you under; the dream is always the same lake | |
| **Hydra** | none | `drowned_marsh` | Cut one head and the presence grows; the answer is never the sword | |
| **Nyogtha** | none | deep caves / new `underworld_stair` | It is under the floor; you cannot see it, only hear it coming | |
| **Rhan-Tegoth** | none | polar / new `ice_idol` | An idol that is not an idol; worship sustains it — and you are worshipping | |
| **Tulzscha** | none | the Veil, at the centre | A green flame at the centre; looking into it costs what you remember | |
| **Zstylzhemghi** | none | the Veil (eroding court) | A name forgotten on purpose; the world erodes around it | |

Two of the existing named entries in `25` stay as they are and are not duplicated here:
`choir_leviathan` (drowned-temple, soothable by a Choir rite) and `the_horror` (the endgame
stage where the banish/summon fork plays out, `11-endgame.md`). An Ancient One can be a
**phase** of `the_horror` rather than a separate fight.

## Why these three are first

Chosen for the **smallest vertical slice**, one of each encounter shape:

1. **Cthulhu** — reuses the `drowned_temple` arena already planned for the Leviathan and needs a
   single sanity source + a `ward` interaction. Proves the *site boss* shape.
2. **The Dunwich Horror** — a lumbering mobile presence over an **existing settlement**, no new
   structure; proves a boss that moves and is heard before it is seen, with a non-combat answer
   (draw it away / a cleansing rite).
3. **Shub-Niggurath** — a pure **biome presence**: no arena, no rig-heavy model; only a spawn
   declaration, a taint aura and a sanity source. Proves the *ambient boss* shape and the
   corruption-vector link cheapest.

## Implementation notes (later)

- One shared **`AncientOneDefinition`** (data): id, display name, replaced role, site/spawn
  attachment, sanity-source id, phases, non-combat solve(s), gate quest. No per-boss Java class;
  behaviour composes from the shared AI-goals library (`25-bestiary-and-entities.md`).
- The replaced vanilla bosses are already suppressed in code (`world/MobSuppressor`); this doc
  only supplies what takes their place.
- Each entry ships: a sound event (a presence is *heard*), a placeholder model, a spawn/site
  declaration, and a rite or ward that solves it without combat.
- Arena rooms are carved by the owning location's `build` (`docs/STRUCTURES-CONTRACT.md`), never
  by the boss.

## Open questions

- Whether Ancient Ones other than `the_horror` can appear in the endgame fork at all.
- Whether an Ancient One is world-state (shared) or per-player instanced (`12-multiplayer.md`).
- How many can be "awake" at once before the ladder collapses into noise.
- Whether two Ancient Ones can occupy one site (e.g. Cthulhu and the Leviathan).
