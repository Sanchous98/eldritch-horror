package com.sanchous98.eldritchhorror.client;

import com.sanchous98.eldritchhorror.entity.PlagueCrone;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.witch.WitchModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.WitchRenderState;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Placeholder renderer for the plague crone: a {@link MobRenderer} over the vanilla witch model — the
 * exact silhouette the design maps it to — with our own flat placeholder texture. Art is the user's
 * job.
 *
 * <p>26.3 verified: {@code WitchModel extends EntityModel<WitchRenderState>} and vanilla
 * {@code WitchRenderer} is a {@code MobRenderer<Witch, WitchRenderState, WitchModel>}.
 */
@OnlyIn(Dist.CLIENT)
public final class PlagueCroneRenderer extends MobRenderer<PlagueCrone, WitchRenderState, WitchModel> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath("eldritch_horror", "textures/entity/plague_crone.png");

    public PlagueCroneRenderer(EntityRendererProvider.Context context) {
        super(context, new WitchModel(context.bakeLayer(ModelLayers.WITCH)), 0.5F);
    }

    @Override
    public WitchRenderState createRenderState() {
        return new WitchRenderState();
    }

    @Override
    public Identifier getTextureLocation(WitchRenderState state) {
        return TEXTURE;
    }
}
