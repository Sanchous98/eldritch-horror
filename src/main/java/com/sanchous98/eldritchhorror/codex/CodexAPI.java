package com.sanchous98.eldritchhorror.codex;

import com.sanchous98.eldritchhorror.registry.ModAttachments;
import com.sanchous98.eldritchhorror.rite.RiteKnowledge;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * Server-authoritative access to a player's codex, layered over the synced
 * {@link ModAttachments#LORE} attachment, and the one place discovery writes knowledge.
 *
 * <p>The attachment value is an immutable {@link Set#copyOf} snapshot, so adding is
 * read-modify-write: copy, add, store — exactly like {@link RiteKnowledge}. Rite entries are the
 * one exception: they live in {@code RITE_KNOWLEDGE} and are <b>derived</b> on read, so learning a
 * rite is never duplicated and {@link #learn} of a rite delegates to {@link RiteKnowledge#add}.
 * Content never touches the attachment directly; it calls this facade.
 */
public final class CodexAPI {

    private CodexAPI() {
    }

    /** @return whether {@code player} knows the entry {@code id} (rites derived). */
    public static boolean knows(ServerPlayer player, String id) {
        if (player.getData(ModAttachments.LORE).contains(id)) {
            return true;
        }
        CodexEntry entry = CodexRegistry.byId(id);
        return entry != null && entry.category() == CodexCategory.RITE && RiteKnowledge.knows(player, id);
    }

    /**
     * Discovers entry {@code id} for {@code player}. Unknown ids are ignored; a rite entry is
     * taught through {@link RiteKnowledge} (the single rite store) rather than copied here.
     *
     * @return {@code true} if this call newly discovered the entry
     */
    public static boolean learn(ServerPlayer player, String id) {
        CodexEntry entry = CodexRegistry.byId(id);
        if (entry == null) {
            return false;
        }
        if (entry.category() == CodexCategory.RITE) {
            return RiteKnowledge.add(player, id);
        }
        Set<String> known = player.getData(ModAttachments.LORE);
        if (known.contains(id)) {
            return false;
        }
        Set<String> next = new HashSet<>(known);
        next.add(id);
        player.setData(ModAttachments.LORE, Set.copyOf(next));
        return true;
    }

    /**
     * @return an immutable snapshot of every entry id {@code player} has discovered, with known
     *         rites folded in. The union of the stored set and the derived rite ids.
     */
    public static Set<String> all(ServerPlayer player) {
        Set<String> out = new HashSet<>(player.getData(ModAttachments.LORE));
        for (String riteId : RiteKnowledge.known(player)) {
            if (CodexRegistry.byId(riteId) != null) {
                out.add(riteId);
            }
        }
        return Set.copyOf(out);
    }

    /** @return the display name for a codex entry id. */
    public static Component name(String id) {
        CodexEntry entry = CodexRegistry.byId(id);
        return entry == null ? Component.literal(id) : entry.name();
    }
}
