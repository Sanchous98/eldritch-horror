package com.sanchous98.eldritchhorror.sanity;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.registry.ModAttachments;
import com.sanchous98.eldritchhorror.registry.ModAttributes;
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
    /** Attribute id for the max sanity ceiling. */
    public static final Identifier MAX_SANITY_ID =
            Identifier.fromNamespaceAndPath(EldritchHorror.MODID, "max_sanity");

    /** Fallback ceiling, used only if the {@code max_sanity} attribute is somehow absent. */
    public static final double DEFAULT_MAX = 100.0;

    private SanitySystem() {
    }

    /** Current sanity, clamped to {@code [0, max(player)]}. */
    public static double get(ServerPlayer player) {
        return Math.clamp(player.getData(ModAttachments.SANITY.get()), 0.0, max(player));
    }

    /** Sets sanity to {@code value} (clamped to {@code [0, max(player)]}). Returns the stored value. */
    public static double set(ServerPlayer player, double value) {
        double v = Math.clamp(value, 0.0, max(player));
        player.setData(ModAttachments.SANITY.get(), v);
        return v;
    }

    /** Adds {@code delta} (may be negative). Returns the new value. */
    public static double add(ServerPlayer player, double delta) {
        return set(player, get(player) + delta);
    }

    /** The meter ceiling for {@code player}: the {@code max_sanity} attribute, or {@link #DEFAULT_MAX}. */
    public static double max(net.minecraft.world.entity.player.Player player) {
        var instance = player.getAttribute(ModAttributes.MAX_SANITY);
        if (instance == null) {
            return DEFAULT_MAX; // attribute absent (non-player or removed): never throw
        }
        double value = instance.getValue();
        return value > 0.0 ? value : DEFAULT_MAX;
    }

    /** The current state band for {@code player}, derived from the meter and the configured cut-offs. */
    public static SanityState state(ServerPlayer player) {
        return SanityState.of(get(player), max(player));
    }
}
