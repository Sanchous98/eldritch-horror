package com.sanchous98.eldritchhorror.world.transport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Guards the game-free timetable arithmetic (design/30): daily departures, the strict
 * after-now rule, service periods, the boarding window, voyage duration and the fare.
 */
class TransportMathTest {

    private static final int SIX = 6;
    private static final int EIGHTEEN = 18;

    @Test
    void departureHoursUseTheVanillaDayClock() {
        assertEquals(0, TransportMath.departureTicks(6));
        assertEquals(12000, TransportMath.departureTicks(18));
        assertEquals(18000, TransportMath.departureTicks(0));
    }

    @Test
    void nextDepartureIsTodayWhenBeforeTheFirst() {
        assertEquals(12000L, TransportMath.nextDeparture(0L, SIX, EIGHTEEN, 1));
        assertEquals(12000L, TransportMath.nextDeparture(11999L, SIX, EIGHTEEN, 1));
    }

    @Test
    void nextDepartureIsStrictlyAfterNow() {
        assertEquals(24000L, TransportMath.nextDeparture(12000L, SIX, EIGHTEEN, 1));
        assertEquals(36000L, TransportMath.nextDeparture(24000L, SIX, EIGHTEEN, 1));
    }

    @Test
    void servicePeriodSkipsOffDays() {
        // Day 1 is not a service day when the period is 2; day 2 is.
        long dayOneNoon = 24000L + 6000L;
        assertEquals(2L * 24000L, TransportMath.nextDeparture(dayOneNoon, SIX, EIGHTEEN, 2));
    }

    @Test
    void equalDepartureHoursAreNotDoubleCounted() {
        assertEquals(24000L, TransportMath.nextDeparture(0L, SIX, SIX, 1));
        assertEquals(48000L, TransportMath.nextDeparture(24000L, SIX, SIX, 1));
    }

    @Test
    void boardingWindowIsTheHourBeforeDeparture() {
        long departure = 12000L;
        long opens = TransportMath.boardingOpens(departure, 60);
        assertEquals(11000L, opens);
        assertFalse(TransportMath.isBoarding(opens - 1L, departure, 60));
        assertTrue(TransportMath.isBoarding(opens, departure, 60));
        assertTrue(TransportMath.isBoarding(departure - 1L, departure, 60));
        assertFalse(TransportMath.isBoarding(departure, departure, 60));
    }

    @Test
    void durationFollowsDistanceAndSpeed() {
        assertEquals(2000, TransportMath.durationTicks(3000.0, 1.5));
        assertEquals(2000L, TransportMath.arrival(6000L, 3000.0, 1.5) - 6000L);
        assertEquals(1, TransportMath.durationTicks(0.1, 1.5));
        assertEquals(0, TransportMath.durationTicks(0.0, 1.5));
    }

    @Test
    void fareIsAllOrNothing() {
        assertTrue(TransportMath.canAfford(2, 2));
        assertFalse(TransportMath.canAfford(1, 2));
        assertEquals(3, TransportMath.remainingAfterFare(5, 2));
        assertEquals(-1, TransportMath.remainingAfterFare(1, 2));
        assertTrue(TransportMath.canAfford(0, 0));
    }

    @Test
    void clockFormatsAsHoursAndMinutes() {
        assertEquals("06:00", TransportMath.formatTime(0L));
        assertEquals("12:00", TransportMath.formatTime(6000L));
        assertEquals("18:00", TransportMath.formatTime(12000L));
        assertEquals("00:00", TransportMath.formatTime(18000L));
    }
}
