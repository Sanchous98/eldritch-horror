package com.sanchous98.eldritchhorror.rite;

import com.sanchous98.eldritchhorror.EldritchHorror;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

/**
 * The altar block-pattern check (design/05: rites are performed "at an altar, matching a block
 * pattern"). Real, not a box count:
 *
 * <ol>
 *   <li>a {@code altar_core} within {@code radius} blocks of the performer (nearest wins);</li>
 *   <li>at least {@code minMarks} {@code rune_stone}/{@code ritual_chalk} marks around it, none of
 *       them hugging the core (they must stand <b>off</b> it — at least {@value #MIN_MARK_DISTANCE}
 *       blocks away, so a heap of blocks is not a circle);</li>
 *   <li>the marks must <b>surround</b> the core: walking the circle, the largest angular gap
 *       between neighbouring marks is at most {@value #MAX_GAP_DEGREES}°, so marks piled on one side
 *       (or an arc) are refused. This also makes the pattern rotation-aware for free — a ring is
 *       judged by coverage, not by absolute orientation.</li>
 * </ol>
 *
 * <p>Loaded-chunks-only (never force-loads), and the core/mark blocks are resolved by id once. A
 * missing block id is treated as absent, not as a match.
 */
public final class RitualAltar {

    private static final String CORE_ID = "altar_core";
    private static final String RUNE_ID = "rune_stone";
    private static final String CHALK_ID = "ritual_chalk";

    /** A mark closer than this (squared distance) to the core is a heap, not a ring. */
    private static final int MIN_MARK_DISTANCE_SQR = 4;
    /** The largest allowed angular gap between neighbouring marks, in degrees (a half circle). */
    private static final double MAX_GAP_DEGREES = 180.0;

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
     * @return whether a valid altar (a core ringed by enough surrounding marks) stands within
     *     {@code radius} blocks of {@code center}, scanning {@code ±radius} horizontally and
     *     {@code ±3} vertically. Loaded cells only.
     */
    public static boolean valid(ServerLevel level, BlockPos center, int radius, int minMarks) {
        resolve();
        if (core == null) {
            return false; // the altar core is not registered; never gate on an impossible pattern
        }
        BlockPos altarCore = nearestCore(level, center, radius);
        if (altarCore == null) {
            return false;
        }
        return ringed(level, altarCore, radius, minMarks);
    }

    /** The nearest {@code altar_core} to {@code center} inside the scan box, or {@code null}. */
    private static @Nullable BlockPos nearestCore(ServerLevel level, BlockPos center, int radius) {
        BlockPos best = null;
        double bestDistance = Double.MAX_VALUE;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                for (int dy = -3; dy <= 3; dy++) {
                    BlockPos pos = center.offset(dx, dy, dz);
                    if (!level.isLoaded(pos) || !level.getBlockState(pos).is(core)) {
                        continue;
                    }
                    double distance = pos.distSqr(center);
                    if (distance < bestDistance) {
                        bestDistance = distance;
                        best = pos;
                    }
                }
            }
        }
        return best;
    }

    /**
     * Whether {@code minMarks} marks stand off the core and around it. Marks are collected in one
     * pass; the angular coverage is then checked with a sorted gap walk (cheap: a handful of
     * marks).
     */
    private static boolean ringed(ServerLevel level, BlockPos corePos, int radius, int minMarks) {
        List<Double> angles = new ArrayList<>();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (dx * dx + dz * dz < MIN_MARK_DISTANCE_SQR) {
                    continue; // too close to the core: not part of a ring
                }
                for (int dy = -3; dy <= 3; dy++) {
                    BlockPos pos = corePos.offset(dx, dy, dz);
                    if (!level.isLoaded(pos)) {
                        continue;
                    }
                    BlockState state = level.getBlockState(pos);
                    if ((rune != null && state.is(rune)) || (chalk != null && state.is(chalk))) {
                        angles.add(Math.toDegrees(Math.atan2(dz, dx)));
                        break; // one mark per column is enough
                    }
                }
            }
        }
        if (angles.size() < minMarks) {
            return false;
        }
        angles.sort(Double::compare);
        double largestGap = 360.0 + angles.get(0) - angles.get(angles.size() - 1);
        for (int i = 1; i < angles.size(); i++) {
            largestGap = Math.max(largestGap, angles.get(i) - angles.get(i - 1));
        }
        return largestGap <= MAX_GAP_DEGREES;
    }
}
