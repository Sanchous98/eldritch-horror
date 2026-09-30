package com.sanchous98.eldritchhorror.corruption;

import com.sanchous98.eldritchhorror.registry.ModAttachments;
import net.minecraft.server.level.ServerPlayer;

/**
 * Corruption / taint: a durable per-player (and per-chunk) exposure value.
 *
 * <p><b>This is a stub.</b> It stores values that can rise and fall and persist/copy on death,
 * but nothing reads it for gameplay yet — no sources, no chunk effect, no stage effects, no block
 * conversion or spawns. The shape is deliberately final so content can be built against it now
 * (see {@code design/27-systems-framework.md}).
 *
 * <p>Later: per-player stages (Dormant → Touched → Marked → Claimed) that gate rites and change
 * the body, plus a per-chunk taint field that converts blocks and spawns ambient horrors.
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

    /**
     * Per-chunk taint, {@code 0..1}, shared by all players. Held on the {@code LevelChunk} so it
     * persists and saves with the world. Not used for anything yet.
     */
    public static double getTaint(net.minecraft.world.level.chunk.LevelChunk chunk) {
        return Math.clamp(chunk.getData(ModAttachments.TAINT.get()), 0.0, 1.0);
    }

    public static double setTaint(net.minecraft.world.level.chunk.LevelChunk chunk, double value) {
        double v = Math.clamp(value, 0.0, 1.0);
        chunk.setData(ModAttachments.TAINT.get(), v);
        chunk.markUnsaved();
        return v;
    }

    public static double addTaint(net.minecraft.world.level.chunk.LevelChunk chunk, double delta) {
        return setTaint(chunk, getTaint(chunk) + delta);
    }
}
