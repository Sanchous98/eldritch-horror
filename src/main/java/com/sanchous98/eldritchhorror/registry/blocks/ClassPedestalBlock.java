package com.sanchous98.eldritchhorror.registry.blocks;

import com.sanchous98.eldritchhorror.classes.ClassAPI;
import com.sanchous98.eldritchhorror.classes.ClassId;
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
import net.minecraft.world.phys.BlockHitResult;

/**
 * A starting-class pedestal in the Threshold. Because the choice is permanent, it is <b>two-step</b>:
 * the first right-click shows a non-committal preview (fantasy, starter kit, starting rite), and a
 * second right-click on the same pedestal within {@value #CONFIRM_WINDOW_TICKS} ticks commits via
 * {@link ClassAPI#set}. This lets a player read every archetype before choosing any — the previous
 * behaviour granted on the first click, so the only way to preview was to commit.
 *
 * <p>No GUI or block entity: the archetype is a construction parameter, so each pedestal is a single
 * blockstate with no properties. Server-authoritative: only the server touches the class state; the
 * client path returns success so the arm swings.
 */
public class ClassPedestalBlock extends Block {

    /** How long (ticks) a first click stays armed for confirmation. */
    private static final int CONFIRM_WINDOW_TICKS = 200;

    private record Pending(ClassId id, long untilTick) {
    }

    /** Per-player armed confirmation, dropped on use or when the window lapses. */
    private static final Map<UUID, Pending> PENDING = new ConcurrentHashMap<>();

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
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.SUCCESS;
        }

        ClassId current = ClassAPI.get(serverPlayer);
        if (current == this.classId) {
            serverPlayer.sendSystemMessage(Component.translatableWithFallback(
                    "class.eldritch_horror.same", "You are already %s.", this.classId.displayName()));
            return InteractionResult.SUCCESS;
        }
        if (current != null) {
            serverPlayer.sendSystemMessage(Component.translatableWithFallback(
                    "class.eldritch_horror.locked", "Your path is already chosen: %s. Ask an operator to reset it.",
                    current.displayName()));
            return InteractionResult.SUCCESS;
        }

        long now = level.getGameTime();
        PENDING.values().removeIf(p -> now > p.untilTick());
        Pending pending = PENDING.get(serverPlayer.getUUID());
        if (pending != null && pending.id() == this.classId && now <= pending.untilTick()) {
            PENDING.remove(serverPlayer.getUUID());
            ClassAPI.set(serverPlayer, this.classId);
        } else {
            PENDING.put(serverPlayer.getUUID(), new Pending(this.classId, now + CONFIRM_WINDOW_TICKS));
            ClassAPI.preview(serverPlayer, this.classId);
        }
        return InteractionResult.SUCCESS;
    }
}
