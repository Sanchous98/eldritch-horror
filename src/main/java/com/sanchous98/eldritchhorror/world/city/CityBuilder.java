package com.sanchous98.eldritchhorror.world.city;

import com.sanchous98.eldritchhorror.world.EarthMap;
import com.sanchous98.eldritchhorror.world.EarthMaps;
import com.sanchous98.eldritchhorror.world.ElevationCurve;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Stamps an abstracted city at a curated real coordinate. We deliberately do **not**
 * reproduce the real city plan — only its "vibe", derived from real geography:
 *
 * <ul>
 *   <li>biome/climate (Köppen layer) → palette and roof material,</li>
 *   <li>coastal vs inland (landmask) → harbour docks or a gated inland town.</li>
 * </ul>
 *
 * <p>The footprint scales with the real city's population (see {@link City#radius()}), so the
 * largest cities span a few chunks. Deterministic (seeded by the city id) and server-side.
 *
 * <p>{@link #build} is invoked once per overlapping chunk; writes outside the chunk currently
 * being generated are dropped by {@link #put}, so each chunk keeps only its own slice.
 */
public final class CityBuilder {

    private CityBuilder() {
    }

    /** Builds the parts of {@code city} that fall inside {@code chunk}. */
    public static void build(WorldGenLevel level, City city, ChunkPos chunk) {
        RandomSource rng = RandomSource.create(city.id().hashCode());
        EarthMap map = EarthMaps.get();
        int radius = city.radius();

        // Plaza ground height at the centre (flattening target).
        int ground = surfaceY(level, map, city.x(), city.z());

        // Flatten the ground under the city to a single plaza height.
        for (int dx = -radius - 4; dx <= radius + 4; dx++) {
            for (int dz = -radius - 4; dz <= radius + 4; dz++) {
                int x = city.x() + dx;
                int z = city.z() + dz;
                int h = surfaceY(level, map, x, z);
                if (h < ElevationCurve.SEA_LEVEL) {
                    continue; // don't build into water
                }
                for (int y = h + 1; y <= ground + 1; y++) {
                    put(level, chunk, x, y, z, Blocks.AIR.defaultBlockState());
                }
                for (int y = ground; y >= ground - 3 && y >= h - 3; y--) {
                    put(level, chunk, x, y, z, Blocks.COBBLESTONE.defaultBlockState());
                }
            }
        }

        boolean coastal = isCoastal(map, city.x(), city.z());
        Palette palette = paletteFor(map, city.x(), city.z(), coastal);

        ring(level, chunk, rng, city, ground, radius, palette, coastal);
        for (int i = 0; i < 4 + rng.nextInt(3); i++) {
            double ang = (2 * Math.PI * i / 6) + rng.nextDouble() * 0.3;
            int len = 12 + rng.nextInt(radius - 8);
            for (int d = 0; d <= len; d++) {
                road(level, chunk, ground,
                        city.x() + (int) Math.round(Math.cos(ang) * d),
                        city.z() + (int) Math.round(Math.sin(ang) * d),
                        palette);
            }
        }

        // Buildings in plots (density and cap scale with radius).
        int step = radius >= 30 ? 6 : 5;
        int maxBuildings = 700;
        int built = 0;
        for (int dx = -radius + 3; dx <= radius - 3 && built < maxBuildings; dx += step) {
            for (int dz = -radius + 3; dz <= radius - 3 && built < maxBuildings; dz += step) {
                if (dx * dx + dz * dz > (radius - 3) * (radius - 3)) {
                    continue;
                }
                int x = city.x() + dx;
                int z = city.z() + dz;
                if (surfaceY(level, map, x, z) < ElevationCurve.SEA_LEVEL || rng.nextFloat() < 0.22f) {
                    continue;
                }
                building(level, chunk, rng, x, z, ground, palette);
                built++;
            }
        }

        landmark(level, chunk, city, ground, palette);
        if (coastal) {
            docks(level, map, rng, chunk, city);
        }
    }

    // ------------------------------------------------------------------ writes
    /** Writes only when (x,z) is inside the chunk being generated. */
    private static void put(WorldGenLevel level, ChunkPos chunk, int x, int y, int z, BlockState state) {
        if (x >> 4 != chunk.x() || z >> 4 != chunk.z()) {
            return;
        }
        level.setBlock(new BlockPos(x, y, z), state, 2);
    }

    // ------------------------------------------------------------------ terrain
    private static int surfaceY(WorldGenLevel level, EarthMap map, int x, int z) {
        return Mth.clamp(ElevationCurve.toY(map.elevationMetres(x, z)),
                level.getMinY() + 1, level.getMaxY() - 1);
    }

    private static boolean isCoastal(EarthMap map, int cx, int cz) {
        for (int r = 16; r <= 48; r += 8) {
            for (int i = 0; i < 8; i++) {
                double a = i * Math.PI / 4;
                if (!map.isLand(cx + (int) (Math.cos(a) * r), cz + (int) (Math.sin(a) * r))) {
                    return true;
                }
            }
        }
        return false;
    }

    // ------------------------------------------------------------------ palette
    private record Palette(BlockState wall, BlockState roof, BlockState road, BlockState accent) {
    }

    private static Palette paletteFor(EarthMap map, int x, int z, boolean coastal) {
        int k = map.koppenClass(x, z);
        BlockState wall, roof, accent;
        BlockState road = Blocks.COBBLESTONE.defaultBlockState();
        switch (k) {
            case 4, 5, 6, 7 -> { // arid
                wall = Blocks.SMOOTH_SANDSTONE.defaultBlockState();
                roof = Blocks.SANDSTONE.defaultBlockState();
                accent = Blocks.CUT_SANDSTONE.defaultBlockState();
            }
            case 1, 2, 3 -> { // tropical / savannah
                wall = Blocks.MUD_BRICKS.defaultBlockState();
                roof = Blocks.DARK_OAK_PLANKS.defaultBlockState();
                accent = Blocks.STRIPPED_JUNGLE_LOG.defaultBlockState();
            }
            case 29, 30 -> { // polar
                wall = Blocks.SPRUCE_PLANKS.defaultBlockState();
                roof = Blocks.DEEPSLATE_TILES.defaultBlockState();
                accent = Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState();
            }
            default -> { // temperate
                wall = coastal ? Blocks.STONE_BRICKS.defaultBlockState()
                        : Blocks.OAK_PLANKS.defaultBlockState();
                roof = Blocks.BRICKS.defaultBlockState();
                accent = Blocks.STRIPPED_OAK_LOG.defaultBlockState();
            }
        }
        return new Palette(wall, roof, road, accent);
    }

    // ------------------------------------------------------------------ pieces
    private static void ring(WorldGenLevel level, ChunkPos chunk, RandomSource rng, City city,
                             int ground, int radius, Palette p, boolean coastal) {
        int gate = rng.nextInt(4);
        int step = Math.max(1, radius / 40);
        for (int i = 0; i < 360; i += step) {
            int quadrant = (i / 90) % 4;
            if (quadrant == gate || (coastal && quadrant == (gate + 2) % 4)) {
                continue; // gates
            }
            int x = city.x() + (int) Math.round(Math.cos(Math.toRadians(i)) * radius);
            int z = city.z() + (int) Math.round(Math.sin(Math.toRadians(i)) * radius);
            for (int y = 0; y < 3; y++) {
                put(level, chunk, x, ground + 1 + y, z, p.wall());
            }
        }
    }

    private static void road(WorldGenLevel level, ChunkPos chunk, int ground, int x, int z, Palette p) {
        put(level, chunk, x, ground, z, p.road());
        put(level, chunk, x + 1, ground, z, p.road());
        put(level, chunk, x, ground, z + 1, p.road());
        put(level, chunk, x + 1, ground, z + 1, p.road());
    }

    private static void building(WorldGenLevel level, ChunkPos chunk, RandomSource rng,
                                 int x, int z, int ground, Palette p) {
        int w = 3 + rng.nextInt(3);
        int d = 3 + rng.nextInt(3);
        int h = 2 + rng.nextInt(5) + (rng.nextFloat() < 0.2f ? 3 : 0);
        for (int dx = 0; dx < w; dx++) {
            for (int dz = 0; dz < d; dz++) {
                boolean edge = dx == 0 || dz == 0 || dx == w - 1 || dz == d - 1;
                boolean corner = (dx == 0 || dx == w - 1) && (dz == 0 || dz == d - 1);
                for (int y = 1; y <= h; y++) {
                    if (edge) {
                        put(level, chunk, x + dx, ground + y, z + dz, corner ? p.accent() : p.wall());
                    } else {
                        put(level, chunk, x + dx, ground + y, z + dz, Blocks.AIR.defaultBlockState());
                    }
                }
            }
        }
        for (int dx = -1; dx <= w; dx++) {
            for (int dz = -1; dz <= d; dz++) {
                put(level, chunk, x + dx, ground + h + 1, z + dz, p.roof());
            }
        }
        put(level, chunk, x, ground + 3, z, Blocks.LANTERN.defaultBlockState());
    }

    private static void landmark(WorldGenLevel level, ChunkPos chunk, City city, int ground, Palette p) {
        int cx = city.x();
        int cz = city.z();
        int top = 8 + (city.radius() >= 30 ? 6 : 0);
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                if (Math.abs(dx) != 2 && Math.abs(dz) != 2) {
                    continue;
                }
                for (int y = 1; y <= top; y++) {
                    put(level, chunk, cx + dx, ground + y, cz + dz, p.wall());
                }
            }
        }
        for (int y = top + 1; y <= top + 2; y++) {
            int r = top + 2 - y;
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    put(level, chunk, cx + dx, ground + y, cz + dz, p.roof());
                }
            }
        }
        put(level, chunk, cx, ground + top + 3, cz, Blocks.LANTERN.defaultBlockState());
    }

    private static void docks(WorldGenLevel level, EarthMap map, RandomSource rng, ChunkPos chunk, City city) {
        for (int d = 0; d < 60; d++) {
            for (int i = 0; i < 16; i++) {
                double a = i * Math.PI / 8;
                int x = city.x() + (int) (Math.cos(a) * d);
                int z = city.z() + (int) (Math.sin(a) * d);
                if (!map.isLand(x, z)) {
                    int px = city.x() + (int) (Math.cos(a) * Math.max(0, d - 2));
                    int pz = city.z() + (int) (Math.sin(a) * Math.max(0, d - 2));
                    for (int step = 0; step < 8; step++) {
                        int wx = px + (int) (Math.cos(a) * step);
                        int wz = pz + (int) (Math.sin(a) * step);
                        put(level, chunk, wx, ElevationCurve.SEA_LEVEL, wz, Blocks.OAK_PLANKS.defaultBlockState());
                        put(level, chunk, wx, ElevationCurve.SEA_LEVEL + 1, wz, Blocks.OAK_FENCE.defaultBlockState());
                    }
                    return;
                }
            }
        }
    }
}
