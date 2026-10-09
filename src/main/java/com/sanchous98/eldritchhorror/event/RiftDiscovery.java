package com.sanchous98.eldritchhorror.event;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.core.ModConfig;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Discovers rifts for each player so the world map can mark them (design/22: rifts are discovered by
 * proximity, or by opening one). Runs every {@value #INTERVAL_TICKS} ticks, overworld only.
 *
 * <p>Bounded and loaded-chunks-only: {@link Rifts#near} never force-loads, and pruning only inspects
 * positions whose chunk is already loaded. A known position whose block is no longer a rift (someone
 * sealed it) is forgotten.
 */
@EventBusSubscriber(modid = EldritchHorror.MODID)
public final class RiftDiscovery {

    /** Discovery cadence: every 2 seconds. */
    private static final int INTERVAL_TICKS = 40;

    private RiftDiscovery() {
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (!ModConfig.ENABLE_RIFT_DISCOVERY.get()) {
            return;
        }
        if (event.getServer().getTickCount() % INTERVAL_TICKS != 0) {
            return;
        }
        ServerLevel overworld = event.getServer().getLevel(Level.OVERWORLD);
        if (overworld == null) {
            return;
        }
        int radius = ModConfig.RIFT_DISCOVERY_CHUNK_RADIUS.get();
        int max = ModConfig.RIFT_DISCOVERY_MAX.get();
        for (ServerPlayer player : overworld.players()) {
            // Discover rifts currently near the player.
            for (BlockPos pos : Rifts.near(overworld, player.blockPosition(), radius, max)) {
                RiftKnowledge.discover(player, pos);
            }
            // Forget rifts that have been sealed (only inspect loaded chunks).
            prune(overworld, player);
        }
    }

    private static void prune(ServerLevel level, ServerPlayer player) {
        Set<String> known = RiftKnowledge.known(player);
        if (known.isEmpty()) {
            return;
        }
        List<BlockPos> stale = new ArrayList<>();
        for (String key : known) {
            BlockPos pos = RiftKnowledge.parse(key);
            if (pos == null) {
                continue;
            }
            if (level.isLoaded(pos) && !Rifts.isRift(level, pos)) {
                stale.add(pos);
            }
        }
        for (BlockPos pos : stale) {
            RiftKnowledge.forget(player, pos);
        }
    }
}
