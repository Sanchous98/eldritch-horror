package com.sanchous98.eldritchhorror.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.sanchous98.eldritchhorror.entity.Abhoth;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.slime.SlimeModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.SlimeRenderState;
import net.minecraft.resources.Identifier;

/**
 * Placeholder renderer for Abhoth: the vanilla slime model scaled up (a huge, churning mass) with
 * our own flat placeholder texture. Art is the user's job.
 */
public final class AbhothRenderer extends MobRenderer<Abhoth, SlimeRenderState, SlimeModel> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath("eldritch_horror", "textures/entity/abhoth.png");

    public AbhothRenderer(EntityRendererProvider.Context context) {
        super(context, new SlimeModel(context.bakeLayer(ModelLayers.SLIME)), 1.2F);
    }

    @Override
    public SlimeRenderState createRenderState() {
        return new SlimeRenderState();
    }

    @Override
    public void extractRenderState(Abhoth entity, SlimeRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.size = 3;
        state.squish = 0.0F;
    }

    @Override
    protected void scale(SlimeRenderState state, PoseStack poseStack) {
        super.scale(state, poseStack);
        float size = state.size;
        poseStack.scale(size, size, size);
    }

    @Override
    public Identifier getTextureLocation(SlimeRenderState state) {
        return TEXTURE;
    }
}
