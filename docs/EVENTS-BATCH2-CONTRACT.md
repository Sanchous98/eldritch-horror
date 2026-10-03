# World events batch 2 — implementation contract

Adds the 4 remaining bespoke events (design/19): `cult_procession`, `blood_moon_rite`, `star_fall`,
`hollow_call`. The framework already exists (`event/`); this is data + a few new context facts.
One writer owns the Java; the main agent does config/lang, then builds once.

**Do not run Gradle. Only touch the files listed.**

## Ownership

- **Writer:** `event/EventContext.java`, `event/EventTrigger.java`, `event/EventTicker.java`,
  `event/Events.java`, `event/EventEffects.java`.
- **Main agent:** `core/ModConfig.java`, `assets/.../lang/en_us.json`, docs, build/verify.

## New EventContext facts (add as trailing record components)

```java
public record EventContext(ServerLevel level, long gameTime, int ticks, boolean night,
                           boolean thundering, double sanity, double taint, List<BlockPos> rifts,
                           boolean fullMoon, CorruptionState corruption, boolean nearCultSite) { }
```
- `fullMoon`: `level.environmentAttributes().getValue(
  net.minecraft.world.attribute.EnvironmentAttributes.MOON_PHASE, player.position(), null)
  == net.minecraft.world.level.MoonPhase.FULL_MOON`. (Verified API: `ServerLevel.environmentAttributes()`
  returns `EnvironmentAttributeSystem`; `getValue(EnvironmentAttribute<Value>, Vec3, @Nullable
  SpatialAttributeInterpolator)`; `EnvironmentAttributes.MOON_PHASE` is `EnvironmentAttribute<MoonPhase>`.)
- `corruption`: `CorruptionState.of(player corruption)` — use `com.sanchous98.eldritchhorror.corruption.CorruptionAPI.get(player)`.
- `nearCultSite`: true when the player is within `ModConfig.EVENT_CULT_SITE_RADIUS.get()` blocks of
  the fixed `cult_stronghold` centre `(-29127, -13835)`. Do NOT call `Locations` (avoid the lazy
  registry in this hot path); use the constant coords in `EventTicker`.

## New EventTrigger values

Add: `CULT_SITE(false)`, `FULL_MOON_MARKED(false)`, `MARKED_RANDOM(false)`, `CLAIMED_CORRUPTION(false)`.
All `usesRifts = false` (they read the new context facts instead).

## Events.enabled switch

Add cases: `cult_procession`, `blood_moon_rite`, `star_fall`, `hollow_call` → the matching
`ModConfig.ENABLE_EVENT_<UPPER>` keys (main agent defines them).

## Events.init — register the 4 new factories (after cleansing_dawn):
`EventEffects.cultProcession(); bloodMoonRite(); starFall(); hollowCall();`

## The 4 events (write these factories in EventEffects)

### cult_procession
- trigger `CULT_SITE`, duration `ModConfig.EVENT_CULT_PROCESSION_DURATION.get()`, weight 18,
  cooldown `ModConfig.EVENT_CULT_PROCESSION_COOLDOWN.get()`.
- test: `ctx.nearCultSite()`.
- effect: every `ModConfig.EVENT_CULT_PROCESSION_SPAWN_INTERVAL.get()` ticks, top up a bounded
  procession through `BestiarySupport.topUp` (gate `anywhere`) of `ModEntities.CULT_ZEALOT`
  (cap `..._SPAWN_CAP`, per-pass 1) and `ModEntities.WORSHIPPER` (cap `..._WORSHIPPER_CAP`, per-pass 1),
  radius `ModConfig.EVENT_CULT_PROCESSION_SPAWN_RADIUS.get()`. Every 60 ticks send a positional
  chant sound to the player (reuse `SoundEvents.EVOKER_AMBIENT` / `RAVAGER_AMBIENT`, pitch ~0.6) and
  a small `SanityAPI.add` of `ModConfig.EVENT_CULT_PROCESSION_SANITY_RATE.get()`. Also grant a small
  reputation window: at start (elapsed<2) add `ModConfig.EVENT_CULT_PROCESSION_REP.get()` to cult
  `hollow_choir` via `CultSystem.add(player, "hollow_choir", ...)` once (guard on elapsed<2).

### blood_moon_rite
- trigger `FULL_MOON_MARKED`, duration `ModConfig.EVENT_BLOOD_MOON_DURATION.get()`, weight 25,
  cooldown `ModConfig.EVENT_BLOOD_MOON_COOLDOWN.get()`.
- test: `ctx.fullMoon() && ctx.corruption().ordinal() >= CorruptionState.MARKED.ordinal()`.
- effect: every 20 ticks taint the loaded 3×3 chunk patch around the player through `TaintAPI.add`
  by `ModConfig.EVENT_BLOOD_MOON_TAINT_RATE.get()` (mirror rift_bloom's patch loop). Every
  `..._SPAWN_INTERVAL` ticks top up `ModEntities.CULT_ZEALOT` (cap `..._CULT_CAP`, per-pass 1) and,
  at half the cadence, a `ModEntities.NIGHT_HAG` (cap `..._HAG_CAP`, per-pass 1). Every 40 ticks a
  red ambience: particles `ParticleTypes.CRIMSON_SPORE` at the player and a positional
  `SoundEvents.WARDEN_ROAR` (or `WITHER_AMBIENT`) at low pitch; `SanityAPI.add` `..._SANITY_RATE`.

### star_fall  (single, short event; the only event that edits blocks)
- trigger `MARKED_RANDOM`, duration `ModConfig.EVENT_STAR_FALL_DURATION.get()` (short, e.g. 100),
  weight 30, cooldown `ModConfig.EVENT_STAR_FALL_COOLDOWN.get()`.
- test: `ctx.corruption().ordinal() >= CorruptionState.MARKED.ordinal()` (the ticker performs a
  weighted pick among candidates; the shared deterministic seed already makes the choice vary, so
  no extra randomness is needed).
- effect (on the FIRST tick only, `elapsed == 0`):
  1. pick a surface spot within `ModConfig.EVENT_STAR_FALL_RADIUS.get()` blocks using
     `BestiarySupport.surfaceSpot(level, x, z, false)` and the deterministic RNG idiom of
     `whisperEffect` (`seed(player, ctx, elapsed)`); skip if none.
  2. carve a bounded crater: for columns within `..._CRATER_RADIUS` blocks of the impact, set the
     surface to `Blocks.MAGMA_BLOCK` / `Blocks.CRYING_OBSIDIAN` / `Blocks.OBSIDIAN` and clear two
     blocks above; **loaded chunks only** (`level.hasChunkAt`), `Block.UPDATE_ALL`, hard-bounded by
     `..._CRATER_BLOCKS`. Never edit outside the crater.
  3. drop the impact reward: `Block.popResource(level, impactPos, new ItemStack(
     BuiltInRegistries.ITEM.getValue(EldritchHorror.id("star_reagent")), 2))`.
  4. loud positional `SoundEvents.GENERIC_EXPLODE` + `ParticleTypes.EXPLOSION_EMITTER`.
  - every 20 ticks for the short duration: `SanityAPI.add` `..._SANITY_RATE` to the player.

### hollow_call
- trigger `CLAIMED_CORRUPTION`, duration `ModConfig.EVENT_HOLLOW_CALL_DURATION.get()`, weight 40,
  cooldown `ModConfig.EVENT_HOLLOW_CALL_COOLDOWN.get()`.
- test: `ctx.corruption() == CorruptionState.CLAIMED`.
- effect: every 20 ticks `player.addEffect(DARKNESS, 60, 0, …)` and a hard
  `SanityAPI.add` `..._SANITY_RATE`; every 60 ticks a positional "call"
  (`SoundEvents.WARDEN_NEARBY_CLOSER` / `ELDER_GUARDIAN_CURSE`) and `ParticleTypes.SCULK_SOUL`;
  every `..._SPAWN_INTERVAL` ticks top up `ModEntities.NIGHT_HAG` (cap `..._CAP`, per-pass 1) and
  `ModEntities.CHOIR_SPITE` (cap `..._CAP`, per-pass 1) on `dark || tainted` ground.

## Rules

- Reuse the existing helpers in EventEffects (`seed`, `sendSound`, `surge` if useful). Do NOT add
  `Math.random`.
- All effects server-side, overworld-only, loaded-chunks-only, bounded by config.
- `star_fall` is the ONLY event that edits blocks; keep it a tiny bounded crater. Document the
  exception to the "events only mutate taint" rule in its javadoc (design/19 asks for a meteor).
- `@NullMarked` is package-level; `@Nullable` only where needed.
- JDK `Math.clamp`; no deprecated APIs.
- The exact `ModConfig` key names are frozen (main agent adds them); mirror the existing event keys
  precisely, e.g. `EVENT_CULT_PROCESSION_DURATION`, `ENABLE_EVENT_CULT_PROCESSION`,
  `EVENT_BLOOD_MOON_TAINT_RATE`, `ENABLE_EVENT_STAR_FALL`, `EVENT_HOLLOW_CALL_SANITY_RATE`, etc.
  If unsure of a suffix, choose the same pattern as the existing `EVENT_RIFT_BLOOM_*`.

Report files edited, the exact config keys you referenced, and any uncertainty. Do NOT build.
