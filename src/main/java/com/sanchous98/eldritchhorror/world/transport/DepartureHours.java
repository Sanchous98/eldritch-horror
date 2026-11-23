package com.sanchous98.eldritchhorror.world.transport;

/**
 * Game-free parsing and staggering for the comma-separated departure-hour lists
 * ({@code design/30-transport.md}).
 *
 * <p>A schedule is a set of 24-hour clock hours, for example {@code "04,08,12,16,20,00"}. The
 * list is parsed once per service tick; malformed entries and hours outside {@code 0..23} are
 * ignored rather than failing the whole timetable. Every line and direction then shifts the list
 * by a stable phase derived from its id, so two stations sharing an hour list do not open their
 * boarding windows at the same moment. The phase is bounded by one service interval, so the
 * configured hours still describe the day (a six-a-day line stays six-a-day).
 *
 * <p>Pure: no Minecraft type appears here.
 */
public final class DepartureHours {
    /** Hours in a day on the 24-hour clock. */
    public static final int HOURS_PER_DAY = 24;
    /** Default train schedule: six departures a day, roughly every four hours. */
    public static final int[] TRAIN_DEFAULT = {4, 8, 12, 16, 20, 0};
    /** Default ship schedule: four departures a day, roughly every six hours. */
    public static final int[] SHIP_DEFAULT = {0, 6, 12, 18};

    private DepartureHours() {
    }

    /**
     * Parses a comma-separated hour list.
     *
     * @param spec         configured list, for example {@code "04,08,12,16,20,00"}
     * @param defaultHours hours used when no usable entry remains
     * @return sorted, distinct clock hours, or a copy of {@code defaultHours}
     */
    public static int[] parse(String spec, int[] defaultHours) {
        boolean[] seen = new boolean[HOURS_PER_DAY];
        int count = 0;
        for (String raw : spec.split(",")) {
            String part = raw.trim();
            if (part.isEmpty()) {
                continue;
            }
            int hour;
            try {
                hour = Integer.parseInt(part);
            } catch (NumberFormatException malformed) {
                continue; // one bad entry must not discard the rest of the schedule
            }
            if (hour < 0 || hour >= HOURS_PER_DAY || seen[hour]) {
                continue;
            }
            seen[hour] = true;
            count++;
        }
        if (count == 0) {
            return defaultHours.clone();
        }
        int[] out = new int[count];
        int index = 0;
        for (int hour = 0; hour < HOURS_PER_DAY; hour++) {
            if (seen[hour]) {
                out[index++] = hour;
            }
        }
        return out;
    }

    /**
     * @param baseHours   parsed departure hours
     * @param offsetHours whole-hour shift, wrapped to a day
     * @return the shifted, sorted, distinct hour list
     */
    public static int[] shift(int[] baseHours, int offsetHours) {
        boolean[] seen = new boolean[HOURS_PER_DAY];
        int count = 0;
        for (int hour : baseHours) {
            int shifted = Math.floorMod(hour + offsetHours, HOURS_PER_DAY);
            if (!seen[shifted]) {
                seen[shifted] = true;
                count++;
            }
        }
        int[] out = new int[count];
        int index = 0;
        for (int hour = 0; hour < HOURS_PER_DAY; hour++) {
            if (seen[hour]) {
                out[index++] = hour;
            }
        }
        return out;
    }

    /**
     * Staggers a schedule for one line and direction.
     *
     * <p>The phase is at most one service interval, so the configured frequency is preserved.
     * {@link String#hashCode()} is specified by the Java language, so the same line always sees
     * the same phase after a restart.
     *
     * @param seed      stable line id plus direction, for example {@code london-new_york:0}
     * @param baseHours parsed departure hours
     * @return the line's staggered departure hours
     */
    public static int[] forLine(String seed, int[] baseHours) {
        int interval = Math.max(1, HOURS_PER_DAY / Math.max(1, baseHours.length));
        int phase = Math.floorMod(seed.hashCode(), interval);
        return shift(baseHours, phase);
    }
}
