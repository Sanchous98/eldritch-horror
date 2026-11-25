package com.sanchous98.eldritchhorror.client;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.entity.AncientOneSizes;
import com.sanchous98.eldritchhorror.client.model.BossRenderer;
import com.sanchous98.eldritchhorror.client.model.boss.ShubNiggurathModel;
import com.sanchous98.eldritchhorror.entity.ShubNiggurath;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;

/** Renderer for the custom {@link ShubNiggurathModel}. */
public final class ShubNiggurathRenderer extends BossRenderer<ShubNiggurath, ShubNiggurathModel> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(EldritchHorror.MODID, "textures/entity/shub_niggurath.png");

    private static final AncientOneSizes.Size SIZE = AncientOneSizes.of("shub_niggurath");

    public ShubNiggurathRenderer(EntityRendererProvider.Context context) {
        super(context, new ShubNiggurathModel(context.bakeLayer(ShubNiggurathModel.LAYER)), SIZE.shadow(2.6F), TEXTURE, SIZE.renderScale());
    }
}
