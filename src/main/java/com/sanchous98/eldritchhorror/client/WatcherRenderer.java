package com.sanchous98.eldritchhorror.client;

import com.sanchous98.eldritchhorror.entity.Watcher;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.enderman.EndermanModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.state.EndermanRenderState;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Placeholder renderer for the Watcher: a {@link HumanoidMobRenderer} over the vanilla enderman
 * model, with our own flat placeholder texture (64x32, the enderman sheet layout). The tall, thin
 * enderman silhouette is the "presence you see at the end of the street" we want; art is the user's
 * job, so we reuse the existing baked {@link ModelLayers#ENDERMAN} layer.
 *
 * <p>26.3 verified: {@code EndermanModel<T> extends HumanoidModel<T>} and
 * {@code HumanoidMobRenderer<T, S, M extends HumanoidModel<S>>}, so
 * {@code HumanoidMobRenderer<Watcher, EndermanRenderState, EndermanModel<EndermanRenderState>>} is
 * the correct shape. {@code EndermanRenderState} extends {@code HumanoidRenderState}.
 */
@OnlyIn(Dist.CLIENT)
public final class WatcherRenderer
        extends HumanoidMobRenderer<Watcher, EndermanRenderState, EndermanModel<EndermanRenderState>> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath("eldritch_horror", "textures/entity/watcher.png");

    public WatcherRenderer(EntityRendererProvider.Context context) {
        super(context, new EndermanModel<>(context.bakeLayer(ModelLayers.ENDERMAN)), 0.5F);
    }

    @Override
    public EndermanRenderState createRenderState() {
        return new EndermanRenderState();
    }

    @Override
    public void extractRenderState(Watcher entity, EndermanRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        // No isCreepy: the Watcher does not flicker like an enderman. It holds still and is looked at.
    }

    @Override
    public Identifier getTextureLocation(EndermanRenderState state) {
        return TEXTURE;
    }
}
