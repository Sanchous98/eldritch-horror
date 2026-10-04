package com.sanchous98.eldritchhorror.client;

import com.sanchous98.eldritchhorror.entity.BoneChoir;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.skeleton.SkeletonModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.state.SkeletonRenderState;
import net.minecraft.resources.Identifier;

/**
 * Placeholder renderer for the bone choir: a {@link HumanoidMobRenderer} over the vanilla skeleton
 * model, with our own flat placeholder texture (64x32, the skeleton sheet layout). Reuses the baked
 * {@link ModelLayers#SKELETON} layer. Art is the user's job.
 *
 * <p>26.3 verified: {@code SkeletonModel<S extends SkeletonRenderState> extends HumanoidModel<S>},
 * and vanilla {@code SkeletonRenderer} is an {@code AbstractSkeletonRenderer}; we take the plain
 * {@code HumanoidMobRenderer} shape and set {@code isAggressive} ourselves so the arms raise.
 */
public final class BoneChoirRenderer
        extends HumanoidMobRenderer<BoneChoir, SkeletonRenderState, SkeletonModel<SkeletonRenderState>> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath("eldritch_horror", "textures/entity/bone_choir.png");

    public BoneChoirRenderer(EntityRendererProvider.Context context) {
        super(context, new SkeletonModel<>(context.bakeLayer(ModelLayers.SKELETON)), 0.5F);
    }

    @Override
    public SkeletonRenderState createRenderState() {
        return new SkeletonRenderState();
    }

    @Override
    public void extractRenderState(BoneChoir entity, SkeletonRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.isAggressive = entity.isAggressive();
    }

    @Override
    public Identifier getTextureLocation(SkeletonRenderState state) {
        return TEXTURE;
    }
}
