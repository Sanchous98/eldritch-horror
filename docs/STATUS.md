# Status & handoff

Read this together with `AGENTS.md` after any context compaction. It is the short, current

## How to resume after compaction

1. Read `AGENTS.md` (working rules — subagents; **host safety, no OOM**).
2. Read this file.
3. See the continuation plan below (chat).
4. Check the board: <https://github.com/users/Sanchous98/projects/2>.

## Project

Eldritch Horror — an RPG-flavoured horror mod for **Minecraft 26.3 / NeoForge / Java 25**.
Repo: <https://github.com/Sanchous98/eldritch-horror> (branch `main`).

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
  **design/25 additions** (`80d4fdc`): the five replacements `ashen_revenant` (wither skeleton,
  corruption), `veil_swine` (hoglin, sanity, herd), `wail_husk` (ghast, sanity, line-of-sight
  drain), `ashen_wisp` (blaze, corruption) and the named `ithaqual_shade` (warden, cold-edge
  spawner, `still_the_shade`); the Choir **Leviathan** (`design/18`, drowned temple, three phases,
  drowned-choir trigger, `still_the_leviathan`). All six have loot tables and generated skins.
  Shared: a dread-aura trait + one ticker, one `topUp`/`topUpAir` spawner, a goals library
  (stalk/pack-alert/retreat/ambush), a mundane taint-conversion helper, and a reusable
  mundane-weapon ward (`EldritchWarded`). **Site populations** inhabit the 5 sites via one bounded
  pass. Cities are populated with crowds of villagers.
  **Ancient Ones** (20/20): Cthulhu, Dunwich Horror, Shub-Niggurath, Azathoth, Yog-Sothoth, Ithaqua,
  Yig, Atlach-Nacha, Nyarlathotep, Cthugha, Glaaki, Hydra, Nyogtha, Rhan-Tegoth, Hastur,
  Nephren-Ka, Abhoth, Chaugnar Faugn, Tulzscha, Zstylzhemghi — each with a sanity/corruption aura
  and a **non-combat solve** (`Outcome.SOOTHE`, taught from a tome). The last six ship with four
  new minor sites (`yellow_court`, `black_pyramid`, `spawning_pool`, `temple_of_the_feaster`);
  Tulzscha and Zstylzhemghi share `rift_scar` as the Veil-centre proxy (a real `the_veil`
  dimension is deferred). The named `ithaqual_shade` joins them as a roaming 21st presence from the
  polar edge/deep places (`still_the_shade`), and the Choir **Leviathan** is the drowned-temple
  boss (`still_the_leviathan`) — both live since `80d4fdc`. **World events** (`event/`, design/19): a bounded
  data-driven framework + whisper, darkness_pulse, rift_bloom, veil_thin, hallucination_wave,
  cleansing_dawn, cult_procession, blood_moon_rite, star_fall, hollow_call (`/eh event`, `/eh events`). **Codex** (`codex/`, design/22): a per-player LORE
  attachment + discovery of every site, city, boss, bestiary mob, rite and event (the entry set is
  generated from the registries, so it grows with content);
  `/eh codex`. **Recipes/economy** (design/16): 25 crafting/smelting recipes make the reagents,
  ritual blocks, tools, gear and utility items obtainable; every reagent has an in-game source.
- **Prologue — the Threshold** (`investigator/`, `world/threshold/`, design/29): a one-time onboarding.
  A new player is teleported to the `eldritch_horror:threshold` dimension (flat, dark, no
  sanity/corruption/event tickers — they gate on `minecraft:overworld`), stamped deterministically
  at `ServerStarted` (idempotent, forceload -> place -> release). **12 named investigator pedestals**
  (inner ring, each with a name+role sign; preview-then-confirm) grant a permanent investigator with
  a starter kit, starting rite and passive bonuses, plus their own signature item active ability;
  24 waystation arch portals (`city=0..23`, walked into, with city signs) teleport to the chosen
  curated city's safe surface and end the prologue (one-way, no return). The hub is
  destruction-proof (`ThresholdProtection`). Passives scale drain/gain via `Progression` in the
  sanity/corruption tickers. `/eh investigator get|set` + `ENABLE_PROLOGUE`.
- **Client world map** (`client/WorldMapScreen`): right-clicking the `world_atlas` opens a
  fullscreen Earth map — the baked landmask (`textures/gui/world_map.png`, 2048×1024) scaled to the
  world extent, with markers for the player, the home city, every curated city, and codex-discovered
  sites. Opened from a `Dist.CLIENT` `RightClickItem` handler by item id, so no GUI class is loaded
  server-side. **Event notifications**: a chat line names each world event when it starts/ends
  (`ENABLE_EVENT_NOTIFICATIONS`), so a player knows what is happening, not just that it is.
- **Startup fixes / deps**: config type `SERVER`→`SYNCED` (NeoForge `.37` renamed the FML config
  types); dependencies bumped to NeoForge `26.3.0.48-beta`, moddev `2.0.148`, JUnit `5.14.4`,
  Gradle `9.8.0`. City arrival now samples a ring of street-level spots and lands on the modal
  height (never on the central landmark — e.g. Rio's statue). Rio's floating halo lanterns removed.
- **Code-review fixes**: the atlas opens via `Item#use` through a dist-safe `ClientHooks` slot (so
  it works while pointing at a block, not only at air); the map screen no longer double-draws the
  background; `/eh event` requires `ENABLE_EVENTS` (a forced event with the master switch off would
  never advance); event/investigator lang keys added and the phantom
  `investigator.eldritch_horror.active.*` key removed (ability messages carry live data — bearing,
  rite name — so they stay literals); dead `InvestigatorRole.id()` removed.
- **Deferred work finished**: rite **offerings are now required and consumed** (all-or-nothing,
  per design/08: the eight seed rites' reagent costs plus a default for the Ancient One solves;
  `REQUIRE_RITE_REAGENTS` gates it; the altar lists each known rite's offerings). **Rite conditions**
  are enforced too (`RiteConditions`): time/moon/weather/near-water/corruption-stage/rift, checked
  before the outcome, refusing with a message and charging the meter cost (not the offerings);
  `REQUIRE_RITE_CONDITIONS` gates it. The investigator
  actives are now **twelve per-investigator signature items** (`seers_lens`, `rosary`, …) instead of
  one shared charm. **Investigator passives are live**: Dr. Amos recovers +50% in cities, Aldous
  finds bonus mob loot. **City state** (`CityState`: Thriving→Uneasy→Besieged→Fallen) is derived from
  the district's taint field (`CityStates`, loaded chunks only) and scales city shelter; `/eh city`.
  Removed the dead `CorruptionAPI.state`, `TaintAPI.getAt`, `CodexAPI.entry`, `SanitySystem.max()`
  nits (`EventTicker.activeCount` is live).
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
- **Deprecated `appendHoverText` removed.** Consumable tooltips now go through NeoForge's
  `RegisterTooltipAppendersEvent` (mod bus) → one `TooltipAppenders` appender at
  `TooltipLocation.POST_CUSTOM`, matching the old lines. No vanilla `@Deprecated` hook remains.
- **Nullness: all 24 packages are `@NullMarked`** via `package-info.java`. Legal null returns are
  annotated `@Nullable` (registry lookups, `server.getLevel`, `getChunkNow`, `getAttribute`); the
  mixin package stays unmarked.

## Known limitations / deferred

- See **`docs/TODO.md`** for the ordered backlog. Open now: reputation decay / the cultist-kill
  penalty (`design/03c`), the third ending ("seal every rift", `design/11`), armour equipment
  assets and the remaining flavour items, and a **human visual pass on the `J`/`C`/`K` screens**
  (API-verified only). The item pass, the altar ring pattern, sound wiring and the Russian locale
  are done.
- Cities now have a derived **state** (Thriving→Fallen from district taint) that scales shelter;
  services remain vanilla villager trades, and city-state depth (fall visual/takeover) is on the
  backlog.
- **Implemented and playable:** bestiary (Ancient Ones + lesser/mundane/ambient), cults (definitions
  + synced reputation + ranks), rites (`RiteEngine` resolves wards/summons/rifts/cleansing), taint
  world-conversion, world events, codex and the recipe economy. Sanity and corruption have real
  tickers, states, effects, config and item interaction. Morok is implemented on both axes (mob
  effects + a sanity source).
- **Still deferred:** chest/site loot tables (Antiquarian currently improves mob drops only);
  a few unused public-API nits from review.
- Some city **palettes remain close** (Paris/Buenos Aires, Jakarta/Los Angeles, London/Moscow);
  now largely distinguished by their differing landmarks, but colour separation is still loose.
- Terrain decoration is **light** (1% trees) and sub-pixel on the 8 blocks/px review renders,
  so it is judged in-game rather than on the map PNGs.
- Spawn is decided by the prologue (the Threshold city gate), not vanilla; a player who skips the
  prologue falls back to the vanilla spawn search. Arrival picks a street ring around the city heart
  (never the landmark) and sets it as the home respawn.
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

## Server-side bot harness (`bot/`)

A headless **BotPlayer** (a real `ServerPlayer` with an offline UUID, registered in the player
list and ticked from `ServerTickEvent.Post`) plays the mod with no client, so every per-player
system sees it. `/eh bot …` exposes the actions a driver needs: `spawnjoin`, `walk` (surface-
snapping travel), `find`/`findentity`, `mine`/`mineat` (drops are vacuumed into the inventory),
`craft` (recipe-driven: rebuilds each recipe's pattern from `PlacementInfo` and tries widths 1–3),
`smelt`, `placeat`, `equip`, `use`/`useitem`/`useat`, `attack`/`attacktype`, `rift`, `sense`. All
gated behind `-Deh.dev`/`EH_DEV` and gamemaster.

The driver (`mcbot/`, **outside this repo**) reads `sense` and issues those commands; it
completes the whole questline autonomously: prologue → Threshold investigator → city gate →
overworld → gather/craft the altar → `ward_of_the_eye` → `soothe_cthulhu` → `open_rift` +
`close_rift`. Verified with **every quest 1/1** (`first_steps`, `first_rite`, `lorekeeper`,
`seal_the_way`, `soothe_a_presence`) and **0 server exceptions**. Two flaky spots found on a clean
world are fixed there: a summoned presence needs a couple of seconds before the solve query can see
it (poll instead of a 0.6 s wait), and boss fights drift the bot off the altar before sealing (walk
back to the remembered core before the rite).

The starter kit seeds ritual reagents (`chalk_reagent`, `salt_reagent`, the rift/solve tomes,
`void_reagent`, `blood_offering`, an `offering_bowl` and stone) so the altar is reachable without
grinding rare night mobs for `minecraft:bone` — a bone/chalk accessibility gap found while testing.

## RPG layer — current seams (found by playing)

Playtesting the questline surfaced four weak spots; all four are now wired:

- **Currencies were inert.** `black_obol`/`cult_token`/… had no sink. `CultService` now has a paid
  `TRADE` kind (rank-gated, all-or-nothing, drops when the pack is full) and the cults sell ritual
  reagents (`buy_chalk`, `buy_salt`, `buy_bone`) plus the Hollow Choir's litanies.
- **Bone was a dead end.** Vanilla mobs are suppressed, so `minecraft:bone` came only from rare mod
  undead. `bone_reagent` now grinds into `bone_meal` (1 → 3), feeding `chalk_reagent` from the mod's
  own drops.
- **Two more skills with real effects:** `iron_will` (+15% max sanity, stacks with `lucid_mind`) and
  `frugal_rites` (25% of successful rites consume no offerings — hooked in `RiteEngine`).
- **Quests paid only a point.** Completion now also pays an item (obols / relic coin / order scrip /
  mark of favour) with its own message.

The altar gate is a **real ring** now (`0a28711`): the nearest `altar_core` must have at least
`riteAltarMarks` rune/chalk marks that stand **off** it (≥ 2 blocks) and **surround** it (the
largest angular gap between neighbouring marks ≤ 180°), so a pile of marks on one side is refused.
Rotation-aware by construction. Bot-verified live: the ring passes the gate, the pile is refused.

## RPG completion pass — outcomes & reputation (design/08, 03c, 09)

The rite engine now resolves the whole outcome vocabulary and pays the design's costs exactly:

- **Success applies the Outcome column**: `ward_of_the_eye` corruption −3, `summon_star_spawn` +12,
  `open_rift` +15, `close_rift` sanity +10, `rite_of_cleansing` −20, plus the signature-rite
  reputation of design/09 (`drowned_blessing` / `ward_of_the_eye` / `summon_star_spawn` +10 with
  their cults, crossing the opposed pairs).
- **Failure applies the Cost-on-failure column** (an unmet condition or an outcome that could not
  resolve); a rite that never starts (no altar, missing offerings, cooldown) costs nothing.
  `respec` is the design's skill reset + 80% refund instead of a heal.
- **`transform` and `curse` are live**: two Hollow Choir rites (`blight_the_earth`,
  `take_the_hollow_mark`; design/08 additions) taint the ground and lay a lasting weakness in
  exchange for standing.
- **Reputation sources** (design/03c): signature rites, quest rewards (new
  `reward_rep_cult` / `reward_rep_amount` keys in the quest datapack — `lorekeeper`,
  `open_the_way`, `seal_the_way` use them) and demanded offerings: a new `OFFER` cult service
  trades one item for +5 standing, open from Neutral, so a rank can be built from zero.

Verified in game: failure `open_rift` at low corruption cost sanity −25 / corruption +10 and paid no
gains; `ward_of_the_eye` success gave corruption −3 and unblinking_eye +10; `blight_the_earth`
tainted +0.30 and +4 corruption; `take_the_hollow_mark` applied weakness, +5 corruption and +10
standing; `offer_pages` traded a page for +5; `respec` released two skills and returned 1 of 2
points (80%).

**Learning a rite is a codex discovery.** Tome reading, cult teachings, investigator starting rites
and Forbidden Insight now route through `CodexAPI.learn`, so every learned rite appears in the codex
and advances the journal's `lorekeeper` quest. Before this, a fresh-world bot learned the kit's tomes
but could not reach `lorekeeper` 10/10 — which is what carries the Order to Initiate and completes
`sworn_ally`. A clean-world run now completes `first_steps`, `first_rite`, `lorekeeper 10/10`,
`open_the_way`, `seal_the_way`, `soothe_a_presence` and `sworn_ally` with 0 exceptions.

## The endgame is real (design/11, design/18)

- **`endgame/EndgameState`** — the per-world decision as `SavedData` (lenient codec);
  `/eh endgame` reads it, `/eh endgame set <none|banished|summoned>` repairs it.
- **`entity/TheHorror`** — invulnerable presence (design/18: "sanity is the real health bar"),
  four stages on an age clock: attention (NoAI, nausea pulses) → approach (AI, taint, capped lesser
  swarms) → grasp (star-spawn, peak aura) → decision (the fork). The aura scales with the stage.
  The clock lives in `tick()` on purpose: attention sets `NoAI`, which stops `customServerAiStep`,
  and a clock there froze the encounter at age 20 (found by playtesting).
- **Three rites** (`rite/Rites.java`, tier 3, taught by the cults at Inner Circle): 
  `awaken_the_horror` (rift within 8 + Claimed), `banish_the_horror` (Order) and
  `summon_the_horror` (Hollow, Devoted).
- **Banish** — closes the loaded rifts, recedes the taint, lifts a summon's floor, resets a large
  part of corruption, costs a skill point and a quarter of every cult's standing, grants
  `banishment_focus`.
- **Summon** — pins the corruption **floor** at the Claimed band (a floor on the meter itself, so
  every cleanse path is refused by the system, no per-path special case), raises Hollow standing,
  tears open rifts, grants `hollow_crown`.
- **Journal**: `awaken_the_horror`, `the_decision` (14 quests load now).

Verified in game: the Horror wakes at a rift and drains sanity; its clock runs through attention
(`NoAI=1b`, age 29) into approach (age 653, AI on); banish closed 5 rifts and set
`endgame = banished` (corruption 100 → 41.93, token granted); summon set `endgame = summoned`,
granted the crown and clamped a `corruption set 5` back to 85 (Claimed); both journal quests
completed. 0 exceptions, PMD 0 violations.

Still open from design/11: the possible third outcome (seal every rift) and whether the endgame is
per-player or per-world in multiplayer (it is per-world today).

## Quests are data, not code

The journal lives in `data/eldritch_horror/quest/*.json`, scanned on every server data load (and
`/reload`) by `quest/QuestReload` — a vanilla `SimpleJsonResourceReloadListener` registered on
NeoForged's `AddServerReloadListenersEvent`. Schema:

```json
{ "id": "ritualist", "target": 10, "stage": 3, "requires": "first_rite",
  "reward_item": "black_obol", "reward_count": 3 }
```

`id`/`target`/`stage` are required; `requires` and the reward pair are optional. Name and description
stay lang keys (`quest.eldritch_horror.<id>` / `.desc`). Adding a quest = one JSON file plus its two
lang keys; if it watches a system with no emission point yet, add one `QuestJournal.add` call there.
`/eh quests` lists the acts in stage order and names the next objective. Twelve quests ship across
seven acts.

## Client screens (design/13)

The three information screens ship: **quest journal** (`J`), **codex** (`C`) and **skill tree**
(`K`), alongside the world map (`M`). They read the already-synced attachments (QUESTS, SKILLS,
SKILL_POINTS, LORE, RITE_KNOWLEDGE) and the common Java registries, so nothing new needed syncing
except the quest **definitions** (`network/QuestSyncPayload`, shipped on `OnDatapackSyncEvent`,
which covers both a joining player and a `/reload`; `client/ClientQuests` mirrors them and is
dropped on logout). Unlocking a skill from the screen sends `SkillUnlockPayload`, re-validated
server-side through `SkillTree.unlock`.

Dist safety: the common `network/ModPayloads` registers the clientbound type **without** a handler;
the handler is attached in `Dist.CLIENT`-only `client/ClientPayloadHandlers` through NeoForge's
`RegisterClientPayloadHandlersEvent` (a dedicated server never resolves a client class — scanned:
zero `eldritchhorror.client` imports outside `client/`).

Caveat: there is no client in the dev environment, so the screens are compile- and
API-verified (26.3 `extractRenderState`, key mappings, payload signatures were javap-checked),
not visually. They need one human pass in a real client. The UI commit also proved why the
bounded smoke test is mandatory: it compiled and passed PMD, then died at startup on a payload id
(`createType` prefixes `minecraft:`) — fixed in the follow-up commit.

## Generated assets (`tools/gen_assets.py`)

Pillow, host-side, deterministic (one seed per file) and palette-driven, so the whole look retunes
in one place. `python3 tools/gen_assets.py` writes:

- `logo.png` / `pack.png` — the mod sigil (void disc, chalk ring, eight tentacles, one eye);
  `logoFile` is wired in `src/main/templates/META-INF/neoforge.mods.toml`.
- An organic "void flesh" skin for **every** entity texture (62), in each entity's palette and at
  the size of the placeholder it replaces. Before: flat dither/noise, seven duplicated groups.
  Regenerate after adding a new entity texture.
- Missing item icons with their item definition and model (`the_horror_spawn_egg` was the only item
  without one).

Programmer art, but a coherent family; hero assets still deserve a human.

## Generated sounds (`tools/gen_sounds.py`)

Host-side, deterministic, stdlib synthesis (no numpy); encoded to mono ogg with ffmpeg — the host
binary if present, otherwise the `linuxserver/ffmpeg` container (`DOCKER_HOST` must point at the
session's docker socket). CLI: `--list`, `--only NAME`, `--force`. **Fourteen** sounds ship
(~510 KiB): the ritual hum and the two outcome stings, the endgame fork (awaken / banish / summon),
the Horror's own ambient / hurt / death / stage change, and the whisper/dread ambience. **Wired**
through `ModSounds` + `sounds.json` (subtitles in both locales): the outcome stings fire for every
rite that started (success or a paid failure), the fork sounds in `Endgame`, the Horror's set and
the awakening horn in `TheHorror`, whisper at the MARKED band and dread when the corruption deepens.

## Docs for players

`README.md` now describes the playable alpha (systems, keys, `/eh`, screens) with the dev sections
kept; `CHANGELOG.md` summarises the snapshot; `docs/TODO.md` is the live backlog.

## The item pass (design/16)

The audit found 140 registered items: 39 live, 53 referenced elsewhere (loot, crafting, kits,
rewards, offerings, service costs), 48 inert. The effects the design/comments already recorded are
wired where a system exists:

- **Weapons/tools carry real stats** — wooden/iron/netherite swords, two maces, a trident, a bow,
  an iron tool; `star_iron_sword` keeps its `ward_breakers` tag.
- **The reagent row is usable** — `grave_dust`…`ritual_ash` apply their recorded sanity/corruption
  deltas and are consumed; `drowned_lung` is a reusable Water Breathing (corruption +2).
- **Carried artefacts** (`registry/items/CarriedItems`: no trinket slot, "carried" is anywhere in
  the inventory) — `ring_of_focus` −20% rite cooldown, `warden_sigil` −40% taint spread within 16
  blocks (`TaintSystem`), `eye_of_the_watcher` sanity −0.1/s + a bearing to the nearest known rift.
- **Navigation answers in chat** — `city_map`, `cartographers_lens`, `compass_of_longing`,
  `veil_compass`, `sextant`.
- **Still flavour** (documented in the code, no hook yet): armour (26.3 wants equipment assets),
  weapon on-hit effects, `prophet_mask`, `mirror_shard`, `compass_of_quiet`, `chalice_of_echoes`,
  `veil_lens`, `bone_charm`, `lantern_of_steadfast`, `wardstone_pendant`, `marked_map`,
  `route_ledger`. Tooltips state only what the item really does.

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
