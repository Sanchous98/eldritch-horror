package com.sanchous98.eldritchhorror.client;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.entity.AncientOneSizes;
import com.sanchous98.eldritchhorror.client.model.BossRenderer;
import com.sanchous98.eldritchhorror.client.model.boss.CthughaModel;
import com.sanchous98.eldritchhorror.entity.Cthugha;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;

/** Renderer for the custom {@link CthughaModel}. */
public final class CthughaRenderer extends BossRenderer<Cthugha, CthughaModel> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(EldritchHorror.MODID, "textures/entity/cthugha.png");

    private static final AncientOneSizes.Size SIZE = AncientOneSizes.of("cthugha");

    public CthughaRenderer(EntityRendererProvider.Context context) {
        super(context, new CthughaModel(context.bakeLayer(CthughaModel.LAYER)), SIZE.shadow(1.5F), TEXTURE, SIZE.renderScale());
    }
}
