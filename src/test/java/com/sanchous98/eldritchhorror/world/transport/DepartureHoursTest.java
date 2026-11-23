package com.sanchous98.eldritchhorror.world.transport;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Guards the configured departure-hour list (design/30): parsing, validation, the per-line stagger
 * and its stability across calls.
 */
class DepartureHoursTest {

    @Test
    void parsesAndSortsTheConfiguredList() {
        assertArrayEquals(new int[]{0, 4, 8, 12, 16, 20},
                DepartureHours.parse("04,08,12,16,20,00", DepartureHours.TRAIN_DEFAULT));
    }

    @Test
    void skipsMalformedAndOutOfRangeEntries() {
        assertArrayEquals(new int[]{4, 8},
                DepartureHours.parse("4, 8, nope, 99, -3, 8", DepartureHours.SHIP_DEFAULT));
    }

    @Test
    void fallsBackWhenNothingUsableRemains() {
        assertArrayEquals(DepartureHours.TRAIN_DEFAULT,
                DepartureHours.parse("", DepartureHours.TRAIN_DEFAULT));
        assertArrayEquals(DepartureHours.SHIP_DEFAULT,
                DepartureHours.parse("nope, 25", DepartureHours.SHIP_DEFAULT));
    }

    @Test
    void shiftingWrapsAroundMidnight() {
        assertArrayEquals(new int[]{1, 7, 13, 19},
                DepartureHours.shift(new int[]{0, 6, 12, 18}, 1));
        assertArrayEquals(new int[]{5, 11, 17, 23},
                DepartureHours.shift(new int[]{0, 6, 12, 18}, 23));
    }

    @Test
    void eachLineKeepsItsOwnStablePhase() {
        int[] forward = DepartureHours.forLine("london-paris:0", DepartureHours.TRAIN_DEFAULT);
        int[] reverse = DepartureHours.forLine("london-paris:1", DepartureHours.TRAIN_DEFAULT);
        assertArrayEquals(forward, DepartureHours.forLine("london-paris:0",
                DepartureHours.TRAIN_DEFAULT));
        assertEquals(DepartureHours.TRAIN_DEFAULT.length, forward.length);
        assertEquals(DepartureHours.TRAIN_DEFAULT.length, reverse.length);
        assertEquals(0, forward[0]);
        assertEquals(1, reverse[0]);
    }
}
