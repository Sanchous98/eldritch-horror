package com.sanchous98.eldritchhorror.world.loc.style;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import org.jspecify.annotations.Nullable;

/**
 * The single mapping from a curated city to its {@link CityStyle}. <b>Owned centrally — workers
 * never edit this file.</b> Each curated city has its own style file (24 cities, 24 styles), so
 * each worker owns exactly one file. A city whose style is absent falls back to
 * {@link FallbackStyle} (climate gothic), so a missing style never breaks generation.
 *
 * <p>See {@code docs/STRUCTURES-CONTRACT.md} § Cultural styles.
 */
public final class CityStyles {

    /** The gothic climate palette + spire landmark, used for any city without a real style. */
    private static final CityStyle FALLBACK = new FallbackStyle();

    private static final Map<String, Supplier<CityStyle>> BY_CITY = new HashMap<>();

    static {
        register("London", LondonStyle::new);
        register("Paris", ParisStyle::new);
        register("Rome", RomeStyle::new);
        register("Istanbul", IstanbulStyle::new);
        register("Cairo", CairoStyle::new);
        register("Lagos", LagosStyle::new);
        register("Nairobi", NairobiStyle::new);
        register("Cape Town", CapeTownStyle::new);
        register("Moscow", MoscowStyle::new);
        register("Delhi", DelhiStyle::new);
        register("Mumbai", MumbaiStyle::new);
        register("Shanghai", ShanghaiStyle::new);
        register("Beijing", BeijingStyle::new);
        register("Tokyo", TokyoStyle::new);
        register("Seoul", SeoulStyle::new);
        register("Bangkok", BangkokStyle::new);
        register("Jakarta", JakartaStyle::new);
        register("Sydney", SydneyStyle::new);
        register("New York", NewYorkStyle::new);
        register("Los Angeles", LosAngelesStyle::new);
        register("Mexico City", MexicoCityStyle::new);
        register("Rio de Janeiro", RioStyle::new);
        register("Buenos Aires", BuenosAiresStyle::new);
        register("Lima", LimaStyle::new);
    }

    private CityStyles() {
    }

    private static void register(String city, Supplier<CityStyle> style) {
        BY_CITY.put(city.toLowerCase(), style);
    }

    /** The style for a curated city, or the fallback gothic style if none is defined. */
    public static CityStyle forCity(@Nullable String cityName) {
        if (cityName == null) {
            return FALLBACK;
        }
        Supplier<CityStyle> s = BY_CITY.get(cityName.toLowerCase());
        return s == null ? FALLBACK : s.get();
    }

    /** Name-based lookup for the fallback, exposed for the central switch. */
    static CityStyle fallback() {
        return FALLBACK;
    }
}
