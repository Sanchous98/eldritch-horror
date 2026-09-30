# 27 — Systems Framework (technical shape)

Status: **decided (design)** for the *framework*; mechanics deferred. This is the technical
spec that `24`, `03a`–`03c` and `06` implement against. It fixes storage, ticks, sync, the
HUD mapping, chunk taint, the cult registry, and the mixin policy — the "sockets and wiring"
the content (items, rites, mobs) will later plug into.

## Shape of the three axes

| Axis | Kind | Store | Horizon | HUD slot |
|---|---|---|---|---|
| **Sanity** | continuous meter | per-player | minutes | **food bar** |
| **Corruption** | continuous meter | per-player **and** per-chunk | whole run | **experience bar** |
| **Reputation** | integer per cult | per-player | whole run | (cult screen, later) |

Sanity is a *state* (recovers); corruption is an *identity* (barely recovers); reputation is
*who will talk to you*. See `01-overview.md` (Lineage) — this is an **RPG analogue of Eldritch
Horror, not a port**: no rounds, dice, decks or tokens.

## Common framework

- **Storage.** NeoForge `AttachmentType`, server-authoritative and persistent. Players carry
  sanity, corruption and reputation; `LevelChunk` carries the corruption field ("taint").
- **Source SPI.** Composable, registered centrally (like `Locations`):
  ```java
  interface SanitySource     { String id(); double deltaPerSecond(ServerPlayer p, SanityContext ctx); }
  interface CorruptionSource { String id(); double deltaPerSecond(ServerPlayer p, CorrContext ctx); }
  ```
  The core sums, clamps, and applies **threshold effects on transitions only** (not every tick).
- **Tick cadence.** One server tick, evaluated ~1/s, and only for players near a relevant cause.
  No sources active → effectively free.
- **Sync.** A custom packet to the **owner only**, throttled (≥1/s or on Δ≥1). The client never
  computes a drain, threshold or effect (`12-multiplayer.md`).
- **Public API.** `SanityAPI`, `CorruptionAPI`/`TaintAPI`, `CultAPI` — the single entry point for
  items/rites/mobs. Content never touches the attachment directly.
- **Config (`SERVER`).** `enableSanity`, `enableCorruption`, multipliers, per-source rates, stage
  thresholds, shared-vs-individual mode.
- **Commands.** `/eh sanity|corruption|taint|rep get|set|add` (debug + admin).
- **Reused content.** `registry/ModEffects` already has `madness`, `marked`, `corrupted`; the
  threshold tables apply/clear those.

## Sanity (`03a`, `24`)

- `double [0, max]`, `max` = the `max_sanity` attribute (default 100).
- States: Composed / Uneasy / Fraying / Breaking / Marked — a small state machine; effects fire on
  the transition, not each tick.
- Sources (all **off by default**, enabled in config): darkness, night, ruins, rift, witnessing,
  tome-burst, rite-cost, **Morok**.
- Recovery: daylight, settlement aura, sleep, wards, Order services.
- **Corruption suppresses recovery** (read via `CorruptionAPI`).

## Corruption (`03b`, `24`)

- **Per player:** `float 0..100`, stages Dormant / Touched / Marked / Claimed; persisted, synced
  to the owner.
- **Per chunk (`ChunkTaint`):** `float 0..1` on `LevelChunk` — **shared by everyone**, persists
  with the chunk. Drives block conversion, ambient spawns, local sanity drain, and map distortion.
  Cleansing a chunk helps all players; opening a rift hurts all.
- Sources: rite use, tomes, rift/altar proximity, tainted blocks.
- Rare, costly cleansing rites are the only way down (see `26-rituals-and-occult.md`).
- It is a **gate, not only a penalty**: high corruption unlocks the dark rites.

## Cult reputation (`03c`, `06`)

- **Data-driven `CultDefinition`:** id, domain, ranks, services, demands, taboos, signature rite,
  opposed pairs. **Java registry for now**, datapack later.
- **Three seed cults:** `drowned_choir`, `unblinking_eye`, `hollow_choir`.
- **Per player, per cult:** `int −100…+100`, stored on the player; ranks are thresholds
  (Outsider / Neutral / Initiate / Member / Devoted / Inner Circle).
- **Opposed pairs** apply cross-consequences: aiding one costs its opposite
  (`drowned_choir` ↔ `unblinking_eye`; `hollow_choir` vs both in spirit).
- **API:** `CultAPI.rep/add/set/rank(player, cultId)`; slow decay toward 0 (config-gated, off by
  default).
- No NPC AI, quests, trading or events in this milestone — definitions + reputation + ranks only.

## HUD mapping (`13`)

| Vanilla slot | Drawn as | Vanilla mechanic |
|---|---|---|
| Food bar | **Sanity** | hunger **disabled** (mixin cancels `FoodData.tick`) |
| Experience bar | **Corruption** | XP/enchanting **disabled** (mixin cancels XP grants) |
| Health, armour | unchanged | unchanged |

**Magic is rituals only** — the enchanting table, XP orbs and Mending have no role.

## Mixin policy

We do use mixins (decided). Enabling them required: a `mixins.json`, a `[[mixins]]` entry in
`neoforge.mods.toml`, and `compatibilityLevel JAVA_22` (26.3 classes are class-file v69, above
Mixin's `JAVA_21`). Verified pitfalls learned the hard way:

- `FoodData.tick` is patched by NeoForge to take **`ServerPlayer`**, not `Player` — the injector
  descriptor must match the runtime signature.
- Mixin must be `JAVA_22` or class-loading fails with "class version 69 required".
- `sponge-mixin 0.17.4` is already on the classpath; there is nothing to add to `build.gradle`.

## Milestone scope ("possible without mechanics")

Build the **whole framework, inert**: attachments, config, commands, API, HUD, mixins for
hunger/XP, threshold tables on the existing effects, and **two opt-in demo sources**
(`darkness`, `city`) so the system is visible and testable. Not yet: the full drain/effect set,
block conversion, spawns, NPC AI, quests, trading, items, rite resolution.

## Open questions

Carried from `24`: meter visibility when composed (default: always, since it replaces hunger);
whether corruption ever fully clears (default: only via a rare rite); exact Morok↔sanity curve;
whether high sanity grants a positive resist.
