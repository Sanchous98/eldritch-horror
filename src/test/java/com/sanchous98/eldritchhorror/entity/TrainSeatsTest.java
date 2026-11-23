package com.sanchous98.eldritchhorror.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * Guards the physical carriage layout: twelve distinct chairs, two per row, all inside the body
 * and clear of the walls, with the rider attachment on the bench top.
 */
class TrainSeatsTest {

    @Test
    void bothCarsOfferTwelveDistinctChairs() {
        assertEquals(12, TrainSeats.TOTAL);
        assertEquals(6, TrainSeats.PER_SIDE);
        assertTwelveDistinct(false);
        assertTwelveDistinct(true);
    }

    @Test
    void chairsAlternateSidesAndStayInsideTheCar() {
        for (boolean locomotive : new boolean[]{false, true}) {
            double[] rows = TrainSeats.rows(locomotive);
            for (int i = 0; i < TrainSeats.TOTAL; i++) {
                double[] seat = TrainSeats.offset(i, locomotive);
                assertEquals(i % 2 == 0 ? -TrainSeats.X : TrainSeats.X, seat[0], 0.0);
                assertEquals(rows[i / 2], seat[1], 0.0);
                assertTrue(Math.abs(seat[0]) < 1.375, "seat must stay inside the wall lining");
                assertTrue(seat[1] > -6.0 && seat[1] < 6.0, "seat must be inside the body");
            }
        }
    }

    @Test
    void theRiderSitsOnTheBenchWithEyesInTheWindowBand() {
        assertEquals(1.125, TrainSeats.RIDE_HEIGHT, 1.0E-9);
        // Floor top 0.5625, bench top 1.125; the eye is 1.62 higher and the window band is
        // 1.4375..3.0 blocks above the entity origin.
        double eye = TrainSeats.RIDE_HEIGHT + 1.62;
        assertTrue(eye > 1.4375 && eye < 3.0, "eye line must fall inside the window band");
    }

    private static void assertTwelveDistinct(boolean locomotive) {
        Set<String> seen = new HashSet<>();
        for (int i = 0; i < TrainSeats.TOTAL; i++) {
            double[] seat = TrainSeats.offset(i, locomotive);
            assertTrue(seen.add(seat[0] + ":" + seat[1]), "duplicate seat " + i);
        }
        assertEquals(TrainSeats.TOTAL, seen.size());
    }
}
