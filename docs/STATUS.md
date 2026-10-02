# Status & handoff

Read this together with `AGENTS.md` after any context compaction. It is the short, current

## How to resume after compaction

1. Read `AGENTS.md` (working rules — subagents; **host safety, no OOM**).
2. Read this file.
3. See the continuation plan below (chat).
4. Check the board: <https://github.com/users/Sanchous98/projects/2>.

## Project

Eldritch Horror — an RPG-flavoured horror mod for **Minecraft 26.3 / NeoForge / Java 25**.
Repo: <https://github.com/Sanchous98/eldritch-horror> (branch `main`, all pushed).

## Done (verified)

- **Earth world generator**: real baked layers (landmask, ETOPO elevation, Köppen), real
  terrain + biomes, Overworld replaced. `Earth world generator ready` on server start.
- **World ×8**: extent **131 072 × 65 536** blocks, layers **16 384 × 8 192** (8 blocks/px),
  elevation **8-bit** (`round(m/75)+127`, ~21 MB). `settlements.json` at 364 blocks/°.
  Explicit **world border** (set on `ServerStarted`).
- **Location frame** (`world/loc/`): `StructureBuilder`/`Builder`, `Location`, `Locations`
  registry, shaped ops (pitchedRoof/spire/buttress/window/crenellations/monument/scatter/
  ruins), `Tier`, `Palette`, `Materials` (26.3 dyed blocks via `ColorCollection.pick`).
- **24 cultural city styles** (`world/loc/style/*Style.java`, one file per city) + `StyleKit`
  shared landmarks (cathedral/mosque/dome/minaret/pagoda/torii/steppedTemple/obelisk/statue/
  lantern). Tokyo is the reference (shikkui + kawara + vermilion + neon + cherry blossom).
- **Iconic self-generated landmarks**: every one of the 24 cities now carries a recognisable
  silhouette built procedurally in its own style file (Eiffel Tower, Big Ben + Parliament,
  Statue of Liberty, Colosseum, Giza pyramids + sphinx, Taj Mahal, Sydney Opera House,
  Christ the Redeemer, Hagia Sophia, St Basil's, Hall of Supreme Harmony, Gyeongbokgung,
  Himeji-style castle, Oriental Pearl, Gateway of India, Wat Arun, Monas, Capitol Records,
  Lagos National Theatre, a colonial twin-tower cathedral, KICC, Castle of Good Hope, the
  Obelisco, the Angel of Independence). No imported/third-party builds (see below).
- **Cities at contract size**: district 380 (~760 across), bigger landmarks, decay pass.
- **City renderer** (`core/CityRenderer`, dev-only `-Deh.renderCities`): PNGs to `run/render/`,
  tick-sliced. It now **shades by height** (top-down) and applies directional relief (iso), so a
  tall landmark reads even when it shares its material with the ground. A labelled contact sheet
  can be built with `tools/contact_sheet.py <render-dir> <out.png>` (needs Pillow).
- **All 24 cities rendered and reviewed** (Phase 1). Verdict: culture reads by colour; then the
  systemic issues below were fixed and all 24 re-rendered.
- **Style fixes**: duplicated landmarks separated (Paris gothic cathedral vs Mexico City colonial
  dome + low bell towers + Aztec platform; Istanbul Ottoman mosque vs Cairo Mamluk madrasa);
  **landmarks scaled 2–3×** so they dominate; the district is **paved into urban fabric**; cities
  **never build on water**.
- **Surface decoration** (`world/SurfaceDecorator`): deterministic grass/flowers/trees/cactus/
  bamboo chosen by Köppen, chunk-clipped, skips city footprints.
- **Cylinder + Morok** (`world/BoundaryTravel`, `EarthMap`): longitude wraps (`floorMod`), the
  seam warp carries a player across `|x|>=HALF_WIDTH`, and a lethal escalating polar debuff
  (darkness/nausea/weakness/slowness → magic damage) past `|z|>HALF_HEIGHT`. Morok never casts
  you back.
- **Varied city fabric**: lots are a mix of houses (height 2–10, flat or pitched), 12–20 towers,
  open squares and walled gardens; the district clears vanilla biome vegetation so a jungle city
  is not buried by its own trees.
- **Second-echelon sites reviewed** by render: `drowned_temple` was built on the deep seabed and
  showed as open ocean — it now rises above sea level on a pedestal. The other four read but are
  small/plain (a later polish pass). See `docs/STATUS.md` render notes.
- **Site polish (v2)**: `Location.renderRadius()` decouples the rendered extent from the cull
  radius, so sites fill the frame; `cult_stronghold`/`order_vault` gained taller walls/towers/a
  dominant strongroom; the altar got a stepped plinth and a ring of standing stones (now legible
  from above); `rift_scar` became a meandering canyon with a leaning monolith. The altar and rift
  were also moved onto flat lowland (picked from the baked elevation map) because their fixed
  original spots sat on ~900 m of relief, where a shallow feature reads as a hill. Sites remain
  five fixed coordinates.
- **Honest coastline**: the landmask is authoritative — ETOPO shelf elevations can no longer
  surface as land in shallow water (fixed the Rio de la Plata estuary).
- **Second-echelon sites** (`world/loc/site/`): `cult_stronghold`, `order_vault`, `drowned_temple`,
  `ritual_altar_site`, `rift_scar` — 5 `Location`s registered as fixed coordinates (29 locations).
- **Tests**: JUnit via `./gradlew test` — `EarthMapCylinderTest` (2 tests) guards the longitude
  wrap and the ocean seam, loading the real baked layers.
- **RPG systems** (`design/27-systems-framework.md`): Sanity = per-player value
  (replaces **hunger**), Corruption = per-player value + per-chunk **taint** (replaces
  **experience**), Reputation = per-cult integer. Mixins disable hunger (`FoodData.tick`) and
  XP/enchanting. Storage = NeoForge attachments; the HUD (`ClientHud`) draws sanity where the food
  bar was and corruption where the experience bar was. Cults: `CultDefinition` + 3 seeds
  (`drowned_choir`, `unblinking_eye`, `hollow_choir`) with opposed pairs.
- **Sanity core implemented** (`sanity/`): `SanitySource` SPI + `SanitySources` registry,
  `SanityState` (Composed…Marked), `SanityTicker` (1/s, overworld, `enableSanity`-gated), and the
  `madness`/`marked` effects applied on state transition. Sources: `darkness` (drains at night or in
  low light; a city shelters you), `city` (recovers inside a curated city footprint) and `morok`
  (drains with polar depth). A player `eldritch_horror:max_sanity` attribute (`registry/ModAttributes`,
  default 100) sets the ceiling; bands and clamps follow it.
- **Corruption core implemented** (`corruption/`): `CorruptionSource` SPI, `CorruptionState`
  (Dormant…Claimed), `CorruptionTicker`, the `corrupted` effect, and `TaintSystem` (per-chunk taint
  diffuses once a second across the loaded chunks around players; player corruption rises in a
  tainted chunk). Config moved to `Type.SERVER`. Commands `/eh sanity|corruption|rep|taint`.
- **Rite framework** (`rite/`): `RiteDefinition` + `Rites` (the 8 rites from `design/08-rituals.md`
  with their exact tiers and **cost**), a per-player `RITE_KNOWLEDGE` attachment (synced, copyOnDeath),
  `/eh rite <id>` and `/eh rites`, and tome-reading grants knowledge of the rite (main-hand only).
  Performing a rite charges its cost and reports the outcome as **not yet implemented** — the
  resolution engine (wards, spawns, rifts, reputation) is the next layer.
- **Rite resolution** (`rite/RiteEngine`): performing a known rite charges its cost then resolves the
  outcome server-side - wards, water blessing, a skill reset, a capped hostile summon, rift
  opening/closing (rift_anchor + chunk taint), and cleansing (lowers the player's corruption and
  local taint). `altar_core` is an interactive block that lists your known rites.
- **Taint changes the world** (`corruption/TaintWorld`): a tainted loaded chunk converts a capped
  few natural surface blocks to `tainted_soil`/`corrupt_stone` (deterministic, open-sky only, never
  builds), with `/eh taint purify` as the debug stop.
- **Bestiary** (`entity/`, `design/25`, `design/28`): vanilla bosses are replaced by **Ancient
  Ones** and every vanilla mob by one of ours — vanilla mobs are suppressed except villagers.
  The **full `design/25` roster is implemented**: Ancient Ones (framework + 14 bosses, below),
  hostile lesser (`risen_husk`, `bone_choir`, `drowned_thrall`,
  `veil_stalker`, `weaver_spawn`, `rift_mite`, `shambler_ooze`, `choir_spite`, `night_hag`,
  `blight_pod`, `byakhee`), minions (`lesser_swarm`, `watcher`, `star_spawn`, `shoggoth_mass`),
  cultists (`worshipper` + `cult_zealot`/`cult_raider`/`rite_binder`/`plague_crone`), mundane
  (`deer`, `wool_hare`, `mire_sow`, `ash_fowl`, `burrowling`, `pack_beast`, `grey_fox`, `hill_hound`,
  `hearth_cat`, `wool_beast`, `bog_bear`, `tide_grazer`, `spore_bee`, `stone_sentinel`) and ambient
  (`cave_drifter`, `pale_drifter`, `lantern_jelly`, `marsh_mote`, `drowned_minnow`, `frost_wisp`).
  Shared: a dread-aura trait + one ticker, one `topUp`/`topUpAir` spawner, a goals library
  (stalk/pack-alert/retreat/ambush), a mundane taint-conversion helper, and a reusable
  mundane-weapon ward (`EldritchWarded`). **Site populations** inhabit the 5 sites via one bounded
  pass. Cities are populated with crowds of villagers.
  **Ancient Ones** (14/20): Cthulhu, Dunwich Horror, Shub-Niggurath, Azathoth, Yog-Sothoth, Ithaqua,
  Yig, Atlach-Nacha, Nyarlathotep, Cthugha, Glaaki, Hydra, Nyogtha, Rhan-Tegoth — each with a
  sanity/corruption aura and a **non-combat solve** (`Outcome.SOOTHE`, taught from a tome). The
  last six need new minor sites and are deferred. **World events** (`event/`, design/19): a bounded
  data-driven framework + whisper, darkness_pulse, rift_bloom, veil_thin, hallucination_wave,
  cleansing_dawn (`/eh event`, `/eh events`). **Codex** (`codex/`, design/22): a per-player LORE
  attachment + discovery of 112 entries (sites, cities, bosses, bestiary, rites, events);
  `/eh codex`. **Recipes/economy** (design/16): 25 crafting/smelting recipes make the reagents,
  ritual blocks, tools, gear and utility items obtainable; every reagent has an in-game source.
  Remaining roster: 6 Ancient Ones needing new minor sites; 4 bespoke world events.
- **Public APIs** (`SanityAPI`/`CorruptionAPI`/`TaintAPI`): the single entry point content uses;
  they delegate to the systems. **Items are functional**: all 15 consumables move sanity/corruption
  by their documented deltas and show a colour-coded tooltip; the 10 tomes charge their rite cost
  (the grant is deferred until a rite-knowledge system exists). Cult `REPUTATION` is synced to the
  owner and ranks are localised (`rank.eldritch_horror.*`).
- **City polish**: varied street paving, a soft rim that fades into the wild (no hard circular
  cut), generic street dressing (stalls/crates/wells) placed before buildings and occupancy-gated.
- **City terraces**: the district is a hillside city, not one flat plate - 24-block terrace tiles
  take the local terrain level (quantised to 2 blocks), joined by steps, with a flat plaza/landmark
  heart; each lot is levelled on a small pad so buildings sit flat while the tiles around them step.
- **City generation fixes (player-reported):** the city level is the mean terrain over the interior
  (not the centre point) and an `EDGE_RING` blend grades the rim into the wild instead of a cliff;
  buildings are placed chunk-locally on a grid-canonical hash (no global cap, so the whole district
  fills); vegetation is cleared to the original surface height (no floating trees); every storey
  gets windows (no blank facades); `buttress` is a solid stepped wedge (no floating columns); every
  walk-in landmark has a ground-level door.
- **Site renderer**: `CityRenderer` can render the second-echelon `Location`s (`-Deh.renderCities=sites`),
  with each site's true radius.
- **Item registry**: **128 content-stub items** across 11 category files
  (`registry/items/*.java`) through one frozen `ModItems.add(id, stack)` surface, with a single
  creative tab, `en_us.json` names + 128 tooltips. Effects are recorded in comments/design only.
- **Design docs**: `design/24-sanity-and-corruption.md`, `25-bestiary-and-entities.md`,
  `26-rituals-and-occult.md`, `27-systems-framework.md` (+ README index).
- **Design**: `design/23-boundary-and-travel.md` (cylinder + Morok);
  `docs/STRUCTURES-CONTRACT.md`; `AGENTS.md`; `docs/HOST-SAFETY.md`.
- **Safety**: hard caps in `docker-compose.yml`, `scripts/guarded-run.sh`.

## Fixed (notable)

- Palette for a city's style was bypassed in `Locations.place` (grey cities) — fixed.
- World border crashed startup (`ServerAboutToStart` → `ServerStarted`).
- Renderer used the legacy city radius (captured only the centre) — fixed.
- **Startup bugs found by a real server run** (invisible to `build`+PMD): `ModEntities` built
  `ANCIENT_ONES` in a `static` initialiser via `DeferredHolder.get()` (unbound value) — now lazily
  resolved; `EldritchCommands` read SERVER `ModConfig` while the command tree was built — commands
  are registered unconditionally and the toggles are checked in the handlers; loot conditions used
  the inner key `"condition"` instead of `"type"` — fixed.
- **Clamp standardised on JDK `Math.clamp`** (Java 25 has every needed overload; int calls bind to
  `(long,int,int)` and return `int`). All 20 `Mth.clamp` call sites swapped; unused `Mth` imports
  dropped. `EarthMap`/`ElevationCurve` keep local `clamp` helpers (Minecraft-free, unit-tested).
- **PMD ruleset: `CloseResource` + `GuardLogStatement` considered and rejected.** `CloseResource`
  flags engine-owned `ServerLevel`/`ServerChunkCache`/`MinecraftServer` (must not be closed) and
  closes already handled by try-with-resources → 76 false positives. `GuardLogStatement` fires on
  every parameterised `{}` log call (~15). Both documented as deliberate exclusions in the ruleset;
  still 0 violations.

## Known limitations / deferred

- Cities have **no services/state** yet (deferred until those features exist).
- **Implemented and playable:** bestiary (Ancient Ones + lesser/mundane/ambient), cults (definitions
  + synced reputation + ranks), rites (`RiteEngine` resolves wards/summons/rifts/cleansing), taint
  world-conversion, world events, codex and the recipe economy. Sanity and corruption have real
  tickers, states, effects, config and item interaction. Morok is implemented on both axes (mob
  effects + a sanity source).
- **Still deferred:** 6 Ancient Ones (Hastur, Nephren-Ka, Abhoth, Chaugnar Faugn, Tulzscha,
  Zstylzhemghi) need new minor sites; 4 bespoke events (`cult_procession`, `blood_moon_rite`,
  `star_fall`, `hollow_call`); rite **reagent-cost consumption** (needs a change to the frozen
  `RiteDefinition`); a client map/codex screen; city services.
- Some city **palettes remain close** (Paris/Buenos Aires, Jakarta/Los Angeles, London/Moscow);
  now largely distinguished by their differing landmarks, but colour separation is still loose.
- Terrain decoration is **light** (1% trees) and sub-pixel on the 8 blocks/px review renders,
  so it is judged in-game rather than on the map PNGs.
- Spawn is vanilla-chosen; a pinned coastal spawn is a tracked task.
- **No third-party builds are imported.** Landmarks/cities are generated procedurally from our
  own code. Community schematics (e.g. Planet Minecraft) were considered and rejected: most are
  All-Rights-Reserved (we publish to a public repo), and raw `.schematic/.litematic` do not drop
  into this generated world. If ever desired, only CC0/CC-BY (or the author's own) files may be
  used, converted to `.nbt` and dropped in as a landmark (the seam in `STRUCTURES-CONTRACT.md`).
- **Rootless docker** starts manually each session (see `docs/HOST-SAFETY.md`); it does not
  auto-start.

## Open incident

The host OOM (dockerd killed, all containers stopped) was caused by the dev container having
the host `docker.sock` (rw): spawned containers were **siblings**, unbounded by the container
cgroup. Remedy in progress: no host socket; build inside the container cgroup. See
`docs/HOST-SAFETY.md`. **Do not use the host socket. Run through `scripts/guarded-run.sh`.**

## Where things live

| Concern | File |
|---|---|
| Host safety (no OOM) | `docs/HOST-SAFETY.md` |
| Working rules (subagents) | `AGENTS.md` |
| Design, source of truth | `design/` |
| World generation | `docs/WORLDGEN.md` |
| Locations / parallel authoring | `docs/STRUCTURES-CONTRACT.md` |
| Multiplayer & sessions | `docs/MULTIPLAYER.md` |
| Windows/Docker pitfalls | `docs/WINDOWS.md` |
