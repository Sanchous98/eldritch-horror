package com.sanchous98.eldritchhorror.event;

import com.sanchous98.eldritchhorror.corruption.CorruptionState;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

/**
 * The cheap, per-player facts an {@link EldritchEvent} trigger may read, snapshotted once per
 * evaluation by {@link EventTicker} (mirrors {@code SanityContext}/{@code CorruptionContext}).
 *
 * @param level        the player's overworld level (always a {@link ServerLevel})
 * @param gameTime     the level game time
 * @param ticks        the server tick count at evaluation
 * @param night        whether it is dark outside at the player's position
 * @param thundering   whether the level is currently thundering
 * @param sanity       the player's current sanity
 * @param taint        the taint of the chunk the player currently occupies, {@code 0..1}
 * @param rifts        up to the configured cap of nearby loaded rift anchors (empty if unused)
 * @param fullMoon     whether the moon phase at the player's position is {@code FULL_MOON}
 * @param corruption   the player's corruption band (from {@code CorruptionAPI.get})
 * @param nearCultSite whether the player is within the configured radius of the fixed
 *                     {@code cult_stronghold} centre
 */
public record EventContext(ServerLevel level, long gameTime, int ticks, boolean night,
                           boolean thundering, double sanity, double taint, List<BlockPos> rifts,
                           boolean fullMoon, CorruptionState corruption, boolean nearCultSite) {
}
