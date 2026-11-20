package com.sanchous98.eldritchhorror.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.sanchous98.eldritchhorror.entity.TrainCar;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.cart.MinecartModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.MinecartRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;

/**
 * Placeholder look for a train car: the vanilla minecart model scaled up until the art pass
 * supplies a proper locomotive and carriage.
 *
 * <p>One renderer serves both entity types; the registration supplies the scale (the locomotive is
 * slightly larger than a wagon). The model is the same baked {@link ModelLayers#MINECART} layer, so
 * no custom layer definition is needed. Client-only, registered from {@link ClientEntityRenderers}
 * (a {@code Dist.CLIENT} subscriber); the dedicated server never loads this class.
 *
 * @param <T> locomotive or wagon
 */
public final class TrainCarRenderer<T extends TrainCar>
        extends EntityRenderer<T, MinecartRenderState> {
    private static final Identifier TEXTURE =
            Identifier.withDefaultNamespace("textures/entity/minecart/minecart.png");

    private final MinecartModel model;
    private final float scale;

    /**
     * @param context renderer context
     * @param scale   uniform model scale against the vanilla minecart
     */
    public TrainCarRenderer(EntityRendererProvider.Context context, float scale) {
        super(context);
        this.model = new MinecartModel(context.bakeLayer(ModelLayers.MINECART));
        this.scale = scale;
        this.shadowRadius = 1.2F * scale;
    }

    @Override
    public MinecartRenderState createRenderState() {
        return new MinecartRenderState();
    }

    @Override
    public void extractRenderState(T car, MinecartRenderState state, float partialTicks) {
        super.extractRenderState(car, state, partialTicks);
        state.yRot = car.getYRot(partialTicks);
        state.xRot = car.getXRot(partialTicks);
    }

    @Override
    public void submit(MinecartRenderState state, PoseStack poseStack,
                       SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        super.submit(state, poseStack, submitNodeCollector, camera);
        poseStack.pushPose();
        poseStack.translate(0.0F, 0.375F * this.scale, 0.0F);
        poseStack.rotateDegrees(Axis.YP, 180.0F - state.yRot);
        poseStack.scale(this.scale, this.scale, this.scale);
        submitNodeCollector.submitModel(this.model, state, poseStack, TEXTURE,
                state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
        poseStack.popPose();
    }
}
