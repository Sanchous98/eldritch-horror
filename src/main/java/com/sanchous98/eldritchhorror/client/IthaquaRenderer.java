package com.sanchous98.eldritchhorror.client;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.client.model.BossRenderer;
import com.sanchous98.eldritchhorror.client.model.boss.IthaquaModel;
import com.sanchous98.eldritchhorror.entity.Ithaqua;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;

/** Renderer for the custom {@link IthaquaModel}. */
public final class IthaquaRenderer extends BossRenderer<Ithaqua, IthaquaModel> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(EldritchHorror.MODID, "textures/entity/ithaqua.png");

    public IthaquaRenderer(EntityRendererProvider.Context context) {
        super(context, new IthaquaModel(context.bakeLayer(IthaquaModel.LAYER)), 1.4F, TEXTURE, 2.1F);
    }
}
