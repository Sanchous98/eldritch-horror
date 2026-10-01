package com.sanchous98.eldritchhorror.client;

import com.sanchous98.eldritchhorror.entity.RiftMite;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.endermite.EndermiteModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Placeholder renderer for the rift mite: a {@link MobRenderer} over the vanilla endermite model,
 * with our own flat placeholder texture (64x32, the endermite sheet layout). The lesser swarm
 * already uses the silverfish model, so the mite uses the other tiny vermin model to keep the two
 * silhouettes distinct. Art is the user's job.
 *
 * <p>26.3 verified: {@code EndermiteModel extends EntityModel<EntityRenderState>} and vanilla
 * {@code EndermiteRenderer} is {@code MobRenderer<Endermite, LivingEntityRenderState,
 * EndermiteModel>}; we mirror it with our own entity type and texture. The 180-degree flip is kept so
 * it skitters on walls/ceilings.
 */
@OnlyIn(Dist.CLIENT)
public final class RiftMiteRenderer
        extends MobRenderer<RiftMite, LivingEntityRenderState, EndermiteModel> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath("eldritch_horror", "textures/entity/rift_mite.png");

    public RiftMiteRenderer(EntityRendererProvider.Context context) {
        super(context, new EndermiteModel(context.bakeLayer(ModelLayers.ENDERMITE)), 0.3F);
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
