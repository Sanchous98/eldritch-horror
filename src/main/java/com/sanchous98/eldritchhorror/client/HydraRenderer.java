package com.sanchous98.eldritchhorror.client;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.entity.AncientOneSizes;
import com.sanchous98.eldritchhorror.client.model.BossRenderer;
import com.sanchous98.eldritchhorror.client.model.boss.HydraModel;
import com.sanchous98.eldritchhorror.entity.Hydra;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;

/** Renderer for the custom {@link HydraModel}. */
public final class HydraRenderer extends BossRenderer<Hydra, HydraModel> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(EldritchHorror.MODID, "textures/entity/hydra.png");

    private static final AncientOneSizes.Size SIZE = AncientOneSizes.of("hydra");

    public HydraRenderer(EntityRendererProvider.Context context) {
        super(context, new HydraModel(context.bakeLayer(HydraModel.LAYER)), SIZE.shadow(1.5F), TEXTURE, SIZE.renderScale());
    }
}
