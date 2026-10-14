package com.sanchous98.eldritchhorror.client;

import com.sanchous98.eldritchhorror.entity.IthaqualShade;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.warden.WardenModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.WardenRenderState;
import net.minecraft.resources.Identifier;

/**
 * Placeholder renderer for the Ithaqual Shade: the vanilla warden silhouette (the design's listed
 * stand-in) with our own frost white-blue texture (128x128, the warden sheet layout). We
 * deliberately do not add the warden's emissive layers. Art is the user's job.
 */
public final class IthaqualShadeRenderer extends MobRenderer<IthaqualShade, WardenRenderState, WardenModel> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath("eldritch_horror", "textures/entity/ithaqual_shade.png");

    public IthaqualShadeRenderer(EntityRendererProvider.Context context) {
        super(context, new WardenModel(context.bakeLayer(ModelLayers.WARDEN)), 1.0F);
    }

    @Override
    public WardenRenderState createRenderState() {
        return new WardenRenderState();
    }

    @Override
    public Identifier getTextureLocation(WardenRenderState state) {
        return TEXTURE;
    }
}
