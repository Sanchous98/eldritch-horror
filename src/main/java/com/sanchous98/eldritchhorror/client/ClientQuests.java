package com.sanchous98.eldritchhorror.client;

import com.sanchous98.eldritchhorror.quest.Quest;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.Nullable;

/**
 * The client's mirror of the datapack quest definitions, filled from {@code QuestSyncPayload} on
 * join and reload (design/13). The server registry ({@code quest/Quests}) is authoritative and the
 * client never loads the datapack, so this is the read-only copy the journal screen renders.
 *
 * <p>Ordered by stage then id, the same journal order the server keeps, so both sides list the acts
 * identically. The swap on sync is atomic for the render thread.
 */
public final class ClientQuests {

    private static volatile List<Quest> all = List.of();
    private static volatile Map<String, Quest> byId = Map.of();

    private ClientQuests() {
    }

    /** Replaces the mirror from a server sync, in journal order (stage, then id). */
    public static void replaceAll(List<Quest> quests) {
        List<Quest> ordered = quests.stream()
                .sorted(Comparator.comparingInt(Quest::stage).thenComparing(Quest::id))
                .toList();
        Map<String, Quest> index = new LinkedHashMap<>();
        for (Quest quest : ordered) {
            index.put(quest.id(), quest);
        }
        all = ordered;
        byId = Map.copyOf(index);
    }

    /** @return every loaded quest, in journal order; empty until the server syncs. */
    public static List<Quest> all() {
        return all;
    }

    /** @return the quest for {@code id}, or {@code null} when the server has not sent it. */
    public static @Nullable Quest byId(String id) {
        return byId.get(id);
    }
}
