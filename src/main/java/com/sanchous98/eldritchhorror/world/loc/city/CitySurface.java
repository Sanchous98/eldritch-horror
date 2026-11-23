package com.sanchous98.eldritchhorror.world.loc.city;

import com.sanchous98.eldritchhorror.world.EarthMap;
import com.sanchous98.eldritchhorror.world.ElevationCurve;
import com.sanchous98.eldritchhorror.world.loc.StructureBuilder;
import java.util.function.IntBinaryOperator;

/**
 * The authored city's terrain-following surface: a flat authored heart blended into the natural
 * terrain over a graded rim, so a city never looks like a piece of land cut out with a knife.
 *
 * <p>Plans use {@link #at(int, int)} to sit a lot or a building on the same surface the shared
 * pass grades. The function is pure and chunk-independent: it reads only the global terrain (a
 * {@link StructureBuilder} during worldgen, the baked Earth map in runtime passes) and the
 * authored centre/ground.
 */
public final class CitySurface {

    private final IntBinaryOperator groundY;
    private final int cx;
    private final int cz;
    private final int ground;
    private final int edgeRing;
    private final int flat;

    private CitySurface(IntBinaryOperator groundY, int cx, int cz, int district,
                        int ground, int edgeRing) {
        this.groundY = groundY;
        this.cx = cx;
        this.cz = cz;
        this.ground = ground;
        this.edgeRing = edgeRing;
        this.flat = district - edgeRing;
    }

    /** Builds the surface for one city. {@code edgeRing} is the shared pass's graded rim width. */
    public static CitySurface of(StructureBuilder b, int cx, int cz, int district, int ground,
                                 int edgeRing) {
        return new CitySurface(b::groundY, cx, cz, district, ground, edgeRing);
    }

    /**
     * A surface backed by the baked Earth map instead of a generating chunk, for runtime passes
     * that must reproduce the authored grading without a {@link StructureBuilder}
     * ({@code CityVegetation}). It mirrors {@link StructureBuilder#groundY} exactly, including the
     * overworld's vertical clamp.
     */
    public static CitySurface ofMap(EarthMap map, int cx, int cz, int district, int ground,
                                    int edgeRing) {
        // Same clamp as Builder.groundY for the overworld (minY = -64, maxY = 319): one block in
        // from ElevationCurve's own absolute floor/ceiling.
        return new CitySurface((x, z) -> Math.clamp(ElevationCurve.toY(map.elevationMetres(x, z)),
                ElevationCurve.MIN_Y + 1, ElevationCurve.MAX_Y - 2),
                cx, cz, district, ground, edgeRing);
    }

    /** @return the graded surface Y at {@code (x,z)}, matching the shared pass exactly. */
    public int at(int x, int z) {
        int dx = x - this.cx;
        int dz = z - this.cz;
        int d2 = dx * dx + dz * dz;
        if (d2 <= this.flat * this.flat) {
            return this.ground;
        }
        double t = Math.clamp((Math.sqrt(d2) - this.flat) / this.edgeRing, 0.0, 1.0);
        return (int) Math.round(this.ground * (1.0 - t) + this.groundY.applyAsInt(x, z) * t);
    }

    /**
     * @return the natural (ungraded) terrain Y at {@code (x,z)}. On a cut slope the biome's trees
     *         stood at this level, so a cleanup sweep must scan up to it + {@code CLEAR_ABOVE}
     *         even though the planned surface is lower.
     */
    public int naturalAt(int x, int z) {
        return this.groundY.applyAsInt(x, z);
    }

    /** @return the graded rim width this surface was built with. */
    public int edgeRing() {
        return this.edgeRing;
    }
}
