package com.sanchous98.eldritchhorror.world.loc;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.world.EarthMap;
import com.sanchous98.eldritchhorror.world.EarthMaps;
import com.sanchous98.eldritchhorror.world.city.Cities;
import com.sanchous98.eldritchhorror.world.city.City;
import com.sanchous98.eldritchhorror.world.loc.city.CityLocation;
import com.sanchous98.eldritchhorror.world.loc.style.CityStyle;
import com.sanchous98.eldritchhorror.world.loc.style.CityStyles;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkAccess;

/**
 * Registry and dispatch for fixed-coordinate locations. See {@code docs/STRUCTURES-CONTRACT.md}.
 *
 * <p>Populated exactly once (the 24 curated cities); the registration list is the only shared
 * state, and it is never mutated after init, so worldgen threads only read it.
 */
public final class Locations {

    /** How far past a location's radius a chunk may be and still be dispatched to it. */
    private static final int MARGIN = 8;

    private record Entry(Location loc, int x, int z) {
    }

    private static final List<Entry> ENTRIES = new CopyOnWriteArrayList<>();
    private static volatile boolean initialised;

    private Locations() {
    }

    /** Registers a fixed-coordinate location. */
    public static void register(Location loc, int x, int z) {
        ENTRIES.add(new Entry(loc, x, z));
    }

    /** One-time registration of the curated cities. */
    private static void init() {
        if (initialised) {
            return;
        }
        synchronized (Locations.class) {
            if (initialised) {
                return;
            }
            for (City city : Cities.all()) {
                register(new CityLocation(city), city.x(), city.z());
            }
            initialised = true;
            EldritchHorror.LOGGER.info("registered {} fixed locations", ENTRIES.size());
        }
    }

    /**
     * Called from {@code applyBiomeDecoration}; dispatches to every registered location whose
     * footprint reaches this chunk. Writes are clipped to the chunk by {@link Builder}.
     */
    public static void place(WorldGenLevel level, ChunkAccess chunk) {
        init();
        if (ENTRIES.isEmpty()) {
            return;
        }
        ChunkPos cp = chunk.getPos();
        int minX = cp.getMinBlockX();
        int minZ = cp.getMinBlockZ();
        int maxX = minX + 15;
        int maxZ = minZ + 15;

        EarthMap map = EarthMaps.get();
        for (Entry e : ENTRIES) {
            int cull = e.loc().radius() + MARGIN;
            if (e.x() + cull < minX || e.x() - cull > maxX
                    || e.z() + cull < minZ || e.z() - cull > maxZ) {
                continue;
            }
            RandomSource rng = RandomSource.create(e.loc().id().hashCode());
            boolean coastal = isCoastal(map, e.x(), e.z());
            int koppen = map.koppenClass(e.x(), e.z());
            Palette palette = Palette.fromBiome(koppen, coastal);
            Builder builder = new Builder(level, chunk, rng, palette, e.x(), e.z());
            e.loc().build(builder);
        }
    }

    /**
     * Builds the {@link Palette} for a location. Cultural styles may override the climate
     * palette (local colour); everything else keeps the climate gothic palette.
     */
    public static Palette paletteFor(Location loc, int koppenClass, boolean coastal) {
        CityStyle style = styleOf(loc);
        return style == null ? Palette.fromBiome(koppenClass, coastal)
                : style.palette(koppenClass, coastal);
    }

    /** The cultural style of a location, if it is a styled city; otherwise {@code null}. */
    private static CityStyle styleOf(Location loc) {
        return loc instanceof CityLocation city ? CityStyles.forCity(city.city().name()) : null;
    }

    /** A site counts as coastal if any of eight directions hits water within 48 blocks. */
    private static boolean isCoastal(EarthMap map, int cx, int cz) {
        for (int r = 16; r <= 48; r += 8) {
            for (int i = 0; i < 8; i++) {
                double a = i * Math.PI / 4;
                if (!map.isLand(cx + (int) (Math.cos(a) * r), cz + (int) (Math.sin(a) * r))) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Snapshot of registered locations (for tests / diagnostics). */
    public static List<Location> all() {
        init();
        List<Location> out = new ArrayList<>(ENTRIES.size());
        for (Entry e : ENTRIES) {
            out.add(e.loc());
        }
        return List.copyOf(out);
    }
}
