# 24 — Sanity and Corruption

Status: **decided (design)**; implementation deferred. Consolidates `03a-sanity.md` and
`03b-corruption.md` into the current model, and treats Morok (`23-boundary-and-travel.md`) as
the **first implemented escalation hook**.

## The two axes

| Axis | Horizon | Recovers? | Owns |
|---|---|---|---|
| **Sanity** | seconds–minutes | yes (light, rest, cities) | what you perceive and can do *now* |
| **Corruption** | permanent (per session) | almost never | what you are *becoming* |

Sanity is a **state**; corruption is an **identity**. Sanity is the horror stat you manage
tonight; corruption is the tab you can never fully close (see `04-pillars.md`: power has a
price). The two interlock: high corruption suppresses sanity recovery and unlocks the worst
effects.

## Sanity replaces hunger (decided)

There is **no hunger/food system**. Vanilla hunger is disabled (the player's `FoodData` is
pinned full — no food drain, no starvation, no hunger-based weakness) and the **sanity meter
occupies the food bar's HUD position**. This mirrors the tabletop, which has no food track.
Food is therefore **not a survival resource**: crops/animals/fish remain only as
offerings/reagents for rites. Health regeneration is decoupled from food (see Implementation
notes).

## Sanity model (decided)

- **Server-authoritative, per player.** The client only renders the synced value; it never
  computes a drain, threshold or effect (`docs/MULTIPLAYER.md`).
- **`max_sanity` attribute** (default 100, range 0–100), changed only by skills/gear/traits.
- **Composable sources.** Every drain/regen is a `SanitySource.tick(player, state) -> delta`;
  content registers its own. The core sums, clamps `[0, max]`, and applies threshold effects.
- **Tick cadence:** evaluated on a fixed server tick (proposed: 1 s), throttled to players
  near a relevant cause, never globally.

## What drains sanity (proposed rates)

| Source | Effect | Notes |
|---|---|---|
| Darkness (light < 4) | `−0.2 /s` | doubled underground |
| Night, outdoors | `−0.1 /s` | stacks with darkness |
| Ruins / second-echelon sites | `−0.3 /s` | within a site radius (see `docs/STRUCTURES-CONTRACT.md`) |
| Rift within 32 blocks | `−1.0 /s`, scaled by proximity | see `20-map.md` |
| **Morok** past the polar edge | escalating `−…/s` | depth-scaled; see `23-boundary-and-travel.md` |
| Witnessing an eldritch entity | `−3` burst, then `−0.5 /s` in line of sight | |
| Watcher in line of sight | `−0.5 /s` | cannot be killed early (`17-mobs.md`) |
| Reading a forbidden tome | `−10` burst | knowledge costs now, corruption forever |
| Performing an occult act | per-rite cost | see `26-rituals-and-occult.md` |

All rates scale by `sanityDrainMultiplier` (server config) and by corruption stage.

## What recovers sanity (decided)

| Source | Effect | Notes |
|---|---|---|
| Daylight / well-lit area | `+0.5 /s` | the anti-darkness |
| City / settlement | `+1.0 /s` | **safe haven** aura; the reason cities matter |
| Sleeping | `+4`, once per night | reduced at high corruption; may become a nightmare |
| Wards / cleansing | burst `+…` | rite-driven (`26-rituals-and-occult.md`) |
| Order services | burst | costs coin/standing (`21-settlements.md`) |

Rest above a threshold clears that threshold's effects; recovery is intentionally easier in
civilisation and harder the deeper you go.

## Escalation ladder (perceptual → gameplay → lethal)

| Sanity | State | Tier | Effects |
|---|---|---|---|
| 70–100 | **Composed** | — | none |
| 40–69 | **Uneasy** | perceptual | occasional whispers; subtle screen pulse |
| 20–39 | **Fraying** | perceptual | directional whispers; brief distortions; false sounds |
| 1–19 | **Breaking** | gameplay | server `Madness` debuff; periodic hallucinations; blindness flashes |
| 0 | **Marked** | lethal | the horror can reach you anywhere; hallucinations become hostile; ambient events converge on you |

Perceptual tiers are **client-rendered** (safe, fake). Gameplay/lethal tiers are
**server-decided** (debuffs, spawns, forced events). Sanity 0 does not kill instantly — it
makes the world *eligible* to kill you.

## Corruption model (decided)

- **Per player:** `0–100` with stages; persisted, server-authoritative. **Sources register**
  (`CorruptionSource`); content declares its taint.
- **Per chunk:** a corruption *field* driving block conversion and ambient spawns
  (`03b-corruption.md`, `20-map.md`). Shared by everyone.
- Corruption **cannot be bought back** in general — only rare rites lower it, and they carry
  their own cost. High corruption is a **gate**, not pure penalty.

| Stage | Range | Effect |
|---|---|---|
| **Dormant** | 0–9 | clean; cults ignore you |
| **Touched** | 10–39 | see rifts; some fauna react; tier-3 rites open |
| **Marked** | 40–69 | villagers fear you; sleep restores less; dark rites; summoning |
| **Claimed** | 70–100 | locked out of the resistance path; endgame; the horror knows your name |

**Corruption changes the body:** altered dialogue/reputation, visible marks, hostile fauna,
reduced sanity regen, and different dream content. It is the permanent twin of sanity —
where sanity is what tonight costs, corruption is what the run costs.

## Interaction with Morok (`23-boundary-and-travel.md`)

- Morok **drains sanity first, then health**, escalating with depth past the edge; turning
  back early is the only survival. Sanity is the warning bell, not a shield.
- At `Marked` sanity, Morok's escalation accelerates — the same place that already hurts
  starts killing sooner.
- Morok is the **first implemented sanity/health hook**; the general framework above must be
  able to express it as one `SanitySource` plus one damage source.

## Interaction with knowledge (`22-map-and-knowledge.md`)

- Knowledge and sanity trade both ways: tomes/rites **cost** sanity and corruption, and low
  sanity **corrupts perception** (map smears, markers lie — the Morok principle made general).
- Corruption stage gates what the map reveals (rifts, cult strongholds, the overlay) and what
  rites are available. Discovery and corruption are per player; the chunk field is shared.

## Multiplayer rules

- **Server-authoritative, no client trust.** Thresholds, effects, spawns and damage are
  decided server-side; the client may only send *intentions* (e.g. "sleep", "begin rite").
- **Individual by default** (`12-multiplayer.md`); an opt-in server config may switch to
  averaged/shared pools. No automatic difficulty scaling by player count; nearby players' low
  sanity does **not** drain yours.
- **Chunk corruption is shared**: cleansing helps everyone, opening a rift hurts everyone.

## Open questions

- Always-visible sanity meter, or only below a threshold? (horror favours partial information).
- Does corruption *ever* fully clear (a "clean" ending), or only reset at session boundary?
- Exact numbers for the Morok ↔ sanity curve; whether Morok also applies off the polar ice.
- Whether high sanity grants any positive resist effect, or is purely the absence of bad ones.

## Implementation notes (later)

- **Attachments:** current sanity + corruption live on a server-side player attachment
  (`SavedData`/capability), synced to the owner only.
- **Source registry:** a datapack/registry list of `SanitySource` and `CorruptionSource`
  entries so content packs extend without touching core.
- **Threshold effects:** a table (config/data) mapping state → debuff set; apply/clear on
  transitions, not every tick.
- **Client half:** HUD meter, whisper/particle overlay and hallucinations — no gameplay state.
- **Replaces hunger:** on the client, hide the vanilla `FOOD` layer and draw the sanity meter in
  its place; on the server, pin `FoodData` full each tick (no mixin required if the GUI-layer
  event and `FoodData` accessors are available in 26.3 — verify at build time).
- **Config (`SERVER` type):** `enableSanity`, `enableCorruption`, `sanityDrainMultiplier`,
  per-source rates, stage thresholds, pooled-vs-individual mode.
