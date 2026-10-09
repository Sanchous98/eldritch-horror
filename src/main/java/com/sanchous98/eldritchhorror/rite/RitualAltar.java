package com.sanchous98.eldritchhorror.rite;

import com.sanchous98.eldritchhorror.EldritchHorror;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

/**
 * The altar block-pattern check (design/05: rites are performed "at an altar, matching a block
 * pattern"). Simplified but real: an {@code altar_core} block within a small box of the performer,
 * marked by at least {@code minMarks} {@code rune_stone} or {@code ritual_chalk} blocks nearby.
 *
 * <p>Loaded-chunks-only (never force-loads), and the core/mark blocks are resolved by id once. A
 * missing block id is treated as absent, not as a match.
 */
public final class RitualAltar {

    private static final String CORE_ID = "altar_core";
    private static final String RUNE_ID = "rune_stone";
    private static final String CHALK_ID = "ritual_chalk";

    private static volatile boolean resolved;
    private static @Nullable Block core;
    private static @Nullable Block rune;
    private static @Nullable Block chalk;

    private RitualAltar() {
    }

    private static void resolve() {
        if (resolved) {
            return;
        }
        core = block(CORE_ID);
        rune = block(RUNE_ID);
        chalk = block(CHALK_ID);
        resolved = true;
    }

    private static @Nullable Block block(String id) {
        Block b = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getValue(EldritchHorror.id(id));
        return b == Blocks.AIR ? null : b;
    }

    /**
     * @return whether a valid altar (a core plus enough marks) stands within {@code radius} blocks
     * of {@code center}, scanning {@code ±radius} horizontally and {@code ±3} vertically. Loaded
     * cells only.
     */
    public static boolean valid(ServerLevel level, BlockPos center, int radius, int minMarks) {
        resolve();
        if (core == null) {
            return false; // the altar core is not registered; never gate on an impossible pattern
        }
        boolean foundCore = false;
        int marks = 0;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                for (int dy = -3; dy <= 3; dy++) {
                    BlockPos pos = center.offset(dx, dy, dz);
                    if (!level.isLoaded(pos)) {
                        continue;
                    }
                    BlockState state = level.getBlockState(pos);
                    if (state.is(core)) {
                        foundCore = true;
                    } else if ((rune != null && state.is(rune)) || (chalk != null && state.is(chalk))) {
                        marks++;
                    }
                }
            }
        }
        return foundCore && marks >= minMarks;
    }
}
