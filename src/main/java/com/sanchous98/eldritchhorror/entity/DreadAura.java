package com.sanchous98.eldritchhorror.entity;

/**
 * Shared trait for bestiary entities that are a <b>presence</b> rather than a fight: standing near
 * one slowly moves a meter. The risen husk's sanity drain established the pattern per-mob; this is
 * the reusable form the design asks for ({@code design/25-bestiary-and-entities.md}, "shared trait/
 * behaviour library"), so a corruption vector and a sanity swarm are the same code with a different
 * axis.
 *
 * <p>Implementors return their numbers from config (so every aura is toggle- and rate-configurable)
 * and {@link BestiarySupport#tickAura} does the single, radius-bounded, loaded-entities-only world
 * query once per second. All values are server-authoritative and capped by {@link #auraMaxStack()}.
 */
public interface DreadAura {

    /** Which meter the aura moves; every entity of one type shares the same axis. */
    enum Axis {
        /** Adds to the per-player sanity meter (negative rate drains). */
        SANITY,
        /** Adds to the per-player corruption meter (positive rate taints). */
        CORRUPTION
    }

    /** The meter this aura writes through {@code SanityAPI}/{@code CorruptionAPI}. */
    Axis auraAxis();

    /** Whether the aura applies at all this tick (the per-mob config enable plus master switch). */
    boolean isAuraActive();

    /** Meter change per second contributed per counted aura (negative drains sanity). */
    double auraRate();

    /**
     * Meter change per second for a specific player; by default the flat {@link #auraRate()}. Bosses
     * override this to make the drain worse while the player sleeps (the dream-leak).
     */
    default double auraRateFor(net.minecraft.server.level.ServerPlayer player) {
        return auraRate();
    }

    /** Radius (blocks) within which this aura counts for a player. */
    double auraRadius();

    /** Most auras of this type counted for one player at once, so a horde stays bounded. */
    int auraMaxStack();

    /**
     * Whether the aura only applies when the mob can actually see the player. The default is
     * proximity alone (the husk/choir/swarm shape); the star-spawn overrides this to true so its
     * heavy drain is a <b>line-of-sight</b> threat you can break by breaking sight
     * ({@code design/25-bestiary-and-entities.md}, "witnessing/line-of-sight is the primary drain").
     */
    default boolean auraRequiresLineOfSight() {
        return false;
    }
}
