package com.sanchous98.eldritchhorror.world.rift;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import org.jspecify.annotations.Nullable;

/**
 * Game-free arithmetic for rift gates (design/26, "The rift gate"): who may travel, what the trip
 * costs, whether the arrival is unstable, and where a body can safely stand on arrival.
 *
 * <p>It touches no level and no Minecraft type: the caller supplies the world facts (rite known,
 * link present, destination block still standing, same dimension) and a {@link Predicate} for "this
 * position is replaceable / free". That keeps every decision unit-testable without a server, exactly
 * like {@link com.sanchous98.eldritchhorror.world.ward.WardMath}.
 */
public final class GateMath {

    /** Horizontal radius searched for a safe standing spot beside a destination gate. */
    public static final int ARRIVAL_RADIUS = 2;

    /** Horizontal radius searched for the fresh gate an unstable arrival tears open. */
    public static final int INSTABILITY_RADIUS = 4;

    /** Minimum squared distance between the arrival and a fresh instability gate (2 blocks). */
    private static final int INSTABILITY_MIN_DISTANCE_SQ = 4;

    /** Vertical offsets tried for a standing/placement spot, in preference order. */
    private static final int[] Y_OFFSETS = {0, 1, -1, 2, -2};

    private GateMath() {
    }

    /** A plain block coordinate, so the search stays free of Minecraft types and unit-testable. */
    public record Spot(int x, int y, int z) {

        /** @return this spot shifted by {@code (dx, dy, dz)}. */
        public Spot offset(int dx, int dy, int dz) {
            return new Spot(this.x + dx, this.y + dy, this.z + dz);
        }
    }

    /**
     * Why a use of a linked gate is refused, in the order the checks apply. {@link #NONE} means the
     * travel may proceed. An <em>unlinked</em> gate is not travel at all: it takes the marking path,
     * so it is reported as {@link #UNLINKED} only when travel was actually requested.
     */
    public enum Refusal {
        /** Travel is allowed. */
        NONE,
        /** Rift gates are switched off in the config. */
        DISABLED,
        /** The player has not learned {@code open_rift}. */
        UNKNOWN_RITE,
        /** This gate has no twin yet. */
        UNLINKED,
        /** The twin is in another dimension (a link can never span worlds). */
        CROSS_DIMENSION,
        /** The twin anchor block no longer exists. */
        DEAD_LINK
    }

    /**
     * The exact price and instability of one successful trip.
     *
     * @param corruption corruption added on arrival
     * @param sanity     sanity delta applied on arrival (negative drains)
     * @param unstable   whether a fresh gate tears open near the arrival
     */
    public record Cost(double corruption, double sanity, boolean unstable) {

        /** The null cost: nothing is charged and nothing opens. */
        public static final Cost NONE = new Cost(0.0, 0.0, false);
    }

    /**
     * Applies the gate's checks in a fixed order: disabled, rite unknown, no link, cross-dimension,
     * dead destination. The first failing check wins, so the player sees the most fundamental reason.
     *
     * @param enabled            whether {@code enableRiftGates} is on
     * @param knowsRite          whether the player knows {@code open_rift}
     * @param linked             whether this gate has a stored twin
     * @param sameDimension      whether the twin is in this gate's level
     * @param destinationPresent whether the twin anchor block still stands
     */
    public static Refusal refusal(boolean enabled, boolean knowsRite, boolean linked,
                                  boolean sameDimension, boolean destinationPresent) {
        if (!enabled) {
            return Refusal.DISABLED;
        }
        if (!knowsRite) {
            return Refusal.UNKNOWN_RITE;
        }
        if (!linked) {
            return Refusal.UNLINKED;
        }
        if (!sameDimension) {
            return Refusal.CROSS_DIMENSION;
        }
        if (!destinationPresent) {
            return Refusal.DEAD_LINK;
        }
        return Refusal.NONE;
    }

    /**
     * Resolves one trip. A refusal charges nothing and tears nothing open, so the price is applied
     * <b>exactly once</b> and only on success; the configured magnitudes are passed through
     * unchanged (the caller passes a negative sanity amount).
     *
     * @param refusal  the verdict from {@link #refusal}
     * @param corrupt  the configured corruption price (added)
     * @param sanity   the configured sanity delta (negative drains)
     * @param chance   the configured instability chance, clamped to {@code [0, 1]}
     * @param roll     the arrival roll in {@code [0, 1)}
     */
    public static Cost resolve(Refusal refusal, double corrupt, double sanity, double chance,
                               double roll) {
        if (refusal != Refusal.NONE) {
            return Cost.NONE;
        }
        return new Cost(corrupt, sanity, unstable(roll, chance));
    }

    /**
     * Whether a fresh gate opens on this arrival: a roll strictly below {@code chance}. Boundaries
     * are therefore testable — {@code roll = 0} always opens when the chance is positive, and
     * {@code roll = 1} never opens. A chance outside {@code [0, 1]} is clamped.
     */
    public static boolean unstable(double roll, double chance) {
        double clamped = Math.clamp(chance, 0.0, 1.0);
        return roll >= 0.0 && roll < clamped;
    }

    /**
     * The first free position near {@code anchor} for a body to stand on arrival. Candidates are
     * tried at the anchor's own height first, then one above, one below, two above, two below, and
     * within each height by increasing horizontal distance. The search is bounded by
     * {@link #ARRIVAL_RADIUS}, so a gate with no reachable ground reports {@code null} instead of
     * scanning the world.
     *
     * @param anchor the destination gate's block position
     * @param free   the caller's world predicate: replaceable block with headroom and a floor
     * @return the chosen feet position, or {@code null} when nothing within the radius qualifies
     */
    public static @Nullable Spot arrivalOffset(Spot anchor, Predicate<Spot> free) {
        return nearestFree(anchor, ARRIVAL_RADIUS, free);
    }

    /**
     * The spot for the fresh gate an unstable arrival opens: a free position at least two blocks
     * from the arrival, preferred on the arrival's own height. The caller decides what "free" means
     * (replaceable with solid ground below) and may further reject occupied positions.
     *
     * @param arrival where the player arrived (never chosen itself)
     * @param free    the caller's world predicate
     * @return the chosen gate position, or {@code null} when the ground does not allow one
     */
    public static @Nullable Spot instabilitySpot(Spot arrival, Predicate<Spot> free) {
        return nearestFree(arrival, INSTABILITY_RADIUS, pos -> {
            int dx = pos.x() - arrival.x();
            int dy = pos.y() - arrival.y();
            int dz = pos.z() - arrival.z();
            return dx * dx + dy * dy + dz * dz >= INSTABILITY_MIN_DISTANCE_SQ && free.test(pos);
        });
    }

    /**
     * Deterministic nearest-free search over the {@link #Y_OFFSETS} planes. Kept public so a caller
     * can search a different radius; {@link #arrivalOffset} is the arrival-shaped shortcut.
     */
    public static @Nullable Spot nearestFree(Spot origin, int radius, Predicate<Spot> free) {
        if (radius < 0) {
            return null;
        }
        for (int dy : Y_OFFSETS) {
            List<Spot> ring = new ArrayList<>();
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (dx * dx + dz * dz > radius * radius) {
                        continue;
                    }
                    ring.add(origin.offset(dx, dy, dz));
                }
            }
            ring.sort(Comparator.comparingInt((Spot p) -> dxzSquared(origin, p))
                    .thenComparingInt(Spot::x)
                    .thenComparingInt(Spot::z));
            for (Spot candidate : ring) {
                if (free.test(candidate)) {
                    return candidate;
                }
            }
        }
        return null;
    }

    private static int dxzSquared(Spot origin, Spot pos) {
        int dx = pos.x() - origin.x();
        int dz = pos.z() - origin.z();
        return dx * dx + dz * dz;
    }
}
