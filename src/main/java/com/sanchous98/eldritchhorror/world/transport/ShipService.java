package com.sanchous98.eldritchhorror.world.transport;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.core.ModConfig;
import com.sanchous98.eldritchhorror.entity.CoastalPacket;
import com.sanchous98.eldritchhorror.registry.ModEntities;
import com.sanchous98.eldritchhorror.world.EarthMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.jspecify.annotations.Nullable;

/**
 * The stage-1 ship timetable ({@code design/30-transport.md}): one docked liner per line and
 * direction while its gangway is open, then a real voyage under {@link CoastalPacket}'s own tick.
 *
 * <p>The service is a small lifecycle manager, not a per-tick pathfinder. It wakes every second,
 * finds the next departure for each line/direction from {@link TransportMath}, and spawns the liner
 * when the boarding window opens. Once spawned, the hull owns its movement, tickets, departure,
 * arrival and despawn. An empty service despawns at departure instead of sailing; a hull with no
 * riders left mid-ocean despawns after a short grace.
 *
 * <p>Schedules are read from the overworld clock, so {@code /time set} moves them; a started voyage
 * runs on real ticks. All state is process-local (liners are never saved): a restart simply cancels
 * the current services and the next boarding window spawns fresh ones. The vessel itself owns its
 * 7 x 7 chunk ticket, its deck walkers and its invisible superstructure parts.
 */
@EventBusSubscriber(modid = EldritchHorror.MODID)
public final class ShipService {
    /** Lifecycle check rate: one second. Movement itself is per-tick in the entity. */
    private static final int TICK_INTERVAL = 20;
    /** Players within this many blocks of a harbour hear the boarding call. */
    private static final double BOARDING_CALL_RADIUS = 192.0;
    /** Don't spawn a second hull on top of a lingering one at the same harbour. */
    private static final double DOCK_CLEARANCE = 24.0;

    private static final Map<String, CoastalPacket> ACTIVE = new HashMap<>();
    /** Last seen {@code hullTicks} per hull, for the stuck-hull watchdog. */
    private static final Map<String, Integer> LAST_TICK = new HashMap<>();
    /** Consecutive no-progress service checks per hull. */
    private static final Map<String, Integer> STUCK = new HashMap<>();

    private ShipService() {
    }

    /**
     * Drops every cached hull and its watchdog state. Called on server stop so a world switch in the
     * same JVM cannot keep strong references to the previous world's entities.
     */
    public static void clear() {
        for (CoastalPacket ship : ACTIVE.values()) {
            if (!ship.isRemoved()) {
                ship.discard();
            }
        }
        ACTIVE.clear();
        LAST_TICK.clear();
        STUCK.clear();
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (!ModConfig.ENABLE_SHIPS.get()) {
            return;
        }
        if (event.getServer().getTickCount() % TICK_INTERVAL != 0) {
            return;
        }
        MinecraftServer server = event.getServer();
        ServerLevel overworld = server.getLevel(Level.OVERWORLD);
        if (overworld != null) {
            tick(overworld);
        }
    }

    private static void tick(ServerLevel level) {
        long now = level.getOverworldClockTime();
        prune(level, now);
        List<ShippingLine> lines = ShippingLines.all();
        if (lines.isEmpty()) {
            return;
        }
        int[] baseHours = DepartureHours.parse(ModConfig.SHIP_DEPARTURE_HOURS.get(),
                DepartureHours.SHIP_DEFAULT);
        int period = Math.max(1, ModConfig.SHIP_PERIOD_DAYS.get());
        int boardingMinutes = Math.max(0, ModConfig.SHIP_BOARDING_MINUTES.get());
        int maxShips = Math.max(1, ModConfig.MAX_SHIPS.get());
        double speed = Math.max(0.1, ModConfig.SHIP_SPEED.get());

        for (ShippingLine line : lines) {
            for (int direction = 0; direction <= 1; direction++) {
                String key = line.id() + ":" + direction;
                if (ACTIVE.containsKey(key)) {
                    continue;
                }
                long departure = nextDeparture(now, line, direction, baseHours, period);
                long opens = TransportMath.boardingOpens(departure, boardingMinutes);
                if (now < opens || now >= departure) {
                    continue;
                }
                if (ACTIVE.size() >= maxShips) {
                    return;
                }
                Port origin = line.origin(direction);
                // A hull is only spawned where somebody waits on the quay; an empty port otherwise
                // fills the ship cap with hulls nobody can board (and whose chunks may not tick).
                if (!playerWaiting(level, origin)) {
                    continue;
                }
                if (dockedAt(level, origin)) {
                    continue;
                }
                CoastalPacket ship = spawn(level, line, direction, departure,
                        (int) Math.clamp(departure - now, 1L, (long) TransportMath.DAY_TICKS), speed);
                if (ship == null) {
                    continue;
                }
                ACTIVE.put(key, ship);
                callBoarding(level, line, direction, departure);
            }
        }
    }

    /** @return true when a player is close enough to the port to board a hull there. */
    private static boolean playerWaiting(ServerLevel level, Port port) {
        for (ServerPlayer player : level.players()) {
            if (port.distanceSqr(player.getX(), player.getZ()) <= 384.0 * 384.0) {
                return true;
            }
        }
        return false;
    }

    /** @return the next departure for one line/direction, with its stable stagger applied. */
    private static long nextDeparture(long now, ShippingLine line, int direction, int[] baseHours,
                                      int period) {
        int[] hours = DepartureHours.forLine(line.id() + ":" + direction, baseHours);
        return TransportMath.nextDeparture(now, hours, period);
    }

    private static void prune(ServerLevel level, long now) {
        Iterator<Map.Entry<String, CoastalPacket>> iterator = ACTIVE.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, CoastalPacket> entry = iterator.next();
            String key = entry.getKey();
            CoastalPacket ship = entry.getValue();
            if (ship.isRemoved() || ship.level() != level) {
                iterator.remove();
                LAST_TICK.remove(key);
                STUCK.remove(key);
                continue;
            }
            int tick = ship.hullTicks();
            Integer previous = LAST_TICK.put(key, tick);
            if (previous != null && previous == tick) {
                int stuck = STUCK.merge(key, 1, Integer::sum);
                // A moving hull can miss a check while a chunk ticket settles; re-assert the
                // entity-ticking ticket line and only drop it after three silent seconds.
                ship.reassertTickets(level);
                if (stuck >= 15) {
                    EldritchHorror.LOGGER.warn("transport: hull {} not ticking for {} checks, "
                            + "discarded", key, stuck);
                    ship.discard();
                    iterator.remove();
                    LAST_TICK.remove(key);
                    STUCK.remove(key);
                }
                continue;
            }
            STUCK.remove(key);
            if (!ship.isSailing() && !ship.isArrived()) {
                if (now > ship.departureClock() + 400L) {
                    EldritchHorror.LOGGER.warn("transport: hull {} outlived its window, discarded",
                            key);
                    ship.discard();
                    iterator.remove();
                    LAST_TICK.remove(key);
                    continue;
                }
                ship.forceDue();
            }
        }
    }

    private static boolean dockedAt(ServerLevel level, Port port) {
        for (CoastalPacket ship : ACTIVE.values()) {
            if (ship.isRemoved() || ship.level() != level) {
                continue;
            }
            if (!ship.isSailing()
                    && EarthMap.horizontalDistanceSqr(ship.getX(), ship.getZ(),
                    port.waterX() + 0.5, port.waterZ() + 0.5)
                    <= DOCK_CLEARANCE * DOCK_CLEARANCE) {
                return true;
            }
        }
        return false;
    }

    private static @Nullable CoastalPacket spawn(ServerLevel level, ShippingLine line, int direction,
                                                 long departureAt, int countdownTicks,
                                                 double speed) {
        Port origin = line.origin(direction);
        ChunkPos chunk = new ChunkPos(origin.waterX() >> 4, origin.waterZ() >> 4);
        CoastalPacket.holdChunks(level, chunk);
        // The ticket load is asynchronous. Load the harbour synchronously before adding the entity:
        // an entity created in a not-yet-loaded chunk can miss the ticking entity list and stay
        // "boarding" forever (the service watchdog also catches that case).
        level.getChunk(chunk.x(), chunk.z());
        CoastalPacket ship = ModEntities.COASTAL_PACKET.get().create(level, EntitySpawnReason.EVENT);
        if (ship == null) {
            CoastalPacket.releaseChunks(level, chunk);
            EldritchHorror.LOGGER.warn("transport: could not create a liner for {}", line.id());
            return null;
        }
        // The berth's own water surface, not the world sea level: an authored inland basin (Mexico
        // City's canal, Rome's Tiber) sits far above (or below) the ocean, and a coastal basin can
        // be carved a course lower than the default sea. Do not use the heightmap either: a
        // structure or overhang above a port can put it tens of blocks above the water (Lima's cliff).
        ship.setInitialPos(origin.waterX() + 0.5, origin.waterY() + 0.85,
                origin.waterZ() + 0.5);
        ship.configure(line, direction, countdownTicks, departureAt, speed, chunk);
        if (!level.addFreshEntity(ship)) {
            CoastalPacket.releaseChunks(level, chunk);
            return null;
        }
        EldritchHorror.LOGGER.info("transport: {} docked at {}, departs in {} ticks",
                line.id(), origin.cityId(), countdownTicks);
        return ship;
    }

    private static void callBoarding(ServerLevel level, ShippingLine line, int direction,
                                     long departure) {
        Port destination = line.destination(direction);
        String time = TransportMath.formatTime(departure);
        Component message = Component.translatable("message.eldritch_horror.ship.boarding",
                destination.cityName(), time);
        Port origin = line.origin(direction);
        for (ServerPlayer player : level.players()) {
            if (EarthMap.horizontalDistanceSqr(player.getX(), player.getZ(),
                    origin.waterX() + 0.5, origin.waterZ() + 0.5)
                    <= BOARDING_CALL_RADIUS * BOARDING_CALL_RADIUS) {
                player.sendSystemMessage(message, false);
            }
        }
        EldritchHorror.LOGGER.info("transport: boarding open for {} -> {} at {}",
                origin.cityId(), destination.cityId(), TransportMath.formatTime(departure));
    }

    /**
     * {@code /eh ship}: a snapshot for the debug probe.
     *
     * @param level overworld
     * @param x     caller X
     * @param z     caller Z
     * @return printable lines, in order
     */
    public static List<String> describe(ServerLevel level, double x, double z) {
        List<String> out = new ArrayList<>();
        if (!ModConfig.ENABLE_SHIPS.get()) {
            out.add("ships disabled (enableShips=false)");
            return out;
        }
        long now = level.getOverworldClockTime();
        out.add("clock " + TransportMath.formatTime(now) + ", day " + TransportMath.dayOf(now)
                + ", active ships " + ACTIVE.size());
        Port nearest = Ports.nearest(x, z);
        if (nearest == null) {
            out.add("no ports (earth map or coastal cities unavailable)");
            return out;
        }
        out.add("nearest port: " + nearest.label() + ", "
                + Math.round(Math.sqrt(nearest.distanceSqr(x, z))) + " blocks");
        boolean found = false;
        for (ShippingLine line : ShippingLines.all()) {
            for (int direction = 0; direction <= 1; direction++) {
                if (!line.origin(direction).cityId().equals(nearest.cityId())) {
                    continue;
                }
                found = true;
                out.add(describeLine(level, line, direction, now));
            }
        }
        if (!found) {
            out.add("no line departs from this port");
        }
        out.addAll(describeShips());
        return out;
    }

    private static String describeLine(ServerLevel level, ShippingLine line, int direction,
                                       long now) {
        int[] baseHours = DepartureHours.parse(ModConfig.SHIP_DEPARTURE_HOURS.get(),
                DepartureHours.SHIP_DEFAULT);
        int period = Math.max(1, ModConfig.SHIP_PERIOD_DAYS.get());
        long departure = nextDeparture(now, line, direction, baseHours, period);
        CoastalPacket ship = ACTIVE.get(line.id() + ":" + direction);
        StringBuilder text = new StringBuilder();
        text.append(line.id()).append(" dir ").append(direction).append(" (")
                .append(line.origin(direction).cityName()).append(" -> ")
                .append(line.destination(direction).cityName()).append("), ")
                .append(Math.round(line.length())).append(" blocks");
        text.append("; next departure ").append(TransportMath.formatTime(departure))
                .append(" (in ").append(departure - now).append(" ticks)");
        if (ship == null || ship.isRemoved()) {
            text.append("; state ").append(TransportMath.isBoarding(now, departure,
                    ModConfig.SHIP_BOARDING_MINUTES.get()) ? "boarding open" : "docked (not spawned)");
        } else {
            BlockPos pos = ship.packetPos();
            text.append("; state ").append(ship.stateLabel()).append(" at ")
                    .append(pos.getX()).append(' ').append(pos.getY()).append(' ')
                    .append(pos.getZ()).append(", riders ").append(ship.getPassengers().size())
                    .append('/').append(ModConfig.SHIP_SEATS.get())
                    .append(", countdown ").append(ship.ticksUntilDeparture())
                    .append(", tick ").append(ship.hullTicks());
        }
        return text.toString();
    }

    private static List<String> describeShips() {
        List<String> out = new ArrayList<>();
        for (Map.Entry<String, CoastalPacket> entry : ACTIVE.entrySet()) {
            CoastalPacket ship = entry.getValue();
            if (ship.isRemoved()) {
                continue;
            }
            BlockPos pos = ship.packetPos();
            out.add("ship " + entry.getKey() + " " + ship.stateLabel() + " at "
                    + pos.getX() + " " + pos.getY() + " " + pos.getZ()
                    + ", riders " + ship.getPassengers().size()
                    + ", tick " + ship.hullTicks());
        }
        return out;
    }

    // ------------------------------------------------------------------ disconnect rule

    /** A passenger who logs off mid-voyage is put ashore at the destination on their next login. */
    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!ModConfig.ENABLE_SHIPS.get()
                || !(event.getEntity() instanceof ServerPlayer player)
                || !(player.getVehicle() instanceof CoastalPacket ship)
                || !ship.isSailing()) {
            return;
        }
        Port destination = ship.destinationPort();
        if (destination == null) {
            return;
        }
        if (player.level() instanceof ServerLevel level) {
            ShipRiders.of(level).put(player.getUUID(), destination.shoreX() + 0.5,
                    destination.shoreZ() + 0.5);
            EldritchHorror.LOGGER.info("transport: {} logged off at sea, will land at {}",
                    player.getGameProfile().name(), destination.cityId());
        }
    }

    /** Resolves a pending mid-voyage logout at the destination shore. */
    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        MinecraftServer server = player.level().getServer();
        if (server == null) {
            return;
        }
        ServerLevel overworld = server.getLevel(Level.OVERWORLD);
        if (overworld == null) {
            return;
        }
        ShipRiders.Drop drop = ShipRiders.of(overworld).take(player.getUUID());
        if (drop == null) {
            return;
        }
        int x = (int) Math.floor(drop.x());
        int z = (int) Math.floor(drop.z());
        int y = overworld.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        player.teleportTo(overworld, drop.x(), Math.max(y, overworld.getSeaLevel() + 1), drop.z(),
                java.util.Set.of(), player.getYRot(), player.getXRot(), false);
        player.sendSystemMessage(
                Component.translatable("message.eldritch_horror.ship.landed_ashore"), false);
    }
}
