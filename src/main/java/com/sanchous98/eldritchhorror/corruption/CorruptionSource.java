package com.sanchous98.eldritchhorror.corruption;

import net.minecraft.server.level.ServerPlayer;

/**
 * One composable influence on a player's corruption.
 *
 * <p>The framework sums the deltas of every enabled source once per second and clamps the result
 * (see {@code design/27-systems-framework.md}). A source is a pure, server-only function of the
 * player and the cheap {@link CorruptionContext}; it must be deterministic (no {@code Math.random()})
 * and must not touch the attachment directly — {@link CorruptionSystem} owns the value.
 */
public interface CorruptionSource {

    /** Stable id used in config/diagnostics, e.g. {@code "taint"} or {@code "altar"}. */
    String id();

    /** Positive increases corruption, negative cleanses, in points per second. */
    double deltaPerSecond(ServerPlayer player, CorruptionContext ctx);
}
