package com.sanchous98.eldritchhorror.sanity;

import com.sanchous98.eldritchhorror.core.ModConfig;
import com.sanchous98.eldritchhorror.world.city.Cities;
import com.sanchous98.eldritchhorror.world.city.City;
import com.sanchous98.eldritchhorror.world.loc.city.CityLocation;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

/**
 * Central registry of {@link SanitySource}s, mirroring the {@code Locations} pattern: register
 * once, read from any thread, deterministic order.
 *
 * <p>The two demo sources below are the milestone's visible, opt-in sample of the framework
 * (see {@code design/27-systems-framework.md}, "Milestone scope"). Real sources —
 * night, ruins, rifts, witnessing — arrive with their content.
 */
public final class SanitySources {

    private static final List<SanitySource> SOURCES = new CopyOnWriteArrayList<>();
    private static volatile boolean initialised;

    private SanitySources() {
    }

    /** Registers a source. Registration order is iteration order. */
    public static void register(SanitySource source) {
        SOURCES.add(source);
    }

    /** Snapshot of all registered sources, in registration order. */
    public static List<SanitySource> all() {
        init();
        return List.copyOf(SOURCES);
    }

    /**
     * Registers the demo sources (idempotent). Called from {@link SanityTicker} so the system is
     * complete as soon as the first sanity tick runs.
     */
    static void init() {
        if (initialised) {
            return;
        }
        synchronized (SanitySources.class) {
            if (initialised) {
                return;
            }
            register(new DarknessSource());
            register(new CitySource());
            initialised = true;
        }
    }

    /**
     * Cheap squared-distance test against the curated city footprints: no chunk lookup, no
     * allocation. Uses each city's <b>generated</b> footprint ({@link CityLocation#radius()},
     * ~220–400 blocks), not the legacy population-derived {@link City#radius()} (10–44), so the
     * recovery zone matches the district that actually renders. Overworld only.
     */
    static boolean nearCity(ServerPlayer player) {
        if (!player.level().dimension().equals(Level.OVERWORLD)) {
            return false;
        }
        double x = player.getX();
        double z = player.getZ();
        for (City city : Cities.all()) {
            double r = new CityLocation(city).radius();
            double dx = x - city.x();
            double dz = z - city.z();
            if (dx * dx + dz * dz <= r * r) {
                return true;
            }
        }
        return false;
    }

    /**
     * Demo: <b>darkness</b>. Drains while the player stands somewhere dark — either the overworld
     * night, or a spot whose combined light is low (caves, unlit interiors). One gentle drain,
     * not two stacked ones.
     */
    private static final class DarknessSource implements SanitySource {
        @Override
        public String id() {
            return "darkness";
        }

        @Override
        public double deltaPerSecond(ServerPlayer player, SanityContext ctx) {
            if (!ModConfig.ENABLE_SANITY_DARKNESS.get()) {
                return 0.0;
            }
            // A settlement does not merely fail to drain you: its lights are a shelter from the
            // night. This must be checked first — at midnight `getMaxLocalRawBrightness` reads 0
            // everywhere (skyDarken subtracts the sky), so the low-light test below cannot tell
            // a lit city street from open wilderness and would otherwise drain inside the city.
            if (ctx.night() && ctx.nearCity()) {
                return 0.0;
            }
            boolean lowLight = ctx.level().getMaxLocalRawBrightness(player.blockPosition()) < 6;
            if (ctx.night() || lowLight) {
                return ModConfig.SANITY_DARKNESS_RATE.get();
            }
            return 0.0;
        }
    }

    /**
     * Demo: <b>city</b>. Recovers while the player is inside a curated city district. In the
     * overworld daylight the two demo sources are mutually exclusive; at night the darkness source
     * already stands down inside a city, so the two never fight.
     */
    private static final class CitySource implements SanitySource {
        @Override
        public String id() {
            return "city";
        }

        @Override
        public double deltaPerSecond(ServerPlayer player, SanityContext ctx) {
            if (!ModConfig.ENABLE_SANITY_CITY.get()) {
                return 0.0;
            }
            if (!ctx.level().dimension().equals(Level.OVERWORLD)) {
                return 0.0;
            }
            return ctx.nearCity() ? ModConfig.SANITY_CITY_RATE.get() : 0.0;
        }
    }
}
