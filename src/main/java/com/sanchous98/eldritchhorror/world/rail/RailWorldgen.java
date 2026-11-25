package com.sanchous98.eldritchhorror.world.rail;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.core.ModConfig;
import com.sanchous98.eldritchhorror.world.loc.CitySigns;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.jspecify.annotations.Nullable;

/**
 * Stamps the cached rail network into the chunk being generated ({@code design/30-transport.md},
 * stage 2).
 *
 * <p>Called from {@code EarthChunkGenerator.applyBiomeDecoration} after the towns. Only lines with
 * a stamped corridor are in the index (see {@code trainMaxCorridorBlocks}); a long intercity line
 * has no entries and its train travels the planned polyline over generated ground. Every write is
 * clipped to the current chunk and the network is pre-bucketed by chunk, so a chunk pays one map
 * lookup plus its own few dozen stamps — never a scan of a line. A line crossing water becomes a
 * dark-oak deck with railings at a fixed height; a line through a ridge carves a bounded 3-wide
 * corridor with a rail+6 ceiling and lines it with stone bricks. Five blocks are kept clear above
 * every rail (the carriage is 3.8 tall). Rails are real blocks: walkable, buildable, blockable.
 */
public final class RailWorldgen {
    private static final BlockState BALLAST = Blocks.GRAVEL.defaultBlockState();
    private static final BlockState DECK = Blocks.DARK_OAK_PLANKS.defaultBlockState();
    private static final BlockState RAILING = Blocks.DARK_OAK_FENCE.defaultBlockState();
    private static final BlockState LINING = Blocks.STONE_BRICKS.defaultBlockState();
    private static final BlockState PLATFORM = Blocks.STONE_BRICKS.defaultBlockState();
    private static final BlockState CANOPY = Blocks.STONE_BRICK_SLAB.defaultBlockState();
    private static final BlockState PILLAR = Blocks.STONE_BRICKS.defaultBlockState();
    private static final BlockState BUFFER = Blocks.STONE_BRICKS.defaultBlockState();
    private static final BlockState LAMP = Blocks.LANTERN.defaultBlockState();
    private static volatile boolean warned;

    private RailWorldgen() {
    }

    /**
     * Writes this chunk's slice of every line and station platform that crosses it.
     *
     * @param level the worldgen region (writes are clipped to {@code chunk})
     * @param chunk the chunk currently generating
     */
    public static void place(WorldGenLevel level, ChunkAccess chunk) {
        if (!ModConfig.ENABLE_TRAINS.get()) {
            return;
        }
        Map<Long, List<RailStamp>> index;
        try {
            index = RailLines.stampsByChunk();
        } catch (RuntimeException error) {
            if (!warned) {
                warned = true;
                EldritchHorror.LOGGER.error("rail: could not build the rail network", error);
            }
            return;
        }
        ChunkPos pos = chunk.getPos();
        List<RailStamp> stamps = index.get(ChunkPos.pack(pos.x(), pos.z()));
        if (stamps == null || stamps.isEmpty()) {
            return;
        }
        for (RailStamp stamp : stamps) {
            switch (stamp.kind()) {
                case TRACK -> track(level, pos, stamp);
                case STATION_FLOOR -> {
                    put(level, pos, stamp.x(), stamp.y(), stamp.z(), PLATFORM);
                    // The floor block's top is the rail level, so the space above it starts at
                    // y; clear six courses (y..y+5) for the platform surface plus five clear.
                    clear(level, pos, stamp.x(), stamp.y(), stamp.z());
                    clear(level, pos, stamp.x(), stamp.y() + 1, stamp.z());
                    clear(level, pos, stamp.x(), stamp.y() + 2, stamp.z());
                    clear(level, pos, stamp.x(), stamp.y() + 3, stamp.z());
                    clear(level, pos, stamp.x(), stamp.y() + 4, stamp.z());
                    clear(level, pos, stamp.x(), stamp.y() + 5, stamp.z());
                }
                case STATION_FENCE ->
                        put(level, pos, stamp.x(), stamp.y(), stamp.z(), RAILING);
                case STATION_LANTERN ->
                        put(level, pos, stamp.x(), stamp.y(), stamp.z(), LAMP);
                case STATION_CANOPY ->
                        put(level, pos, stamp.x(), stamp.y(), stamp.z(), CANOPY);
                case STATION_PILLAR ->
                        put(level, pos, stamp.x(), stamp.y(), stamp.z(), PILLAR);
                case STATION_BUFFER ->
                        put(level, pos, stamp.x(), stamp.y(), stamp.z(), BUFFER);
                case STATION_SIGN -> sign(level, pos, stamp);
                case STATION_CLEAR ->
                        clear(level, pos, stamp.x(), stamp.y(), stamp.z());
            }
        }
    }

    private static void track(WorldGenLevel level, ChunkPos chunk, RailStamp stamp) {
        int perpX = -stamp.dirZ();
        int perpZ = stamp.dirX();
        if (stamp.tunnel()) {
            for (int offset = -1; offset <= 1; offset++) {
                int x = stamp.x() + perpX * offset;
                int z = stamp.z() + perpZ * offset;
                putIfNoRail(level, chunk, x, stamp.y() - 1, z, LINING);
                clearIfNoRail(level, chunk, x, stamp.y(), z);
                clearIfNoRail(level, chunk, x, stamp.y() + 1, z);
                clearIfNoRail(level, chunk, x, stamp.y() + 2, z);
                clearIfNoRail(level, chunk, x, stamp.y() + 3, z);
                clearIfNoRail(level, chunk, x, stamp.y() + 4, z);
                clearIfNoRail(level, chunk, x, stamp.y() + 5, z);
            }
            for (int offset = -2; offset <= 2; offset += 4) {
                for (int dy = -1; dy <= 5; dy++) {
                    putIfNoRail(level, chunk, stamp.x() + perpX * offset, stamp.y() + dy,
                            stamp.z() + perpZ * offset, LINING);
                }
            }
            // A solid tunnel roof at rail+6: the four-tall consist needs a real ceiling, not the
            // three-block corridor that used to clip its roof.
            for (int offset = -1; offset <= 1; offset++) {
                putIfNoRail(level, chunk, stamp.x() + perpX * offset, stamp.y() + 6,
                        stamp.z() + perpZ * offset, LINING);
            }
        } else if (stamp.bridge()) {
            for (int offset = -1; offset <= 1; offset++) {
                int x = stamp.x() + perpX * offset;
                int z = stamp.z() + perpZ * offset;
                putIfNoRail(level, chunk, x, stamp.y() - 1, z, DECK);
                if (offset != 0) {
                    putIfNoRail(level, chunk, x, stamp.y(), z, RAILING);
                }
                clearIfNoRail(level, chunk, x, stamp.y() + 1, z);
                clearIfNoRail(level, chunk, x, stamp.y() + 2, z);
                clearIfNoRail(level, chunk, x, stamp.y() + 3, z);
                clearIfNoRail(level, chunk, x, stamp.y() + 4, z);
                clearIfNoRail(level, chunk, x, stamp.y() + 5, z);
            }
        } else {
            for (int offset = -1; offset <= 1; offset++) {
                int x = stamp.x() + perpX * offset;
                int z = stamp.z() + perpZ * offset;
                putIfNoRail(level, chunk, x, stamp.y() - 1, z, BALLAST);
                clearIfNoRail(level, chunk, x, stamp.y() + 1, z);
                clearIfNoRail(level, chunk, x, stamp.y() + 2, z);
                clearIfNoRail(level, chunk, x, stamp.y() + 3, z);
                clearIfNoRail(level, chunk, x, stamp.y() + 4, z);
                clearIfNoRail(level, chunk, x, stamp.y() + 5, z);
            }
        }
        BlockState rail = Blocks.RAIL.defaultBlockState()
                .setValue(RailBlock.SHAPE, stamp.shape());
        put(level, chunk, stamp.x(), stamp.y(), stamp.z(), rail);
    }

    /**
     * Places a support block unless a rail already occupies the position. At a turn or a slope,
     * the next stamp's three-wide ballast overlaps the previous stamp's rail; protecting it keeps
     * the line continuous instead of burying a rail in gravel.
     */
    private static void putIfNoRail(WorldGenLevel level, ChunkPos chunk, int x, int y, int z,
                                    BlockState state) {
        if (inside(chunk, x, z, y, level)
                && !level.getBlockState(new BlockPos(x, y, z)).is(BlockTags.RAILS)) {
            level.setBlock(new BlockPos(x, y, z), state, 2);
        }
    }

    private static void clearIfNoRail(WorldGenLevel level, ChunkPos chunk, int x, int y, int z) {
        if (inside(chunk, x, z, y, level)
                && !level.getBlockState(new BlockPos(x, y, z)).is(BlockTags.RAILS)) {
            level.setBlock(new BlockPos(x, y, z), Blocks.AIR.defaultBlockState(), 2);
        }
    }

    /**
     * Places a station sign and queues its text through {@link CitySigns}; writing
     * {@code SignBlockEntity#setText} during worldgen would wait on the chunk system.
     */
    private static void sign(WorldGenLevel level, ChunkPos chunk, RailStamp stamp) {
        if (!inside(chunk, stamp.x(), stamp.z(), stamp.y(), level)) {
            return;
        }
        BlockPos pos = new BlockPos(stamp.x(), stamp.y(), stamp.z());
        level.setBlock(pos, Blocks.OAK_SIGN.defaultBlockState(), 2);
        CitySigns.queue(level.getLevel().dimension(), pos, stamp.label(), "STATION");
    }

    private static void put(WorldGenLevel level, ChunkPos chunk, int x, int y, int z,
                            BlockState state) {
        if (inside(chunk, x, z, y, level)) {
            level.setBlock(new BlockPos(x, y, z), state, 2);
        }
    }

    private static void clear(WorldGenLevel level, ChunkPos chunk, int x, int y, int z) {
        if (inside(chunk, x, z, y, level)) {
            level.setBlock(new BlockPos(x, y, z), Blocks.AIR.defaultBlockState(), 2);
        }
    }

    private static boolean inside(ChunkPos chunk, int x, int z, int y, WorldGenLevel level) {
        return x >> 4 == chunk.x() && z >> 4 == chunk.z()
                && y >= level.getMinY() && y <= level.getMaxY();
    }

    /** @return the first stamp kind at a block, for the debug probe, or {@code null}. */
    public static @Nullable RailStamp stampAt(int x, int y, int z) {
        List<RailStamp> stamps = RailLines.stampsByChunk().get(ChunkPos.pack(x >> 4, z >> 4));
        if (stamps == null) {
            return null;
        }
        for (RailStamp stamp : stamps) {
            if (stamp.x() == x && stamp.y() == y && stamp.z() == z) {
                return stamp;
            }
        }
        return null;
    }
}
