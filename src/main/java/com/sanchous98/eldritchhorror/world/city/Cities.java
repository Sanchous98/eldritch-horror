package com.sanchous98.eldritchhorror.world.city;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sanchous98.eldritchhorror.EldritchHorror;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import org.jspecify.annotations.Nullable;

/**
 * Loads the curated cities from {@code assets/eldritch_horror/map/settlements.json} (baked by
 * {@code tools/bake_earth.py}). Only entries whose {@code tier} is {@code "curated"} are used.
 *
 * <p>Server-side; cached once.
 */
public final class Cities {
    private static final String PATH = "assets/eldritch_horror/map/settlements.json";
    private static volatile @Nullable List<City> cached;

    private Cities() {
    }

    public static List<City> all() {
        List<City> local = cached;
        if (local == null) {
            synchronized (Cities.class) {
                local = cached;
                if (local == null) {
                    local = load();
                    cached = local;
                }
            }
        }
        return local;
    }

    private static List<City> load() {
        List<City> out = new ArrayList<>();
        try (InputStream in = Cities.class.getClassLoader().getResourceAsStream(PATH)) {
            if (in == null) {
                EldritchHorror.LOGGER.warn("settlements.json missing; no cities will generate");
                return List.of();
            }
            JsonObject root = JsonParser.parseReader(new InputStreamReader(in)).getAsJsonObject();
            for (JsonElement el : root.getAsJsonArray("settlements")) {
                JsonObject o = el.getAsJsonObject();
                if (!"curated".equals(o.get("tier").getAsString())) {
                    continue;
                }
                out.add(new City(
                        o.get("id").getAsString(),
                        o.get("name").getAsString(),
                        o.get("x").getAsInt(),
                        o.get("z").getAsInt(),
                        o.get("population").getAsInt()));
            }
        } catch (Exception e) {
            EldritchHorror.LOGGER.error("failed to read settlements.json", e);
        }
        EldritchHorror.LOGGER.info("loaded {} curated cities", out.size());
        return List.copyOf(out);
    }
}
