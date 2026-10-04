package com.sanchous98.eldritchhorror.client;

import com.sanchous98.eldritchhorror.entity.Hydra;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.creeper.CreeperModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.CreeperRenderState;
import net.minecraft.resources.Identifier;

/**
 * Placeholder renderer for the Hydra: the vanilla creeper model (a single head, which is the joke)
 * with our own flat placeholder texture (64x32 creeper sheet). Art is the user's job.
 *
 * <p>26.3 verified: {@code CreeperModel extends EntityModel<CreeperRenderState>} and vanilla's
 * {@code CreeperRenderer} is a {@code MobRenderer<Creeper, CreeperRenderState, CreeperModel>}; we
 * deliberately omit its power layer and need only {@code createRenderState()} +
 * {@code getTextureLocation(state)}.
 */
public final class HydraRenderer extends MobRenderer<Hydra, CreeperRenderState, CreeperModel> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath("eldritch_horror", "textures/entity/hydra.png");

    public HydraRenderer(EntityRendererProvider.Context context) {
        super(context, new CreeperModel(context.bakeLayer(ModelLayers.CREEPER)), 0.8F);
    }

    @Override
    public CreeperRenderState createRenderState() {
        return new CreeperRenderState();
    }

    @Override
    public Identifier getTextureLocation(CreeperRenderState state) {
        return TEXTURE;
    }
}
