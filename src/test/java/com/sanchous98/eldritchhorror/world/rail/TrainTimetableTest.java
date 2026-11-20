package com.sanchous98.eldritchhorror.world.rail;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Guards the train-specific timetable layer (design/30 stage 2): the direction-to-hour mapping, the
 * real-tick voyage duration, and the consist shape defaults.
 */
class TrainTimetableTest {
    private static final int SIX = 6;
    private static final int EIGHTEEN = 18;

    @Test
    void eachDirectionUsesItsConfiguredHour() {
        assertEquals(24000L, TrainTimetable.nextDeparture(0L, 0, SIX, EIGHTEEN, 1));
        assertEquals(12000L, TrainTimetable.nextDeparture(0L, 1, SIX, EIGHTEEN, 1));
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
