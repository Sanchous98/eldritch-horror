# Code Review Report #2 (`f190219`)

Second full audit, run after the fix pass that resolved `CODE-REVIEW.md`
(`ebd394a..f190219`). Five area reviewers (items/combat, RPG/commands/bots,
world/transport, entities/city worldgen, client/data/tests/docs) read the whole
tree again, with two rules: do not re-report resolved findings, and actively
hunt for regressions introduced by the fixes. Minecraft API claims were checked
against the decompiled 26.3 sources (`build/moddev/artifacts/*-sources.jar`).
The lead re-verified every High/Medium finding against the source before it
entered this list.

**Result: 32 findings — 1 High, 4 Medium, 17 Low, 10 Nit.** The previous 27
resolutions held up: no fix was found wrong, and #22 (`getMaxY()` inclusive)
remains invalid.

---

## High

### 1. Hydra and Yig escalation is dead code — long overflow at `Long.MIN_VALUE`
- **Files:** `entity/Hydra.java:41,72-76`, `entity/Yig.java:41,72-76`
- **Problem:** `lastEscalation` starts at `Long.MIN_VALUE` and the gate is
  `level.getGameTime() - this.lastEscalation >= ESCALATION_COOLDOWN_TICKS`.
  `gameTime - Long.MIN_VALUE` overflows to a huge *negative* value, the guard is
  false on every hit, and `lastEscalation` is assigned only inside the branch —
  it can never recover. Hydra never buds a `ShamblerOoze` head and Yig never
  draws a `RisenHusk`: the signature "cut it and it grows" mechanic never fires
  once (and with `*_KILLABLE=false` they are plain invulnerable melee bags).
- **Fix:** initialise to `-ESCALATION_COOLDOWN_TICKS` (both files), or guard with
  `lastEscalation == Long.MIN_VALUE ||`. Pin the cooldown arithmetic with a JUnit
  test.

## Medium

### 2. `eh bot god` grants creative mode — outside the honesty allow-list
- **Files:** `bot/BotCommands.java:119-122,955-970`
- **Problem:** `bot god <name> true` sets `GameType.CREATIVE`. Creative is on the
  forbidden-equivalents list and the allow-list (invulnerable, travel teleports,
  time/weather, small forceload windows) does not cover it: while on, the bot
  skips every consumption/durability/placement cost, silently invalidating an
  "honest playtest" that flips it (or forgets `false`).
- **Fix:** delete the subcommand (`bot invulnerable` already covers the
  documented need), or hard-refuse it once a bot has joined a run.

### 3. `eh bot attacktype` strikes from 32 blocks — no vanilla reach enforcement
- **Files:** `bot/BotCommands.java:342-357,973-1003`
- **Problem:** target selection uses `inflate(32.0)`; vanilla range is enforced
  only in the packet listener (`isWithinEntityInteractionRange(..., 3.0)`),
  and `Player.attack` itself has no check (verified in the decompiled sources) —
  the bot deals full melee damage (cooldown-scaled, durability-paying) from up
  to 32 blocks. The `attacktype` comment even claims a teleport that does not
  exist.
- **Fix:** cap selection at ~3 blocks or teleport the bot adjacent before the
  swing; drop the stale comment.

### 4. Rail runtime movement math is not seam-wrapped — the #5 fix stopped at the router
- **Files:** `world/rail/RailSampler.java:53-56,96-110`, `world/rail/TrainTrail.java:80-84`
- **Problem:** `distance()`, `sample()` interpolation and `yaw()` still use raw
  `dx = b.x() - a.x()`. A seam-crossing line has adjacent nodes 131,071 blocks
  apart raw, so `RailLine.length()` inflates by that amount, the locomotive
  sweeps backward across the whole world on the crossing segment, and
  `nodeAt` obstruction checks index past it. Latent today (nearest curated city
  is ~10.5k from the seam; `trainMaxLineBlocks` is configurable to 60,000).
- **Fix:** `dx = EarthMap.wrapDelta(from.x(), to.x())` in distance/interp/yaw;
  interpolate unwrapped and `wrapBlockX` the result; same for `TrainTrail`.

### 5. `RailLines` stamp/shape direction flips at the seam
- **Files:** `world/rail/RailLines.java:369-370,431-435`
- **Problem:** `addLineStamps`/`shape` compute `Integer.signum(next.x() -
  current.x())` raw. At a seam node (65535 → -65536) the sign flips: a corner at
  the crossing renders `NORTH_WEST` instead of `NORTH_EAST`, an ascending run
  gets `ASCENDING_WEST` instead of `ASCENDING_EAST` — a broken rail connection.
- **Fix:** `signum(EarthMap.wrapDelta(current.x(), next.x()))` for both deltas.

## Low

### 6. `eh bot useat`/`mineat` accept arbitrary coordinates — no distance gate
- **Files:** `bot/BotCommands.java:290-308,397-419`
- **Problem:** `ServerPlayerGameMode.useItemOn/destroyBlock` contain no range
  validation (verified), so a bot remotely activates altars/gates or insta-breaks
  any block cross-chunk. Durability and honest drops are paid; reach and break
  time are free.
- **Fix:** require the target within ~5 blocks (or walk the bot there first).

### 7. `eh bot smelt` conjures smelting results without a furnace or fuel
- **Files:** `bot/BotCommands.java:505-550`
- **Problem:** input is consumed but the result appears directly in the
  inventory — no furnace, no fuel, no smelt time. Free ore→ingot is a real
  survival-cost skip.
- **Fix:** require a lit furnace/campfire nearby and charge fuel, or mark the
  subcommand probe-only and exclude it from honest runs.

### 8. `eh bot placeat` overwrites blocks and bypasses survival placement rules
- **Files:** `bot/BotCommands.java:466-503`
- **Problem:** consumes one item (good) but then `setBlockToUpdate` with
  `defaultBlockState()`: can overwrite existing blocks (chest contents vanish),
  ignores `canBeReplaced`/`canSurvive`, drops orientation.
- **Fix:** reject unless the target state `canBeReplaced()`; route through
  `gameMode.useItemOn` so the block item resolves its own state and checks.

### 9. `banish` raises negative cult standings toward zero — the price rewards the banisher
- **Files:** `endgame/Endgame.java:95-98`
- **Problem:** the "pays 25% of reputation" is `current - round(current * 0.25)`;
  for negative `current` (e.g. −60 → −45) hostility *decreases*. Positive
  standings are cut correctly; the sign is unhandled.
- **Fix:** skip non-positive standings, or move them further negative — a
  banish must never raise a standing.

### 10. Spawner passes do not skip creative/spectator players
- **Files:** `entity/BestiarySupport.java:68` and callers (`AncientOneSpawner.java:72`,
  `AmbientSpawner`, `PassiveSpawner`, `SitePopulationSpawner`)
- **Problem:** `tickAura` explicitly skips creative/spectator players, but the
  `topUp*` family iterates `level.players()` unfiltered — horrors, including
  roaming Ancient Ones, spawn around creative builders and spectating admins.
- **Fix:** early-return in `topUp*` (or the spawner loops) on
  `isCreative() || isSpectator()`.

### 11. Market stalls are truncated when they straddle a chunk border
- **Files:** `world/loc/city/CityLocation.java:584` (`streetDetails`/`marketStall`)
- **Problem:** the stall anchor draws its extra rng only where
  `isReplaceable(x, ground+1, z)` holds, which is false outside the generating
  chunk; the neighbouring chunk that owns 1–2 columns of the 3×3 footprint skips
  the stall entirely and `Builder.put` clips the overhang — seam stalls lose
  posts/awning cells. (Crates/wells are single-column and safe.)
- **Fix:** gate on a chunk-independent probe (pavement/land) and let writes
  clip, so every chunk draws the identical stall sequence; or keep stalls off
  `x % 16`-unsafe cells.

### 12. `seamWarp` fights vehicle seat logic for passengers near the seam
- **Files:** `world/BoundaryTravel.java:60-75`
- **Problem:** it teleports *every* player; a passenger whose seat offset pushes
  them past ±65536 is teleported 131,072 blocks away every 10 ticks while
  `positionRider` snaps them back — a desync fight. Ships/trains wrap
  themselves.
- **Fix:** skip riding players (`isPassenger()`), or warp the vehicle root.

### 13. `RailRouteFinder.pathLength` wraps with a hard-coded grid width
- **Files:** `world/rail/RailRouteFinder.java:26,169-187`
- **Problem:** `CYLINDER_CELLS = 2048` matches the production grid, but every
  test grid (width 8/20/32) disagrees — a seam-crossing smoothed pair is charged
  a half-world in tests.
- **Fix:** pass the grid (or its width) like every other wrap site.

### 14. Unwrapped-distance family: port/station selection, city decoration, town queries
- **Files:** `transport/Port.java:17-21`, `rail/RailStation.java:26-30`,
  `SurfaceDecorator.java:157-165` (`inCity`), `CityPopulation.java:125-130`
  (`near`), `town/Towns.java:165`, `town/TownIndex.java:99,123`
- **Problem:** raw `distanceSqr` at all six sites: `/eh ship`/`train` nearest
  picks, printed distances, decoration inside seam-adjacent districts, and
  town proximity are wrong for callers within a half-world of the seam.
  Currently unexercised (the only seam land is Antarctica), but these are live
  gameplay paths.
- **Fix:** one `EarthMap.wrapDelta` per site.

### 15. `EventTicker` static maps are not cleared by `ServerStateReset` (incomplete fix of #10)
- **Files:** `event/EventTicker.java:82-83` vs `core/ServerStateReset.java:47-59`
- **Problem:** `ACTIVE`/`COOLDOWN` are exactly the class of per-world statics
  the reset was built for, but are only dropped on logout. In the same JVM a
  new world restarts `getTickCount()` at 0, so stale absolute stamps make
  `tick < ready` true forever — mod events never trigger again.
- **Fix:** add `EventTicker.clear()` and call it from `clearAll()`.

### 16. `TrainService` static maps are not cleared on world stop
- **Files:** `world/rail/TrainService.java:49-57` vs `core/ServerStateReset.java`
- **Problem:** `ACTIVE/PENDING/PENDING_AGE/LAST_TICK/STUCK` clear only on
  `ServerStarted`, and `clearAll()` lists `ShipService` but not `TrainService`;
  between worlds in one JVM they hold strong refs to the old level's entities.
- **Fix:** add `TrainService.clear()` (all five maps) and register it.

### 17. `SanityTicker`/`CorruptionTicker` `PREVIOUS` maps survive world switches
- **Files:** `sanity/SanityTicker.java:43`, `corruption/CorruptionTicker.java:43`
- **Problem:** cleared on logout, but `Bots.clear()` on `ServerStoppedEvent`
  bypasses `PlayerLoggedOutEvent`, so a same-named bot (deterministic offline
  UUID) inherits a stale band in the next world — worst case one spurious
  dread cue on its first tick.
- **Fix:** add both to `clearAll()`.

### 18. `EarthMaps.getOrNull` never returns null — the contract is a lie
- **Files:** `world/EarthMaps.java:22-39`
- **Problem:** the javadoc promises "never throws; null if missing", but the
  body throws `IllegalStateException` on `IOException`; every caller's
  `== null` branch is dead code.
- **Fix:** return null after logging, or fix the doc and rename.

### 19. Tempering cancels only server-side — the client mispredicts the gesture
- **Files:** `event/WeaponQualityEvents.java:41`
- **Problem:** the handler returns for non-`ServerPlayer`, so with weapon in
  main hand + whetstone in off hand the client still runs its local use
  prediction: `VeilBowItem` starts the draw animation (release looses nothing),
  guns predict a shot.
- **Fix:** mirror the `setCanceled` on the client path (without the roll) so
  the client sees the click consumed.

### 20. Rail candidate search allocates ~33 MB transient per attempt
- **Files:** `world/rail/RailLines.java:170-205` + `RailRouteFinder.find`
- **Problem:** each candidate search allocates 4 arrays over the full
  2048×1024 board, up to `PARTNER_ATTEMPTS`(5) × 24 cities at network build.
  One-time, logged and bounded, but a hoisted scratch-buffer set (or arrays
  sized to the `maxLengthCells` window) cuts startup churn several-fold.
- **Fix:** reuse one scratch set per `RailLines` build.

### 21. `STATUS.md` test counts are 2.4× understated
- **Files:** `docs/STATUS.md:105` (vs line 641)
- **Problem:** "Twelve suites, 77 cases" vs the tree's 29 suites / 166
  `@Test` methods; the doc is the post-compaction source of truth.
- **Fix:** "29 suites, 166 cases", or drop counts in favour of
  `./gradlew test`.

### 22. The sea-side seam fix has no regression test
- **Files:** `transport/SeaPoint.java:16` (vs `RailPathMathTest` seam case)
- **Problem:** `SeaPoint` is a pure, Minecraft-free record; the exact bug
  class that regressed once (unwrapped `distanceTo`) is untested while the rail
  side has a seam case.
- **Fix:** a 3-line `assertEquals(10, new SeaPoint(half-5, 0)
  .distanceTo(new SeaPoint(-half+5, 0)), ...)` in `SeaRouteFinderTest`.

## Nit

### 23. Default bot starter kit is a material cheat by the allow-list's standard
- **Files:** `bot/Bots.java:147-167`
- **Problem:** iron gear, 48 chalk, 32 salt, three endgame-capable tomes, 14
  offerings, 48 stone — `spawnjoinbare` is the honest path, but nothing marks
  non-bare runs as unrepresentative.
- **Fix:** make the kit opt-in, or emit a loud log line when a kit bot earns
  progression.

### 24. `/eh` gating style is split — `.requires` vs in-handler
- **Files:** `commands/EldritchCommands.java:75-77` vs `510-514,621-626,657-662,915-924`
- **Problem:** both are secure, but non-GM players see half the mutating tree in
  tab-complete. Standardise on one idiom.

### 25. Tempering is silently dead during a firearm cooldown
- **Files:** `event/WeaponQualityEvents.java` (vs decompiled `useItem:358-361`)
- **Problem:** `useItem` returns `PASS` on `isOnCooldown` *before* posting
  `RightClickItem`, so a whetstone attempt inside e.g. the carbine's 36-tick
  window consumes nothing and gives no feedback.
- **Fix:** document the rule in the Javadoc, or move the temper hook for guns
  into `FirearmItem.use`.

### 26. `WeaponGrade` javadoc contradicts the colour contract
- **Files:** `registry/items/WeaponGrade.java:9-10`
- **Problem:** says the grade "sets the item name's colour"; per design/16 and
  the implementation, grade is colour-neutral and quality owns rarity. Reword
  before someone "fixes" the code toward the doc.

### 27. design/16 still promises quality for throwables
- **Files:** `design/16-items.md:216`
- **Problem:** after fix #2, dynamite/tear-gas are grade-only (untemperable, no
  quality line); the design sentence is now half-true. Update it.

### 28. `ProphetMaskItem` runs its villager scan once per carried copy
- **Files:** `registry/items/ProphetMaskItem.java:42-56`
- **Problem:** unlike Eye/Mirror/Lens it has no `isFirstCarried` guard; N masks
  run the `getEntitiesOfClass` panic scan N times per 10 ticks (idempotent, so
  it is cost, not corruption).
- **Fix:** guard it the same way.

### 29. `WailHuskSpawner` javadoc points at the pre-fix LOS location
- **Files:** `entity/WailHuskSpawner.java:22`
- **Problem:** the LOS gate now lives in `auraRequiresLineOfSight()` and is
  enforced in `BestiarySupport.tickAura`; the stale comment invites re-adding
  the check in `auraRateFor` (double-counting).

### 30. `UiKeyHandler` derefs two of three nullable key fields unguarded
- **Files:** `client/UiKeyHandler.java:26-33`
- **Problem:** safe today (all three assigned together in one registration
  event), but the invariant is implicit and one refactor away from an NPE.
- **Fix:** guard all three or expose `MapKeyMappings.ready()`.

### 31. `gradle.properties` cites the pre-3g cgroup budget
- **Files:** `gradle.properties:2`
- **Problem:** the `-Xmx2G` comment says "the dev container's cgroup is only
  6 GiB"; the compose default is now `mem_limit: 3g`.
- **Fix:** reword to "capped by `DEV_MEM` (3 GiB by default)".

### 32. Small hygiene: orphaned javadoc, unguarded dev flag, dead store
- **Files:** `world/ModWorldGenEvents.java:83-96,106`,
  `world/SurfaceDecorator.java:67`
- **Problem:** the `renderCities` javadoc block sits orphaned above
  `maybeForceload` (so `maybeRenderCities` has none); `-Deh.forceload`
  `parseInt` is unguarded inside the `ServerStarted` handler (dev-only); and
  `pos.set(x, surfaceY + 1, z)` is dead — line 68 re-sets `pos` before any read.

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
