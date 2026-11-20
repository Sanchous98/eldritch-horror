package com.sanchous98.eldritchhorror.world.rail;

import com.sanchous98.eldritchhorror.world.transport.TransportMath;

/**
 * The train-specific part of the shared timetable stack ({@code design/30-transport.md}).
 *
 * <p>Departures use the same in-game clock and boarding arithmetic as the ships
 * ({@link TransportMath}); this class only maps a direction to its configured departure hour and
 * fixes the consist shape (2–4 wagons, 1–4 seats per car, fixed coupling spacing).
 */
public final class TrainTimetable {
    /** Smallest consist the design allows. */
    public static final int MIN_WAGONS = 2;
    /** Largest consist the design allows. */
    public static final int MAX_WAGONS = 4;
    /** Seats in one car (the locomotive and each wagon). */
    public static final int MAX_SEATS = 4;
    /** Distance between car centres, in blocks. */
    public static final double SPACING = 4.5;

    private TrainTimetable() {
    }

    /**
     * @param now        current overworld clock tick
     * @param direction  0 = A to B, 1 = B to A
     * @param hourA      departure hour for direction 0
     * @param hourB      departure hour for direction 1
     * @param periodDays days between service days
     * @return the next absolute departure tick for that direction
     */
    public static long nextDeparture(long now, int direction, int hourA, int hourB,
                                     int periodDays) {
        int hour = direction == 0 ? hourA : hourB;
        return TransportMath.nextDeparture(now, hour, hour, periodDays);
    }

    /**
     * @param distanceBlocks line length in blocks
     * @param speed          configured speed in blocks per tick
     * @return the real-tick voyage duration
     */
    public static int durationTicks(double distanceBlocks, double speed) {
        return TransportMath.durationTicks(distanceBlocks, speed);
    }

    /** @return the configured wagon count clamped to the design's 2–4. */
    public static int wagons(int configured) {
        return Math.clamp(configured, MIN_WAGONS, MAX_WAGONS);
    }

    /** @return the configured seats per car clamped to the design's 1–4. */
    public static int seats(int configured) {
        return Math.clamp(configured, 1, MAX_SEATS);
    }

    /** @return total passenger seats in a consist of {@code wagons} wagons and {@code seats} seats per car. */
    public static int totalSeats(int wagons, int seats) {
        return (wagons(wagons) + 1) * seats(seats);
    }

    /** @return the length of the whole consist (locomotive + wagons) in blocks. */
    public static double consistLength(int wagons) {
        return wagons(wagons) * SPACING;
    }
}
