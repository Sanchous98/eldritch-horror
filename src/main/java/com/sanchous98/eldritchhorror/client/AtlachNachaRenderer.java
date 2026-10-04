package com.sanchous98.eldritchhorror.client;

import com.sanchous98.eldritchhorror.entity.AtlachNacha;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.spider.SpiderModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;

/**
 * Placeholder renderer for Atlach-Nacha: the vanilla spider model with our own flat placeholder
 * texture (the spider sheet is 64x32), keeping the spider's 180-degree flip so it reads on wall and
 * ceiling. Art is the user's job.
 *
 * <p>26.3 verified as for {@link VeilStalkerRenderer}: {@code SpiderModel extends
 * EntityModel<LivingEntityRenderState>}, so the shape is
 * {@code MobRenderer<AtlachNacha, LivingEntityRenderState, SpiderModel>}.
 */
public final class AtlachNachaRenderer
        extends MobRenderer<AtlachNacha, LivingEntityRenderState, SpiderModel> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath("eldritch_horror", "textures/entity/atlach_nacha.png");

    public AtlachNachaRenderer(EntityRendererProvider.Context context) {
        super(context, new SpiderModel(context.bakeLayer(ModelLayers.SPIDER)), 1.0F);
    }

    @Override
    protected float getFlipDegrees() {
        return 180.0F;
    }

    @Override
    public LivingEntityRenderState createRenderState() {
        return new LivingEntityRenderState();
    }

    @Override
    public Identifier getTextureLocation(LivingEntityRenderState state) {
        return TEXTURE;
    }
}
