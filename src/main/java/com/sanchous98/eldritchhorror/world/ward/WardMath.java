package com.sanchous98.eldritchhorror.world.ward;

/**
 * Game-free arithmetic for wards (design/26, "Warding and sealing"): coverage on the horizontal
 * plane, expiry, and the damage-window bonus. Kept free of Minecraft types so it can be unit-tested
 * without a server; {@link WardLedger} and the listeners supply the world state.
 */
public final class WardMath {

    /** The largest accepted boss damage bonus: +100%. Anything higher is a broken config value. */
    public static final double MAX_BOSS_BONUS = 1.0;

    private WardMath() {
    }

    /**
     * Whether a point offset {@code (dx, dz)} from a ward's centre lies inside its horizontal
     * radius. The ring is circular and the boundary is inclusive — a ward is a cylinder of
     * infinite height, so a player directly above the core is still sheltered.
     *
     * @param dx     horizontal offset from the ward centre (blocks)
     * @param dz     horizontal offset from the ward centre (blocks)
     * @param radius ward radius in blocks; negative values cover nothing
     */
    public static boolean covers(double dx, double dz, double radius) {
        if (radius < 0.0) {
            return false;
        }
        return dx * dx + dz * dz <= radius * radius;
    }

    /**
     * Ticks left until {@code expiresAt}, floored at zero. A ward that never expires
     * ({@link Long#MAX_VALUE}) simply reports the immense remainder.
     */
    public static long remainingTicks(long now, long expiresAt) {
        return Math.max(0L, expiresAt - now);
    }

    /** Whether a ward with {@code expiresAt} is expired at {@code now}. Never-expired is false. */
    public static boolean expired(long now, long expiresAt) {
        return remainingTicks(now, expiresAt) == 0L;
    }

    /**
     * The damage-window multiplier for a blow landed by a player standing in a ward: the base
     * damage plus {@code bonus}, clamped to {@code [0, }{@link #MAX_BOSS_BONUS}{@code ]}. A
     * non-positive base stays zero (damage is never healed), and a non-positive bonus is a no-op.
     */
    public static float scaledBossDamage(float base, double bonus) {
        if (base <= 0.0F) {
            return 0.0F;
        }
        double clamped = Math.clamp(bonus, 0.0, MAX_BOSS_BONUS);
        return (float) (base * (1.0 + clamped));
    }
}
