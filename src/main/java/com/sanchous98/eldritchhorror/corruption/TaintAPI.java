package com.sanchous98.eldritchhorror.corruption;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;

/**
 * Public entry point to the per-chunk taint field for content — cleansing rites, rifts, altars.
 *
 * <p>Per {@code design/27-systems-framework.md} (lines 34-35), content <b>never touches the
 * attachment directly</b>; it calls this facade, a thin delegation to {@link CorruptionSystem}.
 * Taint is {@code 0..1}, shared by everyone and persisted with the chunk, so it is
 * server-authoritative and this facade is server-side only.
 *
 * <p>{@link #get}/{@link #add}/{@link #set} take a loaded {@link LevelChunk}. The convenience
 * overloads take a {@link ServerLevel} + {@link ChunkPos} and return {@code 0} / no-op when the
 * chunk is not loaded — they never force a load or generation.
 */
public final class TaintAPI {
    private TaintAPI() {
    }

    /** Taint of {@code chunk}, clamped to {@code [0, 1]}. */
    public static double get(LevelChunk chunk) {
        return CorruptionSystem.getTaint(chunk);
    }

    /** Adds {@code delta} to {@code chunk} (clamped). Returns the new value. */
    public static double add(LevelChunk chunk, double delta) {
        return CorruptionSystem.addTaint(chunk, delta);
    }

    /** Sets {@code chunk} taint (clamped). Returns the stored value. */
    public static double set(LevelChunk chunk, double value) {
        return CorruptionSystem.setTaint(chunk, value);
    }

    /** Taint of the loaded chunk at {@code pos}, or {@code 0} if it is not loaded. */
    public static double get(ServerLevel level, ChunkPos pos) {
        LevelChunk chunk = level.getChunkSource().getChunkNow(pos.x(), pos.z());
        return chunk == null ? 0.0 : CorruptionSystem.getTaint(chunk);
    }

    /** Adds to the loaded chunk at {@code pos}; no-op if it is not loaded. Returns the new value. */
    public static double add(ServerLevel level, ChunkPos pos, double delta) {
        LevelChunk chunk = level.getChunkSource().getChunkNow(pos.x(), pos.z());
        return chunk == null ? 0.0 : CorruptionSystem.addTaint(chunk, delta);
    }

    /** Sets the loaded chunk at {@code pos}; no-op if it is not loaded. Returns the stored value. */
    public static double set(ServerLevel level, ChunkPos pos, double value) {
        LevelChunk chunk = level.getChunkSource().getChunkNow(pos.x(), pos.z());
        return chunk == null ? 0.0 : CorruptionSystem.setTaint(chunk, value);
    }

    /** Taint of the chunk {@code player} currently occupies, or {@code 0} if it is not loaded. */
    public static double getAt(ServerPlayer player) {
        return CorruptionSystem.getTaintAt(player);
    }
}
