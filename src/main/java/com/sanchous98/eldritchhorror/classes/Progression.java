package com.sanchous98.eldritchhorror.classes;

import net.minecraft.server.level.ServerPlayer;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * The class archetype multipliers the per-second systems read, as fractions of the baseline
 * {@code 1.0}. {@code SanityTicker} scales sanity <b>drain</b> by {@link #sanityDrainMultiplier} and
 * {@code CorruptionTicker} scales corruption <b>gain</b> by {@link #corruptionGainMultiplier} —
 * recovery and cleansing are never penalised. {@link #maxSanityBonus} is the single source of the
 * bonus both {@link ClassAPI} applies to the {@code max_sanity} attribute.
 *
 * <p>Values from {@code design/14-classes.md}: investigator is sanity-resilient and
 * corruption-resistant; the occultist trades sanity for faster learning; the cultist gains
 * corruption fastest and drains sanity fastest. Per {@code design/29-prologue.md}, only the
 * currently-live bonuses are wired; the rest are recorded here for when their systems land.
 */
@NullMarked
public final class Progression {

    private Progression() {
    }

    /** The archetype's max-sanity bonus fraction applied by {@link ClassAPI} ({@code 0.0} = none). */
    public static double maxSanityBonus(ClassId id) {
        return switch (id) {
            case INVESTIGATOR -> 0.20;
            case OCCULTIST -> -0.10;
            case CULTIST -> 0.0;
        };
    }

    /** The archetype's sanity-drain multiplier ({@code 1.0} = no change). */
    public static double sanityDrainMultiplier(ServerPlayer player) {
        return switch (classOf(player)) {
            case INVESTIGATOR -> 1.0;
            case OCCULTIST -> 1.0;
            case CULTIST -> 1.25;
            case null -> 1.0;
        };
    }

    /** The archetype's corruption-gain multiplier ({@code 1.0} = no change). */
    public static double corruptionGainMultiplier(ServerPlayer player) {
        return switch (classOf(player)) {
            case INVESTIGATOR -> 0.75;
            case OCCULTIST -> 1.0;
            case CULTIST -> 1.0;
            case null -> 1.0;
        };
    }

    private static @Nullable ClassId classOf(ServerPlayer player) {
        return ClassAPI.get(player);
    }
}
