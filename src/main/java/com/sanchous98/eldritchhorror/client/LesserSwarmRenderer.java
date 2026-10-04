package com.sanchous98.eldritchhorror.client;

import com.sanchous98.eldritchhorror.entity.LesserSwarm;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.silverfish.SilverfishModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;

/**
 * Placeholder renderer for the lesser swarm: a {@link MobRenderer} over the vanilla silverfish
 * model, with our own flat placeholder texture (64x32, the silverfish sheet layout). Reuses the
 * existing baked {@link ModelLayers#SILVERFISH} layer.
 *
 * <p>26.3 verified: vanilla {@code SilverfishRenderer} is
 * {@code MobRenderer<Silverfish, LivingEntityRenderState, SilverfishModel>}; we mirror it with our
 * own entity type and texture. The 180-degree flip is kept so it skitters on walls/ceilings.
 */
public final class LesserSwarmRenderer
        extends MobRenderer<LesserSwarm, LivingEntityRenderState, SilverfishModel> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath("eldritch_horror", "textures/entity/lesser_swarm.png");

    public LesserSwarmRenderer(EntityRendererProvider.Context context) {
        super(context, new SilverfishModel(context.bakeLayer(ModelLayers.SILVERFISH)), 0.3F);
    }

    @Override
    protected float getFlipDegrees() {
        return 180.0F;
    }

    @Override
    public LivingEntityRenderState createRenderState() {
        return new LivingEntityRenderState();
    }

    @Override
    public Identifier getTextureLocation(LivingEntityRenderState state) {
        return TEXTURE;
    }
}
