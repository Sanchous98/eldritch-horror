# 26 — Rituals and the Occult

Status: **decided (design)**; implementation deferred. Builds on the engine rules in
`05-ritual-engine.md` and the rite list in `08-rituals.md`; this document is the
**player-facing** occult layer: ceremonies, branches, gates and costs.

## What a ritual is

A rite is a **ceremony at a site**, not a crafting recipe. It has three visible parts:

1. **A place** — a `ritual_altar_site` location (`20-map.md`,
   `docs/STRUCTURES-CONTRACT.md`) or an altar inside a larger site. Altar tier gates the
   rite tier (Crude → Tier 1 … Choir → endgame).
2. **A multi-block pattern** — a rune circle / ring of `eldritch stone` around the altar,
   matched rotation-aware against the world (`05-ritual-engine.md`). Larger and stricter
   with tier.
3. **Multi-item offerings** — reagents laid on/around the altar, plus optional blood/health.
   Consumed **atomically** on success only.

The engine already defines the pipeline (identify → validate pattern → conditions →
offerings → consume → resolve → record). This doc constrains the *content* that pipeline
carries.

## Branching consequences: boon vs corruption

Every rite resolves down **one of two axes**, and the player should see which before
committing:

- **Boons** — safety, wards, cleansing, knowledge, travel, temporary power. Usually cost
  offerings + sanity; may cost a little corruption.
- **Corruption** — summoning, rift-opening, pacts, forbidden knowledge. Grant real power,
  **always add corruption**, and often add an ongoing cost.

A rite is never free of both. Per `04-pillars.md`, if it makes you stronger with no axis
cost, it is not a rite. Failure is not a no-op: the listed `on_failure` cost applies
(sanity and/or corruption), and at high corruption a failure can still consume offerings or
summon something unwanted (`05-ritual-engine.md`).

## The rift gate (the occult travel branch)

The travel principle in `23-boundary-and-travel.md` is frozen: cities are network nodes,
and the **occult branch** is the rift gate.

- A **rift gate** is opened by rite (`open_rift` / gate rite) at a deep rift or a
  `ritual_altar_site`; it is a two-way anchor (`20-map.md`).
- **Cost model:** occult fast-travel pays in **corruption**, not coin — the fast route is the
  tainted route. City travel stays mundane/costly-in-kind; rift travel is fast, dangerous,
  and marks you.
- **Instability:** a gate the horror also uses; rifts can open *near where you arrive*
  (map event `rift_opened`), and the route itself drains sanity constantly
  (`20-map.md`). Travel is progression-gated by learning the gate rite, not by gear.
- Exact node-linking rules remain deferred (`23-boundary-and-travel.md` open questions).

## Summoning

- `spawn` is an outcome type (`05-ritual-engine.md`); summoning is its occult expression.
- **Calling** lesser things (`call_the_lesser`) is the entry drug; **summoning** star-spawn
  and eventually **the Horror** (`11-endgame.md`) is the summit.
- Summoning always carries the largest corruption cost and creates a real, persistent threat
  — summoned entities do not politely leave (`25-bestiary-and-entities.md`).
- The Hollow Choir is the teaching channel for these; `summon_star_spawn` / `open_rift` /
  the summon ending are reputation-gated (`09-cults.md`, `11-endgame.md`).

## Warding and sealing

The counter-branch: **ward** (prevent) and **seal** (undo).

- **Wards** (`ward_of_the_eye`, ward tokens) create safe rooms/havens: they suppress sanity
  drain, repel lesser spawns, and create a **damage window** in boss fights
  (`18-bosses.md`).
- **Sealing** closes rifts (`close_rift`), recedes the corruption field
  (`19-events.md` `cleansing_dawn`), and lowers the world's danger. It is the Order's path.
- Warding/sealing are deliberately weaker than summoning *per action* but are the only way
  to **recover** a corrupted area; they cost time and rare reagents, not power.

## The cost model (time, materials, sanity, corruption)

| Cost | Applies to | Feel |
|---|---|---|
| **Time** | all rites (cast + cooldown) | rituals are a commitment; you can be found mid-rite |
| **Materials** | all rites (offerings) | drives exploration, trade, and site visits |
| **Sanity** | most rites; failure always | the immediate price of meddling |
| **Corruption** | dark rites; always on success | the permanent price; gates the endgame |
| **Reputation** | cult rites | helping one closes another door (`04-pillars.md`) |

The **stronger the outcome, the higher the tier, the longer the cast, the rarer the
offerings, and the larger the corruption**. There is no clean build.

## Driving Map and knowledge progression (`22-map-and-knowledge.md`)

Rites are the engine that moves the map and the codex:

| Rite action | Map event (`22-map-and-knowledge.md`) | Knowledge effect |
|---|---|---|
| `open_rift` | `rift_opened` — adds a pulsing rift marker | unlocks deeper rift content |
| `close_rift` | `rift_closed` / `cleansing_dawn` | reveals what the rift hid |
| summoning / dark rites | `veil_thin`, `settlement_uneasy` | codex entries, cult rank |
| cleansing / reclaim | `settlement_reclaimed` | Order favour, safe nodes |
| a site rite performed | (marker state) | site becomes a taught/known node |

- Rites must be **learned**, not merely held: from **tomes** (costs sanity now, corruption
  forever) or **cults** (rank service) — `05-ritual-engine.md`, `04-pillars.md` pillar 2.
- Performing a rite records it in the **codex** and can reveal previously-hidden markers;
  knowledge is per player, world effects are shared (`12-multiplayer.md`).

## Open questions

- Matcher strategy for the multi-block pattern (structure templates vs bespoke matcher;
  see `05-ritual-engine.md` / `docs/ARCHITECTURE.md`).
- Whether a failed rite can partially consume offerings at high corruption.
- How rift-gate nodes link: a fixed graph, discovered anchors, or per-player.
- Whether wards are physical blocks, applied effects, or both.

## Implementation notes (later)

- Content is **data-driven**: each rite is a definition (altar, pattern, offerings,
  conditions, outcomes, `on_failure`); no per-rite Java class (`05-ritual-engine.md`).
- Outcome types are registered so content packs add new ones without core edits
  (`spawn`, `grant`, `transform`, `curse`, `open_rift`, `close_rift`, `grant_skill`,
  `reputation`, `sanity`, `corruption`).
- Altar sites are `Location`s built through `StructureBuilder` — one file per site, no direct
  block writes (`docs/STRUCTURES-CONTRACT.md`).
- **Server-authoritative:** cast validation, consumption and outcomes run server-side;
  multiple performers at one altar are handled serially for determinism.
- Config: `ritualCooldownTicks`, per-rite costs, gate-linking rules (`SERVER` type).
