package com.sanchous98.eldritchhorror.registry.blocks;

import com.sanchous98.eldritchhorror.investigator.Prologue;
import com.sanchous98.eldritchhorror.world.city.Cities;
import com.sanchous98.eldritchhorror.world.city.City;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;

/**
 * One of the 24 destination gates in the Threshold, built as a walk-through portal. The
 * {@link #CITY} property (0–23) indexes {@link Cities#all()}; entering the portal column (or, as a
 * fallback, right-clicking it) sends the player to that city, completing the prologue via
 * {@link Prologue#complete}.
 *
 * <p>Registered with {@code noCollision()} so a player can stand in the gate and be carried through
 * by {@link #entityInside}. Interaction is server-authoritative: the client path only swings the
 * arm. A short per-player cooldown keyed on game time stops a gate that cannot complete (e.g. no
 * investigator chosen yet) from spamming its message every tick.
 *
 * <p>The index is a blockstate rather than a block entity so the whole ring stays deterministic and
 * persists in the world save with no extra state. No property is user-settable beyond the 24
 * variants stamped by {@code world.threshold.ThresholdPlacement}.
 */
public class CityGateBlock extends Block {

    /** The curated-city index, matching the order of {@link Cities#all()}. */
    public static final IntegerProperty CITY = IntegerProperty.create("city", 0, 23);

    /** Ticks between two gate attempts by the same player; one second at 20 TPS. */
    private static final long COOLDOWN_TICKS = 20L;

    /** Last attempt per player, in game time, so a failed gate does not message every tick. */
    private static final Map<UUID, Long> LAST_ATTEMPT = new ConcurrentHashMap<>();

    public CityGateBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(CITY);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity,
                                InsideBlockEffectApplier effectApplier, boolean isPrecise) {
        if (level.isClientSide() || !(entity instanceof ServerPlayer serverPlayer)) {
            return;
        }
        if (onCooldown(level, serverPlayer.getUUID())) {
            return;
        }
        enter(serverPlayer, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hitResult) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (player instanceof ServerPlayer serverPlayer
                && !onCooldown(level, serverPlayer.getUUID())) {
            enter(serverPlayer, state);
        }
        return InteractionResult.SUCCESS;
    }

    /** Resolves the gate's city and completes the prologue for {@code player}. */
    private static void enter(ServerPlayer player, BlockState state) {
        List<City> cities = Cities.all();
        int index = state.getValue(CITY);
        if (index >= cities.size()) {
            // Fewer curated cities than the ring was sized for: the gate is inert rather than
            // throwing an IndexOutOfBoundsException on use.
            player.sendSystemMessage(Component.translatable("prologue.eldritch_horror.no_city"));
            return;
        }
        City city = cities.get(index);
        player.sendSystemMessage(Component.translatable("prologue.eldritch_horror.gate", city.name()));
        Prologue.complete(player, city);
    }

    /**
     * @return whether {@code player} attempted a gate too recently. Records the attempt time when
     *     the player is free to try, so both the portal and right-click paths share the cooldown.
     */
    private static boolean onCooldown(Level level, UUID playerId) {
        long now = level.getGameTime();
        Long last = LAST_ATTEMPT.get(playerId);
        if (last != null && now - last < COOLDOWN_TICKS) {
            return true;
        }
        LAST_ATTEMPT.put(playerId, now);
        return false;
    }
}
