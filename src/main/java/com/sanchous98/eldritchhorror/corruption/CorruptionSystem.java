package com.sanchous98.eldritchhorror.corruption;

import com.sanchous98.eldritchhorror.registry.ModAttachments;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.chunk.LevelChunk;

/**
 * Corruption / taint: a durable per-player (and per-chunk) exposure value.
 *
 * <p>Owns the values; {@link CorruptionSources} supply the deltas and {@link CorruptionTicker}
 * applies them once a second and fires the stage effects. The per-chunk taint field diffuses
 * through {@link TaintSystem}. Block conversion and ambient spawns are still to come.
 *
 * <p>States: Dormant → Touched → Marked → Claimed (see {@link CorruptionState}); the meter is
 * synced to its owner by the {@code CORRUPTION} attachment.
 */
public final class CorruptionSystem {
    /** Default ceiling for the per-player value. */
    public static final double DEFAULT_MAX = 100.0;

    private CorruptionSystem() {
    }

    /** Current player corruption, clamped to {@code [0, DEFAULT_MAX]}. */
    public static double get(ServerPlayer player) {
        return Math.clamp(player.getData(ModAttachments.CORRUPTION.get()), 0.0, DEFAULT_MAX);
    }

    /** Sets player corruption (clamped). Returns the stored value. */
    public static double set(ServerPlayer player, double value) {
        double v = Math.clamp(value, 0.0, DEFAULT_MAX);
        player.setData(ModAttachments.CORRUPTION.get(), v);
        return v;
    }

    /** Adds {@code delta} (may be negative). Returns the new value. */
    public static double add(ServerPlayer player, double delta) {
        return set(player, get(player) + delta);
    }

    /** The current state band for {@code player}, derived from the meter and the configured cut-offs. */
    public static CorruptionState state(ServerPlayer player) {
        return CorruptionState.of(get(player));
    }

    /**
     * Per-chunk taint, {@code 0..1}, shared by all players. Held on the {@code LevelChunk} so it
     * persists and saves with the world; diffused by {@link TaintSystem}.
     */
    public static double getTaint(LevelChunk chunk) {
        return Math.clamp(chunk.getData(ModAttachments.TAINT.get()), 0.0, 1.0);
    }

    public static double setTaint(LevelChunk chunk, double value) {
        double v = Math.clamp(value, 0.0, 1.0);
        chunk.setData(ModAttachments.TAINT.get(), v);
        chunk.markUnsaved();
        return v;
    }

    public static double addTaint(LevelChunk chunk, double delta) {
        return setTaint(chunk, getTaint(chunk) + delta);
    }

    /**
     * Taint of the chunk the player currently occupies, {@code 0..1}. Reads only the player's own
     * chunk via {@code ServerChunkCache.getChunkNow} (no load/generate); returns {@code 0} if it is
     * not loaded yet.
     */
    public static double getTaintAt(ServerPlayer player) {
        ServerLevel level = (ServerLevel) player.level();
        LevelChunk chunk = level.getChunkSource().getChunkNow(
                player.chunkPosition().x(), player.chunkPosition().z());
        return chunk == null ? 0.0 : getTaint(chunk);
    }
}
