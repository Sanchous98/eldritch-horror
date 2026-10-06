package com.sanchous98.eldritchhorror.world.city;

import com.sanchous98.eldritchhorror.core.ModConfig;
import net.minecraft.network.chat.Component;

/**
 * A curated city's condition, driven by the corruption of its district (see
 * {@code design/21-settlements.md}). The state is world state derived from the taint field — it is
 * not stored per player — and it changes how much shelter a city gives the mind.
 *
 * <p>Ordered from healthy to lost; {@link #recoveryMultiplier()} scales the city sanity-recovery
 * source, so a Fallen city is no refuge at all.
 */
public enum CityState {

    /** Low corruption: full shelter, calm. */
    THRIVING("thriving", 1.0),
    /** Rising corruption: rumours, the shelter weakens. */
    UNEASY("uneasy", 0.6),
    /** High corruption: monsters press in, barely a refuge. */
    BESIEGED("besieged", 0.3),
    /** Claimed: abandoned/corrupted, no shelter. */
    FALLEN("fallen", 0.0);

    private final String id;
    private final double recoveryMultiplier;

    CityState(String id, double recoveryMultiplier) {
        this.id = id;
        this.recoveryMultiplier = recoveryMultiplier;
    }

    /** @return the multiplier on the city sanity-recovery source ({@code 0.0} = no shelter). */
    public double recoveryMultiplier() {
        return this.recoveryMultiplier;
    }

    /** @return the localised display name ({@code city.state.eldritch_horror.<id>}). */
    public Component displayName() {
        return Component.translatable("city.state.eldritch_horror." + this.id);
    }

    /** @return the state for a district-average taint in {@code [0, 1]}, by the configured cut-offs. */
    public static CityState of(double averageTaint) {
        if (averageTaint >= ModConfig.CITY_STATE_FALLEN_TAINT.get()) {
            return FALLEN;
        }
        if (averageTaint >= ModConfig.CITY_STATE_BESIEGED_TAINT.get()) {
            return BESIEGED;
        }
        if (averageTaint >= ModConfig.CITY_STATE_UNEASY_TAINT.get()) {
            return UNEASY;
        }
        return THRIVING;
    }
}
