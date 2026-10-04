package com.sanchous98.eldritchhorror.client;

import com.sanchous98.eldritchhorror.entity.ChaugnarFaugn;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.ravager.RavagerModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.RavagerRenderState;
import net.minecraft.resources.Identifier;

/**
 * Placeholder renderer for Chaugnar Faugn: the vanilla ravager quadruped (a squat, hungry bulk) with
 * our own flat placeholder texture. Art is the user's job.
 */
public final class ChaugnarFaugnRenderer
        extends MobRenderer<ChaugnarFaugn, RavagerRenderState, RavagerModel> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath("eldritch_horror", "textures/entity/chaugnar_faugn.png");

    public ChaugnarFaugnRenderer(EntityRendererProvider.Context context) {
        super(context, new RavagerModel(context.bakeLayer(ModelLayers.RAVAGER)), 1.2F);
    }

    @Override
    public RavagerRenderState createRenderState() {
        return new RavagerRenderState();
    }

    @Override
    public Identifier getTextureLocation(RavagerRenderState state) {
        return TEXTURE;
    }
}
