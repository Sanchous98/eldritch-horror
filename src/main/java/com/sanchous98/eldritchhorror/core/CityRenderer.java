package com.sanchous98.eldritchhorror.core;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.world.city.Cities;
import com.sanchous98.eldritchhorror.world.city.City;
import com.sanchous98.eldritchhorror.world.loc.Location;
import com.sanchous98.eldritchhorror.world.loc.Locations;
import com.sanchous98.eldritchhorror.world.loc.city.CityLocation;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.imageio.ImageIO;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Dev-only, server-side top-down + isometric PNG renderer for the generated cities.
 *
 * <p>There is no GPU client in this environment, so this class walks the already-generated chunks
 * on the server thread and paints each column with the game's own map colours. It is inert unless
 * the {@code eh.renderCities} system property is set (see
 * {@code world.ModWorldGenEvents.onServerStarted}).
 *
 * <p><b>Why this is tick-sliced.</b> The first version ran the whole job inside
 * {@code ServerStartedEvent} on the server thread. Sampling a large city (500k+ columns) blocked
 * the main thread for tens of seconds, so the dedicated server's watchdog ("Watching Server")
 * declared "a single server tick took 60.00 seconds" and force-shut the server down after a
 * handful of cities. The render now runs one or two cities per server tick, so no tick ever
 * approaches the watchdog limit. It can also be split across runs with a batch selector.
 *
 * <p>Output: {@code run/render/<city_id>_top.png} and {@code run/render/<city_id>_iso.png}.
 */
@EventBusSubscriber(modid = EldritchHorror.MODID)
public final class CityRenderer {

    /** Hard clamp on the render radius so a single city cannot try to generate the whole world. */
    private static final int MAX_RENDER_RADIUS = 500;
    /** Fallback colour (opaque grey) when a block reports no map colour. */
    private static final int FALLBACK_ARGB = 0xFF808080;
    /**
     * Upper bound used only to thin the load grid if a pathological radius asks for more chunks
     * than this. With {@link #MAX_RENDER_RADIUS} this never triggers in practice.
     */
    private static final int MAX_RENDER_CHUNKS = 5000;
    /**
     * Chunk generation is sliced to at most this long per server tick, so a multi-thousand-chunk
     * city is built over several ticks with a wide margin below the server watchdog (60 s).
     */
    private static final long CHUNK_GEN_BUDGET_NANOS = 12_000_000_000L; // 12 s

    private enum Phase { IDLE, GENERATE, SAMPLE, ISO, DONE }

    /** One city being rendered, advanced a slice at a time from the server tick. */
    private static final class CityTask {
        final City city;
        final ServerLevel level;
        final File dir;
        final int radius;
        final int diameter;
        final int minX;
        final int minZ;
        final int cMinX;
        final int cMaxX;
        final int cMinZ;
        final int cMaxZ;
        final List<long[]> chunkRuns = new ArrayList<>();
        final int[] topY;
        final int[] argb;

        Phase phase = Phase.GENERATE;
        boolean ticketHeld;
        int runIndex;
        int runPos;
        int sampleColX;      // 0..diameter
        int sampleColZ;      // 0..diameter
        int row;             // iso outer loop 0..(2*diameter-2)
        int isoInner;        // iso inner index across spanX
        int wallY;           // current y within a wall run
        int wallBottom;
        int wallIdx;         // column index currently extruding (-1 = none)
        int minSurface = Integer.MAX_VALUE;
        int maxSurface = Integer.MIN_VALUE;
        int chunksGenerated;
        int isoW;
        int isoH;
        int isoOffY;
        int isoDepth;
        BufferedImage top;
        BufferedImage iso;

        CityTask(City city, ServerLevel level, File dir) {
            this(city, level, dir, -1);
        }

        /**
         * @param radiusOverride the location's true half-extent when it is a wrapped site, or
         *                       {@code -1} for a normal city (derive the radius from the city).
         */
        CityTask(City city, ServerLevel level, File dir, int radiusOverride) {
            this.city = city;
            this.level = level;
            this.dir = dir;
            this.radius = radiusOverride > 0
                    ? Math.min(radiusOverride, MAX_RENDER_RADIUS) + 20
                    : renderRadius(city);
            this.diameter = this.radius * 2 + 1;
            this.minX = city.x() - this.radius;
            this.minZ = city.z() - this.radius;
            this.cMinX = this.minX >> 4;
            this.cMaxX = (city.x() + this.radius) >> 4;
            this.cMinZ = this.minZ >> 4;
            this.cMaxZ = (city.z() + this.radius) >> 4;
            this.topY = new int[this.diameter * this.diameter];
            this.argb = new int[this.diameter * this.diameter];
        }

        /** Packs the chunk runs (with the sampling step) that {@link #generateSlice} will load. */
        void planChunks() {
            long total = (long) (this.cMaxX - this.cMinX + 1) * (this.cMaxZ - this.cMinZ + 1);
            int step = (int) Math.max(1, Math.ceil(Math.sqrt(total / (double) MAX_RENDER_CHUNKS)));
            for (int cx = this.cMinX; cx <= this.cMaxX; cx += step) {
                for (int cz = this.cMinZ; cz <= this.cMaxZ; cz += step) {
                    this.chunkRuns.add(new long[]{cx, cz});
                }
            }
        }
    }

    private static final class Session {
        final List<CityTask> tasks = new ArrayList<>();
        final List<String> rendered = new ArrayList<>();
        boolean exitWhenDone;
        int savedPauseSeconds = -1;
        int index;
    }

    private static volatile Session active;

    private CityRenderer() {
    }

    /**
     * Prepares a render session. Must be called on the server thread. Cities are rendered one (or
     * two) per server tick by {@link #onTick(ServerTickEvent.Pre)}.
     *
     * @param spec {@code true}/{@code all} for every city, {@code sites} for every registered
     *             second-echelon location that is not a city, a comma list of city names or
     *             location ids, or {@code batch=I/N} / {@code I/N} for slice {@code I}
     *             (1-based) of {@code N}.
     * @param exitWhenDone shut the server down once every selected city is written (single-shot run)
     * @return the number of cities (or wrapped sites) selected
     */
    public static synchronized int begin(MinecraftServer server, String spec, boolean exitWhenDone) {
        if (active != null) {
            EldritchHorror.LOGGER.warn("CityRenderer: a render session is already active");
            return 0;
        }
        File cwd = new File(".").getAbsoluteFile();
        // The dev server's working directory is already the run/ directory; from the project root
        // it is not. Normalise to <project>/run/render either way.
        String cwdName;
        try {
            cwdName = cwd.getCanonicalFile().getName();
        } catch (java.io.IOException e) {
            cwdName = cwd.getName();
        }
        File dir = cwdName.equals("run") ? new File(cwd, "render") : new File(cwd, "run/render");
        if (!dir.exists() && !dir.mkdirs()) {
            EldritchHorror.LOGGER.error("CityRenderer: could not create {}", dir.getAbsolutePath());
            return 0;
        }

        boolean all = spec == null || spec.isBlank()
                || "true".equalsIgnoreCase(spec) || "all".equalsIgnoreCase(spec);
        // `sites` selects the second-echelon Locations (ruins, vaults, scars) that are not cities.
        boolean sites = !all && "sites".equalsIgnoreCase(spec.trim());
        List<String> names = new ArrayList<>();
        int batchIndex = 0;
        int batchSize = 0;
        int[] batch = new int[]{0, 0};
        if (!all && !sites && parseBatch(spec, batch)) {
            batchIndex = batch[0];
            batchSize = batch[1];
        } else if (!all && !sites) {
            for (String part : spec.split(",")) {
                String trimmed = part.trim();
                if (!trimmed.isEmpty()) {
                    names.add(trimmed);
                }
            }
        }

        List<City> selected = new ArrayList<>();
        if (sites) {
            // Every registered Location that is not a curated city, in deterministic registry
            // order. Wrapping keeps the rest of the pipeline untouched.
            for (Location loc : Locations.all()) {
                if (!(loc instanceof CityLocation)) {
                    selected.add(fromLocation(loc));
                }
            }
        } else {
            int ordinal = 0;
            for (City city : Cities.all()) {
                if (batchSize > 0) {
                    if (Math.floorMod(ordinal, batchSize) != batchIndex) {
                        ordinal++;
                        continue;
                    }
                } else if (!names.isEmpty()
                        && names.stream().noneMatch(n -> n.equalsIgnoreCase(city.name())
                                || n.equalsIgnoreCase(city.id()))) {
                    ordinal++;
                    continue;
                }
                selected.add(city);
                ordinal++;
            }
            // A spec token that names a registered site (e.g. a Location id) selects that site in
            // addition to any city matches. byId is a case-insensitive "contains" lookup; city
            // locations are skipped here because the City loop above already covers them.
            if (!names.isEmpty()) {
                List<String> seen = new ArrayList<>();
                for (City c : selected) {
                    seen.add(c.id());
                }
                for (String name : names) {
                    for (Location loc : Locations.byId(name)) {
                        if (loc instanceof CityLocation || seen.contains(loc.id())) {
                            continue;
                        }
                        selected.add(fromLocation(loc));
                        seen.add(loc.id());
                    }
                }
            }
        }

        Session session = new Session();
        session.exitWhenDone = exitWhenDone;
        // Sites are wrapped as synthetic Cities for the shared pipeline, but their built footprint
        // is small and would clamp to the 220-block city floor. Remember each site's render radius
        // (the built extent, NOT the larger cull radius) per id so the render box matches it.
        Map<String, Integer> siteRadii = new LinkedHashMap<>();
        for (City city : selected) {
            if (siteRadii.containsKey(city.id())) {
                continue;
            }
            for (Location loc : Locations.byId(city.id())) {
                if (loc.id().equals(city.id())) {
                    siteRadii.put(city.id(), loc.renderRadius());
                    break;
                }
            }
        }
        for (City city : selected) {
            session.tasks.add(new CityTask(city, server.overworld(), dir, siteRadii.getOrDefault(city.id(), -1)));
        }
        // A headless render run has no players: the vanilla empty-server pause would stop the
        // server ticking 60 s in and freeze the render. Disable it for the duration.
        if (server instanceof DedicatedServer dedicated && selected.size() > 0) {
            session.savedPauseSeconds = dedicated.pauseWhenEmptySeconds();
            dedicated.setPauseWhenEmptySeconds(0);
        }
        active = session;
        EldritchHorror.LOGGER.info("CityRenderer: session with {} cities{} -> {}",
                selected.size(), batchSize > 0 ? " (batch " + (batchIndex + 1) + "/" + batchSize + ")" : "",
                dir.getAbsolutePath());
        return selected.size();
    }

    /** Parses {@code batch=I/N} or {@code I/N}; returns {@code {index0, size}} on success. */
    private static boolean parseBatch(String spec, int[] out) {
        String s = spec.trim();
        if (s.toLowerCase(Locale.ROOT).startsWith("batch")) {
            s = s.substring(5);
            if (s.startsWith("=")) {
                s = s.substring(1);
            }
        }
        int slash = s.indexOf('/');
        if (slash < 0) {
            return false;
        }
        try {
            int i = Integer.parseInt(s.substring(0, slash).trim());
            int n = Integer.parseInt(s.substring(slash + 1).trim());
            if (i < 1 || n < 1 || i > n) {
                return false;
            }
            out[0] = i - 1;
            out[1] = n;
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /** Advances the active session by a bounded slice. Must run on the server thread. */
    @SubscribeEvent
    public static void onTick(ServerTickEvent.Pre event) {
        Session session = active;
        if (session == null) {
            return;
        }
        try {
            MinecraftServer server = event.getServer();
            if (session.index >= session.tasks.size()) {
                finish(server, session);
                return;
            }
            CityTask task = session.tasks.get(session.index);
            advance(task);
            if (task.phase == Phase.DONE) {
                releaseCity(task);
                session.rendered.add(task.city.id());
                session.index++;
                if (session.index >= session.tasks.size()) {
                    finish(server, session);
                }
            }
        } catch (Throwable t) {
            EldritchHorror.LOGGER.error("CityRenderer: tick failed; aborting session", t);
            Session s = active;
            if (s != null) {
                try {
                    if (s.index < s.tasks.size()) {
                        releaseCity(s.tasks.get(s.index));
                    }
                } catch (Throwable ignored) {
                    // best effort
                }
                s.exitWhenDone = true;
                finish(event.getServer(), s);
            }
        }
    }

    /**
     * Adds (or removes) a non-expiring {@link TicketType#FORCED} ticket on every chunk of the
     * render box, keeping them loaded across the tick-sliced generation and sampling. Each ticket
     * uses radius 0 so the chunk sits at exactly the FULL level and is <b>not</b> promoted to
     * ticking (a low ticket level would force ticking-chunk post-processing, which is very slow in
     * bulk and was itself tripping the watchdog). The default chunk-load ticket expires after a
     * single tick, which is why a strong ticket is needed for work spread over many ticks.
     */
    private static void holdRenderBox(CityTask t, boolean hold) {
        var source = t.level.getChunkSource();
        for (int cx = t.cMinX; cx <= t.cMaxX; cx++) {
            for (int cz = t.cMinZ; cz <= t.cMaxZ; cz++) {
                ChunkPos p = new ChunkPos(cx, cz);
                if (hold) {
                    source.addTicketWithRadius(TicketType.FORCED, p, 0);
                } else {
                    source.removeTicketWithRadius(TicketType.FORCED, p, 0);
                }
            }
        }
    }

    /**
     * Releases a finished city's tickets and saves+unloads its chunks, so a multi-city session does
     * not accumulate the whole rendered world in memory. Runs a few chunk-map ticks first so the
     * unload queues drain, then flushes to disk.
     */
    private static void releaseCity(CityTask t) {
        if (t.ticketHeld) {
            try {
                holdRenderBox(t, false);
            } catch (Throwable ex) {
                EldritchHorror.LOGGER.warn("CityRenderer: could not release render box for {}",
                        t.city.id(), ex);
            }
            t.ticketHeld = false;
        }
        try {
            for (int i = 0; i < 4; i++) {
                t.level.getChunkSource().tick(() -> true, false);
            }
            t.level.getChunkSource().save(false);
            // Drop the renderer's own references so the sampled arrays and images can be collected.
            t.top = null;
            t.iso = null;
        } catch (Throwable ex) {
            EldritchHorror.LOGGER.warn("CityRenderer: could not unload {} chunks", t.city.id(), ex);
        }
    }

    private static void finish(MinecraftServer server, Session session) {
        for (CityTask task : session.tasks) {
            if (task.ticketHeld) {
                releaseCity(task);
            }
        }
        if (session.savedPauseSeconds >= 0 && server instanceof DedicatedServer dedicated) {
            dedicated.setPauseWhenEmptySeconds(session.savedPauseSeconds);
        }
        active = null;
        EldritchHorror.LOGGER.info("CityRenderer: session done ({} rendered) into run/render",
                session.rendered.size());
        if (session.exitWhenDone) {
            try {
                server.halt(false);
            } catch (Throwable t) {
                EldritchHorror.LOGGER.warn("CityRenderer: could not halt server", t);
            }
        }
    }

    /** Runs one bounded slice of {@code task}. */
    private static void advance(CityTask t) {
        switch (t.phase) {
            case GENERATE -> generateSlice(t);
            case SAMPLE -> sampleSlice(t);
            case ISO -> isoSample(t);
            default -> t.phase = Phase.DONE;
        }
    }

    // ------------------------------------------------------------------ 1. force generation

    private static void generateSlice(CityTask t) {
        if (t.chunkRuns.isEmpty()) {
            t.planChunks();
            t.chunksGenerated = 0;
            // Hold the whole render box loaded (FORCED = strong, no timeout) so the tick slices of
            // generation and the later sampling see real chunks. Released on finish/unload.
            holdRenderBox(t, true);
            t.ticketHeld = true;
        }
        long start = System.nanoTime();
        while (t.runIndex < t.chunkRuns.size()) {
            long[] run = t.chunkRuns.get(t.runIndex);
            t.level.getChunk((int) run[0], (int) run[1], ChunkStatus.FULL, true);
            t.chunksGenerated++;
            t.runIndex++;
            // Time-slice, checked every few chunks; a city of several thousand chunks is then
            // built over several ticks and no single tick can approach the watchdog limit.
            if (++t.runPos % 8 == 0 && System.nanoTime() - start > CHUNK_GEN_BUDGET_NANOS) {
                return;
            }
        }
        t.phase = Phase.SAMPLE;
        t.sampleColX = 0;
        t.sampleColZ = 0;
        t.minSurface = Integer.MAX_VALUE;
        t.maxSurface = Integer.MIN_VALUE;
    }

    // ------------------------------------------------------------------ 2. column sampling

    private static final int SAMPLE_COLUMNS_PER_TICK = 20_000;

    private static void sampleSlice(CityTask t) {
        int maxY = t.level.getMaxY();
        int minY = t.level.getMinY();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int done = 0;
        while (t.sampleColX < t.diameter && done < SAMPLE_COLUMNS_PER_TICK) {
            int x = t.minX + t.sampleColX;
            int z = t.minZ + t.sampleColZ;
            int surface = minY;
            BlockState top = null;
            // Only read chunks that are actually loaded: `getBlockState` would otherwise re-enter
            // chunk generation (a multi-second stall) if a chunk were ever missing.
            if (t.level.getChunkSource().getChunkNow(x >> 4, z >> 4) != null) {
                for (int y = maxY; y >= minY; y--) {
                    pos.set(x, y, z);
                    BlockState state = t.level.getBlockState(pos);
                    if (!state.isAir()) {
                        surface = y;
                        top = state;
                        break;
                    }
                }
            }
            int idx = t.sampleColX * t.diameter + t.sampleColZ;
            t.topY[idx] = surface;
            t.argb[idx] = top == null ? FALLBACK_ARGB : colourOf(top, t.level, x, surface, z);
            t.minSurface = Math.min(t.minSurface, surface);
            t.maxSurface = Math.max(t.maxSurface, surface);

            if (++t.sampleColZ >= t.diameter) {
                t.sampleColZ = 0;
                t.sampleColX++;
            }
            done++;
        }
        if (t.sampleColX >= t.diameter) {
            if (t.maxSurface <= t.minSurface) {
                t.maxSurface = t.minSurface + 1;
            }
            EldritchHorror.LOGGER.info(
                    "CityRenderer: {} generated {} chunks, sampled {}x{} columns, y {}..{}, radius {}",
                    t.city.id(), t.chunksGenerated, t.diameter, t.diameter,
                    t.minSurface, t.maxSurface, t.radius);

            // Top-down map (north at top: x -> horizontal, z -> vertical).
            t.top = new BufferedImage(t.diameter, t.diameter, BufferedImage.TYPE_INT_ARGB);
            for (int ix = 0; ix < t.diameter; ix++) {
                for (int iz = 0; iz < t.diameter; iz++) {
                    t.top.setRGB(ix, iz, shadedForHeight(t, ix, iz));
                }
            }
            write(t.top, t.dir, t.city.id() + "_top.png");

            // Prepare the isometric canvas.
            int span = t.diameter;
            t.isoDepth = Math.min(t.maxSurface - t.minSurface + 4, 96);
            t.isoW = span + span;
            t.isoH = (span + span) / 2 + t.isoDepth + 4;
            t.isoOffY = t.isoDepth;
            t.iso = new BufferedImage(t.isoW, t.isoH, BufferedImage.TYPE_INT_ARGB);
            t.row = 0;
            t.isoInner = 0;
            t.wallIdx = -1;
            t.phase = Phase.ISO;
        }
    }

    // ------------------------------------------------------------------ 3. isometric walls

    private static final int ISO_COLUMNS_PER_TICK = 6_000;

    private static void isoSample(CityTask t) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int span = t.diameter;
        int processed = 0;
        while (t.row <= span - 1 + span - 1 && processed < ISO_COLUMNS_PER_TICK) {
            if (t.wallIdx >= 0) {
                processed += flushWall(t, pos);
                continue;
            }
            int ix = t.isoInner;
            int iz = t.row - ix;
            t.isoInner++;
            if (t.isoInner >= span) {
                t.isoInner = 0;
                t.row++;
            }
            if (iz < 0 || iz >= span) {
                continue;
            }
            int idx = ix * t.diameter + iz;
            t.wallIdx = idx;
            t.wallBottom = Math.max(t.topY[idx] - t.isoDepth, t.minSurface - 2);
            t.wallY = t.topY[idx];
            processed += flushWall(t, pos);
        }
        if (t.row > span - 1 + span - 1) {
            write(t.iso, t.dir, t.city.id() + "_iso.png");
            t.phase = Phase.DONE;
        }
    }

    /** Paints up to one column's wall run; returns the number of column-steps consumed. */
    private static int flushWall(CityTask t, BlockPos.MutableBlockPos pos) {
        int idx = t.wallIdx;
        if (idx < 0) {
            return 0;
        }
        int ix = idx / t.diameter;
        int iz = idx % t.diameter;
        int x = t.minX + ix;
        int z = t.minZ + iz;
        int span = t.diameter;
        int sx = ix - iz + span - 1;
        int processed = 0;
        for (int y = t.wallY; y >= t.wallBottom; y--) {
            pos.set(x, y, z);
            BlockState st = t.level.getBlockState(pos);
            if (!st.isAir()) {
                // Directional relief: the two world-tangent faces have different brightness, so
                // iso silhouettes read even when a landmark shares its material with the ground.
                int shade = ((ix + iz) & 1) == 0 ? 118 : 86;
                int col = tint(colourOf(st, t.level, x, y, z), shade);
                int sy = (ix + iz) / 2 - (y - t.minSurface) + t.isoOffY;
                if (sx >= 0 && sx < t.isoW && sy >= 0 && sy < t.isoH) {
                    t.iso.setRGB(sx, sy, col);
                }
            }
            processed++;
        }
        t.wallY = t.wallBottom - 1;
        t.wallIdx = -1;
        return processed;
    }

    // ------------------------------------------------------------------ helpers

    /**
     * Wraps a second-echelon {@link Location} as a synthetic {@link City} so the existing
     * city pipeline (which only knows how to read a {@code City}) can render it unchanged. The
     * location id is used for both id and name; the population is inverted from the location's
     * radius so {@link CityLocation#radius()} reproduces it.
     */
    private static City fromLocation(Location loc) {
        return new City(loc.id(), loc.id(), Locations.xOf(loc), Locations.zOf(loc),
                populationForRadius(loc.radius()));
    }

    /**
     * Inverse of {@code CityLocation.radius()}: for a target radius {@code R} in
     * {@code [220, 400]} it returns the population for which
     * {@code clamp(round(220 + sqrt(pop)/40), 220, 400) == R}. Radii below 220 are clamped, so a
     * smaller site is simply rendered as a 220-radius area.
     */
    private static int populationForRadius(int radius) {
        int r = Math.clamp(radius, 220, 400);
        double sqrtPop = (r - 220) * 40.0;
        double pop = sqrtPop * sqrtPop;
        if (pop >= Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        return (int) Math.round(pop);
    }

    /** How far to render around a city: the built district, not the (larger) cull radius. */
    private static int renderRadius(City city) {
        // The built district half-extent (the city's own radius is already clamped to <=400, and a
        // 380 district cap sits below MAX_RENDER_RADIUS), plus a small margin for the rim.
        int district = Math.min(new CityLocation(city).radius(), 380);
        return district + 20;
    }

    /**
     * Shade a top-down pixel by its height above the city's lowest surface, so a tall landmark
     * casts a readable silhouette even when it is built from the same material as the ground
     * (a sandstone pyramid on sand would otherwise vanish).
     */
    private static int shadedForHeight(CityTask t, int ix, int iz) {
        int base = t.argb[ix * t.diameter + iz];
        int h = t.topY[ix * t.diameter + iz] - t.minSurface;
        int pct = 100 + Math.min(h * 2, 55); // taller = brighter, capped
        return tint(base, pct);
    }

    /** Multiply an opaque ARGB int's RGB by {@code pct}/100, keeping it opaque. */
    private static int tint(int argb, int pct) {
        int r = ((argb >> 16) & 0xFF) * pct / 100;
        int g = ((argb >> 8) & 0xFF) * pct / 100;
        int b = (argb & 0xFF) * pct / 100;
        return 0xFF000000 | (Math.min(r, 255) << 16) | (Math.min(g, 255) << 8) | Math.min(b, 255);
    }

    /** Map colour of a state, as an opaque ARGB int, falling back to grey. */
    private static int colourOf(BlockState state, ServerLevel level, int x, int y, int z) {
        try {
            MapColor mapColor = state.getMapColor(level, new BlockPos(x, y, z));
            if (mapColor != MapColor.NONE && mapColor.col != 0) {
                return mapColor.calculateARGBColor(MapColor.Brightness.HIGH);
            }
        } catch (Exception ignored) {
            // fall through to grey
        }
        return FALLBACK_ARGB;
    }

    private static void write(BufferedImage image, File dir, String name) {
        try {
            // Location ids carry a namespace and a ':' or '/' (e.g. eldritch_horror:site/rift_scar);
            // flatten them so the PNG lands directly in run/render instead of a missing subdir.
            String safe = name.replace(':', '_').replace('/', '_').replace('\\', '_');
            File out = new File(dir, safe);
            ImageIO.write(image, "png", out);
            EldritchHorror.LOGGER.info("CityRenderer: wrote {} ({}x{})",
                    out.getAbsolutePath(), image.getWidth(), image.getHeight());
        } catch (Exception e) {
            EldritchHorror.LOGGER.error("CityRenderer: failed to write {}", name, e);
        }
    }
}
