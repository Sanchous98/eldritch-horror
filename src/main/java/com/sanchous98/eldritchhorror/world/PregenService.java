package com.sanchous98.eldritchhorror.world;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.world.city.City;
import com.sanchous98.eldritchhorror.world.city.Cities;
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
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * One-shot, tick-sliced world pre-generation for the authored cities and the rail corridors.
 *
 * <p>Generating 600–800-block cities on first visit is the expensive part of a session; this
 * service walks a bounded chunk queue on the server thread (two chunks per tick) and lets the
 * normal chunk save/unload machinery persist them. `/eh pregen all` starts it; `-Deh.pregen=all`
 * starts it at server boot. It is deliberately not a resident-set loader: only the current two
 * chunks are forced, and the queue itself is a few tens of thousands of {@link ChunkPos} values.
 */
@EventBusSubscriber(modid = EldritchHorror.MODID)
public final class PregenService {

    /** Chunks generated per tick. Kept low so an empty server still ticks smoothly. */
    private static final int BATCH = 2;
    /** Log progress at most this often (chunks). */
    private static final int LOG_EVERY = 500;

    private static final ArrayDeque<ChunkPos> QUEUE = new ArrayDeque<>();
    private static final Set<Long> SEEN = new HashSet<>();
    private static boolean running;
    private static int total;
    private static int done;
    private static long startedNanos;

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
        startedNanos = System.nanoTime();
        running = total > 0;
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
    }

    /** @return progress for `/eh pregen status`. */
    public static String status() {
        if (!running) {
            return "pregen: idle";
        }
        double seconds = Math.max(0.001, (System.nanoTime() - startedNanos) / 1.0E9);
        double rate = done / seconds;
        long remaining = total - done;
        long eta = rate <= 0.0 ? -1L : Math.round(remaining / rate);
        return String.format(Locale.ROOT, "pregen: %d/%d (%.1f%%), %.1f chunks/s, ETA %s",
                done, total, 100.0 * done / Math.max(1, total), rate,
                eta < 0 ? "?" : eta + "s");
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (!running) {
            return;
        }
        ServerLevel level = event.getServer().overworld();
        for (int i = 0; i < BATCH && !QUEUE.isEmpty(); i++) {
            ChunkPos chunk = QUEUE.poll();
            try {
                level.getChunk(chunk.x(), chunk.z(), ChunkStatus.FULL, true);
            } catch (RuntimeException error) {
                EldritchHorror.LOGGER.warn("pregen: failed at {} {}", chunk.x(), chunk.z(), error);
            }
            done++;
            if (done % LOG_EVERY == 0) {
                EldritchHorror.LOGGER.info(status());
            }
        }
        if (QUEUE.isEmpty()) {
            running = false;
            double seconds = Math.max(0.001, (System.nanoTime() - startedNanos) / 1.0E9);
            EldritchHorror.LOGGER.info("pregen: complete, {} chunks in {}s", done,
                    Math.round(seconds));
        }
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        String pregen = System.getProperty("eh.pregen");
        if (pregen != null && !pregen.isBlank()) {
            start(event.getServer(), pregen);
        }
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
}
