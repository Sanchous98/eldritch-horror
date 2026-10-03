package com.sanchous98.eldritchhorror.event;

import com.sanchous98.eldritchhorror.EldritchHorror;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jspecify.annotations.Nullable;

/**
 * Bounded, loaded-chunks-only lookup of open rift anchors (the {@code rift_anchor} block placed by
 * {@code RiteEngine.OPEN_RIFT}). Used by the event triggers so a rift is detected without editing
 * the rite engine: a rift is simply a block that is there, and a closed rift is one that is gone.
 *
 * <p>Never force-loads or generates a chunk ({@link ServerChunkCache#getChunkNow}). Chunk scans use
 * the vanilla {@link LevelChunk#findBlocks} section predicate, which prunes empty sections, and the
 * result list is capped so a pathological field of anchors cannot blow up memory.
 */
public final class Rifts {

    /** The block id placed by the {@code open_rift} rite. */
    private static final String ANCHOR_ID = "rift_anchor";

    /** Cached resolved anchor block; {@link Blocks#AIR} means it is missing (treat as none). */
    private static @Nullable Block anchorBlock;

    private Rifts() {
    }

    /**
     * Collects up to {@code max} rift anchors in the loaded chunks within {@code chunkRadius} of
     * {@code center}. Returns an immutable list; empty when the anchor block is absent.
     */
    public static List<BlockPos> near(ServerLevel level, BlockPos center, int chunkRadius, int max) {
        Block anchor = resolve();
        if (anchor == null || max <= 0) {
            return List.of();
        }
        List<BlockPos> out = new ArrayList<>();
        ChunkPos origin = ChunkPos.containing(center);
        ServerChunkCache cache = level.getChunkSource();
        for (int dx = -chunkRadius; dx <= chunkRadius && out.size() < max; dx++) {
            for (int dz = -chunkRadius; dz <= chunkRadius && out.size() < max; dz++) {
                LevelChunk chunk = cache.getChunkNow(origin.x() + dx, origin.z() + dz);
                if (chunk == null) {
                    continue; // never load/generate a chunk just to look for a rift
                }
                chunk.findBlocks(state -> state.is(anchor), (pos, state) -> {
                    if (out.size() < max) {
                        out.add(pos.immutable());
                    }
                });
            }
        }
        return List.copyOf(out);
    }

    /** The anchor block, or {@code null} while it is missing (unresolved or {@link Blocks#AIR}). */
    private static @Nullable Block resolve() {
        if (anchorBlock == null) {
            anchorBlock = BuiltInRegistries.BLOCK.getValue(EldritchHorror.id(ANCHOR_ID));
        }
        return anchorBlock == Blocks.AIR ? null : anchorBlock;
    }
}
