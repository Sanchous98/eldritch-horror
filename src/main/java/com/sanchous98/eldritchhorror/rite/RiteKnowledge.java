package com.sanchous98.eldritchhorror.rite;

import com.sanchous98.eldritchhorror.registry.ModAttachments;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashSet;
import java.util.Set;

/**
 * Server-authoritative access to a player's known rites, layered over the synced
 * {@link ModAttachments#RITE_KNOWLEDGE} attachment.
 *
 * <p>The attachment value is an immutable {@link Set#copyOf} snapshot, so adding is
 * read-modify-write: copy, add, store. Content never touches the attachment directly; it calls
 * this facade. Per {@code design/26-rituals-and-occult.md}, a rite must be <b>learned</b> (tome or
 * cult) before it can be performed.
 */
public final class RiteKnowledge {
    private RiteKnowledge() {
    }

    /** @return whether {@code player} knows {@code riteId}. */
    public static boolean knows(ServerPlayer player, String riteId) {
        return player.getData(ModAttachments.RITE_KNOWLEDGE).contains(riteId);
    }

    /**
     * Teaches {@code riteId} to {@code player}. No-op if already known.
     *
     * @return {@code true} if this call newly granted the rite
     */
    public static boolean add(ServerPlayer player, String riteId) {
        Set<String> known = player.getData(ModAttachments.RITE_KNOWLEDGE);
        if (known.contains(riteId)) {
            return false;
        }
        Set<String> next = new HashSet<>(known);
        next.add(riteId);
        player.setData(ModAttachments.RITE_KNOWLEDGE, Set.copyOf(next));
        return true;
    }

    /** @return an immutable snapshot of the rite ids {@code player} knows. */
    public static Set<String> known(ServerPlayer player) {
        return player.getData(ModAttachments.RITE_KNOWLEDGE);
    }

    /** @return the localised display name for a rite id. */
    public static Component name(String riteId) {
        return RiteDefinition.nameOf(riteId);
    }
}
