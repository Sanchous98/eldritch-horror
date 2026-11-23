package com.sanchous98.eldritchhorror.client;

import net.minecraft.client.renderer.entity.state.EntityRenderState;

/**
 * Per-frame state for {@link TrainCarRenderer}: the interpolated heading of the car. Everything
 * else the carriage model needs is static geometry, so this stays deliberately small.
 */
public final class TrainRenderState extends EntityRenderState {
    /** Interpolated yaw in degrees (0 = +Z, as for every entity). */
    public float yRot;
}
