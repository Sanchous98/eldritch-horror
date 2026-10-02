package com.sanchous98.eldritchhorror.client;

import com.sanchous98.eldritchhorror.entity.Azathoth;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.wither.WitherBossModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.WitherRenderState;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Placeholder renderer for Azathoth: the vanilla wither model (a floating, many-part silhouette)
 * with our own flat placeholder texture. Art is the user's job.
 *
 * <p>26.3 verified: {@code WitherBossModel extends EntityModel<WitherRenderState>} and
 * {@code MobRenderer<T, S, M extends EntityModel<? super S>>}, so the shape is
 * {@code MobRenderer<Azathoth, WitherRenderState, WitherBossModel>}; only {@code createRenderState()}
 * and {@code getTextureLocation(state)} are abstract.
 */
@OnlyIn(Dist.CLIENT)
public final class AzathothRenderer extends MobRenderer<Azathoth, WitherRenderState, WitherBossModel> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath("eldritch_horror", "textures/entity/azathoth.png");

    public AzathothRenderer(EntityRendererProvider.Context context) {
        super(context, new WitherBossModel(context.bakeLayer(ModelLayers.WITHER)), 2.0F);
    }

    @Override
    public WitherRenderState createRenderState() {
        return new WitherRenderState();
    }

    @Override
    public Identifier getTextureLocation(WitherRenderState state) {
        return TEXTURE;
    }
}
