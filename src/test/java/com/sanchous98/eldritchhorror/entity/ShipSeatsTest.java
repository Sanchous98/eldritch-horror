package com.sanchous98.eldritchhorror.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * Guards the deck seating: 32 distinct places, sixteen forward and sixteen aft, all on the open
 * planking clear of the cabin block, with the rider's feet just inside the boards.
 */
class ShipSeatsTest {

    @Test
    void theLinerOffersThirtyTwoDistinctDeckSeats() {
        assertEquals(32, ShipSeats.TOTAL, "the deck carries 32");
        assertTrue(ShipSeats.TOTAL >= 24 && ShipSeats.TOTAL <= 40, "aim 24-40 seats");
        Set<String> seen = new HashSet<>();
        for (int i = 0; i < ShipSeats.TOTAL; i++) {
            double[] seat = ShipSeats.offset(i);
            assertTrue(seen.add(seat[0] + ":" + seat[2]), "duplicate seat " + i);
        }
        assertEquals(ShipSeats.TOTAL, seen.size());
    }

    @Test
    void seatsSitOnTheOpenForeAndAftDecksClearOfTheCabin() {
        int fore = 0;
        int aft = 0;
        for (int i = 0; i < ShipSeats.TOTAL; i++) {
            double[] seat = ShipSeats.offset(i);
            double x = seat[0];
            double z = seat[2];
            assertTrue(ShipGeometry.onDeck(x, z), "seat " + i + " is off the planking");
            assertTrue(Math.abs(z) > ShipGeometry.DECKHOUSE_Z_HALF,
                    "seat " + i + " is inside the cabin block");
            assertTrue(Math.abs(x) <= 7.0, "seat " + i + " is too near the bulwark");
            if (z > 0) {
                fore++;
            } else {
                aft++;
            }
        }
        assertEquals(ShipSeats.TOTAL / 2, fore);
        assertEquals(ShipSeats.TOTAL / 2, aft);
        assertEquals(ShipSeats.ROWS * ShipSeats.ACROSS, ShipSeats.TOTAL / 2,
                "four rows of four on each deck");
    }

    @Test
    void everySeatIsAtTheSameComfortableHeight() {
        assertEquals(ShipGeometry.DECK_Y - ShipGeometry.SEAT_SINK
                        + ShipGeometry.RIDER_VEHICLE_ATTACHMENT_Y,
                ShipGeometry.RIDE_HEIGHT, 1.0E-9);
        for (int i = 0; i < ShipSeats.TOTAL; i++) {
            double[] seat = ShipSeats.offset(i);
            assertEquals(ShipGeometry.RIDE_HEIGHT, seat[1], 1.0E-9);
            double feet = seat[1] - ShipGeometry.RIDER_VEHICLE_ATTACHMENT_Y;
            assertEquals(ShipGeometry.DECK_Y - ShipGeometry.SEAT_SINK, feet, 1.0E-9);
            double eye = feet + 1.62;
            assertTrue(eye > ShipGeometry.DECK_Y + 1.0,
                    "a seated eye must clear the bulwark so the sea is visible");
        }
    }

    @Test
    void theSeatRowsAreSpreadAlongTheDeck() {
        Set<Double> foreRows = new HashSet<>();
        Set<Double> aftRows = new HashSet<>();
        for (int i = 0; i < ShipSeats.TOTAL; i++) {
            double z = ShipSeats.offset(i)[2];
            if (z > 0) {
                foreRows.add(z);
            } else {
                aftRows.add(-z);
            }
        }
        assertEquals(ShipSeats.ROWS, foreRows.size(), "distinct foredeck rows");
        assertEquals(ShipSeats.ROWS, aftRows.size(), "distinct aftdeck rows");
        double spread = 0.0;
        for (double z : foreRows) {
            spread = Math.max(spread, z);
        }
        assertTrue(spread >= 38.0, "the last row should reach the stern/bow counter");
    }
}
