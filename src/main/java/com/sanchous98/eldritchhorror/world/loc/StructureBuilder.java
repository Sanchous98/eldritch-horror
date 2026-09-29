package com.sanchous98.eldritchhorror.world.loc;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The one interface every location is authored against (see {@code docs/STRUCTURES-CONTRACT.md}).
 * <b>No location writes blocks directly.</b>
 *
 * <p>A builder is handed the chunk currently generating; writes outside it are dropped, so a
 * large structure is built incrementally as its chunks generate.
 */
public interface StructureBuilder {

    /** Writes one block, clipped to the chunk currently generating. */
    void put(int x, int y, int z, BlockState state);

    /** Fills an inclusive box. */
    void fill(int x0, int y0, int z0, int x1, int y1, int z1, BlockState state);

    /** Hollow box (walls only), inclusive, with the floor/ceiling left as-is. */
    void walls(int x0, int y0, int z0, int x1, int y1, int z1, BlockState state);

    /** A hollow room: floor, ceiling and walls, optionally with a 1x2 doorway on `side`. */
    void room(int x0, int y0, int z0, int x1, int y1, int z1, Doorway... doorways);

    /** Replaces whatever is at (x,z) in [y0,y1] with `state`. */
    void ground(int x0, int z0, int x1, int z1, int y0, int y1, BlockState state);

    /** Deterministic RNG for this location (seeded by the location id). */
    RandomSource rng();

    /** Terrain surface Y at (x,z), clamped to the buildable range. */
    int groundY(int x, int z);

    /** The palette chosen from the real biome/terrain at this location. */
    Palette palette();

    /** A jigsaw marker: prefers a hand-authored .nbt named `name` if present. */
    void marker(String name, int x, int y, int z);

    /** A cardinal side of a room, for doorway placement. */
    enum Side {
        N, E, S, W
    }

    /**
     * A 1x2 doorway on one wall of a {@link #room}. {@code offset} is measured in blocks from the
     * room's minimum corner along that wall.
     */
    record Doorway(Side side, int offset) {
    }
}
