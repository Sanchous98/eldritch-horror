package com.sanchous98.eldritchhorror.registry.blocks;

import com.sanchous98.eldritchhorror.classes.Prologue;
import com.sanchous98.eldritchhorror.world.city.Cities;
import com.sanchous98.eldritchhorror.world.city.City;
import java.util.List;
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
import org.jspecify.annotations.NullMarked;

/**
 * One of the 24 destination gates in the Threshold. The {@link #CITY} property (0–23) indexes
 * {@link Cities#all()}; using the block sends the player to that city, completing the prologue via
 * {@link Prologue#complete}.
 *
 * <p>The index is a blockstate rather than a block entity so the whole ring stays deterministic and
 * persists in the world save with no extra state. No property is user-settable beyond the 24
 * variants stamped by {@code world.threshold.ThresholdPlacement}.
 */
@NullMarked
public class CityGateBlock extends Block {

    /** The curated-city index, matching the order of {@link Cities#all()}. */
    public static final IntegerProperty CITY = IntegerProperty.create("city", 0, 23);

    public CityGateBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(CITY);
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

        List<City> cities = Cities.all();
        int index = state.getValue(CITY);
        if (index >= cities.size()) {
            // Fewer curated cities than the ring was sized for: the gate is inert rather than
            // throwing an IndexOutOfBoundsException on use.
            serverPlayer.sendSystemMessage(Component.translatable("prologue.eldritch_horror.no_city"));
            return InteractionResult.SUCCESS;
        }

        City city = cities.get(index);
        serverPlayer.sendSystemMessage(Component.translatable("prologue.eldritch_horror.gate", city.name()));
        Prologue.complete(serverPlayer, city);
        return InteractionResult.SUCCESS;
    }
}
