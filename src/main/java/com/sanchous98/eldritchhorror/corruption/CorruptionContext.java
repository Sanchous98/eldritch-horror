package com.sanchous98.eldritchhorror.corruption;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/**
 * The cheap, per-tick facts a {@link CorruptionSource} may need. Built once per player per
 * corruption evaluation so sources do not each re-query the world.
 *
 * @param level     the player's level (always a {@link ServerLevel})
 * @param gameTime  the level game time
 * @param chunkTaint the taint of the chunk the player currently occupies, {@code 0..1}
 */
public record CorruptionContext(ServerLevel level, long gameTime, double chunkTaint) {

    /**
     * Snapshot the context for one player. Cheap: reads the player's own chunk only (no chunk
     * loading beyond it) via {@link CorruptionSystem#getTaintAt(ServerPlayer)}.
     */
    public static CorruptionContext of(ServerPlayer player) {
        ServerLevel level = (ServerLevel) player.level();
        return new CorruptionContext(level, level.getGameTime(),
                CorruptionSystem.getTaintAt(player));
    }
}
