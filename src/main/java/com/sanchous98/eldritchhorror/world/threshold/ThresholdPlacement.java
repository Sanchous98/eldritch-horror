package com.sanchous98.eldritchhorror.world.threshold;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.registry.blocks.CityGateBlock;
import com.sanchous98.eldritchhorror.registry.blocks.PrologueBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

/**
 * Stamps the authored Threshold hub once, when the server starts.
 *
 * <p>The dimension's chunk generator is a plain flat plane (see {@code dimension/threshold.json}),
 * so everything that makes the Threshold a place is built here instead: a central observatory
 * platform with the three class pedestals, a ring of 24 city gates, and a barrier wall that keeps
 * players on the island. The layout is fully deterministic — fixed coordinates, no randomness — so
 * it is reproducible across worlds and can be reviewed as code.
 *
 * <p>Idempotent: the obelisk base at the hub centre is a unique marker block, and the whole stamp
 * is skipped if it is already present. That makes the event safe on every subsequent server start
 * and cheap when the dimension has never been visited.
 *
 * <p>The stamp runs inside force-loaded chunks and releases its tickets afterwards, so the hub
 * chunks are generated (not left permanently loaded) and the island is written to disk. Placement
 * uses {@link Block#UPDATE_ALL} because the hub is small and settled before any player can arrive.
 */
@EventBusSubscriber(modid = EldritchHorror.MODID)
public final class ThresholdPlacement {

    /** Radius of the raised central platform, in blocks. */
    private static final int PLATFORM_RADIUS = 12;

    /** Radius of the ring on which the 24 gates stand. */
    private static final int GATE_RADIUS = 34;
    /** Inner/outer radius of the decorative path band beneath the gates. */
    private static final int GATE_PATH_INNER = GATE_RADIUS - 2;
    private static final int GATE_PATH_OUTER = GATE_RADIUS + 2;

    /** Radius of the surrounding barrier wall (the island's hard edge). */
    private static final int ISLAND_RADIUS = 48;
    /** The barrier is two blocks thick, so no diagonal gap exists at this radius. */
    private static final int BARRIER_INNER = ISLAND_RADIUS - 1;

    /** Height of the island's enclosing wall, in blocks. */
    private static final int BARRIER_HEIGHT = 4;

    /** Number of destination gates, one per curated city. */
    private static final int GATE_COUNT = 24;
    /** Height of each gate arch, in blocks. */
    private static final int GATE_HEIGHT = 3;

    /** How many chunks either side of the origin to force-load around the island. */
    private static final int FORCELOAD_CHUNK_RADIUS = 4;

    /** The obelisk base doubles as the "hub already stamped" marker. */
    private static final BlockPos MARKER = new BlockPos(0, Threshold.FLOOR_Y, 0);

    private ThresholdPlacement() {
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        ServerLevel level = event.getServer().getLevel(Threshold.DIMENSION);
        if (level == null) {
            EldritchHorror.LOGGER.warn("threshold: dimension {} is not loaded; hub not stamped",
                    Threshold.DIMENSION.identifier());
            return;
        }
        if (isStamped(level)) {
            return;
        }
        stamp(level);
        EldritchHorror.LOGGER.info("threshold: stamped hub at {}", Threshold.spawn());
    }

    /** @return whether the hub marker is already present, meaning the hub needs no stamping. */
    private static boolean isStamped(ServerLevel level) {
        return level.getBlockState(MARKER).is(Blocks.CHISELED_DEEPSLATE);
    }

    private static void stamp(ServerLevel level) {
        ServerChunkCache chunks = level.getChunkSource();
        holdHubChunks(chunks, true);
        try {
            generateHubChunks(level);
            buildPlatform(level);
            buildObelisk(level);
            buildPedestals(level);
            buildGateRing(level);
            buildBarrier(level);
        } finally {
            holdHubChunks(chunks, false);
        }
    }

    /**
     * Adds/removes a radius-0 {@link TicketType#FORCED} ticket on every hub chunk. Radius 0 keeps
     * the chunk at FULL without promoting it to a ticking chunk, matching {@code CityRenderer}.
     */
    private static void holdHubChunks(ServerChunkCache chunks, boolean hold) {
        for (int cx = -FORCELOAD_CHUNK_RADIUS; cx <= FORCELOAD_CHUNK_RADIUS; cx++) {
            for (int cz = -FORCELOAD_CHUNK_RADIUS; cz <= FORCELOAD_CHUNK_RADIUS; cz++) {
                ChunkPos pos = new ChunkPos(cx, cz);
                if (hold) {
                    chunks.addTicketWithRadius(TicketType.FORCED, pos, 0);
                } else {
                    chunks.removeTicketWithRadius(TicketType.FORCED, pos, 0);
                }
            }
        }
    }

    /** Forces the hub chunks to FULL synchronously so the placements that follow cannot miss. */
    private static void generateHubChunks(ServerLevel level) {
        for (int cx = -FORCELOAD_CHUNK_RADIUS; cx <= FORCELOAD_CHUNK_RADIUS; cx++) {
            for (int cz = -FORCELOAD_CHUNK_RADIUS; cz <= FORCELOAD_CHUNK_RADIUS; cz++) {
                level.getChunk(cx, cz, ChunkStatus.FULL, true);
            }
        }
    }

    /** The observatory floor: a polished disc with a contrasting rim, set into the flat floor. */
    private static void buildPlatform(ServerLevel level) {
        fillDisc(level, 0, 0, PLATFORM_RADIUS, Threshold.FLOOR_Y, Blocks.POLISHED_DEEPSLATE.defaultBlockState());
        // A contrasting rim so the platform reads as a deliberate structure.
        fillRing(level, 0, 0, PLATFORM_RADIUS - 1, PLATFORM_RADIUS, Threshold.FLOOR_Y,
                Blocks.CHISELED_DEEPSLATE.defaultBlockState());
    }

    /** The central obelisk/instrument marking the safe room's centre. */
    private static void buildObelisk(ServerLevel level) {
        for (int y = 0; y < 4; y++) {
            set(level, 0, Threshold.FLOOR_Y + y, 0, Blocks.CHISELED_DEEPSLATE.defaultBlockState());
        }
        set(level, 0, Threshold.FLOOR_Y + 4, 0, Blocks.SEA_LANTERN.defaultBlockState());
    }

    /** Places the three class pedestals in a triangle around the obelisk. */
    private static void buildPedestals(ServerLevel level) {
        set(level, 0, Threshold.FLOOR_Y, -6, PrologueBlocks.INVESTIGATOR_PEDESTAL.get().defaultBlockState());
        set(level, -5, Threshold.FLOOR_Y, 4, PrologueBlocks.OCCULTIST_PEDESTAL.get().defaultBlockState());
        set(level, 5, Threshold.FLOOR_Y, 4, PrologueBlocks.CULTIST_PEDESTAL.get().defaultBlockState());
    }

    /** Lays the path band and raises the 24 evenly-spaced city gates around the platform. */
    private static void buildGateRing(ServerLevel level) {
        fillRing(level, 0, 0, GATE_PATH_INNER, GATE_PATH_OUTER, Threshold.FLOOR_Y,
                Blocks.DEEPSLATE_TILES.defaultBlockState());
        for (int i = 0; i < GATE_COUNT; i++) {
            double angle = Math.toRadians(i * (360.0 / GATE_COUNT));
            int x = (int) Math.round(GATE_RADIUS * Math.cos(angle));
            int z = (int) Math.round(GATE_RADIUS * Math.sin(angle));
            BlockState gate = PrologueBlocks.CITY_GATE.get().defaultBlockState()
                    .setValue(CityGateBlock.CITY, i);
            for (int y = 0; y < GATE_HEIGHT; y++) {
                set(level, x, Threshold.FLOOR_Y + y, z, gate);
            }
        }
    }

    /** The invisible wall at the island's edge, so a player cannot walk off into the flat plane. */
    private static void buildBarrier(ServerLevel level) {
        BlockState barrier = Blocks.BARRIER.defaultBlockState();
        for (int y = 0; y < BARRIER_HEIGHT; y++) {
            fillRing(level, 0, 0, BARRIER_INNER, ISLAND_RADIUS, Threshold.FLOOR_Y + y, barrier);
        }
    }

    /** Fills every block within {@code radius} of ({@code cx}, {@code cz}) on layer {@code y}. */
    private static void fillDisc(ServerLevel level, int cx, int cz, int radius, int y, BlockState state) {
        int radiusSq = radius * radius;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (dx * dx + dz * dz <= radiusSq) {
                    set(level, cx + dx, y, cz + dz, state);
                }
            }
        }
    }

    /** Fills the annulus between {@code inner} and {@code outer} (inclusive) on layer {@code y}. */
    private static void fillRing(ServerLevel level, int cx, int cz, int inner, int outer, int y, BlockState state) {
        int innerSq = inner * inner;
        int outerSq = outer * outer;
        for (int dx = -outer; dx <= outer; dx++) {
            for (int dz = -outer; dz <= outer; dz++) {
                int distSq = dx * dx + dz * dz;
                if (distSq >= innerSq && distSq <= outerSq) {
                    set(level, cx + dx, y, cz + dz, state);
                }
            }
        }
    }

    private static void set(ServerLevel level, int x, int y, int z, BlockState state) {
        level.setBlock(new BlockPos(x, y, z), state, Block.UPDATE_ALL);
    }
}
