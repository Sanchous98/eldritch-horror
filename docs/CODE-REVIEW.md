# Code Review — open findings

Third audit of the project, done directly on the tree at `c27107a` (the 32-fix pass
`b26a045..797e9dd` is already in). The two earlier audits are fully resolved and stay in git
history. This file tracks **open work only**; findings are removed as they close.

Method: read the fix pass hunk by hunk, then re-check the highest-risk paths around it (bot
commands, rail/transport runtime, cylinder-seam queries, per-world statics). Minecraft API
claims were verified against the decompiled 26.3 sources
(`build/moddev/artifacts/minecraft-patched-*-sources.jar`), not guessed. No subagents were used.

**Open: 14 findings — 2 Medium, 6 Low, 6 Nit.**

---

## Medium

### 1. `eh bot attack` still reaches 5 blocks — the sibling fix stopped at `attacktype`
- **File:** `bot/BotCommands.java:338-355`
- **Problem:** `attacktype` was capped to `inflate(3.0)`, but `attack` still selects the nearest
  living entity within `inflate(5.0)` and calls `bot.attack(target)`. `Player.attack` has no
  server-side range check (verified: range lives only in the packet listener), so the bot deals
  full melee damage from 5 blocks — a different threshold from both vanilla (~3) and the fixed
  `attacktype`. The class Javadoc even says "within 5 blocks".
- **Fix:** use `3.0` here too (or teleport adjacent before the swing, as `attacktype`'s old
  comment wrongly claimed), and update the Javadoc.

### 2. `eh bot placeat` still accepts arbitrary coordinates — no reach gate
- **File:** `bot/BotCommands.java:476-540`
- **Problem:** `useat`/`mineat` got a 5-block guard, but `placeat` did not. It resolves any
  `BlockPos` from the command arguments and calls `gameMode.useItemOn`, which (verified in
  `ServerPlayerGameMode`) performs no range validation. A bot can place blocks across the world;
  the new `canBeReplaced` check only protects the target, not the reach.
- **Fix:** apply the same `distanceToSqr(Vec3.atCenterOf(pos)) > 25.0` refusal as `useat`/
  `mineat` (or walk the bot there first).

## Low

### 3. `eh bot smelt` only checks for *a* lit furnace — the cost is still not the bot's
- **File:** `bot/BotCommands.java:546-607`
- **Problem:** requiring any lit furnace within 5 blocks proves that *someone* burned fuel, not
  that this smelt paid for itself; the result still appears instantly and the furnace's input/
  output is untouched. A bot can use a player's furnace, or one lit for a different item, and skip
  the smelt time entirely. This is a partial fix of the original "no furnace" finding.
- **Fix:** either require the bot to insert the input into that furnace and take the result from
  its output (real time/fuel), or mark the subcommand probe-only and exclude it from honest runs.

### 4. `TrainLocomotive.seedTrail` still uses raw sample deltas across the seam
- **File:** `entity/TrainLocomotive.java:284-306`
- **Problem:** the rail fix wrapped `RailSampler`, but `seedTrail` takes samples at distance 0 and
  1 and computes `dx = ahead.x() - start.x()` raw. On a seam-crossing line the two samples are on
  opposite sides of ±65536, so the direction is reversed and every seeded wagon is placed
  ~131,072 blocks away. Latent today (no curated line at the seam), but it is the exact class the
  fix set out to close.
- **Fix:** `double dx = EarthMap.wrapDelta(start.x(), ahead.x());` (keep Z raw) and import
  `EarthMap`.

### 5. Ship/train dock and boarding proximity use raw entity distance
- **Files:** `world/transport/ShipService.java:183,232`,
  `world/rail/TrainService.java:221,276`
- **Problem:** `Port.distanceSqr`/`RailStation.distanceSqr` were wrapped, but the entity checks
  still call `ship.distanceToSqr(portX, y, portZ)` / `player.distanceToSqr(originX, y, originZ)`.
  A ship, train or player just across the seam is measured the long way, so docking is missed and
  the boarding call is silent. Latent (no port/station at the seam) but on live tick paths.
- **Fix:** a small helper that measures the horizontal delta with `EarthMap.wrapDelta` and keeps
  Y raw, used by both services.

### 6. `TownIndex` bucket lookup does not wrap the X cells
- **File:** `world/town/TownIndex.java:90-98,115-123,151-166`
- **Problem:** the distance filters were wrapped, but `near`/`forChunk` still compute
  `minCellX..maxCellX` with raw `cellOf` and `collect` iterates that rectangle. A query just
  inside the seam never visits the bucket on the other side, so the wrapped town is not even
  considered. The production cell (512) divides the cylinder exactly (256 cells), so this is a
  clean wrap; today there are no towns at the seam, hence Low.
- **Fix:** fold each cell X with `Math.floorMod(cell + cells / 2, cells) - cells / 2` (or an
  equivalent wrapped range) and deduplicate visits.

### 7. `RiftGates.PENDING` survives a world switch
- **File:** `world/rift/RiftGates.java:66`
- **Problem:** the per-player gate mark is a static map removed on logout and on link/travel, but
  `Bots.clear()` on `ServerStoppedEvent` bypasses `PlayerLoggedOutEvent`. A same-UUID player (the
  deterministic bot UUID) inherits a stale mark in the next world. The mark holds only a dimension
  key and a `BlockPos`, so it is a correctness wart rather than a level leak.
- **Fix:** add `RiftGates.clear()` (drop `PENDING`) and register it in `core/ServerStateReset`.

### 8. `CityRenderer.active` is not dropped on server stop
- **File:** `core/CityRenderer.java:152,317-352`
- **Problem:** the active render session owns `CityTask`s with `ServerLevel` references and is
  cleared only by `finish(...)`. If the server stops mid-render, `active` survives into the next
  world and `onTick` can advance stale tasks against the old level. Dev-only (`-Deh.renderCities`),
  but it is a real cross-world leak.
- **Fix:** subscribe to `ServerStoppedEvent` and set `active = null` (best-effort release the
  current task's tickets; the level unload drops the rest).

## Nit

### 9. Stale `CityLocation` comment about crate gating
- **File:** `world/loc/city/CityLocation.java:187-188`
- **Problem:** the comment says crates and wells are "gated on the cell being air/replaceable",
  but the crate path now checks only `isLand` (the replaceable test was deliberately removed to
  keep the rng chunk-independent). The comment invites a future re-add that would reintroduce the
  seam divergence.
- **Fix:** say "stalls and crates are gated on chunk-independent land; wells on a land box".

### 10. `attack` Javadoc and method share a line
- **File:** `bot/BotCommands.java:338`
- **Problem:** `/** ... */    private static int attack(...)` — the method declaration starts on
  the Javadoc's closing line. Cosmetic, but it breaks the file's formatting and hides the method
  in diffs.
- **Fix:** put the declaration on its own line.

### 11. `blockTarget` picks 5.0 blocks — slightly beyond vanilla block reach
- **File:** `bot/BotCommands.java:1084-1091`
- **Problem:** `bot use`/`bot mine` raycast 5.0 blocks; vanilla block interaction range is 4.5.
  The difference is small, but it is the same reach-bypass class as findings 1–2.
- **Fix:** use 4.5 (or the vanilla constant) and keep the `useat`/`mineat` 5-block guard as a
  command-argument tolerance.

### 12. `smelt`/`craft` ignore a failed `Inventory.add`
- **Files:** `bot/BotCommands.java:599,710`
- **Problem:** both consume inputs and then call `inv.add(result.copy())` without checking the
  boolean. A full inventory silently loses the crafted/smelted result.
- **Fix:** if `add` returns false, drop the result at the bot's feet (or refuse before consuming).

### 13. `EarthMaps.getOrNull` re-logs on every failed call
- **File:** `world/EarthMaps.java:30-37`
- **Problem:** the new null contract is correct, but `instance` stays null on failure, so every
  caller retries the load and logs the same error. A loop of `available()` checks can spam.
- **Fix:** cache the failure (a separate `loadFailed` flag or a sentinel) and log once per JVM.

### 14. No seam test for `RailSampler.sample` / `TrainTrail.sampleBehind`
- **Files:** `src/test/java/.../world/rail/RailPathMathTest.java`,
  `RailRouteFinderTest.java`
- **Problem:** the new tests pin `pathLength` and `SeaPoint.distanceTo`, but the exact runtime
  interpolation bug (finding 4's sibling) has no test: a two-node line `(65535, y, z)` →
  `(-65536, y, z)` should sample to a point one block across the seam, not 131k away.
- **Fix:** add that two-node case to `RailPathMathTest` (and a `TrainTrail` append/sample case).

---

## Verified OK (no action)

- **Fix pass:** `./gradlew build` green (`compileJava`, `test`, `pmdMain`, `pmdTest`, `check`)
  and a clean server boot (`Done (4.136s)`), on the exact tree reviewed here.
- **Tempering client mirror:** verified against the decompiled `MultiPlayerGameMode.useItem` —
  the `ServerboundUseItemPacket` is built and returned before `CommonHooks.onItemRightClick`, so
  cancelling the client prediction does not suppress the packet; the server still rolls.
- **`placeat` cost path:** `ServerPlayerGameMode.useItemOn` does run the block item's own
  placement/consumption; the loaned main-hand stack is restored and the source stack shrinks by
  what the use consumed (the only defect left is the missing reach gate).
- **Hydra/Yig:** the seeded `-ESCALATION_COOLDOWN_TICKS` makes the first hit pass the cooldown
  check; `lastEscalation` is still only assigned inside the branch, which is now reachable.
- **Seam helpers:** `RailSampler` distance/interpolation/yaw, `TrainTrail` lerp, `RailLines`
  signum, `Port`/`RailStation` distance and `SeaPoint` all wrap X; `TownIndex`/`Towns`/
  `CityPopulation`/`SurfaceDecorator` filters wrap X and keep Z raw.
- **Statics:** `EventTicker`, `SanityTicker`, `CorruptionTicker`, `TrainService`, `ShipService`,
  `Waypoints`, `RiteEchoes`, `VeilLensItem`, `CityGateBlock`, `InvestigatorPedestalBlock` and the
  two site triggers are all reached by `ServerStateReset`; `Bots` clears on stop.
