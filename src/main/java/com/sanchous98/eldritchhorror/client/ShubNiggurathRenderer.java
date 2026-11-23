package com.sanchous98.eldritchhorror.client;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.client.model.BossRenderer;
import com.sanchous98.eldritchhorror.client.model.boss.ShubNiggurathModel;
import com.sanchous98.eldritchhorror.entity.ShubNiggurath;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;

/** Renderer for the custom {@link ShubNiggurathModel}. */
public final class ShubNiggurathRenderer extends BossRenderer<ShubNiggurath, ShubNiggurathModel> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(EldritchHorror.MODID, "textures/entity/shub_niggurath.png");

    public ShubNiggurathRenderer(EntityRendererProvider.Context context) {
        super(context, new ShubNiggurathModel(context.bakeLayer(ShubNiggurathModel.LAYER)), 2.6F, TEXTURE, 1.6F);
    }
}
