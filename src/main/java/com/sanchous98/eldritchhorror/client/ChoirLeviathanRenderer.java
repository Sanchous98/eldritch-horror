package com.sanchous98.eldritchhorror.client;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.client.model.BossRenderer;
import com.sanchous98.eldritchhorror.client.model.boss.ChoirLeviathanModel;
import com.sanchous98.eldritchhorror.entity.ChoirLeviathan;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;

/** Renderer for the custom {@link ChoirLeviathanModel}. */
public final class ChoirLeviathanRenderer extends BossRenderer<ChoirLeviathan, ChoirLeviathanModel> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(EldritchHorror.MODID, "textures/entity/choir_leviathan.png");

    public ChoirLeviathanRenderer(EntityRendererProvider.Context context) {
        super(context, new ChoirLeviathanModel(context.bakeLayer(ChoirLeviathanModel.LAYER)), 2.6F, TEXTURE, 1.7F);
    }
}
