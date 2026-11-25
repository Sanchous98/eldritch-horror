package com.sanchous98.eldritchhorror.world;

import com.sanchous98.eldritchhorror.EldritchHorror;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Catches players who fall out of a prebuilt city.
 *
 * <p>The 1:1 Arnis cities are islands: a thin crust over void. A gap between buildings, an open
 * cellar or the edge of the district drops the player past bedrock, where vanilla would kill them
 * and respawn them somewhere unexpected. This ticker teleports them back to the world spawn with
 * a short slow-fall instead, so exploring a prebuilt city is not a death sentence.
 */
@EventBusSubscriber(modid = EldritchHorror.MODID)
public final class VoidRescue {

    /** Below this Y a player is considered to have fallen out of the world. */
    private static final double FLOOR_Y = -40.0;
    private static final int CHECK_INTERVAL = 10;

    private VoidRescue() {
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (event.getServer().getTickCount() % CHECK_INTERVAL != 0) {
            return;
        }
        ServerLevel overworld = event.getServer().overworld();
        if (overworld == null) {
            return;
        }
        BlockPos spawn = overworld.getRespawnData().globalPos().pos();
        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            if (player.level() != overworld || player.getY() >= FLOOR_Y) {
                continue;
            }
            player.teleportTo(overworld, spawn.getX() + 0.5, spawn.getY() + 1.0,
                    spawn.getZ() + 0.5, java.util.Set.of(), player.getYRot(),
                    player.getXRot(), false);
            player.setDeltaMovement(0.0, 0.0, 0.0);
            player.fallDistance = 0.0F;
            player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 200, 0, false, false));
            player.sendSystemMessage(Component.literal(
                    "Бездна не отпускает — но город возвращает тебя на улицу."));
            EldritchHorror.LOGGER.info("void rescue: {} returned to spawn from y={}",
                    player.getName().getString(), player.getY());
        }
    }
}
