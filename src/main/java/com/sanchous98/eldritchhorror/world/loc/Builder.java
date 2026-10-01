package com.sanchous98.eldritchhorror.world.loc;

import com.sanchous98.eldritchhorror.world.EarthMap;
import com.sanchous98.eldritchhorror.world.EarthMaps;
import com.sanchous98.eldritchhorror.world.ElevationCurve;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
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
        fill(ax, ay, az, bx, ay, bz, this.palette.ground());
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

    // ------------------------------------------------------------------ shaped ops
    @Override
    public void pitchedRoof(int x0, int z0, int x1, int z1, int y, int height, int ridgeAxis) {
        int ax = Math.min(x0, x1);
        int bx = Math.max(x0, x1);
        int az = Math.min(z0, z1);
        int bz = Math.max(z0, z1);
        if (height < 1) {
            return;
        }
        BlockState stairs = this.palette.roofStairs();
        BlockState slab = this.palette.roofSlab();
        BlockState solid = this.palette.roof();

        // 1. A solid stepped shell: the eave plus each shrunken course. This becomes the roof's
        //    solid underside wherever the surface below is not overwritten.
        fill(ax, y, az, bx, y, bz, solid);
        int topCourse = y;
        for (int i = 0; i < height; i++) {
            int ix0 = ax + (ridgeAxis == 0 ? 0 : i);
            int ix1 = bx - (ridgeAxis == 0 ? 0 : i);
            int iz0 = az + (ridgeAxis == 0 ? i : 0);
            int iz1 = bz - (ridgeAxis == 0 ? i : 0);
            if (ix0 > ix1 || iz0 > iz1) {
                break;
            }
            topCourse = y + 1 + i;
            fill(ix0, topCourse, iz0, ix1, topCourse, iz1, solid);
            // Stop once the ridge has collapsed to a line.
            if ((ridgeAxis == 0 && iz1 - iz0 <= 1) || (ridgeAxis == 1 && ix1 - ix0 <= 1)) {
                break;
            }
        }

        // 2. Dress the visible surfaces: stairs on the slope perimeter, slabs on the flats.
        for (int i = 0; i < height; i++) {
            int course = y + 1 + i;
            int ix0 = ax + (ridgeAxis == 0 ? 0 : i);
            int ix1 = bx - (ridgeAxis == 0 ? 0 : i);
            int iz0 = az + (ridgeAxis == 0 ? i : 0);
            int iz1 = bz - (ridgeAxis == 0 ? i : 0);
            if (ix0 > ix1 || iz0 > iz1 || course > topCourse) {
                break;
            }
            boolean last = (ridgeAxis == 0 && iz1 - iz0 <= 1) || (ridgeAxis == 1 && ix1 - ix0 <= 1);
            if (last) {
                fill(ix0, course, iz0, ix1, course, iz1, slab); // ridge cap
                break;
            }
            if (ridgeAxis == 0) {
                for (int x = ix0; x <= ix1; x++) {
                    put(x, course, iz0, withFacing(stairs, Direction.SOUTH));
                    if (iz1 != iz0) {
                        put(x, course, iz1, withFacing(stairs, Direction.NORTH));
                    }
                }
                for (int x = ix0; x <= ix1; x++) {
                    for (int z = iz0 + 1; z <= iz1 - 1; z++) {
                        put(x, course, z, slab);
                    }
                }
            } else {
                for (int z = iz0; z <= iz1; z++) {
                    put(ix0, course, z, withFacing(stairs, Direction.EAST));
                    if (ix1 != ix0) {
                        put(ix1, course, z, withFacing(stairs, Direction.WEST));
                    }
                }
                for (int z = iz0; z <= iz1; z++) {
                    for (int x = ix0 + 1; x <= ix1 - 1; x++) {
                        put(x, course, z, slab);
                    }
                }
            }
        }
    }

    @Override
    public void spire(int cx, int cz, int baseY, int height) {
        if (height < 1) {
            return;
        }
        Palette p = this.palette;
        int half = 2;
        for (int i = 0; i < height; i++) {
            int y = baseY + i;
            int r = Math.max(0, half - (i * (half + 1)) / height);
            if (r == 0) {
                put(cx, y, cz, p.accent());
                continue;
            }
            // Shell of the shaft.
            for (int x = cx - r; x <= cx + r; x++) {
                for (int z = cz - r; z <= cz + r; z++) {
                    boolean edge = x == cx - r || x == cx + r || z == cz - r || z == cz + r;
                    if (!edge) {
                        continue;
                    }
                    put(x, y, z, ((x + z + i) & 1) == 0 ? p.wall() : p.accent());
                }
            }
            // Narrow window slits up the shaft.
            if (i >= 2 && (i & 1) == 0 && r >= 1) {
                put(cx, y, cz - r, p.window());
                put(cx, y, cz + r, p.window());
                put(cx - r, y, cz, p.window());
                put(cx + r, y, cz, p.window());
            }
        }
        // Stair cap.
        int top = baseY + height;
        put(cx, top, cz, withFacing(p.roofStairs(), Direction.NORTH));
        put(cx, top + 1, cz, p.roofSlab());
        put(cx, top + 2, cz, p.light());
    }

    @Override
    public void buttress(int x, int z, int baseY, int height, Side outward) {
        if (height < 1) {
            return;
        }
        // A SOLID stepped wedge leaning against the wall: each outward step is a full column from
        // the base up to its own height, so nothing floats. (The old version placed a diagonal line
        // of single blocks that hovered in mid-air — the "floating columns" on houses.)
        Direction dir = switch (outward) {
            case N -> Direction.NORTH;
            case S -> Direction.SOUTH;
            case W -> Direction.WEST;
            case E -> Direction.EAST;
        };
        Palette p = this.palette;
        for (int i = 0; i < height; i++) {
            int px = x + dir.getStepX() * i;
            int pz = z + dir.getStepZ() * i;
            int top = baseY + (height - 1 - i); // tall at the wall, tapering to the base outward
            for (int y = baseY; y <= top; y++) {
                put(px, y, pz, y == baseY ? p.foundation() : p.wall());
            }
        }
    }

    @Override
    public void window(int x, int y, int z, int height, int width, boolean vertical) {
        Palette p = this.palette;
        int h = Math.max(1, height);
        int w = Math.max(1, width);
        // A border only exists when there is room for one; a 1-wide slit is all glazing.
        boolean hBorder = h >= 3;
        boolean wBorder = w >= 3;
        for (int dw = 0; dw < w; dw++) {
            for (int dy = 0; dy < h; dy++) {
                boolean border = (hBorder && (dy == 0 || dy == h - 1))
                        || (wBorder && (dw == 0 || dw == w - 1));
                put(x + dw, y + dy, z, border ? p.frame() : p.window());
            }
        }
        // A vertical window gets a projecting stone sill underneath; a horizontal band does not.
        if (vertical) {
            for (int dw = -1; dw <= w; dw++) {
                put(x + dw, y - 1, z, p.roofSlab());
            }
        }
    }

    @Override
    public void crenellations(int x0, int z0, int x1, int z1, int y) {
        int ax = Math.min(x0, x1);
        int bx = Math.max(x0, x1);
        int az = Math.min(z0, z1);
        int bz = Math.max(z0, z1);
        Palette p = this.palette;
        for (int x = ax; x <= bx; x++) {
            for (int z = az; z <= bz; z++) {
                boolean perimeter = x == ax || x == bx || z == az || z == bz;
                if (!perimeter) {
                    continue;
                }
                if (((x + z) & 1) == 0) {
                    put(x, y, z, p.wall());
                    put(x, y + 1, z, p.accent());
                }
            }
        }
    }

    @Override
    public void monument(int cx, int cz, int baseY) {
        Palette p = this.palette;
        // Two-tier square plinth.
        fill(cx - 2, baseY, cz - 2, cx + 2, baseY, cz + 2, p.foundation());
        fill(cx - 1, baseY + 1, cz - 1, cx + 1, baseY + 1, cz + 1, p.foundation());
        // Tapering obelisk: 3x3 shaft, then a 1x1 needle.
        int shaftBase = baseY + 2;
        for (int y = shaftBase; y < shaftBase + 4; y++) {
            for (int x = cx - 1; x <= cx + 1; x++) {
                for (int z = cz - 1; z <= cz + 1; z++) {
                    put(x, y, z, p.accent());
                }
            }
        }
        // Corner quoins pick out the transition.
        for (int y = shaftBase; y < shaftBase + 4; y++) {
            put(cx - 1, y, cz - 1, p.wall());
            put(cx + 1, y, cz + 1, p.wall());
        }
        int tip = shaftBase + 4;
        for (int i = 0; i < 4; i++) {
            put(cx, tip + i, cz, p.accent());
        }
        put(cx, tip + 4, cz, p.light());
    }

    @Override
    public void scatter(int x0, int z0, int x1, int z1, int y0, int y1, BlockState state, float chance) {
        if (state == null || chance <= 0f) {
            return;
        }
        int ax = Math.min(x0, x1);
        int bx = Math.max(x0, x1);
        int az = Math.min(z0, z1);
        int bz = Math.max(z0, z1);
        int ay = Math.min(y0, y1);
        int by = Math.max(y0, y1);
        for (int x = ax; x <= bx; x++) {
            for (int z = az; z <= bz; z++) {
                for (int y = ay; y <= by; y++) {
                    if (this.rng.nextFloat() >= chance) {
                        continue;
                    }
                    if (isReplaceable(x, y, z)) {
                        put(x, y, z, state);
                    }
                }
            }
        }
    }

    @Override
    public void ruins(int x0, int y0, int z0, int x1, int y1, int z1, float holeChance) {
        int ax = Math.min(x0, x1);
        int bx = Math.max(x0, x1);
        int ay = Math.min(y0, y1);
        int by = Math.max(y0, y1);
        int az = Math.min(z0, z1);
        int bz = Math.max(z0, z1);
        BlockState air = Blocks.AIR.defaultBlockState();
        for (int x = ax; x <= bx; x++) {
            for (int z = az; z <= bz; z++) {
                // Only the shell decays; skip the solid interior.
                boolean shell = x == ax || x == bx || z == az || z == bz;
                if (!shell) {
                    continue;
                }
                for (int y = ay; y <= by; y++) {
                    if (this.rng.nextFloat() < holeChance) {
                        put(x, y, z, air);
                    }
                }
            }
        }
    }

    /** Copies a state and sets its horizontal facing, when the state has that property. */
    private static BlockState withFacing(BlockState state, Direction facing) {
        if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            return state.setValue(BlockStateProperties.HORIZONTAL_FACING, facing);
        }
        return state;
    }

    /** True when the cell at (x,y,z) is air or otherwise replaceable, where readable. */
    @Override
    public boolean isReplaceable(int x, int y, int z) {
        // Outside the generating chunk we cannot read reliably; let `put` clip the write instead.
        if (x >> 4 != this.chunk.x() || z >> 4 != this.chunk.z()) {
            return false;
        }
        if (y < this.minY || y > this.maxY) {
            return false;
        }
        BlockState current = this.level.getBlockState(this.pos.set(x, y, z));
        return current.isAir() || current.canBeReplaced();
    }

    // ------------------------------------------------------------------ terrain / context
    @Override
    public int groundY(int x, int z) {
        EarthMap map = EarthMaps.get();
        return Mth.clamp(ElevationCurve.toY(map.elevationMetres(x, z)), this.minY + 1, this.maxY - 1);
    }

    @Override
    public boolean isLand(int x, int z) {
        return EarthMaps.get().isLand(x, z);
    }

    @Override
    public int chunkMinX() {
        return this.chunk.getMinBlockX();
    }

    @Override
    public int chunkMinZ() {
        return this.chunk.getMinBlockZ();
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
