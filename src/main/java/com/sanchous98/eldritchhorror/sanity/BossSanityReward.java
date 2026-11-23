package com.sanchous98.eldritchhorror.sanity;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.core.ModConfig;
import com.sanchous98.eldritchhorror.cult.CultBetrayal;
import com.sanchous98.eldritchhorror.cult.CultSystem;
import com.sanchous98.eldritchhorror.entity.AncientOne;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
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
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Killing an Ancient One steadies the mind.
 *
 * <p>Rewards are tiered: players who actually damaged the boss (within
 * {@code bossContributionWindow}) get the full sanity reward, everyone else online gets a small
 * share, and <b>cultists</b> (any cult at {@code INITIATE} or above) get nothing — the death of a
 * presence is not their relief. This is the counterweight to the madness episodes and to the
 * boss-escape penalty: a hard fight is worth taking.
 */
@EventBusSubscriber(modid = EldritchHorror.MODID)
public final class BossSanityReward {

    private static final int PRUNE_INTERVAL = 100;

    /** boss UUID → (player UUID → last damage tick). */
    private static final Map<UUID, Map<UUID, Long>> HITS = new ConcurrentHashMap<>();

    private BossSanityReward() {
    }

    /** Records participation: any damage a player deals to an Ancient One. */
    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (event.getEntity() instanceof AncientOne boss
                && event.getSource().getEntity() instanceof ServerPlayer player) {
            HITS.computeIfAbsent(boss.getUUID(), ignored -> new ConcurrentHashMap<>())
                    .put(player.getUUID(), player.level().getGameTime());
        }
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof AncientOne boss)
                || !(boss.level() instanceof ServerLevel level)) {
            return;
        }
        MinecraftServer server = level.getServer();
        long now = level.getGameTime();
        long window = ModConfig.BOSS_CONTRIBUTION_WINDOW.get();
        Map<UUID, Long> hits = HITS.remove(boss.getUUID());
        Set<ServerPlayer> full = new HashSet<>();
        if (hits != null) {
            for (Map.Entry<UUID, Long> hit : hits.entrySet()) {
                if (now - hit.getValue() > window) {
                    continue;
                }
                ServerPlayer player = server.getPlayerList().getPlayer(hit.getKey());
                if (player != null && !excluded(player)) {
                    full.add(player);
                }
            }
        }
        if (event.getSource().getEntity() instanceof ServerPlayer killer
                && !excluded(killer)) {
            full.add(killer);
        }
        for (ServerPlayer player : full) {
            reward(player, boss, ModConfig.BOSS_KILL_SANITY_REWARD.get(), true);
        }
        double ambient = ModConfig.BOSS_KILL_AMBIENT_SANITY.get();
        if (ambient > 0.0) {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                if (!full.contains(player) && !excluded(player)) {
                    reward(player, boss, ambient, false);
                }
            }
        }
    }

    /** Drops stale participation records so a boss that never dies leaves no map behind. */
    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (event.getServer().getTickCount() % PRUNE_INTERVAL != 0 || HITS.isEmpty()) {
            return;
        }
        long now = event.getServer().overworld().getGameTime();
        long window = ModConfig.BOSS_CONTRIBUTION_WINDOW.get();
        Iterator<Map.Entry<UUID, Map<UUID, Long>>> bosses = HITS.entrySet().iterator();
        while (bosses.hasNext()) {
            Map<UUID, Long> hits = bosses.next().getValue();
            hits.entrySet().removeIf(hit -> now - hit.getValue() > window);
            if (hits.isEmpty()) {
                bosses.remove();
            }
        }
    }

    /** Cultists and the recently severed get no relief from a boss kill. */
    private static boolean excluded(ServerPlayer player) {
        return CultSystem.isInitiated(player) || CultBetrayal.isSevered(player);
    }

    private static void reward(ServerPlayer player, AncientOne boss, double amount, boolean direct) {
        if (amount <= 0.0) {
            return;
        }
        SanityAPI.add(player, amount);
        player.sendSystemMessage(Component.translatable(
                direct ? "boss.reward.eldritch_horror" : "boss.reward.ambient.eldritch_horror",
                boss.getDisplayName(), amount));
    }
}
