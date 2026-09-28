# Events

Ambient and world events that make the horror a *presence*. Events scale with the total
corruption of the area/players and the number of open rifts.

| id | Name | Trigger | Effect | Duration |
|---|---|---|---|---|
| `whisper` | Whispers | Low sanity, anywhere | Directional whisper sounds; brief `Madness` warning | 10–30s |
| `darkness_pulse` | The Dark Breathes | Night, nearby rift | Light level drops; sanity drain spikes | 20s |
| `rift_bloom` | Rift Bloom | Open rift ages | Corruption spreads faster; VFX intensify | until closed |
| `cult_procession` | Cult Procession | Cult stronghold, scheduled | Cultists march and chant; reputation window | 1 in-game day cycle |
| `blood_moon_rite` | Rite of the Red Moon | Full moon, high corruption | Cults perform a large rite; rifts may open | one night |
| `star_fall` | Star-Fall | Random, `Marked`+ | Meteor of `star_reagent`; nearby sanity drain | single event |
| `veil_thin` | The Veil Thins | Storm, many rifts | Corrupted spawns surge; ambient sound rises | 1–2 min |
| `hallucination_wave` | Hallucination Wave | Sanity < 20 | Fake mobs/sounds for nearby players | 30s |
| `hollow_call` | The Hollow Call | `Claimed` corruption | The Horror "calls"; forced night event | one night |
| `cleansing_dawn` | Cleansing Dawn | After `close_rift` | Corruption recedes; sanity recovers | one dawn |

Several events also **change the world map** (add/remove/recolour markers) — settlements
falling, rifts opening, cults taking over. Those are listed in
[`22-map-and-knowledge.md`](22-map-and-knowledge.md#map-change-events).

## Design notes

- Events are **data-driven**: trigger, weight, effect, duration, cooldown.
- Several are *warnings* (whispers, darkness) that telegraph bigger events.
- Events should be readable in the log for debugging, but not spoil the first encounter.
- Accessibility: visual intensity tied to the accessibility options (see `design/README.md`).

## Open questions

- Whether players can *suppress* events via wards/cleansing at scale.
- Event frequency caps for multiplayer servers.
