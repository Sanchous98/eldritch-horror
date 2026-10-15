package com.sanchous98.eldritchhorror.registry.blocks;

import com.sanchous98.eldritchhorror.endgame.RiftLedger;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The rift anchor, with exactly one behaviour: whatever removes it — the {@code close_rift} rite, a
 * banish, a pickaxe, an explosion, or an altar ring built over it — the world's {@link RiftLedger}
 * hears about it. Without this the ledger could keep a rift "open" that no longer exists, and the
 * third outcome ("seal every rift", {@code design/11-endgame.md}) would drift out of reach.
 *
 * <p>26.3 renamed the old {@code onRemove} to {@link #affectNeighborsAfterRemoval}; it is invoked on
 * the <em>old</em> state by {@code LevelChunk#setBlockState} only when the block type actually
 * changes (a same-block state update never reaches it), and the signature already carries a
 * {@link ServerLevel}, so both the old guard and the side check are gone. The ledger also prunes
 * entries whose anchor a loaded chunk says is gone, so a save written before this block existed
 * still heals; this hook keeps the normal path exact rather than relying on the safety net.
 */
public final class RiftAnchorBlock extends Block {

    public RiftAnchorBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos,
                                               boolean movedByPiston) {
        RiftLedger.of(level).remove(pos);
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
    }
}
