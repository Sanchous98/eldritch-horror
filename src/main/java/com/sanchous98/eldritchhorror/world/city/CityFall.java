package com.sanchous98.eldritchhorror.world.city;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.core.ModConfig;
import com.sanchous98.eldritchhorror.cult.CultRank;
import com.sanchous98.eldritchhorror.entity.BestiarySupport;
import com.sanchous98.eldritchhorror.entity.Worshipper;
import com.sanchous98.eldritchhorror.registry.ModEntities;
import com.sanchous98.eldritchhorror.world.loc.city.CityLocation;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * A Fallen city's takeover: once a curated city's corruption reaches {@link CityState#FALLEN}, the
 * Hollow Choir moves in (design/21: "a city that Falls can be taken over by the Hollow Choir").
 *
 * <p>Bounded and idempotent: every {@value #INTERVAL_TICKS} ticks, for each city near a player that
 * is Fallen, it tops the district up towards {@value #CAP} hollow-choir worshippers, spawning one or
 * two per pass on loaded open ground near the centre. Nothing is force-loaded, and the cap stops it
 * from ever becoming a horde.
 */
@EventBusSubscriber(modid = EldritchHorror.MODID)
public final class CityFall {

    /** Cadence: every 10 seconds. */
    private static final int INTERVAL_TICKS = 200;
    /** Hollow-choir worshippers a Fallen city is topped up towards. */
    private static final int CAP = 6;
    /** Per-pass spawn cap. */
    private static final int PER_PASS = 2;
    /** Horizontal radius around the city centre in which take-over worshippers appear. */
    private static final int SPAWN_RADIUS = 24;

    private CityFall() {
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (!ModConfig.ENABLE_CITY_POPULATION.get()) {
            return; // city systems off: no takeover either
        }
        if (event.getServer().getTickCount() % INTERVAL_TICKS != 0) {
            return;
        }
        ServerLevel overworld = event.getServer().getLevel(Level.OVERWORLD);
        if (overworld == null) {
            return;
        }
        Set<City> done = new HashSet<>();
        for (ServerPlayer player : overworld.players()) {
            City city = CityStates.nearestCity(overworld, player.blockPosition());
            if (city == null || !done.add(city)) {
                continue;
            }
            if (CityStates.stateOf(overworld, city) == CityState.FALLEN) {
                takeover(overworld, city, event.getServer().getTickCount());
            }
        }
    }

    private static void takeover(ServerLevel level, City city, int tick) {
        int radius = new CityLocation(city).radius();
        AABB box = new AABB(city.x() - radius, level.getMinY(), city.z() - radius,
                city.x() + radius, level.getMaxY(), city.z() + radius);
        int present = level.getEntitiesOfClass(Worshipper.class, box,
                w -> "hollow_choir".equals(w.cultId())).size();
        if (present >= CAP) {
            return;
        }
        int budget = Math.min(PER_PASS, CAP - present);
        RandomSource random = RandomSource.create(city.x() * 31L + city.z() ^ tick);
        for (int i = 0; i < budget * 8 && budget > 0; i++) {
            int x = city.x() + random.nextInt(2 * SPAWN_RADIUS + 1) - SPAWN_RADIUS;
            int z = city.z() + random.nextInt(2 * SPAWN_RADIUS + 1) - SPAWN_RADIUS;
            BlockPos spot = BestiarySupport.surfaceSpot(level, x, z, false);
            if (spot == null) {
                continue;
            }
            Worshipper worshipper = ModEntities.WORSHIPPER.get().spawn(level, spot, EntitySpawnReason.EVENT);
            if (worshipper == null) {
                continue;
            }
            worshipper.setCult("hollow_choir", CultRank.INITIATE);
            budget--;
        }
    }
}
