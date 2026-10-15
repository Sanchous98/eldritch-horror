package com.sanchous98.eldritchhorror.registry.items;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

/**
 * Route Ledger (design/16): the written half of the marked map — use lists every waypoint the
 * holder has marked (oldest first, with a live bearing to each), from the same in-memory capped
 * {@link Waypoints} store. "Safe routes between strongholds" stay flavour (design/16): the mod has
 * no pathfinding service, so the ledger records where the holder has been, not how to get there.
 */
public final class RouteLedgerItem extends Item {

    public RouteLedgerItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (player instanceof ServerPlayer serverPlayer) {
            List<BlockPos> marks = Waypoints.list(serverPlayer);
            if (marks.isEmpty()) {
                serverPlayer.sendSystemMessage(Component.literal(
                        "The ledger's pages are blank; mark the map first."));
            } else {
                serverPlayer.sendSystemMessage(Component.literal(
                        "Route ledger — " + marks.size() + " mark(s):"));
                for (int i = 0; i < marks.size(); i++) {
                    BlockPos pos = marks.get(i);
                    serverPlayer.sendSystemMessage(Component.literal((i + 1) + ". "
                            + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + " — "
                            + NavigationItem.bearing(serverPlayer, pos)));
                }
            }
        }
        return InteractionResult.SUCCESS;
    }
}
