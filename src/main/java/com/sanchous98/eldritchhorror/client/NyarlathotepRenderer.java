package com.sanchous98.eldritchhorror.client;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.entity.AncientOneSizes;
import com.sanchous98.eldritchhorror.client.model.BossRenderer;
import com.sanchous98.eldritchhorror.client.model.boss.NyarlathotepModel;
import com.sanchous98.eldritchhorror.entity.Nyarlathotep;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;

/** Renderer for the custom {@link NyarlathotepModel}. */
public final class NyarlathotepRenderer extends BossRenderer<Nyarlathotep, NyarlathotepModel> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(EldritchHorror.MODID, "textures/entity/nyarlathotep.png");

    private static final AncientOneSizes.Size SIZE = AncientOneSizes.of("nyarlathotep");

    public NyarlathotepRenderer(EntityRendererProvider.Context context) {
        super(context, new NyarlathotepModel(context.bakeLayer(NyarlathotepModel.LAYER)), SIZE.shadow(0.8F), TEXTURE, SIZE.renderScale());
    }
}
