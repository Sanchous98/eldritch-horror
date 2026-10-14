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
 * Bounded spawner for {@link AshenWisp}. Cthugha's lesser kin keeps to its country, so the gate is
 * <b>tainted ground only</b>, the same as the ashen-waste presences (Yig, Cthugha): an untainted
 * chunk has no watching flame. The rest is the shared {@link BestiarySupport#topUp} contract
 * (server-authoritative, overworld-only, loaded chunks only, deterministic, config-bounded).
 */
@EventBusSubscriber(modid = EldritchHorror.MODID)
public final class AshenWispSpawner {

    private AshenWispSpawner() {
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (!ModConfig.ENABLE_ASHEN_WISP_SPAWNS.get()) {
            return;
        }
        int interval = ModConfig.ASHEN_WISP_SPAWN_INTERVAL_TICKS.get();
        if (event.getServer().getTickCount() % interval != 0) {
            return;
        }
        ServerLevel level = event.getServer().getLevel(Level.OVERWORLD);
        if (level == null) {
            return;
        }
        int perPass = ModConfig.ASHEN_WISP_SPAWN_COUNT.get();
        if (perPass <= 0) {
            return;
        }
        ServerChunkCache cache = level.getChunkSource();
        int tick = event.getServer().getTickCount();
        int radius = ModConfig.ASHEN_WISP_SPAWN_RADIUS.get();
        int cap = ModConfig.ASHEN_WISP_SPAWN_CAP.get();
        for (ServerPlayer player : level.players()) {
            BestiarySupport.topUp(level, cache, player, ModEntities.ASHEN_WISP.get(),
                    AshenWisp.class, radius, cap, perPass, tick,
                    (lvl, spot, dark, tainted) -> tainted);
        }
    }
}
