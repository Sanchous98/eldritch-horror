package com.sanchous98.eldritchhorror.registry;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.entity.BlightPod;
import com.sanchous98.eldritchhorror.entity.BoneChoir;
import com.sanchous98.eldritchhorror.entity.Byakhee;
import com.sanchous98.eldritchhorror.entity.ChoirSpite;
import com.sanchous98.eldritchhorror.entity.Cthulhu;
import com.sanchous98.eldritchhorror.entity.DrownedThrall;
import com.sanchous98.eldritchhorror.entity.DunwichHorror;
import com.sanchous98.eldritchhorror.entity.LesserSwarm;
import com.sanchous98.eldritchhorror.entity.NightHag;
import com.sanchous98.eldritchhorror.entity.RiftMite;
import com.sanchous98.eldritchhorror.entity.RisenHusk;
import com.sanchous98.eldritchhorror.entity.ShamblerOoze;
import com.sanchous98.eldritchhorror.entity.ShoggothMass;
import com.sanchous98.eldritchhorror.entity.ShubNiggurath;
import com.sanchous98.eldritchhorror.entity.StarSpawn;
import com.sanchous98.eldritchhorror.entity.TaintedFauna;
import com.sanchous98.eldritchhorror.entity.VeilStalker;
import com.sanchous98.eldritchhorror.entity.Watcher;
import com.sanchous98.eldritchhorror.entity.WeaverSpawn;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.SpawnEggItem;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Entity registry. The first real bestiary entry lives here: {@code risen_husk}, the low bar of the
 * wrong — a slow shambling dead that drains sanity nearby ({@code design/25-bestiary-and-entities.md},
 * build order 1).
 *
 * <p>Verified against the 26.3 patched sources: the 26.3 builder has no {@code build(String)}
 * overload; the type is built with {@code build(ResourceKey<EntityType<?>>)} (or, more usually, the
 * {@code DeferredRegister.Entities.registerEntityType} helper). The dimension/loot-table defaults
 * come from {@link EntityType.Builder}: a missing loot table id resolves to
 * {@code <modid>:entities/<id>} (here {@code eldritch_horror:entities/risen_husk}).
 */
@EventBusSubscriber(modid = EldritchHorror.MODID)
public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, EldritchHorror.MODID);

    /** The risen husk: 0.6 x 1.95, hostile, tracking range 8 (matching vanilla zombie). */
    public static final DeferredHolder<EntityType<?>, EntityType<RisenHusk>> RISEN_HUSK =
            ENTITY_TYPES.register("risen_husk", () ->
                    EntityType.Builder.<RisenHusk>of(RisenHusk::new, MobCategory.MONSTER)
                            .sized(0.6F, 1.95F)
                            .clientTrackingRange(8)
                            .notInPeaceful()
                            .build(ResourceKey.create(Registries.ENTITY_TYPE, EldritchHorror.id("risen_husk"))));

    /**
     * Spawn egg for testing. Registered through {@link ModItems#ITEMS} (the shared item register) but
     * declared here, because the entity registry owns the entity it spawns. The lambda defers
     * {@code RISEN_HUSK.get()} until the ITEM register event, by which point ENTITY_TYPE has already
     * run (NeoForge's vanilla registry order registers ENTITY_TYPE before ITEM).
     */
    public static final DeferredItem<SpawnEggItem> RISEN_HUSK_SPAWN_EGG =
            ModItems.ITEMS.registerItem("risen_husk_spawn_egg",
                    properties -> new SpawnEggItem(properties.spawnEgg(RISEN_HUSK.get())));

    /** Tainted fauna: 0.9 x 1.4, creature category (an animal made wrong). */
    public static final DeferredHolder<EntityType<?>, EntityType<TaintedFauna>> TAINTED_FAUNA =
            ENTITY_TYPES.register("tainted_fauna", () ->
                    EntityType.Builder.<TaintedFauna>of(TaintedFauna::new, MobCategory.CREATURE)
                            .sized(0.9F, 1.4F)
                            .clientTrackingRange(8)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE, EldritchHorror.id("tainted_fauna"))));

    public static final DeferredItem<SpawnEggItem> TAINTED_FAUNA_SPAWN_EGG =
            ModItems.ITEMS.registerItem("tainted_fauna_spawn_egg",
                    properties -> new SpawnEggItem(properties.spawnEgg(TAINTED_FAUNA.get())));

    /** Lesser swarm: 0.4 x 0.3, tiny hostile hitbox. */
    public static final DeferredHolder<EntityType<?>, EntityType<LesserSwarm>> LESSER_SWARM =
            ENTITY_TYPES.register("lesser_swarm", () ->
                    EntityType.Builder.<LesserSwarm>of(LesserSwarm::new, MobCategory.MONSTER)
                            .sized(0.4F, 0.3F)
                            .clientTrackingRange(8)
                            .notInPeaceful()
                            .build(ResourceKey.create(Registries.ENTITY_TYPE, EldritchHorror.id("lesser_swarm"))));

    public static final DeferredItem<SpawnEggItem> LESSER_SWARM_SPAWN_EGG =
            ModItems.ITEMS.registerItem("lesser_swarm_spawn_egg",
                    properties -> new SpawnEggItem(properties.spawnEgg(LESSER_SWARM.get())));

    /** The Watcher: 0.6 x 2.9, tall hostile presence. */
    public static final DeferredHolder<EntityType<?>, EntityType<Watcher>> WATCHER =
            ENTITY_TYPES.register("watcher", () ->
                    EntityType.Builder.<Watcher>of(Watcher::new, MobCategory.MONSTER)
                            .sized(0.6F, 2.9F)
                            .clientTrackingRange(10)
                            .notInPeaceful()
                            .build(ResourceKey.create(Registries.ENTITY_TYPE, EldritchHorror.id("watcher"))));

    public static final DeferredItem<SpawnEggItem> WATCHER_SPAWN_EGG =
            ModItems.ITEMS.registerItem("watcher_spawn_egg",
                    properties -> new SpawnEggItem(properties.spawnEgg(WATCHER.get())));

    /** Bone choir: 0.6 x 1.9, hostile, tracking range 8 (matching vanilla skeleton). */
    public static final DeferredHolder<EntityType<?>, EntityType<BoneChoir>> BONE_CHOIR =
            ENTITY_TYPES.register("bone_choir", () ->
                    EntityType.Builder.<BoneChoir>of(BoneChoir::new, MobCategory.MONSTER)
                            .sized(0.6F, 1.9F)
                            .clientTrackingRange(8)
                            .notInPeaceful()
                            .build(ResourceKey.create(Registries.ENTITY_TYPE, EldritchHorror.id("bone_choir"))));

    public static final DeferredItem<SpawnEggItem> BONE_CHOIR_SPAWN_EGG =
            ModItems.ITEMS.registerItem("bone_choir_spawn_egg",
                    properties -> new SpawnEggItem(properties.spawnEgg(BONE_CHOIR.get())));

    /** Drowned thrall: 0.6 x 1.95, hostile coastal mob (matching vanilla drowned). */
    public static final DeferredHolder<EntityType<?>, EntityType<DrownedThrall>> DROWNED_THRALL =
            ENTITY_TYPES.register("drowned_thrall", () ->
                    EntityType.Builder.<DrownedThrall>of(DrownedThrall::new, MobCategory.MONSTER)
                            .sized(0.6F, 1.95F)
                            .clientTrackingRange(8)
                            .notInPeaceful()
                            .build(ResourceKey.create(Registries.ENTITY_TYPE, EldritchHorror.id("drowned_thrall"))));

    public static final DeferredItem<SpawnEggItem> DROWNED_THRALL_SPAWN_EGG =
            ModItems.ITEMS.registerItem("drowned_thrall_spawn_egg",
                    properties -> new SpawnEggItem(properties.spawnEgg(DROWNED_THRALL.get())));

    /** Veil stalker: 1.4 x 0.9, hostile ambusher (matching vanilla spider's footprint). */
    public static final DeferredHolder<EntityType<?>, EntityType<VeilStalker>> VEIL_STALKER =
            ENTITY_TYPES.register("veil_stalker", () ->
                    EntityType.Builder.<VeilStalker>of(VeilStalker::new, MobCategory.MONSTER)
                            .sized(1.4F, 0.9F)
                            .clientTrackingRange(8)
                            .notInPeaceful()
                            .build(ResourceKey.create(Registries.ENTITY_TYPE, EldritchHorror.id("veil_stalker"))));

    public static final DeferredItem<SpawnEggItem> VEIL_STALKER_SPAWN_EGG =
            ModItems.ITEMS.registerItem("veil_stalker_spawn_egg",
                    properties -> new SpawnEggItem(properties.spawnEgg(VEIL_STALKER.get())));

    /** Blight pod: 0.6 x 1.7, hostile corruption vector (creeper footprint, a little shorter). */
    public static final DeferredHolder<EntityType<?>, EntityType<BlightPod>> BLIGHT_POD =
            ENTITY_TYPES.register("blight_pod", () ->
                    EntityType.Builder.<BlightPod>of(BlightPod::new, MobCategory.MONSTER)
                            .sized(0.6F, 1.7F)
                            .clientTrackingRange(8)
                            .notInPeaceful()
                            .build(ResourceKey.create(Registries.ENTITY_TYPE, EldritchHorror.id("blight_pod"))));

    public static final DeferredItem<SpawnEggItem> BLIGHT_POD_SPAWN_EGG =
            ModItems.ITEMS.registerItem("blight_pod_spawn_egg",
                    properties -> new SpawnEggItem(properties.spawnEgg(BLIGHT_POD.get())));

    /** Byakhee: 0.9 x 0.5, hostile flier (matching the phantom's low, wide footprint). */
    public static final DeferredHolder<EntityType<?>, EntityType<Byakhee>> BYAKHEE =
            ENTITY_TYPES.register("byakhee", () ->
                    EntityType.Builder.<Byakhee>of(Byakhee::new, MobCategory.MONSTER)
                            .sized(0.9F, 0.5F)
                            .clientTrackingRange(10)
                            .notInPeaceful()
                            .build(ResourceKey.create(Registries.ENTITY_TYPE, EldritchHorror.id("byakhee"))));

    public static final DeferredItem<SpawnEggItem> BYAKHEE_SPAWN_EGG =
            ModItems.ITEMS.registerItem("byakhee_spawn_egg",
                    properties -> new SpawnEggItem(properties.spawnEgg(BYAKHEE.get())));

    /** Star-spawn: 0.6 x 2.9, tall hostile minion (the Watcher's silhouette). */
    public static final DeferredHolder<EntityType<?>, EntityType<StarSpawn>> STAR_SPAWN =
            ENTITY_TYPES.register("star_spawn", () ->
                    EntityType.Builder.<StarSpawn>of(StarSpawn::new, MobCategory.MONSTER)
                            .sized(0.6F, 2.9F)
                            .clientTrackingRange(10)
                            .notInPeaceful()
                            .build(ResourceKey.create(Registries.ENTITY_TYPE, EldritchHorror.id("star_spawn"))));

    public static final DeferredItem<SpawnEggItem> STAR_SPAWN_SPAWN_EGG =
            ModItems.ITEMS.registerItem("star_spawn_spawn_egg",
                    properties -> new SpawnEggItem(properties.spawnEgg(STAR_SPAWN.get())));

    /** Shoggoth mass: 1.6 x 1.6, hostile slow elite (a large amorphous blob). */
    public static final DeferredHolder<EntityType<?>, EntityType<ShoggothMass>> SHOGGOTH_MASS =
            ENTITY_TYPES.register("shoggoth_mass", () ->
                    EntityType.Builder.<ShoggothMass>of(ShoggothMass::new, MobCategory.MONSTER)
                            .sized(1.6F, 1.6F)
                            .clientTrackingRange(10)
                            .notInPeaceful()
                            .build(ResourceKey.create(Registries.ENTITY_TYPE, EldritchHorror.id("shoggoth_mass"))));

    public static final DeferredItem<SpawnEggItem> SHOGGOTH_MASS_SPAWN_EGG =
            ModItems.ITEMS.registerItem("shoggoth_mass_spawn_egg",
                    properties -> new SpawnEggItem(properties.spawnEgg(SHOGGOTH_MASS.get())));

    /** Weaver spawn: 1.4 x 0.9, hostile nest-guard (matching vanilla spider's footprint). */
    public static final DeferredHolder<EntityType<?>, EntityType<WeaverSpawn>> WEAVER_SPAWN =
            ENTITY_TYPES.register("weaver_spawn", () ->
                    EntityType.Builder.<WeaverSpawn>of(WeaverSpawn::new, MobCategory.MONSTER)
                            .sized(1.4F, 0.9F)
                            .clientTrackingRange(8)
                            .notInPeaceful()
                            .build(ResourceKey.create(Registries.ENTITY_TYPE, EldritchHorror.id("weaver_spawn"))));

    public static final DeferredItem<SpawnEggItem> WEAVER_SPAWN_SPAWN_EGG =
            ModItems.ITEMS.registerItem("weaver_spawn_spawn_egg",
                    properties -> new SpawnEggItem(properties.spawnEgg(WEAVER_SPAWN.get())));

    /** Rift mite: 0.3 x 0.2, the tiny hostile vermin hitbox (matching the endermite's). */
    public static final DeferredHolder<EntityType<?>, EntityType<RiftMite>> RIFT_MITE =
            ENTITY_TYPES.register("rift_mite", () ->
                    EntityType.Builder.<RiftMite>of(RiftMite::new, MobCategory.MONSTER)
                            .sized(0.3F, 0.2F)
                            .clientTrackingRange(8)
                            .notInPeaceful()
                            .build(ResourceKey.create(Registries.ENTITY_TYPE, EldritchHorror.id("rift_mite"))));

    public static final DeferredItem<SpawnEggItem> RIFT_MITE_SPAWN_EGG =
            ModItems.ITEMS.registerItem("rift_mite_spawn_egg",
                    properties -> new SpawnEggItem(properties.spawnEgg(RIFT_MITE.get())));

    /** Shambler ooze: 0.9 x 0.9, hostile splitter (a medium slime footprint). */
    public static final DeferredHolder<EntityType<?>, EntityType<ShamblerOoze>> SHAMBLER_OOZE =
            ENTITY_TYPES.register("shambler_ooze", () ->
                    EntityType.Builder.<ShamblerOoze>of(ShamblerOoze::new, MobCategory.MONSTER)
                            .sized(0.9F, 0.9F)
                            .clientTrackingRange(8)
                            .notInPeaceful()
                            .build(ResourceKey.create(Registries.ENTITY_TYPE, EldritchHorror.id("shambler_ooze"))));

    public static final DeferredItem<SpawnEggItem> SHAMBLER_OOZE_SPAWN_EGG =
            ModItems.ITEMS.registerItem("shambler_ooze_spawn_egg",
                    properties -> new SpawnEggItem(properties.spawnEgg(SHAMBLER_OOZE.get())));

    /** Choir spite: 0.4 x 0.4, hostile wall-passing flier (matching the vex's tiny hitbox). */
    public static final DeferredHolder<EntityType<?>, EntityType<ChoirSpite>> CHOIR_SPITE =
            ENTITY_TYPES.register("choir_spite", () ->
                    EntityType.Builder.<ChoirSpite>of(ChoirSpite::new, MobCategory.MONSTER)
                            .sized(0.4F, 0.4F)
                            .clientTrackingRange(8)
                            .notInPeaceful()
                            .build(ResourceKey.create(Registries.ENTITY_TYPE, EldritchHorror.id("choir_spite"))));

    public static final DeferredItem<SpawnEggItem> CHOIR_SPITE_SPAWN_EGG =
            ModItems.ITEMS.registerItem("choir_spite_spawn_egg",
                    properties -> new SpawnEggItem(properties.spawnEgg(CHOIR_SPITE.get())));

    /** Night hag: 0.9 x 0.5, hostile swooper (matching the phantom's low, wide footprint). */
    public static final DeferredHolder<EntityType<?>, EntityType<NightHag>> NIGHT_HAG =
            ENTITY_TYPES.register("night_hag", () ->
                    EntityType.Builder.<NightHag>of(NightHag::new, MobCategory.MONSTER)
                            .sized(0.9F, 0.5F)
                            .clientTrackingRange(10)
                            .notInPeaceful()
                            .build(ResourceKey.create(Registries.ENTITY_TYPE, EldritchHorror.id("night_hag"))));

    public static final DeferredItem<SpawnEggItem> NIGHT_HAG_SPAWN_EGG =
            ModItems.ITEMS.registerItem("night_hag_spawn_egg",
                    properties -> new SpawnEggItem(properties.spawnEgg(NIGHT_HAG.get())));

    // --- Ancient Ones (design/28-ancient-ones.md) ----------------------------------------------

    /** Cthulhu: 3.0 x 4.0, the drowned-temple site boss. */
    public static final DeferredHolder<EntityType<?>, EntityType<Cthulhu>> CTHULHU =
            ENTITY_TYPES.register("cthulhu", () ->
                    EntityType.Builder.<Cthulhu>of(Cthulhu::new, MobCategory.MONSTER)
                            .sized(3.0F, 4.0F)
                            .clientTrackingRange(12)
                            .notInPeaceful()
                            .build(ResourceKey.create(Registries.ENTITY_TYPE, EldritchHorror.id("cthulhu"))));

    public static final DeferredItem<SpawnEggItem> CTHULHU_SPAWN_EGG =
            ModItems.ITEMS.registerItem("cthulhu_spawn_egg",
                    properties -> new SpawnEggItem(properties.spawnEgg(CTHULHU.get())));

    /** The Dunwich Horror: 1.9 x 2.4, the mobile settlement threat. */
    public static final DeferredHolder<EntityType<?>, EntityType<DunwichHorror>> DUNWICH_HORROR =
            ENTITY_TYPES.register("dunwich_horror", () ->
                    EntityType.Builder.<DunwichHorror>of(DunwichHorror::new, MobCategory.MONSTER)
                            .sized(1.9F, 2.4F)
                            .clientTrackingRange(10)
                            .notInPeaceful()
                            .build(ResourceKey.create(Registries.ENTITY_TYPE, EldritchHorror.id("dunwich_horror"))));

    public static final DeferredItem<SpawnEggItem> DUNWICH_HORROR_SPAWN_EGG =
            ModItems.ITEMS.registerItem("dunwich_horror_spawn_egg",
                    properties -> new SpawnEggItem(properties.spawnEgg(DUNWICH_HORROR.get())));

    /** Shub-Niggurath: 4.0 x 5.0, the pure biome presence (large, slow). */
    public static final DeferredHolder<EntityType<?>, EntityType<ShubNiggurath>> SHUB_NIGGURATH =
            ENTITY_TYPES.register("shub_niggurath", () ->
                    EntityType.Builder.<ShubNiggurath>of(ShubNiggurath::new, MobCategory.MONSTER)
                            .sized(4.0F, 5.0F)
                            .clientTrackingRange(12)
                            .notInPeaceful()
                            .build(ResourceKey.create(Registries.ENTITY_TYPE, EldritchHorror.id("shub_niggurath"))));

    public static final DeferredItem<SpawnEggItem> SHUB_NIGGURATH_SPAWN_EGG =
            ModItems.ITEMS.registerItem("shub_niggurath_spawn_egg",
                    properties -> new SpawnEggItem(properties.spawnEgg(SHUB_NIGGURATH.get())));

    /**
     * Adds the spawn egg to the mod's creative tab. The tab is built once at registration from
     * {@code ModItems.ALL}, before items exist, so this event (fired while the tab populates) is the
     * correct place to append content created outside {@code ModItems.add} (verified 26.3:
     * {@code BuildCreativeModeTabContentsEvent} exposes {@code getTabKey()} and {@code accept}).
     */
    @SubscribeEvent
    public static void onBuildTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().equals(ResourceKey.create(Registries.CREATIVE_MODE_TAB, EldritchHorror.id("main")))) {
            event.accept(RISEN_HUSK_SPAWN_EGG.get());
            event.accept(TAINTED_FAUNA_SPAWN_EGG.get());
            event.accept(LESSER_SWARM_SPAWN_EGG.get());
            event.accept(WATCHER_SPAWN_EGG.get());
            event.accept(BONE_CHOIR_SPAWN_EGG.get());
            event.accept(DROWNED_THRALL_SPAWN_EGG.get());
            event.accept(VEIL_STALKER_SPAWN_EGG.get());
            event.accept(BLIGHT_POD_SPAWN_EGG.get());
            event.accept(BYAKHEE_SPAWN_EGG.get());
            event.accept(STAR_SPAWN_SPAWN_EGG.get());
            event.accept(SHOGGOTH_MASS_SPAWN_EGG.get());
            event.accept(WEAVER_SPAWN_SPAWN_EGG.get());
            event.accept(RIFT_MITE_SPAWN_EGG.get());
            event.accept(SHAMBLER_OOZE_SPAWN_EGG.get());
            event.accept(CHOIR_SPITE_SPAWN_EGG.get());
            event.accept(NIGHT_HAG_SPAWN_EGG.get());
            event.accept(CTHULHU_SPAWN_EGG.get());
            event.accept(DUNWICH_HORROR_SPAWN_EGG.get());
            event.accept(SHUB_NIGGURATH_SPAWN_EGG.get());
        }
    }

    /**
     * Base attributes for every bestiary entity. Fired on the mod bus after registration and before
     * common setup; a living type with no supplier here gets no attributes.
     */
    @SubscribeEvent
    public static void onAttributeCreation(EntityAttributeCreationEvent event) {
        event.put(RISEN_HUSK.get(), RisenHusk.createAttributes().build());
        event.put(TAINTED_FAUNA.get(), TaintedFauna.createAttributes().build());
        event.put(LESSER_SWARM.get(), LesserSwarm.createAttributes().build());
        event.put(WATCHER.get(), Watcher.createAttributes().build());
        event.put(BONE_CHOIR.get(), BoneChoir.createAttributes().build());
        event.put(DROWNED_THRALL.get(), DrownedThrall.createAttributes().build());
        event.put(VEIL_STALKER.get(), VeilStalker.createAttributes().build());
        event.put(BLIGHT_POD.get(), BlightPod.createAttributes().build());
        event.put(BYAKHEE.get(), Byakhee.createAttributes().build());
        event.put(STAR_SPAWN.get(), StarSpawn.createAttributes().build());
        event.put(SHOGGOTH_MASS.get(), ShoggothMass.createAttributes().build());
        event.put(WEAVER_SPAWN.get(), WeaverSpawn.createAttributes().build());
        event.put(RIFT_MITE.get(), RiftMite.createAttributes().build());
        event.put(SHAMBLER_OOZE.get(), ShamblerOoze.createAttributes().build());
        event.put(CHOIR_SPITE.get(), ChoirSpite.createAttributes().build());
        event.put(NIGHT_HAG.get(), NightHag.createAttributes().build());
        event.put(CTHULHU.get(), Cthulhu.createAttributes().build());
        event.put(DUNWICH_HORROR.get(), DunwichHorror.createAttributes().build());
        event.put(SHUB_NIGGURATH.get(), ShubNiggurath.createAttributes().build());
    }

    private ModEntities() {
    }
}
