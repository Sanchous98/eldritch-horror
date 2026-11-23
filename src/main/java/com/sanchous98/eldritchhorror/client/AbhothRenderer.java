package com.sanchous98.eldritchhorror.client;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.client.model.BossRenderer;
import com.sanchous98.eldritchhorror.client.model.boss.AbhothModel;
import com.sanchous98.eldritchhorror.entity.Abhoth;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;

/** Renderer for the custom {@link AbhothModel}. */
public final class AbhothRenderer extends BossRenderer<Abhoth, AbhothModel> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(EldritchHorror.MODID, "textures/entity/abhoth.png");

    public AbhothRenderer(EntityRendererProvider.Context context) {
        super(context, new AbhothModel(context.bakeLayer(AbhothModel.LAYER)), 2.0F, TEXTURE, 1.8F);
    }
}
