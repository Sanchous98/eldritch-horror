package com.sanchous98.eldritchhorror.codex;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.core.ModConfig;
import com.sanchous98.eldritchhorror.entity.AncientOne;
import com.sanchous98.eldritchhorror.event.EventTicker;
import com.sanchous98.eldritchhorror.world.loc.Location;
import com.sanchous98.eldritchhorror.world.loc.Locations;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * The bounded, server-side codex discovery pass (design/22-map-and-knowledge.md): once a second it
 * reveals fixed sites/cities near the player, nearby Ancient Ones, and running world events; mobs
 * are revealed by a kill. It never scans the world: the site list is the fixed {@link Locations}
 * registry, and the boss query is a loaded-chunks-only {@code getEntitiesOfClass} box, exactly the
 * pattern the rest of the bestiary uses.
 *
 * <p>Cadence mirrors {@code SanityTicker} (once a second, overworld-only); the radius per trigger
 * comes from {@code ModConfig.CODEX_*_RADIUS}. Discovery sets are de-duplicated by the player's own
 * knowledge, so no random is used anywhere: deterministic.
 *
 * <p>Kills are the one event-driven trigger: {@link LivingDeathEvent} attributes a kill to the
 * killer, which is exact and cheaper than waiting for a proximity pass to catch a fleeing mob.
 */
@EventBusSubscriber(modid = EldritchHorror.MODID)
public final class CodexDiscovery {

    private CodexDiscovery() {
    }

    /** A kill by a player teaches that mob's entry without a proximity pass. */
    @SubscribeEvent
    public static void onKill(LivingDeathEvent event) {
        if (!ModConfig.ENABLE_CODEX.get()) {
            return;
        }
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) {
            return;
        }
        String id = entityId(event.getEntity());
        if (id != null) {
            CodexAPI.learn(player, id);
        }
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (!ModConfig.ENABLE_CODEX.get()) {
            return;
        }
        int interval = Math.max(1, ModConfig.CODEX_INTERVAL_TICKS.get());
        if (event.getServer().getTickCount() % interval != 0) {
            return;
        }
        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            if (player.isSpectator() || !player.isAlive()
                    || !player.level().dimension().equals(Level.OVERWORLD)) {
                continue;
            }
            discoverSites(player);
            discoverBosses(player);
            discoverEvents(player);
        }
    }

    /** Proximity over the fixed location registry: one cheap distance check per registered site. */
    private static void discoverSites(ServerPlayer player) {
        int radius = ModConfig.CODEX_SITE_RADIUS.get();
        long r2 = (long) radius * radius;
        int px = player.blockPosition().getX();
        int pz = player.blockPosition().getZ();
        for (Location site : Locations.all()) {
            long dx = px - (long) Locations.xOf(site);
            long dz = pz - (long) Locations.zOf(site);
            if (dx * dx + dz * dz <= r2) {
                CodexAPI.learn(player, site.id());
            }
        }
    }

    /**
     * Loaded-only nearby Ancient Ones: an encounter is seen, not usually killed, so bosses use a
     * proximity pass. The bestiary does not: a kill through {@link #onKill} is the cheaper and exact
     * trigger, so no per-tick mob scan runs for it.
     */
    private static void discoverBosses(ServerPlayer player) {
        AABB bossBox = player.getBoundingBox().inflate(ModConfig.CODEX_BOSS_RADIUS.get());
        List<LivingEntity> bosses = player.level().getEntitiesOfClass(
                LivingEntity.class, bossBox, e -> e.isAlive() && e instanceof AncientOne);
        for (LivingEntity boss : bosses) {
            String id = entityId(boss);
            if (id != null) {
                CodexAPI.learn(player, id);
            }
        }
    }

    /** Running events for this player are discovered once started (they are already per-player). */
    private static void discoverEvents(ServerPlayer player) {
        for (String id : EventTicker.activeIds(player)) {
            CodexAPI.learn(player, id);
        }
    }

    /**
     * The codex id for a living entity, or {@code null} when its type is not a codex entry. The
     * check goes through the registry, so a vanilla mob (no entry) is ignored and an Ancient One is
     * classified by the same path its type was registered under.
     */
    private static String entityId(Entity entity) {
        Identifier key = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        if (key == null) {
            return null;
        }
        return CodexRegistry.byId(key.getPath()) == null ? null : key.getPath();
    }
}
