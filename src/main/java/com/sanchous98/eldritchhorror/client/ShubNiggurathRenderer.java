package com.sanchous98.eldritchhorror.client;

import com.sanchous98.eldritchhorror.entity.ShubNiggurath;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.ravager.RavagerModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.RavagerRenderState;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Placeholder renderer for Shub-Niggurath: the vanilla ravager quadruped model, with a flat
 * placeholder texture (128x128 ravager sheet). Art is the user's job.
 *
 * <p>Note: the model draws at its vanilla ravager size regardless of the large boss collision box;
 * a truly huge silhouette needs a custom model or the {@code scale} attribute, both deferred to art.
 *
 * <p>26.3 verified as for {@link CthulhuRenderer}: {@code RavagerModel} + {@code RavagerRenderState}.
 */
@OnlyIn(Dist.CLIENT)
public final class ShubNiggurathRenderer
        extends MobRenderer<ShubNiggurath, RavagerRenderState, RavagerModel> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath("eldritch_horror", "textures/entity/shub_niggurath.png");

    public ShubNiggurathRenderer(EntityRendererProvider.Context context) {
        super(context, new RavagerModel(context.bakeLayer(ModelLayers.RAVAGER)), 2.0F);
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
