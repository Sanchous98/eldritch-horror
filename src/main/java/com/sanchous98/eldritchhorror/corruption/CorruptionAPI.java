package com.sanchous98.eldritchhorror.corruption;

import net.minecraft.server.level.ServerPlayer;

/**
 * Public entry point to the per-player corruption meter for content — items, rites, mobs, effects.
 *
 * <p>Per {@code design/27-systems-framework.md} (lines 34-35), content <b>never touches the
 * attachment directly</b>; it calls this facade, which is a thin, server-authoritative delegation
 * to {@link CorruptionSystem}. Values are clamped to {@code [0, DEFAULT_MAX]} by the system.
 *
 * <p>For the per-chunk taint field, use {@link TaintAPI}. Every method takes a
 * {@link ServerPlayer}: corruption is server-authoritative and content applies it on the server.
 */
public final class CorruptionAPI {
    private CorruptionAPI() {
    }

    /** Current player corruption, clamped to {@code [0, DEFAULT_MAX]}. */
    public static double get(ServerPlayer player) {
        return CorruptionSystem.get(player);
    }

    /** Adds {@code delta} (may be negative). Returns the new value. */
    public static double add(ServerPlayer player, double delta) {
        return CorruptionSystem.add(player, delta);
    }

    /** Sets player corruption (clamped). Returns the stored value. */
    public static double set(ServerPlayer player, double value) {
        return CorruptionSystem.set(player, value);
    }
}
