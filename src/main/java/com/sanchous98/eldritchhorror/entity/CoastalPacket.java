package com.sanchous98.eldritchhorror.entity;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.core.ModConfig;
import com.sanchous98.eldritchhorror.registry.ModEntities;
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
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.Prediction;
import org.jspecify.annotations.Nullable;

/**
 * The liner: a 91-block scheduled ocean vessel, a place that moves ({@code design/30-transport.md}).
 *
 * <p>This is deliberately <b>not</b> a boat. It is a plain {@link Entity} with a scripted course;
 * there is no buoyancy, no paddling and no item drop. The hull's own AABB is the <b>deck floor</b>
 * (its top face is the walkable main deck, 7.25 blocks above the sea), computed from the current
 * heading because the deck is 91 x 18 and an entity box cannot rotate. A handful of invisible
 * {@link ShipCollisionPart}s carry the solid deckhouse and bridge above the deck, so there is no
 * 90 x 10 x 20 roof masquerading as a deck.
 *
 * <p>Walkability: the deck and the cabin roofs are real collision surfaces (players collide with
 * the hull and the parts). Players standing on them are carried with the hull each tick, keeping
 * their hull-local position while the vessel moves and turns. Boarding still goes through the one
 * fare interaction ({@link #tryBoard}) from the pier/gangway; riders are attached to
 * {@link ShipSeats} and never fall through the planking.
 *
 * <p>Service semantics are unchanged: {@code ShipService} opens the boarding window, spawns the
 * hull at the harbour, and the hull owns its ticket ({@link ShipGeometry#TICKET_RADIUS} chunks,
 * covering the whole 91-block vessel), departure, movement, arrival and despawn. It is never
 * saved, never pushable, never damageable, and never ejected mid-voyage.
 */
public class CoastalPacket extends Entity {
    /** Passenger capacity of the physical deck. */
    public static final int SEATS = ShipGeometry.SEATS;
    /** How long the empty hull stays at the destination after putting riders ashore. */
    private static final int LINGER_TICKS = 200;
    /** How long a voyage may run with no riders aboard before the hull is removed. */
    private static final int EMPTY_GRACE_TICKS = 200;
    /** A walker further than this from the deck surface is not considered aboard. */
    private static final double DECK_CONTACT = 0.2;
    /** A large vessel swings her head slowly: degrees per tick while under way. */
    private static final float TURN_RATE_DEGREES = 4.0F;

    /** Rider attachment height above the hull origin (deck minus the seated sink, plus the player attachment). */
    public static final double RIDE_HEIGHT = ShipGeometry.RIDE_HEIGHT;
    private static final EntityDimensions DIMENSIONS = EntityDimensions
            .fixed((float) ShipGeometry.BEAM, (float) ShipGeometry.HULL_FLOOR_THICKNESS)
            .withEyeHeight((float) ShipGeometry.DECK_Y);

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

    /** The invisible cabin/bridge boxes, spawned on the first server tick. */
    private final List<ShipCollisionPart> parts = new ArrayList<>(ShipGeometry.COLLISION_PARTS.size());
    private boolean partsReady;
    /** Reused per-tick lists, so carrying the deck does not churn the allocator. */
    private final List<ServerPlayer> walkers = new ArrayList<>();
    private final List<double[]> walkerLocal = new ArrayList<>();

    public CoastalPacket(EntityType<? extends CoastalPacket> type, Level level) {
        super(type, level);
        this.blocksBuilding = true;
        this.noPhysics = true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        // The hull's shape is derived from its synced position and yaw, so it needs no extra data.
    }

    /**
     * Wires a freshly spawned hull to its line and timetable. Called by
     * {@link com.sanchous98.eldritchhorror.world.transport.ShipService} immediately after
     * {@code addFreshEntity}; the port chunk ticket is already held.
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
        this.faceDocked();
    }

    /** Places the hull without the renderer-side spawn pop (same contract as the old packet). */
    public void setInitialPos(double x, double y, double z) {
        this.setPos(x, y, z);
        this.xo = x;
        this.yo = y;
        this.zo = z;
    }

    // ------------------------------------------------------------------ state

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

    /** @return this vessel's configured passenger capacity (never above the physical seats). */
    public int maxPassengers() {
        return Math.clamp(ModConfig.SHIP_SEATS.get(), 1, SEATS);
    }

    /**
     * Holds the ticket around a chunk. Used by the service before spawning, so the hull never
     * appears in an unloaded ocean and the whole long deck stays loaded while it sails.
     */
    public static void holdChunks(ServerLevel level, ChunkPos pos) {
        level.getChunkSource().addTicketWithRadius(SHIP_TICKET, pos, ShipGeometry.TICKET_RADIUS);
    }

    /** Releases the ticket {@link #holdChunks} placed. */
    public static void releaseChunks(ServerLevel level, ChunkPos pos) {
        level.getChunkSource().removeTicketWithRadius(SHIP_TICKET, pos,
                ShipGeometry.TICKET_RADIUS);
    }

    // ------------------------------------------------------------------ dimensions and collision

    /**
     * The deck floor. Its top face is the walkable main deck; the 91 x 18 footprint is enclosed at
     * the current heading because an entity AABB cannot rotate. The superstructure above it is
     * carried by {@link ShipCollisionPart}s.
     */
    @Override
    protected AABB makeBoundingBox(Vec3 position) {
        ShipGeometry.Box box = ShipGeometry.hullBox(position.x, position.y, position.z,
                this.getYRot());
        return new AABB(box.minX(), box.minY(), box.minZ(), box.maxX(), box.maxY(), box.maxZ());
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return DIMENSIONS;
    }

    /**
     * The deck box depends on the heading, and a client rotation packet calls {@code setYRot} on
     * its own, so refresh the box here as well as after every server move.
     */
    @Override
    public void setYRot(float yRot) {
        if (yRot == this.getYRot()) {
            return;
        }
        super.setYRot(yRot);
        this.setBoundingBox(this.makeBoundingBox(this.position()));
    }

    /** The hull is solid: players stand on the deck instead of falling through it. */
    @Override
    public boolean canBeCollidedWith(@Nullable Entity other) {
        return true;
    }

    /** Public transport is never shoved off its lane. */
    @Override
    public boolean isPushable() {
        return false;
    }

    /**
     * Seats are entity-local and rotated with the hull. The attachment height puts the rider's
     * feet 0.3 blocks into the planking, so a seated passenger reads as sitting on deck.
     */
    @Override
    protected Vec3 getPassengerAttachmentPoint(Entity passenger, EntityDimensions dimensions,
                                               float scale) {
        double[] seat = ShipSeats.offset(this.seatIndex(passenger));
        return new Vec3(seat[0], seat[1], seat[2])
                .yRot(-this.getYRot() * (float) (Math.PI / 180.0));
    }

    /** A rider steps off beside their own seat, not at the middle of the ship. */
    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity passenger) {
        double[] seat = ShipSeats.offset(this.seatIndex(passenger));
        double[] offset = ShipGeometry.rotateLocal(seat[0], seat[2], this.getYRot());
        return new Vec3(this.getX() + offset[0], this.getY() + ShipGeometry.DECK_Y,
                this.getZ() + offset[1]);
    }

    private int seatIndex(Entity passenger) {
        return Math.max(0, this.getPassengers().indexOf(passenger));
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
        return this.tryBoard(serverPlayer) ? InteractionResult.SUCCESS : InteractionResult.FAIL;
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
        if (this.getPassengers().size() >= this.maxPassengers()) {
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

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return this.getPassengers().size() < this.maxPassengers();
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
        EldritchHorror.LOGGER.info("transport: liner {} departs with {} riders",
                this.serviceKey(), this.getPassengers().size());
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
        this.captureDeckWalkers(serverLevel);
        double oldX = this.getX();
        double oldZ = this.getZ();
        float oldYaw = this.getYRot();
        if (this.sailing) {
            this.advance(serverLevel);
        }
        if (this.getX() != oldX || this.getZ() != oldZ || this.getYRot() != oldYaw) {
            this.placeDeckWalkers();
        } else {
            this.walkers.clear();
            this.walkerLocal.clear();
        }
        this.spawnParts(serverLevel);
        this.followParts();
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
            this.turnTowards((float) Math.toDegrees(Math.atan2(-dx, dz)));
            if (distance <= remaining) {
                this.setPos(target.x() + 0.5, this.getY(), target.z() + 0.5);
                remaining -= distance;
                this.waypointIndex++;
            } else {
                this.setPos(EarthMap.wrapBlockX(this.getX() + dx / distance * remaining),
                        this.getY(), this.getZ() + dz / distance * remaining);
                remaining = 0.0;
            }
        }
    }

    /**
     * Lies broadside to the quay while boarding: the length runs along the shore and the starboard
     * gangway faces the land. Only when the port has no distinct shore point does she face the
     * first waypoint instead.
     */
    private void faceDocked() {
        Port origin = this.originPort();
        ShippingLine shipping = this.line;
        if (origin == null || shipping == null) {
            return;
        }
        double dx = origin.shoreX() - origin.waterX();
        double dz = origin.shoreZ() - origin.waterZ();
        if (dx * dx + dz * dz < 1.0) {
            this.faceNextWaypoint();
            return;
        }
        this.setYRot(ShipGeometry.dockedYaw(origin.waterX(), origin.waterZ(),
                origin.shoreX(), origin.shoreZ()));
        this.refreshDimensions();
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
        this.refreshDimensions();
    }

    /** Rotates toward the course by at most {@link #TURN_RATE_DEGREES} this tick. */
    private void turnTowards(float wanted) {
        float delta = Mth.wrapDegrees(wanted - this.getYRot());
        float step = Math.max(-TURN_RATE_DEGREES, Math.min(TURN_RATE_DEGREES, delta));
        this.setYRot(this.getYRot() + step);
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
        EldritchHorror.LOGGER.info("transport: liner {} arrived at {} with {} riders",
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

    // ------------------------------------------------------------------ walkable deck

    /**
     * Remembers every non-riding player standing on the deck or a cabin roof, together with their
     * hull-local position, so they can be moved with the vessel instead of being scraped off it.
     */
    private void captureDeckWalkers(ServerLevel level) {
        this.walkers.clear();
        this.walkerLocal.clear();
        double originY = this.getY();
        for (ServerPlayer player : level.players()) {
            if (player.getVehicle() != null || player.isSpectator()) {
                continue;
            }
            double dx = player.getX() - this.getX();
            double dz = player.getZ() - this.getZ();
            double[] local = ShipGeometry.toLocal(dx, dz, this.getYRot());
            if (!ShipGeometry.onDeck(local[0], local[1])) {
                continue;
            }
            double surface = originY + ShipGeometry.surfaceY(local[0], local[1]);
            if (Math.abs(player.getY() - surface) > DECK_CONTACT) {
                continue;
            }
            this.walkers.add(player);
            this.walkerLocal.add(local);
        }
    }

    /** Re-seats the captured walkers on the moved hull, preserving their local deck position. */
    private void placeDeckWalkers() {
        for (int i = 0; i < this.walkers.size(); i++) {
            ServerPlayer player = this.walkers.get(i);
            double[] local = this.walkerLocal.get(i);
            if (player.isRemoved() || player.getVehicle() != null
                    || player.level() != this.level()) {
                continue;
            }
            double[] offset = ShipGeometry.rotateLocal(local[0], local[1], this.getYRot());
            // A standing player is not a passenger, so the client must be told where the moving
            // deck took them; teleportTo is the authoritative position packet.
            player.teleportTo(this.getX() + offset[0],
                    this.getY() + ShipGeometry.surfaceY(local[0], local[1]),
                    this.getZ() + offset[1]);
        }
        this.walkers.clear();
        this.walkerLocal.clear();
    }

    // ------------------------------------------------------------------ collision parts

    /** Spawns the superstructure's invisible boxes once, on the first server tick. */
    private void spawnParts(ServerLevel level) {
        if (this.partsReady) {
            return;
        }
        List<ShipCollisionPart> fresh = new ArrayList<>(ShipGeometry.COLLISION_PARTS.size());
        for (ShipGeometry.PartSpec spec : ShipGeometry.COLLISION_PARTS) {
            ShipCollisionPart part = ModEntities.SHIP_COLLISION_PART.get()
                    .create(level, EntitySpawnReason.EVENT);
            if (part == null) {
                discardParts(fresh);
                return;
            }
            part.configure(this, spec);
            if (!level.addFreshEntity(part)) {
                part.discard();
                discardParts(fresh);
                return;
            }
            fresh.add(part);
        }
        this.parts.addAll(fresh);
        this.partsReady = true;
    }

    private void followParts() {
        for (ShipCollisionPart part : this.parts) {
            if (part.isRemoved()) {
                // A part can only vanish with an unloaded chunk; rebuild the set next tick.
                discardParts(this.parts);
                this.partsReady = false;
                return;
            }
            part.follow();
        }
    }

    private static void discardParts(List<ShipCollisionPart> list) {
        for (ShipCollisionPart part : list) {
            part.discard();
        }
        list.clear();
    }

    // ------------------------------------------------------------------ tickets and teardown

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

    /** Never saved: a voyage is process state, not world state. A restart cancels the service. */
    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        // Process state only: a voyage never survives a restart.
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        // Process state only: a voyage never survives a restart.
    }

    @Override
    public void remove(Entity.RemovalReason reason) {
        if (this.level() instanceof ServerLevel serverLevel && this.ticketCenter != null) {
            releaseChunks(serverLevel, this.ticketCenter);
            this.ticketCenter = null;
        }
        discardParts(this.parts);
        this.partsReady = false;
        super.remove(reason);
    }

    /**
     * Public transport, not a boat item: a player punching it, an explosion or fire must not sink
     * it mid-voyage. The hit is acknowledged and no damage is applied.
     */
    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        return true;
    }

    /** @return the current block position, for diagnostics. */
    public BlockPos packetPos() {
        return this.blockPosition();
    }
}
