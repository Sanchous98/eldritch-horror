package com.sanchous98.eldritchhorror.client;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.client.model.BossRenderer;
import com.sanchous98.eldritchhorror.client.model.boss.AzathothModel;
import com.sanchous98.eldritchhorror.entity.Azathoth;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;

/** Renderer for the custom {@link AzathothModel}. */
public final class AzathothRenderer extends BossRenderer<Azathoth, AzathothModel> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(EldritchHorror.MODID, "textures/entity/azathoth.png");

    public AzathothRenderer(EntityRendererProvider.Context context) {
        super(context, new AzathothModel(context.bakeLayer(AzathothModel.LAYER)), 2.0F, TEXTURE, 1.2F);
    }
}
