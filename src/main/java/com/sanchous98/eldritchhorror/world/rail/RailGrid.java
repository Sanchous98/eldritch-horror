package com.sanchous98.eldritchhorror.world.rail;

/**
 * A coarse map of the land at {@link #CELL_BLOCKS}-block resolution, used once per candidate line.
 *
 * <p>The grid is sampled from the baked landmask and elevation layer (or a test sampler) and
 * answers, in constant time: is a cell land, and what is its surface Y. Rail lines may cross short
 * stretches of water (bridges) and climb gentle slopes; the finder charges both explicitly.
 *
 * <p>Longitude wraps (the world is a cylinder, {@code design/23-boundary-and-travel.md}), so cell
 * column {@code x} is taken modulo {@link #width()}. Latitude does not wrap.
 *
 * <p>Pure: the sampler is a tiny functional interface, so tests build a terrain by hand and the real
 * grid is built from {@link com.sanchous98.eldritchhorror.world.EarthMap}.
 */
public final class RailGrid {
    /** Size of one grid cell in blocks. Keeps A* on a ~2k x 1k board instead of 131k x 65k. */
    public static final int CELL_BLOCKS = 64;

    /** Supplies the terrain facts for one cell (cell coordinates, not blocks). */
    public interface Terrain {
        /** @return true when the cell is land. */
        boolean isLand(int cellX, int cellZ);

        /**
         * @return the worldgen surface block Y of the cell (a value below sea level implies water)
         */
        int surfaceY(int cellX, int cellZ);
    }

    private final int width;
    private final int height;
    private final boolean[] land;
    private final int[] surface;

    private RailGrid(int width, int height, boolean[] land, int[] surface) {
        this.width = width;
        this.height = height;
        this.land = land;
        this.surface = surface;
    }

    /**
     * Samples a fresh grid.
     *
     * @param width  cells east to west (the wrap length)
     * @param height cells north to south
     * @param terrain terrain source, called once per cell
     * @return the sampled grid
     */
    public static RailGrid of(int width, int height, Terrain terrain) {
        if (width < 1 || height < 1) {
            throw new IllegalArgumentException("grid must be at least one cell");
        }
        int size = width * height;
        boolean[] land = new boolean[size];
        int[] surface = new int[size];
        for (int z = 0; z < height; z++) {
            for (int x = 0; x < width; x++) {
                int index = z * width + x;
                land[index] = terrain.isLand(x, z);
                surface[index] = terrain.surfaceY(x, z);
            }
        }
        return new RailGrid(width, height, land, surface);
    }

    /** @return the grid width in cells (the wrapped axis). */
    public int width() {
        return this.width;
    }

    /** @return the grid height in cells (the clamped axis). */
    public int height() {
        return this.height;
    }

    /** @return true when the cell is land (outside the latitude range is water). */
    public boolean land(int cellX, int cellZ) {
        if (cellZ < 0 || cellZ >= this.height) {
            return false;
        }
        return this.land[cellZ * this.width + wrapX(cellX)];
    }

    /** @return the worldgen surface Y of the cell (water cells may be below sea level). */
    public int surfaceY(int cellX, int cellZ) {
        if (cellZ < 0 || cellZ >= this.height) {
            return 0;
        }
        return this.surface[cellZ * this.width + wrapX(cellX)];
    }

    /** Wraps a cell column onto the cylinder. */
    public int wrapX(int cellX) {
        return Math.floorMod(cellX, this.width);
    }

    /** @return the shortest signed horizontal distance from {@code fromX} to {@code toX}, in cells. */
    public int wrapDelta(int fromX, int toX) {
        int delta = Math.floorMod(toX - fromX, this.width);
        return delta > this.width / 2 ? delta - this.width : delta;
    }
}
