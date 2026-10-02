package com.sanchous98.eldritchhorror.entity;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.core.ModConfig;
import com.sanchous98.eldritchhorror.registry.ModEntities;
import com.sanchous98.eldritchhorror.world.loc.Location;
import com.sanchous98.eldritchhorror.world.loc.Locations;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * The single <b>site-population</b> pass ({@code design/25-bestiary-and-entities.md}, "How spawns
 * attach to the world": the <i>site-based</i> mode). One shared ticker keeps the five fixed
 * second-echelon sites inhabited while a player is near them; each mob supplies only its site,
 * gate and config and the shared {@link BestiarySupport#topUp} loop does the work, so there is no
 * per-mob scan loop and <b>one writer</b> owns site populations.
 *
 * <p>Bounds are identical to the rest of the bestiary: server-authoritative, overworld-only,
 * loaded chunks only (the shared loop's {@code getChunkNow} never force-loads or generates), a
 * near-player throttle and per-site concurrent caps, a deterministic {@link
 * net.minecraft.util.RandomSource} seeded from player id + tick (never {@code Math.random}), and no
 * world scan beyond the fixed {@link Locations} registry plus a small radius. Every spawn is
 * clipped to the site's built footprint ({@link Location#renderRadius}) by the gate, so the
 * population cannot leak across the countryside.
 *
 * <p>This replaces the ad-hoc {@link CultistSpawner} (folded away: it duplicated the
 * {@code cult_stronghold} population here) and is where {@code order_vault}'s {@code stone_sentinel}
 * guards come from (their ambient pass stays off). The {@code drowned_temple} boss trigger
 * ({@link CthulhuSiteTrigger}) and the coastal/global tide spawns are separate and untouched.
 */
@EventBusSubscriber(modid = EldritchHorror.MODID)
public final class SitePopulationSpawner {

    private SitePopulationSpawner() {
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (!ModConfig.ENABLE_SITE_POPULATIONS.get()) {
            return;
        }
        int interval = ModConfig.SITE_POPULATION_INTERVAL_TICKS.get();
        if (interval <= 0 || event.getServer().getTickCount() % interval != 0) {
            return;
        }
        ServerLevel level = event.getServer().getLevel(Level.OVERWORLD);
        if (level == null) {
            return;
        }
        ServerChunkCache cache = level.getChunkSource();
        int tick = event.getServer().getTickCount();
        // One site may be near several players; populate each site once per pass, anchored on the
        // first player in range. The registry is fixed, so this is a bounded list, not a scan.
        Set<String> done = new HashSet<>();
        for (Location site : Locations.all()) {
            if (done.contains(site.id())) {
                continue;
            }
            int cx = Locations.xOf(site);
            int cz = Locations.zOf(site);
            int reach = site.renderRadius();
            // Anchor on the NEAREST in-range player, so a distant one cannot starve a closer player
            // (every site is populated at most once per pass).
            ServerPlayer anchor = null;
            double best = Double.MAX_VALUE;
            for (ServerPlayer player : level.players()) {
                if (player.isCreative() || player.isSpectator() || !near(player, cx, cz, reach)) {
                    continue;
                }
                double dx = player.blockPosition().getX() - (double) cx;
                double dz = player.blockPosition().getZ() - (double) cz;
                double dsq = dx * dx + dz * dz;
                if (dsq < best) {
                    best = dsq;
                    anchor = player;
                }
            }
            if (anchor != null) {
                done.add(site.id());
                populate(level, cache, anchor, site, cx, cz, reach, tick);
            }
        }
    }

    /** True if {@code player} is within the site's built footprint plus one spawn radius. */
    private static boolean near(ServerPlayer player, int cx, int cz, int reach) {
        int outer = reach + ModConfig.SITE_POPULATION_SPAWN_RADIUS.get();
        long dx = player.blockPosition().getX() - (long) cx;
        long dz = player.blockPosition().getZ() - (long) cz;
        return dx * dx + dz * dz <= (long) outer * outer;
    }

    /** Runs the one site's population table against {@code player}. */
    private static void populate(ServerLevel level, ServerChunkCache cache, ServerPlayer player,
                                 Location site, int cx, int cz, int reach, int tick) {
        int radius = ModConfig.SITE_POPULATION_SPAWN_RADIUS.get();
        switch (site.id()) {
            case "eldritch_horror:site/cult_stronghold" -> {
                // Cult war-band on dark/tainted ground; the mobs' own target selector gates on
                // reputation (CultRaider/CultZealot only aggro below the cult threshold), so this
                // does not bypass the existing cult standing logic.
                topUp(level, cache, player, ModEntities.CULT_ZEALOT.get(), CultZealot.class, radius,
                        ModConfig.SITE_CULT_ZEALOT_CAP.get(), ModConfig.SITE_CULT_ZEALOT_COUNT.get(),
                        tick, siteGate(cx, cz, reach, SitePopulationSpawner::darkOrTainted));
                topUp(level, cache, player, ModEntities.CULT_RAIDER.get(), CultRaider.class, radius,
                        ModConfig.SITE_CULT_RAIDER_CAP.get(), ModConfig.SITE_CULT_RAIDER_COUNT.get(),
                        tick, siteGate(cx, cz, reach, SitePopulationSpawner::darkOrTainted));
                // Worshippers are passive altar guards: any valid spot inside the stronghold.
                topUp(level, cache, player, ModEntities.WORSHIPPER.get(), Worshipper.class, radius,
                        ModConfig.SITE_WORSHIPPER_CAP.get(), ModConfig.SITE_WORSHIPPER_COUNT.get(),
                        tick, siteGate(cx, cz, reach, SitePopulationSpawner::anywhere));
                // Nyarlathotep wears a face you trust inside the stronghold (design/28): one
                // loaded-only presence, capped, zeroed when its toggle is off.
                topUp(level, cache, player, ModEntities.NYARLATHOTEP.get(), Nyarlathotep.class, radius,
                        ModConfig.SITE_NYARLATHOTEP_CAP.get(),
                        ModConfig.ENABLE_SITE_NYARLATHOTEP.get() ? ModConfig.SITE_NYARLATHOTEP_COUNT.get() : 0,
                        tick, siteGate(cx, cz, reach, SitePopulationSpawner::anywhere));
            }
            case "eldritch_horror:site/drowned_temple" -> {
                // The temple deck stands above the waterline, so a "water within 3 blocks" probe
                // would never see it; the site itself is the drowned context. Any valid dry spot
                // (the platform interior, open to the sky) inside the footprint is enough. The
                // Cthulhu boss has its own once-only trigger and is not duplicated here.
                topUp(level, cache, player, ModEntities.DROWNED_THRALL.get(), DrownedThrall.class,
                        radius, ModConfig.SITE_DROWNED_THRALL_CAP.get(),
                        ModConfig.SITE_DROWNED_THRALL_COUNT.get(), tick,
                        siteGate(cx, cz, reach, SitePopulationSpawner::anywhere));
            }
            case "eldritch_horror:site/order_vault" -> {
                // Order-built guards hold the vault; their ambient pass stays off.
                topUp(level, cache, player, ModEntities.STONE_SENTINEL.get(), StoneSentinel.class,
                        radius, ModConfig.SITE_STONE_SENTINEL_CAP.get(),
                        ModConfig.SITE_STONE_SENTINEL_COUNT.get(), tick,
                        siteGate(cx, cz, reach, (lvl, spot, dark, tainted) -> !tainted));
                // The gate on the observatory plateau (design/28): the order_vault is the existing
                // site that stands on that plateau, so Yog-Sothoth attaches to it. It is
                // stationary-ish by attributes, one loaded-only presence inside the footprint.
                topUp(level, cache, player, ModEntities.YOG_SOTHOTH.get(), YogSothoth.class, radius,
                        ModConfig.SITE_YOG_SOTHOTH_CAP.get(),
                        ModConfig.ENABLE_SITE_YOG_SOTHOTH.get() ? ModConfig.SITE_YOG_SOTHOTH_COUNT.get() : 0,
                        tick, siteGate(cx, cz, reach, SitePopulationSpawner::anywhere));
            }
            case "eldritch_horror:site/rift_scar" -> {
                // The scar is a place of tears: swarm and vermin, and rarely a star-spawn.
                topUp(level, cache, player, ModEntities.LESSER_SWARM.get(), LesserSwarm.class, radius,
                        ModConfig.SITE_LESSER_SWARM_CAP.get(), ModConfig.SITE_LESSER_SWARM_COUNT.get(),
                        tick, siteGate(cx, cz, reach, SitePopulationSpawner::darkOrTainted));
                topUp(level, cache, player, ModEntities.RIFT_MITE.get(), RiftMite.class, radius,
                        ModConfig.SITE_RIFT_MITE_CAP.get(), ModConfig.SITE_RIFT_MITE_COUNT.get(),
                        tick, siteGate(cx, cz, reach, SitePopulationSpawner::darkOrTainted));
                // The scar's two Ancient Ones (design/28): Azathoth at the centre (no melee) and
                // Atlach-Nacha weaving the tear wider. Both are one loaded-only presence per player;
                // each keeps its own count config but zeroes its budget when the toggle is off.
                topUp(level, cache, player, ModEntities.AZATHOTH.get(), Azathoth.class, radius,
                        ModConfig.SITE_AZATHOTH_CAP.get(),
                        ModConfig.ENABLE_SITE_AZATHOTH.get() ? ModConfig.SITE_AZATHOTH_COUNT.get() : 0,
                        tick, siteGate(cx, cz, reach, SitePopulationSpawner::anywhere));
                topUp(level, cache, player, ModEntities.ATLACH_NACHA.get(), AtlachNacha.class, radius,
                        ModConfig.SITE_ATLACH_NACHA_CAP.get(),
                        ModConfig.ENABLE_SITE_ATLACH_NACHA.get() ? ModConfig.SITE_ATLACH_NACHA_COUNT.get() : 0,
                        tick, siteGate(cx, cz, reach, SitePopulationSpawner::darkOrTainted));
                // The pass only samples multiples of the site interval, so express the star cadence
                // in whole passes: every round(starInterval / interval) passes, at least one.
                int interval = ModConfig.SITE_POPULATION_INTERVAL_TICKS.get();
                int starInterval = ModConfig.SITE_STAR_SPAWN_INTERVAL_TICKS.get();
                int starPasses = starInterval <= 0 || interval <= 0
                        ? 0 : Math.max(1, Math.round((float) starInterval / interval));
                if (starPasses > 0 && (tick / interval) % starPasses == 0) {
                    topUp(level, cache, player, ModEntities.STAR_SPAWN.get(), StarSpawn.class, radius,
                            ModConfig.SITE_STAR_SPAWN_CAP.get(), ModConfig.SITE_STAR_SPAWN_COUNT.get(),
                            tick, siteGate(cx, cz, reach, SitePopulationSpawner::darkOrTainted));
                }
            }
            // ritual_altar_site intentionally has no ambient population: the rite engine owns what
            // appears there (design/25: "what the rite calls").
            default -> {
            }
        }
    }

    /**
     * Wraps {@code inner} with the footprint clip: a spot is only valid inside the site's built
     * radius. This is the bound that keeps a site's inhabitants at the site.
     */
    private static BestiarySupport.SpawnGate siteGate(int cx, int cz, int reach,
                                                      BestiarySupport.SpawnGate inner) {
        long r2 = (long) reach * reach;
        return (level, spot, dark, tainted) -> {
            long dx = spot.getX() - (long) cx;
            long dz = spot.getZ() - (long) cz;
            return dx * dx + dz * dz <= r2 && inner.allows(level, spot, dark, tainted);
        };
    }

    private static boolean darkOrTainted(ServerLevel level, BlockPos spot, boolean dark, boolean tainted) {
        return dark || tainted;
    }

    private static boolean anywhere(ServerLevel level, BlockPos spot, boolean dark, boolean tainted) {
        return true;
    }

    private static <T extends Mob> void topUp(ServerLevel level, ServerChunkCache cache,
                                              ServerPlayer player, EntityType<T> type,
                                              Class<T> typeClass, int radius, int cap, int perPass,
                                              int tick, BestiarySupport.SpawnGate gate) {
        BestiarySupport.topUp(level, cache, player, type, typeClass, radius, cap, perPass, tick, gate);
    }
}
