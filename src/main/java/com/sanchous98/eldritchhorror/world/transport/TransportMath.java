package com.sanchous98.eldritchhorror.world.transport;

/**
 * Game-free timetable arithmetic for scheduled transport ({@code design/30-transport.md}).
 *
 * <p>A Minecraft day is {@value #DAY_TICKS} ticks; the in-game clock counts {@value #HOUR_TICKS}
 * ticks per hour. Schedules are expressed in that clock, so {@code /time set} moves the timetable
 * with the sun. Voyages, once started, run on real ticks (distance / speed), so freezing time cannot
 * strand a ship at sea.
 *
 * <p>Pure: no Minecraft type appears here.
 */
public final class TransportMath {
    /** Ticks in one in-game day. */
    public static final int DAY_TICKS = 24000;
    /** Ticks in one in-game hour (the clock, not the real-time daylight curve). */
    public static final int HOUR_TICKS = 1000;
    /** In-game minutes in one in-game day. */
    public static final int MINUTES_PER_DAY = 1440;
    /**
     * Minecraft's day clock: raw day time 0 is sunrise (06:00), 6000 is noon, 12000 is sunset
     * (18:00), 18000 is midnight. The timetable is expressed in that clock, so
     * {@code /time set 0} is 06:00, exactly as the vanilla clock reads.
     */
    public static final int SUNRISE_TICKS = 6000;

    private TransportMath() {
    }

    /** @return the in-game day number of a clock tick (0 is the first day). */
    public static long dayOf(long clock) {
        return Math.floorDiv(clock, DAY_TICKS);
    }

    /** @return the raw time of day in clock ticks, {@code 0..23999} (0 = 06:00). */
    public static long timeOfDay(long clock) {
        return Math.floorMod(clock, DAY_TICKS);
    }

    /**
     * Converts a 24-hour clock hour (as the design writes "06:00") to raw Minecraft day time.
     *
     * @param hour clock hour, {@code 0..23}
     * @return raw day time, {@code 0..23999}
     */
    public static int departureTicks(int hour) {
        return Math.floorMod(Math.clamp(hour, 0, 23) * HOUR_TICKS - SUNRISE_TICKS, DAY_TICKS);
    }

    /**
     * Finds the next departure strictly after {@code now} from a daily hour list.
     *
     * <p>Departures happen on days whose index is a multiple of {@code periodDays}, at any hour in
     * {@code hours} on the 24-hour clock. Duplicate hours are harmless; an empty list has no
     * service, so the fallback is a day later rather than an exception.
     *
     * @param now        current overworld clock tick
     * @param hours      departure hours, each {@code 0..23} (order is irrelevant)
     * @param periodDays days between service days (1 = daily)
     * @return the absolute clock tick of the next departure
     */
    public static long nextDeparture(long now, int[] hours, int periodDays) {
        if (hours.length == 0) {
            return now + DAY_TICKS;
        }
        int period = Math.max(1, periodDays);
        long day = dayOf(now);
        // At most period + 1 days ahead is enough to pass a service day, even at midnight.
        for (long candidateDay = day; candidateDay <= day + period + 1; candidateDay++) {
            if (Math.floorMod(candidateDay, period) != 0) {
                continue;
            }
            long best = Long.MAX_VALUE;
            for (int hour : hours) {
                long candidate = candidateDay * DAY_TICKS + departureTicks(hour);
                if (candidate > now && candidate < best) {
                    best = candidate;
                }
            }
            if (best != Long.MAX_VALUE) {
                return best;
            }
        }
        // Unreachable for a sane period, but a deterministic value beats an exception here.
        return now + DAY_TICKS;
    }

    /**
     * Finds the next departure strictly after {@code now} from the legacy two-hour schedule.
     *
     * @param now        current overworld clock tick
     * @param hourA      first departure hour, {@code 0..23}
     * @param hourB      second departure hour, {@code 0..23}
     * @param periodDays days between service days (1 = daily)
     * @return the absolute clock tick of the next departure
     */
    public static long nextDeparture(long now, int hourA, int hourB, int periodDays) {
        return nextDeparture(now, new int[]{hourA, hourB}, periodDays);
    }

    /**
     * @param departure        absolute departure tick
     * @param boardingMinutes  in-game minutes the gangway is open before departure
     * @return the absolute tick at which boarding opens
     */
    public static long boardingOpens(long departure, int boardingMinutes) {
        return departure - boardingTicks(boardingMinutes);
    }

    /** @return the boarding window in ticks for a number of in-game minutes. */
    public static long boardingTicks(int boardingMinutes) {
        return Math.max(0, boardingMinutes) * HOUR_TICKS / 60L;
    }

    /**
     * @return whether {@code now} is inside the boarding window of {@code departure}
     *         (inclusive open, exclusive departure)
     */
    public static boolean isBoarding(long now, long departure, int boardingMinutes) {
        return now >= boardingOpens(departure, boardingMinutes) && now < departure;
    }

    /**
     * @param distanceBlocks lane length in blocks
     * @param speedBlocksTick configured speed in blocks per tick
     * @return voyage duration in ticks (at least one tick for a non-zero leg)
     */
    public static int durationTicks(double distanceBlocks, double speedBlocksTick) {
        if (distanceBlocks <= 0.0) {
            return 0;
        }
        if (speedBlocksTick <= 0.0) {
            throw new IllegalArgumentException("ship speed must be positive");
        }
        return Math.max(1, (int) Math.ceil(distanceBlocks / speedBlocksTick));
    }

    /** @return the absolute arrival tick for a departure and a lane. */
    public static long arrival(long departure, double distanceBlocks, double speedBlocksTick) {
        return departure + durationTicks(distanceBlocks, speedBlocksTick);
    }

    /** @return whether the player can pay the fare. */
    public static boolean canAfford(int carried, int fare) {
        return carried >= Math.max(0, fare);
    }

    /** @return what is left after paying, or {@code -1} when the fare cannot be paid. */
    public static int remainingAfterFare(int carried, int fare) {
        return canAfford(carried, fare) ? carried - Math.max(0, fare) : -1;
    }

    /** @return the clock as {@code HH:MM} on the vanilla day clock (raw 0 = 06:00). */
    public static String formatTime(long clock) {
        long minutes = Math.floorMod(timeOfDay(clock) + SUNRISE_TICKS, DAY_TICKS)
                * MINUTES_PER_DAY / DAY_TICKS;
        return String.format(java.util.Locale.ROOT, "%02d:%02d", minutes / 60L, minutes % 60L);
    }
}
