# Code Review — open findings

Single live review of the project. Completed findings are removed from this file; the full
report and its resolution stay in git history. The first two audits (27 and 32 findings) were
resolved at `ebd394a..f190219` and `b26a045..797e9dd` and re-verified.

Third audit: done directly on the tree at `c27107a` — the 32-fix pass was read hunk by hunk, then
the highest-risk paths around it were re-checked (bot reach/placement, rail and ship runtime math,
cylinder-seam queries, per-world statics). Minecraft API claims were verified against the
decompiled 26.3 sources (`build/moddev/artifacts/minecraft-patched-*-sources.jar`).

**Open: none.** The third audit found 14 items (2 Medium, 6 Low, 6 Nit); all are fixed in
`d3dde42..60e831c`:

- **bots** — `attack` and `placeat` now enforce vanilla reach, `blockTarget` uses 4.5 blocks,
  `smelt` charges a fuel item from the bot's inventory, and a full inventory drops the
  smelt/craft result instead of losing it;
- **world/transport** — `TrainLocomotive.seedTrail` wraps its sample delta, ship/train dock and
  boarding checks use `EarthMap.horizontalDistanceSqr`, and `TownIndex` folds bucket columns
  across the seam;
- **state/tests** — `RiftGates` joins `ServerStateReset`, `CityRenderer` drops its session on
  server stop, `EarthMaps` caches a failed load, and seam interpolation tests now cover
  `RailSampler`/`TrainTrail` and the `TownIndex` bucket wrap.

Verified green on the final tree: `./gradlew build` (`compileJava`, `test`, `pmdMain`,
`pmdTest`, `check`) and a clean server boot (`Done (4.146s)`).

---

## Verified OK (no action)

- **Tempering client mirror:** `MultiPlayerGameMode.useItem` builds and returns the
  `ServerboundUseItemPacket` before `CommonHooks.onItemRightClick`, so cancelling the client
  prediction does not suppress the packet; the server still rolls. Confirmed in 26.3 sources.
- **Placement cost path:** `ServerPlayerGameMode.useItemOn` runs the block item's own
  placement/consumption; the loaned main-hand stack is restored and the source stack shrinks by
  exactly what the use consumed.
- **Hydra/Yig:** the seeded `-ESCALATION_COOLDOWN_TICKS` makes the first hit pass the cooldown
  check; `lastEscalation` is assigned inside the now-reachable branch.
- **Seam helpers:** `RailSampler` distance/interpolation/yaw, `TrainTrail` lerp,
  `RailLines` signum, `Port`/`RailStation` distance, `SeaPoint`, `TrainLocomotive.seedTrail`,
  ship/train proximity and the `TownIndex`/`Towns`/`CityPopulation`/`SurfaceDecorator` queries
  all wrap X and keep Z raw.
- **Statics:** `EventTicker`, `SanityTicker`, `CorruptionTicker`, `TrainService`, `ShipService`,
  `RiftGates`, `Waypoints`, `RiteEchoes`, `VeilLensItem`, `CityGateBlock`,
  `InvestigatorPedestalBlock` and the two site triggers are reached by `ServerStateReset`;
  `CityRenderer` drops its session on stop; `Bots` clears on stop.
- **Data/tests:** all 76 `set_count` modifiers and 47 recipes still fit `maxStackSize`; both
  locales keep 988/988 keys; the JUnit suite now pins the rail and town seam cases.
