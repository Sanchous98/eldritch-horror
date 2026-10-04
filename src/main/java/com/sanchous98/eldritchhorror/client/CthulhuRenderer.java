package com.sanchous98.eldritchhorror.client;

import com.sanchous98.eldritchhorror.entity.Cthulhu;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.ravager.RavagerModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.RavagerRenderState;
import net.minecraft.resources.Identifier;

/**
 * Placeholder renderer for Cthulhu: a {@link MobRenderer} over the vanilla ravager quadruped model,
 * with our own flat placeholder texture. Art is the user's job; this reuses the existing baked
 * {@link ModelLayers#RAVAGER} layer (128x128 sheet) rather than registering a custom one.
 *
 * <p>26.3 verified: {@code RavagerModel extends EntityModel<RavagerRenderState>} and
 * {@code MobRenderer<T, S, M extends EntityModel<S>>}, so the shape is
 * {@code MobRenderer<Cthulhu, RavagerRenderState, RavagerModel>}; only {@code createRenderState()}
 * and {@code getTextureLocation(state)} are abstract. The ravager animation fields default to 0
 * here (no custom extract), which reads as a slow lumbering walk — acceptable for a placeholder.
 */
public final class CthulhuRenderer extends MobRenderer<Cthulhu, RavagerRenderState, RavagerModel> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath("eldritch_horror", "textures/entity/cthulhu.png");

    public CthulhuRenderer(EntityRendererProvider.Context context) {
        super(context, new RavagerModel(context.bakeLayer(ModelLayers.RAVAGER)), 1.6F);
    }

    @Override
    public RavagerRenderState createRenderState() {
        return new RavagerRenderState();
    }

    @Override
    public Identifier getTextureLocation(RavagerRenderState state) {
        return TEXTURE;
    }
}
