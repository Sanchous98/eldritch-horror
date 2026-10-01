package com.sanchous98.eldritchhorror.client;

import com.sanchous98.eldritchhorror.entity.ChoirSpite;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.vex.VexModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.VexRenderState;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Placeholder renderer for the choir spite: a {@link MobRenderer} over the vanilla vex model, with
 * our own flat placeholder texture (32x32, the vex sheet layout, alpha used for the wings). Art is
 * the user's job.
 *
 * <p>26.3 verified: {@code VexModel extends EntityModel<VexRenderState>} and vanilla
 * {@code VexRenderer} is {@code MobRenderer<Vex, VexRenderState, VexModel>}. The model uses the
 * translucent render type, so the placeholder texture must keep the wing area transparent.
 */
@OnlyIn(Dist.CLIENT)
public final class ChoirSpiteRenderer extends MobRenderer<ChoirSpite, VexRenderState, VexModel> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath("eldritch_horror", "textures/entity/choir_spite.png");

    public ChoirSpiteRenderer(EntityRendererProvider.Context context) {
        super(context, new VexModel(context.bakeLayer(ModelLayers.VEX)), 0.3F);
    }

    @Override
    public VexRenderState createRenderState() {
        return new VexRenderState();
    }

    @Override
    public void extractRenderState(ChoirSpite entity, VexRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.isCharging = false;
    }

    @Override
    public Identifier getTextureLocation(VexRenderState state) {
        return TEXTURE;
    }
}
