package com.sanchous98.eldritchhorror.world.rail;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.core.ModConfig;
import com.sanchous98.eldritchhorror.entity.TrainCar;
import com.sanchous98.eldritchhorror.entity.TrainLocomotive;
import com.sanchous98.eldritchhorror.entity.TrainWagon;
import com.sanchous98.eldritchhorror.registry.ModEntities;
import com.sanchous98.eldritchhorror.world.transport.TransportMath;
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
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.jspecify.annotations.Nullable;

/**
 * The stage-2 train timetable ({@code design/30-transport.md}): one consist per line and direction
 * while its boarding window is open, then a real voyage under {@link TrainLocomotive}'s own tick.
 *
 * <p>The service is a lifecycle manager, not a per-tick pathfinder. It wakes every second, finds the
 * next departure for each line/direction from the shared {@link TransportMath} clock, and spawns a
 * locomotive plus 2–4 coupled wagons when boarding opens. An empty consist despawns at departure
 * instead of running; a consist whose riders all leave mid-line despawns after a short grace. All
 * state is process-local (consists are never saved): a restart cancels the current services.
 */
@EventBusSubscriber(modid = EldritchHorror.MODID)
public final class TrainService {
    /** Lifecycle check rate: one second. Movement itself is per-tick in the entity. */
    private static final int TICK_INTERVAL = 20;
    /** Players within this many blocks of a station hear the boarding call. */
    private static final double BOARDING_CALL_RADIUS = 192.0;
    /** Don't spawn a second consist on top of a lingering one at the same station. */
    private static final double STATION_CLEARANCE = 32.0;

    private static final Map<String, TrainLocomotive> ACTIVE = new HashMap<>();
    /** Chunk tickets held for a consist that is about to be spawned (two-phase spawn). */
    private static final Map<String, ChunkPos> PENDING = new HashMap<>();
    /** Service checks a pending spawn has waited for. */
    private static final Map<String, Integer> PENDING_AGE = new HashMap<>();
    /** Last seen {@code trainTicks} per consist, for the stuck-consist watchdog. */
    private static final Map<String, Integer> LAST_TICK = new HashMap<>();
    /** Consecutive no-progress service checks per consist. */
    private static final Map<String, Integer> STUCK = new HashMap<>();

    private TrainService() {
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (!ModConfig.ENABLE_TRAINS.get()) {
            return;
        }
        if (event.getServer().getTickCount() % TICK_INTERVAL != 0) {
            return;
        }
        ServerLevel overworld = event.getServer().getLevel(Level.OVERWORLD);
        if (overworld != null) {
            tick(overworld);
        }
    }

    /** Forgets every consist and its watchdog state (server start, or a test reset). */
    public static void clear() {
        ACTIVE.clear();
        PENDING.clear();
        PENDING_AGE.clear();
        LAST_TICK.clear();
        STUCK.clear();
    }

    /** Builds and logs the cache once at server start instead of on the first crossing chunk. */
    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        clear();
        if (!ModConfig.ENABLE_TRAINS.get()) {
            return;
        }
        try {
            RailLines.all();
        } catch (RuntimeException error) {
            EldritchHorror.LOGGER.error("rail: network build failed at startup", error);
        }
    }

    private static void tick(ServerLevel level) {
        long now = level.getOverworldClockTime();
        prune(level, now);
        List<RailLine> lines = RailLines.all();
        if (lines.isEmpty()) {
            return;
        }
        int hourA = Math.clamp(ModConfig.TRAIN_DEPARTURE_HOUR_A.get(), 0, 23);
        int hourB = Math.clamp(ModConfig.TRAIN_DEPARTURE_HOUR_B.get(), 0, 23);
        int period = Math.max(1, ModConfig.TRAIN_PERIOD_DAYS.get());
        int boardingMinutes = Math.max(1, ModConfig.TRAIN_BOARDING_MINUTES.get());
        int maxTrains = Math.max(1, ModConfig.MAX_TRAINS.get());
        double speed = Math.max(0.1, ModConfig.TRAIN_SPEED.get());
        int wagons = TrainTimetable.wagons(ModConfig.TRAIN_CARS.get());

        for (RailLine line : lines) {
            for (int direction = 0; direction <= 1; direction++) {
                String key = line.id() + ":" + direction;
                if (ACTIVE.containsKey(key)) {
                    continue;
                }
                long departure = TrainTimetable.nextDeparture(now, direction, hourA, hourB, period);
                long opens = TransportMath.boardingOpens(departure, boardingMinutes);
                if (now < opens || now >= departure) {
                    releasePending(level, key);
                    continue;
                }
                if (ACTIVE.size() >= maxTrains) {
                    releasePending(level, key);
                    continue;
                }
                RailStation origin = line.origin(direction);
                if (stationOccupied(level, origin)) {
                    releasePending(level, key);
                    continue;
                }
                // Two-phase spawn: hold the station chunk first, then add the consist on a later
                // service tick. An entity added while the chunk is merely loaded -- not yet entity
                // ticking -- stays outside the tick list forever and never moves; the ticket needs
                // one tick to raise the chunk's status (the stuck-consist watchdog used to discard
                // those duds, occasionally with a player already aboard).
                ChunkPos held = PENDING.get(key);
                if (held == null) {
                    ChunkPos chunk = new ChunkPos(origin.x() >> 4, origin.z() >> 4);
                    TrainLocomotive.holdChunks(level, chunk);
                    level.getChunk(chunk.x(), chunk.z());
                    PENDING.put(key, chunk);
                    PENDING_AGE.put(key, 0);
                    continue;
                }
                if (PENDING_AGE.merge(key, 1, Integer::sum) < 1) {
                    continue;
                }
                if (!level.isPositionEntityTicking(new BlockPos(origin.x(), origin.y(),
                        origin.z()))) {
                    continue;
                }
                TrainLocomotive train = spawn(level, line, direction, departure,
                        (int) Math.clamp(departure - now, 1L, (long) TransportMath.DAY_TICKS),
                        speed, wagons, held);
                if (train == null) {
                    continue;
                }
                PENDING.remove(key);
                PENDING_AGE.remove(key);
                ACTIVE.put(key, train);
                callBoarding(level, line, direction, departure);
            }
        }
    }

    private static void prune(ServerLevel level, long now) {
        Iterator<Map.Entry<String, TrainLocomotive>> iterator = ACTIVE.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, TrainLocomotive> entry = iterator.next();
            String key = entry.getKey();
            TrainLocomotive train = entry.getValue();
            if (train.isRemoved() || train.level() != level) {
                iterator.remove();
                LAST_TICK.remove(key);
                STUCK.remove(key);
                continue;
            }
            int tick = train.trainTicks();
            Integer previous = LAST_TICK.put(key, tick);
            if (previous != null && previous == tick) {
                int stuck = STUCK.merge(key, 1, Integer::sum);
                train.reassertTickets(level);
                if (stuck >= 3) {
                    EldritchHorror.LOGGER.warn("rail: consist {} not ticking for {} checks, "
                            + "discarded", key, stuck);
                    if (train.riderCount() > 0) {
                        train.evacuate(level);
                    }
                    train.discard();
                    iterator.remove();
                    LAST_TICK.remove(key);
                    STUCK.remove(key);
                }
                continue;
            }
            STUCK.remove(key);
            if (!train.isSailing() && !train.isArrived() && now > train.departureClock() + 400L) {
                train.forceDue();
            }
        }
    }

    private static void releasePending(ServerLevel level, String key) {
        ChunkPos held = PENDING.remove(key);
        PENDING_AGE.remove(key);
        if (held != null) {
            TrainLocomotive.releaseChunks(level, held);
        }
    }

    private static boolean stationOccupied(ServerLevel level, RailStation station) {
        for (TrainLocomotive train : ACTIVE.values()) {
            if (train.isRemoved() || train.level() != level || train.isSailing()) {
                continue;
            }
            BlockPos pos = train.blockPosition();
            if (train.distanceToSqr(station.boardX() + 0.5, pos.getY(),
                    station.boardZ() + 0.5) <= STATION_CLEARANCE * STATION_CLEARANCE) {
                return true;
            }
        }
        return false;
    }

    private static @Nullable TrainLocomotive spawn(ServerLevel level, RailLine line, int direction,
                                                   long departureAt, int countdownTicks,
                                                   double speed, int wagonCount, ChunkPos chunk) {
        RailStation origin = line.origin(direction);
        TrainLocomotive locomotive = ModEntities.TRAIN_LOCOMOTIVE.get().create(level,
                EntitySpawnReason.EVENT);
        if (locomotive == null) {
            EldritchHorror.LOGGER.warn("rail: could not create a locomotive for {}", line.id());
            return null;
        }
        RailSample start = line.sample(direction, 0.0);
        locomotive.configure(line, direction, countdownTicks, departureAt, speed, wagonCount,
                chunk);
        locomotive.setPos(start.x(), start.y() + 0.1, start.z());
        if (!level.addFreshEntity(locomotive)) {
            return null;
        }
        for (int index = 1; index <= TrainTimetable.wagons(wagonCount); index++) {
            TrainWagon wagon = ModEntities.TRAIN_WAGON.get().create(level,
                    EntitySpawnReason.EVENT);
            if (wagon == null) {
                EldritchHorror.LOGGER.warn("rail: could not create wagon {} for {}", index,
                        line.id());
                continue;
            }
            wagon.configure(locomotive, index);
            wagon.positionFromTrail();
            if (level.addFreshEntity(wagon)) {
                locomotive.attachWagon(wagon);
            }
        }
        EldritchHorror.LOGGER.info("rail: {} docked at {}, departs in {} ticks",
                line.id(), origin.cityId(), countdownTicks);
        return locomotive;
    }

    private static void callBoarding(ServerLevel level, RailLine line, int direction,
                                     long departure) {
        RailStation origin = line.origin(direction);
        RailStation destination = line.destination(direction);
        int hour = direction == 0
                ? Math.clamp(ModConfig.TRAIN_DEPARTURE_HOUR_A.get(), 0, 23)
                : Math.clamp(ModConfig.TRAIN_DEPARTURE_HOUR_B.get(), 0, 23);
        String time = String.format(java.util.Locale.ROOT, "%02d:00", hour);
        Component message = Component.translatable("message.eldritch_horror.train.boarding",
                destination.cityName(), time);
        for (ServerPlayer player : level.players()) {
            if (player.distanceToSqr(origin.boardX() + 0.5, player.getY(),
                    origin.boardZ() + 0.5) <= BOARDING_CALL_RADIUS * BOARDING_CALL_RADIUS) {
                player.sendSystemMessage(message, false);
            }
        }
        EldritchHorror.LOGGER.info("rail: boarding open for {} -> {} at {}",
                origin.cityId(), destination.cityId(), TransportMath.formatTime(departure));
    }

    /**
     * {@code /eh train}: a snapshot for the debug probe.
     *
     * @param level overworld
     * @param x     caller X
     * @param z     caller Z
     * @return printable lines, in order
     */
    public static List<String> describe(ServerLevel level, double x, double z) {
        List<String> out = new ArrayList<>();
        if (!ModConfig.ENABLE_TRAINS.get()) {
            out.add("trains disabled (enableTrains=false)");
            return out;
        }
        long now = level.getOverworldClockTime();
        out.add("clock " + TransportMath.formatTime(now) + ", day " + TransportMath.dayOf(now)
                + ", active consists " + ACTIVE.size());
        RailStation nearest = RailLines.nearest(x, z);
        if (nearest == null) {
            out.add("no stations (earth map or curated cities unavailable)");
            return out;
        }
        out.add("nearest station: " + nearest.label() + " at " + nearest.x() + " " + nearest.y()
                + " " + nearest.z() + ", "
                + Math.round(Math.sqrt(nearest.distanceSqr(x, z))) + " blocks");
        boolean found = false;
        for (RailLine line : RailLines.atCity(nearest.cityId())) {
            for (int direction = 0; direction <= 1; direction++) {
                if (!line.origin(direction).cityId().equals(nearest.cityId())) {
                    continue;
                }
                found = true;
                out.add(describeLine(level, line, direction, now));
            }
        }
        if (!found) {
            out.add("no line reaches this station");
        }
        out.addAll(describeConsists());
        return out;
    }

    private static String describeLine(ServerLevel level, RailLine line, int direction,
                                       long now) {
        int hourA = Math.clamp(ModConfig.TRAIN_DEPARTURE_HOUR_A.get(), 0, 23);
        int hourB = Math.clamp(ModConfig.TRAIN_DEPARTURE_HOUR_B.get(), 0, 23);
        int period = Math.max(1, ModConfig.TRAIN_PERIOD_DAYS.get());
        long departure = TrainTimetable.nextDeparture(now, direction, hourA, hourB, period);
        TrainLocomotive train = ACTIVE.get(line.id() + ":" + direction);
        StringBuilder text = new StringBuilder();
        text.append(line.id()).append(" dir ").append(direction).append(" (")
                .append(line.origin(direction).cityName()).append(" -> ")
                .append(line.destination(direction).cityName()).append("), ")
                .append(Math.round(line.length())).append(" blocks")
                .append("; next departure ").append(TransportMath.formatTime(departure))
                .append(" (in ").append(departure - now).append(" ticks)");
        if (line.firstBridge() != null) {
            text.append("; first bridge ").append(line.firstBridge().x()).append(' ')
                    .append(line.firstBridge().y()).append(' ').append(line.firstBridge().z());
        }
        if (line.firstTunnel() != null) {
            text.append("; first tunnel ").append(line.firstTunnel().x()).append(' ')
                    .append(line.firstTunnel().y()).append(' ').append(line.firstTunnel().z());
        }
        if (train == null || train.isRemoved()) {
            text.append("; state ").append(TransportMath.isBoarding(now, departure,
                    ModConfig.TRAIN_BOARDING_MINUTES.get())
                    ? "boarding open" : "waiting (not spawned)");
        } else {
            BlockPos pos = train.blockPosition();
            text.append("; state ").append(train.stateLabel()).append(" at ")
                    .append(pos.getX()).append(' ').append(pos.getY()).append(' ')
                    .append(pos.getZ()).append(", riders ").append(train.riderCount())
                    .append('/').append(TrainTimetable.totalSeats(
                            ModConfig.TRAIN_CARS.get(), ModConfig.TRAIN_SEATS.get()))
                    .append(", countdown ").append(train.ticksUntilDeparture())
                    .append(", tick ").append(train.trainTicks())
                    .append(", rail ").append(train.railStateLabel(level));
        }
        return text.toString();
    }

    private static List<String> describeConsists() {
        List<String> out = new ArrayList<>();
        for (Map.Entry<String, TrainLocomotive> entry : ACTIVE.entrySet()) {
            TrainLocomotive train = entry.getValue();
            if (train.isRemoved()) {
                continue;
            }
            BlockPos pos = train.blockPosition();
            StringBuilder text = new StringBuilder("consist ").append(entry.getKey()).append(' ')
                    .append(train.stateLabel()).append(" at ")
                    .append(pos.getX()).append(' ').append(pos.getY()).append(' ')
                    .append(pos.getZ()).append(", riders ").append(train.riderCount())
                    .append(", tick ").append(train.trainTicks())
                    .append(", travelled ").append(Math.round(train.travelled()));
            RailNode next = train.nextRail();
            if (next != null) {
                text.append(", next rail ").append(next.x()).append(' ').append(next.y())
                        .append(' ').append(next.z());
            }
            out.add(text.toString());
        }
        return out;
    }

    // ------------------------------------------------------------------ disconnect rule

    /** A passenger who logs off mid-voyage is put down at the destination station on next login. */
    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!ModConfig.ENABLE_TRAINS.get()
                || !(event.getEntity() instanceof ServerPlayer player)
                || !(player.getVehicle() instanceof TrainCar car)) {
            return;
        }
        TrainLocomotive train = car.locomotive();
        if (train == null || !train.isSailing()) {
            return;
        }
        RailStation destination = train.destinationStation();
        if (destination == null) {
            return;
        }
        if (player.level() instanceof ServerLevel level) {
            TrainRiders.of(level).put(player.getUUID(), destination.boardX() + 0.5,
                    destination.y(), destination.boardZ() + 0.5);
            EldritchHorror.LOGGER.info("rail: {} logged off on a train, will arrive at {}",
                    player.getGameProfile().name(), destination.cityId());
        }
    }

    /** Resolves a pending mid-voyage train logout at the destination station. */
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
        TrainRiders.Drop drop = TrainRiders.of(overworld).take(player.getUUID());
        if (drop == null) {
            return;
        }
        player.teleportTo(overworld, drop.x(), drop.y(), drop.z(), java.util.Set.of(),
                player.getYRot(), player.getXRot(), false);
        player.sendSystemMessage(
                Component.translatable("message.eldritch_horror.train.landed"), false);
    }
}
