package com.sanchous98.eldritchhorror.client;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.entity.AncientOneSizes;
import com.sanchous98.eldritchhorror.client.model.BossRenderer;
import com.sanchous98.eldritchhorror.client.model.boss.HasturModel;
import com.sanchous98.eldritchhorror.entity.Hastur;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;

/** Renderer for the custom {@link HasturModel}. */
public final class HasturRenderer extends BossRenderer<Hastur, HasturModel> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(EldritchHorror.MODID, "textures/entity/hastur.png");

    private static final AncientOneSizes.Size SIZE = AncientOneSizes.of("hastur");

    public HasturRenderer(EntityRendererProvider.Context context) {
        super(context, new HasturModel(context.bakeLayer(HasturModel.LAYER)), SIZE.shadow(2.2F), TEXTURE, SIZE.renderScale());
    }
}
