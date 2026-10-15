package com.sanchous98.eldritchhorror.registry.items;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import org.jspecify.annotations.Nullable;

/**
 * The marked map's waypoint store (design/16/22): a per-player, in-memory, capped list of the spots
 * a holder has marked. Deliberately not persisted — a mark is a divination focus, not a save, so it
 * resets with the server (the same shape as the rite cooldown map). The map screen already draws
 * only the player, cities, sites and rifts; drawing these marks is client work (design/16).
 *
 * <p>Bounded on purpose: {@value #MAX_MARKS} marks per player, oldest evicted first, so the state
 * cannot grow without limit. The deques are guarded because commands, events and ticks can all
 * touch them.
 */
public final class Waypoints {

    /** Marks kept per player; a new mark past the cap evicts the oldest. */
    public static final int MAX_MARKS = 8;

    private static final Map<UUID, Deque<BlockPos>> MARKS = new ConcurrentHashMap<>();

    private Waypoints() {
    }

    /** Marks {@code pos} for {@code player}, evicting the oldest mark past {@link #MAX_MARKS}. */
    public static void add(ServerPlayer player, BlockPos pos) {
        Deque<BlockPos> marks = MARKS.computeIfAbsent(player.getUUID(), key -> new ArrayDeque<>());
        synchronized (marks) {
            while (marks.size() >= MAX_MARKS) {
                marks.removeFirst();
            }
            marks.addLast(pos.immutable());
        }
    }

    /** A snapshot of {@code player}'s marks, oldest first. */
    public static List<BlockPos> list(ServerPlayer player) {
        Deque<BlockPos> marks = MARKS.get(player.getUUID());
        if (marks == null) {
            return List.of();
        }
        synchronized (marks) {
            return List.copyOf(marks);
        }
    }

    /** The nearest mark to {@code player}, or {@code null} when they have none. */
    public static @Nullable BlockPos nearest(ServerPlayer player) {
        BlockPos nearest = null;
        double best = Double.MAX_VALUE;
        for (BlockPos pos : list(player)) {
            double distance = player.distanceToSqr(
                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
            if (distance < best) {
                best = distance;
                nearest = pos;
            }
        }
        return nearest;
    }
}
