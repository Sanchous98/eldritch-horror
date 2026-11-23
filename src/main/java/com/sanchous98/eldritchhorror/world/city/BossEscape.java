package com.sanchous98.eldritchhorror.world.city;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.core.ModConfig;
import com.sanchous98.eldritchhorror.entity.AncientOne;
import com.sanchous98.eldritchhorror.sanity.SanityAPI;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Cowardice has a cost: a fight with an Ancient One that started inside a city cannot be walked
 * away from for free.
 *
 * <p>An encounter opens when a player and an Ancient One exchange damage while the player is inside
 * a curated city district. Leaving the district for {@value #ESCAPE_GRACE_TICKS} ticks while the
 * boss is still alive costs sanity once and closes the encounter; dying, killing the boss, or
 * logging out also closes it. A per-player cooldown keeps a re-engagement from being milked.
 *
 * <p>This exists so fleeing a losing city fight cannot be used to dodge the city-fall shock: the
 * guilt follows you out. It applies to any Ancient One fight inside a city (roaming or summoned);
 * there is deliberately no dedicated "city boss" in the canonical model.
 */
@EventBusSubscriber(modid = EldritchHorror.MODID)
public final class BossEscape {

    /** How long outside the district before it counts as abandoning the fight (5 s). */
    private static final int ESCAPE_GRACE_TICKS = 100;
    private static final int TICK_INTERVAL = 20;

    private static final class Encounter {
        final City city;
        final UUID boss;
        long outsideSince;

        Encounter(City city, UUID boss) {
            this.city = city;
            this.boss = boss;
        }
    }

    private static final Map<UUID, Encounter> ENCOUNTERS = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> LAST_PENALTY = new ConcurrentHashMap<>();

    private BossEscape() {
    }

    /** Opens or refreshes an encounter when the player and an Ancient One trade a blow in a city. */
    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (!ModConfig.ENABLE_BOSS_ESCAPE_PENALTY.get()) {
            return;
        }
        ServerPlayer player;
        AncientOne boss;
        if (event.getEntity() instanceof ServerPlayer victim
                && event.getSource().getEntity() instanceof AncientOne attacker) {
            player = victim;
            boss = attacker;
        } else if (event.getEntity() instanceof AncientOne victim
                && event.getSource().getEntity() instanceof ServerPlayer attacker) {
            player = attacker;
            boss = victim;
        } else {
            return;
        }
        if (ENCOUNTERS.containsKey(player.getUUID())) {
            return;
        }
        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }
        City city = CityStates.nearestCity(level, player.blockPosition());
        if (city == null) {
            // Fighting from outside the district is not a city defence; do not open an encounter.
            return;
        }
        long now = level.getGameTime();
        Long last = LAST_PENALTY.get(player.getUUID());
        if (last != null && now - last < ModConfig.BOSS_ESCAPE_COOLDOWN.get()) {
            return;
        }
        ENCOUNTERS.put(player.getUUID(), new Encounter(city, boss.getUUID()));
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        if (server.getTickCount() % TICK_INTERVAL != 0 || ENCOUNTERS.isEmpty()) {
            return;
        }
        long now = server.overworld().getGameTime();
        Iterator<Map.Entry<UUID, Encounter>> iterator = ENCOUNTERS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, Encounter> entry = iterator.next();
            ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
            if (player == null || !player.isAlive()) {
                iterator.remove();
                continue;
            }
            if (!(player.level() instanceof ServerLevel level)) {
                iterator.remove();
                continue;
            }
            if (!bossAlive(level, entry.getValue().boss)) {
                iterator.remove();
                continue;
            }
            City here = CityStates.nearestCity(level, player.blockPosition());
            boolean inside = here != null && here.id().equals(entry.getValue().city.id());
            if (inside) {
                entry.getValue().outsideSince = 0L;
                continue;
            }
            if (entry.getValue().outsideSince == 0L) {
                entry.getValue().outsideSince = now;
                continue;
            }
            if (now - entry.getValue().outsideSince < ESCAPE_GRACE_TICKS) {
                continue;
            }
            SanityAPI.add(player, -ModConfig.BOSS_ESCAPE_SANITY.get());
            player.sendSystemMessage(Component.translatable(
                    "boss.escape.eldritch_horror", entry.getValue().city.name()));
            LAST_PENALTY.put(player.getUUID(), now);
            iterator.remove();
        }
    }

    /** The boss died: nobody abandoned anything. */
    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof AncientOne boss)) {
            return;
        }
        ENCOUNTERS.values().removeIf(encounter -> encounter.boss.equals(boss.getUUID()));
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        UUID id = event.getEntity().getUUID();
        ENCOUNTERS.remove(id);
        LAST_PENALTY.remove(id);
    }

    /** @return whether the boss is still loaded and alive; an unloaded boss counts as alive. */
    private static boolean bossAlive(ServerLevel level, UUID id) {
        var entity = level.getEntity(id);
        return entity == null || entity.isAlive();
    }
}
