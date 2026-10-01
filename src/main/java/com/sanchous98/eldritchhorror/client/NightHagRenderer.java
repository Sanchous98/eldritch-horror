package com.sanchous98.eldritchhorror.client;

import com.sanchous98.eldritchhorror.entity.NightHag;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.phantom.PhantomModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.PhantomRenderState;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Placeholder renderer for the night hag: a {@link MobRenderer} over the vanilla phantom model, with
 * our own flat placeholder texture (64x64, the phantom sheet layout). It mirrors
 * {@code ByakheeRenderer} exactly: the wings are driven from the entity id + age, and the fixed size
 * of 0 keeps the phantom's distance scale off. Art is the user's job.
 *
 * <p>26.3 verified: {@code PhantomModel extends EntityModel<PhantomRenderState>} and vanilla
 * {@code PhantomRenderer} is {@code MobRenderer<Phantom, PhantomRenderState, PhantomModel>}.
 */
@OnlyIn(Dist.CLIENT)
public final class NightHagRenderer extends MobRenderer<NightHag, PhantomRenderState, PhantomModel> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath("eldritch_horror", "textures/entity/night_hag.png");

    public NightHagRenderer(EntityRendererProvider.Context context) {
        super(context, new PhantomModel(context.bakeLayer(ModelLayers.PHANTOM)), 0.75F);
    }

    @Override
    public PhantomRenderState createRenderState() {
        return new PhantomRenderState();
    }

    @Override
    public void extractRenderState(NightHag entity, PhantomRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.flapTime = entity.getId() * 3 + state.ageInTicks;
        state.size = 0;
    }

    @Override
    public Identifier getTextureLocation(PhantomRenderState state) {
        return TEXTURE;
    }
}
