package com.sanchous98.eldritchhorror.world.rail;

import com.sanchous98.eldritchhorror.world.EarthMap;

/**
 * A sampled map of the terrain used by the rail route finder, at a configurable cell size.
 *
 * <p>Two resolutions are used: a coarse intercity board (512-block cells) for the long middle of a
 * route and a fine local board (64-block cells) for the last few kilometres around a station. The
 * grid answers, in constant time: is a cell land, and what is its surface Y. Rail lines may cross
 * short stretches of water (bridges) and climb gentle slopes; the finder charges both explicitly.
 *
 * <p>A grid is either a <b>cylinder</b> (the legacy Earth map: longitude wraps, latitude does not)
 * or a <b>plane</b> (a 1:1 map package whose cities span more than the baked world). A plane indexes
 * cells from {@link #originX()}/{@link #originZ()} without wrapping.
 *
 * <p>Pure: the sampler is a tiny functional interface, so tests build a terrain by hand and the real
 * grids are built from {@link com.sanchous98.eldritchhorror.world.EarthMap}.
 */
public final class RailGrid {
    /** Size of one fine grid cell in blocks. Keeps the station approach at neighbourhood scale. */
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
    private final int cellBlocks;
    private final int originX;
    private final int originZ;
    private final boolean wrapped;
    private final boolean[] land;
    private final int[] surface;

    private RailGrid(int width, int height, int cellBlocks, int originX, int originZ,
                     boolean wrapped, boolean[] land, int[] surface) {
        this.width = width;
        this.height = height;
        this.cellBlocks = cellBlocks;
        this.originX = originX;
        this.originZ = originZ;
        this.wrapped = wrapped;
        this.land = land;
        this.surface = surface;
    }

    /**
     * Samples a fresh fine grid over the legacy cylindrical Earth map, for tests and probes.
     *
     * @param width  cells east to west (the wrap length)
     * @param height cells north to south
     * @param terrain terrain source, called once per cell
     * @return the sampled grid
     */
    public static RailGrid of(int width, int height, Terrain terrain) {
        return of(width, height, CELL_BLOCKS, -EarthMap.HALF_WIDTH, -EarthMap.HALF_HEIGHT, true,
                terrain);
    }

    /**
     * Samples a fresh grid.
     *
     * @param width      cells east to west (the wrap length when {@code wrapped})
     * @param height     cells north to south
     * @param cellBlocks size of one cell in blocks
     * @param originX    world X of cell column 0's west edge
     * @param originZ    world Z of cell row 0's north edge
     * @param wrapped    true for the cylinder, false for a plane
     * @param terrain    terrain source, called once per cell
     * @return the sampled grid
     */
    public static RailGrid of(int width, int height, int cellBlocks, int originX, int originZ,
                              boolean wrapped, Terrain terrain) {
        if (width < 1 || height < 1) {
            throw new IllegalArgumentException("grid must be at least one cell");
        }
        if (cellBlocks < 1) {
            throw new IllegalArgumentException("cell size must be positive");
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
        return new RailGrid(width, height, cellBlocks, originX, originZ, wrapped, land, surface);
    }

    /** @return the grid width in cells (the wrapped axis on a cylinder). */
    public int width() {
        return this.width;
    }

    /** @return the grid height in cells (the clamped axis). */
    public int height() {
        return this.height;
    }

    /** @return the size of one cell in blocks. */
    public int cellBlocks() {
        return this.cellBlocks;
    }

    /** @return true when the X axis wraps (the legacy Earth cylinder). */
    public boolean wrapped() {
        return this.wrapped;
    }

    /** @return the world X of cell column 0's west edge. */
    public int originX() {
        return this.originX;
    }

    /** @return the world Z of cell row 0's north edge. */
    public int originZ() {
        return this.originZ;
    }

    /** @return the world X at the centre of a cell column. */
    public int centreX(int cellX) {
        return this.originX + cellX * this.cellBlocks + this.cellBlocks / 2;
    }

    /** @return the world Z at the centre of a cell row. */
    public int centreZ(int cellZ) {
        return this.originZ + cellZ * this.cellBlocks + this.cellBlocks / 2;
    }

    /** @return the cell column containing a world X (wrapped on a cylinder). */
    public int cellX(int blockX) {
        int cell = Math.floorDiv(blockX - this.originX, this.cellBlocks);
        return this.wrapped ? wrapX(cell) : cell;
    }

    /** @return the cell row containing a world Z (never wrapped). */
    public int cellZ(int blockZ) {
        return Math.floorDiv(blockZ - this.originZ, this.cellBlocks);
    }

    /** @return true when the cell is land (outside the board is water). */
    public boolean land(int cellX, int cellZ) {
        if (cellZ < 0 || cellZ >= this.height) {
            return false;
        }
        int column = this.wrapped ? wrapX(cellX) : cellX;
        if (column < 0 || column >= this.width) {
            return false;
        }
        return this.land[cellZ * this.width + column];
    }

    /** @return the worldgen surface Y of the cell (water cells may be below sea level). */
    public int surfaceY(int cellX, int cellZ) {
        if (cellZ < 0 || cellZ >= this.height) {
            return 0;
        }
        int column = this.wrapped ? wrapX(cellX) : cellX;
        if (column < 0 || column >= this.width) {
            return 0;
        }
        return this.surface[cellZ * this.width + column];
    }

    /** Wraps a cell column onto the cylinder; a plane leaves it unchanged. */
    public int wrapX(int cellX) {
        return this.wrapped ? Math.floorMod(cellX, this.width) : cellX;
    }

    /** @return the shortest signed horizontal distance from {@code fromX} to {@code toX}, in cells. */
    public int wrapDelta(int fromX, int toX) {
        if (!this.wrapped) {
            return toX - fromX;
        }
        int delta = Math.floorMod(toX - fromX, this.width);
        return delta > this.width / 2 ? delta - this.width : delta;
    }
}
