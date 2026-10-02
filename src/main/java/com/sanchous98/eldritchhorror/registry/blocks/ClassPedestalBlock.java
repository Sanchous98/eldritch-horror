package com.sanchous98.eldritchhorror.registry.blocks;

import com.sanchous98.eldritchhorror.classes.ClassAPI;
import com.sanchous98.eldritchhorror.classes.ClassId;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.NullMarked;

/**
 * A starting-class pedestal in the Threshold. Right-click chooses the block's {@link ClassId} via
 * {@link ClassAPI#set}, which is permanent (a second, different choice is refused with a message).
 * No GUI or block entity: the archetype is a construction parameter, so each pedestal is a single
 * blockstate with no properties.
 *
 * <p>Server-authoritative: only the server touches the class state; the client path returns success
 * so the arm swings.
 */
@NullMarked
public class ClassPedestalBlock extends Block {

    private final ClassId classId;

    public ClassPedestalBlock(ClassId classId, Properties properties) {
        super(properties);
        this.classId = classId;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hitResult) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (player instanceof ServerPlayer serverPlayer) {
            ClassAPI.set(serverPlayer, this.classId);
        }
        return InteractionResult.SUCCESS;
    }
}
