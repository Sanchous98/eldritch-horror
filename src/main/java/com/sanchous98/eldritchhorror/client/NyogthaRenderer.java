package com.sanchous98.eldritchhorror.client;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.client.model.BossRenderer;
import com.sanchous98.eldritchhorror.client.model.boss.NyogthaModel;
import com.sanchous98.eldritchhorror.entity.Nyogtha;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;

/** Renderer for the custom {@link NyogthaModel}. */
public final class NyogthaRenderer extends BossRenderer<Nyogtha, NyogthaModel> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(EldritchHorror.MODID, "textures/entity/nyogtha.png");

    public NyogthaRenderer(EntityRendererProvider.Context context) {
        super(context, new NyogthaModel(context.bakeLayer(NyogthaModel.LAYER)), 2.0F, TEXTURE, 1.6F);
    }
}
