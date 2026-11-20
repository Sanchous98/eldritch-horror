package com.sanchous98.eldritchhorror.quest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

/**
 * Guards the investigator field-kit chains (design/14): every one of the twelve has exactly two
 * data-driven tasks, the second follows the first, and each pays its owner's kit from the single
 * {@code Investigators} table rather than duplicating item lists in JSON.
 *
 * <p>Reads the real datapack files, so a typo in an id or a missing chain fails the build instead of
 * leaving an investigator unable to earn a kit. A small field reader keeps the test on the plain
 * JUnit classpath (the game's codecs are not available to tests).
 */
class StarterQuestsTest {

    private static final Path QUESTS =
            Path.of("src/main/resources/data/eldritch_horror/quest");
    private static final Set<String> TRIGGERS =
            Set.of("pickup", "kill", "craft", "rite", "codex");
    private static final List<String> INVESTIGATORS = List.of(
            "eleanor_vance", "jack_corrigan", "tom_mallory", "cormac_blackwood",
            "sister_agatha", "marion_delacroix", "vera_nightingale", "nikolai_volkov",
            "dr_amos_hartley", "evelyn_ashcombe", "aldous_pemberton", "hazel_quinn");

    /** The handful of JSON fields this gate cares about. */
    private record Row(String id, String investigator, String trigger, String param,
                       String requires, int target, String rewardKit, boolean rewardSignature,
                       int stage) {
    }

    @Test
    void everyInvestigatorHasTwoOrderedFieldTasks() throws IOException {
        Map<String, List<Row>> byInvestigator = loadStarterQuests();
        for (String investigator : INVESTIGATORS) {
            List<Row> chain = byInvestigator.get(investigator);
            assertNotNull(chain, "field-kit chain for " + investigator);
            assertEquals(2, chain.size(), "field-kit chain for " + investigator);
            Row first = chain.get(0);
            Row second = chain.get(1);
            assertEquals("", first.requires(), first.id());
            assertEquals(first.id(), second.requires(), second.id());
            assertEquals(investigator, first.rewardKit(), first.id());
            assertTrue(second.rewardSignature(), second.id());
            assertTrue(first.target() > 0, first.id());
            assertTrue(second.target() > 0, second.id());
            for (Row row : chain) {
                assertTrue(TRIGGERS.contains(row.trigger()), row.id() + " trigger");
                if (!"codex".equals(row.trigger())) {
                    assertTrue(!row.param().isEmpty(), row.id() + " param");
                }
            }
        }
    }

    private static Map<String, List<Row>> loadStarterQuests() throws IOException {
        Map<String, List<Row>> result = new HashMap<>();
        try (var files = Files.list(QUESTS)) {
            for (Path file : files.filter(p -> p.toString().endsWith(".json")).toList()) {
                Row row = read(Files.readString(file), file.getFileName().toString());
                if (row.trigger().isEmpty() || row.investigator().isEmpty()) {
                    continue;
                }
                result.computeIfAbsent(row.investigator(), key -> new ArrayList<>()).add(row);
            }
        }
        result.values().forEach(chain -> chain.sort(
                java.util.Comparator.comparingInt(Row::stage)));
        return result;
    }

    private static Row read(String json, String file) {
        return new Row(
                string(json, "id", file),
                optional(json, "investigator", file),
                optional(json, "trigger", file),
                optional(json, "param", file),
                optional(json, "requires", file),
                integer(json, "target", file),
                optional(json, "reward_kit", file),
                bool(json, "reward_signature", file),
                optionalInt(json, "stage", file));
    }

    private static String string(String json, String key, String file) {
        Matcher matcher = Pattern.compile("\"" + key + "\"\\s*:\\s*\"([^\"]*)\"").matcher(json);
        assertTrue(matcher.find(), file + " has no " + key);
        return matcher.group(1);
    }

    private static String optional(String json, String key, String file) {
        Matcher matcher = Pattern.compile("\"" + key + "\"\\s*:\\s*\"([^\"]*)\"").matcher(json);
        return matcher.find() ? matcher.group(1) : "";
    }

    private static int integer(String json, String key, String file) {
        Matcher matcher = Pattern.compile("\"" + key + "\"\\s*:\\s*(-?\\d+)").matcher(json);
        assertTrue(matcher.find(), file + " has no " + key);
        return Integer.parseInt(matcher.group(1));
    }

    private static int optionalInt(String json, String key, String file) {
        Matcher matcher = Pattern.compile("\"" + key + "\"\\s*:\\s*(-?\\d+)").matcher(json);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : 0;
    }

    private static boolean bool(String json, String key, String file) {
        Matcher matcher = Pattern.compile("\"" + key + "\"\\s*:\\s*(true|false)").matcher(json);
        return matcher.find() && Boolean.parseBoolean(matcher.group(1));
    }
}
