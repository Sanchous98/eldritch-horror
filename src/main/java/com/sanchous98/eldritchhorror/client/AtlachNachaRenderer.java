package com.sanchous98.eldritchhorror.client;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.client.model.BossRenderer;
import com.sanchous98.eldritchhorror.client.model.boss.AtlachNachaModel;
import com.sanchous98.eldritchhorror.entity.AtlachNacha;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;

/** Renderer for the custom {@link AtlachNachaModel}. */
public final class AtlachNachaRenderer extends BossRenderer<AtlachNacha, AtlachNachaModel> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(EldritchHorror.MODID, "textures/entity/atlach_nacha.png");

    public AtlachNachaRenderer(EntityRendererProvider.Context context) {
        super(context, new AtlachNachaModel(context.bakeLayer(AtlachNachaModel.LAYER)), 1.6F, TEXTURE, 1.4F);
    }
}
