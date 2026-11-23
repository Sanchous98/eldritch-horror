package com.sanchous98.eldritchhorror.client;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.client.model.BossRenderer;
import com.sanchous98.eldritchhorror.client.model.boss.YigModel;
import com.sanchous98.eldritchhorror.entity.Yig;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;

/** Renderer for the custom {@link YigModel}. */
public final class YigRenderer extends BossRenderer<Yig, YigModel> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(EldritchHorror.MODID, "textures/entity/yig.png");

    public YigRenderer(EntityRendererProvider.Context context) {
        super(context, new YigModel(context.bakeLayer(YigModel.LAYER)), 1.4F, TEXTURE, 2.3F);
    }
}
