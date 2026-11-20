package com.sanchous98.eldritchhorror.world.town;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.core.ModConfig;
import com.sanchous98.eldritchhorror.world.EarthMap;
import com.sanchous98.eldritchhorror.world.EarthMaps;
import com.sanchous98.eldritchhorror.world.city.Cities;
import com.sanchous98.eldritchhorror.world.city.City;
import com.sanchous98.eldritchhorror.world.loc.Builder;
import com.sanchous98.eldritchhorror.world.loc.Palette;
import com.sanchous98.eldritchhorror.world.loc.city.CityLocation;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.jspecify.annotations.Nullable;

/**
 * Loads the <b>non-curated</b> settlements from {@code assets/eldritch_horror/map/settlements.json}
 * once and dispatches them as generic {@link TownLocation}s during chunk generation.
 *
 * <p>Curated cities are untouched ({@link Cities}); this class owns the other ~3,000 entries. It
 * filters two kinds of unsittable town at init (overlapping a curated district, or a centre that
 * is ocean), buckets the survivors in a {@link TownIndex}, and then costs a chunk only a bucket
 * lookup and at most one town build.
 *
 * <p>Server-side; lazily cached like {@link Cities}. The published snapshot is immutable.
 */
public final class Towns {

    /** Same baked file as the curated cities. */
    private static final String PATH = "assets/eldritch_horror/map/settlements.json";
    /** Extra cull margin, matching {@code Locations.MARGIN}. */
    private static final int MARGIN = 8;

    private static volatile @Nullable Snapshot cached;

    private Towns() {
    }

    /** Every buildable town (after the curated-overlap and water filters). */
    public static List<Town> all() {
        return snapshot().towns();
    }

    /** The sealed spatial index over {@link #all()}. */
    public static TownIndex index() {
        return snapshot().index();
    }

    private static Snapshot snapshot() {
        Snapshot local = cached;
        if (local == null) {
            synchronized (Towns.class) {
                local = cached;
                if (local == null) {
                    local = load();
                    cached = local;
                }
            }
        }
        return local;
    }

    private static Snapshot load() {
        List<Town> raw = parse();
        List<CityLocation> cityLocations = new ArrayList<>(Cities.all().size());
        for (City city : Cities.all()) {
            cityLocations.add(new CityLocation(city));
        }

        EarthMap map = EarthMaps.get();
        TownIndex index = new TownIndex();
        List<Town> accepted = new ArrayList<>(raw.size());
        int overlap = 0;
        int water = 0;
        for (Town town : raw) {
            int radius = TownSiting.radiusFor(town.population());
            if (overlapsCurated(cityLocations, town.x(), town.z(), radius)) {
                overlap++;
                continue;
            }
            if (!TownSiting.centreOnLand(town.x(), town.z(), map::isLand)) {
                water++;
                continue;
            }
            index.add(town);
            accepted.add(town);
        }
        index.seal();

        EldritchHorror.LOGGER.info(
                "loaded {} towns ({} buildable, {} skipped: {} overlapping curated cities, {} on water)",
                raw.size(), accepted.size(), overlap + water, overlap, water);
        return new Snapshot(List.copyOf(accepted), index);
    }

    /** True if the town circle would sit inside any curated city's district circle. */
    private static boolean overlapsCurated(List<CityLocation> cities, int x, int z, int radius) {
        for (CityLocation city : cities) {
            City c = city.city();
            if (TownSiting.intersects(x, z, radius, c.x(), c.z(), city.radius())) {
                return true;
            }
        }
        return false;
    }

    private static List<Town> parse() {
        List<Town> out = new ArrayList<>();
        try (InputStream in = Towns.class.getClassLoader().getResourceAsStream(PATH)) {
            if (in == null) {
                EldritchHorror.LOGGER.warn("settlements.json missing; no towns will generate");
                return List.of();
            }
            JsonObject root = JsonParser.parseReader(new InputStreamReader(in)).getAsJsonObject();
            for (JsonElement el : root.getAsJsonArray("settlements")) {
                JsonObject o = el.getAsJsonObject();
                if ("curated".equals(o.get("tier").getAsString())) {
                    continue;
                }
                out.add(new Town(
                        o.get("id").getAsString(),
                        o.get("name").getAsString(),
                        o.get("x").getAsInt(),
                        o.get("z").getAsInt(),
                        o.get("population").getAsInt()));
            }
        } catch (Exception e) {
            EldritchHorror.LOGGER.error("failed to read settlements.json for towns", e);
        }
        return out;
    }

    /**
     * Called from {@code applyBiomeDecoration} right after {@code Locations.place}. Looks only at
     * the buckets within {@link TownLocation#MAX_CULL_RADIUS} of this chunk and builds at most
     * <b>one</b> town, so a dense real-world cluster cannot stall chunk generation.
     */
    public static void place(WorldGenLevel level, ChunkAccess chunk) {
        if (!ModConfig.ENABLE_TOWNS.get()) {
            return;
        }
        ChunkPos cp = chunk.getPos();
        List<Town> candidates = index().forChunk(cp.x(), cp.z(), TownLocation.MAX_CULL_RADIUS + MARGIN);
        if (candidates.isEmpty()) {
            return;
        }
        int minX = cp.getMinBlockX();
        int maxX = minX + 15;
        int minZ = cp.getMinBlockZ();
        int maxZ = minZ + 15;
        EarthMap map = EarthMaps.get();

        Town best = null;
        for (Town town : candidates) {
            int cull = new TownLocation(town).radius() + MARGIN;
            if (town.x() + cull < minX || town.x() - cull > maxX
                    || town.z() + cull < minZ || town.z() - cull > maxZ) {
                continue;
            }
            // Deterministic pick when two towns reach one chunk (the cap is one build per chunk).
            if (best == null || town.id().compareTo(best.id()) < 0) {
                best = town;
            }
        }
        if (best == null) {
            return;
        }

        TownLocation location = new TownLocation(best);
        int koppen = map.koppenClass(best.x(), best.z());
        boolean coastal = isCoastal(map, best.x(), best.z(), location.radius());
        Palette palette = Palette.fromBiome(koppen, coastal);
        RandomSource rng = RandomSource.create(best.id().hashCode());
        location.build(new Builder(level, chunk, rng, palette, best.x(), best.z()));
    }

    /** A town counts as coastal if any of eight directions hits water just past its rim. */
    private static boolean isCoastal(EarthMap map, int cx, int cz, int radius) {
        int distance = radius + 8;
        for (int i = 0; i < 8; i++) {
            double a = i * Math.PI / 4;
            int x = cx + (int) Math.round(Math.cos(a) * distance);
            int z = cz + (int) Math.round(Math.sin(a) * distance);
            if (!map.isLand(x, z)) {
                return true;
            }
        }
        return false;
    }

    /** Immutable init snapshot: the accepted list and its sealed index. */
    private record Snapshot(List<Town> towns, TownIndex index) {
    }
}
