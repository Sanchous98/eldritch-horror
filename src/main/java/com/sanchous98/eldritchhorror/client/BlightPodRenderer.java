package com.sanchous98.eldritchhorror.client;

import com.sanchous98.eldritchhorror.entity.BlightPod;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.creeper.CreeperModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.CreeperRenderState;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Placeholder renderer for the blight pod: a {@link MobRenderer} over the vanilla creeper model,
 * with our own flat placeholder texture (64x32, the creeper sheet layout). Art is the user's job.
 *
 * <p>26.3 verified: {@code CreeperModel extends EntityModel<CreeperRenderState>}, and vanilla
 * {@code CreeperRenderer} is {@code MobRenderer<Creeper, CreeperRenderState, CreeperModel>}; we
 * mirror it with our own entity type and texture. The swelling scale/overlay is deliberately not
 * carried over (the pod does not charge), so the default {@code swelling = 0} reads as a plain walk.
 */
@OnlyIn(Dist.CLIENT)
public final class BlightPodRenderer extends MobRenderer<BlightPod, CreeperRenderState, CreeperModel> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath("eldritch_horror", "textures/entity/blight_pod.png");

    public BlightPodRenderer(EntityRendererProvider.Context context) {
        super(context, new CreeperModel(context.bakeLayer(ModelLayers.CREEPER)), 0.5F);
    }

    @Override
    public CreeperRenderState createRenderState() {
        return new CreeperRenderState();
    }

    @Override
    public Identifier getTextureLocation(CreeperRenderState state) {
        return TEXTURE;
    }
}
