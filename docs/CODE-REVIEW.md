# Code Review — open findings

Single live review of the project. Completed findings are removed from this file; the full
report and its resolution stay in git history. The first audit (27 findings) was resolved at
`ebd394a..f190219` and re-verified with no regressions (`847be76`).

Method: five area reviewers (items/combat, RPG/commands/bots, world/transport,
entities/city worldgen, client/data/tests/docs) re-read the whole tree at `f190219` with two
rules — do not re-report resolved findings, and hunt regressions from their fixes. Minecraft
API claims were checked against the decompiled 26.3 sources
(`build/moddev/artifacts/*-sources.jar`); the lead re-verified every High/Medium finding
against the source before it entered the list.

**Open: none.** The `f190219` audit found 32 findings (1 High, 4 Medium, 17 Low, 10 Nit);
all are fixed in `b26a045..797e9dd`, by area:

- **rail/transport** — seam-wrapped runtime movement math, grid-width `pathLength`, reusable
  A* buffers, `TrainService.clear()`, passenger-safe `seamWarp`, regression tests;
- **entities/statics** — Hydra/Yig escalation cooldown, creative/spectator spawner skip,
  `ProphetMask` once-per-copy guard, the four missing `ServerStateReset` owners;
- **world/city** — chunk-independent stall/crate rng, seam-aware port/station/town/city
  queries, honest `EarthMaps` contract, guarded dev flag;
- **bots/commands** — `bot god` removed, honest reach/`smelt`/`placeat`, opt-in starter kit,
  `banish` sign, `GAMEMASTER` requirements on mutating nodes;
- **items/client/docs** — mirrored client temper cancel, grade/quality doc contract,
  nullable key guards, refreshed build/test figures.

Verified green on the final tree: `./gradlew build` (`compileJava`, `test`, `pmdMain`,
`pmdTest`, `check`) and a clean server boot (`Done (4.136s)`).

---

## Verified OK (no action)

- **RPG core:** `RITE_READY` stamps after all pre-checks; `closeRift` fails at
  0 and grants +10 once; `consume` honours Frugal-Rites; `DoomClock` latches
  exactly once with no half/double spawn; all `SavedData` resolves to the
  overworld; ticker re-assertion is read-only, once per second, no spam;
  ModConfig never read at command-tree build.
- **Items:** the charge gate matches vanilla exactly (ticker reset moved to the
  end of `attack()` — scale still accurate inside `LivingDamageEvent.Pre`);
  whetstone/tome interactions verified against `ServerPlayerGameMode` sources —
  no free or double teach; artifact once-per-tick identity scan is correct
  against 26.3 `Inventory.tick`/`EntityEquipment` sources (offhand included);
  all 76 `set_count` modifiers and 47 recipes re-checked against
  `maxStackSize` — nothing over-stacks.
- **World:** `getBaseHeight` is per-type correct against the 26.3
  `Heightmap.Types` sources; `maybeRenderCities` is non-blocking with
  radius-0 tickets released on every exit path; `CultRituals.nearestCore` never
  loads chunks; ship/train watchdog maps release on every discard branch;
  mixins minimal with no client refs.
- **Entities/city:** `CityLocation` replay parity holds (rng consumers are
  `chest`/`scatter`/`ruins` only, drawn over full boxes — identical order in
  near and far chunks); wards cover every hostile mod `Monster` with
  `TaintedFauna`/`SporeBee` included and harmless entities excluded;
  `SporeBee` flight is NBT-safe and matches Byakhee/NightHag; the `setVillagerXp(1)`
  fix is effective against vanilla `ResetProfession`.
- **Client/data:** zero `@OnlyIn` and zero client leaks; 988/988 lang keys in
  both locales with `LangParityTest` gating parity; all JSON/recipes/textures
  resolve; no secrets; `.gitignore` covers bytecode; README matches compose.
