package com.sanchous98.eldritchhorror.registry.items;

import com.sanchous98.eldritchhorror.corruption.CorruptionAPI;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

/**
 * Drowned Lung (design/16): a reusable active relic. Right-clicking grants long Water Breathing and
 * pays the design's drawback in corruption. Nothing is consumed — the lung is an artefact, not a
 * tonic — and the design's "indefinitely" is represented by {@value #BREATHING_TICKS} ticks
 * ({@value #BREATHING_TICKS}/{@code 1200} minutes) of breathing per use, the longest water blessing
 * in the mod.
 *
 * <p>Server-authoritative: the client path only swings. The corruption number is the "slowly" the
 * design records: a flat, small per-use cost (not a per-tick drain, because no lasting-effect system
 * exists — see {@link Consumables}' note on "over time").
 */
public final class DrownedLungItem extends Item {

    /** Water Breathing granted per use, in ticks (8 minutes). */
    private static final int BREATHING_TICKS = 9600;
    /** Corruption paid per use, applied through {@link CorruptionAPI}. */
    private static final double CORRUPTION_PER_USE = 2.0;

    public DrownedLungItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (player instanceof ServerPlayer serverPlayer) {
            CorruptionAPI.add(serverPlayer, CORRUPTION_PER_USE);
            serverPlayer.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING,
                    BREATHING_TICKS, 0, true, false, false));
            serverPlayer.sendSystemMessage(Component.literal("The drowned lung fills your chest for "
                    + BREATHING_TICKS / 1200 + " minutes (corruption +"
                    + (int) CORRUPTION_PER_USE + ")."));
        }
        return InteractionResult.SUCCESS;
    }
}
