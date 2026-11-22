package com.sanchous98.eldritchhorror.world;

import com.sanchous98.eldritchhorror.EldritchHorror;
import java.io.IOException;
import org.jspecify.annotations.Nullable;

/**
 * Server-side access to the baked {@link EarthMap}. The layers are read once, lazily, from
 * the mod jar through this class's class loader (so it works on a dedicated server) and
 * shared read-only across worldgen threads.
 *
 * <p>Never call this from client-only code.
 */
public final class EarthMaps {
    /** Pixel width of the baked layers (8 blocks/pixel). Must match the baked assets. */
    public static final int PIXEL_WIDTH = 16384;

    private static volatile @Nullable EarthMap instance;

    private EarthMaps() {
    }

    /** @return the loaded map, or {@code null} if the layers are missing (never throws). */
    public static @Nullable EarthMap getOrNull() {
        EarthMap local = instance;
        if (local == null) {
            synchronized (EarthMaps.class) {
                local = instance;
                if (local == null) {
                    try {
                        local = EarthMap.load(EarthMaps.class.getClassLoader(), PIXEL_WIDTH);
                    } catch (IOException e) {
                        // The contract is "null if the layers are missing, never throws": log and
                        // let get() turn the null into a clear IllegalStateException for callers
                        // that cannot proceed without the map.
                        EldritchHorror.LOGGER.error("Earth map layers are missing", e);
                        return null;
                    }
                    instance = local;
                }
            }
        }
        return local;
    }

    /** @return the map; throws if missing. */
    public static EarthMap get() {
        EarthMap map = getOrNull();
        if (map == null) {
            throw new IllegalStateException("Earth map layers are missing");
        }
        return map;
    }

    /** @return whether the baked layers loaded; never throws. */
    public static boolean available() {
        return getOrNull() != null;
    }
}
