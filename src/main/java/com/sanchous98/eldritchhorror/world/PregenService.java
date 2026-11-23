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
import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.Files;
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
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Tick-sliced world pre-generation for the authored cities, the rail corridors and the whole map.
 *
 * <p>Two modes share one tick budget:
 *
 * <ul>
 *   <li><b>content</b> ({@code cities}, {@code rail}, {@code all}) — a bounded in-memory queue of
 *       the 24 city districts plus a 3x3 chunk corridor around every rail node (73k chunks on the
 *       current world). One-shot, minutes.</li>
 *   <li><b>world</b> — the whole playable cylinder: {@code 8192 * 4096 = 33,554,432} chunks. This
 *       cannot be queued in memory, so it is a cursor job: the next chunk position and the count
 *       live in {@link State} and are flushed every {@value #CURSOR_SAVE_EVERY} chunks, which makes
 *       the run resumable across restarts. At the measured ~25 chunks/s it is a two-week job on two
 *       cores, hence: it never runs while a player is online, and it stops itself when free disk
 *       drops below {@value #MIN_FREE_GIB} GiB.</li>
 * </ul>
 *
 * <p>`/eh pregen <scope>` starts a run, `-Deh.pregen=<scope>` starts it at boot (a completed world
 * run is skipped; a partial world run resumes), `/eh pregen status` reports progress and
 * `/eh pregen stop` cancels. `-Deh.pregen.batch=<1..8>` tunes chunks per tick (default 2).
 */
@EventBusSubscriber(modid = EldritchHorror.MODID)
public final class PregenService {

    /** Chunks per tick while nobody is online. */
    private static final int EMPTY_BATCH = Math.clamp(
            Integer.getInteger("eh.pregen.batch", 2), 1, 8);
    /** One chunk every this many ticks while players are online. */
    private static final int BUSY_PERIOD = 5;
    /** Log progress at most this often (chunks). */
    private static final int LOG_EVERY = 500;
    /** How often the throughput sample is refreshed (chunks). */
    private static final int RATE_PERIOD = 100;
    /** Persist the world cursor this often (chunks). */
    private static final int CURSOR_SAVE_EVERY = 256;
    /** Stop the world job when the world volume has less than this much usable space. */
    private static final int MIN_FREE_GIB = 8;
    /** Warn at most this often when the world job is waiting for players to leave. */
    private static final long WAIT_LOG_NANOS = 300_000_000_000L;

    /** Chunk bounds of the playable cylinder ({@link EarthMap#HALF_WIDTH} x half height). */
    private static final int MIN_X = -EarthMap.HALF_WIDTH / 16;
    private static final int MAX_X = (EarthMap.HALF_WIDTH - 1) / 16;
    private static final int MIN_Z = -EarthMap.HALF_HEIGHT / 16;
    private static final int MAX_Z = (EarthMap.HALF_HEIGHT - 1) / 16;
    /** {@code 8192 * 4096}. */
    private static final int WORLD_TOTAL =
            (MAX_X - MIN_X + 1) * (MAX_Z - MIN_Z + 1);

    private static final ArrayDeque<ChunkPos> QUEUE = new ArrayDeque<>();
    private static final Set<Long> SEEN = new HashSet<>();
    private static boolean running;
    private static boolean worldMode;
    private static int total;
    private static int done;
    private static int failures;
    private static long startedNanos;
    private static long sampleNanos;
    private static int sampleDone;
    private static double rate;
    private static int tickCounter;
    private static int cursorX = MIN_X;
    private static int cursorZ = MIN_Z;
    private static boolean pausedForPlayers;
    private static long lastWaitLogNanos;

    private PregenService() {
    }

    /**
     * Starts a pre-generation run.
     *
     * @param server server whose overworld is generated
     * @param scope  {@code cities}, {@code rail}, {@code all} or {@code world}
     * @return a short human-readable result
     */
    public static String start(MinecraftServer server, String scope) {
        String selected = scope.toLowerCase(Locale.ROOT);
        if (selected.equals("world")) {
            return startWorld(server);
        }
        if (running && worldMode) {
            saveWorldCursor(server);
        }
        stop();
        if (selected.equals("cities") || selected.equals("all")) {
            queueCities();
        }
        if (selected.equals("rail") || selected.equals("all")) {
            queueRail();
        }
        worldMode = false;
        total = QUEUE.size();
        done = 0;
        failures = 0;
        tickCounter = 0;
        beginRun();
        if (running) {
            State state = State.of(server);
            state.done = false;
            state.chunks = 0;
            state.setDirty();
        }
        EldritchHorror.LOGGER.info("pregen: {} chunks queued ({})", total, selected);
        return running ? "pregen started: " + total + " chunks" : "pregen: nothing to generate";
    }

    private static String startWorld(MinecraftServer server) {
        if (running && worldMode) {
            return "pregen world: already running (" + status() + ")";
        }
        stop();
        State state = State.of(server);
        if (state.worldComplete) {
            return "pregen world: already complete (" + WORLD_TOTAL + " chunks)";
        }
        cursorX = Math.clamp(state.worldX, MIN_X, MAX_X);
        cursorZ = Math.clamp(state.worldZ, MIN_Z, MAX_Z);
        done = Math.max(0, state.worldDone);
        worldMode = true;
        total = WORLD_TOTAL;
        failures = 0;
        tickCounter = 0;
        beginRun();
        EldritchHorror.LOGGER.info(
                "pregen: world run {}/{} chunks, resuming at {} {} ({} GiB free)",
                done, total, cursorX, cursorZ, freeGiB(server));
        return "pregen world started: " + done + "/" + total + " at " + cursorX + " " + cursorZ;
    }

    private static void beginRun() {
        startedNanos = System.nanoTime();
        sampleNanos = startedNanos;
        sampleDone = done;
        rate = 0.0;
        running = total > done;
    }

    /** Stops the current run and drops its queue; a world run keeps its cursor. */
    public static void stop() {
        if (running) {
            EldritchHorror.LOGGER.info("pregen: stopped at {}/{}", done, total);
        }
        running = false;
        worldMode = false;
        pausedForPlayers = false;
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
        String head = String.format(Locale.ROOT,
                "pregen: %d/%d (%.1f%%), %.1f chunks/s, ETA %s, %d failed",
                done, total, 100.0 * done / Math.max(1, total), rate, formatEta(eta), failures);
        if (worldMode && pausedForPlayers) {
            head += ", paused (players online)";
        }
        return worldMode ? head + ", at " + cursorX + " " + cursorZ : head;
    }

    /** @return free GiB on the world volume, or {@code -1} if it cannot be read. */
    public static long freeGiB(MinecraftServer server) {
        try {
            FileStore store = Files.getFileStore(server.getWorldPath(LevelResource.ROOT));
            return store.getUsableSpace() >> 30;
        } catch (IOException | RuntimeException error) {
            return -1L;
        }
    }

    /** @return the persisted completion flag for this world, for logs and admins. */
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
        boolean players = server.getPlayerList().getPlayerCount() > 0;
        if (worldMode) {
            tickWorld(server, level, players);
        } else {
            tickQueue(server, level, players);
        }
        if (done - sampleDone >= RATE_PERIOD || tickCounter % 20 == 0) {
            double seconds = (System.nanoTime() - sampleNanos) / 1.0E9;
            if (seconds >= 5.0) {
                rate = (done - sampleDone) / seconds;
                sampleNanos = System.nanoTime();
                sampleDone = done;
            }
        }
    }

    private static void tickQueue(MinecraftServer server, ServerLevel level, boolean players) {
        int batch = players ? (tickCounter++ % BUSY_PERIOD == 0 ? 1 : 0) : EMPTY_BATCH;
        for (int i = 0; i < batch && !QUEUE.isEmpty(); i++) {
            ChunkPos chunk = QUEUE.poll();
            generate(level, chunk);
            done++;
            if (done % LOG_EVERY == 0) {
                EldritchHorror.LOGGER.info(status());
            }
        }
        if (QUEUE.isEmpty()) {
            State state = State.of(server);
            state.done = true;
            state.chunks = done;
            state.setDirty();
            finishRun();
        }
    }

    private static void tickWorld(MinecraftServer server, ServerLevel level, boolean players) {
        if (players) {
            pausedForPlayers = true;
            // A dedicated pregen phase: do not fight players for the tick budget.
            long now = System.nanoTime();
            if (now - lastWaitLogNanos > WAIT_LOG_NANOS) {
                lastWaitLogNanos = now;
                EldritchHorror.LOGGER.info(
                        "pregen: world run paused while players are online ({} done)", done);
            }
            return;
        }
        pausedForPlayers = false;
        int batch = EMPTY_BATCH;
        for (int i = 0; i < batch; i++) {
            ChunkPos chunk = new ChunkPos(cursorX, cursorZ);
            generate(level, chunk);
            done++;
            if (!advanceCursor()) {
                State state = State.of(server);
                state.worldComplete = true;
                state.worldDone = done;
                state.setDirty();
                finishRun();
                return;
            }
            if (done % CURSOR_SAVE_EVERY == 0) {
                saveWorldCursor(server);
            }
            if (done % LOG_EVERY == 0) {
                EldritchHorror.LOGGER.info(status());
            }
            if (done % (CURSOR_SAVE_EVERY * 4) == 0) {
                long free = freeGiB(server);
                if (free >= 0 && free < MIN_FREE_GIB) {
                    saveWorldCursor(server);
                    EldritchHorror.LOGGER.warn(
                            "pregen: world run stopping, only {} GiB free on the world volume", free);
                    running = false;
                    return;
                }
            }
        }
    }

    /** @return {@code false} once the whole cylinder has been walked. */
    private static boolean advanceCursor() {
        cursorX++;
        if (cursorX > MAX_X) {
            cursorX = MIN_X;
            cursorZ++;
        }
        return cursorZ <= MAX_Z;
    }

    private static void generate(ServerLevel level, ChunkPos chunk) {
        try {
            level.getChunk(chunk.x(), chunk.z(), ChunkStatus.FULL, true);
        } catch (RuntimeException error) {
            failures++;
            EldritchHorror.LOGGER.warn("pregen: failed at {} {}", chunk.x(), chunk.z(), error);
        }
    }

    private static void finishRun() {
        running = false;
        double seconds = Math.max(0.001, (System.nanoTime() - startedNanos) / 1.0E9);
        EldritchHorror.LOGGER.info("pregen: complete, {} chunks in {}s ({} failed)",
                done, Math.round(seconds), failures);
    }

    private static void saveWorldCursor(MinecraftServer server) {
        State state = State.of(server);
        state.worldX = cursorX;
        state.worldZ = cursorZ;
        state.worldDone = done;
        state.setDirty();
    }

    private static String formatEta(long seconds) {
        if (seconds < 0) {
            return "?";
        }
        if (seconds < 120) {
            return seconds + "s";
        }
        if (seconds < 7200) {
            return (seconds / 60) + "m";
        }
        if (seconds < 172800) {
            return String.format(Locale.ROOT, "%.1fh", seconds / 3600.0);
        }
        return String.format(Locale.ROOT, "%.1fd", seconds / 86400.0);
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        String pregen = System.getProperty("eh.pregen");
        if (pregen == null || pregen.isBlank()) {
            return;
        }
        String scope = pregen.toLowerCase(Locale.ROOT);
        if (scope.equals("world")) {
            EldritchHorror.LOGGER.info("pregen: {}", startWorld(event.getServer()));
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
        if (running && worldMode) {
            saveWorldCursor(event.getServer());
        }
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

    /** Per-world marker and world-job cursor. */
    private static final class State extends SavedData {

        private static final Codec<State> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.BOOL.optionalFieldOf("done", false).forGetter(state -> state.done),
                Codec.INT.optionalFieldOf("chunks", 0).forGetter(state -> state.chunks),
                Codec.BOOL.optionalFieldOf("world_complete", false)
                        .forGetter(state -> state.worldComplete),
                Codec.INT.optionalFieldOf("world_x", MIN_X).forGetter(state -> state.worldX),
                Codec.INT.optionalFieldOf("world_z", MIN_Z).forGetter(state -> state.worldZ),
                Codec.INT.optionalFieldOf("world_done", 0).forGetter(state -> state.worldDone)
        ).apply(instance, State::new));

        private static final SavedDataType<State> TYPE =
                new SavedDataType<>(EldritchHorror.id("pregen"), State::new, CODEC);

        private boolean done;
        private int chunks;
        private boolean worldComplete;
        private int worldX = MIN_X;
        private int worldZ = MIN_Z;
        private int worldDone;

        private State() {
        }

        private State(boolean done, int chunks, boolean worldComplete,
                      int worldX, int worldZ, int worldDone) {
            this.done = done;
            this.chunks = chunks;
            this.worldComplete = worldComplete;
            this.worldX = worldX;
            this.worldZ = worldZ;
            this.worldDone = worldDone;
        }

        private static State of(MinecraftServer server) {
            return server.overworld().getDataStorage().computeIfAbsent(TYPE);
        }
    }
}
