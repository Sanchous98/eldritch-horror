package com.sanchous98.eldritchhorror.entity;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.core.ModConfig;
import com.sanchous98.eldritchhorror.cult.CultRank;
import com.sanchous98.eldritchhorror.cult.CultSystem;
import com.sanchous98.eldritchhorror.registry.ModEntities;
import com.sanchous98.eldritchhorror.world.loc.Location;
import com.sanchous98.eldritchhorror.world.loc.Locations;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.jspecify.annotations.Nullable;

/**
 * Bounded, deterministic, once-per-process trigger for {@link ChoirLeviathan}, the
 * {@code design/18-bosses.md} boss "summoned via the {@code drowned_blessing} chain" at a drowned
 * temple. It is {@link CthulhuSiteTrigger}'s exact shape with one extra gate: the waking player must
 * hold at least {@link CultRank#INITIATE} with the {@code drowned_choir} — the chain the
 * {@code drowned_blessing} rite opens — so an outsider standing on the temple wakes nothing. When
 * the presence answers, the Choir answers in chat.
 *
 * <p>Boundedness and determinism are the Cthulhu trigger's: the site list is the fixed
 * {@link Locations} registry, proximity is a cheap distance check, the centre chunk is never
 * force-loaded ({@code hasChunkAt}/{@code getChunkNow} refuse to generate), and the once-only guard
 * lives in memory keyed by the site id (a process lifetime; a restart re-arms it, as with Cthulhu).
 */
@EventBusSubscriber(modid = EldritchHorror.MODID)
public final class ChoirLeviathanTrigger {

    /** The one site that owns this boss. */
    private static final String SITE_ID = "eldritch_horror:site/drowned_temple";

    /** The drowned_blessing chain gate: at least INITIATE standing with the Drowned Choir. */
    private static final String GATE_CULT = "drowned_choir";

    /** Sites already spawned this process; the once-only guard. */
    private static final Map<String, Boolean> SPAWNED = new HashMap<>();

    private ChoirLeviathanTrigger() {
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (!ModConfig.ENABLE_BOSS_SITE_TRIGGERS.get() || !ModConfig.ENABLE_CHOIR_LEVIATHAN_TRIGGER.get()) {
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
            // The drowned_blessing chain: only the Choir's own may wake what the Choir keeps asleep.
            if (!CultSystem.rankOf(player, GATE_CULT).atLeast(CultRank.INITIATE)) {
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
                player.sendSystemMessage(Component.translatable("choir.eldritch_horror.answers"));
                return;
            }
        }
    }

    /** The registered site by id, or {@code null} if the registry has not been populated yet. */
    private static @Nullable Location site() {
        return Locations.all().stream().filter(candidate -> candidate.id().equals(SITE_ID))
                .findFirst().orElse(null);
    }

    /**
     * Wakes one Leviathan on the temple's loaded floor (the conservative
     * {@link BestiarySupport#surfaceSpot} scan; it never generates a chunk). Returns whether a boss
     * actually appeared, so the once-only guard is only set on a real spawn.
     */
    private static boolean spawn(ServerLevel level, int cx, int cz) {
        // Survive a restart: if a Leviathan from a previous session still stands at the site, do not
        // summon a second one. Loaded-only query, no generation.
        if (!level.hasChunkAt(cx, cz)) {
            return false;
        }
        BlockPos centre = new BlockPos(cx, level.getHeight(Heightmap.Types.OCEAN_FLOOR, cx, cz), cz);
        if (!level.getEntitiesOfClass(ChoirLeviathan.class,
                new AABB(centre).inflate(ModConfig.BOSS_SITE_TRIGGER_RADIUS.get())).isEmpty()) {
            return true; // already awake here; keep the guard set
        }
        BlockPos spot = BestiarySupport.surfaceSpot(level, cx, cz, true);
        if (spot == null) {
            return false;
        }
        ChoirLeviathan boss = ModEntities.CHOIR_LEVIATHAN.get().spawn(level, spot, EntitySpawnReason.EVENT);
        if (boss == null) {
            return false;
        }
        boss.setPersistenceRequired();
        EldritchHorror.LOGGER.info("The Choir Leviathan has woken at the drowned temple ({}, {})", cx, cz);
        return true;
    }
}
