# TODO / backlog

Working list for finishing the mod. Ordered by priority. Each item states the design source, the
concrete gap, and a definition of done. When an item lands, move it to **Done** with its commit.

Status legend: `[ ]` open · `[~]` in progress · `[x]` done.

## Open

- [~] **Item debt (design/16)** — audit of all 140 registered items: 39 live, 53 referenced
  elsewhere (loot, crafting, kits, rewards, offerings, service costs), 48 inert. Plan: wire the
  effects the design/comments already record — consume deltas on the reagent row
  (`grave_dust`…`ritual_ash`), weapon attributes (a new components pattern), the artefact passives
  with a named hook (`ring_of_focus` ritual speed, `warden_sigil` taint spread, `eye_of_the_watcher`,
  `drowned_lung`), navigation reveal items where the map-knowledge API allows; keep pure-flavour
  items flavour; armour stays stat-less until equipment assets exist.
- [~] **Altar block pattern: a real ring (design/05)** — `RitualAltar` now wants a core whose
  rune/chalk marks stand off it and *surround* it (largest angular gap ≤ 180°); the bot driver
  builds an 8-mark ring. Pending: an end-to-end rite run on a fresh world.
- [~] **Sound wiring** — `tools/gen_sounds.py` renders the pack (`ritual.*`, `endgame.*`,
  `entity.the_horror.*`, `ambient.whisper|dread`, 10 ogg, mono); pending: `sounds.json` entries,
  `ModSounds` registration, subtitles, and the play hooks (rites, endgame, the Horror — which still
  uses vanilla wither sounds).
- [ ] **Reputation decay & the cultist-kill penalty (design/03c)** — rep decay over time and −40 for
  killing a cultist of that faction.
- [ ] **Third ending: seal every rift (design/11)** — the banish path resolves at the finale;
  consider a "sealed world" outcome when every rift scar is closed.
- [ ] **Visual pass on the new screens (design/13)** — Journal/Codex/Skills (`J`/`C`/`K`) are
  verified by API/compile only; needs a human client run.

## Done

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
