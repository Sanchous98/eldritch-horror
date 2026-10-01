package com.sanchous98.eldritchhorror.client;

import com.sanchous98.eldritchhorror.entity.TaintedFauna;
import net.minecraft.client.model.animal.cow.CowModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Placeholder renderer for tainted fauna: a {@link MobRenderer} over the vanilla cow quadruped
 * model, with our own flat placeholder texture (64x64, the cow sheet layout). Art is the user's job,
 * so this reuses an existing baked model layer ({@link ModelLayers#COW}) rather than registering a
 * custom one — the fewest 26.3 client APIs touched.
 *
 * <p>26.3 verified: {@code CowModel extends QuadrupedModel<LivingEntityRenderState>}, so
 * {@link LivingEntityRenderState} is the matching render state; {@code MobRenderer} leaves only
 * {@code createRenderState()} and {@code getTextureLocation(state)} to implement.
 */
@OnlyIn(Dist.CLIENT)
public final class TaintedFaunaRenderer
        extends MobRenderer<TaintedFauna, LivingEntityRenderState, CowModel> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath("eldritch_horror", "textures/entity/tainted_fauna.png");

    public TaintedFaunaRenderer(EntityRendererProvider.Context context) {
        super(context, new CowModel(context.bakeLayer(ModelLayers.COW)), 0.7F);
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
