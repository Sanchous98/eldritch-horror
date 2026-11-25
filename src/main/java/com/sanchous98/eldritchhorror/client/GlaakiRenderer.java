package com.sanchous98.eldritchhorror.client;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.entity.AncientOneSizes;
import com.sanchous98.eldritchhorror.client.model.BossRenderer;
import com.sanchous98.eldritchhorror.client.model.boss.GlaakiModel;
import com.sanchous98.eldritchhorror.entity.Glaaki;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;

/** Renderer for the custom {@link GlaakiModel}. */
public final class GlaakiRenderer extends BossRenderer<Glaaki, GlaakiModel> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(EldritchHorror.MODID, "textures/entity/glaaki.png");

    private static final AncientOneSizes.Size SIZE = AncientOneSizes.of("glaaki");

    public GlaakiRenderer(EntityRendererProvider.Context context) {
        super(context, new GlaakiModel(context.bakeLayer(GlaakiModel.LAYER)), SIZE.shadow(1.5F), TEXTURE, SIZE.renderScale());
    }
}
