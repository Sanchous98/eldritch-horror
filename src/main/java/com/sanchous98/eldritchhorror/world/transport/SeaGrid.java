package com.sanchous98.eldritchhorror.world.transport;

import com.sanchous98.eldritchhorror.world.EarthMap;
import java.util.Arrays;

/**
 * A coarse map of the ocean at {@link #CELL_BLOCKS}-block resolution, with a land safety margin.
 *
 * <p>The grid is sampled once from the baked landmask (or a test mask) and answers two questions in
 * constant time: is a cell water, and is it <em>safe</em> water — water with every cell within
 * {@code margin} Chebyshev cells also water. A lane may leave the safe band only within a short
 * escape radius of a port, because a harbour by definition touches the coast.
 *
 * <p>Longitude wraps (the world is a cylinder, {@code design/23-boundary-and-travel.md}), so cell
 * column {@code x} is taken modulo {@link #width()}. Latitude does not wrap.
 *
 * <p>Pure: the sampler is a tiny functional interface, so tests build a mask by hand and the real
 * grid is built from {@link com.sanchous98.eldritchhorror.world.EarthMap}.
 */
public final class SeaGrid {
    /** Size of one grid cell in blocks. Keeps A* on a ~2k x 1k board instead of 131k x 65k. */
    public static final int CELL_BLOCKS = 64;

    /** Supplies the land fact for one cell (cell coordinates, not blocks). */
    @FunctionalInterface
    public interface LandSampler {
        /** @return true when the cell is land. */
        boolean isLand(int cellX, int cellZ);
    }

    private final int width;
    private final int height;
    private final int margin;
    private final boolean[] water;
    private final boolean[] safe;

    private SeaGrid(int width, int height, int margin, boolean[] water, boolean[] safe) {
        this.width = width;
        this.height = height;
        this.margin = margin;
        this.water = water;
        this.safe = safe;
    }

    /**
     * Samples a fresh grid.
     *
     * @param width  cells east to west (the wrap length)
     * @param height cells north to south
     * @param margin land safety margin in cells (0 disables erosion; production uses 1)
     * @param sampler land source, called once per cell
     * @return the sampled grid
     */
    public static SeaGrid of(int width, int height, int margin, LandSampler sampler) {
        if (width < 1 || height < 1) {
            throw new IllegalArgumentException("grid must be at least one cell");
        }
        if (margin < 0) {
            throw new IllegalArgumentException("margin must not be negative");
        }
        int size = width * height;
        boolean[] water = new boolean[size];
        for (int z = 0; z < height; z++) {
            for (int x = 0; x < width; x++) {
                water[z * width + x] = !sampler.isLand(x, z);
            }
        }
        boolean[] safe = erode(water, width, height, margin);
        return new SeaGrid(width, height, margin, water, safe);
    }

    private static boolean[] erode(boolean[] water, int width, int height, int margin) {
        if (margin == 0) {
            return Arrays.copyOf(water, water.length);
        }
        boolean[] safe = new boolean[water.length];
        for (int z = margin; z < height - margin; z++) {
            for (int x = 0; x < width; x++) {
                if (!water[z * width + x]) {
                    continue;
                }
                boolean clear = true;
                for (int dz = -margin; dz <= margin && clear; dz++) {
                    for (int dx = -margin; dx <= margin; dx++) {
                        int nx = Math.floorMod(x + dx, width);
                        if (!water[(z + dz) * width + nx]) {
                            clear = false;
                            break;
                        }
                    }
                }
                safe[z * width + x] = clear;
            }
        }
        return safe;
    }

    /** @return the grid width in cells (the wrapped axis). */
    public int width() {
        return this.width;
    }

    /** @return the grid height in cells (the clamped axis). */
    public int height() {
        return this.height;
    }

    /** @return the land safety margin in cells. */
    public int margin() {
        return this.margin;
    }

    /** @return true when the cell is water (or outside the latitude range). */
    public boolean water(int cellX, int cellZ) {
        if (cellZ < 0 || cellZ >= this.height) {
            return false;
        }
        return this.water[cellZ * this.width + wrapX(cellX)];
    }

    /** @return true when the cell is water and every cell within {@link #margin} is water too. */
    public boolean safe(int cellX, int cellZ) {
        if (cellZ < 0 || cellZ >= this.height) {
            return false;
        }
        return this.safe[cellZ * this.width + wrapX(cellX)];
    }

    /** Wraps a cell column onto the cylinder. */
    public int wrapX(int cellX) {
        return Math.floorMod(cellX, this.width);
    }

    /** @return the cell column holding a world X (wrapped onto the cylinder). */
    public static int cellX(int blockX) {
        return Math.floorDiv(blockX + EarthMap.HALF_WIDTH, CELL_BLOCKS);
    }

    /** @return the cell row holding a world Z (clamped, latitude does not wrap). */
    public static int cellZ(int blockZ) {
        return Math.floorDiv(blockZ + EarthMap.HALF_HEIGHT, CELL_BLOCKS);
    }

    /** @return the world X of a cell column's centre. */
    public static int centreX(int cellX) {
        return -EarthMap.HALF_WIDTH + cellX * CELL_BLOCKS + CELL_BLOCKS / 2;
    }

    /** @return the world Z of a cell row's centre. */
    public static int centreZ(int cellZ) {
        return -EarthMap.HALF_HEIGHT + cellZ * CELL_BLOCKS + CELL_BLOCKS / 2;
    }

    /** @return the shortest signed horizontal distance from {@code fromX} to {@code toX}, in cells. */
    public int wrapDelta(int fromX, int toX) {
        int delta = Math.floorMod(toX - fromX, this.width);
        return delta > this.width / 2 ? delta - this.width : delta;
    }
}
