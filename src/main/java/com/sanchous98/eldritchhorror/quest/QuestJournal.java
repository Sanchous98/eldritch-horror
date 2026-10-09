package com.sanchous98.eldritchhorror.quest;

import com.sanchous98.eldritchhorror.registry.ModAttachments;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * Server-authoritative quest progress, layered over the synced {@link ModAttachments#QUESTS}
 * attachment (design/13). The value is an immutable snapshot, so writes are read-modify-write.
 *
 * <p>Progress is advanced by the systems a quest watches (a rite performed, a codex entry found, a
 * rift sealed, a presence soothed, a starting city taken); completion is announced in chat.
 */
public final class QuestJournal {

    private QuestJournal() {
    }

    /** @return the player's current progress toward {@code questId} ({@code 0} if untouched). */
    public static int progress(ServerPlayer player, String questId) {
        return player.getData(ModAttachments.QUESTS).getOrDefault(questId, 0);
    }

    /** @return whether {@code player} has finished {@code questId}. */
    public static boolean complete(ServerPlayer player, String questId) {
        Quest quest = Quests.byId(questId);
        return quest != null && progress(player, questId) >= quest.target();
    }

    /**
     * Advances {@code questId} by {@code delta}, capped at its target, and announces the completion
     * once. Unknown ids are ignored; an already-complete quest is left alone.
     *
     * @return whether this call finished the quest
     */
    public static boolean add(ServerPlayer player, String questId, int delta) {
        Quest quest = Quests.byId(questId);
        if (quest == null || delta <= 0) {
            return false;
        }
        int current = progress(player, questId);
        if (current >= quest.target()) {
            return false; // already done
        }
        int next = Math.min(quest.target(), current + delta);
        Map<String, Integer> map = new HashMap<>(player.getData(ModAttachments.QUESTS));
        map.put(questId, next);
        player.setData(ModAttachments.QUESTS, Map.copyOf(map));
        if (next >= quest.target()) {
            player.sendSystemMessage(Component.translatable(
                    "quest.eldritch_horror.complete", quest.name()));
            // Finishing a quest earns a skill point.
            com.sanchous98.eldritchhorror.skill.SkillTree.addPoints(player, 1);
            return true;
        }
        return false;
    }
}
