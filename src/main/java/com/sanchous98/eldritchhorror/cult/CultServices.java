package com.sanchous98.eldritchhorror.cult;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.codex.CodexAPI;
import com.sanchous98.eldritchhorror.corruption.CorruptionAPI;
import com.sanchous98.eldritchhorror.corruption.TaintAPI;
import com.sanchous98.eldritchhorror.rite.RiteKnowledge;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Prediction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.Locale;

/**
 * Server-authoritative resolution for a {@link CultService}: checks the caller's {@link CultRank}
 * against the service's {@link CultService#minRank()} and then performs it. Reputation is the gate:
 * {@code TEACH} and {@code CLEANSE} are earned and free, while {@code TRADE} and {@code OFFER}
 * charge an item — see {@link #perform}.
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
 *   <li>{@link CultService.Kind#OFFER} — consumes the cult's demanded item and pays reputation
 *       ({@code design/03c-reputation.md}: "+5 per offering"), so standing has a source that is not
 *       combat or a rite.</li>
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
            case TRADE -> trade(player, service);
            case OFFER -> offer(player, cultId, service);
        };
    }

    /** Teaches {@link CultService#target()}; a player who already knows it is told so. */
    private static Outcome teach(ServerPlayer player, CultService service) {
        if (CodexAPI.learn(player, service.target())) {
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

    /**
     * Exchanges {@link CultService#costCount()} of {@code costItem} for
     * {@link CultService#grantCount()} of {@code target}. All-or-nothing: a short purse refuses and
     * charges nothing. This is the sink that makes the mod's currencies (obols, tokens, scrip)
     * spendable, and one of the reachable sources of ritual reagents.
     */
    private static Outcome trade(ServerPlayer player, CultService service) {
        Item cost = item(service.costItem());
        Item grant = item(service.target());
        if (cost == null || grant == null) {
            return new Outcome(false, Component.translatable("service.eldritch_horror.trade.unknown"));
        }
        int carried = player.getInventory().clearOrCountMatchingItems(
                stack -> stack.is(cost), true, 0, player.inventoryMenu.getCraftSlots());
        if (carried < service.costCount()) {
            return new Outcome(false, Component.translatable("service.eldritch_horror.fail.cost",
                    service.name(), service.costCount(), new ItemStack(cost).getHoverName(),
                    carried));
        }
        player.getInventory().clearOrCountMatchingItems(
                stack -> stack.is(cost), false, service.costCount(),
                player.inventoryMenu.getCraftSlots());
        ItemStack reward = new ItemStack(grant, service.grantCount());
        // Inventory#add consumes the passed stack, so keep a copy for the message.
        ItemStack granted = reward.copy();
        if (!player.getInventory().add(reward)) {
            player.drop(reward, false, Prediction.SERVER_ONLY);
        }
        return new Outcome(true, Component.translatable("service.eldritch_horror.trade.success",
                service.name(), granted.getCount(), granted.getHoverName(),
                service.costCount(), new ItemStack(cost).getHoverName()));
    }

    /**
     * Consumes the cult's demanded offering and pays reputation (design/03c: "Offer a demanded item:
     * +5 per offering"). All-or-nothing; the gain still crosses the opposed pairs through
     * {@link CultSystem#add}. Reports the actual shift, which clamping or an opposed loss can differ
     * from the nominal amount.
     */
    private static Outcome offer(ServerPlayer player, String cultId, CultService service) {
        Item cost = item(service.costItem());
        if (cost == null) {
            return new Outcome(false, Component.translatable("service.eldritch_horror.offer.unknown"));
        }
        int carried = player.getInventory().clearOrCountMatchingItems(
                stack -> stack.is(cost), true, 0, player.inventoryMenu.getCraftSlots());
        if (carried < service.costCount()) {
            return new Outcome(false, Component.translatable("service.eldritch_horror.fail.cost",
                    service.name(), service.costCount(), new ItemStack(cost).getHoverName(),
                    carried));
        }
        player.getInventory().clearOrCountMatchingItems(
                stack -> stack.is(cost), false, service.costCount(),
                player.inventoryMenu.getCraftSlots());
        player.containerMenu.broadcastChanges();
        int before = CultSystem.get(player, cultId);
        int after = CultSystem.add(player, cultId, service.grantCount());
        return new Outcome(true, Component.translatable("service.eldritch_horror.offer.success",
                service.name(), service.costCount(), new ItemStack(cost).getHoverName(),
                after - before, after));
    }

    /** @return the registered item for a mod path, or {@code null} when unregistered. */
    private static Item item(String path) {
        if (path == null || path.isEmpty()) {
            return null;
        }
        Item resolved = BuiltInRegistries.ITEM.getValue(EldritchHorror.id(path));
        return resolved == Items.AIR ? null : resolved;
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
