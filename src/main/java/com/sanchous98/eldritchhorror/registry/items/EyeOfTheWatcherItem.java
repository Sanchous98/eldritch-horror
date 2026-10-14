package com.sanchous98.eldritchhorror.registry.items;

import com.sanchous98.eldritchhorror.event.RiftKnowledge;
import com.sanchous98.eldritchhorror.sanity.SanityAPI;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/**
 * Eye of the Watcher (design/16). <b>Passive:</b> sanity drains at {@value #DRAIN_PER_SECOND}/s
 * while carried. <b>Use:</b> names the direction of the nearest rift the holder has already
 * discovered. The design's see-through-walls rendering is not wired — it needs a client renderer,
 * and the mod's map/rift knowledge is the server-side half of that effect.
 *
 * <p>Carried means anywhere in the inventory (the mod has no trinket slot);
 * {@link Item#inventoryTick} fires for player inventories only, so a chest full of eyes drains
 * nobody.
 */
public final class EyeOfTheWatcherItem extends Item {

    /** Sanity lost per second while the eye is carried (design/16: "sanity drains while equipped"). */
    private static final double DRAIN_PER_SECOND = 0.1;
    /** One drain per second, matching the sanity ticker's cadence. */
    private static final int DRAIN_INTERVAL_TICKS = 20;

    public EyeOfTheWatcherItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (player instanceof ServerPlayer serverPlayer) {
            BlockPos nearest = nearestKnownRift(serverPlayer);
            if (nearest == null) {
                serverPlayer.sendSystemMessage(Component.literal(
                        "The Eye sees no rift you already know."));
            } else {
                serverPlayer.sendSystemMessage(Component.literal(
                        "The Eye turns toward a known rift: " + bearing(serverPlayer, nearest) + "."));
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void inventoryTick(ItemStack itemStack, ServerLevel level, Entity owner,
                              @Nullable EquipmentSlot slot) {
        if (owner instanceof ServerPlayer player && level.getGameTime() % DRAIN_INTERVAL_TICKS == 0) {
            SanityAPI.add(player, -DRAIN_PER_SECOND);
        }
    }

    /** The nearest discovered rift, from the synced per-player knowledge, or {@code null}. */
    private static @Nullable BlockPos nearestKnownRift(ServerPlayer player) {
        BlockPos nearest = null;
        double best = Double.MAX_VALUE;
        for (String key : RiftKnowledge.known(player)) {
            BlockPos pos = RiftKnowledge.parse(key);
            if (pos == null) {
                continue;
            }
            double distance = player.distanceToSqr(
                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
            if (distance < best) {
                best = distance;
                nearest = pos;
            }
        }
        return nearest;
    }

    /** {@code "<direction>, N blocks"} from the player to {@code target}. */
    private static String bearing(ServerPlayer player, BlockPos target) {
        int dx = target.getX() - player.getBlockX();
        int dz = target.getZ() - player.getBlockZ();
        Direction direction = Direction.getNearest(dx, 0, dz, Direction.NORTH);
        int distance = (int) Math.round(Math.sqrt(player.distanceToSqr(
                target.getX() + 0.5, target.getY() + 0.5, target.getZ() + 0.5)));
        return direction.getName() + ", " + distance + " blocks";
    }
}
