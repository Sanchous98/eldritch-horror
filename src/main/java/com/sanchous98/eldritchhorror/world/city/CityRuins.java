package com.sanchous98.eldritchhorror.world.city;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.core.ModConfig;
import com.sanchous98.eldritchhorror.corruption.TaintWorld;
import com.sanchous98.eldritchhorror.registry.ModAttachments;
import com.sanchous98.eldritchhorror.world.loc.city.CityLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Materialises a city's ledger state as <b>partial</b> ruins in loaded chunks.
 *
 * <p>Runs only while a player is inside the district, so nothing is force-loaded. Each loaded chunk
 * is upgraded at most once per state (the {@link ModAttachments#RUIN_STAGE} attachment): stage 1
 * converts ~6% of natural surface columns, stage 2 ~16%, stage 3 ~30%. Only the natural
 * soil/rock allow-list from {@link TaintWorld} is touched, so authored buildings, roads and walls
 * survive — the city is damaged, not erased, and no column is ever cleared to air. At the fallen
 * stage some converted columns also get a rubble block on top.
 *
 * <p>Conversion is deterministic from the chunk position and the stage, so a revisit produces the
 * same ruins without storing a block diff.
 */
@EventBusSubscriber(modid = EldritchHorror.MODID)
public final class CityRuins {

    private static final int INTERVAL_TICKS = 20;
    /** Columns converted at stage 0..3 (index = {@link CityState#ordinal()}). */
    private static final double[] CONVERT_CHANCE = {0.0, 0.06, 0.16, 0.30};
    /** Rubble placed above converted columns at the fallen stage. */
    private static final double RUBBLE_CHANCE = 0.12;
    private static final Block[] RUBBLE = {
            Blocks.GRAVEL, Blocks.COBBLESTONE, Blocks.ANDESITE, Blocks.MOSSY_COBBLESTONE,
            Blocks.TUFF};

    private CityRuins() {
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        if (server.getTickCount() % INTERVAL_TICKS != 0) {
            return;
        }
        if (!ModConfig.ENABLE_CITY_RUINS.get()) {
            return;
        }
        ServerLevel level = server.overworld();
        int budget = ModConfig.CITY_RUINS_CHUNKS_PER_TICK.get();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (player.level() != level) {
                continue;
            }
            City city = CityStates.nearestCity(level, player.blockPosition());
            if (city == null) {
                continue;
            }
            int stage = CityStates.stateOf(level, city).ordinal();
            if (stage <= 0) {
                continue;
            }
            upgradeAround(level, city, stage, player.blockPosition(), budget);
        }
    }

    private static void upgradeAround(ServerLevel level, City city, int stage, BlockPos centre,
                                      int budget) {
        int radius = new CityLocation(city).radius();
        int centreChunkX = centre.getX() >> 4;
        int centreChunkZ = centre.getZ() >> 4;
        int left = budget;
        for (int dx = -4; dx <= 4 && left > 0; dx++) {
            for (int dz = -4; dz <= 4 && left > 0; dz++) {
                int chunkX = centreChunkX + dx;
                int chunkZ = centreChunkZ + dz;
                if (!CityReclaimMath.withinDistrict(chunkX - (city.x() >> 4),
                        chunkZ - (city.z() >> 4), radius)) {
                    continue;
                }
                LevelChunk chunk = level.getChunkSource().getChunkNow(chunkX, chunkZ);
                if (chunk == null) {
                    continue;
                }
                if (chunk.getData(ModAttachments.RUIN_STAGE.get()) >= stage) {
                    continue;
                }
                upgrade(level, chunk, stage);
                left--;
            }
        }
    }

    private static void upgrade(ServerLevel level, LevelChunk chunk, int stage) {
        double chance = CONVERT_CHANCE[Math.clamp(stage, 0, CONVERT_CHANCE.length - 1)];
        ChunkPos chunkPos = chunk.getPos();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        long chunkSeed = mix(chunkPos.pack() ^ stage * 0xC2B2AE3D27D4EB4FL);
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int worldX = chunkPos.getMinBlockX() + x;
                int worldZ = chunkPos.getMinBlockZ() + z;
                long hash = mix(chunkSeed ^ worldX * 0x9E3779B97F4A7C15L
                        ^ worldZ * 0xD6E8FEB86659FD93L);
                double roll = (hash >>> 11) * 0x1.0p-53;
                if (roll >= chance) {
                    continue;
                }
                int y = level.getHeight(Heightmap.Types.OCEAN_FLOOR, worldX, worldZ) - 1;
                if (y <= level.getMinY()) {
                    continue;
                }
                cursor.set(worldX, y, worldZ);
                BlockState tainted = TaintWorld.corruptVariant(level.getBlockState(cursor));
                if (tainted == null) {
                    continue;
                }
                level.setBlock(cursor, tainted, Block.UPDATE_ALL);
                if (stage >= 3 && (hash >>> 40 & 0xFF) < (int) (RUBBLE_CHANCE * 256.0)) {
                    BlockPos above = cursor.above();
                    if (level.getBlockState(above).isAir()) {
                        Block rubble = RUBBLE[(int) ((hash >>> 48) % RUBBLE.length)];
                        level.setBlock(above, rubble.defaultBlockState(), Block.UPDATE_ALL);
                    }
                }
            }
        }
        chunk.setData(ModAttachments.RUIN_STAGE.get(), stage);
        chunk.markUnsaved();
    }

    /** SplitMix64 finaliser: stable per-column randomness across visits and restarts. */
    private static long mix(long value) {
        long z = value + 0x9E3779B97F4A7C15L;
        z = (z ^ z >>> 30) * 0xBF58476D1CE4E5B9L;
        z = (z ^ z >>> 27) * 0x94D049BB133111EBL;
        return z ^ z >>> 31;
    }
}
