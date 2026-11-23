package com.sanchous98.eldritchhorror.client;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.client.model.BossRenderer;
import com.sanchous98.eldritchhorror.client.model.boss.NyarlathotepModel;
import com.sanchous98.eldritchhorror.entity.Nyarlathotep;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;

/** Renderer for the custom {@link NyarlathotepModel}. */
public final class NyarlathotepRenderer extends BossRenderer<Nyarlathotep, NyarlathotepModel> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(EldritchHorror.MODID, "textures/entity/nyarlathotep.png");

    public NyarlathotepRenderer(EntityRendererProvider.Context context) {
        super(context, new NyarlathotepModel(context.bakeLayer(NyarlathotepModel.LAYER)), 0.8F, TEXTURE, 1.45F);
    }
}
