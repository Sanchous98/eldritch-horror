package com.sanchous98.eldritchhorror.entity;

/**
 * The physical seat layout shared by boarding and the client car models.
 *
 * <p>A car is a real carriage: a wooden floor, an aisle down the middle and twelve sightseeing
 * chairs in two rows, each chair facing its side window. Boarding keeps its abstract capacity
 * ({@code ModConfig.TRAIN_SEATS}, clamped by {@link com.sanchous98.eldritchhorror.world.rail.TrainTimetable}),
 * but every passenger index maps to one of these twelve positions, so the number of riders can
 * never exceed the number of chairs and the client model draws a chair exactly where a rider sits.
 *
 * <p>Coordinates are in blocks in the car's local frame, measured from the entity origin (the rail
 * contact point): {@code x} is positive to the right, {@code z} is positive towards the rear. The
 * locomotives and wagons are different lengths, so they have different {@code z} rows; both share
 * the same {@link #X} and {@link #RIDE_HEIGHT}.
 */
public final class TrainSeats {
    /** Chairs per side (two rows). */
    public static final int PER_SIDE = 6;
    /** Chairs in one car. */
    public static final int TOTAL = PER_SIDE * 2;

    /**
     * Distance of a seat centre from the car centreline, in blocks. The player's box is 0.6 wide,
     * so its outer edge stays inside the wall lining (1.375 blocks) with room to spare.
     */
    public static final double X = 1.0;

    /**
     * Rider attachment height above the entity origin: the top of a bench cushion (0.5625 floor +
     * 0.5625 cushion). A riding player's eye is then 1.62 blocks higher, which lands on the middle
     * of the window band instead of on a wall.
     */
    public static final double RIDE_HEIGHT = 1.125;

    /** Wagon rows: six chairs two blocks apart, centred in the 14-block carriage. */
    private static final double[] WAGON_Z = {-5.0, -3.0, -1.0, 1.0, 3.0, 5.0};

    /**
     * Locomotive rows: the engine nose takes the front four blocks, so the six rows sit in the cab
     * from z = -3.0 to z = 5.0 at 1.6-block spacing.
     */
    private static final double[] LOCOMOTIVE_Z = {-3.0, -1.4, 0.2, 1.8, 3.4, 5.0};

    private TrainSeats() {
    }

    /** @return the six row positions of a wagon. */
    public static double[] wagonRows() {
        return WAGON_Z.clone();
    }

    /** @return the six row positions of a locomotive cab. */
    public static double[] locomotiveRows() {
        return LOCOMOTIVE_Z.clone();
    }

    /** @return the row positions used by a car. */
    public static double[] rows(boolean locomotive) {
        return locomotive ? locomotiveRows() : wagonRows();
    }

    /**
     * @param passengerIndex position in {@link net.minecraft.world.entity.Entity#getPassengers()}
     * @param locomotive     whether the car is the locomotive
     * @return {x, z} in the car's local frame, in blocks
     */
    public static double[] offset(int passengerIndex, boolean locomotive) {
        double[] rows = rows(locomotive);
        int index = Math.max(0, passengerIndex);
        int row = Math.min(index / 2, rows.length - 1);
        double x = (index & 1) == 0 ? -X : X;
        return new double[]{x, rows[row]};
    }
}
