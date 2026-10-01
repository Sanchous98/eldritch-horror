package com.sanchous98.eldritchhorror.client;

import com.sanchous98.eldritchhorror.entity.StarSpawn;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.enderman.EndermanModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.state.EndermanRenderState;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Placeholder renderer for the star-spawn: the same verified enderman shape as
 * {@link WatcherRenderer} (a {@link HumanoidMobRenderer} over {@link EndermanModel} with an
 * {@link EndermanRenderState}), but our own texture. The tall, thin silhouette reads as an elite
 * minion; art is the user's job.
 *
 * <p>26.3 verified as for {@link WatcherRenderer}: {@code EndermanModel<T extends
 * EndermanRenderState> extends HumanoidModel<T>} and {@code HumanoidMobRenderer<T, S, M extends
 * HumanoidModel<S>>}.
 */
@OnlyIn(Dist.CLIENT)
public final class StarSpawnRenderer
        extends HumanoidMobRenderer<StarSpawn, EndermanRenderState, EndermanModel<EndermanRenderState>> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath("eldritch_horror", "textures/entity/star_spawn.png");

    public StarSpawnRenderer(EntityRendererProvider.Context context) {
        super(context, new EndermanModel<>(context.bakeLayer(ModelLayers.ENDERMAN)), 0.5F);
    }

    @Override
    public EndermanRenderState createRenderState() {
        return new EndermanRenderState();
    }

    @Override
    public void extractRenderState(StarSpawn entity, EndermanRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        // No isCreepy: the star-spawn does not flicker like an enderman.
    }

    @Override
    public Identifier getTextureLocation(EndermanRenderState state) {
        return TEXTURE;
    }
}
