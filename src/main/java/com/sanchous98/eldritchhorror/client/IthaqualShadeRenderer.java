package com.sanchous98.eldritchhorror.client;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.entity.AncientOneSizes;
import com.sanchous98.eldritchhorror.client.model.BossRenderer;
import com.sanchous98.eldritchhorror.client.model.boss.IthaqualShadeModel;
import com.sanchous98.eldritchhorror.entity.IthaqualShade;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;

/** Renderer for the custom {@link IthaqualShadeModel}. */
public final class IthaqualShadeRenderer extends BossRenderer<IthaqualShade, IthaqualShadeModel> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(EldritchHorror.MODID, "textures/entity/ithaqual_shade.png");

    private static final AncientOneSizes.Size SIZE = AncientOneSizes.of("ithaqual_shade");

    public IthaqualShadeRenderer(EntityRendererProvider.Context context) {
        super(context, new IthaqualShadeModel(context.bakeLayer(IthaqualShadeModel.LAYER)), SIZE.shadow(1.0F), TEXTURE, SIZE.renderScale());
    }
}
