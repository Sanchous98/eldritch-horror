package com.sanchous98.eldritchhorror.world.loc.city;

import com.sanchous98.eldritchhorror.world.loc.StructureBuilder;

/**
 * The authored city's terrain-following surface: a flat authored heart blended into the natural
 * terrain over a graded rim, so a city never looks like a piece of land cut out with a knife.
 *
 * <p>Plans use {@link #at(int, int)} to sit a lot or a building on the same surface the shared
 * pass grades. The function is pure and chunk-independent: it reads only the global terrain
 * through {@link StructureBuilder#groundY} and the authored centre/ground.
 */
public final class CitySurface {

    private final StructureBuilder builder;
    private final int cx;
    private final int cz;
    private final int ground;
    private final int edgeRing;
    private final int flat;

    private CitySurface(StructureBuilder builder, int cx, int cz, int district, int ground,
                        int edgeRing) {
        this.builder = builder;
        this.cx = cx;
        this.cz = cz;
        this.ground = ground;
        this.edgeRing = edgeRing;
        this.flat = district - edgeRing;
    }

    /** Builds the surface for one city. {@code edgeRing} is the shared pass's graded rim width. */
    public static CitySurface of(StructureBuilder b, int cx, int cz, int district, int ground,
                                 int edgeRing) {
        return new CitySurface(b, cx, cz, district, ground, edgeRing);
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
        return (int) Math.round(this.ground * (1.0 - t) + this.builder.groundY(x, z) * t);
    }

    /** @return the graded rim width this surface was built with. */
    public int edgeRing() {
        return this.edgeRing;
    }
}
