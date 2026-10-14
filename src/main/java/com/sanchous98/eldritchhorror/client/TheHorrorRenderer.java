package com.sanchous98.eldritchhorror.client;

import com.sanchous98.eldritchhorror.entity.TheHorror;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.wither.WitherBossModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.WitherRenderState;
import net.minecraft.resources.Identifier;

/**
 * Placeholder renderer for The Horror: the vanilla wither silhouette (a many-part, floating
 * presence) with our own flat placeholder texture. Art is the user's job.
 */
public final class TheHorrorRenderer extends MobRenderer<TheHorror, WitherRenderState, WitherBossModel> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath("eldritch_horror", "textures/entity/the_horror.png");

    public TheHorrorRenderer(EntityRendererProvider.Context context) {
        super(context, new WitherBossModel(context.bakeLayer(ModelLayers.WITHER)), 2.4F);
    }

    @Override
    public WitherRenderState createRenderState() {
        return new WitherRenderState();
    }

    @Override
    public Identifier getTextureLocation(WitherRenderState state) {
        return TEXTURE;
    }
}
