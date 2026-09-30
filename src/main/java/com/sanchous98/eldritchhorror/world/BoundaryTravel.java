package com.sanchous98.eldritchhorror.world;

import com.sanchous98.eldritchhorror.EldritchHorror;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * The world is a <b>cylinder</b> (see {@code design/23-boundary-and-travel.md}).
 *
 * <ul>
 *   <li><b>East–west is closed.</b> Longitude wraps in the sampler; here a <b>seam warp</b>
 *       carries a player who reaches {@code |x| >= HALF_WIDTH} to the opposite edge, same Z,
 *       keeping momentum, so the ocean simply continues. No wall, no debuff.</li>
 *   <li><b>North–south is bounded by Morok.</b> Past the last baked latitude row the sampler
 *       clamps and the terrain repeats — the frozen, featureless end of the charted world.
 *       Morok is <b>lethal</b>: the deeper past the edge, the harder it stacks darkness and
 *       harm. It never casts you back; it only lets you turn around while you still can.</li>
 * </ul>
 */
@EventBusSubscriber(modid = EldritchHorror.MODID)
public final class BoundaryTravel {

    /** The sampler clamps beyond this |z|; past it the world repeats and Morok begins. */
    private static final int MOROK_EDGE_Z = EarthMap.HALF_HEIGHT;
    /** How far past the edge before Morok becomes fatal (blocks). ~9° of latitude at 364 blk/deg. */
    private static final double MOROK_LETHAL_DEPTH = 4096.0;

    private BoundaryTravel() {
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        long tick = event.getServer().getTickCount();
        // Two cheap passes a second is plenty for both the seam and the debuff.
        if (tick % 10 != 0) {
            return;
        }
        for (ServerLevel level : event.getServer().getAllLevels()) {
            if (!level.dimension().equals(net.minecraft.world.level.Level.OVERWORLD)) {
                continue;
            }
            for (ServerPlayer player : level.getPlayers(p -> true)) {
                seamWarp(player);
                morok(level, player);
            }
        }
    }

    /** Carry a player across the longitude seam, preserving Z and momentum. */
    private static void seamWarp(ServerPlayer player) {
        double x = player.getX();
        int half = EarthMap.HALF_WIDTH;
        if (x >= half) {
            player.teleportTo(x - 2.0 * half, player.getY(), player.getZ());
        } else if (x < -half) {
            player.teleportTo(x + 2.0 * half, player.getY(), player.getZ());
        }
    }

    /** Escalating polar debuff: darkness, nausea, then direct harm, scaled with depth. */
    private static void morok(ServerLevel level, ServerPlayer player) {
        double z = player.getZ();
        double depth = Math.abs(z) - MOROK_EDGE_Z;
        if (depth <= 0) {
            return;
        }
        double f = Math.min(1.0, depth / MOROK_LETHAL_DEPTH); // 0..1
        int amp = (int) Math.min(3, f * 4);

        apply(player, MobEffects.DARKNESS, amp, 60);
        apply(player, MobEffects.NAUSEA, amp, 60);
        if (f > 0.25) {
            apply(player, MobEffects.WEAKNESS, amp, 60);
        }
        if (f > 0.5) {
            apply(player, MobEffects.SLOWNESS, amp, 60);
        }
        // Past the halfway point the world starts taking you: roughly 1 heart per second at full
        // depth. This is what makes Morok kill rather than merely annoy.
        if (f > 0.5 && level.getGameTime() % 10 == 0) {
            float damage = (float) (2.0 * (f - 0.5) * 2.0);
            if (damage > 0.0f) {
                player.hurtServer(level, level.damageSources().magic(), damage);
            }
        }
    }

    private static void apply(Player player, Holder<MobEffect> effect, int amplifier, int ticks) {
        player.addEffect(new MobEffectInstance(effect, ticks, amplifier, true, false, false));
    }
}
