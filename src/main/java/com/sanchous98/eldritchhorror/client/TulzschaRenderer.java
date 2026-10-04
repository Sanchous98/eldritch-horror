package com.sanchous98.eldritchhorror.client;

import com.sanchous98.eldritchhorror.entity.Tulzscha;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.blaze.BlazeModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;

/**
 * Placeholder renderer for Tulzscha: the vanilla blaze model (a hovering flame) with our own flat
 * placeholder texture. Art is the user's job.
 */
public final class TulzschaRenderer
        extends MobRenderer<Tulzscha, LivingEntityRenderState, BlazeModel> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath("eldritch_horror", "textures/entity/tulzscha.png");

    public TulzschaRenderer(EntityRendererProvider.Context context) {
        super(context, new BlazeModel(context.bakeLayer(ModelLayers.BLAZE)), 1.0F);
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
