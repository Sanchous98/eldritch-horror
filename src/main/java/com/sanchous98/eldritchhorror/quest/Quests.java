package com.sanchous98.eldritchhorror.quest;

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.Nullable;

/**
 * The quest registry (design/13). Populated from the {@code quest} data directory by
 * {@link QuestReload} on every server data (re)load, so quests are <b>content, not code</b>: adding
 * one is a JSON file plus its two lang keys. Callers only ever read {@link #all()} /
 * {@link #byId(String)}, which is why the storage can be swapped atomically on reload.
 */
public final class Quests {

    private static volatile List<Quest> all = List.of();
    private static volatile Map<String, Quest> byId = Map.of();

    private Quests() {
    }

    /**
     * Replaces the registry from a data load. Ordered by stage then id, so the journal reads as acts;
     * the swap is atomic for readers on other threads.
     */
    public static void replaceAll(Collection<Quest> quests) {
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

    /** @return every quest, in journal order (stage, then id). */
    public static List<Quest> all() {
        return all;
    }

    /** @return the quest for {@code id}, or {@code null} when the loaded data set does not define it. */
    public static @Nullable Quest byId(@Nullable String id) {
        return id == null ? null : byId.get(id);
    }
}
