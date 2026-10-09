# 05 — Ritual Engine

Rituals are the progression engine. A rite is a **data-driven** recipe performed at an
altar: match a block pattern, satisfy conditions, consume offerings, resolve outcomes.
Failure always costs something.

## Altar pattern (implemented)

`RiteEngine` refuses a rite unless an `altar_core` stands within `riteAltarRadius` (default 6) of
the performer, marked by at least `riteAltarMarks` (default 4) `rune_stone` or `ritual_chalk` blocks
(`rite/RitualAltar`, loaded chunks only). `requireRiteAltar` disables the gate. The
`ritual_altar_site` places a real core plus eight rune stones, so it is a working altar; all three
blocks are craftable.

> Content: the actual rites are in [`08-rituals.md`](08-rituals.md). This document is the
> engine's rules.

## A ritual definition

| Field | Meaning |
|---|---|
| `altar` | The centre block (altar tier gates the rite) |
| `pattern` | Relative block pattern around the altar |
| `offerings` | Items (or blood/health) consumed |
| `conditions` | Time, moon, weather, dimension, corruption stage, proximity |
| `on_success` | One or more outcomes |
| `on_failure` | Cost applied when validation fails or is refused |

## Resolution pipeline

1. **Identify** — player triggers an altar with a known rite selected.
2. **Validate pattern** — match `pattern` against the world (rotation-aware).
3. **Validate conditions** — time/moon/weather/dimension/stage/proximity.
4. **Validate offerings** — present and sufficient (or blood, if allowed).
5. **Consume** — offerings removed **atomically** (all-or-nothing).
6. **Resolve outcomes** — run each outcome in order.
7. **Record** — cooldown, reputation, codex updates.

Any failure at 2–4 applies `on_failure` (never a silent no-op).

## Outcome types

| Type | Effect |
|---|---|
| `spawn` | Spawn N entities at/near the altar |
| `grant` | Give items |
| `transform` | Convert blocks/entities (e.g. corruption) |
| `curse` | Apply a negative effect or cursed item |
| `open_rift` / `close_rift` | Change rift state and link |
| `grant_skill` | Unlock a skill/rite or teach knowledge |
| `reputation` | Adjust standing with a cult |
| `sanity` / `corruption` | Adjust an axis |

Outcomes are registered as types, so content packs extend them without core edits.

## Learning

- Possessing the definition does **not** grant the rite.
- Learned from: **tomes** (costs sanity/corruption) or **cults** (rank service).
- The codex records known rites; unknown ones are hidden or hinted.

## Altar tiers

| Altar | Max rite tier |
|---|---|
| Crude Altar | 1 |
| Consecrated Altar | 2 |
| Blasphemous Altar | 3 |
| Choir Altar | Endgame |

## Backlash

- Failure costs sanity or corruption (listed per rite).
- At high corruption, failure can *still* consume offerings or summon something unwanted.
- Backlash is intentional: experimentation has weight.

## Cooldown & safety

- `ritualCooldownTicks` (config) prevents spam.
- Multiple performers at one altar are handled serially to keep outcomes deterministic.

## Open questions

- Matcher strategy: structure templates vs. a bespoke matcher (see `docs/ARCHITECTURE.md`).
- Whether offerings are ever partially consumed on a near-miss.
