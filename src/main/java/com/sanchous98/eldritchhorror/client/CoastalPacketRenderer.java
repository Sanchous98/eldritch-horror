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
 * Draws the liner with its own 91-block model ({@link CoastalPacketModel}).
 *
 * <p>The transform mirrors the vanilla boat renderer: a half turn plus the entity yaw, the
 * model-space Y flip, then a quarter turn so the model's bow axis lands on the entity's forward
 * axis. There is no buoyancy lift any more: the model's origin is the entity origin, so the deck
 * that is drawn is exactly the deck the hull AABB exposes to players. Lanterns are a separate
 * baked part drawn with the eyes render type, which keeps them lit at night without making the
 * whole ship full-bright.
 *
 * <p>The culling box is inflated vertically for the masts and funnels (the entity box only reaches
 * the deck) and horizontally for the hulls of the lifeboats, so the whole vessel is frustum-culled
 * as one object. The model is static: extraction copies the yaw and submit allocates nothing.
 */
public final class CoastalPacketRenderer
        extends EntityRenderer<CoastalPacket, CoastalPacketRenderState> {

    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
            EldritchHorror.MODID, "textures/entity/coastal_packet.png");

    private final CoastalPacketModel model;
    private final ModelPart lanterns;

    public CoastalPacketRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new CoastalPacketModel(context.bakeLayer(CoastalPacketModel.LAYER));
        this.lanterns = context.bakeLayer(CoastalPacketModel.LANTERN_LAYER).getChild("lanterns");
        this.shadowRadius = 7.0F;
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
     * The entity hitbox is the deck, 6.4 blocks tall; the model rises to the masthead (about 21
     * blocks) and the lifeboats hang a little past the deck sides. Inflate vertically for the
     * rig and horizontally for the overhangs so nothing pops out at the screen edge.
     */
    @Override
    protected AABB getBoundingBoxForCulling(CoastalPacket entity, float partialTicks) {
        return super.getBoundingBoxForCulling(entity, partialTicks).inflate(2.0, 20.0, 2.0);
    }
}
