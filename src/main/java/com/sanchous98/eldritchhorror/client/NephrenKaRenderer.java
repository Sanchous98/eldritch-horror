package com.sanchous98.eldritchhorror.client;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.client.model.BossRenderer;
import com.sanchous98.eldritchhorror.client.model.boss.NephrenKaModel;
import com.sanchous98.eldritchhorror.entity.NephrenKa;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;

/** Renderer for the custom {@link NephrenKaModel}. */
public final class NephrenKaRenderer extends BossRenderer<NephrenKa, NephrenKaModel> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(EldritchHorror.MODID, "textures/entity/nephren_ka.png");

    public NephrenKaRenderer(EntityRendererProvider.Context context) {
        super(context, new NephrenKaModel(context.bakeLayer(NephrenKaModel.LAYER)), 1.4F, TEXTURE, 1.75F);
    }
}
