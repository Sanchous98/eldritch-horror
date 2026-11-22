package com.sanchous98.eldritchhorror.world.loc.city.plan;

import com.sanchous98.eldritchhorror.world.loc.StructureBuilder;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A handful of stateless helpers for authored city plans. They only compose
 * {@link StructureBuilder} writes — no prefabs, no RNG, no shared state.
 *
 * <p>They are conveniences, not requirements: a plan may always write its own blocks directly
 * through the builder.
 */
public final class CityPlanSupport {

    private CityPlanSupport() {
    }

    /** Places a standing sign (delegates to {@link StructureBuilder#sign}). */
    public static void sign(StructureBuilder b, int x, int y, int z, Direction facing, String... lines) {
        b.sign(x, y, z, facing, lines);
    }

    /**
     * Lays a {@code width}-wide street along the centre line from {@code (x0,z0)} to
     * {@code (x1,z1)}, one paving course on the natural terrain surface
     * ({@link StructureBuilder#groundY}) at each column. For a street on the levelled city
     * surface, pass the explicit {@code y} overload instead.
     */
    public static void path(StructureBuilder b, int x0, int z0, int x1, int z1, int width,
                            BlockState state) {
        street(b, x0, z0, x1, z1, width, 0, true, state);
    }

    /**
     * Lays a {@code width}-wide street along the centre line from {@code (x0,z0)} to
     * {@code (x1,z1)} at the fixed authored level {@code y}.
     */
    public static void path(StructureBuilder b, int x0, int z0, int x1, int z1, int width, int y,
                            BlockState state) {
        street(b, x0, z0, x1, z1, width, y, false, state);
    }

    /** Fills a rectangular authored plaza with one course of {@code state} at level {@code y}. */
    public static void plaza(StructureBuilder b, int x0, int z0, int x1, int z1, int y,
                             BlockState state) {
        b.fill(x0, y, z0, x1, y, z1, state);
    }

    /**
     * Shared street body: the line is the centre line, {@code width} is the full cross-street
     * width and the dominant axis decides the orientation. {@code atSurface} picks
     * {@code b.groundY} per column instead of the fixed {@code y}.
     */
    private static void street(StructureBuilder b, int x0, int z0, int x1, int z1, int width,
                               int y, boolean atSurface, BlockState state) {
        int w = Math.max(1, width);
        int lo = -(w - 1) / 2;
        int hi = w / 2;
        int ax = Math.min(x0, x1);
        int bx = Math.max(x0, x1);
        int az = Math.min(z0, z1);
        int bz = Math.max(z0, z1);
        if (bx - ax >= bz - az) {
            int cz = (z0 + z1) / 2;
            for (int x = ax; x <= bx; x++) {
                for (int z = cz + lo; z <= cz + hi; z++) {
                    b.put(x, atSurface ? b.groundY(x, z) : y, z, state);
                }
            }
        } else {
            int cx = (x0 + x1) / 2;
            for (int z = az; z <= bz; z++) {
                for (int x = cx + lo; x <= cx + hi; x++) {
                    b.put(x, atSurface ? b.groundY(x, z) : y, z, state);
                }
            }
        }
    }
}
