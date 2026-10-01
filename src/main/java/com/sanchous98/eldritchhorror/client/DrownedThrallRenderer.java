package com.sanchous98.eldritchhorror.client;

import com.sanchous98.eldritchhorror.entity.DrownedThrall;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.zombie.ZombieModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Placeholder renderer for the drowned thrall: a {@link HumanoidMobRenderer} over the vanilla zombie
 * model, with our own flat placeholder texture. It deliberately reuses the same verified shape as
 * {@link RisenHuskRenderer} ({@link ModelLayers#ZOMBIE} + {@link ZombieRenderState}) rather than the
 * drowned model, whose swim pose reads the vanilla {@code Drowned} state we do not implement. Art
 * is the user's job.
 */
@OnlyIn(Dist.CLIENT)
public final class DrownedThrallRenderer
        extends HumanoidMobRenderer<DrownedThrall, ZombieRenderState, ZombieModel<ZombieRenderState>> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath("eldritch_horror", "textures/entity/drowned_thrall.png");

    public DrownedThrallRenderer(EntityRendererProvider.Context context) {
        super(context, new ZombieModel<>(context.bakeLayer(ModelLayers.ZOMBIE)), 0.5F);
    }

    @Override
    public ZombieRenderState createRenderState() {
        return new ZombieRenderState();
    }

    @Override
    public void extractRenderState(DrownedThrall entity, ZombieRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.isAggressive = entity.isAggressive();
    }

    @Override
    public Identifier getTextureLocation(ZombieRenderState state) {
        return TEXTURE;
    }
}
