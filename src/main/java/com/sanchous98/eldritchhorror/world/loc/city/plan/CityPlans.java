package com.sanchous98.eldritchhorror.world.loc.city.plan;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.jspecify.annotations.Nullable;

/**
 * The single registry of authored {@link CityPlan}s, keyed by lowercase city name. Owned
 * centrally: a city agent adds its plan file only, and the lead adds the registration line.
 *
 * <p>Registration is expected to finish during class initialisation (a static block), before any
 * worldgen thread reads the map; after that it is read-only in practice.
 */
public final class CityPlans {

    private static final Map<String, CityPlan> PLANS = new LinkedHashMap<>();

    static {
        // The lead adds one entry per authored city as it lands.
        register(new JakartaPlan());
        register(new ShanghaiPlan());
        register(new SeoulPlan());
        register(new BeijingPlan());
        register(new TokyoPlan());
        register(new BangkokPlan());
        register(new DelhiPlan());
        register(new NairobiPlan());
        register(new CairoPlan());
        register(new MoscowPlan());
        register(new ParisPlan());
        register(new RomePlan());
        register(new LagosPlan());
        register(new MumbaiPlan());
        register(new IstanbulPlan());
        register(new LondonPlan());
        register(new CapeTownPlan());
        register(new SydneyPlan());
        register(new NewYorkPlan());
        register(new LosAngelesPlan());
        register(new RioPlan());
        register(new BuenosAiresPlan());
        register(new LimaPlan());
        register(new MexicoCityPlan());
    }

    private CityPlans() {
    }

    /** Registers one plan under {@link CityPlan#cityId()}, lowercased. */
    public static void register(CityPlan plan) {
        PLANS.put(key(plan.cityId()), plan);
    }

    /** The plan for a city name (case-insensitive), or {@code null} if none is authored yet. */
    public static @Nullable CityPlan forCity(@Nullable String cityName) {
        return cityName == null ? null : PLANS.get(key(cityName));
    }

    /** Case-insensitive city key: underscores and spaces are equivalent. */
    private static String key(String name) {
        return name.toLowerCase(Locale.ROOT).replace('_', ' ').trim();
    }

    /** Snapshot of every registered plan, in registration order (tests / preview). */
    public static List<CityPlan> all() {
        return List.copyOf(PLANS.values());
    }
}
