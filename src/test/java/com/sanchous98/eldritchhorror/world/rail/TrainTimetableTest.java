package com.sanchous98.eldritchhorror.world.rail;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

/**
 * Guards the train-specific timetable layer (design/30 stage 2): the per-line direction stagger, the
 * real-tick voyage duration, and the consist shape defaults.
 */
class TrainTimetableTest {
    private static final int[] HOURS = {4, 8, 12, 16, 20, 0};

    @Test
    void eachDirectionGetsAStableStagger() {
        int[] forward = TrainTimetable.staggeredHours("london-paris", 0, HOURS);
        int[] reverse = TrainTimetable.staggeredHours("london-paris", 1, HOURS);
        assertEquals(HOURS.length, forward.length);
        assertArrayEquals(forward, TrainTimetable.staggeredHours("london-paris", 0, HOURS));
        assertNotEquals(forward[0], reverse[0], "opposite directions must not share a phase");
    }

    @Test
    void theHourListDrivesTheNextDeparture() {
        // Clock 0 is 06:00; the first departure of the day is 08:00 (raw 2000).
        assertEquals(2000L,
                TrainTimetable.nextDeparture(0L, "london-paris", 0, HOURS, 1));
        assertEquals(2000L,
                TrainTimetable.nextDeparture(0L, "london-paris", 0, HOURS, 2));
    }

    @Test
    void voyageDurationIsProportionalToLength() {
        assertEquals(84, TrainTimetable.durationTicks(100.0, 1.2));
        assertEquals(1000, TrainTimetable.durationTicks(1000.0, 1.0));
    }

    @Test
    void consistShapeIsClampedToTheDesign() {
        assertEquals(2, TrainTimetable.wagons(0));
        assertEquals(3, TrainTimetable.wagons(3));
        assertEquals(4, TrainTimetable.wagons(9));
        assertEquals(1, TrainTimetable.seats(0));
        assertEquals(4, TrainTimetable.seats(9));
        assertEquals(16, TrainTimetable.totalSeats(3, 4));
        assertEquals(13.5, TrainTimetable.consistLength(3), 1.0E-9);
    }
}
