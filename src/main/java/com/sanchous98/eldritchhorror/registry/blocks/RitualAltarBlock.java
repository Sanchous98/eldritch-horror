package com.sanchous98.eldritchhorror.registry.blocks;

import com.sanchous98.eldritchhorror.rite.RiteConditions;
import com.sanchous98.eldritchhorror.rite.RiteDefinition;
import com.sanchous98.eldritchhorror.rite.RiteKnowledge;
import com.sanchous98.eldritchhorror.rite.RiteReagents;
import com.sanchous98.eldritchhorror.rite.Rites;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.TreeSet;

/**
 * The altar centrepiece: an interactive block that reports which rites the performer knows.
 *
 * <p>Right-clicking opens no GUI. Server-side (and server-authoritative) it lists the caller's
 * known rites and reminds them to perform one with {@code /eh rite <id>}. The client path only
 * returns success so the swing animates; it changes no state. The rite itself is resolved by
 * {@code RiteEngine}, so the altar stays a thin, deterministic prompt.
 */
public class RitualAltarBlock extends Block {

    public RitualAltarBlock(Properties properties) {
        super(properties);
    }

    /**
     * 26.3 empty-hand block interaction. Verified against
     * {@code BlockBehaviour#useWithoutItem(BlockState, Level, BlockPos, Player, BlockHitResult)}.
     */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hitResult) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (player instanceof ServerPlayer serverPlayer) {
            TreeSet<String> known = new TreeSet<>(RiteKnowledge.known(serverPlayer));
            if (known.isEmpty()) {
                serverPlayer.sendSystemMessage(Component.literal(
                        "The altar is cold: you know no rites. Seek a tome or a cult."));
            } else {
                serverPlayer.sendSystemMessage(Component.literal("Known rites (offerings):"));
                for (String id : known) {
                    RiteDefinition rite = Rites.byId(id);
                    if (rite == null) {
                        continue;
                    }
                    serverPlayer.sendSystemMessage(Component.literal("  " + id + " — ")
                            .append(RiteReagents.describe(RiteReagents.offerings(rite)))
                            .append(Component.literal("  [").append(RiteConditions.of(rite).describe())
                                    .append(Component.literal("]"))));
                }
                serverPlayer.sendSystemMessage(Component.literal(
                        "Use /eh rite <id> to lay the offerings and perform one."));
            }
        }
        return InteractionResult.SUCCESS;
    }
}
