package com.sanchous98.eldritchhorror.client;

import com.sanchous98.eldritchhorror.entity.VeilSwine;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.hoglin.HoglinModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.HoglinRenderState;
import net.minecraft.resources.Identifier;

/**
 * Placeholder renderer for the veil swine: a {@link MobRenderer} over the vanilla hoglin model,
 * with our own sickly pink-grey texture (128x64, the hoglin sheet layout). The hoglin silhouette is
 * the design's stand-in for the parody herd. Art is the user's job.
 */
public final class VeilSwineRenderer extends MobRenderer<VeilSwine, HoglinRenderState, HoglinModel> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath("eldritch_horror", "textures/entity/veil_swine.png");

    public VeilSwineRenderer(EntityRendererProvider.Context context) {
        super(context, new HoglinModel(context.bakeLayer(ModelLayers.HOGLIN)), 0.7F);
    }

    @Override
    public HoglinRenderState createRenderState() {
        return new HoglinRenderState();
    }

    @Override
    public Identifier getTextureLocation(HoglinRenderState state) {
        return TEXTURE;
    }
}
