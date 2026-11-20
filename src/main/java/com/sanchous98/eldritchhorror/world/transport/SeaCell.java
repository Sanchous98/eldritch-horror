package com.sanchous98.eldritchhorror.world.transport;

/**
 * One coarse sea-lane cell: {@link SeaGrid#CELL_BLOCKS} blocks square, indexed from the world
 * origin. Pure (no Minecraft type), so the lane finder is unit-tested without a game.
 *
 * @param x cell column (wraps with the cylinder, see {@link SeaGrid#wrapX})
 * @param z cell row, north to south
 */
public record SeaCell(int x, int z) {
}
