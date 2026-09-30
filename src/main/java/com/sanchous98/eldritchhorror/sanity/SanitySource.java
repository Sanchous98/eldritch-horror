package com.sanchous98.eldritchhorror.sanity;

import net.minecraft.server.level.ServerPlayer;

/**
 * One composable influence on a player's sanity.
 *
 * <p>The framework sums the deltas of every enabled source once per second and clamps the result
 * (see {@code design/27-systems-framework.md}). A source is a pure, server-only function of the
 * player and the cheap {@link SanityContext}; it must be deterministic (no {@code Math.random()})
 * and must not touch the attachment directly — {@link SanitySystem} owns the value.
 */
public interface SanitySource {

    /** Stable id used in config/diagnostics, e.g. {@code "darkness"} or {@code "city"}. */
    String id();

    /** Positive recovers, negative drains, in points per second. */
    double deltaPerSecond(ServerPlayer player, SanityContext ctx);
}
