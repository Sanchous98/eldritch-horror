package com.sanchous98.eldritchhorror.entity;

/**
 * The liner's passenger seats: sixteen on the open foredeck, sixteen on the open aft deck, four
 * across on each of four rows per side. Seats are entity-local {@code {x, attachmentY, z}} with
 * {@code +Z} toward the bow; the rider's feet land 0.3 into the planking.
 */
public final class ShipSeats {

    /** All seats on the vessel. */
    public static final int TOTAL = ShipGeometry.SEATS;
    /** Rows on each of the two open decks. */
    public static final int ROWS = 4;
    /** Seats across the beam in one row. */
    public static final int ACROSS = 4;

    /** Starboard to port positions across one row. */
    private static final double[] X = {6.2, 2.2, -2.2, -6.2};
    /** Forward distance of each row from amidships. */
    private static final double[] ROW_Z = {25.0, 30.0, 35.0, 40.0};

    private ShipSeats() {
    }

    /**
     * @param index passenger index in the vehicle's passenger list, 0-based
     * @return {local x, attachment y, local z}
     */
    public static double[] offset(int index) {
        int seat = Math.floorMod(index, TOTAL);
        boolean aft = seat >= TOTAL / 2;
        int local = aft ? seat - TOTAL / 2 : seat;
        int row = local / ACROSS;
        int column = local % ACROSS;
        double z = ROW_Z[row] * (aft ? -1.0 : 1.0);
        return new double[]{X[column], ShipGeometry.RIDE_HEIGHT, z};
    }

    /** @return whether the seat is on the aft deck (handy for command descriptions). */
    public static boolean onAftDeck(int index) {
        return Math.floorMod(index, TOTAL) >= TOTAL / 2;
    }
}
