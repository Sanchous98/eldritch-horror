package com.sanchous98.eldritchhorror.registry.items;

/**
 * The weapons' pure arithmetic ({@code design/16-items.md}), deliberately free of game state so
 * JUnit can assert on it without a running server.
 *
 * <p>Every number here is small on purpose: the design asks for a nudge (a fifth to a half again),
 * not a new damage tier. {@link #scaled} is the single gate that keeps an odd input (a negative
 * base, a non-positive multiplier) from turning into negative or "healing" damage.
 */
public final class WeaponMath {

    /** Corruption at which the sacrificial dagger reaches its full bonus (the meter's ceiling). */
    public static final double CORRUPTION_FULL = 100.0;

    /** Extra dagger damage at full corruption, as a fraction of the base hit (+50%). */
    public static final double DAGGER_BONUS_AT_FULL = 0.5;

    /** The tidal trident's underwater bonus, as a fraction of the base hit (+25%). */
    public static final double WATER_BONUS = 0.25;

    /** The veil bow's flat bonus against an eldritch target, in damage points. */
    public static final float VEIL_BOW_ELDRITCH_BONUS = 2.0F;

    private WeaponMath() {
    }

    /**
     * The sacrificial dagger's damage multiplier: {@code 1.0} at no corruption, rising linearly to
     * {@code 1.5} at {@link #CORRUPTION_FULL}. Corruption outside the meter's range is clamped, so a
     * negative or over-cap value can never invert or inflate the curve.
     */
    public static double daggerMultiplier(double corruption) {
        double clamped = Math.clamp(corruption, 0.0, CORRUPTION_FULL);
        return 1.0 + clamped / CORRUPTION_FULL * DAGGER_BONUS_AT_FULL;
    }

    /** The tidal trident's multiplier: {@code 1.25} underwater, {@code 1.0} otherwise. */
    public static double waterMultiplier(boolean underwater) {
        return underwater ? 1.0 + WATER_BONUS : 1.0;
    }

    /**
     * A flat bonus applied only when {@code applies} is true, floored at zero. Base damage below
     * zero returns {@code 0}, never a negative hit that would heal the target.
     */
    public static float bonusDamage(float damage, float bonus, boolean applies) {
        if (damage <= 0.0F) {
            return 0.0F;
        }
        return applies ? damage + bonus : damage;
    }

    /**
     * Multiplies {@code damage} by {@code multiplier}, floored at zero. A non-positive base or
     * multiplier yields {@code 0}; this is the clamp that keeps a broken event value from producing
     * negative damage.
     */
    public static float scaled(float damage, double multiplier) {
        if (damage <= 0.0F || multiplier <= 0.0) {
            return 0.0F;
        }
        return (float) (damage * multiplier);
    }
}
