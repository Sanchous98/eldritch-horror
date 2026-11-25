package com.sanchous98.eldritchhorror.client;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.entity.AncientOneSizes;
import com.sanchous98.eldritchhorror.client.model.BossRenderer;
import com.sanchous98.eldritchhorror.client.model.boss.CthulhuModel;
import com.sanchous98.eldritchhorror.entity.Cthulhu;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;

/** Renderer for the custom {@link CthulhuModel}. */
public final class CthulhuRenderer extends BossRenderer<Cthulhu, CthulhuModel> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(EldritchHorror.MODID, "textures/entity/cthulhu.png");

    private static final AncientOneSizes.Size SIZE = AncientOneSizes.of("cthulhu");

    public CthulhuRenderer(EntityRendererProvider.Context context) {
        super(context, new CthulhuModel(context.bakeLayer(CthulhuModel.LAYER)), SIZE.shadow(2.5F), TEXTURE, SIZE.renderScale());
    }
}
