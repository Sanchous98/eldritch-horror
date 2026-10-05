package com.sanchous98.eldritchhorror.registry.items;

import com.sanchous98.eldritchhorror.core.ClientHooks;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

/**
 * The world atlas: right-clicking opens the fullscreen Earth map (see {@code design/22}).
 *
 * <p>Use runs on both sides. On the client it runs {@link ClientHooks#openWorldMap}, which the
 * client-only setup fills in; on the server the slot stays a no-op, so no GUI class is touched and
 * the common class never references the client package. Going through {@code Item#use} (rather than
 * a right-click event) means the map opens even while the player is pointing at a block.
 */
public final class WorldAtlasItem extends Item {

    public WorldAtlasItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) {
            ClientHooks.openWorldMap.run();
        }
        return InteractionResult.SUCCESS;
    }
}
