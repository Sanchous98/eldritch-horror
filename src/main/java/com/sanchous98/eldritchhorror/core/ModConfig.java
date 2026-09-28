package com.sanchous98.eldritchhorror.core;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Mod configuration. Common config is registered in {@code EldritchHorror}; split into
 * {@code STARTUP}/{@code SERVER} (or add a client config on the client-only side) as the
 * systems below come online.
 */
public final class ModConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue ENABLE_SANITY =
            BUILDER.comment("Enable the Sanity system. Disable for a pure-content playthrough.")
                    .define("enableSanity", true);

    public static final ModConfigSpec.DoubleValue SANITY_DRAIN_MULTIPLIER =
            BUILDER.comment("Global multiplier applied to all sanity drain.")
                    .defineInRange("sanityDrainMultiplier", 1.0, 0.0, 10.0);

    public static final ModConfigSpec.BooleanValue ENABLE_CORRUPTION_SPREAD =
            BUILDER.comment("Allow corruption to spread from altars and rifts.")
                    .define("enableCorruptionSpread", true);

    public static final ModConfigSpec.IntValue RITUAL_COOLDOWN_TICKS =
            BUILDER.comment("Minimum ticks between ritual attempts at the same altar.")
                    .defineInRange("ritualCooldownTicks", 40, 0, 72000);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private ModConfig() {
    }
}
