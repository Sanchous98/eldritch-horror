package com.sanchous98.eldritchhorror.client;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.client.model.BossRenderer;
import com.sanchous98.eldritchhorror.client.model.boss.TulzschaModel;
import com.sanchous98.eldritchhorror.entity.Tulzscha;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;

/** Renderer for the custom {@link TulzschaModel}. */
public final class TulzschaRenderer extends BossRenderer<Tulzscha, TulzschaModel> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(EldritchHorror.MODID, "textures/entity/tulzscha.png");

    public TulzschaRenderer(EntityRendererProvider.Context context) {
        super(context, new TulzschaModel(context.bakeLayer(TulzschaModel.LAYER)), 1.3F, TEXTURE, 1.25F);
    }
}
