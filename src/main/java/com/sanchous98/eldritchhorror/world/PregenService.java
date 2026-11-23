package com.sanchous98.eldritchhorror.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.world.city.Cities;
import com.sanchous98.eldritchhorror.world.city.City;
import com.sanchous98.eldritchhorror.world.loc.city.plan.CityPlan;
import com.sanchous98.eldritchhorror.world.loc.city.plan.CityPlans;
import com.sanchous98.eldritchhorror.world.rail.RailLine;
import com.sanchous98.eldritchhorror.world.rail.RailLines;
import com.sanchous98.eldritchhorror.world.rail.RailNode;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * One-shot, tick-sliced world pre-generation for the authored cities and the rail corridors.
 *
 * <p>Generating 600–800-block cities on first visit is the expensive part of a session; this
 * service walks a bounded chunk queue on the server thread and lets the normal chunk save/unload
 * machinery persist them. `/eh pregen all` starts it; `-Deh.pregen=all` starts it at boot unless
 * this world already finished a run (the {@link State} flag survives restarts).
 *
 * <p>Two chunks are generated per tick while the server is empty. As soon as a player is online the
 * rate drops to one chunk every five ticks, so a late joiner gets lag spikes instead of a stalled
 * server. A single chunk can take tens of milliseconds, so `Can't keep up` warnings during an
 * empty-server run are expected and harmless. The queue is not a resident-set loader: the chunks
 * are persisted by the normal world save and later sessions only read them from disk.
 */
@EventBusSubscriber(modid = EldritchHorror.MODID)
public final class PregenService {

    /** Chunks per tick while nobody is online. */
    private static final int EMPTY_BATCH = 2;
    /** One chunk every this many ticks while players are online. */
    private static final int BUSY_PERIOD = 5;
    /** Log progress at most this often (chunks). */
    private static final int LOG_EVERY = 500;
    /** How often the throughput sample is refreshed (ticks). */
    private static final int RATE_PERIOD = 100;

    private static final ArrayDeque<ChunkPos> QUEUE = new ArrayDeque<>();
    private static final Set<Long> SEEN = new HashSet<>();
    private static boolean running;
    private static int total;
    private static int done;
    private static int failures;
    private static long startedNanos;
    private static long sampleNanos;
    private static int sampleDone;
    private static double rate;
    private static int tickCounter;

    private PregenService() {
    }

    /**
     * Starts a pre-generation run.
     *
     * @param server server whose overworld is generated
     * @param scope  {@code cities}, {@code rail} or {@code all}
     * @return a short human-readable result
     */
    public static String start(MinecraftServer server, String scope) {
        stop();
        String selected = scope.toLowerCase(Locale.ROOT);
        if (selected.equals("cities") || selected.equals("all")) {
            queueCities();
        }
        if (selected.equals("rail") || selected.equals("all")) {
            queueRail();
        }
        total = QUEUE.size();
        done = 0;
        failures = 0;
        startedNanos = System.nanoTime();
        sampleNanos = startedNanos;
        sampleDone = 0;
        rate = 0.0;
        tickCounter = 0;
        running = total > 0;
        if (running) {
            State state = State.of(server);
            state.done = false;
            state.chunks = 0;
            state.setDirty();
        }
        EldritchHorror.LOGGER.info("pregen: {} chunks queued ({})", total, selected);
        return running ? "pregen started: " + total + " chunks" : "pregen: nothing to generate";
    }

    /** Stops the current run and drops its queue. */
    public static void stop() {
        if (running) {
            EldritchHorror.LOGGER.info("pregen: stopped at {}/{}", done, total);
        }
        running = false;
        QUEUE.clear();
        SEEN.clear();
        total = 0;
        done = 0;
        failures = 0;
        rate = 0.0;
    }

    /** @return progress for `/eh pregen status`. */
    public static String status() {
        if (!running) {
            return "pregen: idle" + (done > 0 ? " (last run " + done + " chunks)" : "");
        }
        long remaining = total - done;
        long eta = rate <= 0.0 ? -1L : Math.round(remaining / rate);
        return String.format(Locale.ROOT, "pregen: %d/%d (%.1f%%), %.1f chunks/s, ETA %s, %d failed",
                done, total, 100.0 * done / Math.max(1, total), rate,
                eta < 0 ? "?" : eta + "s", failures);
    }

    /** @return the persisted flag for this world, for logs and admins. */
    public static boolean isComplete(MinecraftServer server) {
        return State.of(server).done;
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (!running) {
            return;
        }
        MinecraftServer server = event.getServer();
        ServerLevel level = server.overworld();
        int batch = server.getPlayerList().getPlayerCount() > 0
                ? (tickCounter++ % BUSY_PERIOD == 0 ? 1 : 0)
                : EMPTY_BATCH;
        for (int i = 0; i < batch && !QUEUE.isEmpty(); i++) {
            ChunkPos chunk = QUEUE.poll();
            try {
                level.getChunk(chunk.x(), chunk.z(), ChunkStatus.FULL, true);
            } catch (RuntimeException error) {
                failures++;
                EldritchHorror.LOGGER.warn("pregen: failed at {} {}", chunk.x(), chunk.z(), error);
            }
            done++;
            if (done % LOG_EVERY == 0) {
                EldritchHorror.LOGGER.info(status());
            }
        }
        if (done - sampleDone >= RATE_PERIOD || tickCounter % 20 == 0) {
            double seconds = (System.nanoTime() - sampleNanos) / 1.0E9;
            if (seconds >= 5.0) {
                rate = (done - sampleDone) / seconds;
                sampleNanos = System.nanoTime();
                sampleDone = done;
            }
        }
        if (QUEUE.isEmpty()) {
            running = false;
            double seconds = Math.max(0.001, (System.nanoTime() - startedNanos) / 1.0E9);
            State state = State.of(server);
            state.done = true;
            state.chunks = done;
            state.setDirty();
            EldritchHorror.LOGGER.info("pregen: complete, {} chunks in {}s ({} failed)",
                    done, Math.round(seconds), failures);
        }
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        String pregen = System.getProperty("eh.pregen");
        if (pregen == null || pregen.isBlank()) {
            return;
        }
        if (isComplete(event.getServer())) {
            EldritchHorror.LOGGER.info("pregen: this world is already pre-generated, skipping '{}'",
                    pregen);
            return;
        }
        start(event.getServer(), pregen);
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        stop();
    }

    private static void queueCities() {
        for (City city : Cities.all()) {
            CityPlan plan = CityPlans.forCity(city.name());
            int half = plan == null ? 340 : Math.clamp(plan.halfExtent(), 300, 400);
            int minX = city.x() - half;
            int maxX = city.x() + half;
            int minZ = city.z() - half;
            int maxZ = city.z() + half;
            for (int cx = minX >> 4; cx <= maxX >> 4; cx++) {
                for (int cz = minZ >> 4; cz <= maxZ >> 4; cz++) {
                    add(cx, cz);
                }
            }
        }
    }

    private static void queueRail() {
        for (RailLine line : RailLines.all()) {
            for (RailNode node : line.nodes()) {
                int cx = node.x() >> 4;
                int cz = node.z() >> 4;
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        add(cx + dx, cz + dz);
                    }
                }
            }
        }
    }

    private static void add(int chunkX, int chunkZ) {
        if (SEEN.add(ChunkPos.pack(chunkX, chunkZ))) {
            QUEUE.add(new ChunkPos(chunkX, chunkZ));
        }
    }

    /** Per-world marker: a completed run is not repeated on the next boot. */
    private static final class State extends SavedData {

        private static final Codec<State> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.BOOL.optionalFieldOf("done", false).forGetter(state -> state.done),
                Codec.INT.optionalFieldOf("chunks", 0).forGetter(state -> state.chunks)
        ).apply(instance, State::new));

        private static final SavedDataType<State> TYPE =
                new SavedDataType<>(EldritchHorror.id("pregen"), State::new, CODEC);

        private boolean done;
        private int chunks;

        private State() {
        }

        private State(boolean done, int chunks) {
            this.done = done;
            this.chunks = chunks;
        }

        private static State of(MinecraftServer server) {
            return server.overworld().getDataStorage().computeIfAbsent(TYPE);
        }
    }
}
