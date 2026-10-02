package com.sanchous98.eldritchhorror.sanity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/**
 * The cheap, per-tick facts a {@link SanitySource} may need. Built once per player per sanity
 * evaluation so sources do not each re-query the world.
 *
 * @param level     the player's level (always a {@link ServerLevel})
 * @param gameTime  the level game time
 * @param night     whether it is dark outside at the player's position
 * @param nearCity  whether the player stands inside a curated city's footprint
 */
public record SanityContext(ServerLevel level, long gameTime, boolean night, boolean nearCity) {

    /** Snapshot the context for one player. Cheap: one brightness/clock read, no chunk access. */
    public static SanityContext of(ServerPlayer player) {
        ServerLevel level = player.level();
        return new SanityContext(
                level,
                level.getGameTime(),
                level.isDarkOutside(),
                SanitySources.nearCity(player));
    }
}
