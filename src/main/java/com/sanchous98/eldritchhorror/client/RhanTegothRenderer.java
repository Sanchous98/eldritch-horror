package com.sanchous98.eldritchhorror.client;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.entity.AncientOneSizes;
import com.sanchous98.eldritchhorror.client.model.BossRenderer;
import com.sanchous98.eldritchhorror.client.model.boss.RhanTegothModel;
import com.sanchous98.eldritchhorror.entity.RhanTegoth;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;

/** Renderer for the custom {@link RhanTegothModel}. */
public final class RhanTegothRenderer extends BossRenderer<RhanTegoth, RhanTegothModel> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(EldritchHorror.MODID, "textures/entity/rhan_tegoth.png");

    private static final AncientOneSizes.Size SIZE = AncientOneSizes.of("rhan_tegoth");

    public RhanTegothRenderer(EntityRendererProvider.Context context) {
        super(context, new RhanTegothModel(context.bakeLayer(RhanTegothModel.LAYER)), SIZE.shadow(1.8F), TEXTURE, SIZE.renderScale());
    }
}
