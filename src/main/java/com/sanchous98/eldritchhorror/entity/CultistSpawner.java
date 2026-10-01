package com.sanchous98.eldritchhorror.entity;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.core.ModConfig;
import com.sanchous98.eldritchhorror.registry.ModEntities;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Bounded ambient spawner for the cult war-band ({@link CultRaider} and {@link CultZealot}).
 * Reuses the shared {@link BestiarySupport#topUp} loop; the per-role gate is <b>dark or tainted
 * ground within a configured radius of a registered {@code cult_stronghold}</b>, so the war-band
 * "attaches to the world" as site-adjacent population rather than roaming everywhere
 * ({@code design/25}: site-based spawns). Raiders are on by default; zealots are rarer and off by
 * default; the worshipper NPC's ambient pass is also off by default (it is a site population per
 * {@code design/25}, with eggs and a later site batch working without it). The binder and crone are
 * rite/cult-site content and have no ambient pass here.
 *
 * <p>Everything is config-interval/count/radius/cap bounded, loaded-chunks-only (the shared loop
 * never force-loads) and deterministic.
 */
@EventBusSubscriber(modid = EldritchHorror.MODID)
public final class CultistSpawner {

    private CultistSpawner() {
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        boolean raiders = ModConfig.ENABLE_CULT_RAIDER_SPAWNS.get();
        boolean zealots = ModConfig.ENABLE_CULT_ZEALOT_SPAWNS.get();
        boolean worshippers = ModConfig.ENABLE_WORSHIPPER_SPAWNS.get();
        if (!raiders && !zealots && !worshippers) {
            return;
        }
        int interval = ModConfig.CULT_SPAWN_INTERVAL_TICKS.get();
        if (interval <= 0 || event.getServer().getTickCount() % interval != 0) {
            return;
        }
        ServerLevel level = event.getServer().getLevel(Level.OVERWORLD);
        if (level == null) {
            return;
        }
        ServerChunkCache cache = level.getChunkSource();
        int tick = event.getServer().getTickCount();
        int siteRadius = ModConfig.CULT_SPAWN_SITE_RADIUS.get();
        for (ServerPlayer player : level.players()) {
            if (raiders) {
                BestiarySupport.topUp(level, cache, player,
                        ModEntities.CULT_RAIDER.get(), CultRaider.class,
                        ModConfig.CULT_RAIDER_SPAWN_RADIUS.get(),
                        ModConfig.CULT_RAIDER_SPAWN_CAP.get(),
                        ModConfig.CULT_RAIDER_SPAWN_COUNT.get(), tick,
                        (lvl, spot, dark, tainted) -> (dark || tainted) && CultistSupport.nearStronghold(spot, siteRadius));
            }
            if (zealots) {
                BestiarySupport.topUp(level, cache, player,
                        ModEntities.CULT_ZEALOT.get(), CultZealot.class,
                        ModConfig.CULT_ZEALOT_SPAWN_RADIUS.get(),
                        ModConfig.CULT_ZEALOT_SPAWN_CAP.get(),
                        ModConfig.CULT_ZEALOT_SPAWN_COUNT.get(), tick,
                        (lvl, spot, dark, tainted) -> (dark || tainted) && CultistSupport.nearStronghold(spot, siteRadius));
            }
            if (worshippers) {
                BestiarySupport.topUp(level, cache, player,
                        ModEntities.WORSHIPPER.get(), Worshipper.class,
                        ModConfig.WORSHIPPER_SPAWN_RADIUS.get(),
                        ModConfig.WORSHIPPER_SPAWN_CAP.get(),
                        ModConfig.WORSHIPPER_SPAWN_COUNT.get(), tick,
                        (lvl, spot, dark, tainted) -> CultistSupport.nearStronghold(spot, siteRadius));
            }
        }
    }
}
