package com.sanchous98.eldritchhorror.cult;

import com.sanchous98.eldritchhorror.corruption.CorruptionAPI;
import com.sanchous98.eldritchhorror.corruption.TaintAPI;
import com.sanchous98.eldritchhorror.rite.RiteKnowledge;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.Locale;

/**
 * Server-authoritative resolution for a {@link CultService}: checks the caller's {@link CultRank}
 * against the service's {@link CultService#minRank()} and then performs it. Reputation is the gate,
 * so nothing is deducted (services are earned, not bought) — see {@link #perform}.
 *
 * <p>Effects are deterministic and never force a chunk to load:
 * <ul>
 *   <li>{@link CultService.Kind#TEACH} — grants the rite via {@link RiteKnowledge#add}; already-known
 *       is reported as such, never a silent no-op.</li>
 *   <li>{@link CultService.Kind#CLEANSE} — lowers the player's corruption and the local chunk taint
 *       (the Order's "cleansing rites" from {@code design/09-cults.md}). This cannot reuse
 *       {@code RiteEngine}'s private cleanse path, so the documented outcome is re-expressed here:
 *       corruption {@code −20} on the meter and {@code −0.30} taint across the loaded 3×3 chunk patch,
 *       mirroring the rite's numbers.</li>
 * </ul>
 */
public final class CultServices {

    /** Meter reduction applied by {@link CultService.Kind#CLEANSE} (mirrors {@code rite_of_cleansing}). */
    private static final double CLEANSE_CORRUPTION = 20.0;
    /** Taint removed from each loaded chunk in the patch. */
    private static final double CLEANSE_TAINT = 0.30;
    /** Chunk radius cleansed (1 ⇒ a 3×3 chunk patch); only loaded chunks. */
    private static final int CLEANSE_RADIUS = 1;

    private CultServices() {
    }

    /**
     * Performs {@code service} for {@code player} if their {@link CultRank} in {@code cultId} is at
     * least the service's minimum. Never a silent no-op: a failure explains the shortfall.
     *
     * @return what happened; {@code ok=false} means nothing changed
     */
    public static Outcome perform(ServerPlayer player, String cultId, CultService service) {
        CultRank rank = CultSystem.rankOf(player, cultId);
        if (!rank.atLeast(service.minRank())) {
            return new Outcome(false, Component.translatable(
                    "service.eldritch_horror.fail.rank",
                    service.name(), service.minRank().display(), rank.display()));
        }
        return switch (service.kind()) {
            case TEACH -> teach(player, service);
            case CLEANSE -> cleanse(player, service);
        };
    }

    /** Teaches {@link CultService#target()}; a player who already knows it is told so. */
    private static Outcome teach(ServerPlayer player, CultService service) {
        if (RiteKnowledge.add(player, service.target())) {
            return new Outcome(true, Component.translatable(
                    "service.eldritch_horror.teach.success", service.name(),
                    RiteKnowledge.name(service.target())));
        }
        return new Outcome(false, Component.translatable(
                "service.eldritch_horror.teach.known", service.name(),
                RiteKnowledge.name(service.target())));
    }

    /** Cleanses the caller and the local taint; reports before → after so the effect is visible. */
    private static Outcome cleanse(ServerPlayer player, CultService service) {
        double before = CorruptionAPI.get(player);
        double after = CorruptionAPI.add(player, -CLEANSE_CORRUPTION);
        double taint = cleanseTaint(player);
        return new Outcome(true, Component.translatable(
                "service.eldritch_horror.cleanse.success", service.name(),
                fmt(before), fmt(after), fmt(taint)));
    }

    /** Lowers the taint of every loaded chunk in the 3×3 patch; returns the origin chunk's new taint. */
    private static double cleanseTaint(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel level)) {
            return 0.0;
        }
        ChunkPos centre = player.chunkPosition();
        for (int dx = -CLEANSE_RADIUS; dx <= CLEANSE_RADIUS; dx++) {
            for (int dz = -CLEANSE_RADIUS; dz <= CLEANSE_RADIUS; dz++) {
                ChunkPos pos = new ChunkPos(centre.x() + dx, centre.z() + dz);
                LevelChunk chunk = level.getChunkSource().getChunkNow(pos.x(), pos.z());
                if (chunk != null) {
                    TaintAPI.set(chunk, Math.max(0.0, TaintAPI.get(chunk) - CLEANSE_TAINT));
                }
            }
        }
        return TaintAPI.get(level, centre);
    }

    private static String fmt(double v) {
        return String.format(Locale.ROOT, "%.0f", v);
    }

    /**
     * What a service did. {@code ok=false} means the rank gate refused (or a teach was redundant); the
     * message is always safe to show the caller.
     */
    public record Outcome(boolean ok, Component message) {
    }
}
