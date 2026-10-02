package com.sanchous98.eldritchhorror.corruption;

import com.sanchous98.eldritchhorror.core.ModConfig;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.server.level.ServerPlayer;

/**
 * Central registry of {@link CorruptionSource}s, mirroring {@link com.sanchous98.eldritchhorror.sanity.SanitySources}:
 * register once, read from any thread, deterministic order.
 *
 * <p>The one source here is the milestone's visible link between the per-chunk taint field and the
 * per-player meter (see {@code design/27-systems-framework.md}). Real sources — rites, tomes,
 * altars, rifts — arrive with their content.
 */
public final class CorruptionSources {

    private static final List<CorruptionSource> SOURCES = new CopyOnWriteArrayList<>();
    private static volatile boolean initialised;

    private CorruptionSources() {
    }

    /** Registers a source. Registration order is iteration order. */
    public static void register(CorruptionSource source) {
        SOURCES.add(source);
    }

    /** Snapshot of all registered sources, in registration order. */
    public static List<CorruptionSource> all() {
        init();
        return List.copyOf(SOURCES);
    }

    /**
     * Registers the built-in sources (idempotent). Called from {@link CorruptionTicker} so the
     * system is complete as soon as the first corruption tick runs.
     */
    static void init() {
        if (initialised) {
            return;
        }
        synchronized (CorruptionSources.class) {
            if (initialised) {
                return;
            }
            register(new TaintSource());
            initialised = true;
        }
    }

    /**
     * Baseline source: standing in a tainted chunk slowly corrupts. Linear in the chunk's taint so
     * a half-tainted chunk is a half-rate drain, and a clean chunk is silent.
     */
    private static final class TaintSource implements CorruptionSource {
        @Override
        public String id() {
            return "taint";
        }

        @Override
        public double deltaPerSecond(ServerPlayer player, CorruptionContext ctx) {
            // Chunk taint is documented 0..1 (CorruptionSystem clamps it) and the rate is >= 0, so
            // the product is already non-negative: no floor/guard is needed for a clean chunk.
            return ctx.chunkTaint() * ModConfig.CORRUPTION_TAINT_RATE.get();
        }
    }
}
