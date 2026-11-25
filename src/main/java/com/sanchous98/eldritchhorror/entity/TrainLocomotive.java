package com.sanchous98.eldritchhorror.entity;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.world.rail.RailLine;
import com.sanchous98.eldritchhorror.world.rail.RailNode;
import com.sanchous98.eldritchhorror.world.rail.RailSample;
import com.sanchous98.eldritchhorror.world.rail.RailStation;
import com.sanchous98.eldritchhorror.world.rail.TrainTimetable;
import com.sanchous98.eldritchhorror.world.rail.TrainTrail;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * The locomotive of a scheduled train ({@code design/30-transport.md}, stage 2).
 *
 * <p>It owns the line, the timetable and the consist: the server drives it along a cached
 * {@link RailLine} at {@code trainSpeed} blocks per tick, and the coupled {@link TrainWagon}s sample
 * its recent trail at fixed spacing. Chunk tickets follow the consist: a wide 9×9 ticket covers the
 * whole train while it is spawned/docked (the rear wagon of a four-carriage train is 64 blocks
 * behind the station chunk), then a 5×5 ticket centred on the middle carriage keeps the line under
 * the train entity-ticking while it runs. Nothing else is kept loaded per car.
 *
 * <p>Physical rule: before each step it checks the rail under it and {@value #LOOKAHEAD} blocks
 * ahead. A missing rail or a block in the corridor stops the train and announces once; it waits
 * where it is, lets riders dismount, and resumes when the track is clear. It never clips through.
 */
public class TrainLocomotive extends TrainCar {
    /**
     * Ticket radius used by {@link #holdChunks}/{@link #releaseChunks} (the service's spawn ticket
     * and the locomotive's initial ownership). A four-carriage consist is 64 blocks long, so the
     * 9×9 square is the smallest one that covers the wagons spawned behind the station.
     */
    public static final int SPAWN_TICKET_RADIUS = 4;
    /**
     * Ticket radius that follows the consist in service, centred on the middle carriage.
     *
     * <p>It must equal the spawn radius: a chunk is entity-ticking only when its ticket level is
     * at most 31, and a radius-{@code r} ticket ticks chunks up to {@code r - 2} away from its
     * centre. A radius of 2 therefore ticked only the middle chunk itself and left the locomotive
     * and the wagons in the neighbouring chunks frozen.
     */
    private static final int CRUISE_TICKET_RADIUS = SPAWN_TICKET_RADIUS;
    /** Blocks checked ahead of the locomotive each step. */
    private static final int LOOKAHEAD = 3;
    /** Distance over which the train slows into a station, in blocks. */
    private static final int SLOW_RADIUS = 24;
    /** How long the consist lingers at the destination after putting riders ashore. */
    private static final int LINGER_TICKS = 200;
    /** How long a moving consist with no riders may run before it is removed. */
    private static final int EMPTY_GRACE_TICKS = 200;

    /**
     * The 5×5 ticket uses vanilla's {@code DRAGON} type for the same reason the ship does: it is
     * registered, has no timeout, and both loads and simulates the chunks it keeps.
     */
    private static final TicketType TRAIN_TICKET = TicketType.DRAGON;

    private @Nullable RailLine line;
    private int direction;
    private boolean sailing;
    private boolean arrived;
    private boolean stopped;
    private int ticksUntilDeparture;
    private long departureClock;
    private int lingerTicks;
    private int emptyTicks;
    private int trainTicks;
    private double speed = 1.2;
    private double travelled;
    private int wagonCount;
    private @Nullable ChunkPos ticketCenter;
    private int ticketRadius = SPAWN_TICKET_RADIUS;
    private final TrainTrail trail = new TrainTrail();
    private final List<TrainWagon> wagons = new ArrayList<>();

    public TrainLocomotive(EntityType<? extends TrainLocomotive> type, Level level) {
        super(type, level);
    }

    /** @return the locomotive body length (engine nose + cab), in blocks. */
    @Override
    public double carLength() {
        return 16.0;
    }

    /**
     * Wires a freshly spawned locomotive to its line and timetable. The station chunk ticket is
     * already held by the service.
     *
     * @param line           the service
     * @param direction      0 = a to b, 1 = b to a
     * @param countdownTicks ticks until departure (0 = leave at once)
     * @param departureAt    absolute clock tick this voyage was scheduled to leave
     * @param blocksPerTick  configured speed
     * @param wagonCount     number of coupled wagons (for the spawn-time trail seed)
     */
    public void configure(RailLine line, int direction, int countdownTicks, long departureAt,
                          double blocksPerTick, int wagonCount) {
        this.line = line;
        this.direction = direction == 0 ? 0 : 1;
        this.trail.setWrapWidth(line.wrapWidth());
        this.ticksUntilDeparture = Math.max(0, countdownTicks);
        this.departureClock = departureAt;
        this.speed = Math.max(0.1, blocksPerTick);
        this.wagonCount = TrainTimetable.wagons(wagonCount);
        this.travelled = 0.0;
        this.configure(this, 0);
        this.seedTrail();
        RailSample start = line.sample(this.direction, 0.0);
        this.setPos(start.x(), start.y() + 0.1, start.z());
        this.setYRot(start.yaw());
        // Own the consist's 5x5 ticket from the first tick instead of adopting the service's
        // station ticket: the service releases that hold as soon as the consist is spawned, and a
        // shared ticket would take the chunk (and the consist) with it.
        this.ticketCenter = null;
        this.ticketRadius = SPAWN_TICKET_RADIUS;
        if (this.level() instanceof ServerLevel serverLevel) {
            this.updateTickets(serverLevel);
        }
    }

    /** @return whether the boarding window is still open. */
    public boolean isBoardingOpen() {
        return !this.sailing && !this.arrived && this.ticksUntilDeparture > 0;
    }

    /** @return whether the consist is under way. */
    public boolean isSailing() {
        return this.sailing;
    }

    /** @return whether the consist has reached its destination and is lingering. */
    public boolean isArrived() {
        return this.arrived;
    }

    /** @return whether the consist is held by a missing rail or a blocked corridor. */
    public boolean isStopped() {
        return this.stopped;
    }

    /** @return ticks this locomotive's server tick has actually run (the service watchdog's signal). */
    public int trainTicks() {
        return this.trainTicks;
    }

    /** @return the absolute clock tick this voyage was scheduled to depart. */
    public long departureClock() {
        return this.departureClock;
    }

    /** @return the line this locomotive serves, or {@code null} before {@link #configure}. */
    public @Nullable RailLine line() {
        return this.line;
    }

    /** @return the direction (0 = a to b, 1 = b to a). */
    public int direction() {
        return this.direction;
    }

    /** @return ticks left on the boarding countdown (0 = departed). */
    public int ticksUntilDeparture() {
        return this.ticksUntilDeparture;
    }

    /** @return distance travelled from the origin station, in blocks. */
    public double travelled() {
        return this.travelled;
    }

    /** @return the destination station, or {@code null} before {@link #configure}. */
    public @Nullable RailStation destinationStation() {
        return this.line == null ? null : this.line.destination(this.direction);
    }

    /** @return the {@link com.sanchous98.eldritchhorror.world.rail.TrainService} key. */
    public String serviceKey() {
        return (this.line == null ? "?" : this.line.id()) + ":" + this.direction;
    }

    /** @return a one-line state label for {@code /eh train}. */
    public String stateLabel() {
        if (this.arrived) {
            return "arrived";
        }
        if (this.sailing) {
            return this.stopped ? "stopped" : "departed";
        }
        return this.isBoardingOpen() ? "boarding" : "docked";
    }

    /** @return the rail block the locomotive is over (for diagnostics). */
    public @Nullable RailNode currentRail() {
        return this.line == null ? null
                : this.line.nodeAt(this.direction, this.travelled);
    }

    /** @return the rail block {@value #LOOKAHEAD} blocks ahead (for the probe's obstruction test). */
    public @Nullable RailNode nextRail() {
        if (this.line == null) {
            return null;
        }
        return this.line.nodeAt(this.direction,
                Math.min(this.line.length(), this.travelled + LOOKAHEAD));
    }

    /** @return the number of riders in every car of this consist. */
    public int riderCount() {
        int total = this.getPassengers().size();
        for (TrainWagon wagon : this.wagons) {
            total += wagon.getPassengers().size();
        }
        return total;
    }

    /** @return the coupled wagons (immutable snapshot). */
    public List<TrainWagon> wagons() {
        return List.copyOf(this.wagons);
    }

    /** Couples a spawned wagon to this locomotive. */
    public void attachWagon(TrainWagon wagon) {
        this.wagons.add(wagon);
    }

    /**
     * @param behind blocks behind the locomotive
     * @return the trail sample for a wagon, or {@code null} before the trail is seeded
     */
    public @Nullable RailSample trailBehind(double behind) {
        return this.trail.sampleBehind(behind);
    }

    /** Holds the wide spawn ticket around a chunk (used by the service before spawning). */
    public static void holdChunks(ServerLevel level, ChunkPos pos) {
        holdChunks(level, pos, SPAWN_TICKET_RADIUS);
    }

    /** Releases the ticket {@link #holdChunks} placed. */
    public static void releaseChunks(ServerLevel level, ChunkPos pos) {
        releaseChunks(level, pos, SPAWN_TICKET_RADIUS);
    }

    private static void holdChunks(ServerLevel level, ChunkPos pos, int radius) {
        level.getChunkSource().addTicketWithRadius(TRAIN_TICKET, pos, radius);
    }

    private static void releaseChunks(ServerLevel level, ChunkPos pos, int radius) {
        level.getChunkSource().removeTicketWithRadius(TRAIN_TICKET, pos, radius);
    }

    /** Resolves a due boarding window: an empty consist despawns, one with riders departs. */
    public void forceDue() {
        if (this.sailing || this.arrived || this.ticksUntilDeparture > 0) {
            return;
        }
        if (this.riderCount() == 0) {
            this.discard();
            return;
        }
        this.sailing = true;
        this.announceRiders("message.eldritch_horror.train.departing");
        EldritchHorror.LOGGER.info("rail: consist {} departs with {} riders",
                this.serviceKey(), this.riderCount());
    }

    @Override
    public void tick() {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            super.tick();
            return;
        }
        this.trainTicks++;
        if (this.arrived) {
            if (--this.lingerTicks <= 0) {
                this.discard();
                return;
            }
        } else if (this.sailing) {
            if (this.riderCount() == 0) {
                if (++this.emptyTicks > EMPTY_GRACE_TICKS) {
                    this.discard();
                    return;
                }
            } else {
                this.emptyTicks = 0;
            }
        } else {
            if (this.ticksUntilDeparture > 0) {
                this.ticksUntilDeparture--;
            }
            if (this.ticksUntilDeparture <= 0) {
                this.forceDue();
                if (this.isRemoved()) {
                    return;
                }
            }
        }
        this.setDeltaMovement(Vec3.ZERO);
        super.tick();
        if (this.sailing) {
            this.advance(serverLevel);
        }
        this.updateTickets(serverLevel);
    }

    private void seedTrail() {
        RailLine rail = this.line;
        if (rail == null) {
            return;
        }
        this.trail.clear();
        RailSample start = rail.sample(this.direction, 0.0);
        RailSample ahead = rail.sample(this.direction, Math.min(1.0, rail.length()));
        double dx = rail.wrapDelta(start.x(), ahead.x());
        double dz = ahead.z() - start.z();
        double length = Math.sqrt(dx * dx + dz * dz);
        if (length < 1.0E-6) {
            dx = 0.0;
            dz = 1.0;
        } else {
            dx /= length;
            dz /= length;
        }
        int cars = (int) Math.ceil(TrainTimetable.consistLength(this.wagonCount)
                + TrainTimetable.SPACING);
        for (int i = cars; i >= 1; i--) {
            this.trail.append(start.x() - dx * i, start.y(), start.z() - dz * i,
                    start.yaw(), -i);
        }
        this.trail.append(start.x(), start.y(), start.z(), start.yaw(), 0.0);
    }

    private void advance(ServerLevel level) {
        RailLine rail = this.line;
        if (rail == null) {
            this.discard();
            return;
        }
        double remaining = rail.length() - this.travelled;
        if (remaining <= 1.0E-4) {
            this.arrive(level);
            return;
        }
        if (!rail.hasCorridor()) {
            // No stamped corridor: a sparse planned polyline has no rail block at every node, and
            // the ground is whatever the generator produced. Only wait for the ticket to settle.
            BlockPos here = BlockPos.containing(this.getX(), this.getY(), this.getZ());
            if (!level.isLoaded(here)) {
                return;
            }
        } else {
            RailNode current = rail.nodeAt(this.direction, this.travelled);
            RailNode ahead = rail.nodeAt(this.direction,
                    Math.min(rail.length(), this.travelled + LOOKAHEAD));
            if (!level.isLoaded(new BlockPos(current.x(), current.y(), current.z()))) {
                return; // the ticket is still settling; wait without accusing the line
            }
            String obstruction = obstruction(level, current);
            if (obstruction == null) {
                obstruction = obstruction(level, ahead);
            }
            if (obstruction != null) {
                if ("unloaded".equals(obstruction)) {
                    return;
                }
                this.stop(obstruction);
                return;
            }
        }
        if (this.stopped) {
            this.stopped = false;
            this.announceRiders("message.eldritch_horror.train.resumed");
            EldritchHorror.LOGGER.info("rail: consist {} resumes", this.serviceKey());
        }
        double step = this.speed;
        if (remaining < SLOW_RADIUS) {
            step *= Math.max(0.2, remaining / SLOW_RADIUS);
        }
        step = Math.max(0.05, Math.min(step, remaining));
        this.travelled = Math.min(rail.length(), this.travelled + step);
        RailSample sample = rail.sample(this.direction, this.travelled);
        this.setPos(sample.x(), sample.y() + 0.1, sample.z());
        this.setYRot(sample.yaw());
        this.setDeltaMovement(Vec3.ZERO);
        this.trail.append(sample.x(), sample.y(), sample.z(), sample.yaw(), this.travelled);
        if (this.travelled >= rail.length() - 1.0E-4) {
            this.arrive(level);
        }
    }

    /**
     * @return {@code null} when the rail and the space above it are clear, otherwise a short
     *         description of the obstruction
     */
    private static @Nullable String obstruction(ServerLevel level, RailNode node) {
        BlockPos pos = new BlockPos(node.x(), node.y(), node.z());
        if (!level.isLoaded(pos)) {
            return "unloaded";
        }
        BlockState rail = level.getBlockState(pos);
        if (!rail.is(BlockTags.RAILS)) {
            return "missing rail (" + rail.getBlock().getName().getString() + ")";
        }
        for (int dy = 1; dy <= 2; dy++) {
            BlockPos above = pos.above(dy);
            BlockState state = level.getBlockState(above);
            if (state.getCollisionShape(level, above).isEmpty()) {
                continue;
            }
            if (state.is(BlockTags.LEAVES) || state.is(BlockTags.LOGS)
                    || state.is(BlockTags.REPLACEABLE_BY_TREES)) {
                // Trees from a neighbouring chunk can grow back over a finished line. The crew
                // clears foliage and undergrowth; a placed solid block still stops the train.
                level.setBlock(above, Blocks.AIR.defaultBlockState(), 3);
                continue;
            }
            return "blocked by " + state.getBlock().getName().getString();
        }
        return null;
    }

    private void stop(String reason) {
        if (this.stopped) {
            return;
        }
        this.stopped = true;
        this.announceRiders("message.eldritch_horror.train.stopped");
        EldritchHorror.LOGGER.info("rail: consist {} stopped: {}", this.serviceKey(), reason);
    }

    private void arrive(ServerLevel level) {
        if (this.arrived) {
            return;
        }
        this.arrived = true;
        this.sailing = false;
        this.stopped = false;
        this.lingerTicks = LINGER_TICKS;
        RailStation destination = this.destinationStation();
        if (destination == null) {
            this.discard();
            return;
        }
        this.announceRiders("message.eldritch_horror.train.arrived");
        EldritchHorror.LOGGER.info("rail: consist {} arrived at {} with {} riders",
                this.serviceKey(), destination.cityId(), this.riderCount());
        for (TrainCar car : this.cars()) {
            List<Entity> riders = new ArrayList<>(car.getPassengers());
            for (Entity rider : riders) {
                rider.stopRiding();
                if (rider instanceof ServerPlayer player) {
                    player.teleportTo(level, destination.boardX() + 0.5, destination.y(),
                            destination.boardZ() + 0.5, java.util.Set.of(), player.getYRot(),
                            player.getXRot(), false);
                    player.sendSystemMessage(Component.translatable(
                            "message.eldritch_horror.train.disembarked",
                            destination.cityName()), false);
                }
            }
        }
    }

    /**
     * Puts every rider back on the platform of the boarding station after a service failed while
     * the consist could not tick at all (a safety net; the two-phase spawn avoids this in practice).
     */
    public void evacuate(ServerLevel level) {
        RailLine rail = this.line;
        RailStation station = rail == null ? null : rail.origin(this.direction);
        for (TrainCar car : this.cars()) {
            for (Entity rider : new ArrayList<>(car.getPassengers())) {
                rider.stopRiding();
                if (!(rider instanceof ServerPlayer player)) {
                    continue;
                }
                if (station != null) {
                    player.teleportTo(level, station.boardX() + 0.5, station.y(),
                            station.boardZ() + 0.5, java.util.Set.of(), player.getYRot(),
                            player.getXRot(), false);
                    int fare = Math.max(0, com.sanchous98.eldritchhorror.core.ModConfig
                            .TRAIN_FARE.get());
                    if (fare > 0) {
                        TrainCar.giveObols(player, fare);
                    }
                    player.sendSystemMessage(Component.translatable(
                            "message.eldritch_horror.train.stranded", station.cityName()), false);
                } else {
                    player.sendSystemMessage(Component.translatable(
                            "message.eldritch_horror.train.stranded", "?"), false);
                }
            }
        }
    }

    private List<TrainCar> cars() {
        List<TrainCar> all = new ArrayList<>(this.wagons.size() + 1);
        all.add(this);
        all.addAll(this.wagons);
        return all;
    }

    private void announceRiders(String key) {
        RailStation destination = this.destinationStation();
        Component message = Component.translatable(key,
                destination == null ? "?" : destination.cityName());
        for (TrainCar car : this.cars()) {
            for (Entity passenger : car.getPassengers()) {
                if (passenger instanceof ServerPlayer player) {
                    player.sendSystemMessage(message, false);
                }
            }
        }
    }

    /** Re-adds the entity-ticking ticket after a stalled service check. */
    public void reassertTickets(ServerLevel level) {
        if (this.ticketCenter != null) {
            releaseChunks(level, this.ticketCenter, this.ticketRadius);
        }
        this.ticketCenter = null;
        this.updateTickets(level);
    }

    private void updateTickets(ServerLevel level) {
        ChunkPos current = this.consistCenter();
        if (current.equals(this.ticketCenter)) {
            return;
        }
        if (this.ticketCenter != null) {
            releaseChunks(level, this.ticketCenter, this.ticketRadius);
        }
        holdChunks(level, current, CRUISE_TICKET_RADIUS);
        this.ticketCenter = current;
        this.ticketRadius = CRUISE_TICKET_RADIUS;
    }

    /** @return the chunk under the middle carriage, which is where the cruise ticket belongs. */
    private ChunkPos consistCenter() {
        RailSample middle = this.trail.sampleBehind(
                TrainTimetable.consistLength(this.wagonCount) * 0.5);
        if (middle == null) {
            return this.chunkPosition();
        }
        return ChunkPos.containing(BlockPos.containing(middle.x(), middle.y(), middle.z()));
    }

    @Override
    public void remove(Entity.RemovalReason reason) {
        if (this.level() instanceof ServerLevel serverLevel && this.ticketCenter != null) {
            releaseChunks(serverLevel, this.ticketCenter, this.ticketRadius);
            this.ticketCenter = null;
        }
        List<TrainWagon> copy = new ArrayList<>(this.wagons);
        this.wagons.clear();
        for (TrainWagon wagon : copy) {
            wagon.detachAndDiscard();
        }
        super.remove(reason);
    }

    /** @return the block under the locomotive as {@code rail}/{@code planned} or {@code blocked (...)}. */
    public String railStateLabel(ServerLevel level) {
        RailLine rail = this.line;
        RailNode node = this.currentRail();
        if (rail == null) {
            return "no line";
        }
        if (!rail.hasCorridor()) {
            return "planned (no corridor)";
        }
        if (node == null) {
            return "no line";
        }
        String obstruction = obstruction(level, node);
        return obstruction == null ? "rail" : "blocked (" + obstruction + ")";
    }
}
