package com.sanchous98.eldritchhorror.bot;

import com.sanchous98.eldritchhorror.EldritchHorror;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

/**
 * Server-side walking for {@link BotPlayer}: a target set by {@code /eh bot goto} is followed one
 * small, collision-checked step per server tick, so the bot crosses the ground like a player
 * instead of being teleported across the map.
 *
 * <p>What this is not: a pathfinder. The step is straight at the target, but it goes through
 * {@link net.minecraft.world.entity.Entity#move} (so walls stop it and steps up to the player step
 * height are climbed), it snaps to the surface height under it, and it jumps when it stops making
 * horizontal progress. Deep water and cliffs are handled by the surface snap; a target behind a
 * long wall needs a waypoint, which the driver supplies.
 *
 * <p>Chunks along the route are ticket-loaded as the bot advances (a small forced ticket moved
 * with the bot), because a headless player has no client chunk stream of its own.
 */
public final class BotWalk {

    /** Walk speed in blocks per tick (~4.3 blocks/s, the vanilla walking speed). */
    private static final double WALK_SPEED = 0.215;

    /** Sprint speed, used for long legs. */
    private static final double SPRINT_SPEED = 0.28;

    /** Ticks without horizontal progress before a jump is attempted. */
    private static final int STUCK_TICKS = 8;

    /** Radius (chunks) of the moving forced ticket. */
    private static final int CHUNK_RADIUS = 3;

    private static final Map<UUID, Target> TARGETS = new ConcurrentHashMap<>();
    private static final Map<UUID, ChunkPos> TICKETS = new ConcurrentHashMap<>();

    private BotWalk() {
    }

    /** One bot's current destination. */
    private record Target(double x, double z, boolean sprint, long startedAt) {
    }

    /** Starts (or replaces) a walk to {@code (x, z)}. Returns the distance in blocks. */
    public static double start(BotPlayer bot, double x, double z, boolean sprint) {
        double distance = Math.hypot(x - bot.getX(), z - bot.getZ());
        TARGETS.put(bot.getUUID(), new Target(x, z, sprint, bot.level().getGameTime()));
        EldritchHorror.LOGGER.info("bot walk: {} -> {}, {} ({:.0f} blocks{})",
                bot.getGameProfile().name(), x, z, distance, sprint ? ", sprint" : "");
        return distance;
    }

    /** Stops walking. */
    public static void stop(BotPlayer bot) {
        TARGETS.remove(bot.getUUID());
    }

    /** @return whether the bot is walking. */
    public static boolean isWalking(BotPlayer bot) {
        return TARGETS.containsKey(bot.getUUID());
    }

    /** @return the remaining distance, or {@code -1} when idle. */
    public static double remaining(BotPlayer bot) {
        Target target = TARGETS.get(bot.getUUID());
        return target == null ? -1 : Math.hypot(target.x() - bot.getX(), target.z() - bot.getZ());
    }

    /** Advances every walking bot by one tick. Called from the bot ticker. */
    public static void tick(BotPlayer bot) {
        Target target = TARGETS.get(bot.getUUID());
        if (target == null) {
            return;
        }
        double dx = target.x() - bot.getX();
        double dz = target.z() - bot.getZ();
        double distance = Math.hypot(dx, dz);
        if (distance <= 1.5) {
            TARGETS.remove(bot.getUUID());
            releaseTicket(bot);
            EldritchHorror.LOGGER.info("bot walk: {} arrived at {}, {}",
                    bot.getGameProfile().name(), Math.round(bot.getX()), Math.round(bot.getZ()));
            return;
        }
        if (!(bot.level() instanceof ServerLevel level)) {
            return;
        }
        // A stuck bot (a wall, a fence, a two-block step) jumps; a long chase without progress is
        // better abandoned than allowed to spin forever.
        long elapsed = level.getGameTime() - target.startedAt();
        if (elapsed > 20L * 60 * 30) {
            TARGETS.remove(bot.getUUID());
            releaseTicket(bot);
            EldritchHorror.LOGGER.info("bot walk: {} gave up after 30 minutes", bot.getGameProfile().name());
            return;
        }

        moveTicket(bot, level);
        double speed = target.sprint() ? SPRINT_SPEED : WALK_SPEED;
        double stepX = dx / distance * speed;
        double stepZ = dz / distance * speed;
        double yaw = Math.toDegrees(Math.atan2(-dx, dz));
        bot.setYRot((float) yaw);
        bot.setYHeadRot((float) yaw);

        double beforeX = bot.getX();
        double beforeZ = bot.getZ();
        bot.move(net.minecraft.world.entity.MoverType.PLAYER, new Vec3(stepX, 0.0, stepZ));

        // Snap to the walking surface: a step up is climbed by the move call, a step down is a
        // short fall; never leave the bot floating over a drop or buried in a slope.
        BlockPos feet = bot.blockPosition();
        int ground = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, feet.getX(), feet.getZ());
        if (bot.onGround() && bot.getY() < ground) {
            bot.setPos(bot.getX(), ground, bot.getZ());
        }
        bot.resetFallDistance();

        double moved = Math.hypot(bot.getX() - beforeX, bot.getZ() - beforeZ);
        if (moved < speed * 0.3) {
            if (bot.onGround()) {
                Vec3 motion = bot.getDeltaMovement();
                bot.setDeltaMovement(motion.x, 0.42, motion.z);
            }
        }
    }

    /** Moves the bot's small forced ticket so the chunks around it simulate while it walks. */
    private static void moveTicket(BotPlayer bot, ServerLevel level) {
        ChunkPos pos = ChunkPos.containing(bot.blockPosition());
        ChunkPos previous = TICKETS.put(bot.getUUID(), pos);
        if (pos.equals(previous)) {
            return;
        }
        if (previous != null) {
            level.getChunkSource().removeTicketWithRadius(
                    net.minecraft.server.level.TicketType.FORCED, previous, CHUNK_RADIUS);
        }
        level.getChunkSource().addTicketWithRadius(
                net.minecraft.server.level.TicketType.FORCED, pos, CHUNK_RADIUS);
    }

    private static void releaseTicket(BotPlayer bot) {
        ChunkPos previous = TICKETS.remove(bot.getUUID());
        if (previous != null && bot.level() instanceof ServerLevel level) {
            level.getChunkSource().removeTicketWithRadius(
                    net.minecraft.server.level.TicketType.FORCED, previous, CHUNK_RADIUS);
        }
    }
}
