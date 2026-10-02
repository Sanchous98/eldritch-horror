package com.sanchous98.eldritchhorror.world.threshold;

import com.sanchous98.eldritchhorror.EldritchHorror;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NullMarked;

/**
 * The authored {@code eldritch_horror:threshold} dimension and its fixed geometry. The dimension
 * itself is an infinite flat plane of bedrock + stone (see {@code dimension/threshold.json}); the
 * actual hub — platform, gate ring and barrier — is stamped once by {@link ThresholdPlacement},
 * so the layout is deterministic and version-controlled rather than terrain-generated.
 *
 * <p>All vertical constants are expressed against the dimension's {@code min_y} of {@code -64}: the
 * generator's top solid block sits at {@link #FLOOR_Y} and a player stands at {@link #SURFACE_Y}.
 */
@NullMarked
public final class Threshold {

    /** The dimension key, matching {@code data/eldritch_horror/dimension/threshold.json}. */
    public static final ResourceKey<Level> DIMENSION =
            ResourceKey.create(Registries.DIMENSION, EldritchHorror.id("threshold"));

    /** The dimension's lowest block, from {@code min_y} in the dimension type. */
    public static final int MIN_Y = -64;

    /** The generator's top solid layer; also where floor decoration is applied. */
    public static final int FLOOR_Y = MIN_Y + 1;

    /** The first air layer above the floor: the level a player's feet occupy. */
    public static final int SURFACE_Y = FLOOR_Y + 1;

    private Threshold() {
    }

    /**
     * The fixed arrival point: on the platform floor, two blocks south of the central obelisk so
     * the player faces it.
     */
    public static BlockPos spawn() {
        return new BlockPos(0, SURFACE_Y, 2);
    }
}
