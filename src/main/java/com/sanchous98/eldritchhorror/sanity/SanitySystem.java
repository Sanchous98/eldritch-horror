package com.sanchous98.eldritchhorror.sanity;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.registry.ModAttachments;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

/**
 * The Sanity system: a per-player RPG meter.
 *
 * <p><b>This is a stub.</b> It stores a value that can go up and down and persists/copies on
 * death, but nothing reads it for gameplay yet — no drain sources, no thresholds, no effects. The
 * shape is deliberately final so content can be built against it now (see
 * {@code design/27-systems-framework.md}).
 *
 * <p>Later: composable {@code SanitySource}s tick it down/up, thresholds apply the {@code Madness}
 * effects on transition, and it replaces the hunger bar on the HUD.
 */
public final class SanitySystem {
    /** Attribute id for max sanity (registered once the attribute is implemented). */
    public static final Identifier MAX_SANITY_ID =
            Identifier.fromNamespaceAndPath(EldritchHorror.MODID, "max_sanity");

    /** Default ceiling. A future {@code max_sanity} attribute will override this. */
    public static final double DEFAULT_MAX = 100.0;

    private SanitySystem() {
    }

    /** Current sanity, clamped to {@code [0, DEFAULT_MAX]}. */
    public static double get(ServerPlayer player) {
        return clamp(player.getData(ModAttachments.SANITY.get()));
    }

    /** Sets sanity to {@code value} (clamped). Returns the stored value. */
    public static double set(ServerPlayer player, double value) {
        double v = clamp(value);
        player.setData(ModAttachments.SANITY.get(), v);
        return v;
    }

    /** Adds {@code delta} (may be negative). Returns the new value. */
    public static double add(ServerPlayer player, double delta) {
        return set(player, get(player) + delta);
    }

    private static double clamp(double v) {
        return Math.max(0.0, Math.min(DEFAULT_MAX, v));
    }
}
