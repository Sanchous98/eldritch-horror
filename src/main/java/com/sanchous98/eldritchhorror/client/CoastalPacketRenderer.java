package com.sanchous98.eldritchhorror.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.BoatRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.BoatRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;

/**
 * Placeholder look for the coastal packet: the vanilla oak boat scaled up uniformly ({@value #SCALE}x)
 * until the art pass supplies a hull model. Uniform scale is deliberate — the outer pose scale is
 * rotation-invariant, so the ship stretches neither along nor across its heading.
 *
 * <p>Client-only, registered from {@link ClientEntityRenderers} (a {@code Dist.CLIENT} subscriber);
 * the dedicated server never loads this class.
 */
public final class CoastalPacketRenderer extends BoatRenderer {
    /** Hull scale against the vanilla boat. */
    private static final float SCALE = 3.2F;

    public CoastalPacketRenderer(EntityRendererProvider.Context context) {
        super(context, ModelLayers.OAK_BOAT);
        this.shadowRadius = 2.0F;
    }

    @Override
    public void submit(BoatRenderState state, PoseStack poseStack,
                       SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.scale(SCALE, SCALE, SCALE);
        super.submit(state, poseStack, submitNodeCollector, camera);
        poseStack.popPose();
    }
}
