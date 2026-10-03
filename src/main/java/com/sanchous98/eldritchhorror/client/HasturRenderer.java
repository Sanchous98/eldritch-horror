package com.sanchous98.eldritchhorror.client;

import com.sanchous98.eldritchhorror.entity.Hastur;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.wither.WitherBossModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.WitherRenderState;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Placeholder renderer for Hastur: the vanilla wither model (a many-part, floating silhouette) with
 * our own flat placeholder texture. Art is the user's job.
 */
@OnlyIn(Dist.CLIENT)
public final class HasturRenderer extends MobRenderer<Hastur, WitherRenderState, WitherBossModel> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath("eldritch_horror", "textures/entity/hastur.png");

    public HasturRenderer(EntityRendererProvider.Context context) {
        super(context, new WitherBossModel(context.bakeLayer(ModelLayers.WITHER)), 1.8F);
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
