package com.sanchous98.eldritchhorror.registry.items;

import com.sanchous98.eldritchhorror.codex.CodexAPI;
import com.sanchous98.eldritchhorror.registry.ModSounds;
import com.sanchous98.eldritchhorror.sanity.SanityAPI;
import com.sanchous98.eldritchhorror.world.loc.Location;
import com.sanchous98.eldritchhorror.world.loc.Locations;
import com.sanchous98.eldritchhorror.world.loc.city.CityLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
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
 * Mirror Shard (design/16). <b>Use:</b> a bearing to the nearest registered site the holder has
 * <b>not</b> discovered — the non-city {@code Locations}, minus the {@link CodexAPI} known set, so
 * the shard shows what the codex has not yet recorded. <b>Passive:</b> while carried, a rare sanity
 * jolt and the same whisper the sanity ticker uses at the deep end.
 *
 * <p>flavour (design/16): "reveals a nearby rite" has no rite-location registry to read; the shard
 * answers with the nearest undiscovered site instead.
 */
public final class MirrorShardItem extends Item {

    /** Sanity lost per jolt. */
    private static final double JOLT = 2.0;
    /** One check per second; 1/120 is a jolt roughly every two minutes on average. */
    private static final int JOLT_CHECK_INTERVAL_TICKS = 20;
    private static final int JOLT_CHANCE = 120;

    public MirrorShardItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (player instanceof ServerPlayer serverPlayer) {
            BlockPos nearest = nearestUndiscovered(serverPlayer);
            serverPlayer.sendSystemMessage(nearest == null
                    ? Component.literal("The shard reflects only places you have already found.")
                    : Component.literal("The shard clouds toward something unfound: "
                            + NavigationItem.bearing(serverPlayer, nearest) + "."));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void inventoryTick(ItemStack itemStack, ServerLevel level, Entity owner,
                              @Nullable EquipmentSlot slot) {
        if (owner instanceof ServerPlayer player
                && level.getGameTime() % JOLT_CHECK_INTERVAL_TICKS == 0
                && player.getRandom().nextInt(JOLT_CHANCE) == 0) {
            SanityAPI.add(player, -JOLT);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    ModSounds.AMBIENT_WHISPER.get(), SoundSource.AMBIENT, 0.4F, 1.0F);
        }
    }

    /** The nearest non-city location whose codex entry the holder does not know, or {@code null}. */
    private static @Nullable BlockPos nearestUndiscovered(ServerPlayer player) {
        BlockPos nearest = null;
        double best = Double.MAX_VALUE;
        for (Location loc : Locations.all()) {
            if (loc instanceof CityLocation || CodexAPI.knows(player, loc.id())) {
                continue;
            }
            double distance = player.distanceToSqr(
                    Locations.xOf(loc), player.getY(), Locations.zOf(loc));
            if (distance < best) {
                best = distance;
                nearest = new BlockPos(Locations.xOf(loc), player.getBlockY(), Locations.zOf(loc));
            }
        }
        return nearest;
    }
}
