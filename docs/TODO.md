# TODO / backlog

Working list for finishing the mod. Ordered by priority. Each item states the design source, the
concrete gap, and a definition of done. When an item lands, move it to **Done** with its commit.

Status legend: `[ ]` open · `[~]` in progress · `[x]` done.

## Open

- [ ] **Hollow Choir Inner Circle is a long blood farm (playtest)** — `awaken_the_horror` needs
  INNER_CIRCLE (80) and the cult accepts only `blood_offering` (5 rep each): 16 blood for the rank
  plus 4 + 4 for `open_rift`/`awaken_the_horror`, 28 in total. Chaugnar Faugn is the only renewable
  source, and its loot pool rolls **one of** blood 2–4 / bone 1–3 / grave dust, so the expectation
  is ~1 blood per kill, not 2–4 per kill (~28 kills). That is farmable but long; decide whether the
  intended pace is a multi-hour Chaugnar grind or whether a second offering (e.g. `fragment_page`
  for the Order's Inner Circle, or a tome that teaches `awaken_the_horror`) should exist.
- [ ] **Dev-server OOM under a long bot run** — two playtests were cut by cgroup OOM kills (8 GiB
  cgroup, 5 GiB container cap, `-Xmx3g`); the server restarts itself and bots are session-scoped.
  Bound worldgen/forceload churn for long runs or raise the container budget.
- [ ] **Headless-bot playtests must force-load the target's chunk** — a bot never sends move
  packets, so it gets no vanilla player chunk ticket. A chunk that is loaded but not *simulating*
  freezes its mobs: AI, position and `HurtTime` stop, the mob takes the first point of damage and
  then ignores every later hit (this is what stalled the night-3 bone-choir kills at 9/10 HP).
  The night-3 driver now keeps at most two small `forceload` windows and re-centres one on every
  teleport destination before a swing; combat and boss farms work with that in place.
- [ ] **Visual pass on the new screens (design/13)** — Journal/Codex/Skills (`J`/`C`/`K`) are
  verified by API/compile only; needs a human client run.
- [ ] **Client-only item bits (design/16)** — the veil-lens corruption overlay and the waypoint
  markers on the world map stay commented in place; both are client rendering and need a human
  visual pass. (`ritual_knife` blood rites and `eye_scepter` rift reveal also remain flavour.)
- [ ] **`rift_scout` (design/10)** — the "once per rift" side quest needs a per-rift axis (which rift
  was scouted); daily/weekly repeatables and the hard `requires` gate are done.

## Done

- [x] **Investigator field kits are earned (design/10, 14)** — two unique data-driven tasks per
  investigator: the first pays the kit minus the signature item, the second the signature item;
  interruption-free journal/sidebar gating, generic pickup/kill/craft/rite/codex triggers, and the
  `reward_kit` table that keeps quests in sync with the roster.
- [x] **Scheduled rail transport (design/30 stage 2)** — five A*-routed lines between 24 stations,
  real ballast/sleepers/rails with bridges and tunnels, a locomotive plus coupled wagons, the ship's
  timetable/boarding/fare model, and a physical stop on a missing rail or a placed block; a full
  Beijing→Shanghai ride and the stopped/resumed cases are verified.
- [x] **Scheduled sea transport (design/30 stage 1)** — 17 harbour cities, A* sea lanes over the
  baked landmask, a large multi-seat packet that really sails on a daily in-game timetable, chunk
  tickets that follow the hull, fares, boarding windows, an empty-service despawn, and a full
  Bangkok→Jakarta ride verified — `90bc0a2`.
- [x] **NPC cult ritual cells + the doom clock (design/06, 11)** — the Hollow Choir gathers at an
  altar, chants and summons (star-spawn + taint) on its own; interrupting pays Order standing and
  buys time. The world's doom meter rises passively (~150 min), accelerates with open rifts, and
  ends in the summon at 100 % after the Horror awakens at 70 %; `/eh doom` reads it — `5492903`.
- [x] **Towns have people and shelter (design/21)** — each loaded near-player town tops up to five
  persistent villagers with vanilla trades, deterministic placement, a taint-abandon gate, and the
  footprint shelters at night with a small sanity recovery — `6ede27c`.
- [x] **Generic towns from the 3,065 non-curated settlements (design/21)** — `world/town` loads and
  filters them (96 overlap curated, 73 water), indexes 2,896 by 512-block cells and builds one
  deterministic 40–64-block town per chunk: plaza/well, 5–8 houses, lamps, farm, paths — `e0d37fb`.
- [x] **GameTest coverage (design/13)** — the new 26.3 data-driven suite (`test_instance/*.json` +
  `TEST_FUNCTION` registry entries) covers the altar ring, the ward ledger's coverage/expiry, gate-link
  symmetry/cleanup and the seal on an empty vs open ledger; `runGameTestServer` is 5/5 green and a
  broken assertion fails the build — `2eca24d`.
- [x] **Reclaiming a fallen city (design/21)** — the `reclaim_city` rite (Order, Devoted) only
  resolves inside a Fallen city: up to 256 loaded district chunks lose 65% of their taint (the state
  samples are cleaned first), up to 32 Choir worshippers are driven out, and `reclaim_the_city`
  completes; the refusals pay nothing — `dea8404`.
- [x] **Rift-gate travel (design/26, 23, 20)** — standing anchors link two-way with no UI (mark, then
  link; same gate clears), trips cost corruption +3 / sanity −8, arrival nausea plus a 25 %
  instability that can tear a new gate open; links are per-level SavedData and die with a broken
  anchor; the rules are pinned in `design/26` — `45ea35d`.
- [x] **Area wards (design/26, 18)** — `ward_of_the_eye` raises a persisted, capped haven on the altar
  core: negative sanity sources dropped (recovery kept), fresh lesser horrors refused, +25% to an
  Ancient One from a warded player; radius/lifetime/bonus in config, `/eh ward` reads it, and the
  dead `ward_token` grant is gone — `0275474`.
- [x] **Quest gate + repeatables (design/10)** — `requires` is now enforced server-side (a dangling id
  counts as unmet), quests can be `daily`/`weekly` with the stamp in a synced attachment, and the
  three side quests have real triggers (`star_hunt` kills, `tide_offering` pearl pickups,
  `cleansing_contract` rites) — `02915ac`.
- [x] **Weapon on-hit effects (design/16)** — `WeaponEffects` + `WeaponMath`: silver blade and
  star-iron sword target bonuses (data tags `#eldritch`, `#star_spawn`), the dagger's corruption
  scaling, the trident's underwater cut, the censer's paid ward, the pact blade's kill price and the
  veil bow's shot cost, eldritch bonus and piercing; lore lines updated in both locales — `73cdfc3`.
- [x] **The occultism skill tree (design/15)** — four real nodes with prerequisites:
  `forbidden_lore` (tome sanity −20%), `rite_affinity` (+10% ritual speed, multiplicative with the
  focus ring), `divination` (empty-hand inspection reports tags and, for a presence, its aura axis),
  `lucid_dreams` (a natural sleep can teach an unknown tier ≤ 2 rite; endgame fork excluded); the
  unlock gate and both new math helpers are unit-tested and the gate/tome were bot-probed —
  `a504d65`.
- [x] **Armour and the remaining artefacts (design/16, slice 2)** — the eight worn pieces are real
  armour (equipment assets + generated worn sheets, per-set stats); the bone charm saves one fatal
  blow, the lantern stills the darkness drain, the mask summons more star-spawn and lowers the
  sanity ceiling, the chalice echoes the last rite, the compass reads the safest city, the lens
  reads the local taint, the maps remember waypoints — `3bbe206`.
- [x] **Bots can take damage in 26.3** — `ServerPlayer#isInvulnerableTo` treated the bot's unloaded
  client and its prologue dimension-change flag as invulnerable, so every combat probe silently did
  nothing; the harness now reports the client loaded and clears the flag — `5d63b47`.

- [x] **The third ending — sealed (design/11)** — a world **rift ledger** (the open rite, the summon
  path, and the anchor block itself breaking tell it; `prune` heals stale entries) and
  `Endgame.trySeal` at the decision: an empty ledger seals the world, no cost is paid, and
  `the_seal` completes — `657cdc3`.
- [x] **Reputation decay & the cultist-kill penalty (design/03c)** — decay config-gated and off by
  default (one point per period of inactivity per cult, one-way to 0); killing a cultist costs
  exactly −40 with that cult — `a2523f6`.
- [x] **Item mechanics (design/16)** — weapon/tool stats, the reagent deltas, the four carried
  artefacts and five navigation items; the rest documented as flavour — `0be6ae6`.
- [x] **The sound pack is live (design/07)** — 13 mono ogg, `ModSounds` + `sounds.json` +
  subtitles; rite outcomes, the endgame fork, the Horror's own voice, whisper/dread — `7a70d48`.
- [x] **The altar pattern is a ring (design/05)** — the core's marks must stand off it and surround
  it (max angular gap ≤ 180°); a pile is refused; bot-verified — `0a28711`.
- [x] **Bestiary additions (design/25, 18)** — the five replacements, the Ithaqual Shade and the
  Choir Leviathan — `80d4fdc`.
- [x] **Russian locale** — `ru_ru` (853 keys incl. subtitles), independently reviewed — `a4c3dbc`,
  `92c3983`; parity is now gated by `LangParityTest` on every `./gradlew test`.
- [x] **Altar block-pattern gate (design/05)** — rite needs an `altar_core` marked with runes/chalk;
  the ritual site now builds a real altar — `10e0abb`.
- [x] **Fallen-city Hollow Choir takeover (design/21)** — bounded, capped, loaded-chunks only —
  `10e0abb`.
- [x] **Quest journal + skill tree (design/13)** — seed quests advanced by existing systems;
  two real skill nodes; `/eh quests`, `/eh skill` — `10e0abb`.
- [x] **Antiquarian bonus on site chests (design/16, design/14)** — the passive now applies to our
  `chests/*` tables too (global loot modifier) — `b8b773e`.
- [x] **Map markers: rifts (design/22)** — discovered rifts recorded and drawn; cult strongholds
  already showed as discovered sites — `da2882b`.
- [x] **Rite conditions (design/08)** — `d8632f6`.
- [x] **Prologue reset command (`/eh prologue reset`)** — `d8632f6`.
- [x] **Site chests + loot tables (design/16)** — `a6daaad`.
- [x] **Map keybind (design/22)** — `M` opens the map while the atlas is held — `b40cd01`.
- [x] **World map + event notifications** — `770c919` (fixed in `202240b`).
- [x] **Rite offerings consumed; per-investigator signature items; live passives (city recovery,
  mob loot); city state derived from taint; dead API nits removed** — `d3ce502`.
- [x] **Street-level city arrival; Rio halo fix; deps bump** — `08cf25a`.
- [x] **Code-review fixes** — `202240b`.
- [x] **`docs/TESTING.md`** — `abc399d`.
