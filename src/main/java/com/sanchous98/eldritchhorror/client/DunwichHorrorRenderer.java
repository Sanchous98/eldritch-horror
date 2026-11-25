package com.sanchous98.eldritchhorror.client;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.entity.AncientOneSizes;
import com.sanchous98.eldritchhorror.client.model.BossRenderer;
import com.sanchous98.eldritchhorror.client.model.boss.DunwichHorrorModel;
import com.sanchous98.eldritchhorror.entity.DunwichHorror;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;

/** Renderer for the custom {@link DunwichHorrorModel}. */
public final class DunwichHorrorRenderer extends BossRenderer<DunwichHorror, DunwichHorrorModel> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(EldritchHorror.MODID, "textures/entity/dunwich_horror.png");

    private static final AncientOneSizes.Size SIZE = AncientOneSizes.of("dunwich_horror");

    public DunwichHorrorRenderer(EntityRendererProvider.Context context) {
        super(context, new DunwichHorrorModel(context.bakeLayer(DunwichHorrorModel.LAYER)), SIZE.shadow(1.6F), TEXTURE, SIZE.renderScale());
    }
}
