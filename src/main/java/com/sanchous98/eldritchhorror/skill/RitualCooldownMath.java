package com.sanchous98.eldritchhorror.skill;

/**
 * The rite cooldown arithmetic, deliberately free of game state so it can be asserted on without a
 * running game (mirroring {@code cult/ReputationDecayMath}).
 *
 * <p>Two independent reductions stack <b>multiplicatively</b>: {@link Skills#RITE_AFFINITY} shortens
 * the wait 10%, and the carried {@code ring_of_focus} another 20%. The result is never below one
 * tick, and a zero base keeps its "cooldown disabled" meaning.
 */
public final class RitualCooldownMath {

    /** Cooldown factor from {@link Skills#RITE_AFFINITY} (+10% ritual speed ⇒ −10% wait). */
    public static final double RITE_AFFINITY_FACTOR = 0.90;
    /** Cooldown factor from the carried {@code ring_of_focus} (design/16: −20% casting time). */
    public static final double RING_OF_FOCUS_FACTOR = 0.80;

    private RitualCooldownMath() {
    }

    /**
     * @param baseTicks   the configured cooldown in ticks ({@code 0} disables it)
     * @param riteAffinity whether {@link Skills#RITE_AFFINITY} is unlocked
     * @param ringOfFocus  whether the player carries a {@code ring_of_focus}
     * @return the effective cooldown, at least one tick when {@code baseTicks > 0}
     */
    public static int cooldownTicks(int baseTicks, boolean riteAffinity, boolean ringOfFocus) {
        if (baseTicks <= 0) {
            return baseTicks;
        }
        double ticks = baseTicks;
        if (riteAffinity) {
            ticks *= RITE_AFFINITY_FACTOR;
        }
        if (ringOfFocus) {
            ticks *= RING_OF_FOCUS_FACTOR;
        }
        return Math.max(1, (int) Math.floor(ticks));
    }
}
