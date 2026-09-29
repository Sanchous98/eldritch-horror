package com.sanchous98.eldritchhorror.world.loc;

import com.sanchous98.eldritchhorror.world.EarthMap;
import com.sanchous98.eldritchhorror.world.EarthMaps;
import com.sanchous98.eldritchhorror.world.ElevationCurve;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

/**
 * Concrete {@link StructureBuilder}: writes through the {@link WorldGenLevel} of the chunk
 * currently generating. Writes whose (x,z) falls outside that chunk are silently dropped, so a
 * large location is stamped incrementally, one chunk slice at a time.
 *
 * <p>This mirrors how the old {@code CityBuilder} wrote blocks during worldgen: no neighbour
 * updates, no lighting recalculation — plain {@code setBlock} with flags {@code 2}.
 */
public final class Builder implements StructureBuilder {

    /** Same flags the old CityBuilder used: no neighbour updates, no lighting update. */
    private static final int FLAGS = 2;

    private final WorldGenLevel level;
    private final ChunkPos chunk;
    private final RandomSource rng;
    private final Palette palette;
    private final int centerX;
    private final int centerZ;
    private final int minY;
    private final int maxY;
    private final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

    public Builder(WorldGenLevel level, ChunkAccess chunk, RandomSource rng, Palette palette,
                   int centerX, int centerZ) {
        this.level = level;
        this.chunk = chunk.getPos();
        this.rng = rng;
        this.palette = palette;
        this.centerX = centerX;
        this.centerZ = centerZ;
        this.minY = level.getMinY();
        this.maxY = level.getMaxY();
    }

    // ------------------------------------------------------------------ primitive
    @Override
    public void put(int x, int y, int z, BlockState state) {
        if (x >> 4 != this.chunk.x() || z >> 4 != this.chunk.z()) {
            return; // outside the chunk currently generating
        }
        if (y < this.minY || y > this.maxY) {
            return;
        }
        this.level.setBlock(this.pos.set(x, y, z), state, FLAGS);
    }

    // ------------------------------------------------------------------ shapes
    @Override
    public void fill(int x0, int y0, int z0, int x1, int y1, int z1, BlockState state) {
        int ax = Math.min(x0, x1);
        int bx = Math.max(x0, x1);
        int ay = Math.min(y0, y1);
        int by = Math.max(y0, y1);
        int az = Math.min(z0, z1);
        int bz = Math.max(z0, z1);
        for (int x = ax; x <= bx; x++) {
            for (int z = az; z <= bz; z++) {
                for (int y = ay; y <= by; y++) {
                    put(x, y, z, state);
                }
            }
        }
    }

    @Override
    public void walls(int x0, int y0, int z0, int x1, int y1, int z1, BlockState state) {
        int ax = Math.min(x0, x1);
        int bx = Math.max(x0, x1);
        int ay = Math.min(y0, y1);
        int by = Math.max(y0, y1);
        int az = Math.min(z0, z1);
        int bz = Math.max(z0, z1);
        for (int y = ay; y <= by; y++) {
            for (int x = ax; x <= bx; x++) {
                put(x, y, az, state);
                if (bz != az) {
                    put(x, y, bz, state);
                }
            }
            for (int z = az; z <= bz; z++) {
                put(ax, y, z, state);
                if (bx != ax) {
                    put(bx, y, z, state);
                }
            }
        }
    }

    @Override
    public void room(int x0, int y0, int z0, int x1, int y1, int z1, Doorway... doorways) {
        int ax = Math.min(x0, x1);
        int bx = Math.max(x0, x1);
        int ay = Math.min(y0, y1);
        int by = Math.max(y0, y1);
        int az = Math.min(z0, z1);
        int bz = Math.max(z0, z1);

        BlockState air = Blocks.AIR.defaultBlockState();
        // Floor and ceiling.
        fill(ax, ay, az, bx, ay, bz, this.palette.surface());
        fill(ax, by, az, bx, by, bz, this.palette.roof());
        // Walls between.
        if (by - 1 >= ay + 1) {
            walls(ax, ay + 1, az, bx, by - 1, bz, this.palette.wall());
            // Hollow the interior.
            if (bx - 1 >= ax + 1 && bz - 1 >= az + 1) {
                fill(ax + 1, ay + 1, az + 1, bx - 1, by - 1, bz - 1, air);
            }
        }
        // Doorways: a 1x2 opening on the requested wall.
        for (Doorway d : doorways) {
            if (d == null) {
                continue;
            }
            int y = ay + 1;
            switch (d.side()) {
                case N -> { // wall at z = az
                    int x = ax + Mth.clamp(d.offset(), 1, Math.max(1, bx - ax - 1));
                    put(x, y, az, air);
                    put(x, y + 1, az, air);
                }
                case S -> { // wall at z = bz
                    int x = ax + Mth.clamp(d.offset(), 1, Math.max(1, bx - ax - 1));
                    put(x, y, bz, air);
                    put(x, y + 1, bz, air);
                }
                case W -> { // wall at x = ax
                    int z = az + Mth.clamp(d.offset(), 1, Math.max(1, bz - az - 1));
                    put(ax, y, z, air);
                    put(ax, y + 1, z, air);
                }
                case E -> { // wall at x = bx
                    int z = az + Mth.clamp(d.offset(), 1, Math.max(1, bz - az - 1));
                    put(bx, y, z, air);
                    put(bx, y + 1, z, air);
                }
            }
        }
    }

    @Override
    public void ground(int x0, int z0, int x1, int z1, int y0, int y1, BlockState state) {
        int ax = Math.min(x0, x1);
        int bx = Math.max(x0, x1);
        int az = Math.min(z0, z1);
        int bz = Math.max(z0, z1);
        int ay = Math.min(y0, y1);
        int by = Math.max(y0, y1);
        for (int x = ax; x <= bx; x++) {
            for (int z = az; z <= bz; z++) {
                for (int y = ay; y <= by; y++) {
                    put(x, y, z, state);
                }
            }
        }
    }

    // ------------------------------------------------------------------ terrain / context
    @Override
    public int groundY(int x, int z) {
        EarthMap map = EarthMaps.get();
        return Mth.clamp(ElevationCurve.toY(map.elevationMetres(x, z)), this.minY + 1, this.maxY - 1);
    }

    @Override
    public RandomSource rng() {
        return this.rng;
    }

    @Override
    public Palette palette() {
        return this.palette;
    }

    @Override
    public void marker(String name, int x, int y, int z) {
        // TODO: prefer a hand-authored .nbt named `name` under
        // data/eldritch_horror/structure/ if present; no templates emitted yet.
    }

    /** Centre X this builder is building around (for convenience in location code). */
    public int centerX() {
        return this.centerX;
    }

    /** Centre Z this builder is building around. */
    public int centerZ() {
        return this.centerZ;
    }
}
