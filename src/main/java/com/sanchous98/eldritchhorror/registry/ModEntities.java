package com.sanchous98.eldritchhorror.registry;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.entity.Cthulhu;
import com.sanchous98.eldritchhorror.entity.DunwichHorror;
import com.sanchous98.eldritchhorror.entity.LesserSwarm;
import com.sanchous98.eldritchhorror.entity.RisenHusk;
import com.sanchous98.eldritchhorror.entity.ShubNiggurath;
import com.sanchous98.eldritchhorror.entity.TaintedFauna;
import com.sanchous98.eldritchhorror.entity.Watcher;
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
        event.put(CTHULHU.get(), Cthulhu.createAttributes().build());
        event.put(DUNWICH_HORROR.get(), DunwichHorror.createAttributes().build());
        event.put(SHUB_NIGGURATH.get(), ShubNiggurath.createAttributes().build());
    }

    private ModEntities() {
    }
}
