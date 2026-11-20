package com.sanchous98.eldritchhorror.i18n;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

/**
 * Permanent parity gate for the shipped locales: the two files must have the same key set, no
 * duplicate keys, and the same {@code %s}/{@code %1$s}-style placeholders per key. Runs with
 * {@code ./gradlew test} on the plain JUnit classpath; no game is started.
 *
 * <p>The parser is a small strict JSON reader (object/array/string/scalar) kept dependency-free:
 * Gson is available to the production sources through Minecraft, but not on the test classpath.
 */
class LangParityTest {

    private static final String LANG_DIR = "assets/eldritch_horror/lang/";
    private static final String ENGLISH = "en_us.json";
    private static final String RUSSIAN = "ru_ru.json";
    private static final Pattern PLACEHOLDER = Pattern.compile("%(?:\\d+\\$)?[sdf]");

    @Test
    void englishAndRussianHaveTheSameKeys() throws IOException {
        Map<String, String> en = read(ENGLISH);
        Map<String, String> ru = read(RUSSIAN);

        Set<String> missingInRussian = new TreeSet<>(en.keySet());
        missingInRussian.removeAll(ru.keySet());
        Set<String> missingInEnglish = new TreeSet<>(ru.keySet());
        missingInEnglish.removeAll(en.keySet());

        assertTrue(missingInRussian.isEmpty(), "keys missing from " + RUSSIAN + ": " + missingInRussian);
        assertTrue(missingInEnglish.isEmpty(), "keys missing from " + ENGLISH + ": " + missingInEnglish);
    }

    @Test
    void noDuplicateKeys() throws IOException {
        assertEquals(List.of(), duplicates(ENGLISH), "duplicate keys in " + ENGLISH);
        assertEquals(List.of(), duplicates(RUSSIAN), "duplicate keys in " + RUSSIAN);
    }

    @Test
    void placeholdersMatchPerKey() throws IOException {
        Map<String, String> en = read(ENGLISH);
        Map<String, String> ru = read(RUSSIAN);

        for (Map.Entry<String, String> entry : en.entrySet()) {
            String key = entry.getKey();
            String translated = ru.get(key);
            if (translated == null) {
                continue; // reported by englishAndRussianHaveTheSameKeys
            }
            assertEquals(placeholders(entry.getValue()), placeholders(translated),
                    "placeholder mismatch for " + key + ": en=" + entry.getValue()
                            + " ru=" + translated);
        }
    }

    private static Map<String, String> read(String file) throws IOException {
        Object root = new Parser(readText(file)).parseDocument();
        if (!(root instanceof Map<?, ?> object)) {
            throw new IOException(LANG_DIR + file + " is not a JSON object");
        }
        Map<String, String> out = new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : object.entrySet()) {
            if (!(entry.getValue() instanceof String value)) {
                throw new IOException(LANG_DIR + file + ": value of " + entry.getKey() + " is not a string");
            }
            out.put((String) entry.getKey(), value);
        }
        return out;
    }

    private static List<String> duplicates(String file) throws IOException {
        return new Parser(readText(file)).duplicates();
    }

    private static String readText(String file) throws IOException {
        try (InputStream stream = LangParityTest.class.getClassLoader()
                .getResourceAsStream(LANG_DIR + file)) {
            if (stream == null) {
                throw new IOException("missing resource " + LANG_DIR + file);
            }
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static List<String> placeholders(String value) {
        List<String> out = new ArrayList<>();
        Matcher matcher = PLACEHOLDER.matcher(value);
        while (matcher.find()) {
            out.add(matcher.group());
        }
        Collections.sort(out);
        return out;
    }

    /** Minimal strict JSON parser: enough for the lang files, with per-object duplicate tracking. */
    private static final class Parser {
        private final String text;
        private final List<String> duplicates = new ArrayList<>();
        private int index;

        Parser(String text) {
            this.text = text;
        }

        List<String> duplicates() {
            return duplicates;
        }

        Object parseDocument() throws IOException {
            Object value = parseValue("");
            skipWhitespace();
            if (index != text.length()) {
                throw error("trailing content");
            }
            return value;
        }

        private Object parseValue(String path) throws IOException {
            skipWhitespace();
            if (index >= text.length()) {
                throw error("unexpected end of input");
            }
            char c = text.charAt(index);
            if (c == '{') {
                return parseObject(path);
            }
            if (c == '[') {
                return parseArray(path);
            }
            if (c == '"') {
                return parseString();
            }
            if (c == 't') {
                expect("true");
                return Boolean.TRUE;
            }
            if (c == 'f') {
                expect("false");
                return Boolean.FALSE;
            }
            if (c == 'n') {
                expect("null");
                return null;
            }
            return parseNumber();
        }

        private Map<String, Object> parseObject(String path) throws IOException {
            expect('{');
            Map<String, Object> out = new LinkedHashMap<>();
            skipWhitespace();
            if (peek() == '}') {
                index++;
                return out;
            }
            while (true) {
                skipWhitespace();
                String key = parseString();
                if (out.containsKey(key)) {
                    duplicates.add(path + "." + key);
                }
                skipWhitespace();
                expect(':');
                out.put(key, parseValue(path + "." + key));
                skipWhitespace();
                char next = peek();
                if (next == ',') {
                    index++;
                } else if (next == '}') {
                    index++;
                    return out;
                } else {
                    throw error("expected ',' or '}'");
                }
            }
        }

        private List<Object> parseArray(String path) throws IOException {
            expect('[');
            List<Object> out = new ArrayList<>();
            skipWhitespace();
            if (peek() == ']') {
                index++;
                return out;
            }
            int element = 0;
            while (true) {
                out.add(parseValue(path + "[" + element++ + "]"));
                skipWhitespace();
                char next = peek();
                if (next == ',') {
                    index++;
                } else if (next == ']') {
                    index++;
                    return out;
                } else {
                    throw error("expected ',' or ']'");
                }
            }
        }

        private String parseString() throws IOException {
            expect('"');
            StringBuilder out = new StringBuilder();
            while (index < text.length()) {
                char c = text.charAt(index++);
                if (c == '"') {
                    return out.toString();
                }
                if (c != '\\') {
                    out.append(c);
                    continue;
                }
                if (index >= text.length()) {
                    throw error("unfinished escape");
                }
                char escape = text.charAt(index++);
                switch (escape) {
                    case '"', '\\', '/' -> out.append(escape);
                    case 'b' -> out.append('\b');
                    case 'f' -> out.append('\f');
                    case 'n' -> out.append('\n');
                    case 'r' -> out.append('\r');
                    case 't' -> out.append('\t');
                    case 'u' -> {
                        if (index + 4 > text.length()) {
                            throw error("unfinished unicode escape");
                        }
                        out.append((char) Integer.parseInt(text.substring(index, index + 4), 16));
                        index += 4;
                    }
                    default -> throw error("unknown escape \\" + escape);
                }
            }
            throw error("unterminated string");
        }

        private Double parseNumber() throws IOException {
            int start = index;
            if (peek() == '-') {
                index++;
            }
            while (index < text.length() && "0123456789.eE+-".indexOf(text.charAt(index)) >= 0) {
                index++;
            }
            try {
                return Double.valueOf(text.substring(start, index));
            } catch (NumberFormatException e) {
                throw error("bad number '" + text.substring(start, index) + "'");
            }
        }

        private void expect(String literal) throws IOException {
            if (!text.startsWith(literal, index)) {
                throw error("expected '" + literal + "'");
            }
            index += literal.length();
        }

        private void expect(char c) throws IOException {
            if (peek() != c) {
                throw error("expected '" + c + "'");
            }
            index++;
        }

        private char peek() {
            return index < text.length() ? text.charAt(index) : '\0';
        }

        private void skipWhitespace() {
            while (index < text.length() && Character.isWhitespace(text.charAt(index))) {
                index++;
            }
        }

        private IOException error(String message) {
            return new IOException("JSON at char " + index + ": " + message);
        }
    }
}
