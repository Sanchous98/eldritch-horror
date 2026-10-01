package com.sanchous98.eldritchhorror.client;

import com.sanchous98.eldritchhorror.entity.WeaverSpawn;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.spider.SpiderModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Placeholder renderer for the weaver spawn: a {@link MobRenderer} over the vanilla spider model,
 * with our own flat placeholder texture (64x32, the spider sheet layout). We reuse the baked
 * {@link ModelLayers#SPIDER} layer as the veil stalker does; the two are distinguished by texture and
 * behaviour, not silhouette. Art is the user's job.
 *
 * <p>26.3 verified: {@code SpiderModel extends EntityModel<LivingEntityRenderState>}; the
 * 180-degree flip is kept so it reads on wall and ceiling.
 */
@OnlyIn(Dist.CLIENT)
public final class WeaverSpawnRenderer
        extends MobRenderer<WeaverSpawn, LivingEntityRenderState, SpiderModel> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath("eldritch_horror", "textures/entity/weaver_spawn.png");

    public WeaverSpawnRenderer(EntityRendererProvider.Context context) {
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
