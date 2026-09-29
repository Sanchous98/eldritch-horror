package com.sanchous98.eldritchhorror.world;

import java.io.IOException;

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

    private static volatile EarthMap instance;

    private EarthMaps() {
    }

    /** @return the loaded map, or {@code null} if the layers are missing (never throws). */
    public static EarthMap getOrNull() {
        EarthMap local = instance;
        if (local == null) {
            synchronized (EarthMaps.class) {
                local = instance;
                if (local == null) {
                    try {
                        local = EarthMap.load(EarthMaps.class.getClassLoader(), PIXEL_WIDTH);
                    } catch (IOException e) {
                        throw new IllegalStateException("Earth map layers are missing", e);
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

    public static boolean available() {
        try {
            return get() != null;
        } catch (Throwable t) {
            return false;
        }
    }
}
