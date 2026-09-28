package com.sanchous98.eldritchhorror.client;

/**
 * Client-only setup: HUD sanity/corruption meters, overlays (madness distortion, breathing
 * vignette), particles (motes, tendrils), and entity renderers.
 *
 * <p>Wired from {@code @EventBusSubscriber(value = Dist.CLIENT, ...)} classes once content
 * exists; registration here must never run on a dedicated server.
 */
public final class ClientSetup {
    private ClientSetup() {
    }
}
