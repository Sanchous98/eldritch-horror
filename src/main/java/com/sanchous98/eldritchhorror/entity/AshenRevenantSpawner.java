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
 * Bounded spawner for {@link AshenRevenant}. Vanilla mobs are removed, so this pass is what makes
 * the burning dead appear. It mirrors {@link RisenHuskSpawner} exactly: server-authoritative,
 * overworld-only, loaded chunks only, deterministic sampling, config-bounded counts, and the same
 * <b>dark or tainted</b> context gate ({@code design/25}: the ashen waste and the dark).
 */
@EventBusSubscriber(modid = EldritchHorror.MODID)
public final class AshenRevenantSpawner {

    private AshenRevenantSpawner() {
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (!ModConfig.ENABLE_ASHEN_REVENANT_SPAWNS.get()) {
            return;
        }
        int interval = ModConfig.ASHEN_REVENANT_SPAWN_INTERVAL_TICKS.get();
        if (event.getServer().getTickCount() % interval != 0) {
            return;
        }
        ServerLevel level = event.getServer().getLevel(Level.OVERWORLD);
        if (level == null) {
            return;
        }
        int perPass = ModConfig.ASHEN_REVENANT_SPAWN_COUNT.get();
        if (perPass <= 0) {
            return;
        }
        ServerChunkCache cache = level.getChunkSource();
        int tick = event.getServer().getTickCount();
        int radius = ModConfig.ASHEN_REVENANT_SPAWN_RADIUS.get();
        int cap = ModConfig.ASHEN_REVENANT_SPAWN_CAP.get();
        for (ServerPlayer player : level.players()) {
            BestiarySupport.topUp(level, cache, player, ModEntities.ASHEN_REVENANT.get(),
                    AshenRevenant.class, radius, cap, perPass, tick,
                    (lvl, spot, dark, tainted) -> dark || tainted);
        }
    }
}
