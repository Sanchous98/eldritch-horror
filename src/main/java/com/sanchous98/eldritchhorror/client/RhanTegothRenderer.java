package com.sanchous98.eldritchhorror.client;

import com.sanchous98.eldritchhorror.entity.RhanTegoth;
import net.minecraft.client.model.animal.golem.IronGolemModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.IronGolemRenderState;
import net.minecraft.resources.Identifier;

/**
 * Placeholder renderer for Rhan-Tegoth: the vanilla iron golem model (a standing carved shape, which
 * is closer to an idol on a plinth than any quadruped) with our own flat placeholder texture (128x128
 * golem sheet). Art is the user's job.
 *
 * <p>26.3 verified as for {@code PassiveRenderers.StoneSentinelRenderer}: {@code IronGolemModel} +
 * {@code IronGolemRenderState}, only {@code createRenderState()} and {@code getTextureLocation} to
 * implement.
 */
public final class RhanTegothRenderer
        extends MobRenderer<RhanTegoth, IronGolemRenderState, IronGolemModel> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath("eldritch_horror", "textures/entity/rhan_tegoth.png");

    public RhanTegothRenderer(EntityRendererProvider.Context context) {
        super(context, new IronGolemModel(context.bakeLayer(ModelLayers.IRON_GOLEM)), 1.2F);
    }

    @Override
    public IronGolemRenderState createRenderState() {
        return new IronGolemRenderState();
    }

    @Override
    public Identifier getTextureLocation(IronGolemRenderState state) {
        return TEXTURE;
    }
}
