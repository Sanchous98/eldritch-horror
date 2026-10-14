package com.sanchous98.eldritchhorror.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.sanchous98.eldritchhorror.entity.ChoirLeviathan;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.ghast.GhastModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.GhastRenderState;
import net.minecraft.resources.Identifier;

/**
 * Placeholder renderer for the Choir Leviathan: a {@link MobRenderer} over the vanilla ghast model
 * (the design's ghast stand-in, grown), with our own deep drowned green-teal texture (64x32, the
 * ghast sheet layout). The ghast mesh is baked 4.5x, so {@link #scale} sizes the tide-thing to its
 * 5-block hitbox. Art is the user's job.
 */
public final class ChoirLeviathanRenderer extends MobRenderer<ChoirLeviathan, GhastRenderState, GhastModel> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath("eldritch_horror", "textures/entity/choir_leviathan.png");

    /** The ghast model's baked scale, drawn up a little for a boss silhouette. */
    private static final float SCALE = 1.2F;

    public ChoirLeviathanRenderer(EntityRendererProvider.Context context) {
        super(context, new GhastModel(context.bakeLayer(ModelLayers.GHAST)), 2.0F);
    }

    @Override
    public GhastRenderState createRenderState() {
        return new GhastRenderState();
    }

    /** Draws the baked-large ghast mesh up to the Leviathan's boss silhouette. */
    @Override
    protected void scale(GhastRenderState state, PoseStack poseStack) {
        poseStack.scale(SCALE, SCALE, SCALE);
    }

    @Override
    public Identifier getTextureLocation(GhastRenderState state) {
        return TEXTURE;
    }
}
