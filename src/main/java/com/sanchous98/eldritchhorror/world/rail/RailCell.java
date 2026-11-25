package com.sanchous98.eldritchhorror.world.rail;

/**
 * One route cell: a square of {@link RailGrid#cellBlocks()} blocks, indexed from the grid origin.
 * Pure (no Minecraft type), so the line finder is unit-tested without a game.
 *
 * @param x cell column (wraps on the legacy cylinder, see {@link RailGrid#wrapX})
 * @param z cell row, north to south
 */
public record RailCell(int x, int z) {
}
