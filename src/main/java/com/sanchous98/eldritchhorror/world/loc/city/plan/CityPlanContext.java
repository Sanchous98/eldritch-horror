package com.sanchous98.eldritchhorror.world.loc.city.plan;

import com.sanchous98.eldritchhorror.world.loc.city.CitySurface;

/**
 * Everything a city plan needs to know about its site: the authored centre, the flat-heart ground
 * level, the district half-extent and the shared terrain-following {@link CitySurface}.
 *
 * @param cx       city centre X
 * @param cz       city centre Z
 * @param ground   flat-heart ground Y
 * @param district authored district half-extent (equal to {@link CityPlan#halfExtent()})
 * @param surface  the exact paved surface the shared pass lays down
 */
public record CityPlanContext(int cx, int cz, int ground, int district, CitySurface surface) {
}
