package com.sanchous98.eldritchhorror.world;

import com.sanchous98.eldritchhorror.world.city.Cities;
import com.sanchous98.eldritchhorror.world.city.City;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

/**
 * A light, deterministic surface decoration pass: grass, flowers, sparse trees / bamboo / cacti
 * chosen by the real Köppen class. Without it the Earth terrain is literally bare — flat colour
 * with no plants.
 *
 * <p>Chunk-clipped and cheap: it only touches the chunk currently generating, and skips columns
 * inside a curated city's footprint so buildings stay clean.
 */
public final class SurfaceDecorator {

    /** District half-extent cap, mirrored from {@code CityLocation#DISTRICT_CAP}. */
    private static final int CITY_DISTRICT_CAP = 380;
    /**
     * Keep-out margin beyond the built district. Buildings overhang their 13-block lots by at
     * most ~12 blocks (largest footprint + roof eave), so this is the smallest radius that still
     * guarantees no decoration lands under a structure — while keeping the denuded rim thin so
     * the terrain pass bleeds right up to the city and softens its edge.
     */
    private static final int CITY_MARGIN = 16;

    private SurfaceDecorator() {
    }

    /** Decorate the given chunk. Called from {@code applyBiomeDecoration}. */
    public static void decorate(WorldGenLevel level, ChunkAccess chunk) {
        EarthMap map = EarthMaps.get();
        int x0 = chunk.getPos().getMinBlockX();
        int z0 = chunk.getPos().getMinBlockZ();
        // Seeded by chunk so the pattern is stable and independent of neighbours.
        RandomSource rng = RandomSource.create(((long) x0 * 341873128712L) ^ ((long) z0 * 132897987541L));
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int minY = level.getMinY();
        int maxY = level.getMaxY();

        for (int lx = 0; lx < 16; lx++) {
            for (int lz = 0; lz < 16; lz++) {
                int x = x0 + lx;
                int z = z0 + lz;
                if (inCity(map, x, z)) {
                    continue;
                }
                int surfaceY = Mth.clamp(ElevationCurve.toY(map.elevationMetres(x, z)), minY + 1, maxY - 1);
                if (surfaceY <= ElevationCurve.SEA_LEVEL) {
                    continue; // underwater / sea ice: leave it
                }
                pos.set(x, surfaceY + 1, z);
                BlockState ground = level.getBlockState(pos.set(x, surfaceY, z));
                if (ground.is(Blocks.GRASS_BLOCK)) {
                    grassAndFlowers(level, rng, pos, x, surfaceY, z, map.koppenClass(x, z));
                } else if (ground.is(Blocks.SAND)) {
                    desert(level, rng, pos, x, surfaceY, z);
                } else if (ground.is(Blocks.POWDER_SNOW) || ground.is(Blocks.SNOW_BLOCK)) {
                    polar(level, rng, pos, x, surfaceY, z);
                }
            }
        }
    }

    /** Grass tufts and flowers on temperate/tropical grassland. */
    private static void grassAndFlowers(WorldGenLevel level, RandomSource rng, BlockPos.MutableBlockPos pos,
                                        int x, int y, int z, int koppen) {
        if (!level.getBlockState(pos.set(x, y + 1, z)).isAir()) {
            return;
        }
        float r = rng.nextFloat();
        boolean tropical = koppen >= 1 && koppen <= 3;
        if (r < 0.10f) {
            set(level, pos, x, y + 1, z, tropical ? Blocks.FERN : Blocks.SHORT_GRASS);
        } else if (r < 0.14f) {
            set(level, pos, x, y + 1, z, Blocks.DANDELION);
        } else if (r < 0.16f && tropical) {
            set(level, pos, x, y + 1, z, Blocks.RED_TULIP);
        } else if (r < 0.17f) {
            tree(level, rng, pos, x, y, z, tropical);
        }
    }

    /** Cacti, dead bushes and dry grass on desert / steppe. */
    private static void desert(WorldGenLevel level, RandomSource rng, BlockPos.MutableBlockPos pos,
                               int x, int y, int z) {
        float r = rng.nextFloat();
        if (r < 0.02f) {
            int h = 2 + rng.nextInt(3);
            for (int i = 1; i <= h; i++) {
                set(level, pos, x, y + i, z, Blocks.CACTUS);
            }
        } else if (r < 0.05f) {
            set(level, pos, x, y + 1, z, Blocks.DEAD_BUSH);
        }
    }

    /** A little snow-dusted spruce on polar ground. */
    private static void polar(WorldGenLevel level, RandomSource rng, BlockPos.MutableBlockPos pos,
                              int x, int y, int z) {
        if (rng.nextFloat() < 0.01f) {
            tree(level, rng, pos, x, y, z, false);
        }
    }

    /** A tiny tree: a short trunk and a leaf canopy; bamboo in the tropics. */
    private static void tree(WorldGenLevel level, RandomSource rng, BlockPos.MutableBlockPos pos,
                             int x, int y, int z, boolean tropical) {
        if (tropical) {
            int h = 4 + rng.nextInt(6);
            for (int i = 1; i <= h; i++) {
                set(level, pos, x, y + i, z, Blocks.BAMBOO);
            }
            return;
        }
        int h = 3 + rng.nextInt(3);
        for (int i = 1; i <= h; i++) {
            set(level, pos, x, y + i, z, Blocks.SPRUCE_LOG);
        }
        int top = y + h;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                for (int dy = 0; dy <= 1; dy++) {
                    if (Math.abs(dx) == 1 && Math.abs(dz) == 1 && dy == 0) {
                        continue;
                    }
                    if (level.getBlockState(pos.set(x + dx, top + dy, z + dz)).isAir()) {
                        set(level, pos, x + dx, top + dy, z + dz, Blocks.SPRUCE_LEAVES);
                    }
                }
            }
        }
        set(level, pos, x, top + 2, z, Blocks.SPRUCE_LEAVES);
    }

    private static void set(WorldGenLevel level, BlockPos.MutableBlockPos pos,
                            int x, int y, int z, net.minecraft.world.level.block.Block block) {
        level.setBlock(pos.set(x, y, z), block.defaultBlockState(), 2);
    }

    /** True if (x,z) lies inside a curated city's built footprint (keep it clear). */
    private static boolean inCity(EarthMap map, int x, int z) {
        for (City c : Cities.all()) {
            // Match the city's *built* district (radius capped at DISTRICT_CAP) plus a small
            // margin — not the larger cull radius. This keeps greenery out from under the
            // buildings while leaving a thin natural rim that softens the district edge.
            // clamp(radius, 220, DISTRICT_CAP), exactly as CityLocation computes its district.
            int district = Math.max(220, Math.min(CITY_DISTRICT_CAP,
                    (int) Math.round(220.0 + Math.sqrt(Math.max(c.population(), 1)) / 40.0)));
            int r = district + CITY_MARGIN;
            long dx = (long) x - c.x();
            long dz = (long) z - c.z();
            if (dx * dx + dz * dz <= (long) r * r) {
                return true;
            }
        }
        return false;
    }
}
