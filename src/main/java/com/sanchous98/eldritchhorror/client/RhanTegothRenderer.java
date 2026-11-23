package com.sanchous98.eldritchhorror.client;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.client.model.BossRenderer;
import com.sanchous98.eldritchhorror.client.model.boss.RhanTegothModel;
import com.sanchous98.eldritchhorror.entity.RhanTegoth;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;

/** Renderer for the custom {@link RhanTegothModel}. */
public final class RhanTegothRenderer extends BossRenderer<RhanTegoth, RhanTegothModel> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(EldritchHorror.MODID, "textures/entity/rhan_tegoth.png");

    public RhanTegothRenderer(EntityRendererProvider.Context context) {
        super(context, new RhanTegothModel(context.bakeLayer(RhanTegothModel.LAYER)), 1.8F, TEXTURE, 1.6F);
    }
}
