package com.sanchous98.eldritchhorror.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.client.model.CoastalPacketModel;
import com.sanchous98.eldritchhorror.client.model.CoastalPacketRenderState;
import com.sanchous98.eldritchhorror.entity.CoastalPacket;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.AABB;

/**
 * Draws the coastal packet with its own model ({@link CoastalPacketModel}) instead of a scaled
 * vanilla boat.
 *
 * <p>The transform mirrors {@code AbstractBoatRenderer}: a half turn plus the entity yaw, the
 * model-space Y flip, then a quarter turn so the model's bow axis lands on the entity's forward
 * axis. A vertical offset lifts the model to the boat's buoyancy waterline (see
 * {@link #WATERLINE_LIFT}). Lanterns are a separate baked part drawn with the eyes render type,
 * which keeps them lit at night without making the whole ship full-bright.
 *
 * <p>Client-only, registered from {@link ClientEntityRenderers} (a {@code Dist.CLIENT}
 * subscriber); the dedicated server never loads this class.
 */
public final class CoastalPacketRenderer
        extends EntityRenderer<CoastalPacket, CoastalPacketRenderState> {

    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
            EldritchHorror.MODID, "textures/entity/coastal_packet.png");
    /**
     * The packet is an {@code AbstractBoat}: its equilibrium is 65% of the hitbox height below the
     * water surface. With the 3.6-block hitbox that is 2.34 blocks, so the model and the seat
     * heights are lifted by that much (plus a little freeboard) to keep the deck out of the sea
     * instead of awash.
     */
    private static final float WATERLINE_LIFT = 2.9F;

    private final CoastalPacketModel model;
    private final ModelPart lanterns;

    public CoastalPacketRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new CoastalPacketModel(context.bakeLayer(CoastalPacketModel.LAYER));
        this.lanterns = context.bakeLayer(CoastalPacketModel.LANTERN_LAYER).getChild("lanterns");
        this.shadowRadius = 2.6F;
    }

    @Override
    public CoastalPacketRenderState createRenderState() {
        return new CoastalPacketRenderState();
    }

    @Override
    public void extractRenderState(CoastalPacket entity, CoastalPacketRenderState state,
                                   float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.yRot = entity.getYRot(partialTicks);
    }

    @Override
    public void submit(CoastalPacketRenderState state, PoseStack poseStack,
                       SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.translate(0.0F, WATERLINE_LIFT, 0.0F);
        poseStack.rotateDegrees(Axis.YP, 180.0F - state.yRot);
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.rotateDegrees(Axis.YP, 90.0F);
        submitNodeCollector.submitModel(this.model, state, poseStack, TEXTURE,
                state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
        submitNodeCollector.submitModelPart(this.lanterns, poseStack,
                RenderTypes.eyes(TEXTURE), state.lightCoords, OverlayTexture.NO_OVERLAY, null);
        poseStack.popPose();
        super.submit(state, poseStack, submitNodeCollector, camera);
    }

    /**
     * The entity hitbox is only as wide as the beam; the model is much longer. Inflate the culling
     * box so the bow and stern do not pop out while the hull centre is off screen.
     */
    @Override
    protected AABB getBoundingBoxForCulling(CoastalPacket entity, float partialTicks) {
        return super.getBoundingBoxForCulling(entity, partialTicks).inflate(8.0);
    }
}
