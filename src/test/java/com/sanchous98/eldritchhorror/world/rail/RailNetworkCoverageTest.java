package com.sanchous98.eldritchhorror.world.rail;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sanchous98.eldritchhorror.world.EarthMaps;
import com.sanchous98.eldritchhorror.world.city.Cities;
import com.sanchous98.eldritchhorror.world.city.City;
import com.sanchous98.eldritchhorror.world.city.MapPackage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * The honest gate for the 1:1 rail network: every curated city of the real map package has a
 * partner, the network has at least {@value #MIN_LINES} lines, and no city is isolated.
 *
 * <p>The package and layout are the real ones from {@code paris/worlds} (copied into the test
 * resources because the server package is not inside the mod jar). The test also checks that the
 * two files agree, so it cannot silently drift into a synthetic layout.
 *
 * <p>The Earth layers are large optional assets, so a checkout without them skips rather than
 * fails; the server probe is the gameplay gate. The per-city partner table is written to
 * {@code build/rail-network-report.txt}.
 */
class RailNetworkCoverageTest {
    /** More lines than this many cities keep the network from collapsing into a few routes. */
    private static final int MIN_LINES = 30;
    /** A headless network build must stay interactive (the old long-route search took seconds). */
    private static final long MAX_BUILD_MS = 10_000;

    private static final Path PACKAGE_DIR = Path.of("src", "test", "resources", "rail");

    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @AfterAll
    static void restore() {
        RailLines.clear();
        MapPackage.clear();
    }

    @Test
    void everyCuratedCityHasARailPartner() throws IOException {
        if (EarthMaps.getOrNull() == null) {
            return;
        }
        RailLines.clear();
        assertTrue(MapPackage.load(PACKAGE_DIR, null), "the real map package should load");
        List<City> cities = Cities.all();
        assertEquals(24, cities.size(), "the 1:1 layout has 24 curated cities");
        assertEquals(24, MapPackage.entries().size(), "the package and the city table should agree");
        assertRealLayout(cities);

        long started = System.nanoTime();
        List<RailLine> lines = RailLines.all();
        List<RailStation> stations = RailLines.stations();
        long buildMs = (System.nanoTime() - started) / 1_000_000L;

        Map<String, List<String>> partners = new LinkedHashMap<>();
        for (City city : cities) {
            partners.put(city.id(), new ArrayList<>());
        }
        for (RailLine line : lines) {
            partners.get(line.a().cityId()).add(line.b().cityId());
            partners.get(line.b().cityId()).add(line.a().cityId());
        }
        List<String> report = new ArrayList<>();
        report.add("city | z | partners (lines)");
        List<String> isolated = new ArrayList<>();
        for (City city : cities) {
            List<String> names = partners.get(city.id());
            if (names.isEmpty()) {
                isolated.add(city.id());
            }
            report.add(String.format(Locale.ROOT, "%s | %d | %s", city.id(), city.z(),
                    names.isEmpty() ? "NONE" : String.join(", ", names)));
        }
        report.add("lines=" + lines.size() + " stations=" + stations.size()
                + " isolated=" + isolated.size() + " " + isolated + " build=" + buildMs + " ms");
        for (RailLine line : lines) {
            report.add(String.format(Locale.ROOT, "line %s: %.0f blocks, %d nodes, corridor=%s",
                    line.id(), line.length(), line.size(), line.hasCorridor()));
        }
        Files.createDirectories(Path.of("build"));
        Files.write(Path.of("build", "rail-network-report.txt"), report);
        for (String row : report) {
            System.out.println(row);
        }

        assertTrue(lines.size() >= MIN_LINES,
                "the rail network should have at least " + MIN_LINES + " lines, had "
                        + lines.size());
        assertTrue(isolated.isEmpty(),
                "every curated city needs a rail partner, isolated: " + isolated);
        for (City city : cities) {
            assertFalse(RailLines.atCity(city.id()).isEmpty(), city.id() + " has no line");
        }
        for (RailLine line : lines) {
            assertTrue(line.length() > 0.0, line.id() + " has zero length");
            assertNotNull(line.a(), line.id() + " has no first station");
            assertTrue(line.nodes().size() >= 2, line.id() + " has too few nodes");
        }
        assertTrue(buildMs < MAX_BUILD_MS,
                "the network build should stay under " + MAX_BUILD_MS + " ms, took " + buildMs);
    }

    /** Asserts that the package contains the real 1:1 coordinates, not a miniature test layout. */
    private static void assertRealLayout(List<City> cities) throws IOException {
        JsonObject layout = JsonParser.parseString(
                Files.readString(PACKAGE_DIR.resolve("layout.json"))).getAsJsonObject();
        Map<String, City> byId = new LinkedHashMap<>();
        for (City city : cities) {
            byId.put(city.id(), city);
        }
        for (var element : layout.getAsJsonArray("cities")) {
            JsonObject row = element.getAsJsonObject();
            City city = byId.get(row.get("id").getAsString());
            assertNotNull(city, "layout city missing from the package");
            assertEquals(row.get("x").getAsInt(), city.x(),
                    city.id() + " X differs between layout.json and eh-map.json");
            assertEquals(row.get("z").getAsInt(), city.z(),
                    city.id() + " Z differs between layout.json and eh-map.json");
        }
        City sydney = byId.get("sydney");
        assertNotNull(sydney, "the real layout contains Sydney");
        assertTrue(sydney.z() < -180_000,
                "the real 1:1 layout reaches past the baked map (Sydney z=" + sydney.z() + ")");
    }
}
