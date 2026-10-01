package com.sanchous98.eldritchhorror.corruption;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.core.ModConfig;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * The world-effect layer of the per-chunk {@link CorruptionSystem#getTaint taint field}: once a
 * second, a tainted loaded chunk converts a bounded handful of its natural surface blocks to the
 * mod's corrupted variants ({@code tainted_soil}, {@code corrupt_stone}).
 *
 * <p>Mirrors {@link TaintSystem}: overworld-only, server-authoritative, and it only ever touches
 * chunks that are already loaded — the same {@link ServerChunkCache#getChunkNow} / {@code
 * hasChunkAt} gate, so it never loads or generates a chunk. Candidate positions are drawn from a
 * {@link RandomSource} seeded only by the chunk key and the server tick, so conversion is fully
 * deterministic (no {@code Math.random}); the per-chunk and global work is capped.
 *
 * <p>Only a small vanilla allow-list is converted and only the single topmost exposed block of a
 * column — never structures, never block entities, never anything already from this mod. If the
 * taint later falls back below the threshold, conversion simply stops; terrain is <b>not</b>
 * restored yet (documented behaviour, see {@link #convertChunk}).
 */
@EventBusSubscriber(modid = EldritchHorror.MODID)
public final class TaintWorld {

    /** Evaluate once per second, like the rest of the meters. */
    private static final int INTERVAL_TICKS = 20;

    /** Half-width, in chunks, of the patch considered around each player. */
    private static final int CHUNK_RADIUS = 2;

    /** Probe budget per chunk per pass, as a multiple of the configured per-chunk conversion cap. */
    private static final int PROBE_FACTOR = 4;

    /** One in this many successful conversions also emits an ambient particle puff. */
    private static final int PARTICLE_ONE_IN = 4;

    /** Resolved once from the registry; {@code null} until the mod's corrupted blocks exist. */
    private static Block taintedSoil;
    private static Block corruptStone;

    private TaintWorld() {
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (!ModConfig.ENABLE_TAINT_WORLD.get()) {
            return;
        }
        if (event.getServer().getTickCount() % INTERVAL_TICKS != 0) {
            return;
        }
        ServerLevel overworld = event.getServer().getLevel(Level.OVERWORLD);
        if (overworld == null) {
            return;
        }
        int rate = ModConfig.TAINT_WORLD_RATE.get();
        double threshold = ModConfig.TAINT_WORLD_THRESHOLD.get();
        if (rate <= 0) {
            return;
        }
        ensureBlocks();
        // getValue() on a DefaultedRegistry returns Blocks.AIR (never null) for a missing id, so a
        // missing corrupted block would otherwise "convert" terrain to air (delete it).
        if (taintedSoil == Blocks.AIR || corruptStone == Blocks.AIR) {
            return;
        }

        ServerChunkCache cache = overworld.getChunkSource();
        Set<Long> visited = new HashSet<>();
        int tick = event.getServer().getTickCount();
        for (ServerPlayer player : overworld.players()) {
            ChunkPos centre = player.chunkPosition();
            for (int dx = -CHUNK_RADIUS; dx <= CHUNK_RADIUS; dx++) {
                for (int dz = -CHUNK_RADIUS; dz <= CHUNK_RADIUS; dz++) {
                    LevelChunk chunk = cache.getChunkNow(centre.x() + dx, centre.z() + dz);
                    if (chunk == null || !visited.add(chunk.getPos().pack())) {
                        continue;
                    }
                    if (CorruptionSystem.getTaint(chunk) < threshold) {
                        continue;
                    }
                    convertChunk(overworld, chunk, rate, tick);
                }
            }
        }
    }

    /**
     * Converts up to {@code rate} natural surface blocks in {@code chunk}. A fully converted chunk
     * costs only the probe budget and changes nothing: there is no persistent "done" flag, because
     * the allow-list itself is the test. Cleansing below the threshold merely stops future
     * conversions — restored terrain needs a worldgen/palette pass that does not exist yet.
     */
    private static void convertChunk(ServerLevel level, LevelChunk chunk, int rate, int tick) {
        ChunkPos cp = chunk.getPos();
        // Deterministic: the same chunk at the same tick always produces the same candidate list.
        RandomSource random = RandomSource.create(cp.pack() * 0x9E3779B97F4A7C15L ^ tick);
        int changed = 0;
        int probes = rate * PROBE_FACTOR;
        for (int i = 0; i < probes && changed < rate; i++) {
            int x = cp.getMinBlockX() + random.nextInt(16);
            int z = cp.getMinBlockZ() + random.nextInt(16);
            // Never force a load/generate: skip a column whose chunk is not already here.
            if (!level.hasChunkAt(x, z)) {
                continue;
            }
            int surfaceY = level.getHeight(Heightmap.Types.OCEAN_FLOOR, x, z) - 1;
            BlockPos pos = new BlockPos(x, surfaceY, z);
            BlockState old = level.getBlockState(pos);
            if (old.hasBlockEntity()) {
                continue;
            }
            BlockState replacement = corruptFor(old);
            if (replacement == null) {
                continue;
            }
            // Only the exposed top block: skip anything buried by leaves or a build.
            BlockState above = level.getBlockState(pos.above());
            if (!above.isAir() && !above.is(Blocks.WATER)) {
                continue;
            }
            // Require true sky exposure: the topmost block could otherwise be a player's stone roof,
            // path or wall cap (allow-list material with air directly above). A column open to the
            // sky is natural terrain, not a build.
            if (!level.canSeeSky(pos.above())) {
                continue;
            }
            if (!level.setBlock(pos, replacement, Block.UPDATE_ALL)) {
                continue;
            }
            changed++;
            if (random.nextInt(PARTICLE_ONE_IN) == 0) {
                level.sendParticles(ParticleTypes.ASH,
                        x + 0.5, surfaceY + 1.1, z + 0.5,
                        1, 0.3, 0.2, 0.3, 0.0);
            }
        }
    }

    /**
     * The conversion allow-list: natural soil → {@code tainted_soil}, natural rock →
     * {@code corrupt_stone}. Anything else (logs, leaves, ores, our blocks, builds) returns
     * {@code null} and is left alone.
     */
    private static BlockState corruptFor(BlockState state) {
        if (state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.DIRT) || state.is(Blocks.COARSE_DIRT)
                || state.is(Blocks.PODZOL) || state.is(Blocks.MYCELIUM) || state.is(Blocks.MOSS_BLOCK)
                || state.is(Blocks.SAND) || state.is(Blocks.RED_SAND) || state.is(Blocks.GRAVEL)
                || state.is(Blocks.CLAY)) {
            return taintedSoil.defaultBlockState();
        }
        if (state.is(Blocks.STONE) || state.is(Blocks.DEEPSLATE) || state.is(Blocks.GRANITE)
                || state.is(Blocks.DIORITE) || state.is(Blocks.ANDESITE) || state.is(Blocks.TUFF)
                || state.is(Blocks.CALCITE) || state.is(Blocks.DRIPSTONE_BLOCK)) {
            return corruptStone.defaultBlockState();
        }
        return null;
    }

    /** Resolve the mod's corrupted blocks from the registry once (they are registered at setup). */
    private static void ensureBlocks() {
        if (taintedSoil == null) {
            taintedSoil = BuiltInRegistries.BLOCK.getValue(EldritchHorror.id("tainted_soil"));
        }
        if (corruptStone == null) {
            corruptStone = BuiltInRegistries.BLOCK.getValue(EldritchHorror.id("corrupt_stone"));
        }
    }
}
