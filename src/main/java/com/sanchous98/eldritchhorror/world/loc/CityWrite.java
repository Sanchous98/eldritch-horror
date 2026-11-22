package com.sanchous98.eldritchhorror.world.loc;

import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

/**
 * One recorded block write from a city plan, optionally with a block-entity initializer. The plan
 * is run once per city into these writes; every chunk then replays only its own slice.
 *
 * @param pos    absolute block position
 * @param state  block state to place
 * @param entity optional initializer run on the block entity after placement
 */
public record CityWrite(BlockPos pos, BlockState state,
                        @Nullable Consumer<BlockEntity> entity) {
}
