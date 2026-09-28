# Development plan — where to start

Opinionated guidance for starting implementation. The roadmap is on the
[project board](https://github.com/users/Sanchous98/projects/2); this document says
*which* issues to do first and *why*, so the project doesn't sprawl.

## The one-line answer

**Build the core plumbing, then ship the Sanity axis as the first playable vertical
slice — end to end, including a test and a debug command — before touching corruption,
rituals, or content.**

Do not start with the ritual engine, the bestiary, or worldgen. They are downstream of
everything below and will be reworked if the foundations change.

## Why sanity first

The three axes are sanity, corruption, reputation. Sanity is the right first target:

- **Self-contained** — it needs no blocks, entities, cults, or worldgen.
- **It exercises the whole spine** — an attribute, persisted per-player state, a sync
  packet, a HUD, a command, and a test. If those work, every later system is easier.
- **It is the mod's signature** and the cheapest thing to make *felt*. You will learn
  whether the game is fun before investing in content.
- Corruption reuses the same plumbing (plus throttled chunk ticks); rituals then become
  "just data + a block".

## Phase 0 — one decision first (30 min)

- **#26 Decide sanity storage mechanism (attachment vs capability).** This shapes #5 and
  every later system that persists per-player state. NeoForge attachments are almost
  certainly right on 26.3, but confirm and write it down.
- #25 (multiplayer semantics) and #27 (ritual matching) can wait — #27 until M4.

## Phase 1 — Foundations, but only the essential subset

Do **not** complete all of M1 before writing a feature. Take the minimum:

| Issue | Why now |
|---|---|
| #43 Registry & resource-id helpers | tiny; already partly done via `EldritchHorror.id()` |
| #42 Split config into COMMON / SERVER / CLIENT | prevents rework; server balance lives server-side |
| #45 Persistence layer (attachments/saved data) | required by #5 |
| #44 Networking framework | required by sanity sync (#5) |
| #46 Debug command suite (`/eldritch ...`) | you will live in this; do it early |
| #48 Unit test setup + #47 GameTest harness | establish the safety net now, not later |

**Defer:** #49 throttled ticks (needed at corruption), #50 datapack conventions (at
rituals/cults), #51/#52 diagnostics, #2 access transformer (add when you actually need
a widened member), #3 logo (cosmetic).

> A good **first commit**: #43 + #46 + #4 — registering one attribute and being able to
> inspect/mutate it with a command. Small, visible, and it proves the dev loop works.

## Phase 2 — Sanity, end to end (the first vertical slice)

In order:

1. **#4** Register the Sanity attributes — `max_sanity`.
2. **#5** State storage + client sync — current sanity, synced.
3. **#6** Sanity source SPI — composable drain/regen (`SanitySource`).
4. **#7** Drain/regen rules — darkness, light, altars, rifts, tomes.
5. **#8** Madness thresholds + effects — whispers, `Madness`, `Marked`.
6. **#9** HUD meter — visibility policy (see the open question).

**Milestone definition (proves the slice):** in a dev world, standing in the dark drains
sanity; resting in light restores it; at 0 the player gets `Madness`; every rate is
configurable; a GameTest covers the thresholds; the HUD reflects the value.

Then iterate on the rest of M2 (#65–#71) — hallucinations, rest/nightmares, items.

## Phase 3 — the other axes, in dependency order

- **M3 Corruption** (#31) — reuses attachment persistence + #49 throttled ticks.
  Start with #10 (stages) → #11 (per-chunk field) → #12 (spread).
- **M4 Rituals** (#32) — needs items (a few reagents) and the altar block:
  #13 (datapack type) → #15 (resolver) → **#16 Altar block + first playable ritual**.
- **M4 Cults** (#33) — after rituals exist to teach.
- Then **M9 Gear** → **M5 Bestiary/endgame** → **M7 World** → **M8 Quest/Dialogue**.

## Ways to start badly (avoid)

- **Breadth-first:** one story from every epic at once. Nothing is playable, nothing is
  testable, and interfaces churn.
- **Content-first:** blocks/entities/textures before the systems that use them.
- **Perfect foundations:** building all of M1 before any feature. You will guess wrong
  about what you need; let the first feature pull the plumbing.
- **Numbers on paper:** balance sanity/corruption only after you can *feel* them in-game.

## Practical cadence

- Get the **dev client running early** (`docker compose --profile gpu run … runClient`
  or local) and play your own mechanic every time it changes.
- Write the **test with the feature** (#48/#47), not as a later chore.
- Do a **tuning pass** after the first playable slice, before adding content.
- Keep the **bible and board in sync**: when a design question is answered, close its
  `Decide …` issue and update the matching `design/*.md`.

## Definition of "first milestone reached"

> A player can start a dev world, watch sanity drain in the dark and recover in light,
> hit `Madness` at zero, and tune all of it from config — with a test proving the
> thresholds and a command to inspect the value.
