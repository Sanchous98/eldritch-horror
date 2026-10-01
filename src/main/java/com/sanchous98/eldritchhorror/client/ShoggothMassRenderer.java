package com.sanchous98.eldritchhorror.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.sanchous98.eldritchhorror.entity.ShoggothMass;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.slime.SlimeModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.SlimeRenderState;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Placeholder renderer for the shoggoth mass: a {@link MobRenderer} over the vanilla slime model,
 * with our own flat placeholder texture (64x32, the slime sheet layout). Art is the user's job.
 *
 * <p>26.3 verified: {@code SlimeModel extends EntityModel<EntityRenderState>} and its
 * {@code createOuterBodyLayer} bakes a single cube, so a fixed {@link SlimeRenderState} with
 * {@code size = 3} draws a large blob. We extend the plain {@code MobRenderer} rather than vanilla's
 * {@code AbstractCubeMobRenderer} (our class is not an {@code AbstractCubeMob}); the {@link #scale}
 * override reproduces that renderer's size application so the model matches the 1.6-block hitbox.
 */
@OnlyIn(Dist.CLIENT)
public final class ShoggothMassRenderer extends MobRenderer<ShoggothMass, SlimeRenderState, SlimeModel> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath("eldritch_horror", "textures/entity/shoggoth_mass.png");

    public ShoggothMassRenderer(EntityRendererProvider.Context context) {
        super(context, new SlimeModel(context.bakeLayer(ModelLayers.SLIME)), 0.6F);
    }

    @Override
    public SlimeRenderState createRenderState() {
        return new SlimeRenderState();
    }

    @Override
    public void extractRenderState(ShoggothMass entity, SlimeRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.size = 3;
        state.squish = 0.0F;
    }

    /**
     * The slime model is a fixed single cube, so the size must be applied as a pose scale (what
     * vanilla's {@code AbstractCubeMobRenderer.applySizeAndSquish} does); setting
     * {@link SlimeRenderState#size} alone is not consumed by this renderer.
     */
    @Override
    protected void scale(SlimeRenderState state, PoseStack poseStack) {
        super.scale(state, poseStack);
        float size = state.size;
        float squish = 1.0F / (state.squish / (size * 0.5F + 1.0F) + 1.0F);
        poseStack.scale(squish * size, 1.0F / squish * size, squish * size);
    }

    @Override
    public Identifier getTextureLocation(SlimeRenderState state) {
        return TEXTURE;
    }
}
