package com.sanchous98.eldritchhorror.world.city;

import com.sanchous98.eldritchhorror.core.ModConfig;
import com.sanchous98.eldritchhorror.sanity.SanitySystem;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/**
 * The moment a city is lost: every player standing in the district takes a one-time sanity hit and
 * the chat tells them why. Generic decay and the reclaim rite handle the slow pressure; this is the
 * sharp event that makes a fall feel like a loss rather than a stat drift.
 */
public final class CityFallShock {

    private CityFallShock() {
    }

    /** Applies the shock to every player currently inside {@code city}'s district. */
    public static void apply(ServerLevel level, City city) {
        double shock = ModConfig.CITY_FALL_SANITY_SHOCK.get();
        if (shock <= 0.0) {
            return;
        }
        for (ServerPlayer player : level.players()) {
            City near = CityStates.nearestCity(level, player.blockPosition());
            if (near == null || !near.id().equals(city.id())) {
                continue;
            }
            SanitySystem.add(player, -shock);
            player.sendSystemMessage(
                    Component.translatable("city.fall.eldritch_horror", city.name()));
        }
    }
}
