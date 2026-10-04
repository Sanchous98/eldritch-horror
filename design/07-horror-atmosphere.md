# 07 — Horror & Atmosphere

*How* the game is frightening. Dread is designed as a system, not a jump-scare.

## The principle

The horror acts **through the world** before it is ever an entity: weather, whispers,
corrupted terrain, cult activity, events. The player feels watched long before anything
attacks.

## Levers

| Lever | Used by |
|---|---|
| **Information hiding** | Partial sanity visibility; unknown rites; the Watcher |
| **Consequence** | Failure costs; corruption is permanent |
| **Ambience** | Soundscape, fog, colour grading, particles |
| **Escalation** | Ambient events scale with corruption and rifts |
| **The uncanny** | Hallucinations, non-Euclidean rifts, moving landmarks |
| **Helplessness** | Early entities that cannot be killed; sanity as the real health bar |

## The Watcher

- Follows at a distance; **hides when observed directly**.
- Drains sanity while in line of sight.
- Cannot be conventionally killed early — it is a *presence*, not a fight.

## Events as dread

Ambient events (`19-events.md`) telegraph and escalate: whispers → darkness → rift bloom.
The player learns to read the world.

## Hallucinations

- Client-side only (no server entities): fake mobs, sounds, whispers.
- Triggered at low sanity; vanish on approach or over time.
- Honest to the player's *state*, not to the world.

## Soundscape

- Whispers (directional), chants (direction + distance), altar hum (proximity).
- Silence is a tool: the quiet before an event.

## Accessibility

Horror must be **fair**: photosensitivity, colour-blind palettes, motion and overlay
options, and a config to soften distortion. See `13-ui-ux.md` and the accessibility story.

## Anti-patterns

- No untelegraphed lethal scares.
- No effect that removes player agency with no counterplay.
- No gore for its own sake.

## Open questions

- How much information the HUD should reveal (meter vs. stage only).
- Whether the Watcher can ever be banished.
