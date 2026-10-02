package com.sanchous98.eldritchhorror.client;

import com.sanchous98.eldritchhorror.entity.YogSothoth;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.wither.WitherBossModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.WitherRenderState;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Placeholder renderer for Yog-Sothoth: the vanilla wither model, scaled up, with our own flat
 * placeholder texture (the wither sheet is 64x64). Art is the user's job.
 *
 * <p>26.3 verified as for {@link AzathothRenderer}: {@code WitherBossModel} + {@code WitherRenderState}.
 */
@OnlyIn(Dist.CLIENT)
public final class YogSothothRenderer extends MobRenderer<YogSothoth, WitherRenderState, WitherBossModel> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath("eldritch_horror", "textures/entity/yog_sothoth.png");

    public YogSothothRenderer(EntityRendererProvider.Context context) {
        super(context, new WitherBossModel(context.bakeLayer(ModelLayers.WITHER)), 2.5F);
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
