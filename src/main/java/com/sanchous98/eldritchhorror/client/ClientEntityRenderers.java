package com.sanchous98.eldritchhorror.client;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.registry.ModEntities;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/**
 * Client-only entity renderer registration. Guarded by {@code @EventBusSubscriber(value = Dist.CLIENT)}
 * so the dedicated server never loads {@link RisenHuskRenderer} or any of the client model classes
 * it references (the whole renderer class is {@code @OnlyIn(Dist.CLIENT)}).
 *
 * <p>26.3 verified: {@code EntityRenderersEvent.RegisterRenderers} implements {@code IModBusEvent}
 * and exposes {@code registerEntityRenderer(EntityType, EntityRendererProvider)}.
 */
@EventBusSubscriber(modid = EldritchHorror.MODID, value = Dist.CLIENT)
public final class ClientEntityRenderers {

    private ClientEntityRenderers() {
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.RISEN_HUSK.get(), RisenHuskRenderer::new);
        event.registerEntityRenderer(ModEntities.TAINTED_FAUNA.get(), TaintedFaunaRenderer::new);
        event.registerEntityRenderer(ModEntities.LESSER_SWARM.get(), LesserSwarmRenderer::new);
        event.registerEntityRenderer(ModEntities.WATCHER.get(), WatcherRenderer::new);
        event.registerEntityRenderer(ModEntities.BONE_CHOIR.get(), BoneChoirRenderer::new);
        event.registerEntityRenderer(ModEntities.DROWNED_THRALL.get(), DrownedThrallRenderer::new);
        event.registerEntityRenderer(ModEntities.VEIL_STALKER.get(), VeilStalkerRenderer::new);
        event.registerEntityRenderer(ModEntities.BLIGHT_POD.get(), BlightPodRenderer::new);
        event.registerEntityRenderer(ModEntities.BYAKHEE.get(), ByakheeRenderer::new);
        event.registerEntityRenderer(ModEntities.STAR_SPAWN.get(), StarSpawnRenderer::new);
        event.registerEntityRenderer(ModEntities.SHOGGOTH_MASS.get(), ShoggothMassRenderer::new);
        event.registerEntityRenderer(ModEntities.WEAVER_SPAWN.get(), WeaverSpawnRenderer::new);
        event.registerEntityRenderer(ModEntities.RIFT_MITE.get(), RiftMiteRenderer::new);
        event.registerEntityRenderer(ModEntities.SHAMBLER_OOZE.get(), ShamblerOozeRenderer::new);
        event.registerEntityRenderer(ModEntities.CHOIR_SPITE.get(), ChoirSpiteRenderer::new);
        event.registerEntityRenderer(ModEntities.NIGHT_HAG.get(), NightHagRenderer::new);
        event.registerEntityRenderer(ModEntities.WORSHIPPER.get(), WorshipperRenderer::new);
        event.registerEntityRenderer(ModEntities.CULT_ZEALOT.get(), CultistRenderers.Zealot::new);
        event.registerEntityRenderer(ModEntities.CULT_RAIDER.get(), CultistRenderers.Raider::new);
        event.registerEntityRenderer(ModEntities.RITE_BINDER.get(), RiteBinderRenderer::new);
        event.registerEntityRenderer(ModEntities.PLAGUE_CRONE.get(), PlagueCroneRenderer::new);
        event.registerEntityRenderer(ModEntities.DEER.get(), PassiveRenderers.DeerRenderer::new);
        event.registerEntityRenderer(ModEntities.WOOL_HARE.get(), PassiveRenderers.WoolHareRenderer::new);
        event.registerEntityRenderer(ModEntities.MIRE_SOW.get(), PassiveRenderers.MireSowRenderer::new);
        event.registerEntityRenderer(ModEntities.ASH_FOWL.get(), PassiveRenderers.AshFowlRenderer::new);
        event.registerEntityRenderer(ModEntities.BURROWLING.get(), PassiveRenderers.BurrowlingRenderer::new);
        event.registerEntityRenderer(ModEntities.PACK_BEAST.get(), PassiveRenderers.PackBeastRenderer::new);
        event.registerEntityRenderer(ModEntities.GREY_FOX.get(), PassiveRenderers.GreyFoxRenderer::new);
        event.registerEntityRenderer(ModEntities.HILL_HOUND.get(), PassiveRenderers.HillHoundRenderer::new);
        event.registerEntityRenderer(ModEntities.HEARTH_CAT.get(), PassiveRenderers.HearthCatRenderer::new);
        event.registerEntityRenderer(ModEntities.WOOL_BEAST.get(), PassiveRenderers.WoolBeastRenderer::new);
        event.registerEntityRenderer(ModEntities.BOG_BEAR.get(), PassiveRenderers.BogBearRenderer::new);
        event.registerEntityRenderer(ModEntities.TIDE_GRAZER.get(), PassiveRenderers.TideGrazerRenderer::new);
        event.registerEntityRenderer(ModEntities.SPORE_BEE.get(), PassiveRenderers.SporeBeeRenderer::new);
        event.registerEntityRenderer(ModEntities.STONE_SENTINEL.get(), PassiveRenderers.StoneSentinelRenderer::new);
        event.registerEntityRenderer(ModEntities.CTHULHU.get(), CthulhuRenderer::new);
        event.registerEntityRenderer(ModEntities.DUNWICH_HORROR.get(), DunwichHorrorRenderer::new);
        event.registerEntityRenderer(ModEntities.SHUB_NIGGURATH.get(), ShubNiggurathRenderer::new);
    }
}
