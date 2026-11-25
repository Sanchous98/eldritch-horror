package com.sanchous98.eldritchhorror.client;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.entity.AncientOneSizes;
import com.sanchous98.eldritchhorror.client.model.BossRenderer;
import com.sanchous98.eldritchhorror.client.model.boss.ChaugnarFaugnModel;
import com.sanchous98.eldritchhorror.entity.ChaugnarFaugn;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;

/** Renderer for the custom {@link ChaugnarFaugnModel}. */
public final class ChaugnarFaugnRenderer extends BossRenderer<ChaugnarFaugn, ChaugnarFaugnModel> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(EldritchHorror.MODID, "textures/entity/chaugnar_faugn.png");

    private static final AncientOneSizes.Size SIZE = AncientOneSizes.of("chaugnar_faugn");

    public ChaugnarFaugnRenderer(EntityRendererProvider.Context context) {
        super(context, new ChaugnarFaugnModel(context.bakeLayer(ChaugnarFaugnModel.LAYER)), SIZE.shadow(1.8F), TEXTURE, SIZE.renderScale());
    }
}
