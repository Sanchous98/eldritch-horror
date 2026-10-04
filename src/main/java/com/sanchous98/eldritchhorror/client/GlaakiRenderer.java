package com.sanchous98.eldritchhorror.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.sanchous98.eldritchhorror.entity.Glaaki;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.slime.SlimeModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.SlimeRenderState;
import net.minecraft.resources.Identifier;

/**
 * Placeholder renderer for Glaaki: the vanilla slime model (a green half-drowned mass) with our own
 * flat placeholder texture (64x32 slime sheet). Art is the user's job.
 *
 * <p>26.3 verified as for {@code ShoggothMassRenderer}: {@code SlimeModel} bakes a single cube, so a
 * fixed {@code SlimeRenderState.size} plus the {@link #scale} override draws a body matching the
 * 1.6-block hitbox without a custom layer.
 */
public final class GlaakiRenderer extends MobRenderer<Glaaki, SlimeRenderState, SlimeModel> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath("eldritch_horror", "textures/entity/glaaki.png");

    public GlaakiRenderer(EntityRendererProvider.Context context) {
        super(context, new SlimeModel(context.bakeLayer(ModelLayers.SLIME)), 0.8F);
    }

    @Override
    public SlimeRenderState createRenderState() {
        return new SlimeRenderState();
    }

    @Override
    public void extractRenderState(Glaaki entity, SlimeRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.size = 2;
        state.squish = 0.0F;
    }

    @Override
    protected void scale(SlimeRenderState state, PoseStack poseStack) {
        super.scale(state, poseStack);
        float size = state.size;
        poseStack.scale(size, size, size);
    }

    @Override
    public Identifier getTextureLocation(SlimeRenderState state) {
        return TEXTURE;
    }
}
