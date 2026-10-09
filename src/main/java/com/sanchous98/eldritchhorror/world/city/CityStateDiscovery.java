package com.sanchous98.eldritchhorror.world.city;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.core.ModConfig;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.jspecify.annotations.Nullable;

/**
 * Records the state of the city each player is standing in, so their world map can colour it
 * (design/21, design/22). Runs every {@value #INTERVAL_TICKS} ticks, overworld only.
 *
 * <p>Cheap and bounded: only the nearest curated district is evaluated (loaded chunks only, never
 * force-loads), and the attachment is written only when the state actually changes.
 */
@EventBusSubscriber(modid = EldritchHorror.MODID)
public final class CityStateDiscovery {

    /** Sync cadence: every 5 seconds (city corruption moves slowly). */
    private static final int INTERVAL_TICKS = 100;

    private CityStateDiscovery() {
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (!ModConfig.ENABLE_SANITY_CITY.get()) {
            return; // the city systems are off; do not track state either
        }
        if (event.getServer().getTickCount() % INTERVAL_TICKS != 0) {
            return;
        }
        ServerLevel overworld = event.getServer().getLevel(Level.OVERWORLD);
        if (overworld == null) {
            return;
        }
        for (ServerPlayer player : overworld.players()) {
            @Nullable City city = CityStates.nearestCity(overworld, player.blockPosition());
            if (city != null) {
                CityStateKnowledge.record(player, city, CityStates.stateOf(overworld, city));
            }
        }
    }
}
