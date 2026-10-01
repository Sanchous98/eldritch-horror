package com.sanchous98.eldritchhorror.registry;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.entity.RisenHusk;
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
        }
    }

    /**
     * Base attributes for every bestiary entity. Fired on the mod bus after registration and before
     * common setup; a living type with no supplier here gets no attributes.
     */
    @SubscribeEvent
    public static void onAttributeCreation(EntityAttributeCreationEvent event) {
        event.put(RISEN_HUSK.get(), RisenHusk.createAttributes().build());
    }

    private ModEntities() {
    }
}
