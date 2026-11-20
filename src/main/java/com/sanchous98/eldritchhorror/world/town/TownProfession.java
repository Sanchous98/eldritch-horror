package com.sanchous98.eldritchhorror.world.town;

/**
 * The small, town-appropriate trade pool for generic-town villagers. Kept free of Minecraft
 * types so the choice is unit-testable in a bare JVM; {@link TownPopulation} maps a constant to
 * the vanilla {@code VillagerProfession} holder. Trades are the vanilla ones — no custom lists.
 */
public enum TownProfession {

    /** Grows the town's farm plot. */
    FARMER,
    /** Keeps the books; the town's one scholarly trade. */
    LIBRARIAN,
    /** Tends the well and the marker; the gothic note. */
    CLERIC,
    /** Works the coast or the river, when the town has one. */
    FISHERMAN,
    /** Tends the pens on the town's edge. */
    SHEPHERD;

    /** The pool in declaration order, for deterministic indexing. */
    public static final TownProfession[] POOL = values();
}
