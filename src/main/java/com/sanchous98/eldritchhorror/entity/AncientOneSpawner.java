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
        if (!dunwichDue && !shubDue && !ithaquaDue && !yigDue) {
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
}
