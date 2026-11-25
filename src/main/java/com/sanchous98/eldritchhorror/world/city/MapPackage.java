package com.sanchous98.eldritchhorror.world.city;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sanchous98.eldritchhorror.EldritchHorror;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.Nullable;

/**
 * A pluggable map: which cities the world has, where they are, and the anchors the systems need.
 *
 * <p>The mod works without this file. When {@code eh-map.json} is present (server config folder
 * first, then the world folder) the mod's city list, station, harbour and landmark anchors come
 * from it — that is how a prebuilt 1:1 map (Arnis + {@code merge_worlds.py}) plugs in. Without it
 * the baked settlements table and the plan scans are used exactly as before.
 *
 * <p>Schema (see {@code docs/MAP-PACKAGE.md}):
 * <pre>
 * { "version": 1, "name": "...", "cities": [
 *     { "id": "paris", "name": "Paris", "x": 0, "z": 0, "radius": 5600,
 *       "station": {"dx": 0, "dz": -3100, "axis": "north_south"},
 *       "port":    {"dx": -600, "dz": 200, "water_y": 63},
 *       "landmarks": [{"name": "Eiffel Tower", "dx": -4226, "dz": -199}] } ] }
 * </pre>
 *
 * <p>Offsets are blocks from the city centre. Server-side; loaded once on start.
 */
public final class MapPackage {

    /** A station or harbour anchor, in blocks relative to the city centre. */
    public record Anchor(int dx, int dz, @Nullable Integer y, @Nullable Integer waterY,
                         String axis) {
    }

    /** A named place used by contracts and the locator bar. */
    public record Landmark(String name, int dx, int dz) {
    }

    /** One city of the package with its anchors. */
    public record Entry(City city, @Nullable Anchor station, @Nullable Anchor port,
                        List<Landmark> landmarks, List<Place> places,
                        List<Anchor> stations) {
    }

    /**
     * A point of interest the quest chains can lead to. The type decides what the objective asks
     * for: a market is a place to trade, an underground is a place to descend and clear, a vantage
     * is a roof to climb, a museum is a collection to inspect.
     */
    public record Place(String name, int dx, int dz, String type) {
    }

    /**
     * One scripted objective of a quest chain, straight from the package.
     *
     * @param type   {@code talk}, {@code board}, {@code travel}, {@code gather}, {@code hunt},
     *               {@code return}, {@code discover}; unknown types are skipped by the engine
     * @param id     stable step id, or {@code null} to derive one
     * @param dx     objective offset from the city centre, or {@code null} for the centre
     * @param dz     objective offset from the city centre, or {@code null} for the centre
     * @param place  human-readable place name for the dialogue and the waypoint
     * @param item   registry path for {@code gather}
     * @param entity registry path for {@code hunt}
     * @param count  how many items or kills, or {@code 1}
     */
    public record StepSpec(String type, @Nullable String id, @Nullable Integer dx,
                           @Nullable Integer dz, @Nullable String place, @Nullable String item,
                           @Nullable String entity, int count) {
    }

    /** One scripted chain: a city, a title and its steps. */
    public record QuestSpec(String id, String city, @Nullable String title, List<StepSpec> steps) {
    }

    private static final String FILE = "eh-map.json";

    private static volatile @Nullable List<Entry> entries;
    private static volatile @Nullable String name;
    private static volatile List<QuestSpec> quests = List.of();

    private MapPackage() {
    }

    /** @return true when a package is installed. */
    public static boolean present() {
        return entries != null;
    }

    /** @return the package name, or {@code null} when none is installed. */
    public static @Nullable String name() {
        return name;
    }

    /** @return every city of the package, in file order (empty without a package). */
    public static List<Entry> entries() {
        List<Entry> local = entries;
        return local == null ? List.of() : local;
    }

    /** @return the scripted chains of a city, in file order (empty when the pack has none). */
    public static List<QuestSpec> quests(String cityId) {
        List<QuestSpec> out = new ArrayList<>();
        for (QuestSpec quest : quests) {
            if (quest.city().equals(cityId)) {
                out.add(quest);
            }
        }
        return List.copyOf(out);
    }

    /** @return the package entry for a city id, or {@code null}. */
    public static @Nullable Entry entry(String cityId) {
        for (Entry entry : entries()) {
            if (entry.city().id().equals(cityId)) {
                return entry;
            }
        }
        return null;
    }

    /**
     * Loads the package from the server config folder (preferred) or the world folder.
     *
     * @param configDir server config directory
     * @param worldDir  world directory, or {@code null}
     * @return true when a package was installed
     */
    public static synchronized boolean load(Path configDir, @Nullable Path worldDir) {
        Path path = configDir.resolve(FILE);
        if (!Files.isReadable(path) && worldDir != null) {
            path = worldDir.resolve(FILE);
        }
        if (!Files.isReadable(path)) {
            EldritchHorror.LOGGER.info("map package: none at {}, using the baked city table",
                    path);
            entries = null;
            name = null;
            return false;
        }
        try {
            JsonObject root = JsonParser.parseString(Files.readString(path)).getAsJsonObject();
            List<Entry> parsed = new ArrayList<>();
            Map<String, Integer> radii = new LinkedHashMap<>();
            JsonArray cities = root.getAsJsonArray("cities");
            for (JsonElement element : cities) {
                JsonObject o = element.getAsJsonObject();
                String id = o.get("id").getAsString();
                String display = o.has("name") ? o.get("name").getAsString() : id;
                int x = o.get("x").getAsInt();
                int z = o.get("z").getAsInt();
                int radius = o.has("radius") ? o.get("radius").getAsInt() : 5600;
                int population = o.has("population") ? o.get("population").getAsInt() : 1;
                City city = new City(id, display, x, z, population);
                radii.put(id, radius);
                Anchor station = o.has("station") ? anchor(o.getAsJsonObject("station"), true)
                        : null;
                Anchor port = o.has("port") ? anchor(o.getAsJsonObject("port"), false) : null;
                List<Landmark> landmarks = new ArrayList<>();
                if (o.has("landmarks")) {
                    for (JsonElement mark : o.getAsJsonArray("landmarks")) {
                        JsonObject m = mark.getAsJsonObject();
                        landmarks.add(new Landmark(m.get("name").getAsString(),
                                m.get("dx").getAsInt(), m.get("dz").getAsInt()));
                    }
                }
                List<Place> places = new ArrayList<>();
                if (o.has("places")) {
                    for (JsonElement mark : o.getAsJsonArray("places")) {
                        JsonObject m = mark.getAsJsonObject();
                        places.add(new Place(m.get("name").getAsString(),
                                m.get("dx").getAsInt(), m.get("dz").getAsInt(),
                                m.has("type") ? m.get("type").getAsString() : "place"));
                    }
                }
                List<Anchor> stations = new ArrayList<>();
                if (o.has("stations")) {
                    for (JsonElement mark : o.getAsJsonArray("stations")) {
                        stations.add(anchor(mark.getAsJsonObject(), true));
                    }
                }
                if (stations.isEmpty() && station != null) {
                    stations.add(station);
                }
                parsed.add(new Entry(city, station, port, List.copyOf(landmarks),
                        List.copyOf(places), List.copyOf(stations)));
            }
            List<QuestSpec> parsedQuests = new ArrayList<>();
            if (root.has("quests")) {
                for (JsonElement element : root.getAsJsonArray("quests")) {
                    JsonObject q = element.getAsJsonObject();
                    List<StepSpec> steps = new ArrayList<>();
                    for (JsonElement stepElement : q.getAsJsonArray("steps")) {
                        JsonObject st = stepElement.getAsJsonObject();
                        steps.add(new StepSpec(
                                st.get("type").getAsString(),
                                st.has("id") ? st.get("id").getAsString() : null,
                                st.has("dx") ? st.get("dx").getAsInt() : null,
                                st.has("dz") ? st.get("dz").getAsInt() : null,
                                st.has("place") ? st.get("place").getAsString() : null,
                                st.has("item") ? st.get("item").getAsString() : null,
                                st.has("entity") ? st.get("entity").getAsString() : null,
                                st.has("count") ? st.get("count").getAsInt() : 1));
                    }
                    parsedQuests.add(new QuestSpec(
                            q.get("id").getAsString(),
                            q.get("city").getAsString(),
                            q.has("title") ? q.get("title").getAsString() : null,
                            List.copyOf(steps)));
                }
            }
            quests = List.copyOf(parsedQuests);
            City.setRadiusOverrides(radii);
            Cities.install(parsed.stream().map(Entry::city).toList());
            entries = List.copyOf(parsed);
            name = root.has("name") ? root.get("name").getAsString() : "unnamed";
            EldritchHorror.LOGGER.info("map package: {} with {} cities and {} quests", name,
                    parsed.size(), quests.size());
            return true;
        } catch (Exception error) {
            EldritchHorror.LOGGER.error("map package: failed to read {}", path, error);
            return false;
        }
    }

    /** Clears the package and restores the baked city table. Used on server stop and in tests. */
    public static synchronized void clear() {
        entries = null;
        name = null;
        quests = List.of();
        City.setRadiusOverrides(Map.of());
        Cities.install(null);
    }

    private static Anchor anchor(JsonObject o, boolean station) {
        int dx = o.get("dx").getAsInt();
        int dz = o.get("dz").getAsInt();
        Integer y = o.has("y") ? o.get("y").getAsInt() : null;
        Integer waterY = o.has("water_y") ? o.get("water_y").getAsInt() : null;
        String axis = o.has("axis") ? o.get("axis").getAsString()
                : (station ? "north_south" : "");
        return new Anchor(dx, dz, y, waterY, axis);
    }
}
