package com.sanchous98.eldritchhorror.entity;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.core.ModConfig;
import com.sanchous98.eldritchhorror.registry.ModEntities;
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
 * Bounded ambient spawner for the six ambience drifters (design/25 "Ambient role" table). One
 * shared pass, like {@link PassiveSpawner}: each mob supplies only its gate and config and the
 * shared {@link BestiarySupport#topUp}/{@link BestiarySupport#topUpAir} does the work, so there is
 * no per-mob scan loop. Gates are terrain/context-appropriate: dark for the cave flier, the tide
 * line for the aquatic drifters, wet ground for the marsh mote, and a cold biome for the frost
 * wisp. Loaded chunks only, deterministic, per-player capped.
 *
 * <p>The aquatic drifters use the shared <b>ground</b> {@code topUp} with the {@link
 * BestiarySupport#nearWater} gate, so they appear at the shore/tide line and then swim in (their
 * {@link WaterAmbientDrifter} goal); a dedicated submerged placer would be the only alternative and
 * is deliberately not added, to keep the shared framework frozen.
 */
@EventBusSubscriber(modid = EldritchHorror.MODID)
public final class AmbientSpawner {

    private AmbientSpawner() {
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        int interval = ModConfig.AMBIENT_SPAWN_INTERVAL_TICKS.get();
        if (interval <= 0 || event.getServer().getTickCount() % interval != 0) {
            return;
        }
        ServerLevel level = event.getServer().getLevel(Level.OVERWORLD);
        if (level == null) {
            return;
        }
        ServerChunkCache cache = level.getChunkSource();
        int tick = event.getServer().getTickCount();
        for (ServerPlayer player : level.players()) {
            if (ModConfig.ENABLE_CAVE_DRIFTER_SPAWNS.get()) {
                air(level, cache, player, ModEntities.CAVE_DRIFTER.get(), CaveDrifter.class,
                        ModConfig.CAVE_DRIFTER_SPAWN_CAP.get(), tick, (lvl, spot, dark) -> dark);
            }
            if (ModConfig.ENABLE_MARSH_MOTE_SPAWNS.get()) {
                air(level, cache, player, ModEntities.MARSH_MOTE.get(), MarshMote.class,
                        ModConfig.MARSH_MOTE_SPAWN_CAP.get(), tick,
                        (lvl, spot, dark) -> BestiarySupport.nearWater(lvl, spot));
            }
            if (ModConfig.ENABLE_FROST_WISP_SPAWNS.get()) {
                air(level, cache, player, ModEntities.FROST_WISP.get(), FrostWisp.class,
                        ModConfig.FROST_WISP_SPAWN_CAP.get(), tick,
                        (lvl, spot, dark) -> lvl.getBiome(spot).value().coldEnoughToSnow(spot, lvl.getSeaLevel()));
            }
            if (ModConfig.ENABLE_PALE_DRIFTER_SPAWNS.get()) {
                water(level, cache, player, ModEntities.PALE_DRIFTER.get(), PaleDrifter.class,
                        ModConfig.PALE_DRIFTER_SPAWN_CAP.get(), tick);
            }
            if (ModConfig.ENABLE_LANTERN_JELLY_SPAWNS.get()) {
                water(level, cache, player, ModEntities.LANTERN_JELLY.get(), LanternJelly.class,
                        ModConfig.LANTERN_JELLY_SPAWN_CAP.get(), tick);
            }
            if (ModConfig.ENABLE_DROWNED_MINNOW_SPAWNS.get()) {
                water(level, cache, player, ModEntities.DROWNED_MINNOW.get(), DrownedMinnow.class,
                        ModConfig.DROWNED_MINNOW_SPAWN_CAP.get(), tick);
            }
        }
    }

    private static <T extends Mob> void air(ServerLevel level, ServerChunkCache cache, ServerPlayer player,
                                            EntityType<T> type, Class<T> typeClass, int cap, int tick,
                                            BestiarySupport.AirGate gate) {
        BestiarySupport.topUpAir(level, cache, player, type, typeClass,
                ModConfig.AMBIENT_SPAWN_RADIUS.get(), cap, ModConfig.AMBIENT_SPAWN_COUNT.get(),
                level.getMinY() + 1, tick, gate);
    }

    private static <T extends Mob> void water(ServerLevel level, ServerChunkCache cache, ServerPlayer player,
                                              EntityType<T> type, Class<T> typeClass, int cap, int tick) {
        BestiarySupport.topUp(level, cache, player, type, typeClass,
                ModConfig.AMBIENT_SPAWN_RADIUS.get(), cap, ModConfig.AMBIENT_SPAWN_COUNT.get(), tick,
                (lvl, spot, dark, tainted) -> !tainted && BestiarySupport.nearWater(lvl, spot));
    }
}
