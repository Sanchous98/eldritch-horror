package com.sanchous98.eldritchhorror.registry.blocks;

import com.sanchous98.eldritchhorror.investigator.Investigator;
import com.sanchous98.eldritchhorror.investigator.InvestigatorAPI;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;

/**
 * An investigator pedestal in the Threshold. The 12 pedestals share one block; the
 * {@link #INVESTIGATOR} property (0–11) indexes {@link Investigator#values()} so the whole inner
 * ring is a single registered block with 12 variants (see
 * {@code world.threshold.ThresholdPlacement.buildPedestals}).
 *
 * <p>Because the choice is permanent, it is <b>two-step</b>: the first right-click shows a
 * non-committal preview (occupation, starter kit, starting rite), and a second right-click on the
 * same pedestal within {@value #CONFIRM_WINDOW_TICKS} ticks commits via
 * {@link InvestigatorAPI#set}. This lets a player read every investigator before choosing any.
 *
 * <p>No GUI or block entity: the investigator is the blockstate property. Server-authoritative:
 * only the server touches the investigator state; the client path returns success so the arm swings.
 */
public class InvestigatorPedestalBlock extends Block {

    /** The investigator index, matching the order of {@link Investigator#values()}. */
    public static final IntegerProperty INVESTIGATOR = IntegerProperty.create("investigator", 0, 11);

    /** How long (ticks) a first click stays armed for confirmation. */
    private static final int CONFIRM_WINDOW_TICKS = 200;

    private record Pending(Investigator id, long untilTick) {
    }

    /** Per-player armed confirmation, dropped on use or when the window lapses. */
    private static final Map<UUID, Pending> PENDING = new ConcurrentHashMap<>();

    public InvestigatorPedestalBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(INVESTIGATOR);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hitResult) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.SUCCESS;
        }

        Investigator id = Investigator.values()[state.getValue(INVESTIGATOR)];
        Investigator current = InvestigatorAPI.get(serverPlayer);
        if (current == id) {
            serverPlayer.sendSystemMessage(Component.translatableWithFallback(
                    "investigator.eldritch_horror.same", "You are already %s.", id.displayName()));
            return InteractionResult.SUCCESS;
        }
        if (current != null) {
            serverPlayer.sendSystemMessage(Component.translatableWithFallback(
                    "investigator.eldritch_horror.locked",
                    "Your path is already chosen: %s. Ask an operator to reset it.",
                    current.displayName()));
            return InteractionResult.SUCCESS;
        }

        long now = level.getGameTime();
        PENDING.values().removeIf(p -> now > p.untilTick());
        Pending pending = PENDING.get(serverPlayer.getUUID());
        if (pending != null && pending.id() == id && now <= pending.untilTick()) {
            PENDING.remove(serverPlayer.getUUID());
            InvestigatorAPI.set(serverPlayer, id);
        } else {
            PENDING.put(serverPlayer.getUUID(), new Pending(id, now + CONFIRM_WINDOW_TICKS));
            InvestigatorAPI.preview(serverPlayer, id);
        }
        return InteractionResult.SUCCESS;
    }
}
