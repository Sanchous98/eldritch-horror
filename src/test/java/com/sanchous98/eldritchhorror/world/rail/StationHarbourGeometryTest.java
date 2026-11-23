package com.sanchous98.eldritchhorror.world.rail;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sanchous98.eldritchhorror.world.EarthMap;
import com.sanchous98.eldritchhorror.world.EarthMaps;
import com.sanchous98.eldritchhorror.world.ElevationCurve;
import com.sanchous98.eldritchhorror.world.city.Cities;
import com.sanchous98.eldritchhorror.world.city.City;
import com.sanchous98.eldritchhorror.world.loc.Palette;
import com.sanchous98.eldritchhorror.world.loc.city.CityLocation;
import com.sanchous98.eldritchhorror.world.loc.city.plan.CityPlan;
import com.sanchous98.eldritchhorror.world.loc.city.plan.CityPlanProbe;
import com.sanchous98.eldritchhorror.world.loc.city.plan.CityPlans;
import com.sanchous98.eldritchhorror.world.loc.city.plan.CityPoi;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * The honest gate for the large-vehicle station/harbour pass.
 *
 * <p>For every authored city it records the plan headlessly and asserts the transport geometry the
 * train and the packet need: a STATION anchor with an explicit axis, an authored platform column
 * three blocks (give or take two) from the rail centreline with five clear blocks above it, and a
 * berth of at least 12x24 water four deep for every city with a PORT. It also checks the generated
 * worldgen platform itself (81 long, five wide, edge at three, canopy at rail+6, sign, lamp and
 * buffer).
 *
 * <p>All failures are collected so one run reports every city; the per-city numbers are written to
 * {@code build/station-harbour-report.txt}.
 */
class StationHarbourGeometryTest {

    private static final int GROUND = 80;
    private static final int RAIL_SEARCH = 32;
    private static final int BERTH_SEARCH = 128;
    private static final int MIN_PLATFORM_LENGTH = 64;
    private static final int MIN_PLATFORM_WIDTH = 5;
    private static final int MIN_BERTH_WIDTH = 12;
    private static final int MIN_BERTH_LENGTH = 24;
    private static final int MIN_BERTH_DEPTH = 4;

    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void everyAuthoredCityFitsTheLargeTrainAndShip() throws IOException {
        EarthMap map = EarthMaps.getOrNull();
        List<String> report = new ArrayList<>();
        List<String> failures = new ArrayList<>();
        report.add("city | station axis | rail | authored platform (run x width, clear) | "
                + "worldgen platform | berth L x W x D @ Y");
        for (CityPlan plan : CityPlans.all()) {
            String city = plan.cityId();
            // The layout gate runs headlessly at a synthetic centre, so it does not depend on the
            // baked map; a second, tightly bounded probe at the real city gets the real berth Y.
            CityPlanProbe probe = CityPlanProbe.record(plan, Palette.fromBiome(1, false),
                    0, 0, GROUND, null, null);
            City cityRecord = cityOf(city);
            CityPoi station = first(plan, CityPoi.Kind.STATION);
            if (station == null) {
                failures.add(city + ": no STATION POI");
                report.add(city + " | MISSING STATION");
                continue;
            }
            String data = station.data().toLowerCase(Locale.ROOT);
            boolean northSouth = data.contains("north_south") || data.contains("south_north");
            boolean eastWest = data.contains("east_west") || data.contains("west_east");
            if (!northSouth && !eastWest) {
                failures.add(city + ": STATION data has no north_south/east_west: "
                        + station.data());
            }
            int stationX = station.dx();
            int stationZ = station.dz();
            CityPlanProbe.Rail rail = probe.nearestRail(stationX, stationZ, RAIL_SEARCH);
            if (rail == null) {
                failures.add(city + ": no authored rail near the STATION anchor");
                report.add(city + " | " + (northSouth ? "north_south" : eastWest ? "east_west"
                        : "none") + " | NO RAIL");
                continue;
            }
            int[] authored = authoredPlatform(probe, rail, northSouth);
            if (authored[0] < 1) {
                failures.add(city + ": no platform column within 1..5 blocks of the rail");
            }
            if (authored[2] < MIN_PLATFORM_WIDTH) {
                failures.add(city + ": authored platform is only " + authored[2]
                        + " wide at the rail");
            }
            if (authored[3] < 1 || authored[3] > 5) {
                failures.add(city + ": authored platform edge is " + authored[3]
                        + " blocks from the rail (expected 1..5)");
            }

            List<RailStamp> stamps = RailLines.stationStamps(new RailStation(city, city, 0, 0,
                    GROUND, 5, 0, northSouth ? 0 : 1, northSouth ? 1 : 0, 1));
            int[] generated = generatedPlatform(stamps, northSouth);
            if (generated[0] < MIN_PLATFORM_LENGTH) {
                failures.add(city + ": worldgen platform is only " + generated[0] + " long");
            }
            if (generated[1] < MIN_PLATFORM_WIDTH) {
                failures.add(city + ": worldgen platform side is only " + generated[1] + " wide");
            }
            if (generated[2] != 3) {
                failures.add(city + ": worldgen platform edge is " + generated[2]
                        + " blocks from the rail, expected 3");
            }
            if (generated[3] < 5) {
                failures.add(city + ": worldgen platform has only " + generated[3]
                        + " clear blocks above it");
            }
            if (generated[4] != 6) {
                failures.add(city + ": worldgen canopy is at rail+" + generated[4]);
            }
            if (generated[5] < 1 || generated[6] < 1) {
                failures.add(city + ": worldgen platform has no sign or lamp");
            }
            if (generated[7] < 1) {
                failures.add(city + ": worldgen platform has no buffer stop");
            }

            String berth = "-";
            CityPoi port = first(plan, CityPoi.Kind.PORT);
            if (port != null) {
                CityPlanProbe.Berth water = probe.waterBerth(port.dx(), port.dz(), BERTH_SEARCH);
                int realY = realBerthY(plan, cityRecord, map, port);
                if (water == null) {
                    failures.add(city + ": PORT anchor has no authored water");
                    berth = "NO WATER";
                } else {
                    if (water.width() < MIN_BERTH_WIDTH) {
                        failures.add(city + ": berth is only " + water.width() + " wide");
                    }
                    if (water.length() < MIN_BERTH_LENGTH) {
                        failures.add(city + ": berth is only " + water.length() + " long");
                    }
                    if (water.depth() < MIN_BERTH_DEPTH) {
                        failures.add(city + ": berth is only " + water.depth() + " deep");
                    }
                    berth = water.length() + "x" + water.width() + "x" + water.depth()
                            + " @ Y=" + (realY < 0 ? water.y() : realY);
                }
            }
            report.add(city + " | " + (northSouth ? "north_south" : eastWest ? "east_west"
                    : "none") + " | (" + rail.x() + "," + rail.y() + "," + rail.z() + ")"
                    + " | " + authored[1] + "x" + authored[2] + ", edge " + authored[3]
                    + ", clear " + authored[0]
                    + " | " + generated[0] + "x" + generated[1]
                    + ", edge " + generated[2] + ", clear " + generated[3]
                    + ", canopy rail+" + generated[4]
                    + " | " + berth);
        }
        Files.createDirectories(Path.of("build"));
        Files.write(Path.of("build", "station-harbour-report.txt"), report);
        report.forEach(System.out::println);
        assertTrue(failures.isEmpty(), String.join("\n", failures));
    }

    /**
     * The authored berth's real surface Y: a bounded probe at the real city centre reproduces the
     * plan's gradable surface, so an inland basin (Mexico City) reports its own level rather than
     * the synthetic 80.
     */
    private static int realBerthY(CityPlan plan, City city, EarthMap map, CityPoi port) {
        if (map == null || city == null) {
            return -1;
        }
        int ground = CityLocation.authoredGround(map, city);
        int portX = city.x() + port.dx();
        int portZ = city.z() + port.dz();
        CityPlanProbe probe = CityPlanProbe.record(plan,
                Palette.fromBiome(map.koppenClass(city.x(), city.z()), true),
                city.x(), city.z(), ground, new MapTerrain(map),
                CityPlanProbe.Bounds.around(portX, portZ, BERTH_SEARCH));
        CityPlanProbe.Berth berth = probe.waterBerth(portX, portZ, BERTH_SEARCH);
        return berth == null ? -1 : berth.y();
    }

    private static City cityOf(String planId) {
        for (City city : Cities.all()) {
            if (city.name().toLowerCase(Locale.ROOT).replace(' ', '_').equals(planId)) {
                return city;
            }
        }
        return null;
    }

    /** The baked map, as the headless plan probe sees it. */
    private record MapTerrain(EarthMap map) implements CityPlanProbe.Terrain {

        @Override
        public int surfaceY(int x, int z) {
            return Math.clamp(ElevationCurve.toY(this.map.elevationMetres(x, z)), -60, 300);
        }

        @Override
        public boolean isLand(int x, int z) {
            return this.map.isLand(x, z);
        }
    }

    private static CityPoi first(CityPlan plan, CityPoi.Kind kind) {
        for (CityPoi poi : plan.pois()) {
            if (poi.kind() == kind) {
                return poi;
            }
        }
        return null;
    }

    /**
     * Measures the authored platform next to a rail.
     *
     * @return {clearance, run length, width, edge}: five when a usable platform exists, the
     *         longest consecutive run along the track, the widest contiguous row across it and the
     *         nearest platform column's distance from the rail
     */
    private static int[] authoredPlatform(CityPlanProbe probe, CityPlanProbe.Rail rail,
                                          boolean northSouth) {
        int dirX = northSouth ? 0 : 1;
        int dirZ = northSouth ? 1 : 0;
        int acrossX = -dirZ;
        int acrossZ = dirX;
        int bestRun = 0;
        int bestWidth = 0;
        int bestEdge = Integer.MAX_VALUE;
        for (int side = -1; side <= 1; side += 2) {
            int run = 0;
            for (int along = -24; along <= 24; along++) {
                int start = 0;
                int length = 0;
                boolean clear = false;
                for (int across = 1; across <= 8; across++) {
                    int x = rail.x() + dirX * along + acrossX * across * side;
                    int z = rail.z() + dirZ * along + acrossZ * across * side;
                    int top = platformTop(probe, x, rail.y(), z);
                    if (top < 0) {
                        if (start == 0) {
                            continue;
                        }
                        break;
                    }
                    if (start == 0) {
                        start = across;
                    }
                    length++;
                    if (!clear) {
                        clear = clearance(probe, x, rail.y(), z) >= 5;
                    }
                }
                if (length >= 5 && clear && start >= 1 && start <= 5) {
                    run++;
                    bestRun = Math.max(bestRun, run);
                    bestWidth = Math.max(bestWidth, length);
                    bestEdge = Math.min(bestEdge, start);
                } else {
                    run = 0;
                }
            }
        }
        return new int[]{bestRun == 0 ? 0 : 5, bestRun, bestWidth,
                bestEdge == Integer.MAX_VALUE ? 0 : bestEdge};
    }

    /** @return the platform top course within two blocks of the rail, or -1 for no floor. */
    private static int platformTop(CityPlanProbe probe, int x, int railY, int z) {
        for (int top = railY - 1; top <= railY + 1; top++) {
            if (probe.solid(x, top, z)) {
                return top;
            }
        }
        return -1;
    }

    /** @return clear blocks above the first platform top within two courses of the rail. */
    private static int clearance(CityPlanProbe probe, int x, int railY, int z) {
        for (int top = railY - 1; top <= railY + 1; top++) {
            if (!probe.solid(x, top, z)) {
                continue;
            }
            int clear = 0;
            while (clear < 5 && !probe.solid(x, top + 1 + clear, z)) {
                clear++;
            }
            if (clear == 5) {
                return clear;
            }
        }
        return 0;
    }

    /**
     * @return {length, side width, edge distance, clearance, canopy height, signs, lamps,
     *         buffers} of the generated stamps, in rail-relative coordinates
     */
    private static int[] generatedPlatform(List<RailStamp> stamps, boolean northSouth) {
        int minAlong = Integer.MAX_VALUE;
        int maxAlong = Integer.MIN_VALUE;
        int maxPositive = 0;
        int minNegative = 0;
        int edge = Integer.MAX_VALUE;
        int clearance = 0;
        int canopy = 0;
        int signs = 0;
        int lamps = 0;
        int buffers = 0;
        for (RailStamp stamp : stamps) {
            int along = northSouth ? stamp.z() : stamp.x();
            int across = northSouth ? stamp.x() : stamp.z();
            switch (stamp.kind()) {
                case STATION_FLOOR -> {
                    minAlong = Math.min(minAlong, along);
                    maxAlong = Math.max(maxAlong, along);
                    if (across >= 0) {
                        maxPositive = Math.max(maxPositive, across);
                    } else {
                        minNegative = Math.min(minNegative, across);
                    }
                    edge = Math.min(edge, Math.abs(across));
                }
                case STATION_CLEAR -> clearance = Math.max(clearance, stamp.y());
                case STATION_CANOPY -> canopy = Math.max(canopy, stamp.y());
                case STATION_SIGN -> signs++;
                case STATION_LANTERN -> lamps++;
                case STATION_BUFFER -> buffers++;
                default -> {
                }
            }
        }
        int length = minAlong == Integer.MAX_VALUE ? 0 : maxAlong - minAlong + 1;
        int width = edge == Integer.MAX_VALUE ? 0
                : Math.max(maxPositive - edge + 1, edge - minNegative + 1);
        return new int[]{length, width, edge == Integer.MAX_VALUE ? 0 : edge,
                Math.max(0, clearance - GROUND), Math.max(0, canopy - GROUND), signs, lamps,
                buffers};
    }
}
