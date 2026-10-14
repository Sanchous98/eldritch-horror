package com.sanchous98.eldritchhorror.client;

import com.sanchous98.eldritchhorror.entity.AshenRevenant;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.skeleton.SkeletonModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.state.SkeletonRenderState;
import net.minecraft.resources.Identifier;

/**
 * Placeholder renderer for the ashen revenant: a {@link HumanoidMobRenderer} over the vanilla
 * skeleton model, with our own charcoal-and-ember texture (64x32, the skeleton sheet layout). It is
 * the bone choir's renderer shape, so the arms raise while it hunts. Art is the user's job.
 */
public final class AshenRevenantRenderer
        extends HumanoidMobRenderer<AshenRevenant, SkeletonRenderState, SkeletonModel<SkeletonRenderState>> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath("eldritch_horror", "textures/entity/ashen_revenant.png");

    public AshenRevenantRenderer(EntityRendererProvider.Context context) {
        super(context, new SkeletonModel<>(context.bakeLayer(ModelLayers.SKELETON)), 0.5F);
    }

    @Override
    public SkeletonRenderState createRenderState() {
        return new SkeletonRenderState();
    }

    @Override
    public void extractRenderState(AshenRevenant entity, SkeletonRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.isAggressive = entity.isAggressive();
    }

    @Override
    public Identifier getTextureLocation(SkeletonRenderState state) {
        return TEXTURE;
    }
}
