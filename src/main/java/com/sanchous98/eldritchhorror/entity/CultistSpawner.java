package com.sanchous98.eldritchhorror.entity;

/**
 * <b>Folded away.</b> The cult war-band ({@code cult_raider}, {@code cult_zealot} and the
 * worshipper NPC) is now part of the single {@link SitePopulationSpawner} pass, which owns every
 * {@code cult_stronghold} inhabitant. This class no longer subscribes to the server tick, so there
 * is exactly one writer for site populations and no double-spawning.
 *
 * <p>The old per-mob keys ({@code enableCultRaiderSpawns}, {@code enableCultZealotSpawns},
 * {@code enableWorshipperSpawns}, their radius/count/cap and {@code cultSpawnIntervalTicks} /
 * {@code cultSpawnSiteRadius}) remain in {@code ModConfig} but are no longer read; the site pass is
 * gated by {@code enableSitePopulations} and the {@code siteCult*} / {@code siteWorshipper*} keys.
 */
public final class CultistSpawner {

    private CultistSpawner() {
    }
}
