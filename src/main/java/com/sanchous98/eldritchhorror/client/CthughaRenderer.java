package com.sanchous98.eldritchhorror.client;

import com.sanchous98.eldritchhorror.entity.Cthugha;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.blaze.BlazeModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;

/**
 * Placeholder renderer for Cthugha: the vanilla blaze model (a hovering fire) with our own flat
 * placeholder texture (64x32 blaze sheet). Art is the user's job.
 *
 * <p>26.3 verified: {@code BlazeModel extends EntityModel<LivingEntityRenderState>} and vanilla's
 * {@code BlazeRenderer} is a {@code MobRenderer<Blaze, LivingEntityRenderState, BlazeModel>}; we take
 * the same shape and need only {@code createRenderState()} + {@code getTextureLocation(state)}.
 */
public final class CthughaRenderer
        extends MobRenderer<Cthugha, LivingEntityRenderState, BlazeModel> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath("eldritch_horror", "textures/entity/cthugha.png");

    public CthughaRenderer(EntityRendererProvider.Context context) {
        super(context, new BlazeModel(context.bakeLayer(ModelLayers.BLAZE)), 1.0F);
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
