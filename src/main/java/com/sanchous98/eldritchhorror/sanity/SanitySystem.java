package com.sanchous98.eldritchhorror.sanity;

import com.sanchous98.eldritchhorror.EldritchHorror;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;

/**
 * The Sanity system: a per-player RPG meter that drains near the eldritch, in darkness, and
 * while a rift is open, and recovers with rest, light, and (carefully) certain items.
 *
 * <p>Planned shape:
 * <ul>
 *   <li>A registered {@code Attribute} (max sanity) + a server-side per-player value with a sync packet.</li>
 *   <li>Drain/regen sources registered as a small, composable {@code SanitySource} SPI so content
 *       can add its own without touching core.</li>
 *   <li>Thresholds that apply the {@code Madness} effect, hallucinations, and auditory cues.</li>
 * </ul>
 */
public final class SanitySystem {
    /** Attribute id for max sanity. Registered in the attribute registry once implemented. */
    public static final Identifier MAX_SANITY_ID =
            Identifier.fromNamespaceAndPath(EldritchHorror.MODID, "max_sanity");

    private SanitySystem() {
    }
}
