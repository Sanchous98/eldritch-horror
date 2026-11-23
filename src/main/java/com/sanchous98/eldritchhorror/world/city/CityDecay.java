package com.sanchous98.eldritchhorror.world.city;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.core.ModConfig;
import com.sanchous98.eldritchhorror.endgame.DoomClock;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Advances the {@link CityLedger} while the world is inhabited.
 *
 * <p>One step every {@value #INTERVAL_TICKS} ticks (5 s). With no player online nothing is
 * advanced, so a server left overnight does not wake up with every city fallen. The doom clock is
 * read once per step and accelerates decay (config {@code cityDecayDoomBoost}).
 */
@EventBusSubscriber(modid = EldritchHorror.MODID)
public final class CityDecay {

    static final int INTERVAL_TICKS = 100;

    private CityDecay() {
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        if (server.getTickCount() % INTERVAL_TICKS != 0) {
            return;
        }
        if (!ModConfig.ENABLE_CITY_DECAY.get()) {
            return;
        }
        if (server.getPlayerList().getPlayerCount() == 0) {
            return;
        }
        ServerLevel level = server.overworld();
        for (City city : CityLedger.of(level).advance(level, level.getGameTime(),
                DoomClock.of(level).doom())) {
            CityFallShock.apply(level, city);
        }
    }
}
