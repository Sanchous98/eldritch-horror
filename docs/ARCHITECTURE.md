# Architecture

## Stack

| Piece | Choice | Why |
|---|---|---|
| Loader | **NeoForge** | Rich data-driven systems + capability/attribute APIs; current 26.x line. |
| Minecraft | **26.3** | Current release; Java 25 toolchain, datapack format 121. |
| Build | **ModDevGradle 2.0.x** | Modern plugin; runs the game from Gradle; official MDK path. |
| Java | **25** | Required by Minecraft 26.3. |

## Layers

The dependency arrow is **content → systems → (registry/entry)**; nothing in a system package
depends on a loader class that would break the dedicated-server side.

```
                 +-----------------------------+
                 |  EldritchHorror (@Mod)      |  entry: register + config only
                 +--------------+--------------+
                                |
          +---------------------+---------------------+
          |                                           |
    +-----v-----+                              +------v------+
    | registry/ |  DeferredRegisters           | core/       |  config, utils,
    |           |  (blocks/items/entities/     |             |  shared plumbing
    |           |   effects/sounds)            |             |
    +-----------+                              +-------------+
                                |
          +---------------------+---------------------+----- ...
          |                     |                     |
    +-----v------+       +------v------+       +------v-----+
    | sanity/    |       | corruption/ |       | cult/      |   one package
    | ritual/    |       |             |       |            |   per RPG system
    +------------+       +-------------+       +------------+
                                |
                        +-------v-------+
                        | client/       |  (client-only; never referenced from server code)
                        +---------------+
```

## Per-system plan

### `sanity/`
- Register a `max_sanity` **Attribute** (via `DeferredRegister` on the attribute registry) so other
  mods and equipment can modify it; keep the *current* sanity as a synced per-player value
  (attachment + `AttachmentType` + a small S2C packet), since attributes are max-only.
- A `SanitySource` interface: `tick(Player, SanityState)` returning a delta. Content registers
  sources (darkness, rift proximity, cult presence, …). This keeps drain composable and
  datapack/config-tunable rather than hard-coded.
- Thresholds apply `Madness`, hallucination sounds, and the client overlay.

### `corruption/`
- Per-player value with stages, stored as an attachment, plus a per-`LevelChunk` field for
  environmental corruption. The chunk field drives block conversion and ambient spawns and is
  the expensive part — plan chunk-attachment persistence and only tick near players.
- A `CorruptionSource` parallel to sanity, so content declares its taint without core edits.

### `cult/`
- `Cult` as a **datapack registry** entry (`RitualDefinition`-style JSON): id, rites, demands,
  taboos, ranks. Reputation per-player/per-cult.
- Cultists are an AI faction over a base mob goal set; their behaviour is driven by the cult
  definition rather than a subclass per cult.

### `ritual/`
- `RitualDefinition` datapack type: a **block pattern** (like a multiblock structure), a list of
  offerings, optional conditions (time, moon, dimension, corruption stage), and a list of
  **outcomes** (registered `RitualOutcome` types: spawn, transform, grant item, curse, open rift).
- A resolver validates the pattern around an altar and consumes inputs atomically.
- Failed/refused rituals cost sanity or corruption instead of no-op — see `design/05-ritual-engine.md`.

### `client/`
- HUD meters, the madness overlay, particles, entity renderers. Registered from
  `@EventBusSubscriber(value = Dist.CLIENT)`. **Never** imported by common/server code.

## Conventions

- **One `DeferredRegister` per registry**, held in `registry/Mod*`, registered in the entry point.
- **IDs** are always `EldritchHorror.id("path")`.
- **Config** lives in `core/ModConfig`; server-affecting values belong in a `SERVER` config, not
  `COMMON`, once multiplayer balance matters.
- **Data over code**: cults and rituals are datapack-defined; new content should be JSON first.

## Open questions (decide before the vertical slice)

1. **Sanity storage**: attachment + packet, or a capability? (NeoForge attachments are the modern
   path; confirm sync ergonomics.)
2. **Corruption chunk storage**: `AttachmentType` on `LevelChunk`, or a saved-data map keyed by
   chunk pos? Persistence and cleanup differ.
3. **Ritual matching**: reuse NeoForge's structure template system, or a purpose-built pattern
   matcher? A bespoke matcher is simpler but loses structure-block tooling.
4. **Client rendering**: vanilla `GuiGraphics` overlay vs. a separate render layer for the
   distortion effect.

## Server-first & sessions

The mod targets a **dedicated server with many players**; see
[`MULTIPLAYER.md`](MULTIPLAYER.md). Two rules follow:

- **Server-only is the default.** Gameplay state (terrain, sanity, corruption, reputation,
  rifts, session phase) lives on the server. The client half is limited to presentation
  (HUD, overlays, particles) and must never decide outcomes or generate terrain.
- **The map is server-side.** The baked Earth layers are **common** mod resources read by
  the server at startup through the mod class loader (`world/EarthMap`). No client classes
  are referenced from `world/`. The `ContinentChunkGenerator` and `EarthBiomeSource` run
  only where worldgen runs — the server (or the integrated server in singleplayer).
