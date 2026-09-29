package com.sanchous98.eldritchhorror.world.loc.style;

import com.sanchous98.eldritchhorror.world.loc.Palette;
import com.sanchous98.eldritchhorror.world.loc.StructureBuilder;
import net.minecraft.util.RandomSource;

/**
 * A cultural city style: the local colour that climate alone cannot express (Tokyo is Köppen
 * {@code Cfa}, the same class as New Orleans, yet the two must not look alike).
 *
 * <p>The shared {@link com.sanchous98.eldritchhorror.world.loc.city.CityLocation} owns the
 * district layout (jittered grid, alleys, decay, lights); a style supplies only the
 * <b>materials</b> and the <b>flourishes</b> that make a city read as its culture. One style =
 * one file, so many styles can be authored in parallel without touching shared code. See
 * {@code docs/STRUCTURES-CONTRACT.md} § Cultural styles.
 *
 * <p>All methods must be deterministic and use only {@link StructureBuilder} + {@link Palette}
 * blocks.
 */
public interface CityStyle {

    /** Stable id, e.g. {@code "japanese"}. Never change once shipped. */
    String id();

    /**
     * Materials for this culture, varied by the real Köppen class and coast. Culture leads;
     * climate is variation (e.g. a roof pitch or overgrowth). Implementations may delegate
     * unchanged fields to {@link Palette#fromBiome(int, boolean)}.
     */
    Palette palette(int koppenClass, boolean coastal);

    /**
     * The skyline landmark that dominates the city (cathedral / temple / mosque / pagoda / …).
     * Sited at the city centre {@code (cx,cz)} on ground {@code ground}.
     */
    void landmark(StructureBuilder b, RandomSource rng, int cx, int cz, int ground, Palette p);

    /**
     * Optional per-building flourish (signs, balconies, verandas, awnings…). Called after a
     * building shell and its roof are placed. Box is inclusive; {@code y0} is floor, {@code y1}
     * is the top of the walls (roof sits above {@code y1}).
     */
    default void flourish(StructureBuilder b, RandomSource rng, int x, int y0, int z,
                          int x1, int y1, int z1, Palette p) {
    }

    /**
     * Optional street furniture for the whole district (lanterns, torii, neon, statues…). Called
     * once, after the buildings, with the district half-extent and the centre.
     */
    default void streetProps(StructureBuilder b, RandomSource rng, int cx, int cz,
                             int district, int ground, Palette p) {
    }
}
