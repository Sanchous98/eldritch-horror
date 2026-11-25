package com.sanchous98.eldritchhorror.world.city;

import com.sanchous98.eldritchhorror.EldritchHorror;
import com.sanchous98.eldritchhorror.core.ModConfig;
import com.sanchous98.eldritchhorror.registry.ModAttachments;
import com.sanchous98.eldritchhorror.world.loc.city.CityLocation;
import com.sanchous98.eldritchhorror.world.loc.city.CitySurface;
import com.sanchous98.eldritchhorror.world.loc.city.plan.CityPlans;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Runtime safety net for the <b>authored</b> (plan) cities: removes biome trees that survived the
 * worldgen structure pass, or that were added to an already-built district later.
 *
 * <p>Plan cities return early in {@link CityLocation#build} after replaying the recorded plan and
 * lighting it, so they never ran the procedural {@code paveDistrict} vegetation sweep. The
 * generation-time sweep in {@code CityLocation#sweepPlanVegetation} now covers fresh chunks; this
 * ticker fixes districts that were generated before it, and catches trees that biome decoration or
 * another worldgen step adds around a plan's edges after the structure pass.
 *
 * <p>It runs only for <b>plan</b> cities and only in loaded chunks near a player (four chunks out,
 * inside the district circle), so nothing is force-loaded. A chunk is swept at most once (the
 * {@link ModAttachments#VEGETATION_SWEPT} attachment) and one pass removes at most
 * {@code cityVegetationBlocksPerPass} blocks. Only blocks in {@code BlockTags.LOGS} /
 * {@code BlockTags.LEAVES} above the authored surface are touched; every authored log and leaf
 * position is exempt, so a plan's courtyard palms and garden trees are never removed.
 *
 * <p><b>Authored data:</b> the exemption set comes from {@link CityLocation#authoredVegetation},
 * i.e. the server's in-memory plan cache filled during worldgen. When the city's plan is not
 * cached (a fresh server, or the cache has evicted it) there is no honest way to tell an authored
 * palm from a biome tree, so this pass leaves that city alone rather than risk the plan. A chunk is
 * therefore only marked swept after a real, cache-backed inspection.
 */
@EventBusSubscriber(modid = EldritchHorror.MODID)
public final class CityVegetation {

    private static final int INTERVAL_TICKS = 20;
    /** Chunk radius around a player searched for loaded district chunks. */
    private static final int SEARCH_RADIUS_CHUNKS = 4;

    private CityVegetation() {
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        if (server.getTickCount() % INTERVAL_TICKS != 0) {
            return;
        }
        if (!ModConfig.ENABLE_CITY_VEGETATION.get()) {
            return;
        }
        ServerLevel level = server.overworld();
        int chunkBudget = ModConfig.CITY_VEGETATION_CHUNKS_PER_TICK.get();
        int blockBudget = ModConfig.CITY_VEGETATION_BLOCKS_PER_PASS.get();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (player.level() != level) {
                continue;
            }
            City city = CityStates.nearestCity(level, player.blockPosition());
            if (city == null || CityPlans.forCity(city.name()) == null) {
                continue;
            }
            sweepAround(level, city, player.blockPosition(), chunkBudget, blockBudget);
        }
    }

    private static void sweepAround(ServerLevel level, City city, BlockPos centre,
                                    int chunkBudget, int blockBudget) {
        CityLocation location = new CityLocation(city);
        int district = location.renderRadius();
        CitySurface surface = CityLocation.surfaceOf(city);
        int cx = city.x();
        int cz = city.z();
        int centreChunkX = centre.getX() >> 4;
        int centreChunkZ = centre.getZ() >> 4;
        int[] blocksLeft = {blockBudget};
        int chunksLeft = chunkBudget;
        for (int dx = -SEARCH_RADIUS_CHUNKS; dx <= SEARCH_RADIUS_CHUNKS && chunksLeft > 0; dx++) {
            for (int dz = -SEARCH_RADIUS_CHUNKS; dz <= SEARCH_RADIUS_CHUNKS && chunksLeft > 0; dz++) {
                int chunkX = centreChunkX + dx;
                int chunkZ = centreChunkZ + dz;
                if (!CityReclaimMath.withinDistrict(chunkX - (cx >> 4), chunkZ - (cz >> 4),
                        district)) {
                    continue;
                }
                LevelChunk chunk = level.getChunkSource().getChunkNow(chunkX, chunkZ);
                if (chunk == null || chunk.getData(ModAttachments.VEGETATION_SWEPT.get())) {
                    continue;
                }
                Set<Long> authored = location.authoredVegetation(chunkX, chunkZ);
                if (authored == null) {
                    continue; // plan not cached: cannot tell authored palms from biome trees
                }
                chunksLeft--;
                if (sweepChunk(level, chunk, surface, cx, cz, district, authored, blocksLeft)) {
                    chunk.setData(ModAttachments.VEGETATION_SWEPT.get(), true);
                    chunk.markUnsaved();
                }
                if (blocksLeft[0] <= 0) {
                    return;
                }
            }
        }
    }


    /**
     * Diagnostic: counts the stray logs/leaves the sweep would remove around a point, without
     * touching the world. Used by {@code /eh trees} to explain why a district still has biome
     * trees (chunk not loaded, plan not cached, or nothing left).
     *
     * @param level        overworld
     * @param centre       probe centre
     * @param radiusChunks chunk radius around the centre
     * @return one line per chunk that still holds stray vegetation, plus a summary
     */
    public static List<String> diagnose(ServerLevel level, BlockPos centre, int radiusChunks) {
        List<String> out = new ArrayList<>();
        City city = CityStates.nearestCity(level, centre);
        if (city == null) {
            out.add("no curated city near " + centre.toShortString());
            return out;
        }
        CityLocation location = new CityLocation(city);
        int district = location.renderRadius();
        CitySurface surface = CityLocation.surfaceOf(city);
        int cx = city.x();
        int cz = city.z();
        int district2 = district * district;
        int chunks = 0;
        int visible = 0;
        int stray = 0;
        int swept = 0;
        for (int dx = -radiusChunks; dx <= radiusChunks; dx++) {
            for (int dz = -radiusChunks; dz <= radiusChunks; dz++) {
                int chunkX = (centre.getX() >> 4) + dx;
                int chunkZ = (centre.getZ() >> 4) + dz;
                LevelChunk chunk = level.getChunkSource().getChunkNow(chunkX, chunkZ);
                if (chunk == null) {
                    continue;
                }
                chunks++;
                if (chunk.getData(ModAttachments.VEGETATION_SWEPT.get())) {
                    swept++;
                    continue;
                }
                Set<Long> authored = location.authoredVegetation(chunkX, chunkZ);
                if (authored == null) {
                    out.add(String.format(java.util.Locale.ROOT,
                            "%s chunk (%d,%d): plan not cached, sweep skipped", city.id(), chunkX,
                            chunkZ));
                    continue;
                }
                int found = 0;
                StringBuilder first = new StringBuilder();
                for (int x = chunkX << 4; x < (chunkX << 4) + 16; x++) {
                    int ddx = x - cx;
                    if (ddx * ddx > district2) {
                        continue;
                    }
                    for (int z = chunkZ << 4; z < (chunkZ << 4) + 16; z++) {
                        int ddz = z - cz;
                        if (ddx * ddx + ddz * ddz > district2) {
                            continue;
                        }
                        int s = surface.at(x, z);
                        int top = Math.min(level.getMaxY(),
                                Math.max(s, surface.naturalAt(x, z)) + CityLocation.CLEAR_ABOVE);
                        for (int y = Math.max(level.getMinY(), s + 1); y <= top; y++) {
                            if (authored.contains(BlockPos.asLong(x, y, z))) {
                                continue;
                            }
                            if (!CityLocation.isVegetation(level.getBlockState(
                                    new BlockPos(x, y, z)))) {
                                continue;
                            }
                            found++;
                            stray++;
                            if (first.length() < 46 && found <= 3) {
                                first.append(' ').append(x).append(',').append(y).append(',')
                                        .append(z);
                            }
                        }
                    }
                }
                if (found > 0) {
                    visible++;
                    out.add(String.format(java.util.Locale.ROOT,
                            "%s chunk (%d,%d): %d stray at%s", city.id(), chunkX, chunkZ, found,
                            first));
                }
            }
        }
        out.add(String.format(java.util.Locale.ROOT,
                "%s: %d chunks loaded, %d swept already, %d with stray vegetation, %d stray blocks",
                city.id(), chunks, swept, visible, stray));
        return out;
    }

    /**
     * Sweeps one loaded chunk. Returns {@code true} when the whole chunk was inspected (so it can
     * be marked done), or {@code false} when the block budget ran out and the chunk must be
     * revisited next pass.
     */
    private static boolean sweepChunk(ServerLevel level, LevelChunk chunk, CitySurface surface,
                                      int cx, int cz, int district, Set<Long> authored,
                                      int[] blocksLeft) {
        ChunkPos chunkPos = chunk.getPos();
        BlockState air = Blocks.AIR.defaultBlockState();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        int minY = level.getMinY();
        int maxY = level.getMaxY();
        int district2 = district * district;
        for (int x = chunkPos.getMinBlockX(); x <= chunkPos.getMaxBlockX(); x++) {
            int dx = x - cx;
            if (dx * dx > district2) {
                continue;
            }
            for (int z = chunkPos.getMinBlockZ(); z <= chunkPos.getMaxBlockZ(); z++) {
                int dz = z - cz;
                if (dx * dx + dz * dz > district2) {
                    continue;
                }
                int s = surface.at(x, z);
                int top = Math.min(maxY, Math.max(s, surface.naturalAt(x, z))
                        + CityLocation.CLEAR_ABOVE);
                for (int y = Math.max(minY, s + 1); y <= top; y++) {
                    if (authored.contains(BlockPos.asLong(x, y, z))) {
                        continue; // the plan's own palm or garden tree
                    }
                    cursor.set(x, y, z);
                    if (!CityLocation.isVegetation(level.getBlockState(cursor))) {
                        continue;
                    }
                    if (blocksLeft[0] <= 0) {
                        return false;
                    }
                    level.setBlock(cursor, air, Block.UPDATE_ALL);
                    blocksLeft[0]--;
                }
            }
        }
        return true;
    }
}
