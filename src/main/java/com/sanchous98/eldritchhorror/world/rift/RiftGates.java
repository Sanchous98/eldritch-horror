package com.sanchous98.eldritchhorror.world.rift;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.core.ModConfig;
import com.sanchous98.eldritchhorror.corruption.CorruptionAPI;
import com.sanchous98.eldritchhorror.endgame.RiftLedger;
import com.sanchous98.eldritchhorror.rite.RiteKnowledge;
import com.sanchous98.eldritchhorror.sanity.SanityAPI;
import com.sanchous98.eldritchhorror.world.rift.GateMath.Cost;
import com.sanchous98.eldritchhorror.world.rift.GateMath.Refusal;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.jspecify.annotations.Nullable;

/**
 * The rift gate: the occult fast-travel branch (design/26, "The rift gate"; design/23, "Travel").
 *
 * <p><b>Pinned rules.</b>
 * <ol>
 *   <li>A gate is a standing {@code rift_anchor} block. Using one with an empty hand and knowing
 *       {@code open_rift} is the only way to interact; without the rite the click is refused and
 *       nothing changes.</li>
 *   <li>No UI. The first use of an unlinked gate <em>marks</em> it for that player; the next use of
 *       a different unlinked gate in the same dimension links the pair two-way and clears the mark.
 *       Using the same gate again <b>clears</b> the mark (a way to change your mind). A mark in
 *       another dimension is refused and kept.</li>
 *   <li>Using a linked gate pays corruption and sanity and teleports to a safe spot beside the twin
 *       anchor. The link lives in the per-level {@link GateLinks}, so it can never cross dimensions.
 *       A twin whose block is gone kills the link: nothing is charged.</li>
 *   <li>Every arrival applies a short Nausea. A roll against the configured instability chance may
 *       tear open a fresh gate near the arrival (loaded chunks only, bounded radius); it is added to
 *       the {@link RiftLedger} and announced. No hostile entity is ever spawned.</li>
 *   <li>Breaking an anchor removes its links in {@code RiftAnchorBlock}.</li>
 *   <li>The pending mark is deliberately <b>in memory</b> (keyed by player UUID): it is a transient
 *       "this gate waits" intent, not world state, and is dropped on logout so a reconnect never
 *       links a gate the player no longer remembers choosing.</li>
 * </ol>
 */
@EventBusSubscriber(modid = EldritchHorror.MODID)
public final class RiftGates {

    /** The rite that unlocks gate use (design/26). */
    public static final String OPEN_RITE = "open_rift";

    /** Nausea duration on every arrival: 6 seconds, the route's short vertigo. */
    public static final int ARRIVAL_NAUSEA_TICKS = 120;

    /** Marks are per player and in memory; see the class javadoc for why they are not saved. */
    private static final Map<UUID, Mark> PENDING = new ConcurrentHashMap<>();

    private RiftGates() {
    }

    /** A gate a player has touched and that now waits for its twin. */
    public record Mark(ResourceKey<Level> dimension, BlockPos pos) {
    }

    /**
     * Handles a right-click on a gate. Server-authoritative: the block calls this only on the
     * server and the client only animates the swing.
     */
    public static void use(ServerPlayer player, BlockPos pos) {
        ServerLevel level = player.level();
        if (!ModConfig.ENABLE_RIFT_GATES.get()) {
            player.sendSystemMessage(Component.translatable("rift.eldritch_horror.disabled"));
            return;
        }
        if (!RiteKnowledge.knows(player, OPEN_RITE)) {
            player.sendSystemMessage(Component.translatable("rift.eldritch_horror.unknown_rite"));
            return;
        }
        GateLinks links = GateLinks.of(level);
        BlockPos twin = links.linked(pos);
        if (twin != null) {
            travel(player, level, links, pos, twin);
            return;
        }
        markOrLink(player, level, links, pos);
    }

    /**
     * Removes {@code pos}'s links. Called by {@code RiftAnchorBlock} when the block is destroyed,
     * however that happens, so a broken gate never leaves a one-way link behind.
     */
    public static void breakLinks(ServerLevel level, BlockPos pos) {
        GateLinks.of(level).remove(pos);
        PENDING.values().removeIf(mark -> mark.dimension().equals(level.dimension())
                && mark.pos().equals(pos));
    }

    /** @return the player's pending mark, or {@code null}. Used by {@code /eh gate}. */
    public static @Nullable Mark pending(UUID playerId) {
        return PENDING.get(playerId);
    }

    /** The marking half: mark, clear, link, or refuse a cross-dimension pairing. */
    private static void markOrLink(ServerPlayer player, ServerLevel level, GateLinks links,
                                   BlockPos pos) {
        Mark mark = PENDING.get(player.getUUID());
        if (mark == null) {
            PENDING.put(player.getUUID(), new Mark(level.dimension(), pos));
            player.sendSystemMessage(Component.translatable("rift.eldritch_horror.marked"));
            return;
        }
        if (mark.dimension().equals(level.dimension()) && mark.pos().equals(pos)) {
            PENDING.remove(player.getUUID());
            player.sendSystemMessage(Component.translatable("rift.eldritch_horror.unmarked"));
            return;
        }
        if (!mark.dimension().equals(level.dimension())) {
            player.sendSystemMessage(Component.translatable("rift.eldritch_horror.cross_dimension"));
            return;
        }
        if (!isGate(level, mark.pos())) {
            // The marked gate was broken while the player was away: forget it, mark this one instead.
            PENDING.put(player.getUUID(), new Mark(level.dimension(), pos));
            player.sendSystemMessage(Component.translatable("rift.eldritch_horror.twin_gone"));
            return;
        }
        links.add(mark.pos(), pos);
        PENDING.remove(player.getUUID());
        player.sendSystemMessage(Component.translatable("rift.eldritch_horror.linked"));
    }

    /** The travel half: check the link, pick a safe spot, charge once, move and roll instability. */
    private static void travel(ServerPlayer player, ServerLevel level, GateLinks links,
                               BlockPos origin, BlockPos destination) {
        boolean destinationPresent = isGate(level, destination);
        Refusal refusal = GateMath.refusal(true, true, true, true, destinationPresent);
        if (refusal == Refusal.DEAD_LINK) {
            links.remove(origin);
            player.sendSystemMessage(Component.translatable("rift.eldritch_horror.dead_link"));
            return;
        }
        Predicate<GateMath.Spot> safe = p -> {
            BlockPos at = new BlockPos(p.x(), p.y(), p.z());
            return level.isLoaded(at)
                    && level.getBlockState(at).canBeReplaced()
                    && level.getBlockState(at.above()).canBeReplaced()
                    && !level.getBlockState(at.below()).canBeReplaced();
        };
        GateMath.Spot arrivalSpot = GateMath.arrivalOffset(
                new GateMath.Spot(destination.getX(), destination.getY(), destination.getZ()), safe);
        if (arrivalSpot == null) {
            player.sendSystemMessage(Component.translatable("rift.eldritch_horror.no_arrival"));
            return;
        }
        BlockPos arrival = new BlockPos(arrivalSpot.x(), arrivalSpot.y(), arrivalSpot.z());
        boolean moved = player.teleportTo(level, arrival.getX() + 0.5, arrival.getY(),
                arrival.getZ() + 0.5, java.util.Set.of(), player.getYRot(), player.getXRot(), false);
        if (!moved) {
            player.sendSystemMessage(Component.translatable("rift.eldritch_horror.no_arrival"));
            return;
        }

        double chance = ModConfig.RIFT_GATE_INSTABILITY_CHANCE.get();
        Cost cost = GateMath.resolve(refusal, ModConfig.RIFT_GATE_CORRUPTION.get(),
                -ModConfig.RIFT_GATE_SANITY.get(), chance, level.getRandom().nextDouble());
        CorruptionAPI.add(player, cost.corruption());
        SanityAPI.add(player, cost.sanity());
        player.addEffect(new MobEffectInstance(MobEffects.NAUSEA, ARRIVAL_NAUSEA_TICKS, 0));
        player.sendSystemMessage(Component.translatable("rift.eldritch_horror.travelled",
                String.format(java.util.Locale.ROOT, "%.1f", cost.corruption()),
                String.format(java.util.Locale.ROOT, "%.1f", Math.abs(cost.sanity()))));
        if (cost.unstable()) {
            openNearby(level, arrival);
        }
    }

    /** Places and announces the fresh gate an unstable arrival tears open, when ground allows. */
    private static void openNearby(ServerLevel level, BlockPos arrival) {
        Block anchor = anchorBlock();
        if (anchor == null) {
            return;
        }
        Predicate<GateMath.Spot> free = p -> {
            BlockPos at = new BlockPos(p.x(), p.y(), p.z());
            return level.isLoaded(at)
                    && level.getBlockState(at).canBeReplaced()
                    && !level.getBlockState(at.below()).canBeReplaced();
        };
        GateMath.Spot spot = GateMath.instabilitySpot(
                new GateMath.Spot(arrival.getX(), arrival.getY(), arrival.getZ()), free);
        if (spot == null) {
            return;
        }
        BlockPos at = new BlockPos(spot.x(), spot.y(), spot.z());
        level.setBlock(at, anchor.defaultBlockState(), Block.UPDATE_ALL);
        RiftLedger.of(level).add(at);
        level.getPlayers(p -> p.distanceToSqr(spot.x() + 0.5, spot.y(),
                spot.z() + 0.5) <= 64.0 * 64.0).forEach(p -> p.sendSystemMessage(
                Component.translatable("rift.eldritch_horror.unstable", spot.x(), spot.y(),
                        spot.z())));
    }

    /** @return whether {@code pos} still holds a rift anchor (loads the destination chunk). */
    private static boolean isGate(ServerLevel level, BlockPos pos) {
        Block anchor = anchorBlock();
        if (anchor == null) {
            return false;
        }
        BlockState state = level.getBlockState(pos);
        return state.is(anchor);
    }

    private static @Nullable Block anchorBlock() {
        Block block = BuiltInRegistries.BLOCK.getValue(EldritchHorror.id("rift_anchor"));
        return block == Blocks.AIR ? null : block;
    }

    /** Drops a pending mark when its player leaves; see the class javadoc. */
    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PENDING.remove(player.getUUID());
        }
    }

    /** Forgets every pending mark; called when the world goes away. */
    public static void clear() {
        PENDING.clear();
    }
}
