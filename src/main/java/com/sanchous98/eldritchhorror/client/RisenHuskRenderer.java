package com.sanchous98.eldritchhorror.client;

import com.sanchous98.eldritchhorror.entity.RisenHusk;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.zombie.ZombieModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Placeholder renderer for the risen husk: a {@link HumanoidMobRenderer} over the vanilla zombie
 * humanoid model, with our own flat placeholder texture. Art is the user's job, so this deliberately
 * reuses an existing baked model layer ({@link ModelLayers#ZOMBIE}) rather than registering a custom
 * one — the fewest 26.3 client APIs touched, and nothing to get wrong in
 * {@code EntityRenderersEvent.RegisterLayerDefinitions}.
 *
 * <p>26.3 verified: {@code HumanoidMobRenderer} is abstract but leaves only
 * {@code createRenderState()} and {@code getTextureLocation(state)} to implement; its 3-arg
 * constructor {@code (context, model, shadow)} passes the model as its own baby model, so no second
 * bake is needed. {@code ZombieRenderState} is the matching render state for {@link ZombieModel}.
 */
@OnlyIn(Dist.CLIENT)
public final class RisenHuskRenderer
        extends HumanoidMobRenderer<RisenHusk, ZombieRenderState, ZombieModel<ZombieRenderState>> {

    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("eldritch_horror", "textures/entity/risen_husk.png");

    public RisenHuskRenderer(EntityRendererProvider.Context context) {
        super(context, new ZombieModel<>(context.bakeLayer(ModelLayers.ZOMBIE)), 0.5F);
    }

    @Override
    public ZombieRenderState createRenderState() {
        return new ZombieRenderState();
    }

    @Override
    public void extractRenderState(RisenHusk entity, ZombieRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        // The zombie model reads this to raise its arms while attacking; without it the placeholder
        // walks with arms down. AbstractZombieRenderer does the same, but is typed to Zombie.
        state.isAggressive = entity.isAggressive();
    }

    @Override
    public Identifier getTextureLocation(ZombieRenderState state) {
        return TEXTURE;
    }
}
