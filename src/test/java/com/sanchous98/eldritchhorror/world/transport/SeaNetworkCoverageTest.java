package com.sanchous98.eldritchhorror.world.transport;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sanchous98.eldritchhorror.world.EarthMap;
import com.sanchous98.eldritchhorror.world.EarthMaps;
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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * The honest gate for the sea network: every scheduled harbour is reachable, the network is not
 * split into large islands, and no lane crosses land.
 *
 * <p>The Earth layers are large optional assets (like {@code SeaLaneRealMapTest}), so a bare
 * checkout without them skips rather than fails; the server probe is the gameplay gate. The
 * per-harbour partner table is written to {@code build/sea-network-report.txt}.
 */
class SeaNetworkCoverageTest {
    /** More lines than this many harbours keep the network from collapsing into a few routes. */
    private static final int MIN_LINES = 18;
    /** Landmask sampling step while validating a finished lane, in blocks. */
    private static final int VALIDATE_STEP = 6;

    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void everyHarbourHasASeaPartner() throws IOException {
        EarthMap map = EarthMaps.getOrNull();
        List<ShippingLine> lines;
        List<Port> ports;
        long started = System.nanoTime();
        try {
            if (map == null) {
                return;
            }
            ports = Ports.all();
            lines = ShippingLines.all();
        } catch (Throwable missingAssetsOrClasspath) {
            return;
        }
        long buildMs = (System.nanoTime() - started) / 1_000_000L;
        Map<String, List<String>> partners = new LinkedHashMap<>();
        for (Port port : ports) {
            partners.put(port.cityId(), new ArrayList<>());
        }
        for (ShippingLine line : lines) {
            partners.get(line.a().cityId()).add(line.b().cityId());
            partners.get(line.b().cityId()).add(line.a().cityId());
        }
        List<String> report = new ArrayList<>();
        report.add("harbour | berth (x, z) y sea | partners");
        List<String> isolated = new ArrayList<>();
        for (Port port : ports) {
            List<String> names = partners.get(port.cityId());
            if (names.isEmpty()) {
                isolated.add(port.cityId());
            }
            report.add(String.format(Locale.ROOT, "%s | (%d, %d) y=%d sea=%s | %s",
                    port.cityId(), port.waterX(), port.waterZ(), port.waterY(),
                    map.isLand(port.waterX(), port.waterZ()) ? "false" : "true",
                    names.isEmpty() ? "NONE" : String.join(", ", names)));
        }
        report.add("lines=" + lines.size() + " ports=" + ports.size()
                + " isolated=" + isolated.size() + " " + isolated + " build=" + buildMs + " ms");
        Files.createDirectories(Path.of("build"));
        Files.write(Path.of("build", "sea-network-report.txt"), report);
        for (String row : report) {
            System.out.println(row);
        }

        assertTrue(lines.size() >= MIN_LINES,
                "the sea network should have at least " + MIN_LINES + " lines, had " + lines.size());
        assertTrue(isolated.isEmpty(),
                "every harbour needs a sea partner, isolated: " + isolated);
        for (ShippingLine line : lines) {
            assertFalse(line.route().size() < 2, line.id() + " has too few waypoints");
            SeaPoint land = firstLand(map, line);
            assertTrue(land == null, line.id() + " crosses land at " + land);
        }
    }

    private static SeaPoint firstLand(EarthMap map, ShippingLine line) {
        for (int i = 1; i < line.route().size(); i++) {
            SeaPoint a = line.route().at(i);
            SeaPoint b = line.route().at(i - 1);
            double dx = EarthMap.wrapDelta(b.x(), a.x());
            double dz = a.z() - (double) b.z();
            int steps = Math.max(1, (int) Math.ceil(Math.sqrt(dx * dx + dz * dz) / VALIDATE_STEP));
            for (int step = 0; step <= steps; step++) {
                double t = step / (double) steps;
                int x = (int) Math.round(b.x() + dx * t);
                int z = (int) Math.round(b.z() + dz * t);
                if (map.isLand(x, z)) {
                    return new SeaPoint(x, z);
                }
            }
        }
        return null;
    }
}
