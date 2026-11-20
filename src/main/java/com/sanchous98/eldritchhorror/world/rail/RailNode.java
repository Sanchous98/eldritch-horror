package com.sanchous98.eldritchhorror.world.rail;

/**
 * One block of a finished rail line: the rail block's world coordinates.
 *
 * <p>The rail block sits at {@code y}; the block below is ballast or a bridge deck. Pure, so the
 * sampler is unit-tested without a game.
 *
 * @param x world X of the rail block
 * @param y world Y of the rail block
 * @param z world Z of the rail block
 */
public record RailNode(int x, int y, int z) {
}
