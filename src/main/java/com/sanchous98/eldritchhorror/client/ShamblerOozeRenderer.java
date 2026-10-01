package com.sanchous98.eldritchhorror.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.sanchous98.eldritchhorror.entity.ShamblerOoze;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.slime.SlimeModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.SlimeRenderState;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Placeholder renderer for the shambler ooze: a {@link MobRenderer} over the vanilla slime model,
 * with our own flat placeholder texture (64x32, the slime sheet layout). It mirrors
 * {@code ShoggothMassRenderer}: the fixed single-cube model needs the size applied as a pose scale.
 * Here the size comes from the entity (a split copy is smaller), so the recursion reads visually.
 * Art is the user's job.
 *
 * <p>26.3 verified: {@code SlimeModel extends EntityModel<EntityRenderState>}; the {@link #scale}
 * override reproduces vanilla's {@code AbstractCubeMobRenderer.applySizeAndSquish}.
 */
@OnlyIn(Dist.CLIENT)
public final class ShamblerOozeRenderer
        extends MobRenderer<ShamblerOoze, SlimeRenderState, SlimeModel> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath("eldritch_horror", "textures/entity/shambler_ooze.png");

    public ShamblerOozeRenderer(EntityRendererProvider.Context context) {
        super(context, new SlimeModel(context.bakeLayer(ModelLayers.SLIME)), 0.5F);
    }

    @Override
    public SlimeRenderState createRenderState() {
        return new SlimeRenderState();
    }

    @Override
    public void extractRenderState(ShamblerOoze entity, SlimeRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.size = entity.getOozeSize() + 1;
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
