package com.sanchous98.eldritchhorror.client;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.client.model.BossRenderer;
import com.sanchous98.eldritchhorror.client.model.boss.HasturModel;
import com.sanchous98.eldritchhorror.entity.Hastur;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;

/** Renderer for the custom {@link HasturModel}. */
public final class HasturRenderer extends BossRenderer<Hastur, HasturModel> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(EldritchHorror.MODID, "textures/entity/hastur.png");

    public HasturRenderer(EntityRendererProvider.Context context) {
        super(context, new HasturModel(context.bakeLayer(HasturModel.LAYER)), 2.2F, TEXTURE, 2.2F);
    }
}
