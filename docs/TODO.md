# TODO / backlog

Working list for finishing the mod. Ordered by priority. Each item states the design source, the
concrete gap, and a definition of done. When an item lands, move it to **Done** with its commit.

Status legend: `[ ]` open · `[~]` in progress · `[x]` done.

## Open

- [ ] **Visual pass on the new screens (design/13)** — Journal/Codex/Skills (`J`/`C`/`K`) are
  verified by API/compile only; needs a human client run.
- [ ] **Weapon on-hit effects and client-only bits (design/16)** — the recorded kill/water/ward/
  scaling effects on the weapons, the veil-lens corruption overlay and waypoint markers on the world
  map stay commented in place.

## Done

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
- [x] **The sound pack is live (design/07)** — 14 mono ogg, `ModSounds` + `sounds.json` +
  subtitles; rite outcomes, the endgame fork, the Horror's own voice, whisper/dread — `7a70d48`.
- [x] **The altar pattern is a ring (design/05)** — the core's marks must stand off it and surround
  it (max angular gap ≤ 180°); a pile is refused; bot-verified — `0a28711`.
- [x] **Bestiary additions (design/25, 18)** — the five replacements, the Ithaqual Shade and the
  Choir Leviathan — `80d4fdc`.
- [x] **Russian locale** — `ru_ru` (785 keys + subtitles), independently reviewed — `a4c3dbc`,
  `92c3983`.
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
