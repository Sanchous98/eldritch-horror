package com.sanchous98.eldritchhorror.client;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.client.model.BossRenderer;
import com.sanchous98.eldritchhorror.client.model.boss.HydraModel;
import com.sanchous98.eldritchhorror.entity.Hydra;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;

/** Renderer for the custom {@link HydraModel}. */
public final class HydraRenderer extends BossRenderer<Hydra, HydraModel> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(EldritchHorror.MODID, "textures/entity/hydra.png");

    public HydraRenderer(EntityRendererProvider.Context context) {
        super(context, new HydraModel(context.bakeLayer(HydraModel.LAYER)), 1.5F, TEXTURE, 1.5F);
    }
}
