package com.sanchous98.eldritchhorror.world.rift;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sanchous98.eldritchhorror.world.rift.GateMath.Cost;
import com.sanchous98.eldritchhorror.world.rift.GateMath.Spot;
import com.sanchous98.eldritchhorror.world.rift.GateMath.Refusal;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * Guards the game-free gate arithmetic (design/26, "The rift gate"): the refusal order, the price
 * charged exactly once, the instability boundaries, and the bounded safe-arrival search. No server
 * or level is needed because {@link GateMath} takes the world facts as arguments and a predicate.
 */
class GateMathTest {

    private static final Spot ANCHOR = new Spot(100, 64, 100);
    private static final double EPSILON = 1.0E-9;

    @Test
    void aKnownLinkedDestinationInThisWorldIsAllowed() {
        assertEquals(Refusal.NONE, GateMath.refusal(true, true, true, true, true));
    }

    @Test
    void anUnlinkedGateCannotTravel() {
        assertEquals(Refusal.UNLINKED, GateMath.refusal(true, true, false, true, true));
    }

    @Test
    void aLinkNeverCrossesDimensions() {
        assertEquals(Refusal.CROSS_DIMENSION, GateMath.refusal(true, true, true, false, true));
    }

    @Test
    void aMissingDestinationKillsTheLink() {
        assertEquals(Refusal.DEAD_LINK, GateMath.refusal(true, true, true, true, false));
    }

    @Test
    void theMoreFundamentalRefusalWins() {
        assertEquals(Refusal.DISABLED, GateMath.refusal(false, false, false, false, false));
        assertEquals(Refusal.UNKNOWN_RITE, GateMath.refusal(true, false, true, true, true));
    }

    @Test
    void aSuccessfulTripChargesTheConfiguredPriceExactlyOnce() {
        Cost none = GateMath.resolve(Refusal.NONE, 3.0, -8.0, 0.25, 0.99);
        assertEquals(3.0, none.corruption(), EPSILON);
        assertEquals(-8.0, none.sanity(), EPSILON);
        assertFalse(none.unstable());
    }

    @Test
    void everyRefusalCostsNothing() {
        for (Refusal refusal : Refusal.values()) {
            if (refusal == Refusal.NONE) {
                continue;
            }
            Cost cost = GateMath.resolve(refusal, 3.0, -8.0, 1.0, 0.0);
            assertEquals(0.0, cost.corruption(), EPSILON);
            assertEquals(0.0, cost.sanity(), EPSILON);
            assertFalse(cost.unstable());
        }
    }

    @Test
    void theInstabilityRollBoundariesAreExact() {
        assertTrue(GateMath.unstable(0.0, 0.25));
        assertTrue(GateMath.unstable(0.249_999, 0.25));
        assertFalse(GateMath.unstable(0.25, 0.25));
        assertFalse(GateMath.unstable(1.0, 0.25));
        assertFalse(GateMath.unstable(0.0, 0.0));
        assertTrue(GateMath.unstable(0.999_999, 1.0));
        assertFalse(GateMath.unstable(1.0, 1.0));
    }

    @Test
    void aChanceOutsideTheRangeIsClamped() {
        assertFalse(GateMath.unstable(0.0, -1.0));
        assertTrue(GateMath.unstable(0.5, 4.0));
    }

    @Test
    void arrivalPrefersTheGateHeightThenNearest() {
        Spot spot = new Spot(ANCHOR.x() + 2, ANCHOR.y(), ANCHOR.z());
        Spot arrival = GateMath.arrivalOffset(ANCHOR, p -> p.equals(spot));
        assertEquals(spot, arrival);
    }

    @Test
    void arrivalPrefersASameHeightSpotOverOneAbove() {
        Spot sameHeight = new Spot(ANCHOR.x() - 2, ANCHOR.y(), ANCHOR.z());
        Spot above = new Spot(ANCHOR.x(), ANCHOR.y() + 1, ANCHOR.z());
        Spot arrival = GateMath.arrivalOffset(ANCHOR,
                p -> p.equals(sameHeight) || p.equals(above));
        assertEquals(sameHeight, arrival);
    }

    @Test
    void arrivalIsBoundedAndCanFail() {
        assertNull(GateMath.arrivalOffset(ANCHOR, p -> false));
        Spot far = ANCHOR.offset(GateMath.ARRIVAL_RADIUS + 1, 0, 0);
        assertNull(GateMath.arrivalOffset(ANCHOR, p -> p.equals(far)));
    }

    @Test
    void instabilityKeepsItsDistanceFromTheArrival() {
        Spot arrival = ANCHOR;
        Spot beside = arrival.offset(2, 0, 0);
        Spot tooClose = arrival.offset(1, 0, 0);
        Set<Spot> free = Set.of(tooClose, beside);
        assertEquals(beside, GateMath.instabilitySpot(arrival, free::contains));
    }

    @Test
    void instabilityReportsNoGroundWhenNothingIsFree() {
        assertNull(GateMath.instabilitySpot(ANCHOR, p -> false));
    }
}
