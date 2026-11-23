package com.sanchous98.eldritchhorror.client;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.client.model.BossRenderer;
import com.sanchous98.eldritchhorror.client.model.boss.YogSothothModel;
import com.sanchous98.eldritchhorror.entity.YogSothoth;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;

/** Renderer for the custom {@link YogSothothModel}. */
public final class YogSothothRenderer extends BossRenderer<YogSothoth, YogSothothModel> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(EldritchHorror.MODID, "textures/entity/yog_sothoth.png");

    public YogSothothRenderer(EntityRendererProvider.Context context) {
        super(context, new YogSothothModel(context.bakeLayer(YogSothothModel.LAYER)), 2.5F, TEXTURE, 1.45F);
    }
}
