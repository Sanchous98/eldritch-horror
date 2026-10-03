package com.sanchous98.eldritchhorror.sanity;

import com.sanchous98.eldritchhorror.core.ModConfig;
import com.sanchous98.eldritchhorror.world.BoundaryTravel;
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
            register(new MorokSource());
            initialised = true;
        }
    }

    /**
     * Curated city footprints, cached once: {@code {x, z, radius²}}. Built lazily from
     * {@link Cities#all()} using each city's <b>generated</b> footprint
     * ({@link CityLocation#radius()}, ~220–400 blocks) rather than the legacy population-derived
     * {@link City#radius()} (10–44), so the recovery zone matches the district that actually
     * renders. Cached so the once-a-second per-player check allocates nothing.
     */
    private static volatile long[][] cityFootprints;

    private static long[][] cityFootprints() {
        long[][] cached = cityFootprints;
        if (cached != null) {
            return cached;
        }
        synchronized (SanitySources.class) {
            if (cityFootprints == null) {
                List<City> cities = Cities.all();
                long[][] out = new long[cities.size()][];
                for (int i = 0; i < cities.size(); i++) {
                    City city = cities.get(i);
                    long r = new CityLocation(city).radius();
                    out[i] = new long[] { city.x(), city.z(), r * r };
                }
                cityFootprints = out;
            }
            return cityFootprints;
        }
    }

    /**
     * Cheap squared-distance test against the cached curated city footprints. Overworld only.
     */
    static boolean nearCity(ServerPlayer player) {
        if (!player.level().dimension().equals(Level.OVERWORLD)) {
            return false;
        }
        double x = player.getX();
        double z = player.getZ();
        for (long[] city : cityFootprints()) {
            double dx = x - city[0];
            double dz = z - city[1];
            if (dx * dx + dz * dz <= city[2]) {
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

    /**
     * <b>Morok</b>: the lethal polar end of the charted world. Past {@code |z| =
     * BoundaryTravel.morokEdgeZ()} the drain grows linearly with depth, reaching {@code -2.0/s}
     * at full lethality ({@code morokLethalDepth} blocks past the edge). Overworld only.
     *
     * <p>{@code BoundaryTravel.morok} keeps its darkness/nausea/damage pressure; this source owns
     * only the sanity consequence, so the player is never drained twice for the same depth.
     */
    private static final class MorokSource implements SanitySource {
        /** Sanity points lost per second at full lethality (depth &ge; {@code MOROK_LETHAL_DEPTH}). */
        private static final double FULL_LETHAL_RATE = 2.0;

        @Override
        public String id() {
            return "morok";
        }

        @Override
        public double deltaPerSecond(ServerPlayer player, SanityContext ctx) {
            if (!ModConfig.ENABLE_SANITY_MOROK.get()) {
                return 0.0;
            }
            if (!ctx.level().dimension().equals(Level.OVERWORLD)) {
                return 0.0;
            }
            double depth = Math.abs(player.getZ()) - BoundaryTravel.morokEdgeZ();
            if (depth <= 0.0) {
                return 0.0;
            }
            double f = Math.min(1.0, depth / BoundaryTravel.morokLethalDepth()); // 0..1
            return -FULL_LETHAL_RATE * f;
        }
    }
}
