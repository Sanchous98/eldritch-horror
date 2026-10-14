package com.sanchous98.eldritchhorror.corruption;

import com.sanchous98.eldritchhorror.core.ModConfig;
import com.sanchous98.eldritchhorror.registry.items.CarriedItems;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;

/**
 * The per-chunk taint field: a slow, deterministic diffusion over the chunks around each player.
 *
 * <p>Once a second each loaded chunk above {@link ModConfig#TAINT_SPREAD_THRESHOLD} bleeds a fixed
 * fraction of its excess into its 4 (N/E/S/W) neighbours. To stay O(loaded chunks) — and never
 * touch the whole world — the step only visits the loaded chunks in the players' vicinity, using
 * {@link ServerChunkCache#getChunkNow} so it never loads or generates a chunk. Persisted through
 * the existing {@code TAINT} attachment.
 */
public final class TaintSystem {

    /** Radius in blocks within which a carried {@code warden_sigil} damps spread (design/16). */
    private static final double SIGIL_RADIUS = 16.0;

    /** Fraction of the bleed a carried sigil removes inside its radius (design/16: −40%). */
    private static final double SIGIL_SUPPRESSION = 0.4;

    private TaintSystem() {
    }

    /**
     * One second of diffusion around every player in {@code level}. Overworld-only by the caller.
     *
     * <p>All gains are gathered from the pre-step values first and applied afterwards, so the result
     * is independent of player/seed iteration order.
     */
    public static void spread(ServerLevel level) {
        if (!ModConfig.ENABLE_CORRUPTION_SPREAD.get()) {
            return;
        }
        double rate = ModConfig.TAINT_SPREAD_RATE.get();
        double threshold = ModConfig.TAINT_SPREAD_THRESHOLD.get();
        if (rate <= 0.0) {
            return;
        }

        ServerChunkCache cache = level.getChunkSource();
        Map<Long, Double> gains = new HashMap<>();
        Set<Long> visited = new HashSet<>();
        // Warden's Sigil (design/16): a carried sigil damps spread within its radius. Collect the
        // carriers once, so each candidate source chunk is a cheap distance test against a short list.
        List<BlockPos> sigils = new ArrayList<>();
        for (ServerPlayer player : level.players()) {
            if (CarriedItems.carries(player, "warden_sigil")) {
                sigils.add(player.blockPosition());
            }
        }
        for (ServerPlayer player : level.players()) {
            ChunkPos centre = player.chunkPosition();
            // Candidate sources: the player's chunk and its immediate neighbours, if loaded.
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    LevelChunk chunk = cache.getChunkNow(centre.x() + dx, centre.z() + dz);
                    if (chunk == null || !visited.add(chunk.getPos().pack())) {
                        continue;
                    }
                    double taint = CorruptionSystem.getTaint(chunk);
                    if (taint <= threshold) {
                        continue;
                    }
                    double bleed = (taint - threshold) * rate * sigilFactor(chunk.getPos(), sigils);
                    ChunkPos pos = chunk.getPos();
                    addGain(gains, pos.x() + 1, pos.z(), bleed);
                    addGain(gains, pos.x() - 1, pos.z(), bleed);
                    addGain(gains, pos.x(), pos.z() + 1, bleed);
                    addGain(gains, pos.x(), pos.z() - 1, bleed);
                }
            }
        }

        // Apply: only chunks that are already loaded receive taint (never loads/generates).
        for (Map.Entry<Long, Double> entry : gains.entrySet()) {
            ChunkPos pos = ChunkPos.unpack(entry.getKey());
            LevelChunk chunk = cache.getChunkNow(pos.x(), pos.z());
            if (chunk != null) {
                CorruptionSystem.addTaint(chunk, entry.getValue());
            }
        }
    }

    private static void addGain(Map<Long, Double> gains, int x, int z, double amount) {
        gains.merge(ChunkPos.pack(x, z), amount, Double::sum);
    }

    /**
     * @return {@code 1.0}, or the suppressed factor when a sigil carrier sits within
     *         {@value #SIGIL_RADIUS} blocks of {@code pos}'s centre (a 16-block radius covers the
     *         carrier's own chunk and parts of its neighbours, so only that bleed is damped)
     */
    private static double sigilFactor(ChunkPos pos, List<BlockPos> sigils) {
        if (sigils.isEmpty()) {
            return 1.0;
        }
        int x = pos.getMiddleBlockX();
        int z = pos.getMiddleBlockZ();
        for (BlockPos sigil : sigils) {
            double dx = x - sigil.getX();
            double dz = z - sigil.getZ();
            if (dx * dx + dz * dz <= SIGIL_RADIUS * SIGIL_RADIUS) {
                return 1.0 - SIGIL_SUPPRESSION;
            }
        }
        return 1.0;
    }
}
