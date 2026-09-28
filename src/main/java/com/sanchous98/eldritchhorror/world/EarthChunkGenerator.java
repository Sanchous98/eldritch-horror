package com.sanchous98.eldritchhorror.world;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.densityfunction.SamplerContext;
import org.jspecify.annotations.Nullable;

/**
 * Chunk generator for the Earth map: builds a solid column from the baked elevation layer,
 * sea level at {@link ElevationCurve#SEA_LEVEL}, stone below and grass/sand/water at the
 * surface. Biomes come from {@link EarthBiomeSource}.
 *
 * <p>Server-side only. Deliberately simple: no caves, ores or noise — the whole point is the
 * authored surface. Those can be layered on later.
 */
public class EarthChunkGenerator extends ChunkGenerator {
    public static final MapCodec<EarthChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            EarthBiomeSource.CODEC.forGetter(g -> (EarthBiomeSource) g.getBiomeSource())
    ).apply(i, (biomeSource) -> new EarthChunkGenerator(biomeSource)));

    private final EarthBiomeSource biomeSource;

    public EarthChunkGenerator(EarthBiomeSource biomeSource) {
        super(biomeSource);
        this.biomeSource = biomeSource;
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    public void spawnOriginalMobs(net.minecraft.server.level.WorldGenRegion region) {
        // no natural spawns here; spawning is handled elsewhere
    }

    @Override
    public int getGenDepth() {
        return 384;
    }

    @Override
    public int getSeaLevel() {
        return ElevationCurve.SEA_LEVEL;
    }

    @Override
    public int getMinY() {
        return -64;
    }

    /** Fills the chunk: solid to the surface, then water if below sea level. */
    @Override
    public CompletableFuture<ChunkAccess> buildTerrain(
            ChunkAccess chunk,
            Blender blender,
            RandomState randomState,
            StructureManager structureManager,
            BiomeManager biomeManager,
            @Nullable WorldGenRegion carverRegion,
            Set<Holder<Biome>> possibleBiomes) {

        int minY = chunk.getMinY();
        int maxY = chunk.getMaxY();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        Heightmap oceanFloor = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
        Heightmap worldSurface = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);
        ChunkPos chunkPos = chunk.getPos();

        for (int lx = 0; lx < 16; lx++) {
            for (int lz = 0; lz < 16; lz++) {
                int worldX = chunkPos.getMinBlockX() + lx;
                int worldZ = chunkPos.getMinBlockZ() + lz;
                EarthMap map = EarthMaps.get();
                int surfaceY = Mth.clamp(ElevationCurve.toY(map.elevationMetres(worldX, worldZ)), minY + 1, maxY - 1);
                boolean ocean = surfaceY < ElevationCurve.SEA_LEVEL;

                for (int y = minY; y <= Math.max(surfaceY, ElevationCurve.SEA_LEVEL); y++) {
                    BlockState state;
                    if (y > surfaceY) {
                        state = Blocks.WATER.defaultBlockState();
                    } else if (y == surfaceY) {
                        state = surfaceBlock(map, worldX, worldZ, surfaceY);
                    } else if (y >= surfaceY - 3) {
                        state = Blocks.DIRT.defaultBlockState();
                    } else {
                        state = Blocks.STONE.defaultBlockState();
                    }
                    chunk.setBlockState(pos.set(lx, y, lz), state);
                    oceanFloor.update(lx, y, lz, state);
                    worldSurface.update(lx, y, lz, state);
                }

                // ensure the block above the water surface is air for lighting
                if (ocean) {
                    chunk.setBlockState(pos.set(lx, ElevationCurve.SEA_LEVEL + 1, lz),
                            Blocks.AIR.defaultBlockState());
                }
            }
        }
        return CompletableFuture.completedFuture(chunk);
    }

    private static BlockState surfaceBlock(EarthMap map, int x, int z, int surfaceY) {
        if (surfaceY < ElevationCurve.SEA_LEVEL) {
            return Blocks.GRAVEL.defaultBlockState(); // seabed
        }
        int koppen = map.koppenClass(x, z);
        return switch (koppen) {
            case 4, 5, 6, 7 -> Blocks.SAND.defaultBlockState();      // arid
            case 29, 30 -> Blocks.POWDER_SNOW.defaultBlockState();    // polar
            default -> Blocks.GRASS_BLOCK.defaultBlockState();
        };
    }

    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types type, LevelHeightAccessor heightAccessor, RandomState randomState) {
        return Mth.clamp(ElevationCurve.toY(EarthMaps.get().elevationMetres(x, z)),
                heightAccessor.getMinY(), heightAccessor.getMaxY());
    }

    @Override
    public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor heightAccessor, RandomState randomState) {
        int minY = heightAccessor.getMinY();
        int surfaceY = Mth.clamp(ElevationCurve.toY(EarthMaps.get().elevationMetres(x, z)), minY + 1, heightAccessor.getMaxY() - 1);
        BlockState[] states = new BlockState[heightAccessor.getHeight()];
        for (int i = 0; i < states.length; i++) {
            int y = minY + i;
            if (y > Math.max(surfaceY, ElevationCurve.SEA_LEVEL)) {
                states[i] = Blocks.AIR.defaultBlockState();
            } else if (y > surfaceY) {
                states[i] = Blocks.WATER.defaultBlockState();
            } else if (y == surfaceY) {
                states[i] = Blocks.GRASS_BLOCK.defaultBlockState();
            } else if (y >= surfaceY - 3) {
                states[i] = Blocks.DIRT.defaultBlockState();
            } else {
                states[i] = Blocks.STONE.defaultBlockState();
            }
        }
        return new NoiseColumn(minY, states);
    }

    @Override
    public void addDebugScreenInfo(List<String> result, RandomState randomState, BlockPos feetPos, SamplerContext samplerContext) {
        EarthMap map = EarthMaps.get();
        result.add(Component.literal(String.format("Earth elevation %.0f m",
                map.elevationMetres(feetPos.getX(), feetPos.getZ()))).getString());
        result.add(Component.literal("Köppen class " + map.koppenClass(feetPos.getX(), feetPos.getZ())).getString());
    }

    @Override
    public int getSpawnHeight(LevelHeightAccessor level) {
        return ElevationCurve.SEA_LEVEL + 1;
    }
}
