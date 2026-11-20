package com.sanchous98.eldritchhorror.registry.blocks;

import com.sanchous98.eldritchhorror.core.ModConfig;
import com.sanchous98.eldritchhorror.endgame.DoomClock;
import com.sanchous98.eldritchhorror.endgame.RiftLedger;
import com.sanchous98.eldritchhorror.world.rift.RiftGates;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * The rift anchor: a standing gate (design/26, "The rift gate") and a ledger entry.
 *
 * <p>Two behaviours. Empty-hand use delegates to {@link RiftGates} — the rite check, two-step
 * linking and paid travel all live there, so the block itself stays a thin trigger and the client
 * only animates the swing. Whatever removes the block — the {@code close_rift} rite, a banish, a
 * pickaxe, an explosion, or an altar ring built over it — the world's {@link RiftLedger} hears about
 * it and its {@link com.sanchous98.eldritchhorror.world.rift.GateLinks}-side links are dropped, so no
 * one can travel to a gate that is gone.
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

    /**
     * 26.3 empty-hand block interaction. Guards the rite and travels/links through
     * {@link RiftGates}; a refused click changes nothing.
     */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hitResult) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (player instanceof ServerPlayer serverPlayer) {
            RiftGates.use(serverPlayer, pos);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos,
                                               boolean movedByPiston) {
        RiftLedger.of(level).remove(pos);
        // A closed rift buys the world time (design/11, "Relief"): the doom clock for the overworld
        // — the only clock there is — takes the configured step back. addDoom/relieve clamp and
        // stop themselves once the endgame is decided.
        DoomClock.relieve(level, ModConfig.DOOM_RIFT_RELIEF.get());
        RiftGates.breakLinks(level, pos);
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
    }
}
