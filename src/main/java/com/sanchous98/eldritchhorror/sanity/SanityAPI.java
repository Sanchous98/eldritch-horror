package com.sanchous98.eldritchhorror.sanity;

import net.minecraft.server.level.ServerPlayer;

/**
 * Public entry point to the sanity meter for content — items, rites, mobs, effects.
 *
 * <p>Per {@code design/27-systems-framework.md} (lines 34-35), content <b>never touches the
 * attachment directly</b>; it calls this facade, which is a thin, server-authoritative delegation
 * to {@link SanitySystem}. All reads/writes clamp via the system, so a caller cannot exceed
 * {@code [0, max(player)]}.
 *
 * <p>Every method takes a {@link ServerPlayer}: sanity is server-authoritative and the client must
 * never mutate it (the client only displays the synced value on the HUD).
 */
public final class SanityAPI {
    private SanityAPI() {
    }

    /** Current sanity of {@code player}, clamped to {@code [0, max(player)]}. */
    public static double get(ServerPlayer player) {
        return SanitySystem.get(player);
    }

    /** Adds {@code delta} (may be negative, e.g. a tome's cost). Returns the new value. */
    public static double add(ServerPlayer player, double delta) {
        return SanitySystem.add(player, delta);
    }

    /** Sets sanity to {@code value}, clamped to {@code [0, max(player)]}. Returns the stored value. */
    public static double set(ServerPlayer player, double value) {
        return SanitySystem.set(player, value);
    }

    /** The meter ceiling for {@code player}: the {@code max_sanity} attribute, or the fallback. */
    public static double max(ServerPlayer player) {
        return SanitySystem.max(player);
    }
}
