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
    }
}
