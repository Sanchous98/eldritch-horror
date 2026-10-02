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
 * Bounded spawners for the <b>roaming</b> Ancient Ones (the site-bound ones — Cthulhu,
 * Azathoth, Yog-Sothoth, Atlach-Nacha — live in {@link CthulhuSiteTrigger} or the shared
 * {@link SitePopulationSpawner}). All are server-authoritative, overworld-only, deterministic (the
 * shared {@link BestiarySupport#topUp} seeds its {@code RandomSource} from player id + tick, never
 * {@code Math.random}) and only ever touch already-loaded chunks.
 *
 * <ul>
 *   <li><b>Dunwich Horror</b> — a mobile settlement/wild threat: appears in the dark <i>or</i> in
 *       tainted terrain, one per player at most.</li>
 *   <li><b>Shub-Niggurath</b> — a pure biome presence: tainted terrain only, one per player, and a
 *       much longer interval (it is a place more than an event).</li>
 *   <li><b>Ithaqua</b> — the walking wind: cold, untainted loaded ground only, one per player.</li>
 *   <li><b>Yig</b> — the ashen-waste presence: tainted loaded terrain only, one per player.</li>
 *   <li><b>Cthugha</b> — the watching flame of the same ashen waste: tainted loaded ground only.</li>
 *   <li><b>Glaaki</b> and the <b>Hydra</b> — the drowned marsh: water within a few blocks of loaded
 *       ground; both are one per player. The Hydra buds a head when struck (bounded escalation).</li>
 *   <li><b>Nyogtha</b> — deep loaded cave floor (below {@code nyogthaSpawnMaxY}); it is heard, not
 *       seen. Placed by {@link BestiarySupport#topUpUnderground}, never on the surface.</li>
 *   <li><b>Rhan-Tegoth</b> — the cold, untainted loaded ground of the polar edge (as Ithaqua's).</li>
 * </ul>
 */
@EventBusSubscriber(modid = EldritchHorror.MODID)
public final class AncientOneSpawner {

    private AncientOneSpawner() {
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        int tick = event.getServer().getTickCount();
        ServerLevel level = event.getServer().getLevel(Level.OVERWORLD);
        if (level == null) {
            return;
        }
        ServerChunkCache cache = level.getChunkSource();
        boolean dunwichDue = ModConfig.ENABLE_DUNWICH_HORROR_SPAWNS.get()
                && tick % ModConfig.DUNWICH_HORROR_SPAWN_INTERVAL_TICKS.get() == 0;
        boolean shubDue = ModConfig.ENABLE_SHUB_NIGGURATH_SPAWNS.get()
                && tick % ModConfig.SHUB_NIGGURATH_SPAWN_INTERVAL_TICKS.get() == 0;
        boolean ithaquaDue = ModConfig.ENABLE_ITHAQUA_SPAWNS.get()
                && tick % ModConfig.ITHAQUA_SPAWN_INTERVAL_TICKS.get() == 0;
        boolean yigDue = ModConfig.ENABLE_YIG_SPAWNS.get()
                && tick % ModConfig.YIG_SPAWN_INTERVAL_TICKS.get() == 0;
        boolean cthughaDue = ModConfig.ENABLE_CTHUGHA_SPAWNS.get()
                && tick % ModConfig.CTHUGHA_SPAWN_INTERVAL_TICKS.get() == 0;
        boolean glaakiDue = ModConfig.ENABLE_GLAAKI_SPAWNS.get()
                && tick % ModConfig.GLAAKI_SPAWN_INTERVAL_TICKS.get() == 0;
        boolean hydraDue = ModConfig.ENABLE_HYDRA_SPAWNS.get()
                && tick % ModConfig.HYDRA_SPAWN_INTERVAL_TICKS.get() == 0;
        boolean nyogthaDue = ModConfig.ENABLE_NYOGTHA_SPAWNS.get()
                && tick % ModConfig.NYOGTHA_SPAWN_INTERVAL_TICKS.get() == 0;
        boolean rhanDue = ModConfig.ENABLE_RHAN_TEGOTH_SPAWNS.get()
                && tick % ModConfig.RHAN_TEGOTH_SPAWN_INTERVAL_TICKS.get() == 0;
        if (!dunwichDue && !shubDue && !ithaquaDue && !yigDue && !cthughaDue && !glaakiDue
                && !hydraDue && !nyogthaDue && !rhanDue) {
            return;
        }
        for (ServerPlayer player : level.players()) {
            if (dunwichDue) {
                spawnDunwich(level, cache, player, tick);
            }
            if (shubDue) {
                spawnShub(level, cache, player, tick);
            }
            if (ithaquaDue) {
                spawnIthaqua(level, cache, player, tick);
            }
            if (yigDue) {
                spawnYig(level, cache, player, tick);
            }
            if (cthughaDue) {
                spawnCthugha(level, cache, player, tick);
            }
            if (glaakiDue) {
                spawnGlaaki(level, cache, player, tick);
            }
            if (hydraDue) {
                spawnHydra(level, cache, player, tick);
            }
            if (nyogthaDue) {
                spawnNyogtha(level, cache, player, tick);
            }
            if (rhanDue) {
                spawnRhanTegoth(level, cache, player, tick);
            }
        }
    }

    /** Dark or tainted ground; the horror stalks the wild edge of a settlement. */
    private static void spawnDunwich(ServerLevel level, ServerChunkCache cache, ServerPlayer player, int tick) {
        BestiarySupport.topUp(level, cache, player, ModEntities.DUNWICH_HORROR.get(), DunwichHorror.class,
                ModConfig.DUNWICH_HORROR_SPAWN_RADIUS.get(),
                ModConfig.DUNWICH_HORROR_SPAWN_CAP.get(),
                1, tick, (lvl, spot, dark, tainted) -> dark || tainted);
    }

    /** Tainted ground only: the woods must already be wrong for it to be there at all. */
    private static void spawnShub(ServerLevel level, ServerChunkCache cache, ServerPlayer player, int tick) {
        BestiarySupport.topUp(level, cache, player, ModEntities.SHUB_NIGGURATH.get(), ShubNiggurath.class,
                ModConfig.SHUB_NIGGURATH_SPAWN_RADIUS.get(),
                ModConfig.SHUB_NIGGURATH_SPAWN_CAP.get(),
                1, tick, (lvl, spot, dark, tainted) -> tainted);
    }

    /** Ithaqua walks the cold: it appears only on cold, loaded ground (the polar edge / highlands). */
    private static void spawnIthaqua(ServerLevel level, ServerChunkCache cache, ServerPlayer player, int tick) {
        BestiarySupport.topUp(level, cache, player, ModEntities.ITHAQUA.get(), Ithaqua.class,
                ModConfig.ITHAQUA_SPAWN_RADIUS.get(),
                ModConfig.ITHAQUA_SPAWN_CAP.get(),
                1, tick, (lvl, spot, dark, tainted) ->
                        !tainted && lvl.getBiome(spot).value().coldEnoughToSnow(spot, lvl.getSeaLevel()));
    }

    /** Yig is drawn to tainted ground: the ashen waste must already be wrong for it to be there. */
    private static void spawnYig(ServerLevel level, ServerChunkCache cache, ServerPlayer player, int tick) {
        BestiarySupport.topUp(level, cache, player, ModEntities.YIG.get(), Yig.class,
                ModConfig.YIG_SPAWN_RADIUS.get(),
                ModConfig.YIG_SPAWN_CAP.get(),
                1, tick, (lvl, spot, dark, tainted) -> tainted);
    }

    /** Cthugha watches the ashen waste: tainted loaded ground only, like Yig's country. */
    private static void spawnCthugha(ServerLevel level, ServerChunkCache cache, ServerPlayer player, int tick) {
        BestiarySupport.topUp(level, cache, player, ModEntities.CTHUGHA.get(), Cthugha.class,
                ModConfig.CTHUGHA_SPAWN_RADIUS.get(),
                ModConfig.CTHUGHA_SPAWN_CAP.get(),
                1, tick, (lvl, spot, dark, tainted) -> tainted);
    }

    /** Glaaki and the Hydra are the drowned_marsh: water within a few blocks of loaded ground. */
    private static void spawnGlaaki(ServerLevel level, ServerChunkCache cache, ServerPlayer player, int tick) {
        BestiarySupport.topUp(level, cache, player, ModEntities.GLAAKI.get(), Glaaki.class,
                ModConfig.GLAAKI_SPAWN_RADIUS.get(),
                ModConfig.GLAAKI_SPAWN_CAP.get(),
                1, tick, (lvl, spot, dark, tainted) -> BestiarySupport.nearWater(lvl, spot));
    }

    private static void spawnHydra(ServerLevel level, ServerChunkCache cache, ServerPlayer player, int tick) {
        BestiarySupport.topUp(level, cache, player, ModEntities.HYDRA.get(), Hydra.class,
                ModConfig.HYDRA_SPAWN_RADIUS.get(),
                ModConfig.HYDRA_SPAWN_CAP.get(),
                1, tick, (lvl, spot, dark, tainted) -> BestiarySupport.nearWater(lvl, spot));
    }

    /** Nyogtha is heard under the floor: deep, loaded cave floor below the configured depth. */
    private static void spawnNyogtha(ServerLevel level, ServerChunkCache cache, ServerPlayer player, int tick) {
        int maxY = ModConfig.NYOGTHA_SPAWN_MAX_Y.get();
        BestiarySupport.topUpUnderground(level, cache, player, ModEntities.NYOGTHA.get(), Nyogtha.class,
                ModConfig.NYOGTHA_SPAWN_RADIUS.get(),
                ModConfig.NYOGTHA_SPAWN_CAP.get(),
                1, maxY, tick, (lvl, spot, dark, tainted) -> dark);
    }

    /** Rhan-Tegoth is the idol at the cold edge: cold, untainted loaded ground, like Ithaqua's. */
    private static void spawnRhanTegoth(ServerLevel level, ServerChunkCache cache, ServerPlayer player, int tick) {
        BestiarySupport.topUp(level, cache, player, ModEntities.RHAN_TEGOTH.get(), RhanTegoth.class,
                ModConfig.RHAN_TEGOTH_SPAWN_RADIUS.get(),
                ModConfig.RHAN_TEGOTH_SPAWN_CAP.get(),
                1, tick, (lvl, spot, dark, tainted) ->
                        !tainted && lvl.getBiome(spot).value().coldEnoughToSnow(spot, lvl.getSeaLevel()));
    }
}
