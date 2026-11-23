package com.sanchous98.eldritchhorror.world.loc;

import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StandingSignBlock;
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

    /**
     * Reads the block at {@code (x,y,z)} where it can be read. Outside the generating chunk, or
     * outside the world's vertical range, this returns air, mirroring {@link #put}'s clipping: a
     * caller never sees a neighbour chunk's stale state.
     */
    BlockState getState(int x, int y, int z);

    /**
     * Places a loot chest at {@code (x,y,z)}, filled from {@code lootTable} with a deterministic
     * seed. Clipped to the current chunk like {@link #put}; a cell already holding a chest is left
     * alone so two overlapping writes cannot double-place.
     */
    void chest(int x, int y, int z, net.minecraft.resources.ResourceKey<net.minecraft.world.level.storage.loot.LootTable> lootTable);

    /** Fills an inclusive box. */
    void fill(int x0, int y0, int z0, int x1, int y1, int z1, BlockState state);

    /** Hollow box (walls only), inclusive, with the floor/ceiling left as-is. */
    void walls(int x0, int y0, int z0, int x1, int y1, int z1, BlockState state);

    /** A hollow room: floor, ceiling and walls, optionally with a 1x2 doorway on `side`. */
    void room(int x0, int y0, int z0, int x1, int y1, int z1, Doorway... doorways);

    /** Replaces whatever is at (x,z) in [y0,y1] with `state`. */
    void ground(int x0, int z0, int x1, int z1, int y0, int y1, BlockState state);

    // ------------------------------------------------------------------ shaped ops
    // (see "Architecture vocabulary" in docs/STRUCTURES-CONTRACT.md)

    /**
     * A gable/hip roof over the rectangle, rising {@code height} blocks from base Y {@code y}.
     * {@code ridgeAxis} 0 = ridge runs along X, 1 = along Z. Built from {@link Palette#roofStairs()}
     * and {@link Palette#roofSlab()}; the underside is filled with {@link Palette#roof()}.
     */
    void pitchedRoof(int x0, int z0, int x1, int z1, int y, int height, int ridgeAxis);

    /** A tapered tower (wall/accent) with a stair cap and narrow window slits. */
    void spire(int cx, int cz, int baseY, int height);

    /** A stepped, leaning support column against a wall on {@code outward}. */
    void buttress(int x, int z, int baseY, int height, Side outward);

    /**
     * A framed window set into a wall line. {@code vertical} true makes it tall and narrow;
     * false makes it a wide horizontal band. {@code height}/{@code width} are in blocks.
     */
    void window(int x, int y, int z, int height, int width, boolean vertical);

    /** Alternating merlons around the rectangular perimeter at Y {@code y}. */
    void crenellations(int x0, int z0, int x1, int z1, int y);

    /** An obelisk / statue plinth (foundation base + tapering accent column + top). Not occult. */
    void monument(int cx, int cz, int baseY);

    /**
     * Deterministically scatters {@code state} through the box with probability {@code chance},
     * only replacing cells that are currently air / replaceable where that can be read.
     */
    void scatter(int x0, int z0, int x1, int z1, int y0, int y1, BlockState state, float chance);

    /** Deterministically carves holes through the box to read as decay. */
    void ruins(int x0, int y0, int z0, int x1, int y1, int z1, float holeChance);

    /** Deterministic RNG for this location (seeded by the location id). */
    RandomSource rng();

    /** Terrain surface Y at (x,z), clamped to the buildable range. */
    int groundY(int x, int z);

    /** True if (x,z) is real land (not ocean) per the baked landmask. */
    boolean isLand(int x, int z);

    /**
     * True if the cell at (x,y,z) is air or otherwise replaceable, where it can be read.
     * Inside the generating chunk this reflects the live chunk; outside it returns {@code false}
     * (writes there are clipped rather than compared). Used to place loose dressing without
     * overwriting a wall, door or road.
     */
    boolean isReplaceable(int x, int y, int z);

    /** Minimum block X of the chunk currently generating (inclusive). */
    int chunkMinX();

    /** Minimum block Z of the chunk currently generating (inclusive). */
    int chunkMinZ();

    /** The palette chosen from the real biome/terrain at this location. */
    Palette palette();

    /** A jigsaw marker: prefers a hand-authored .nbt named `name` if present. */
    void marker(String name, int x, int y, int z);

    /**
     * Places a standing sign at {@code (x,y,z)} facing {@code facing}. The default implementation
     * writes the block only; text is written by the concrete builder when the chunk is generating.
     */
    default void sign(int x, int y, int z, Direction facing, String... lines) {
        put(x, y, z, Blocks.OAK_SIGN.defaultBlockState()
                .setValue(StandingSignBlock.ROTATION, signRotation(facing)));
    }

    /** Standing-sign rotation segment: 0 = south, 4 = west, 8 = north, 12 = east. */
    private static int signRotation(Direction facing) {
        return switch (facing) {
            case SOUTH -> 0;
            case WEST -> 4;
            case NORTH -> 8;
            case EAST -> 12;
            default -> 8; // a standing sign cannot face up or down; default to north
        };
    }

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
