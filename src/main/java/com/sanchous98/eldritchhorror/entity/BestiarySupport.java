package com.sanchous98.eldritchhorror.entity;

import com.sanchous98.eldritchhorror.corruption.CorruptionAPI;
import com.sanchous98.eldritchhorror.corruption.CorruptionSystem;
import com.sanchous98.eldritchhorror.core.ModConfig;
import com.sanchous98.eldritchhorror.sanity.SanityAPI;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.block.PathBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;

/**
 * Shared, bounded support for the bestiary: a deterministic per-player spawner and the
 * {@link DreadAura} tick. Both mirror the risen husk's rules exactly —
 * server-authoritative, overworld-only, deterministic {@link RandomSource} seeded from the player
 * id and server tick (never {@code Math.random}), and only ever touching already-loaded chunks
 * ({@code getChunkNow} / {@code getEntitiesOfClass}, which never force-load or generate).
 *
 * <p>Generalises {@link RisenHuskSpawner} so each new mob supplies only its gate, caps and config;
 * no duplicated scan loop.
 */
public final class BestiarySupport {

    /** Candidate attempts per desired spawn; unsuitable columns are simply retried. */
    public static final int PROBE_FACTOR = 16;

    private BestiarySupport() {
    }

    /** Per-mob spawn context gate: dark/tainted, or family-specific (e.g. taint only). */
    @FunctionalInterface
    public interface SpawnGate {
        boolean allows(ServerLevel level, BlockPos spot, boolean dark, boolean tainted);
    }

    /**
     * Tops {@code player} up towards {@code cap} with up to {@code perPass} mobs of {@code type} on
     * valid loaded ground, respecting {@code gate}. Counts only already-loaded entities nearby, so
     * it neither force-loads chunks nor double-counts a horde.
     */
    public static <T extends Mob> void topUp(ServerLevel level, ServerChunkCache cache, ServerPlayer player,
                                             EntityType<T> type, Class<T> typeClass,
                                             int radius, int cap, int perPass, int tick,
                                             SpawnGate gate) {
        if (cap <= 0 || perPass <= 0) {
            return;
        }
        BlockPos origin = player.blockPosition();
        AABB box = player.getBoundingBox().inflate(radius);
        int present = level.getEntitiesOfClass(typeClass, box, mob -> mob.isAlive()).size();
        int budget = Math.min(perPass, cap - present);
        if (budget <= 0) {
            return;
        }

        RandomSource random = RandomSource.create(
                player.getUUID().getMostSignificantBits() ^ tick * 0x9E3779B97F4A7C15L);
        int spawned = 0;
        int attempts = budget * PROBE_FACTOR;
        for (int i = 0; i < attempts && spawned < budget; i++) {
            int x = origin.getX() + random.nextInt(radius * 2 + 1) - radius;
            int z = origin.getZ() + random.nextInt(radius * 2 + 1) - radius;
            LevelChunk chunk = cache.getChunkNow(x >> 4, z >> 4);
            if (chunk == null) {
                continue; // never load/generate a chunk just to spawn
            }
            boolean tainted = CorruptionSystem.getTaint(chunk) >= ModConfig.TAINT_SPREAD_THRESHOLD.get();
            BlockPos spot = surfaceSpot(level, x, z, tainted);
            if (spot == null) {
                continue;
            }
            boolean dark = Monster.isDarkEnoughToSpawn(level, spot, random);
            if (!gate.allows(level, spot, dark, tainted)) {
                continue;
            }
            T mob = type.spawn(level, spot, EntitySpawnReason.EVENT);
            if (mob == null) {
                continue;
            }
            mob.setPersistenceRequired();
            spawned++;
        }
    }

    /**
     * A valid standing spot at {@code (x,z)}: ground below (solid or path), two air blocks of body
     * space and no fluid. On open surface it must see the sky; if {@code allowUnderground} (a
     * tainted chunk) it may also be a cave/indoor floor, so a tainted area genuinely spawns in the
     * dark. Same conservative gate as {@code CityPopulation.surfaceSpot}.
     */
    public static BlockPos surfaceSpot(ServerLevel level, int x, int z, boolean allowUnderground) {
        if (!level.hasChunkAt(x, z)) {
            return null;
        }
        int y = level.getHeight(Heightmap.Types.OCEAN_FLOOR, x, z);
        // On the open surface, use the heightmap column. Tainted chunks may spawn in the dark, so
        // scan downward from the surface for the first valid floor (a cave or indoor ground).
        int lowest = allowUnderground ? level.getMinY() + 1 : y;
        for (int cy = y; cy >= lowest; cy--) {
            BlockPos candidate = new BlockPos(x, cy, z);
            if (!level.isLoaded(candidate)) {
                break; // never read unloaded columns
            }
            BlockState below = level.getBlockState(candidate.below());
            boolean ground = below.isSolidRender() || below.getBlock() instanceof PathBlock;
            if (ground && below.getFluidState().isEmpty()
                    && level.getBlockState(candidate).isAir()
                    && level.getBlockState(candidate.above()).isAir()) {
                if (allowUnderground || level.canSeeSky(candidate)) {
                    return candidate;
                }
            }
        }
        return null;
    }

    /**
     * The single dread-aura pass for one player: counts up to each aura's own {@code auraMaxStack}
     * among loaded mobs within their {@code auraRadius} (bounded by {@code queryRadius}) and writes
     * the summed rate through the public {@code SanityAPI}/{@code CorruptionAPI} facade.
     */
    public static <T extends Mob & DreadAura> void tickAura(ServerPlayer player, Class<T> type,
                                                            double queryRadius) {
        if (queryRadius <= 0.0) {
            return;
        }
        AABB box = player.getBoundingBox().inflate(queryRadius);
        List<T> auras = player.level().getEntitiesOfClass(type, box,
                aura -> aura.isAlive() && aura.isAuraActive()
                        && aura.distanceToSqr(player) <= aura.auraRadius() * aura.auraRadius());
        if (auras.isEmpty()) {
            return;
        }
        int max = Math.max(auras.get(0).auraMaxStack(), 0);
        int count = Math.min(auras.size(), max);
        if (count <= 0) {
            return;
        }
        double total = 0.0;
        for (int i = 0; i < count; i++) {
            total += auras.get(i).auraRateFor(player);
        }
        if (total == 0.0) {
            return;
        }
        switch (auras.get(0).auraAxis()) {
            case SANITY -> SanityAPI.add(player, total);
            case CORRUPTION -> CorruptionAPI.add(player, total);
        }
    }
}
