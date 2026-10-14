package com.sanchous98.eldritchhorror.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.sanchous98.eldritchhorror.entity.WailHusk;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.ghast.GhastModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.GhastRenderState;
import net.minecraft.resources.Identifier;

/**
 * Placeholder renderer for the wail husk: a {@link MobRenderer} over the vanilla ghast model, with
 * our own bone white-grey texture (64x32, the ghast sheet layout). The ghast model is baked 4.5x
 * large, so {@link #scale} draws it down to the mourner's own 1.6-block silhouette. Art is the
 * user's job.
 */
public final class WailHuskRenderer extends MobRenderer<WailHusk, GhastRenderState, GhastModel> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath("eldritch_horror", "textures/entity/wail_husk.png");

    /** The ghast model is baked at 4.5x; a fraction of that reads as a man-sized mourner. */
    private static final float SCALE = 0.4F;

    public WailHuskRenderer(EntityRendererProvider.Context context) {
        super(context, new GhastModel(context.bakeLayer(ModelLayers.GHAST)), 0.75F);
    }

    @Override
    public GhastRenderState createRenderState() {
        return new GhastRenderState();
    }

    /** Draws the baked-large ghast mesh down to the mourner's hitbox. */
    @Override
    protected void scale(GhastRenderState state, PoseStack poseStack) {
        poseStack.scale(SCALE, SCALE, SCALE);
    }

    @Override
    public Identifier getTextureLocation(GhastRenderState state) {
        return TEXTURE;
    }
}
