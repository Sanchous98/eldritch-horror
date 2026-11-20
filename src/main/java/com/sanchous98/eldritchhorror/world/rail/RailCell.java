package com.sanchous98.eldritchhorror.world.rail;

/**
 * One coarse rail-line cell: {@link RailGrid#CELL_BLOCKS} blocks square, indexed from the world
 * origin. Pure (no Minecraft type), so the line finder is unit-tested without a game.
 *
 * @param x cell column (wraps with the cylinder, see {@link RailGrid#wrapX})
 * @param z cell row, north to south
 */
public record RailCell(int x, int z) {
}
