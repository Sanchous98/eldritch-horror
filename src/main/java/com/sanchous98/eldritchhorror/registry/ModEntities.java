package com.sanchous98.eldritchhorror.registry;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.entity.AshFowl;
import com.sanchous98.eldritchhorror.entity.AtlachNacha;
import com.sanchous98.eldritchhorror.entity.Azathoth;
import com.sanchous98.eldritchhorror.entity.BlightPod;
import com.sanchous98.eldritchhorror.entity.BogBear;
import com.sanchous98.eldritchhorror.entity.BoneChoir;
import com.sanchous98.eldritchhorror.entity.Burrowling;
import com.sanchous98.eldritchhorror.entity.Byakhee;
import com.sanchous98.eldritchhorror.entity.CaveDrifter;
import com.sanchous98.eldritchhorror.entity.ChoirSpite;
import com.sanchous98.eldritchhorror.entity.Cthulhu;
import com.sanchous98.eldritchhorror.entity.CultRaider;
import com.sanchous98.eldritchhorror.entity.CultZealot;
import com.sanchous98.eldritchhorror.entity.Deer;
import com.sanchous98.eldritchhorror.entity.DrownedMinnow;
import com.sanchous98.eldritchhorror.entity.DrownedThrall;
import com.sanchous98.eldritchhorror.entity.DunwichHorror;
import com.sanchous98.eldritchhorror.entity.FrostWisp;
import com.sanchous98.eldritchhorror.entity.GreyFox;
import com.sanchous98.eldritchhorror.entity.HearthCat;
import com.sanchous98.eldritchhorror.entity.HillHound;
import com.sanchous98.eldritchhorror.entity.Ithaqua;
import com.sanchous98.eldritchhorror.entity.LanternJelly;
import com.sanchous98.eldritchhorror.entity.LesserSwarm;
import com.sanchous98.eldritchhorror.entity.MarshMote;
import com.sanchous98.eldritchhorror.entity.MireSow;
import com.sanchous98.eldritchhorror.entity.NightHag;
import com.sanchous98.eldritchhorror.entity.PackBeast;
import com.sanchous98.eldritchhorror.entity.PaleDrifter;
import com.sanchous98.eldritchhorror.entity.PlagueCrone;
import com.sanchous98.eldritchhorror.entity.RiftMite;
import com.sanchous98.eldritchhorror.entity.RisenHusk;
import com.sanchous98.eldritchhorror.entity.RiteBinder;
import com.sanchous98.eldritchhorror.entity.ShamblerOoze;
import com.sanchous98.eldritchhorror.entity.ShoggothMass;
import com.sanchous98.eldritchhorror.entity.ShubNiggurath;
import com.sanchous98.eldritchhorror.entity.SporeBee;
import com.sanchous98.eldritchhorror.entity.StarSpawn;
import com.sanchous98.eldritchhorror.entity.StoneSentinel;
import com.sanchous98.eldritchhorror.entity.TaintedFauna;
import com.sanchous98.eldritchhorror.entity.TideGrazer;
import com.sanchous98.eldritchhorror.entity.VeilStalker;
import com.sanchous98.eldritchhorror.entity.Watcher;
import com.sanchous98.eldritchhorror.entity.WeaverSpawn;
import com.sanchous98.eldritchhorror.entity.WoolBeast;
import com.sanchous98.eldritchhorror.entity.WoolHare;
import com.sanchous98.eldritchhorror.entity.Worshipper;
import com.sanchous98.eldritchhorror.entity.Yig;
import com.sanchous98.eldritchhorror.entity.YogSothoth;
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

    // --- Cult faction (design/09-cults.md, design/25 cultist family) --------------------------

    /** Worshipper: 0.6 x 1.95, a passive humanoid NPC (the villager footprint). */
    public static final DeferredHolder<EntityType<?>, EntityType<Worshipper>> WORSHIPPER =
            ENTITY_TYPES.register("worshipper", () ->
                    EntityType.Builder.<Worshipper>of(Worshipper::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.95F)
                            .clientTrackingRange(10)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE, EldritchHorror.id("worshipper"))));

    public static final DeferredItem<SpawnEggItem> WORSHIPPER_SPAWN_EGG =
            ModItems.ITEMS.registerItem("worshipper_spawn_egg",
                    properties -> new SpawnEggItem(properties.spawnEgg(WORSHIPPER.get())));

    /** Cult zealot: 0.6 x 1.95, hostile melee elite (matching vanilla vindicator). */
    public static final DeferredHolder<EntityType<?>, EntityType<CultZealot>> CULT_ZEALOT =
            ENTITY_TYPES.register("cult_zealot", () ->
                    EntityType.Builder.<CultZealot>of(CultZealot::new, MobCategory.MONSTER)
                            .sized(0.6F, 1.95F)
                            .clientTrackingRange(8)
                            .notInPeaceful()
                            .build(ResourceKey.create(Registries.ENTITY_TYPE, EldritchHorror.id("cult_zealot"))));

    public static final DeferredItem<SpawnEggItem> CULT_ZEALOT_SPAWN_EGG =
            ModItems.ITEMS.registerItem("cult_zealot_spawn_egg",
                    properties -> new SpawnEggItem(properties.spawnEgg(CULT_ZEALOT.get())));

    /** Cult raider: 0.6 x 1.95, hostile fast melee skirmisher (matching vanilla pillager). */
    public static final DeferredHolder<EntityType<?>, EntityType<CultRaider>> CULT_RAIDER =
            ENTITY_TYPES.register("cult_raider", () ->
                    EntityType.Builder.<CultRaider>of(CultRaider::new, MobCategory.MONSTER)
                            .sized(0.6F, 1.95F)
                            .clientTrackingRange(8)
                            .notInPeaceful()
                            .build(ResourceKey.create(Registries.ENTITY_TYPE, EldritchHorror.id("cult_raider"))));

    public static final DeferredItem<SpawnEggItem> CULT_RAIDER_SPAWN_EGG =
            ModItems.ITEMS.registerItem("cult_raider_spawn_egg",
                    properties -> new SpawnEggItem(properties.spawnEgg(CULT_RAIDER.get())));

    /** Rite binder: 0.6 x 1.95, hostile caster (matching vanilla evoker). */
    public static final DeferredHolder<EntityType<?>, EntityType<RiteBinder>> RITE_BINDER =
            ENTITY_TYPES.register("rite_binder", () ->
                    EntityType.Builder.<RiteBinder>of(RiteBinder::new, MobCategory.MONSTER)
                            .sized(0.6F, 1.95F)
                            .clientTrackingRange(8)
                            .notInPeaceful()
                            .build(ResourceKey.create(Registries.ENTITY_TYPE, EldritchHorror.id("rite_binder"))));

    public static final DeferredItem<SpawnEggItem> RITE_BINDER_SPAWN_EGG =
            ModItems.ITEMS.registerItem("rite_binder_spawn_egg",
                    properties -> new SpawnEggItem(properties.spawnEgg(RITE_BINDER.get())));

    /** Plague crone: 0.6 x 1.95, hostile debuffer (matching vanilla witch). */
    public static final DeferredHolder<EntityType<?>, EntityType<PlagueCrone>> PLAGUE_CRONE =
            ENTITY_TYPES.register("plague_crone", () ->
                    EntityType.Builder.<PlagueCrone>of(PlagueCrone::new, MobCategory.MONSTER)
                            .sized(0.6F, 1.95F)
                            .clientTrackingRange(8)
                            .notInPeaceful()
                            .build(ResourceKey.create(Registries.ENTITY_TYPE, EldritchHorror.id("plague_crone"))));

    public static final DeferredItem<SpawnEggItem> PLAGUE_CRONE_SPAWN_EGG =
            ModItems.ITEMS.registerItem("plague_crone_spawn_egg",
                    properties -> new SpawnEggItem(properties.spawnEgg(PLAGUE_CRONE.get())));

    // --- Passive (mundane) fauna (design/25 passive role table) ------------------------------
    // Registered through the compact register()/egg() helpers below; all are our namespace, so
    // MobSuppressor allows them. Sizes match the vanilla role each replaces.

    /** Deer: 0.9 x 1.4 (cow footprint). Family: mundane. */
    public static final DeferredHolder<EntityType<?>, EntityType<Deer>> DEER =
            register("deer", Deer::new, MobCategory.CREATURE, 0.9F, 1.4F, 10);
    public static final DeferredItem<SpawnEggItem> DEER_SPAWN_EGG = egg("deer", DEER);

    /** Wool hare: 0.9 x 1.0 (sheep-ish, smaller). Family: mundane. */
    public static final DeferredHolder<EntityType<?>, EntityType<WoolHare>> WOOL_HARE =
            register("wool_hare", WoolHare::new, MobCategory.CREATURE, 0.9F, 1.0F, 10);
    public static final DeferredItem<SpawnEggItem> WOOL_HARE_SPAWN_EGG = egg("wool_hare", WOOL_HARE);

    /** Mire sow: 0.9 x 1.0 (pig footprint). Family: mundane (taint vector). */
    public static final DeferredHolder<EntityType<?>, EntityType<MireSow>> MIRE_SOW =
            register("mire_sow", MireSow::new, MobCategory.CREATURE, 0.9F, 1.0F, 10);
    public static final DeferredItem<SpawnEggItem> MIRE_SOW_SPAWN_EGG = egg("mire_sow", MIRE_SOW);

    /** Ash fowl: 0.5 x 0.7 (chicken footprint). Family: mundane (warning). */
    public static final DeferredHolder<EntityType<?>, EntityType<AshFowl>> ASH_FOWL =
            register("ash_fowl", AshFowl::new, MobCategory.CREATURE, 0.5F, 0.7F, 10);
    public static final DeferredItem<SpawnEggItem> ASH_FOWL_SPAWN_EGG = egg("ash_fowl", ASH_FOWL);

    /** Burrowling: 0.4 x 0.5 (rabbit footprint). Family: mundane. */
    public static final DeferredHolder<EntityType<?>, EntityType<Burrowling>> BURROWLING =
            register("burrowling", Burrowling::new, MobCategory.CREATURE, 0.4F, 0.5F, 10);
    public static final DeferredItem<SpawnEggItem> BURROWLING_SPAWN_EGG = egg("burrowling", BURROWLING);

    /** Pack beast: 1.4 x 1.6 (horse-ish). Family: mundane (travel). */
    public static final DeferredHolder<EntityType<?>, EntityType<PackBeast>> PACK_BEAST =
            register("pack_beast", PackBeast::new, MobCategory.CREATURE, 1.4F, 1.6F, 10);
    public static final DeferredItem<SpawnEggItem> PACK_BEAST_SPAWN_EGG = egg("pack_beast", PACK_BEAST);

    /** Grey fox: 0.6 x 0.7 (fox footprint). Family: mundane (contrast). */
    public static final DeferredHolder<EntityType<?>, EntityType<GreyFox>> GREY_FOX =
            register("grey_fox", GreyFox::new, MobCategory.CREATURE, 0.6F, 0.7F, 10);
    public static final DeferredItem<SpawnEggItem> GREY_FOX_SPAWN_EGG = egg("grey_fox", GREY_FOX);

    /** Hill hound: 0.6 x 0.85 (wolf footprint). Family: mundane (taint vector). */
    public static final DeferredHolder<EntityType<?>, EntityType<HillHound>> HILL_HOUND =
            register("hill_hound", HillHound::new, MobCategory.CREATURE, 0.6F, 0.85F, 10);
    public static final DeferredItem<SpawnEggItem> HILL_HOUND_SPAWN_EGG = egg("hill_hound", HILL_HOUND);

    /** Hearth cat: 0.6 x 0.7 (cat footprint). Family: mundane (warning). */
    public static final DeferredHolder<EntityType<?>, EntityType<HearthCat>> HEARTH_CAT =
            register("hearth_cat", HearthCat::new, MobCategory.CREATURE, 0.6F, 0.7F, 10);
    public static final DeferredItem<SpawnEggItem> HEARTH_CAT_SPAWN_EGG = egg("hearth_cat", HEARTH_CAT);

    /** Wool beast: 0.9 x 1.87 (llama footprint). Family: mundane (travel). */
    public static final DeferredHolder<EntityType<?>, EntityType<WoolBeast>> WOOL_BEAST =
            register("wool_beast", WoolBeast::new, MobCategory.CREATURE, 0.9F, 1.87F, 10);
    public static final DeferredItem<SpawnEggItem> WOOL_BEAST_SPAWN_EGG = egg("wool_beast", WOOL_BEAST);

    /** Bog bear: 1.3 x 1.4 (panda footprint). Family: mundane (taint vector). */
    public static final DeferredHolder<EntityType<?>, EntityType<BogBear>> BOG_BEAR =
            register("bog_bear", BogBear::new, MobCategory.CREATURE, 1.3F, 1.4F, 10);
    public static final DeferredItem<SpawnEggItem> BOG_BEAR_SPAWN_EGG = egg("bog_bear", BOG_BEAR);

    /** Tide grazer: 1.2 x 0.4 (turtle footprint). Family: mundane (Choir contrast). */
    public static final DeferredHolder<EntityType<?>, EntityType<TideGrazer>> TIDE_GRAZER =
            register("tide_grazer", TideGrazer::new, MobCategory.CREATURE, 1.2F, 0.4F, 10);
    public static final DeferredItem<SpawnEggItem> TIDE_GRAZER_SPAWN_EGG = egg("tide_grazer", TIDE_GRAZER);

    /** Spore bee: 0.7 x 0.6 (bee footprint), tainted flier. Family: tainted. */
    public static final DeferredHolder<EntityType<?>, EntityType<SporeBee>> SPORE_BEE =
            register("spore_bee", SporeBee::new, MobCategory.CREATURE, 0.7F, 0.6F, 10);
    public static final DeferredItem<SpawnEggItem> SPORE_BEE_SPAWN_EGG = egg("spore_bee", SPORE_BEE);

    /** Stone sentinel: 1.4 x 2.7 (iron-golem footprint), Order construct. Family: order. */
    public static final DeferredHolder<EntityType<?>, EntityType<StoneSentinel>> STONE_SENTINEL =
            register("stone_sentinel", StoneSentinel::new, MobCategory.MISC, 1.4F, 2.7F, 10);
    public static final DeferredItem<SpawnEggItem> STONE_SENTINEL_SPAWN_EGG = egg("stone_sentinel", STONE_SENTINEL);

    // --- Ambient (ambience) fauna (design/25 ambient role table) ------------------------------
    // Non-combat drifters: peaceful (no notInPeaceful()), ambient/water_ambient where they fit the
    // vanilla role, creature for the marsh flier. Sizes match the vanilla role each replaces.

    /** Cave drifter: 0.5 x 0.9 (bat footprint), ambient flier. Family: ambient. */
    public static final DeferredHolder<EntityType<?>, EntityType<CaveDrifter>> CAVE_DRIFTER =
            register("cave_drifter", CaveDrifter::new, MobCategory.AMBIENT, 0.5F, 0.9F, 8);
    public static final DeferredItem<SpawnEggItem> CAVE_DRIFTER_SPAWN_EGG = egg("cave_drifter", CAVE_DRIFTER);

    /** Pale drifter: 0.8 x 0.8 (squid-ish), water ambient. Family: ambient. */
    public static final DeferredHolder<EntityType<?>, EntityType<PaleDrifter>> PALE_DRIFTER =
            register("pale_drifter", PaleDrifter::new, MobCategory.WATER_AMBIENT, 0.8F, 0.8F, 8);
    public static final DeferredItem<SpawnEggItem> PALE_DRIFTER_SPAWN_EGG = egg("pale_drifter", PALE_DRIFTER);

    /** Lantern jelly: 0.8 x 0.8 (glow-squid role), water ambient. Family: ambient. */
    public static final DeferredHolder<EntityType<?>, EntityType<LanternJelly>> LANTERN_JELLY =
            register("lantern_jelly", LanternJelly::new, MobCategory.WATER_AMBIENT, 0.8F, 0.8F, 8);
    public static final DeferredItem<SpawnEggItem> LANTERN_JELLY_SPAWN_EGG = egg("lantern_jelly", LANTERN_JELLY);

    /** Marsh mote: 0.5 x 0.5 (allay/tadpole scale), creature flier. Family: ambient. */
    public static final DeferredHolder<EntityType<?>, EntityType<MarshMote>> MARSH_MOTE =
            register("marsh_mote", MarshMote::new, MobCategory.CREATURE, 0.5F, 0.5F, 8);
    public static final DeferredItem<SpawnEggItem> MARSH_MOTE_SPAWN_EGG = egg("marsh_mote", MARSH_MOTE);

    /** Drowned minnow: 0.5 x 0.4 (fish footprint), water ambient. Family: ambient. */
    public static final DeferredHolder<EntityType<?>, EntityType<DrownedMinnow>> DROWNED_MINNOW =
            register("drowned_minnow", DrownedMinnow::new, MobCategory.WATER_AMBIENT, 0.5F, 0.4F, 8);
    public static final DeferredItem<SpawnEggItem> DROWNED_MINNOW_SPAWN_EGG = egg("drowned_minnow", DROWNED_MINNOW);

    /** Frost wisp: 0.4 x 0.4 (motey), ambient flier. Family: ambient. */
    public static final DeferredHolder<EntityType<?>, EntityType<FrostWisp>> FROST_WISP =
            register("frost_wisp", FrostWisp::new, MobCategory.AMBIENT, 0.4F, 0.4F, 8);
    public static final DeferredItem<SpawnEggItem> FROST_WISP_SPAWN_EGG = egg("frost_wisp", FROST_WISP);

    private static <T extends net.minecraft.world.entity.Mob> DeferredHolder<EntityType<?>, EntityType<T>> register(
            String id, net.minecraft.world.entity.EntityType.EntityFactory<T> factory, MobCategory category,
            float width, float height, int trackingRange) {
        return ENTITY_TYPES.register(id, () ->
                EntityType.Builder.of(factory, category)
                        .sized(width, height)
                        .clientTrackingRange(trackingRange)
                        .build(ResourceKey.create(Registries.ENTITY_TYPE, EldritchHorror.id(id))));
    }

    private static DeferredItem<SpawnEggItem> egg(
            String id, DeferredHolder<EntityType<?>, ? extends EntityType<?>> type) {
        return ModItems.ITEMS.registerItem(id + "_spawn_egg",
                properties -> new SpawnEggItem(properties.spawnEgg(type.get())));
    }

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

    /** Azathoth: 3.5 x 3.5, the rift_scar presence that has no melee (replaces the Wither). */
    public static final DeferredHolder<EntityType<?>, EntityType<Azathoth>> AZATHOTH =
            ENTITY_TYPES.register("azathoth", () ->
                    EntityType.Builder.<Azathoth>of(Azathoth::new, MobCategory.MONSTER)
                            .sized(3.5F, 3.5F)
                            .clientTrackingRange(12)
                            .notInPeaceful()
                            .build(ResourceKey.create(Registries.ENTITY_TYPE, EldritchHorror.id("azathoth"))));

    public static final DeferredItem<SpawnEggItem> AZATHOTH_SPAWN_EGG =
            ModItems.ITEMS.registerItem("azathoth_spawn_egg",
                    properties -> new SpawnEggItem(properties.spawnEgg(AZATHOTH.get())));

    /** Yog-Sothoth: 4.0 x 6.0, the gate on the observatory plateau (replaces the Ender Dragon). */
    public static final DeferredHolder<EntityType<?>, EntityType<YogSothoth>> YOG_SOTHOTH =
            ENTITY_TYPES.register("yog_sothoth", () ->
                    EntityType.Builder.<YogSothoth>of(YogSothoth::new, MobCategory.MONSTER)
                            .sized(4.0F, 6.0F)
                            .clientTrackingRange(14)
                            .notInPeaceful()
                            .build(ResourceKey.create(Registries.ENTITY_TYPE, EldritchHorror.id("yog_sothoth"))));

    public static final DeferredItem<SpawnEggItem> YOG_SOTHOTH_SPAWN_EGG =
            ModItems.ITEMS.registerItem("yog_sothoth_spawn_egg",
                    properties -> new SpawnEggItem(properties.spawnEgg(YOG_SOTHOTH.get())));

    /** Ithaqua: 0.9 x 2.9, the walking wind of the cold edge (replaces the Warden). */
    public static final DeferredHolder<EntityType<?>, EntityType<Ithaqua>> ITHAQUA =
            ENTITY_TYPES.register("ithaqua", () ->
                    EntityType.Builder.<Ithaqua>of(Ithaqua::new, MobCategory.MONSTER)
                            .sized(0.9F, 2.9F)
                            .clientTrackingRange(10)
                            .notInPeaceful()
                            .build(ResourceKey.create(Registries.ENTITY_TYPE, EldritchHorror.id("ithaqua"))));

    public static final DeferredItem<SpawnEggItem> ITHAQUA_SPAWN_EGG =
            ModItems.ITEMS.registerItem("ithaqua_spawn_egg",
                    properties -> new SpawnEggItem(properties.spawnEgg(ITHAQUA.get())));

    /** Yig: 1.4 x 1.4, the ashen_waste presence that escalates when struck. */
    public static final DeferredHolder<EntityType<?>, EntityType<Yig>> YIG =
            ENTITY_TYPES.register("yig", () ->
                    EntityType.Builder.<Yig>of(Yig::new, MobCategory.MONSTER)
                            .sized(1.4F, 1.4F)
                            .clientTrackingRange(10)
                            .notInPeaceful()
                            .build(ResourceKey.create(Registries.ENTITY_TYPE, EldritchHorror.id("yig"))));

    public static final DeferredItem<SpawnEggItem> YIG_SPAWN_EGG =
            ModItems.ITEMS.registerItem("yig_spawn_egg",
                    properties -> new SpawnEggItem(properties.spawnEgg(YIG.get())));

    /** Atlach-Nacha: 2.0 x 1.2, the rift_scar weaver (a large spider). */
    public static final DeferredHolder<EntityType<?>, EntityType<AtlachNacha>> ATLACH_NACHA =
            ENTITY_TYPES.register("atlach_nacha", () ->
                    EntityType.Builder.<AtlachNacha>of(AtlachNacha::new, MobCategory.MONSTER)
                            .sized(2.0F, 1.2F)
                            .clientTrackingRange(10)
                            .notInPeaceful()
                            .build(ResourceKey.create(Registries.ENTITY_TYPE, EldritchHorror.id("atlach_nacha"))));

    public static final DeferredItem<SpawnEggItem> ATLACH_NACHA_SPAWN_EGG =
            ModItems.ITEMS.registerItem("atlach_nacha_spawn_egg",
                    properties -> new SpawnEggItem(properties.spawnEgg(ATLACH_NACHA.get())));

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
            event.accept(WORSHIPPER_SPAWN_EGG.get());
            event.accept(CULT_ZEALOT_SPAWN_EGG.get());
            event.accept(CULT_RAIDER_SPAWN_EGG.get());
            event.accept(RITE_BINDER_SPAWN_EGG.get());
            event.accept(PLAGUE_CRONE_SPAWN_EGG.get());
            event.accept(DEER_SPAWN_EGG.get());
            event.accept(WOOL_HARE_SPAWN_EGG.get());
            event.accept(MIRE_SOW_SPAWN_EGG.get());
            event.accept(ASH_FOWL_SPAWN_EGG.get());
            event.accept(BURROWLING_SPAWN_EGG.get());
            event.accept(PACK_BEAST_SPAWN_EGG.get());
            event.accept(GREY_FOX_SPAWN_EGG.get());
            event.accept(HILL_HOUND_SPAWN_EGG.get());
            event.accept(HEARTH_CAT_SPAWN_EGG.get());
            event.accept(WOOL_BEAST_SPAWN_EGG.get());
            event.accept(BOG_BEAR_SPAWN_EGG.get());
            event.accept(TIDE_GRAZER_SPAWN_EGG.get());
            event.accept(SPORE_BEE_SPAWN_EGG.get());
            event.accept(STONE_SENTINEL_SPAWN_EGG.get());
            event.accept(CAVE_DRIFTER_SPAWN_EGG.get());
            event.accept(PALE_DRIFTER_SPAWN_EGG.get());
            event.accept(LANTERN_JELLY_SPAWN_EGG.get());
            event.accept(MARSH_MOTE_SPAWN_EGG.get());
            event.accept(DROWNED_MINNOW_SPAWN_EGG.get());
            event.accept(FROST_WISP_SPAWN_EGG.get());
            event.accept(CTHULHU_SPAWN_EGG.get());
            event.accept(DUNWICH_HORROR_SPAWN_EGG.get());
            event.accept(SHUB_NIGGURATH_SPAWN_EGG.get());
            event.accept(AZATHOTH_SPAWN_EGG.get());
            event.accept(YOG_SOTHOTH_SPAWN_EGG.get());
            event.accept(ITHAQUA_SPAWN_EGG.get());
            event.accept(YIG_SPAWN_EGG.get());
            event.accept(ATLACH_NACHA_SPAWN_EGG.get());
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
        event.put(WORSHIPPER.get(), Worshipper.createAttributes().build());
        event.put(CULT_ZEALOT.get(), CultZealot.createAttributes().build());
        event.put(CULT_RAIDER.get(), CultRaider.createAttributes().build());
        event.put(RITE_BINDER.get(), RiteBinder.createAttributes().build());
        event.put(PLAGUE_CRONE.get(), PlagueCrone.createAttributes().build());
        event.put(DEER.get(), Deer.createAttributes().build());
        event.put(WOOL_HARE.get(), WoolHare.createAttributes().build());
        event.put(MIRE_SOW.get(), MireSow.createAttributes().build());
        event.put(ASH_FOWL.get(), AshFowl.createAttributes().build());
        event.put(BURROWLING.get(), Burrowling.createAttributes().build());
        event.put(PACK_BEAST.get(), PackBeast.createAttributes().build());
        event.put(GREY_FOX.get(), GreyFox.createAttributes().build());
        event.put(HILL_HOUND.get(), HillHound.createAttributes().build());
        event.put(HEARTH_CAT.get(), HearthCat.createAttributes().build());
        event.put(WOOL_BEAST.get(), WoolBeast.createAttributes().build());
        event.put(BOG_BEAR.get(), BogBear.createAttributes().build());
        event.put(TIDE_GRAZER.get(), TideGrazer.createAttributes().build());
        event.put(SPORE_BEE.get(), SporeBee.createAttributes().build());
        event.put(STONE_SENTINEL.get(), StoneSentinel.createAttributes().build());
        event.put(CAVE_DRIFTER.get(), CaveDrifter.createAttributes().build());
        event.put(PALE_DRIFTER.get(), PaleDrifter.createAttributes().build());
        event.put(LANTERN_JELLY.get(), LanternJelly.createAttributes().build());
        event.put(MARSH_MOTE.get(), MarshMote.createAttributes().build());
        event.put(DROWNED_MINNOW.get(), DrownedMinnow.createAttributes().build());
        event.put(FROST_WISP.get(), FrostWisp.createAttributes().build());
        event.put(CTHULHU.get(), Cthulhu.createAttributes().build());
        event.put(DUNWICH_HORROR.get(), DunwichHorror.createAttributes().build());
        event.put(SHUB_NIGGURATH.get(), ShubNiggurath.createAttributes().build());
        event.put(AZATHOTH.get(), Azathoth.createAttributes().build());
        event.put(YOG_SOTHOTH.get(), YogSothoth.createAttributes().build());
        event.put(ITHAQUA.get(), Ithaqua.createAttributes().build());
        event.put(YIG.get(), Yig.createAttributes().build());
        event.put(ATLACH_NACHA.get(), AtlachNacha.createAttributes().build());
    }

    private ModEntities() {
    }
}
