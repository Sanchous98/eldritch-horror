package com.sanchous98.eldritchhorror.client;

import com.sanchous98.eldritchhorror.entity.Ithaqua;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.warden.WardenModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.WardenRenderState;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Placeholder renderer for Ithaqua: the vanilla warden model (a tall, hunched silhouette) with our
 * own flat placeholder texture (the warden sheet is 128x128). Art is the user's job.
 *
 * <p>26.3 verified: {@code WardenModel extends EntityModel<WardenRenderState>} and
 * {@code MobRenderer<T, S, M extends EntityModel<? super S>>}, so the shape is
 * {@code MobRenderer<Ithaqua, WardenRenderState, WardenModel>}. We deliberately do not add the
 * warden's emissive layers (they need warden-specific render-state animations).
 */
@OnlyIn(Dist.CLIENT)
public final class IthaquaRenderer extends MobRenderer<Ithaqua, WardenRenderState, WardenModel> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath("eldritch_horror", "textures/entity/ithaqua.png");

    public IthaquaRenderer(EntityRendererProvider.Context context) {
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
