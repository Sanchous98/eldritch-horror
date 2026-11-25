package com.sanchous98.eldritchhorror.entity;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.core.ModConfig;
import com.sanchous98.eldritchhorror.world.EarthMap;
import com.sanchous98.eldritchhorror.world.transport.Port;
import com.sanchous98.eldritchhorror.world.transport.SeaPoint;
import com.sanchous98.eldritchhorror.world.transport.ShippingLine;
import com.sanchous98.eldritchhorror.world.transport.TransportMath;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.Prediction;
import org.jspecify.annotations.Nullable;

/**
 * The coastal packet: a large, scheduled passenger ship ({@code design/30-transport.md}, stage 1).
 *
 * <p>It is a boat-derived hull with {@value #SEATS} deck seats, driven by the server along a cached
 * {@link ShippingLine} at {@code shipSpeed} blocks per tick. Players cannot steer it; the only way
 * aboard is the fare interaction while it is docked in the boarding window. It carries its own
 * chunk tickets (a 5x5 entity-ticking square, one ticket moved with the hull) so the ocean ahead
 * loads and the ocean behind unloads; no lane-wide loading ever happens.
 *
 * <p>Lifecycle: spawned docked by {@link com.sanchous98.eldritchhorror.world.transport.ShipService}
 * at boarding open; departs on the schedule (despawns instead if nobody boarded); on arrival it puts
 * riders down at the destination shore and lingers briefly before despawning. A rider who logs off
 * mid-voyage is recorded by {@code ShipRiders} and put down at the destination on their next login.
 *
 * <p>Art: the client renders the full 17.75 x 5.5 block hull from
 * {@code client/model/CoastalPacketModel}, with a walkable deck, bulwark and cap rail, a cabin
 * with windows and a railed poop deck, a mast with square sails, a funnel and lanterns.
 */
public class CoastalPacket extends AbstractBoat {
    /** Deck seats: twelve on the two railing benches, four under the cabin roof. */
    public static final int SEATS = 14;
    private static final int TICKET_RADIUS = 2;
    /** How long the empty hull stays at the destination after putting riders ashore. */
    private static final int LINGER_TICKS = 200;
    /** How long a voyage may run with no riders aboard before the hull is removed. */
    private static final int EMPTY_GRACE_TICKS = 200;
    /**
     * Rider feet above the hull origin. {@code CoastalPacketRenderer} lifts the whole model 2.9
     * blocks to clear the {@code AbstractBoat} buoyancy line, so this is that lift plus 0.33:
     * the deck top is 3.65 blocks above the origin, the rider sits 0.42 into it (legs hidden
     * inside the hull, as in a vanilla boat) and the eye lands about 1.2 blocks above the
     * planking, just over the cap rail.
     */
    private static final double RIDE_HEIGHT = 3.23;
    /**
     * Seat offsets in the hull's local frame: {x, z}, in blocks. The model's bow is +Z here.
     * Rows 1-10 are the benches along the port and starboard bulwarks, rows 11-14 sit inside the
     * cabin against its side benches.
     */
    private static final double[][] SEAT_OFFSETS = {
            {-2.06, 3.25}, {2.06, 3.25},
            {-2.06, 1.625}, {2.06, 1.625},
            {-2.06, 0.0}, {2.06, 0.0},
            {-2.06, -1.625}, {2.06, -1.625},
            {-2.06, -3.25}, {2.06, -3.25},
            {-1.55, -4.4}, {1.55, -4.4},
            {-1.55, -6.0}, {1.55, -6.0},
    };

    /**
     * The hull's chunk ticket. This deliberately reuses vanilla's {@code DRAGON} type: it is
     * registered, has no timeout, loads and <b>simulates</b> ({@code FLAG_LOADING|FLAG_SIMULATION})
     * and does not persist. A custom type with the same flags is functionally identical, but an
     * <b>unregistered</b> type is not: chunks kept by it are loaded but do not stay entity-ticking,
     * which froze the countdown and left hulls "boarding" forever.
     */
    private static final TicketType SHIP_TICKET = TicketType.DRAGON;

    private @Nullable ShippingLine line;
    private int direction;
    private int waypointIndex;
    private boolean sailing;
    private boolean arrived;
    private int ticksUntilDeparture;
    private long departureClock;
    private int lingerTicks;
    private int emptyTicks;
    private double speed = 1.5;
    private @Nullable ChunkPos ticketCenter;
    /** Our own tick counter: {@code Entity.tickCount} is not advanced on every server tick path. */
    private int hullTicks;

    public CoastalPacket(EntityType<? extends CoastalPacket> type, Level level) {
        super(type, level, () -> Items.AIR);
    }

    /**
     * Wires a freshly spawned hull to its line and timetable. Called by
     * {@link com.sanchous98.eldritchhorror.world.transport.ShipService} immediately after
     * {@code addFreshEntity}; the port chunk ticket is already held.
     *
     * @param line                the service
     * @param direction           0 = a to b, 1 = b to a
     * @param countdownTicks      ticks until the gangway closes (0 = depart at once)
     * @param blocksPerTick       configured speed
     * @param initialTicket       chunk already ticketed by the service
     */
    public void configure(ShippingLine line, int direction, int countdownTicks, long departureAt,
                          double blocksPerTick, ChunkPos initialTicket) {
        this.line = line;
        this.direction = direction;
        this.ticksUntilDeparture = Math.max(0, countdownTicks);
        this.departureClock = departureAt;
        this.speed = Math.max(0.1, blocksPerTick);
        this.waypointIndex = 0;
        this.ticketCenter = initialTicket;
        this.faceNextWaypoint();
    }

    /** @return whether the gangway is still open (docked, not yet departed). */
    public boolean isBoardingOpen() {
        return !this.sailing && !this.arrived && this.ticksUntilDeparture > 0;
    }

    /** @return whether the hull is under way. */
    public boolean isSailing() {
        return this.sailing;
    }

    /** @return whether the hull has reached its destination and is lingering. */
    public boolean isArrived() {
        return this.arrived;
    }

    /** @return ticks this hull's server tick has actually run (the service watchdog's signal). */
    public int hullTicks() {
        return this.hullTicks;
    }

    /** @return the absolute clock tick this voyage was scheduled to depart. */
    public long departureClock() {
        return this.departureClock;
    }

    /** @return the line this hull serves, or {@code null} before {@link #configure}. */
    public @Nullable ShippingLine line() {
        return this.line;
    }

    /** @return the direction (0 = a to b, 1 = b to a). */
    public int direction() {
        return this.direction;
    }

    /** @return the harbour this voyage ends at, or {@code null} before {@link #configure}. */
    public @Nullable Port destinationPort() {
        return this.line == null ? null : this.line.destination(this.direction);
    }

    /** @return the harbour this voyage left, or {@code null} before {@link #configure}. */
    public @Nullable Port originPort() {
        return this.line == null ? null : this.line.origin(this.direction);
    }

    /** @return a one-line state label for {@code /eh ship}. */
    public String stateLabel() {
        if (this.arrived) {
            return "arrived";
        }
        if (this.sailing) {
            return "departed";
        }
        return this.isBoardingOpen() ? "boarding" : "docked";
    }

    /** @return ticks left on the gangway countdown (0 = departed). */
    public int ticksUntilDeparture() {
        return this.ticksUntilDeparture;
    }

    /** @return the {@link ShipService} key suffix, for diagnostics. */
    public String serviceKey() {
        return (this.line == null ? "?" : this.line.id()) + ":" + this.direction;
    }

    /**
     * Holds the 5x5 ticket around a chunk. Used by the service before spawning, so the hull never
     * appears in an unloaded ocean.
     */
    public static void holdChunks(ServerLevel level, ChunkPos pos) {
        level.getChunkSource().addTicketWithRadius(SHIP_TICKET, pos, TICKET_RADIUS);
    }

    /** Releases the ticket {@link #holdChunks} placed. */
    public static void releaseChunks(ServerLevel level, ChunkPos pos) {
        level.getChunkSource().removeTicketWithRadius(SHIP_TICKET, pos, TICKET_RADIUS);
    }

    // ------------------------------------------------------------------ boarding

    @Override
    public InteractionResult interact(Player player, InteractionHand hand, Vec3 location) {
        if (this.level().isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.PASS;
        }
        return tryBoard(serverPlayer) ? InteractionResult.SUCCESS : InteractionResult.FAIL;
    }

    /**
     * The one boarding path, shared by right-click and the {@code /eh ship board} debug hook: checks
     * the window, a free seat and the fare, charges it, then seats the player.
     *
     * @param player the player trying to board
     * @return true when the player is now a passenger
     */
    public boolean tryBoard(ServerPlayer player) {
        if (!this.isBoardingOpen()) {
            player.sendSystemMessage(
                    Component.translatable("message.eldritch_horror.ship.not_boarding"), true);
            return false;
        }
        if (this.getPassengers().size() >= this.getMaxPassengers()) {
            player.sendSystemMessage(Component.translatable("message.eldritch_horror.ship.full"), true);
            return false;
        }
        int fare = Math.max(0, ModConfig.SHIP_FARE.get());
        int carried = countObols(player);
        if (!TransportMath.canAfford(carried, fare)) {
            player.sendSystemMessage(
                    Component.translatable("message.eldritch_horror.ship.no_fare", fare), true);
            return false;
        }
        if (fare > 0) {
            takeObols(player, fare);
        }
        if (!player.startRiding(this, true, true)) {
            if (fare > 0) {
                giveObols(player, fare);
            }
            player.sendSystemMessage(
                    Component.translatable("message.eldritch_horror.ship.not_boarding"), true);
            return false;
        }
        player.sendSystemMessage(
                Component.translatable("message.eldritch_horror.ship.boarded", fare), true);
        return true;
    }

    /**
     * Resolves a due gangway: an empty hull despawns (no empty service streams the ocean), a hull
     * with riders departs. The service watchdog calls this too, so a hull whose chunk tick was
     * delayed still leaves on the first tick it can run instead of lingering "boarding" forever.
     */
    public void forceDue() {
        if (this.sailing || this.arrived || this.ticksUntilDeparture > 0) {
            return;
        }
        if (this.getPassengers().isEmpty()) {
            this.discard();
            return;
        }
        this.sailing = true;
        this.announceRiders("message.eldritch_horror.ship.departing");
        EldritchHorror.LOGGER.info("transport: packet {} departs with {} riders",
                this.serviceKey(), this.getPassengers().size());
    }

    private static Item obol() {
        return BuiltInRegistries.ITEM.getValue(EldritchHorror.id("black_obol"));
    }

    private static int countObols(Player player) {
        Item obol = obol();
        if (obol == Items.AIR) {
            return 0;
        }
        int total = 0;
        for (ItemStack stack : player.getInventory()) {
            if (stack.getItem() == obol) {
                total += stack.getCount();
            }
        }
        return total;
    }

    private static void takeObols(Player player, int count) {
        Item obol = obol();
        int left = count;
        for (ItemStack stack : player.getInventory()) {
            if (left <= 0) {
                break;
            }
            if (stack.getItem() != obol) {
                continue;
            }
            int taken = Math.min(left, stack.getCount());
            stack.shrink(taken);
            left -= taken;
        }
    }

    private static void giveObols(Player player, int count) {
        Item obol = obol();
        if (obol != Items.AIR) {
            player.getInventory().placeItemBackInInventory(new ItemStack(obol, count),
                    Prediction.SERVER_ONLY);
        }
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return this.getPassengers().size() < this.getMaxPassengers();
    }

    @Override
    protected int getMaxPassengers() {
        return Math.clamp(ModConfig.SHIP_SEATS.get(), 1, SEATS);
    }

    /** The packet is boarded only through {@link #tryBoard}; never by walking into it. */
    @Override
    public boolean hasEnoughSpaceFor(Entity entity) {
        return false;
    }

    @Override
    protected Vec3 getPassengerAttachmentPoint(Entity passenger, EntityDimensions dimensions,
                                               float scale) {
        int index = Math.max(0, this.getPassengers().indexOf(passenger));
        double[] seat = SEAT_OFFSETS[Math.min(index, SEAT_OFFSETS.length - 1)];
        return new Vec3(seat[0], RIDE_HEIGHT, seat[1])
                .yRot(-this.getYRot() * (float) (Math.PI / 180.0));
    }

    @Override
    protected double rideHeight(EntityDimensions dimensions) {
        return RIDE_HEIGHT;
    }

    // ------------------------------------------------------------------ lifecycle

    @Override
    public void tick() {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            super.tick();
            return;
        }
        this.hullTicks++;
        if (this.arrived) {
            if (--this.lingerTicks <= 0) {
                this.discard();
                return;
            }
        } else if (this.sailing) {
            if (this.getPassengers().isEmpty()) {
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
        if (this.isUnderWater()) {
            // A harbour can be a one-block shelf; a packet is not a rowboat, so lift the deck back
            // to the waterline instead of letting the hull sit submerged. The water surface is the
            // sea level; a heightmap would follow a cliff or structure above the port (Lima).
            this.setPos(this.getX(), serverLevel.getSeaLevel() + 0.85, this.getZ());
            this.setDeltaMovement(Vec3.ZERO);
        }
        if (this.sailing) {
            this.advance(serverLevel);
        }
        if (this.getY() < serverLevel.getSeaLevel() - 4.0) {
            this.setPos(this.getX(), serverLevel.getSeaLevel() - 1.0, this.getZ());
        }
        // Move the ticket after the hull: the chunk the hull just entered becomes entity-ticking
        // before the entity manager's end-of-tick pass.
        this.updateTickets(serverLevel);
    }

    private void advance(ServerLevel level) {
        ShippingLine shipping = this.line;
        if (shipping == null) {
            this.discard();
            return;
        }
        double remaining = this.speed;
        while (remaining > 0.0) {
            if (this.waypointIndex >= shipping.route().size()) {
                this.arrive(level);
                return;
            }
            SeaPoint target = shipping.waypoint(this.direction, this.waypointIndex);
            // Longitude wraps: steer the short way round the seam.
            double dx = EarthMap.wrapDelta(this.getX(), target.x() + 0.5);
            double dz = target.z() + 0.5 - this.getZ();
            double distance = Math.sqrt(dx * dx + dz * dz);
            if (distance < 1.0E-4) {
                this.waypointIndex++;
                continue;
            }
            this.setYRot((float) Math.toDegrees(Math.atan2(-dx, dz)));
            if (distance <= remaining) {
                this.setPos(target.x() + 0.5, this.getY(), target.z() + 0.5);
                remaining -= distance;
                this.waypointIndex++;
            } else {
                this.setPos(EarthMap.wrapBlockX(this.getX() + dx / distance * remaining), this.getY(),
                        this.getZ() + dz / distance * remaining);
                remaining = 0.0;
            }
        }
    }

    private void faceNextWaypoint() {
        ShippingLine shipping = this.line;
        if (shipping == null) {
            return;
        }
        int index = Math.min(1, shipping.route().size() - 1);
        SeaPoint target = shipping.waypoint(this.direction, index);
        // Longitude wraps: face the short way round the seam.
        double dx = EarthMap.wrapDelta(this.getX(), target.x() + 0.5);
        this.setYRot((float) Math.toDegrees(
                Math.atan2(-dx, target.z() + 0.5 - this.getZ())));
    }

    private void arrive(ServerLevel level) {
        if (this.arrived) {
            return;
        }
        this.arrived = true;
        this.sailing = false;
        this.lingerTicks = LINGER_TICKS;
        Port destination = this.destinationPort();
        if (destination == null) {
            this.discard();
            return;
        }
        this.announceRiders("message.eldritch_horror.ship.arrived");
        List<Entity> riders = new ArrayList<>(this.getPassengers());
        EldritchHorror.LOGGER.info("transport: packet {} arrived at {} with {} riders",
                this.serviceKey(), destination.cityId(), riders.size());
        for (Entity rider : riders) {
            rider.stopRiding();
            if (rider instanceof ServerPlayer player) {
                this.putAshore(level, player, destination);
            }
        }
    }

    private void putAshore(ServerLevel level, ServerPlayer player, Port port) {
        int x = port.shoreX();
        int z = port.shoreZ();
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        player.teleportTo(x + 0.5, Math.max(y, level.getSeaLevel() + 1), z + 0.5);
    }

    private void announceRiders(String key) {
        Port destination = this.destinationPort();
        Component message = Component.translatable(key,
                destination == null ? "?" : destination.cityName());
        for (Entity passenger : this.getPassengers()) {
            if (passenger instanceof ServerPlayer player) {
                player.sendSystemMessage(message, false);
            }
        }
    }

    /** Re-adds the entity-ticking ticket after a stalled service check. */
    public void reassertTickets(ServerLevel level) {
        this.ticketCenter = null;
        this.updateTickets(level);
    }

    private void updateTickets(ServerLevel level) {
        ChunkPos current = this.chunkPosition();
        if (current.equals(this.ticketCenter)) {
            return;
        }
        if (this.ticketCenter != null) {
            releaseChunks(level, this.ticketCenter);
        }
        holdChunks(level, current);
        this.ticketCenter = current;
    }

    // ------------------------------------------------------------------ persistence

    /** Never saved: a voyage is process state, not world state. A restart cancels the service. */
    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    public void remove(Entity.RemovalReason reason) {
        if (this.level() instanceof ServerLevel serverLevel && this.ticketCenter != null) {
            releaseChunks(serverLevel, this.ticketCenter);
            this.ticketCenter = null;
        }
        super.remove(reason);
    }

    /**
     * A scheduled packet is public transport, not a boat item: a player punching it, an explosion
     * or fire must not sink it mid-voyage. Vanilla {@code VehicleEntity.hurtServer} destroys the
     * hull after about 4 HP of hits (instantly in creative); this override acknowledges the hit and
     * applies no damage, exactly like a consist's {@code TrainCar}.
     */
    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        return true;
    }

    @Override
    public void destroy(ServerLevel level, Item dropItem) {
        // No item drop: the packet is public transport, not a possession.
        this.discard();
    }

    /** @return the current block position, for diagnostics. */
    public BlockPos packetPos() {
        return this.blockPosition();
    }
}
