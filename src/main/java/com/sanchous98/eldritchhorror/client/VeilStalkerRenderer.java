package com.sanchous98.eldritchhorror.client;

import com.sanchous98.eldritchhorror.entity.VeilStalker;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.spider.SpiderModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;

/**
 * Placeholder renderer for the veil stalker: a {@link MobRenderer} over the vanilla spider model,
 * with our own flat placeholder texture (64x32, the spider sheet layout). Reuses the baked
 * {@link ModelLayers#SPIDER} layer and keeps the spider's 180-degree flip so it reads on wall and
 * ceiling. Art is the user's job.
 *
 * <p>26.3 verified: {@code SpiderModel extends EntityModel<LivingEntityRenderState>}, so
 * {@link LivingEntityRenderState} is the matching render state; vanilla {@code SpiderRenderer} is the
 * same shape. The {@code SpiderEyesLayer} is not added — we have no eye texture yet.
 */
public final class VeilStalkerRenderer
        extends MobRenderer<VeilStalker, LivingEntityRenderState, SpiderModel> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath("eldritch_horror", "textures/entity/veil_stalker.png");

    public VeilStalkerRenderer(EntityRendererProvider.Context context) {
        super(context, new SpiderModel(context.bakeLayer(ModelLayers.SPIDER)), 0.6F);
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
