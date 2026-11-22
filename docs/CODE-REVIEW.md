# Code Review Report (`b1d7e61`)

Full codebase audit across all 485 Java source files, 29 JUnit test suites, JSON data/assets, and
Python tooling. Organized by severity (`High` → `Medium` → `Low / Nit`), with exact file paths,
line numbers, root cause analysis, and recommended fixes.

## Resolution (2026-11-22)

Every finding was re-checked against the source and fixed in one pass, except #22, which is
invalid (see below). Verification: `./gradlew build` green (compile + JUnit + PMD, 0 violations)
and a bounded server boot reached `Done (3.983s)` with no datapack/initialiser errors.

- **1** Tempering now reads the whetstone from the **off hand only**; max-quality weapons keep
  their normal use; `BotCommands.useitem` honours a cancelled `RightClickItem`.
- **2** `WeaponQualityStacks.isGradedWeapon` requires `isDamageableItem()`, so throwables are no
  longer temperable; `dynamite`/`tear_gas` now carry the grade lore line.
- **3** Starter kits split every `count = 2` on a `maxStack = 1` item into two single stacks;
  `order.json`/`relic.json` no longer over-stack `holy_water`/`mirror_shard`.
- **4** All mutating `/eh` subcommands (sanity, corruption, taint, rep, endgame, event) are
  gamemaster-gated.
- **5** Cylinder-wrapped deltas in `SeaPoint`, `ShippingLines`, `CoastalPacket`,
  `RailPathMath`, `BoundaryTravel`; `RailRouteFinder.pathLength` uses real step distance;
  `EarthMap.sample` uses `Math.floorDiv`.
- **6** `Bullet.onHitEntity` is server-side only; `EldritchWarded` only borrows the held weapon
  for direct melee hits.
- **7** `DoomClock.awaken` returns success and `awakened` latches only after placement.
- **8** `RITE_READY` is stamped after all pre-checks; `closeRift` fails when it removed nothing.
- **9** `EndgameState.of` / `RiftLedger.of` normalise to `server.overworld()`.
- **10** New `core/ServerStateReset` clears every static cache on server start/stop; each owner
  exposes `clear()`.
- **11** `CityPopulation.style` sets `setVillagerXp(1)` for real professions.
- **12** `forbiddenInsight` draws from `LucidDreamPool.eligible` (tier ≤ 2, no endgame forks).
- **13** Sanity/corruption tickers re-assert a band's missing infinite effect.
- **14** Flat melee bonuses require a full-charge swing; the pact blade keys off the killing
  weapon.
- **15** Ward/sentinel lists now cover the whole mod bestiary (mod namespace only, no vanilla
  monsters, no Ancient Ones, no cultists).
- **16** `CityLocation` caches city geometry per location and skips landmark writes outside its
  reach while replaying RNG; `SurfaceDecorator` caches keep-out circles; `WailHusk` overrides
  `auraRequiresLineOfSight`.
- **17** Eye/mirror/lens passives act once per player per tick (first carried copy, all slots).
- **18** Tomes teach on both hands, at `LOWEST` priority and only when the use is not cancelled.
- **19** Projectile entity lang keys fixed in both locales.
- **20** Tokyo/Rome trees use the levelled city datum.
- **21** `OrderVault` portal jambs written once; Beijing gate doorway centred.
- **22** **Invalid.** `Builder.maxY` is `level.getMaxY()`, which is inclusive (319) in 26.3, so
  `y > maxY` is correct; no change made.
- **23** `getBaseHeight` returns `surfaceY + 1` and reports the water surface for surface/motion
  heightmaps.
- **24** `SporeBee` is a real flyer (`setNoGravity(true)` + `travelFlying`).
- **25** `CultServices.trade` broadcasts menu changes; `CultSystem.add` notifies every changed
  rank.
- **26** `@Nullable` added to all listed members.
- **27** `tools/__pycache__` removed from the index; `__pycache__/` and `*.pyc` ignored.

---

## High Severity (Gameplay Blockers, Serialization, Security & World Math)

### 1. Carrying a Whetstone Anywhere in Inventory Hijacks Right-Click for Firearms, Throwables, Bows, and Tridents
- **Files:**
  - `src/main/java/com/sanchous98/eldritchhorror/event/WeaponQualityEvents.java` (lines 36–62, 93–102)
  - `src/main/java/com/sanchous98/eldritchhorror/bot/BotCommands.java` (lines 311–326)
- **Problem:**
  1. `design/16-items.md` (lines 249–250) and the `WeaponQualityEvents` class Javadoc (lines 20–26)
     specify that tempering requires the weapon in the main hand and the whetstone in the **off hand**,
     or an explicit non-conflicting gesture. However, `findWhetstone(player)` scans the **entire player
     inventory** (`0 .. inventory.getContainerSize()`).
  2. `PlayerInteractEvent.RightClickItem` fires **before** `Item#use`. Because `revolver_38`,
     `automatic_45`, `shotgun_double`, `carbine_rifle`, `dynamite`, `tear_gas`, `tidal_trident`, and
     `veil_bow` are all graded weapons in `WeaponGrade.BY_ITEM`, right-clicking to fire a gun, throw
     dynamite/tear gas, throw the trident, or draw the bow while a whetstone sits *anywhere* in the
     player's inventory cancels the right-click (`event.setCanceled(true)`), consumes the whetstone,
     and rolls the temper gamble (potentially **shattering** the player's gun in combat instead of
     shooting!).
  3. Even when a weapon is already at `WeaponQuality.MAX` (5), `event.setCanceled(true)` executes on
     line 51 **before** `if (WeaponQuality.isMax(quality))` on line 55 — completely blocking the
     player from firing a max-quality gun as long as they carry any whetstone.
  4. In `BotCommands.useitem` (`BotCommands.java:321-322`), `PlayerInteractEvent.RightClickItem` is
     posted to `NeoForge.EVENT_BUS`, `event.isCanceled()` is ignored, and `stack.use(...)` is called
     unconditionally on the next line.
- **Fix:** Require the whetstone in `OFF_HAND` (or require crouching + whetstone in off hand), move
  `event.setCanceled(true)` or allow normal use when not tempering, and check `if (event.isCanceled())`
  in `BotCommands.useitem`.

### 2. Throwables (`dynamite`, `tear_gas`) Are Temperable with Whetstones for Zero Benefit and Stack Corruption
- **Files:**
  - `src/main/java/com/sanchous98/eldritchhorror/registry/items/WeaponGrade.java` (lines 41–42)
  - `src/main/java/com/sanchous98/eldritchhorror/registry/items/WeaponQualityStacks.java` (lines 29–42, 58–69)
  - `src/main/java/com/sanchous98/eldritchhorror/registry/items/Firearms.java` (lines 33–40)
- **Problem:**
  - `dynamite` and `tear_gas` have stack size `16` and no durability (`maxDamage == 0`), yet are in
    `WeaponGrade.BY_ITEM`.
  - `WeaponQualityStacks.isGradedWeapon` checks only `WeaponGrade.byItem().containsKey(id)`. Tempering
    a stack of 16 dynamite grants no durability (`base <= 0`), mutates `WEAPON_QUALITY` and `RARITY`
    on the entire stack (breaking stacking with untempered dynamite), or shrinks the stack on `BREAK`.
  - Additionally, `Firearms.DYNAMITE` and `Firearms.TEAR_GAS` do not call `Weapons.lore(p, id)`, so
    they lack the grey grade line and `.tooltip` lore component that all other 18 graded items have.
- **Fix:** Require `stack.isDamageableItem()` in `WeaponQualityStacks.isGradedWeapon` (and in
  `TooltipAppenders`'s quality line check), and wrap `DYNAMITE` and `TEAR_GAS` properties in
  `Weapons.lore(p, id)`.

### 3. Over-Stacked Unstackable Items (`count = 2` on `maxStackSize = 1`) in Investigator Kits and Chest Loot Tables
- **Files:**
  - `src/main/java/com/sanchous98/eldritchhorror/investigator/Investigators.java` (lines 89, 108–110, 115)
  - `src/main/java/com/sanchous98/eldritchhorror/registry/items/Consumables.java` (lines 19, 24, 26, 30)
  - `src/main/java/com/sanchous98/eldritchhorror/registry/items/Conditions.java` (line 39)
  - `src/main/resources/data/eldritch_horror/loot_table/chests/order.json` (line 39)
  - `src/main/resources/data/eldritch_horror/loot_table/chests/relic.json` (line 33)
- **Problem:**
  - `holy_water`, `lucid_draught`, `soothing_tea`, `valerian_tonic`, `blessed_water`, and
    `mirror_shard` are registered with `maxStack = 1`.
  - `Investigators.starterKit` calls `stack("holy_water", 2)`, `stack("lucid_draught", 2)`,
    `stack("soothing_tea", 2)`, `stack("valerian_tonic", 2)`, and `stack("blessed_water", 2)`,
    constructing `new ItemStack(item, 2)`.
  - In Minecraft 26.3, `ItemStack.CODEC` enforces `count <= item.getMaxStackSize()`; an over-stacked
    `ItemStack` fails codec validation during player/container save.
  - Similarly, `order.json` sets `set_count` `1..2` on `holy_water` and `relic.json` sets `set_count`
    `1..2` on `mirror_shard`.
- **Fix:** Either give two separate `stack(..., 1)` entries in `starterKit` (or raise tonic/water
  `maxStack` to `16` like `iron_broth`), and cap `set_count` for `maxStack = 1` items in loot tables.

### 4. Unrestricted State-Mutating Admin Commands Exposed to Non-Op Players
- **File:** `src/main/java/com/sanchous98/eldritchhorror/commands/EldritchCommands.java` (lines 76–260, 535–555, 668–684)
- **Problem:** While `investigator set`, `prologue reset`, `home`, `codex learn`, and `cultist` check
  `Permissions.COMMANDS_GAMEMASTER`, the following state-mutating commands lack any permission gate:
  - `/eh sanity set <value>` and `/eh sanity add <delta>` (lines 78–81)
  - `/eh corruption set <value>` and `/eh corruption add <delta>` (lines 82–85)
  - `/eh taint set <value>`, `/eh taint add <delta>`, and `/eh taint purify` (lines 86–129)
  - `/eh rep set <cult> <value>` and `/eh rep add <cult> <delta>` (lines 145–174)
  - `/eh endgame set <outcome>` (lines 234–238, 543–555) — allows any regular player to force or
    reset the server-wide endgame!
  - `/eh event <id>` (lines 214–216, 668–684) — force-starts a world event without op permission.
- **Fix:** Add `.requires(s -> Permissions.COMMANDS_GAMEMASTER.check(s.permissions()))` (matching the
  existing gamemaster-gated subcommands) to all mutating subcommands.

### 5. Cylinder / Antimeridian Seam (`±65536`) Bugs in Sea & Rail Routing and Negative Division in `EarthMap.sample`
- **Files:**
  - `src/main/java/com/sanchous98/eldritchhorror/world/transport/SeaPoint.java` (lines 12–16)
  - `src/main/java/com/sanchous98/eldritchhorror/world/transport/ShippingLines.java` (lines 221–253)
  - `src/main/java/com/sanchous98/eldritchhorror/world/transport/CoastalPacket.java` (lines 409–424)
  - `src/main/java/com/sanchous98/eldritchhorror/world/rail/RailRouteFinder.java` (lines 165–175)
  - `src/main/java/com/sanchous98/eldritchhorror/world/rail/RailPathMath.java` (lines 64–118)
  - `src/main/java/com/sanchous98/eldritchhorror/world/BoundaryTravel.java` (lines 68–75)
  - `src/main/java/com/sanchous98/eldritchhorror/world/EarthMap.java` (lines 138–142)
- **Problem:**
  1. `SeaGrid` and `RailGrid` use `wrapDelta` during A* search, but downstream path validation and
     movement (`SeaPoint.distanceTo`, `ShippingLines.firstLand`/`waterSegment`, `CoastalPacket.advance`,
     `RailPathMath.appendSegment`) compute `dx = b.x() - a.x()` without wrapping across `2 * HALF_WIDTH`.
     Any segment crossing the Pacific seam (`±65536`) traces a 131,072-block line backwards across all
     continents.
  2. `RailRouteFinder.pathLength(route)` iterates over smoothed waypoints and adds only `1.0` or
     `SQRT2` per waypoint pair instead of the actual cell distance between smoothed waypoints,
     underestimating smoothed route lengths severalfold.
  3. `EarthMap.sample` computes `(worldX + HALF_WIDTH) / BLOCKS_PER_PIXEL` using Java `/` (truncation
     toward zero) instead of `Math.floorDiv(worldX + HALF_WIDTH, BLOCKS_PER_PIXEL)`, duplicating pixel
     `0` when `worldX < -HALF_WIDTH`.
- **Fix:** Use cylinder-wrapped `dx` in `SeaPoint`, `ShippingLines`, `CoastalPacket`, and
  `RailPathMath`; compute actual Euclidean/octile step distance in `RailRouteFinder.pathLength`; use
  `Math.floorDiv` in `EarthMap.sample`.

### 6. `Bullet.onHitEntity` Runs Unchecked on Client Side and Loses Weapon Context for `EldritchWarded`
- **Files:**
  - `src/main/java/com/sanchous98/eldritchhorror/entity/Bullet.java` (lines 64–72)
  - `src/main/java/com/sanchous98/eldritchhorror/entity/EldritchWarded.java` (lines 57–74)
- **Problem:**
  - `Bullet.onHitEntity` calls `hit.getEntity().hurt(...)`, decrements `this.pierce`, and calls
    `this.discard()` on both client and server without `if (!this.level().isClientSide())` (unlike
    `onHit` on line 77). Client-side `discard()` causes visual desyncs on piercing rounds.
  - Because `this.damageSources().thrown(this, this.getOwner())` has `source.getWeaponItem() == null`,
    `EldritchWarded.wardsOff` falls back to `attacker.getWeaponItem()` (the player's currently held
    main-hand item at impact time), allowing a player to fire a bullet/arrow and swap to
    `star_iron_sword` before impact to bypass the ward.
- **Fix:** Guard damage and `pierce`/`discard()` in `Bullet.onHitEntity` with
  `if (!this.level().isClientSide())`, and only fall back to `attacker.getWeaponItem()` in
  `EldritchWarded` for direct melee attacks (`source.isDirect()`).

---

## Medium Severity (Logic Bugs, State Leaks & Edge Cases)

### 7. `DoomClock.awaken` Permanently Latches `awakened = true` Even When Spawning Fails
- **File:** `src/main/java/com/sanchous98/eldritchhorror/endgame/DoomClock.java` (lines 190–196, 214–245)
- **Problem:** The Javadoc on `awaken(ServerLevel)` states: *"If it cannot place the presence this
  pass, nothing is announced and the next tick is free to try again."* However, `checkThresholds` sets
  `clock.awakened = true; clock.setDirty();` **before** calling `awaken(level)`. If `awaken(level)`
  cannot find a valid surface spot on that tick (e.g., player is underground or at sea), the 70%
  awakening never retries.
- **Fix:** Have `awaken(level)` return `boolean` (`true` when `horror != null` or already nearby) and
  only latch `clock.awakened = true` when it succeeds (or when `!NPC_CULT_RITUALS_CAN_AWAKEN.get()`).

### 8. `RiteEngine`: Cooldown Stamped Before Altar/Offering Checks, and `closeRift` Consumes Offerings for 0 Rifts
- **File:** `src/main/java/com/sanchous98/eldritchhorror/rite/RiteEngine.java` (lines 154–220, 737–767)
- **Problem:**
  1. Line 161 writes `RITE_READY.put(player.getUUID(), now + cooldown)` **before** verifying that the
     player is at a valid altar (line 168), inside a Fallen city (line 178), or holding required
     offerings (line 214), contradicting the class Javadoc (*"a rite that never started — altar,
     cooldown, missing offerings — pays nothing"*).
  2. `closeRift` (lines 737–767) returns `new Result(true, "The Way is sealed; 0 rift marker(s) removed...")`
     even when `removed == 0` (which happens if `REQUIRE_RITE_CONDITIONS` is false, or at the corner of
     the `riftNear` sphere vs `blockPosition()` box), consuming offerings and granting `+10` sanity
     when no rift was closed.
- **Fix:** Stamp `RITE_READY` only after all pre-checks (altar, city, offerings) pass, and return
  `new Result(false, ...)` in `closeRift` when `removed == 0`.

### 9. `EndgameState.of(level)` and `RiftLedger.of(level)` Are Not Normalized to `server.overworld()`
- **Files:**
  - `src/main/java/com/sanchous98/eldritchhorror/endgame/EndgameState.java` (lines 70–73)
  - `src/main/java/com/sanchous98/eldritchhorror/endgame/RiftLedger.java` (lines 51–54)
- **Problem:** Unlike `DoomClock.of(level)` (which resolves `level.getServer().overworld()`),
  `EndgameState.of` and `RiftLedger.of` use `level.getDataStorage()` on whatever dimension is passed
  in, splitting endgame state if accessed from `eldritch_horror:threshold` or another dimension.
- **Fix:** Normalize `ServerLevel overworld = level.getServer().overworld();` in both `of(ServerLevel)`
  methods.

### 10. Static Maps Not Cleared on Server Stop / World Switch (Cross-World State & Tick Lockouts)
- **Files:**
  - `src/main/java/com/sanchous98/eldritchhorror/world/cult/CultRituals.java` (lines 121–123: `CELLS`, `COOLDOWNS`)
  - `src/main/java/com/sanchous98/eldritchhorror/world/transport/ShipService.java` (lines 50–54: `ACTIVE`, `LAST_TICK`, `STUCK`)
  - `src/main/java/com/sanchous98/eldritchhorror/entity/CthulhuSiteTrigger.java` (line 44: `SPAWNED`)
  - `src/main/java/com/sanchous98/eldritchhorror/entity/ChoirLeviathanTrigger.java` (line 48: `SPAWNED`)
  - `src/main/java/com/sanchous98/eldritchhorror/rite/RiteEngine.java` (line 104: `RITE_READY`)
  - `src/main/java/com/sanchous98/eldritchhorror/cult/ReputationDecayTicker.java` (line 37: `LAST_TOUCH`)
  - `src/main/java/com/sanchous98/eldritchhorror/registry/items/VeilLensItem.java` (line 39: `DRAIN`)
  - `src/main/java/com/sanchous98/eldritchhorror/registry/items/RiteEchoes.java` (line 20: `LAST`)
  - `src/main/java/com/sanchous98/eldritchhorror/registry/items/Waypoints.java` (line 28: `MARKS`)
  - `src/main/java/com/sanchous98/eldritchhorror/registry/blocks/CityGateBlock.java` (line 48: `LAST_USE`)
  - `src/main/java/com/sanchous98/eldritchhorror/registry/blocks/InvestigatorPedestalBlock.java` (line 46: `PREVIEW`)
- **Problem:** When switching from a high-`gameTime` world to a fresh world in the same JVM (or across
  GameTest runs), stale tick timestamps in `RITE_READY`, `COOLDOWNS`, `LAST_USE`, and `LAST_TOUCH`
  lock out rites/altars/gates because `now < ready`; `SPAWNED` prevents Cthulhu/Leviathan from ever
  spawning in the second world; `ShipService.ACTIVE` retains strong references to the old
  `ServerLevel`; `RiteEchoes.LAST` lets a player echo a previous world's rite.
- **Fix:** Clear these maps on `ServerStoppedEvent` / `ServerStartedEvent` (and per-player maps on
  `PlayerLoggedOutEvent`), or check `now < ready - maxCooldown` to self-heal on clock regression.

### 11. Curated City Villagers Immediately Lose Their Professions (`CityPopulation`)
- **File:** `src/main/java/com/sanchous98/eldritchhorror/world/CityPopulation.java` (lines 219–227)
- **Problem:** `TownPopulation.java` (line 172) sets `villager.setVillagerXp(1)` so vanilla's
  `ResetProfession` AI behavior does not strip jobless level-1 villagers back to `NONE` on their first
  tick. `CityPopulation.style()` assigns a profession from `PROFESSIONS` but omits
  `villager.setVillagerXp(1)`, causing every professional villager in the 24 curated cities to immediately
  revert to `VillagerProfession.NONE`.
- **Fix:** Call `villager.setVillagerXp(1)` in `CityPopulation.style()` whenever `profession` is not
  `NONE` or `NITWIT`.

### 12. Nikolai Volkov's `Forbidden Insight` Active Ability Can Teach Endgame Fork Rites
- **File:** `src/main/java/com/sanchous98/eldritchhorror/investigator/SignatureAbilities.java` (lines 164–176)
- **Problem:** Unlike `LucidDreamPool` (which restricts to tier ≤ 2 and excludes the endgame fork
  rites), `forbiddenInsight` (20s cooldown, no reagent cost) picks any unknown rite from `Rites.all()`.
  A player can spam the item every 20 seconds to learn all tier-3 rites, Ancient One solves, and
  `awaken_the_horror` / `banish_the_horror` / `summon_the_horror` without ever reaching `INNER_CIRCLE`
  or reading a tome.
- **Fix:** Filter `forbiddenInsight` candidates through `LucidDreamPool.eligible(...)` (or exclude
  `LucidDreamPool.EXCLUDED` and tier 3 cult-gated rites).

### 13. `SanityTicker` / `CorruptionTicker` Do Not Re-Apply Threshold Effects After `Sedate` or Dimension Travel
- **Files:**
  - `src/main/java/com/sanchous98/eldritchhorror/sanity/SanityTicker.java` (lines 71–84)
  - `src/main/java/com/sanchous98/eldritchhorror/corruption/CorruptionTicker.java` (lines 72–85)
  - `src/main/java/com/sanchous98/eldritchhorror/investigator/SignatureAbilities.java` (lines 90–91)
- **Problem:** `SanityTicker` only calls `applyEffects(player, after)` when `before != after`. When
  Dr. Amos Hartley uses `Sedate` (removing `MADNESS`/`MARKED` and restoring `+20` sanity) while deep
  inside `BREAKING` or `MARKED` so that `after` stays in the same band (`before == after`),
  `SanityTicker` never re-applies `MADNESS` until the player crosses a band boundary or relogs.
- **Fix:** In `SanityTicker` and `CorruptionTicker`, also check whether the expected infinite effect for
  `after` is missing on the player, or clear `PREVIOUS.remove(player.getUUID())` on dimension change.

### 14. Combat Exploits in `WeaponEffects` (Spam-Click Bonus Damage & `hollow_pact_blade` Off-Weapon Kills)
- **File:** `src/main/java/com/sanchous98/eldritchhorror/registry/items/WeaponEffects.java` (lines 130–193)
- **Problem:**
  1. `applyMelee` adds flat `+2.0F` / `+3.0F` bonus damage (`woodsman_axe`, `silver_blade`,
     `star_iron_sword`) and drains sanity in `LivingDamageEvent.Pre` without checking attack charge, so
     0-cooldown spam-clicking deals full flat bonus damage on every click.
  2. `onDeath` checks `isHeld(player.getMainHandItem(), "hollow_pact_blade")` at the moment an entity
     dies rather than checking `event.getSource().getWeaponItem()`, charging `-4.0` sanity and `+1.0`
     corruption when a projectile/explosion kills a mob while the player holds the blade (or allowing a
     player to swap away right before death).

### 15. Incomplete Lesser-Horror Lists in `MobSuppressor.isWardedLesserSpawn` and `StoneSentinel.isWardable`
- **Files:**
  - `src/main/java/com/sanchous98/eldritchhorror/world/MobSuppressor.java` (lines 98–110)
  - `src/main/java/com/sanchous98/eldritchhorror/entity/StoneSentinel.java` (lines 57–64)
- **Problem:** `MobSuppressor.isWardedLesserSpawn` only blocks 3 early mobs (`LesserSwarm`, `Watcher`,
  `BoneChoir`), allowing the other 13 lesser/tainted horrors (`RisenHusk`, `VeilStalker`,
  `DrownedThrall`, `StarSpawn`, `NightHag`, `Byakhee`, `AshenRevenant`, etc.) to spawn freely inside an
  active `ward_of_the_eye`.

### 16. Worldgen & Aura Performance Bottlenecks (`CityLocation.build`, `SurfaceDecorator.inCity`, `WailHusk`)
- **Files:**
  - `src/main/java/com/sanchous98/eldritchhorror/world/loc/city/CityLocation.java` (lines 122–160, 275–317)
  - `src/main/java/com/sanchous98/eldritchhorror/world/SurfaceDecorator.java` (lines 46–52, 147–164)
  - `src/main/java/com/sanchous98/eldritchhorror/entity/WailHusk.java` (lines 120–122)
- **Problem:**
  - `CityLocation.build` runs on all ~2,600 chunks of a city and recomputes ~3,600 elevation probes
    (`meanLevel` + `edgeRingWidth`) and executes tens of thousands of `b.put(...)` calls for the
    central landmark on every single chunk, even 350 blocks away where every block is clipped out.
    This directly contributes to the high CPU/memory churn during worldgen noted in `docs/TODO.md`.
  - `WailHusk` checks `hasLineOfSight(player)` inside `auraRateFor(player)` instead of overriding
    `auraRequiresLineOfSight()`. When 3 `WailHusk`s are in range (`auraMaxStack = 2`) and the first 2
    are behind a wall, they fill the stack cap of 2 with `0.0` drain and block the 3rd `WailHusk` that
    actually has line of sight.

### 17. Multiple Unstackable Artifacts (`EyeOfTheWatcherItem`, `MirrorShardItem`, `VeilLensItem`) Multiply Passive Sanity Drain
- **Files:**
  - `src/main/java/com/sanchous98/eldritchhorror/registry/items/EyeOfTheWatcherItem.java` (lines 60–65)
  - `src/main/java/com/sanchous98/eldritchhorror/registry/items/MirrorShardItem.java` (lines 60–69)
  - `src/main/java/com/sanchous98/eldritchhorror/registry/items/VeilLensItem.java` (lines 60–74)
- **Problem:** `inventoryTick` fires once per matching stack in the inventory. Carrying two
  `MirrorShardItem`s or two `VeilLensItem`s drains sanity at 2× the intended rate on the same tick.

### 18. `TomeRiteKnowledge` Listens to `RightClickItem` Instead of `Item#use`
- **File:** `src/main/java/com/sanchous98/eldritchhorror/rite/TomeRiteKnowledge.java` (lines 61–90)
- **Problem:**
  - `TomeRiteKnowledge` ignores `OFF_HAND` (`event.getHand() != InteractionHand.MAIN_HAND`), whereas
    `ConsumableItem.use` still runs for `OFF_HAND` and drains the player's sanity/corruption without
    teaching the rite.
  - Conversely, right-clicking an interactable block (chest/door/altar) with a tome in `MAIN_HAND`
    fires `RightClickItem` and teaches the rite for free without `ConsumableItem.use` running.

---

## Low Severity & Nits

19. **Malformed Translation Keys for Projectile Entities (`en_us.json:922-924`, `ru_ru.json:922-924`):**
    Keys are written as `"item.eldritch_horror.entity.eldritch_horror.{bullet,thrown_dynamite,tear_gas}"`
    instead of `"entity.eldritch_horror.{bullet,thrown_dynamite,tear_gas}"`, leaving death messages and
    entity names untranslated.
20. **`TokyoStyle.flourish` (`TokyoStyle.java:68`) and `RomeStyle.streetProps` (`RomeStyle.java:190`):**
    Trees are placed at `b.groundY(x, z)` (raw unlevelled terrain) instead of the levelled city datum
    `y0` / `ground`, causing floating or buried trees whenever terrain was cut or filled.
21. **Coordinate Overwrite in `OrderVault.vaultPortal` (`OrderVault.java:165-171`) and Clamped Doorway in `BeijingStyle` (`BeijingStyle.java:160-161`):**
    `OrderVault` lines 170–171 overwrite `(cx ± 2, y, z)` written on lines 165/168. `BeijingStyle` passes
    doorway offset `17` on a 16-wide wall (`cx - 8 .. cx + 8`), clamping the gate to the corner instead
    of the center (`offset = 8`).
22. **`Builder.put` & `RailWorldgen.inside` Off-By-One at `maxY` (`Builder.java:56, 69, 471`, `RailWorldgen.java:164`):**
    Checks `y > this.maxY` instead of `y >= this.maxY` (`maxY` is exclusive `320`; valid block Y is
    `-64..319`).
23. **`EarthChunkGenerator.getBaseHeight` (`EarthChunkGenerator.java:138-141`):**
    Returns `surfaceY` instead of `surfaceY + 1` and ignores `Heightmap.Types` over oceans (returning
    seabed instead of `SEA_LEVEL + 1` for `WORLD_SURFACE_WG`).
24. **`SporeBee` (`entity/SporeBee.java:42-68`):**
    Uses `FlyingMoveControl` without `setNoGravity(true)` or `travelFlying`, dropping to the ground
    whenever idle.
25. **`CultServices.trade` & `CultSystem.add` (`CultServices.java:115-124`, `CultSystem.java:53-72`):**
    `CultServices.trade` omits `player.containerMenu.broadcastChanges()`. `CultSystem.add` does not call
    `notifyRank` for opposed cults whose standing increases on a negative delta.
26. **`@NullMarked` Missing `@Nullable` Annotations:**
    - `sanity/SanitySources.java:72` (`cityFootprints`)
    - `cult/CultServices.java:159` (`item(String path)`)
    - `investigator/SignatureAbilities.java:200` (`nearestRift`)
    - `loot/AntiquarianLootModifier.java:77` (`actingPlayer`)
    - `client/MapKeyMappings.java:28-34` (`openMap`, `openJournal`, `openCodex`, `openSkills`)
    - `core/CityRenderer.java:168` (`spec` parameter)
    - `world/cult/CultRituals.java:433` (`BlockPos[] found = {null}`)
27. **Tracked Python Bytecode Artifact in Git:**
    `tools/__pycache__/gen_item_icons.cpython-312.pyc` was committed in `6a1b12c`. Remove via
    `git rm --cached` and add `__pycache__/` to `.gitignore`.
