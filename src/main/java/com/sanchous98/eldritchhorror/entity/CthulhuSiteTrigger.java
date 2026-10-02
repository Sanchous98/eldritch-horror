package com.sanchous98.eldritchhorror.entity;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.core.ModConfig;
import com.sanchous98.eldritchhorror.registry.ModEntities;
import com.sanchous98.eldritchhorror.world.loc.Location;
import com.sanchous98.eldritchhorror.world.loc.Locations;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Bounded, deterministic, once-per-site trigger for {@link Cthulhu}. Server-authoritative and
 * overworld-only; every two seconds it checks each player against the fixed {@code drowned_temple}
 * centre, and if one is within the configured radius <b>and the centre chunk is already loaded</b>
 * it spawns a single Cthulhu once.
 *
 * <p>Boundedness: the site list is the fixed {@link Locations} registry (no world scan), the
 * proximity test is a cheap distance check, and the chunk is never force-loaded
 * ({@code level.hasChunkAt} / {@code getChunkNow} both refuse to generate). Determinism: only one
 * site exists per id and it is chosen explicitly, so there is no positional randomness.
 *
 * <p>Once-only guard lives in memory, keyed by site id
 * {@code eldritch_horror:site/drowned_temple} — <b>no attachment or marker is invented</b>. It is a
 * server-lifetime process (a restart re-arms the trigger), which is the simplest correct behaviour:
 * a real "already awoken" world flag would need a saved-data type that does not exist yet.
 */
@EventBusSubscriber(modid = EldritchHorror.MODID)
public final class CthulhuSiteTrigger {

    /** The one site that owns this Ancient One. */
    private static final String SITE_ID = "eldritch_horror:site/drowned_temple";

    /** Sites already spawned this process; the once-only guard. */
    private static final Map<String, Boolean> SPAWNED = new HashMap<>();

    private CthulhuSiteTrigger() {
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (!ModConfig.ENABLE_BOSS_SITE_TRIGGERS.get()) {
            return;
        }
        if (event.getServer().getTickCount() % 40 != 0) {
            return;
        }
        if (SPAWNED.getOrDefault(SITE_ID, false)) {
            return;
        }
        ServerLevel level = event.getServer().getLevel(Level.OVERWORLD);
        if (level == null) {
            return;
        }
        Location site = site();
        if (site == null) {
            return;
        }
        int cx = Locations.xOf(site);
        int cz = Locations.zOf(site);
        int radius = ModConfig.BOSS_SITE_TRIGGER_RADIUS.get();
        int radiusSqr = radius * radius;
        for (ServerPlayer player : level.players()) {
            if (player.isCreative() || player.isSpectator()) {
                continue;
            }
            double dx = player.getX() - cx;
            double dz = player.getZ() - cz;
            if (dx * dx + dz * dz > radiusSqr) {
                continue;
            }
            // Only a player already standing in the loaded area can wake it: no chunk generation.
            if (!level.hasChunkAt(cx, cz)) {
                continue;
            }
            if (spawn(level, cx, cz)) {
                SPAWNED.put(SITE_ID, true);
                return;
            }
        }
    }

    /** The registered site by id, or {@code null} if the registry has not been populated yet. */
    private static Location site() {
        return Locations.all().stream().filter(candidate -> candidate.id().equals(SITE_ID))
                .findFirst().orElse(null);
    }

    /**
     * Spawns one Cthulhu on the temple's loaded floor (reusing the conservative
     * {@link BestiarySupport#surfaceSpot} scan; it never generates a chunk). Returns whether a boss
     * actually appeared, so the once-only guard is only set on a real spawn.
     */
    private static boolean spawn(ServerLevel level, int cx, int cz) {
        // Survive a restart: if a Cthulhu from a previous session still stands at the site, do not
        // summon a second one. Loaded-only query, no generation.
        if (!level.hasChunkAt(cx, cz)) {
            return false;
        }
        BlockPos centre = new BlockPos(cx, level.getHeight(Heightmap.Types.OCEAN_FLOOR, cx, cz), cz);
        if (!level.getEntitiesOfClass(Cthulhu.class,
                new AABB(centre).inflate(ModConfig.BOSS_SITE_TRIGGER_RADIUS.get())).isEmpty()) {
            return true; // already awake here; keep the guard set
        }
        BlockPos spot = BestiarySupport.surfaceSpot(level, cx, cz, true);
        if (spot == null) {
            return false;
        }
        Cthulhu boss = ModEntities.CTHULHU.get().spawn(level, spot, EntitySpawnReason.EVENT);
        if (boss == null) {
            return false;
        }
        boss.setPersistenceRequired();
        EldritchHorror.LOGGER.info("Cthulhu has woken at the drowned temple ({}, {})", cx, cz);
        return true;
    }
}
