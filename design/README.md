# Eldritch Horror — Game Design Bible

**This folder is the source of truth for the game.** If code and this bible disagree, the
bible is right until it is deliberately changed. Everything a designer, implementer, or
artist needs to know about *what the game is* lives here.

`docs/DESIGN.md` is a short summary; `docs/ARCHITECTURE.md` covers the code. This folder
is the complete specification.

## Reading order

**For everyone (the 30-minute version):**

1. [01-overview.md](01-overview.md) — the pitch, the three axes, the loop.
2. [04-pillars.md](04-pillars.md) — the four rules every decision must obey.
3. [02-progression.md](02-progression.md) — how a player advances, start to endgame.

**Systems:**

4. [03a-sanity.md](03a-sanity.md) — the short-term resource.
5. [03b-corruption.md](03b-corruption.md) — the long-term resource.
6. [03c-reputation.md](03c-reputation.md) — the social resource.
7. [05-ritual-engine.md](05-ritual-engine.md) — the progression engine.
8. [06-factions.md](06-factions.md) — how cults work.
9. [07-horror-atmosphere.md](07-horror-atmosphere.md) — how it frightens.
10. [11-endgame.md](11-endgame.md) — the fork.
11. [12-multiplayer.md](12-multiplayer.md) — world vs. player state.
12. [13-ui-ux.md](13-ui-ux.md) — what the player sees.

**Content (what exists):**

13. [08-rituals.md](08-rituals.md) — every rite.
14. [09-cults.md](09-cults.md) — every faction.
15. [10-quests.md](10-quests.md) — every quest chain.
16. [14-classes.md](14-classes.md) — player archetypes.
17. [15-skills.md](15-skills.md) — the skill trees.
18. [16-items.md](16-items.md) — reagents, artefacts, tomes, gear, currency.
19. [17-mobs.md](17-mobs.md) — the bestiary.
20. [18-bosses.md](18-bosses.md) — boss encounters.
21. [19-events.md](19-events.md) — ambient and world events.
22. [20-map.md](20-map.md) — biomes, dimensions, structures.
23. [21-settlements.md](21-settlements.md) — cities and towns (real Earth coordinates).
24. [22-map-and-knowledge.md](22-map-and-knowledge.md) — the world map and map-change events.
25. [23-boundary-and-travel.md](23-boundary-and-travel.md) — the cylinder world, Morok, travel.
26. [24-sanity-and-corruption.md](24-sanity-and-corruption.md) — sanity & corruption (consolidates 03a/03b).
27. [25-bestiary-and-entities.md](25-bestiary-and-entities.md) — the entity roster (consolidates 17/18).
28. [28-ancient-ones.md](28-ancient-ones.md) — the Ancient One bosses that replace vanilla bosses.
29. [26-rituals-and-occult.md](26-rituals-and-occult.md) — the occult systems (consolidates 05/08).
30. [27-systems-framework.md](27-systems-framework.md) — the technical shape of sanity/corruption/cult.

## The one-paragraph summary

You are a person in a world something vast has noticed. You manage three resources —
**sanity** (short-term, regenerating), **corruption** (long-term, near-permanent), and
**reputation** (social, per cult) — and advance by **learning and performing rituals**,
which consume offerings and cost you on an axis. Cults remember and oppose each other.
At the end you either **banish** the horror at great cost or **summon** it.

## Conventions

- IDs are `snake_case` and unique within a document; cross-references use the id.
- Balance numbers are **first-pass placeholders**, not final.
- Each system doc ends with **Open questions** — unresolved decisions to be made.
- When content changes here, update the matching GitHub story.

## Where decisions live

- **Open design questions** are listed at the end of each document and tracked as
  `Decide …` issues on the [project board](https://github.com/users/Sanchous98/projects/2).
- **Implementation shape** (classes, registries, conventions) is in
  [`docs/ARCHITECTURE.md`](../docs/ARCHITECTURE.md).

## Format note

This bible is prose + tables so it reads well in a diff and on GitHub. If the project
later needs validation or data generation, a machine-readable layer can be generated
from these tables; it is deliberately not the source of truth today.
