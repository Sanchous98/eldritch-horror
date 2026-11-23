package com.sanchous98.eldritchhorror.client.model;

import net.minecraft.client.renderer.entity.state.EntityRenderState;

/**
 * Render state for the coastal packet. Only the body yaw is needed: the hull is rigid, so the
 * model is drawn at the entity's interpolated heading and every seat stays exactly where the
 * server puts its rider.
 */
public final class CoastalPacketRenderState extends EntityRenderState {
    /** Interpolated body yaw, in degrees. */
    public float yRot;
}
