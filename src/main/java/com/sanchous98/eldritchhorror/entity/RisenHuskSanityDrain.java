package com.sanchous98.eldritchhorror.entity;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.core.ModConfig;
import com.sanchous98.eldritchhorror.sanity.SanityAPI;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * The risen husk's sanity hook: nearby husks drain a little sanity each second, capped so a horde
 * cannot stack without bound. This is the mob's whole point — the design is explicit that the
 * scariest things do little damage and drain sanity instead
 * ({@code design/25-bestiary-and-entities.md}, "Dread over damage").
 *
 * <p><b>Why a ticker and not a {@code SanitySource}:</b> the {@link com.sanchous98.eldritchhorror.sanity.SanitySource}
 * SPI is a pure function of the player and the cheap {@code SanityContext}, which carries no entity
 * view; counting husks means a world query, which that SPI deliberately avoids. So the drain runs
 * here, once per second, and writes through the public {@link SanityAPI} facade (the documented
 * entry point for content — never the attachment directly). It is overworld-only, server-only,
 * radius-bounded (default 8) and reads only already-loaded chunks ({@code AABB} + {@code
 * getEntitiesOfClass}, which never force-loads).
 *
 * <p>Note: like the source-based drains this runs every 20 ticks; unlike them it is not scaled by
 * {@code SANITY_DRAIN_MULTIPLIER} (it is content, not a source). The per-husk rate is config-driven.
 */
@EventBusSubscriber(modid = EldritchHorror.MODID)
public final class RisenHuskSanityDrain {

    /** Evaluate once per second, matching the sanity source pass. */
    private static final int INTERVAL_TICKS = 20;

    private RisenHuskSanityDrain() {
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (!ModConfig.ENABLE_SANITY.get() || !ModConfig.ENABLE_SANITY_RISEN_HUSK.get()) {
            return;
        }
        if (event.getServer().getTickCount() % INTERVAL_TICKS != 0) {
            return;
        }
        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            if (player.level().dimension().equals(Level.OVERWORLD)) {
                drain(player);
            }
        }
    }

    /** Drains {@code rate * min(count, max)} for one player, counted in loaded chunks only. */
    private static void drain(ServerPlayer player) {
        double radius = ModConfig.SANITY_RISEN_HUSK_RADIUS.get();
        int max = ModConfig.SANITY_RISEN_HUSK_MAX.get();
        AABB box = player.getBoundingBox().inflate(radius);
        List<RisenHusk> nearby =
                player.level().getEntitiesOfClass(RisenHusk.class, box, husk -> husk.isAlive());
        if (nearby.isEmpty()) {
            return;
        }
        int count = Math.min(nearby.size(), max);
        SanityAPI.add(player, ModConfig.SANITY_RISEN_HUSK_RATE.get() * count);
    }
}
