package com.sanchous98.eldritchhorror.client;

import com.sanchous98.eldritchhorror.entity.Byakhee;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.phantom.PhantomModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.PhantomRenderState;
import net.minecraft.resources.Identifier;

/**
 * Placeholder renderer for the byakhee: a {@link MobRenderer} over the vanilla phantom model, with
 * our own flat placeholder texture (64x64, the phantom sheet layout). Art is the user's job.
 *
 * <p>26.3 verified: {@code PhantomModel extends EntityModel<PhantomRenderState>} and vanilla
 * {@code PhantomRenderer} is {@code MobRenderer<Phantom, PhantomRenderState, PhantomModel>}; we
 * mirror it and drive the wings from the entity id + age exactly as the phantom does, with a fixed
 * size of 0 (no scaling).
 */
public final class ByakheeRenderer extends MobRenderer<Byakhee, PhantomRenderState, PhantomModel> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath("eldritch_horror", "textures/entity/byakhee.png");

    public ByakheeRenderer(EntityRendererProvider.Context context) {
        super(context, new PhantomModel(context.bakeLayer(ModelLayers.PHANTOM)), 0.75F);
    }

    @Override
    public PhantomRenderState createRenderState() {
        return new PhantomRenderState();
    }

    @Override
    public void extractRenderState(Byakhee entity, PhantomRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.flapTime = entity.getId() * 3 + state.ageInTicks;
        state.size = 0;
    }

    @Override
    public Identifier getTextureLocation(PhantomRenderState state) {
        return TEXTURE;
    }
}
