package com.sanchous98.eldritchhorror.client;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.client.model.BossRenderer;
import com.sanchous98.eldritchhorror.client.model.boss.TheHorrorModel;
import com.sanchous98.eldritchhorror.entity.TheHorror;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;

/** Renderer for the custom {@link TheHorrorModel}. */
public final class TheHorrorRenderer extends BossRenderer<TheHorror, TheHorrorModel> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(EldritchHorror.MODID, "textures/entity/the_horror.png");

    public TheHorrorRenderer(EntityRendererProvider.Context context) {
        super(context, new TheHorrorModel(context.bakeLayer(TheHorrorModel.LAYER)), 2.8F, TEXTURE, 1.55F);
    }
}
