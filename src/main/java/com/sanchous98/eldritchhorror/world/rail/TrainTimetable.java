package com.sanchous98.eldritchhorror.world.rail;

import com.sanchous98.eldritchhorror.world.transport.DepartureHours;
import com.sanchous98.eldritchhorror.world.transport.TransportMath;

/**
 * The train-specific part of the shared timetable stack ({@code design/30-transport.md}).
 *
 * <p>Departures use the same in-game clock and boarding arithmetic as the ships
 * ({@link TransportMath}); this class only staggers the configured hour list per line and direction
 * and fixes the consist shape (2–4 wagons, 1–12 seats per car, fixed coupling spacing).
 */
public final class TrainTimetable {
    /** Smallest consist the design allows. */
    public static final int MIN_WAGONS = 2;
    /** Largest consist the design allows. */
    public static final int MAX_WAGONS = 4;
    /** Seats in one car (the locomotive and each wagon); matches {@code TrainSeats.TOTAL}. */
    public static final int MAX_SEATS = 12;
    /**
     * Distance between car centres, in blocks. A wagon is 14 blocks long and the locomotive 16, so
     * this leaves a 2-block coupling gap between wagons and 1 block behind the locomotive.
     */
    public static final double SPACING = 16.0;

    private TrainTimetable() {
    }

    /**
     * @param lineId     stable line id (used for the stagger phase)
     * @param direction  0 = A to B, 1 = B to A
     * @param baseHours  configured departure hours, before staggering
     * @return the line's staggered departure hours, stable across restarts
     */
    public static int[] staggeredHours(String lineId, int direction, int[] baseHours) {
        return DepartureHours.forLine(lineId + ":" + direction, baseHours);
    }

    /**
     * @param now        current overworld clock tick
     * @param lineId     stable line id
     * @param direction  0 = A to B, 1 = B to A
     * @param baseHours  configured departure hours, before staggering
     * @param periodDays days between service days
     * @return the next absolute departure tick for that line and direction
     */
    public static long nextDeparture(long now, String lineId, int direction, int[] baseHours,
                                     int periodDays) {
        return TransportMath.nextDeparture(now, staggeredHours(lineId, direction, baseHours),
                periodDays);
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

    /** @return the configured seats per car clamped to the design's 1–12. */
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
