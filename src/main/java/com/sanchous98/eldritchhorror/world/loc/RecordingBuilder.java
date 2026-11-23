package com.sanchous98.eldritchhorror.world.loc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootTable;

/**
 * Records a city plan instead of writing it to the world. The plan is run once per city; the
 * resulting {@link CityWrite}s are replayed per chunk, so a 600–800-block authored city does not
 * re-run every placement for every one of its ~1,800 chunks.
 */
public final class RecordingBuilder extends Builder {

    private final Map<Long, CityWrite> byPosition = new HashMap<>();

    RecordingBuilder(WorldGenLevel level, ChunkPos chunk, RandomSource rng, Palette palette,
                     int centerX, int centerZ) {
        super(level, chunk, rng, palette, centerX, centerZ);
    }

    @Override
    public void put(int x, int y, int z, BlockState state) {
        add(new CityWrite(new BlockPos(x, y, z), state, null));
    }

    @Override
    public BlockState getState(int x, int y, int z) {
        // The recorder never reads the world: only its own recorded writes exist, and anything
        // unrecorded is air. Used for the plan's authored-vegetation bookkeeping.
        CityWrite write = this.byPosition.get(BlockPos.asLong(x, y, z));
        return write == null ? Blocks.AIR.defaultBlockState() : write.state();
    }

    @Override
    public void chest(int x, int y, int z, ResourceKey<LootTable> lootTable) {
        long seed = this.rng().nextLong();
        add(new CityWrite(new BlockPos(x, y, z), Blocks.CHEST.defaultBlockState(),
                blockEntity -> {
                    if (blockEntity instanceof ChestBlockEntity chest) {
                        chest.setLootTable(lootTable, seed);
                    }
                }));
    }

    @Override
    public void sign(int x, int y, int z, Direction facing, String... lines) {
        BlockState state = Blocks.OAK_SIGN.defaultBlockState()
                .setValue(StandingSignBlock.ROTATION, signRotation(facing));
        add(new CityWrite(new BlockPos(x, y, z), state, blockEntity -> {
            if (blockEntity.getLevel() != null) {
                CitySigns.queue(blockEntity.getLevel().dimension(), blockEntity.getBlockPos(), lines);
            }
        }));
    }

    @Override
    public void marker(String name, int x, int y, int z) {
        // Jigsaw markers are not needed for an authored city; the plan's blocks are the content.
    }

    public Map<Long, List<CityWrite>> writes() {
        Map<Long, List<CityWrite>> byChunk = new HashMap<>();
        for (CityWrite write : this.byPosition.values()) {
            byChunk.computeIfAbsent(ChunkPos.pack(write.pos().getX() >> 4,
                    write.pos().getZ() >> 4), ignored -> new ArrayList<>()).add(write);
        }
        return byChunk;
    }

    private void add(CityWrite write) {
        // Plans commonly paint the same column more than once (paving, then a wall, then a prop).
        // Keep only the final state per position: the replay must not apply stale overwrites, and
        // a 600–800-block city otherwise stores far more writes than it has blocks.
        this.byPosition.put(write.pos().asLong(), write);
    }

    private static int signRotation(Direction facing) {
        return switch (facing) {
            case SOUTH -> 0;
            case WEST -> 4;
            case NORTH -> 8;
            case EAST -> 12;
            default -> 8;
        };
    }
}
