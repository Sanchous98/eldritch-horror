package com.sanchous98.eldritchhorror.registry.items;

import com.sanchous98.eldritchhorror.corruption.CorruptionAPI;
import com.sanchous98.eldritchhorror.rite.RiteDefinition;
import com.sanchous98.eldritchhorror.rite.RiteEngine;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

/**
 * Chalice of Echoes (design/16): holds the holder's last <b>successful</b> rite — recorded by
 * {@code RiteEngine} on success into {@link RiteEchoes} (in-memory, documented there) — and lets
 * them re-cast it at an altar. The re-cast skips the rite's offerings
 * ({@code RiteEngine.perform(player, rite, true)}); each echo instead pays corruption
 * +{@value #ECHO_CORRUPTION} at the chalice, on top of the rite's own gains. Repeatable: every
 * echo adds corruption, exactly as the design's drawback says.
 */
public final class ChaliceOfEchoesItem extends Item {

    /** Corruption paid per echo, in place of the rite's reagents. */
    private static final double ECHO_CORRUPTION = 5.0;

    public ChaliceOfEchoesItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (player instanceof ServerPlayer serverPlayer) {
            RiteDefinition echo = RiteEchoes.last(serverPlayer);
            if (echo == null) {
                serverPlayer.sendSystemMessage(Component.literal(
                        "The chalice holds no echo; a rite that takes effect will fill it."));
            } else {
                RiteEngine.Result result = RiteEngine.perform(serverPlayer, echo, true);
                serverPlayer.sendSystemMessage(result.message());
                if (result.ok()) {
                    CorruptionAPI.add(serverPlayer, ECHO_CORRUPTION);
                    serverPlayer.sendSystemMessage(Component.literal(
                            "The echo is drunk; corruption +" + (int) ECHO_CORRUPTION + "."));
                }
            }
        }
        return InteractionResult.SUCCESS;
    }
}
