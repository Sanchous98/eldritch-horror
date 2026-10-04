package com.sanchous98.eldritchhorror.client;

import com.sanchous98.eldritchhorror.entity.Zstylzhemghi;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.creeper.CreeperModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.CreeperRenderState;
import net.minecraft.resources.Identifier;

/**
 * Placeholder renderer for Zstylzhemghi: the vanilla creeper model (a tall, wrong silhouette) with
 * our own flat placeholder texture. Art is the user's job.
 */
public final class ZstylzhemghiRenderer
        extends MobRenderer<Zstylzhemghi, CreeperRenderState, CreeperModel> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath("eldritch_horror", "textures/entity/zstylzhemghi.png");

    public ZstylzhemghiRenderer(EntityRendererProvider.Context context) {
        super(context, new CreeperModel(context.bakeLayer(ModelLayers.CREEPER)), 1.0F);
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
