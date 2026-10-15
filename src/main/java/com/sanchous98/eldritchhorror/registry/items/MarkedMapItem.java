package com.sanchous98.eldritchhorror.registry.items;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

/**
 * Marked Map (design/16): the honest half of "stores and reveals player-placed waypoints".
 *
 * <p><b>Sneak-use</b> marks the spot the holder stands on ({@link Waypoints}: per-player, in-memory,
 * capped); <b>plain use</b> points at the nearest mark. client-only (design/16): rendering the marks
 * on the fullscreen world map would be a client change — the map today draws the player, cities,
 * discovered sites and rifts only.
 */
public final class MarkedMapItem extends Item {

    public MarkedMapItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (player instanceof ServerPlayer serverPlayer) {
            if (serverPlayer.isShiftKeyDown()) {
                Waypoints.add(serverPlayer, serverPlayer.blockPosition());
                serverPlayer.sendSystemMessage(Component.literal("You mark this place ("
                        + Waypoints.list(serverPlayer).size() + "/" + Waypoints.MAX_MARKS + ")."));
            } else {
                serverPlayer.sendSystemMessage(Component.literal(pull(serverPlayer)));
            }
        }
        return InteractionResult.SUCCESS;
    }

    /** The plain-use line: a bearing to the nearest mark, or a hint that there are none yet. */
    private static String pull(ServerPlayer player) {
        BlockPos nearest = Waypoints.nearest(player);
        if (nearest == null) {
            return "The marked map is blank; sneak-use to mark where you stand.";
        }
        return "The marked map pulls " + NavigationItem.bearing(player, nearest) + ".";
    }
}
