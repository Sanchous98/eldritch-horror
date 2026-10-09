package com.sanchous98.eldritchhorror.quest;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.Nullable;

/**
 * The seed quest journal (design/13). A small, data-driven list: each entry is a milestone the
 * existing systems already emit, so progress is real rather than scripted. A Java registry for now;
 * a datapack loader can populate it later without changing callers.
 */
public final class Quests {

    private static final List<Quest> ALL = List.of(
            new Quest("first_steps", 1),        // choose a starting city (Prologue.complete)
            new Quest("first_rite", 1),         // perform any rite
            new Quest("lorekeeper", 10),        // discover 10 codex entries
            new Quest("seal_the_way", 1),       // seal a rift
            new Quest("soothe_a_presence", 1)); // soothe a non-combat Ancient One

    private static final Map<String, Quest> BY_ID = index(ALL);

    private Quests() {
    }

    private static Map<String, Quest> index(List<Quest> quests) {
        Map<String, Quest> out = new LinkedHashMap<>();
        for (Quest quest : quests) {
            out.put(quest.id(), quest);
        }
        return Map.copyOf(out);
    }

    /** @return all quests, in presentation order. */
    public static List<Quest> all() {
        return ALL;
    }

    /** @return the quest for {@code id}, or {@code null} if unknown. */
    public static @Nullable Quest byId(@Nullable String id) {
        return id == null ? null : BY_ID.get(id);
    }
}
