# Prologue implementation contract (increment 1)

Frozen interfaces for the Threshold prologue (`design/29-prologue.md`). Two writers work in
parallel against these exact names; the main agent wires config/lang, then runs one build.

**Do not run Gradle. Do not edit files outside your ownership list.** Write code that compiles
against the names below. If something is missing, use the contract's stub shape rather than
inventing a new name.

## Ownership

- **Writer A (world + blocks):** `world/threshold/Threshold.java`,
  `world/threshold/ThresholdPlacement.java`, `registry/blocks/PrologueBlocks.java`,
  `registry/blocks/ClassPedestalBlock.java`, `registry/blocks/CityGateBlock.java`,
  `src/main/resources/data/eldritch_horror/dimension_type/threshold.json`,
  `src/main/resources/data/eldritch_horror/dimension/threshold.json`,
  `src/main/resources/assets/eldritch_horror/blockstates/{investigator_pedestal,occultist_pedestal,cultist_pedestal,city_gate}.json`,
  `src/main/resources/assets/eldritch_horror/models/block/*` and `models/item/*` for those 4.
- **Writer B (class + prologue flow):** `class/ClassId.java`, `class/Classes.java`,
  `class/ClassAPI.java`, `class/Progression.java`, `class/Prologue.java`,
  edit `registry/ModAttachments.java`, edit `commands/EldritchCommands.java`.
- **Main agent:** `core/ModConfig.java`, `registry/ModBlocks.registerCategories()` wiring,
  `resources/assets/eldritch_horror/lang/en_us.json`, final build/verify/commit.

## Frozen names

### package `com.sanchous98.eldritchhorror.classes` (Writer B; `class` is a reserved word so the package is `classes`)

```java
public enum ClassId {
    INVESTIGATOR, OCCULTIST, CULTIST;
    public String id();                 // "investigator" | "occultist" | "cultist"
    public static ClassId byId(String); // null when unknown
    public Component displayName();     // from lang key "class.eldritch_horror.<id>"
}

public final class Classes {
    public static List<ClassId> all();
    public static List<ItemStack> starterKit(ClassId);
    public static List<String> startingRites(ClassId); // rite ids in Rites
}

public final class ClassAPI {
    public static ClassId get(ServerPlayer);           // null if none chosen
    public static boolean hasChosen(ServerPlayer);
    public static void set(ServerPlayer, ClassId);      // permanent; grants kit+rites+bonus
}

public final class Progression {
    public static double maxSanityFactor(ServerPlayer);      // 1.0 default
    public static double sanityDrainMultiplier(ServerPlayer); // 1.0 default
    public static double corruptionGainMultiplier(ServerPlayer); // 1.0 default
}
```

### package `com.sanchous98.eldritchhorror.world.threshold` (Writer A)

```java
public final class Threshold {
    public static final ResourceKey<Level> DIMENSION; // eldritch_horror:threshold
    public static BlockPos spawn();                    // initial player spawn (top of floor)
}

public final class ThresholdPlacement { // @EventBusSubscriber; stamps the hub once
    // on ServerStarted: forceload the hub chunks, place gates + pedestals deterministically,
    // release tickets. Idempotent (skip if the marker block is already present).
}
```

### package `com.sanchous98.eldritchhorror.registry.blocks` (Writer A)

```java
public final class PrologueBlocks {
    public static final DeferredBlock<Block> INVESTIGATOR_PEDESTAL;
    public static final DeferredBlock<Block> OCCULTIST_PEDESTAL;
    public static final DeferredBlock<Block> CULTIST_PEDESTAL;
    public static final DeferredBlock<Block> CITY_GATE;
    public static void init(); // register the 4 blocks + their BlockItems; call from ModBlocks
}

public class ClassPedestalBlock extends Block {   // ctor: (ClassId id, Properties)
    // useWithoutItem: server-side -> ClassAPI.set(serverPlayer, id); message; SUCCESS on client too
}

public class CityGateBlock extends Block {
    public static final IntegerProperty CITY = IntegerProperty.create("city", 0, 23);
    // useWithoutItem: server-side -> int i = state.getValue(CITY); City c = Cities.all().get(i);
    //                 Prologue.complete(serverPlayer, c); city name message
}
```

### package `com.sanchous98.eldritchhorror.classes` (Writer B, continues)

```java
public final class Prologue {
    public static boolean enabled();                       // ModConfig.ENABLE_PROLOGUE.get()
    public static boolean needsPrologue(ServerPlayer);     // enabled && !PROLOGUE_DONE
    public static void enter(ServerPlayer);                // teleport to Threshold.spawn()
    public static void complete(ServerPlayer, City);       // mark done + teleport to city surface
    // @EventBusSubscriber PlayerLoggedInEvent -> if needsPrologue(p) then enter(p)
}
```

## Flow / behaviour

1. `PlayerLoggedInEvent` (Writer B, in `Prologue`): `if (!Prologue.enabled()) return;` then
   `if (p instanceof ServerPlayer sp && Prologue.needsPrologue(sp)) Prologue.enter(sp);`.
2. `Prologue.enter`: mark `STARTED` (so a disconnect mid-prologue keeps you in the hub — actually
   the flag is `PROLOGUE_DONE`; only `complete` sets it), teleport cross-dimension to
   `Threshold.DIMENSION` at `Threshold.spawn()`.
3. `ClassPedestalBlock` use → `ClassAPI.set` (idempotent if already that class; refuse to change
   an already-chosen different class with a message).
4. `CityGateBlock` use → `Prologue.complete(sp, Cities.all().get(cityIndex))`:
   mark `PROLOGUE_DONE = true`, teleport to the city's safe surface
   (`BestiarySupport.surfaceSpot(overworld, city.x(), city.z(), false)`, fallback to `getHeight`),
   grant nothing else.
5. `Prologue.complete` must be a no-op-with-message if the player has not chosen a class yet
   (`!ClassAPI.hasChosen`) — you must pick a class first.

## Frozen details

- **Attachments** (Writer B edits `ModAttachments`):
  - `PLAYER_CLASS`: `AttachmentType<String>`, default `""`, `serialize(Codec.STRING.fieldOf("id"))`,
    `sync((h,t)->h==t, ByteBufCodecs.STRING_UTF8)`, `copyOnDeath`.
  - `PROLOGUE_DONE`: `AttachmentType<Boolean>`, default `false`, `serialize(Codec.BOOL.fieldOf("done"))`,
    `sync((h,t)->h==t, ByteBufCodecs.BOOL)`, `copyOnDeath`.
- **Config** (Main): `ENABLE_PROLOGUE` boolean `enablePrologue=true`; `ENABLE_CLASS_COMMANDS`
  boolean `enableClassCommands=true`. Writer B reads `ModConfig.ENABLE_PROLOGUE.get()` at runtime.
- **City order**: `Cities.all()` order is authoritative and stable (settlements.json). Both the
  gate placement (A, city index `i`) and the use handler (A via `Cities.all().get(i)`) use it.
- **Class → kit / rite** (Writer B):
  | ClassId | kit | rite |
  |---|---|---|
  | INVESTIGATOR | `watchers_diary`, `wardstone_pendant`, `minecraft:torch`×16, `minecraft:bread`×8 | `ward_of_the_eye` |
  | OCCULTIST | `star_codex`, `void_reagent`×4, `chalk_reagent`×8 | `drowned_blessing` |
  | CULTIST | `bone_ledger`, `blood_offering`×4, `bone_reagent`×8 | `call_the_lesser` |
  Item ids are namespaced `eldritch_horror:` unless prefixed `minecraft:`. Look them up in
  `registry/items/*` — they exist.
- **Bonuses now** (Writer B, `ClassAPI.set`): max sanity via the `eldritch_horror:max_sanity`
  attribute modifier (`AttributeModifier`, ADD_MULTIPLIED_BASE): Investigator `+0.20`, Occultist
  `-0.10`, Cultist `0.0`. Expose the three `Progression` multipliers; the main agent wires them
  into the sanity/corruption tickers.
- **Dimension** (Writer A): `dimension_type/threshold.json` modelled on vanilla overworld
  (`has_skylight:false`, `has_ceiling:false`, `ambient_light:0.0`, `effects`/skybox `THE_END`,
  `min_y:-64`, `height:384`, `logical_height:384`, `coordinate_scale:1.0`,
  `monster_spawn_block_light_limit:0`, `monster_spawn_light_level:0`, `infiniburn:#minecraft:infiniburn_overworld`,
  a dark `visual/fog_color`/`sky_color`). `dimension/threshold.json` uses
  `"type":"minecraft:flat"` with `generator.settings`: `layers` = one layer of
  `minecraft:stone` height 1 (plus a `minecraft:bedrock` height 1 below), `biome: minecraft:the_void`,
  `lakes:false`, `features:false`, `structure_overrides: []`; or a suitable equivalent. Keep the
  island small by relying on the world border is **not** available per-dimension — instead build
  the island in `ThresholdPlacement` by only stamping a bounded 96×96 area and leaving the rest as
  flat void/barrier (place a barrier ring).
- **Teleport** (Writer B): cross-dimension via
  `player.teleportTo(level, x, y, z, Set.of(), yaw, pitch, false)` where
  `ServerLevel level = server.getLevel(Threshold.DIMENSION)`; server-side only; `ServerPlayer.level()`
  is covariantly `ServerLevel`.
- **Commands** (Writer B): `/eh class get` (any player) and `/eh class set <id>` (gamemaster),
  gated by `commandsDisabled(ctx, ModConfig.ENABLE_CLASS_COMMANDS.get())` at execution.
- **No BlockEntity / no menu / no screen** — use blockstate properties only.
- **Assets**: reference an existing texture (e.g. `minecraft:block/gold_block`,
  `minecraft:block/amethyst_block`, `minecraft:block/obsidian`) so nothing renders as a missing
  texture; final art is the user's job. Lang keys are owned by the main agent.
- Follow `AGENTS.md`: `@NullMarked` on our classes (add `@Nullable` where a method can return
  null, e.g. `ClassAPI.get`, `ClassId.byId`), no deprecated APIs, JDK `Math.clamp`.

## Deferred (do NOT implement now)

- ritual-power / learning-speed / reputation-gain wiring; return travel; skill trees; NPCs.
