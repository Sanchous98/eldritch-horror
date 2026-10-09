package com.sanchous98.eldritchhorror.event;

import com.sanchous98.eldritchhorror.registry.ModAttachments;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import org.jspecify.annotations.Nullable;

/**
 * Server-authoritative access to the rift markers a player has discovered, layered over the synced
 * {@link ModAttachments#KNOWN_RIFTS} attachment (design/22: rifts are markers discovered by
 * proximity or by opening one). The map reads it on the client.
 *
 * <p>Positions are encoded as {@code "x,y,z"} so the same string key round-trips through the
 * attachment's map codec. The value is an immutable snapshot, so writes are read-modify-write.
 */
public final class RiftKnowledge {

    private RiftKnowledge() {
    }

    /** @return the encoded key for {@code pos}. */
    public static String key(BlockPos pos) {
        return pos.getX() + "," + pos.getY() + "," + pos.getZ();
    }

    /** @return the position encoded by {@link #key}, or {@code null} when {@code key} is malformed. */
    public static @Nullable BlockPos parse(String key) {
        String[] parts = key.split(",");
        if (parts.length != 3) {
            return null;
        }
        try {
            return new BlockPos(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]),
                    Integer.parseInt(parts[2]));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** @return an immutable snapshot of the discovered rift keys. */
    public static Set<String> known(ServerPlayer player) {
        return player.getData(ModAttachments.KNOWN_RIFTS);
    }

    /** Records {@code pos} as a discovered rift. No-op if already known. */
    public static void discover(ServerPlayer player, BlockPos pos) {
        String key = key(pos);
        Set<String> known = player.getData(ModAttachments.KNOWN_RIFTS);
        if (known.contains(key)) {
            return;
        }
        Set<String> next = new HashSet<>(known);
        next.add(key);
        player.setData(ModAttachments.KNOWN_RIFTS, Set.copyOf(next));
    }

    /** Forgets {@code pos} (a rift that closed). No-op if not known. */
    public static void forget(ServerPlayer player, BlockPos pos) {
        String key = key(pos);
        Set<String> known = player.getData(ModAttachments.KNOWN_RIFTS);
        if (!known.contains(key)) {
            return;
        }
        Set<String> next = new HashSet<>(known);
        next.remove(key);
        player.setData(ModAttachments.KNOWN_RIFTS, Set.copyOf(next));
    }
}
