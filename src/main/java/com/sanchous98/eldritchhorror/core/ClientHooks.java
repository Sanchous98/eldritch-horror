package com.sanchous98.eldritchhorror.core;

/**
 * A dist-safe bridge for client-only actions, so common code can request them without importing a
 * client class. On a dedicated server the slots stay no-ops (the client class that fills them is
 * never loaded); the client fills them at startup. This mirrors the vanilla {@code Player#openItemGui}
 * split (a no-op on the base entity, overridden client-side), without an engine hook.
 */
public final class ClientHooks {

    /** Opens the world-map screen; no-op until the client installs the real opener. */
    public static Runnable openWorldMap = () -> { };

    private ClientHooks() {
    }
}
