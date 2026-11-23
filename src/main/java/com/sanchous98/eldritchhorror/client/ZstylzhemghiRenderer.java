package com.sanchous98.eldritchhorror.client;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.client.model.BossRenderer;
import com.sanchous98.eldritchhorror.client.model.boss.ZstylzhemghiModel;
import com.sanchous98.eldritchhorror.entity.Zstylzhemghi;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;

/** Renderer for the custom {@link ZstylzhemghiModel}. */
public final class ZstylzhemghiRenderer extends BossRenderer<Zstylzhemghi, ZstylzhemghiModel> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(EldritchHorror.MODID, "textures/entity/zstylzhemghi.png");

    public ZstylzhemghiRenderer(EntityRendererProvider.Context context) {
        super(context, new ZstylzhemghiModel(context.bakeLayer(ZstylzhemghiModel.LAYER)), 1.8F, TEXTURE, 1.45F);
    }
}
