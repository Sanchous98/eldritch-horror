# TODO / backlog

Working list for finishing the mod. Ordered by priority. Each item states the design source, the
concrete gap, and a definition of done. When an item lands, move it to **Done** with its commit.

Status legend: `[ ]` open · `[~]` in progress · `[x]` done.

## Open

- [ ] **Visual pass on the new screens (design/13)** — Journal/Codex/Skills (`J`/`C`/`K`) are
  verified by API/compile only; needs a human client run.
- [ ] **Client-only item bits (design/16)** — the veil-lens corruption overlay and the waypoint
  markers on the world map stay commented in place; both are client rendering and need a human
  visual pass. (`ritual_knife` blood rites and `eye_scepter` rift reveal also remain flavour.)
- [ ] **`rift_scout` (design/10)** — the "once per rift" side quest needs a per-rift axis (which rift
  was scouted); daily/weekly repeatables and the hard `requires` gate are done.

## Done

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
