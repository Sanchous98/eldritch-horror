package com.sanchous98.eldritchhorror.client;

import com.sanchous98.eldritchhorror.entity.Yig;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.slime.SlimeModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;

/**
 * Placeholder renderer for Yig: the vanilla slime cube model with our own flat placeholder texture
 * (the slime sheet is 64x32). Art is the user's job.
 *
 * <p>26.3 verified: {@code SlimeModel extends EntityModel<EntityRenderState>}, and
 * {@code MobRenderer<T, S, M extends EntityModel<? super S>>} with {@code S extends
 * LivingEntityRenderState}; binding {@code S = LivingEntityRenderState} makes {@code SlimeModel} a
 * valid {@code EntityModel<? super S>}. (Vanilla's {@code SlimeRenderer} instead extends the
 * cube-mob renderer, but the plain shape is enough for a placeholder.) Only {@code createRenderState()}
 * and {@code getTextureLocation(state)} are abstract.
 */
public final class YigRenderer extends MobRenderer<Yig, LivingEntityRenderState, SlimeModel> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath("eldritch_horror", "textures/entity/yig.png");

    public YigRenderer(EntityRendererProvider.Context context) {
        super(context, new SlimeModel(context.bakeLayer(ModelLayers.SLIME)), 1.0F);
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
