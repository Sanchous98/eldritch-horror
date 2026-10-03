package com.sanchous98.eldritchhorror.world.loc.site;

import com.sanchous98.eldritchhorror.world.loc.Location;
import com.sanchous98.eldritchhorror.world.loc.Palette;
import com.sanchous98.eldritchhorror.world.loc.StructureBuilder;
import com.sanchous98.eldritchhorror.world.loc.Tier;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The Temple of the Feaster — the hall of Chaugnar Faugn (design/28). A squat, blood-stained
 * temple of red nether brick with a vast open maw of an entrance on its north face and a long
 * feasting hall within, laid with a red runner, a high table and flanking pillars. The hunger
 * here is a thing you hear before you see it.
 *
 * <p>The footprint is levelled first; the temple shell is written as boxes, then its walls are
 * punched through to open the maw and a dark chancel behind the table.
 */
public final class TempleOfTheFeaster implements Location {

    /** Half-width of the temple footprint (blocks). */
    private static final int HALF = 22;
    /** Interior floor course (above the levelled datum). */
    private static final int FLOOR = 1;
    /** Interior ceiling course. */
    private static final int ROOF = 12;
    /** Half-width of the maw opening on the north face. */
    private static final int MAW = 4;
    /** Radius of the levelled courtyard. */
    private static final int FIELD = HALF + 10;
    /** Half-width of the raised chancel at the far end. */
    private static final int CHANCEL = 5;

    private final int centerX;
    private final int centerZ;

    /** Constructed at a fixed centre; {@link #build} has no coordinate argument. */
    public TempleOfTheFeaster(int x, int z) {
        this.centerX = x;
        this.centerZ = z;
    }

    @Override
    public String id() {
        return "eldritch_horror:site/temple_of_the_feaster";
    }

    @Override
    public Tier tier() {
        return Tier.MINOR;
    }

    @Override
    public int radius() {
        return 60; // cull radius — unchanged
    }

    @Override
    public int renderRadius() {
        return FIELD + 4; // the levelled courtyard, not the larger cull radius
    }

    @Override
    public void build(StructureBuilder b) {
        int cx = this.centerX;
        int cz = this.centerZ;
        int ground = baseLevel(b, cx, cz, FIELD);
        Palette p = b.palette();

        // Level the courtyard to a single datum.
        level(b, cx, cz, ground, FIELD, p);

        // The squat crimson shell.
        temple(b, cx, cz, ground);

        // Cut the vast maw and the side windows.
        openings(b, cx, cz, ground);

        // The feasting hall: runner, high table, flanking pillars, chancel and braziers.
        feast(b, cx, cz, ground);

        b.marker("temple_of_the_feaster", cx, ground + FLOOR, cz);
    }

    // ------------------------------------------------------------------ pieces

    /** The squat crimson shell: a solid stepped plinth, hollow hall, and a low gabled roof. */
    private static void temple(StructureBuilder b, int cx, int cz, int ground) {
        BlockState brick = Blocks.RED_NETHER_BRICKS.defaultBlockState();
        BlockState dark = Blocks.NETHER_BRICKS.defaultBlockState();
        BlockState tile = Blocks.DEEPSLATE_TILES.defaultBlockState();
        BlockState conc = Blocks.CONCRETE.pick(DyeColor.RED).defaultBlockState();
        int x0 = b.chunkMinX();
        int z0 = b.chunkMinZ();

        // Raised base course, 2 tall, slightly wider than the walls.
        for (int x = x0; x < x0 + 16; x++) {
            int dx = x - cx;
            if (Math.abs(dx) > HALF + 1) {
                continue;
            }
            for (int z = z0; z < z0 + 16; z++) {
                int dz = z - cz;
                if (Math.abs(dz) > HALF + 1 || !b.isLand(x, z)) {
                    continue;
                }
                for (int y = ground; y <= ground + 2; y++) {
                    b.put(x, y, z, y == ground + 2 ? dark : brick);
                }
            }
        }

        int floorY = ground + 2 + FLOOR;
        int roofY = ground + 2 + ROOF;
        int ax = cx - HALF;
        int bx = cx + HALF;
        int az = cz - HALF;
        int bz = cz + HALF;

        // Perimeter walls with a blood-stained band every third course.
        for (int y = floorY; y <= roofY - 1; y++) {
            BlockState wall = ((y - floorY) % 3 == 2) ? conc : brick;
            for (int x = ax; x <= bx; x++) {
                b.put(x, y, az, wall);
                b.put(x, y, bz, wall);
            }
            for (int z = az; z <= bz; z++) {
                b.put(ax, y, z, wall);
                b.put(bx, y, z, wall);
            }
        }

        // Floor and roof slab.
        b.fill(ax, floorY, az, bx, floorY, bz, tile);
        b.fill(ax, roofY, az, bx, roofY, bz, brick);
        b.pitchedRoof(ax - 1, az - 1, bx + 1, bz + 1, roofY + 1, 4, 0);
    }

    /** The maw and side windows, punched through the shell (all writes idempotent air). */
    private static void openings(StructureBuilder b, int cx, int cz, int ground) {
        BlockState air = Blocks.AIR.defaultBlockState();
        int floorY = ground + 2 + FLOOR;
        int roofY = ground + 2 + ROOF;
        int az = cz - HALF;

        // The vast open maw on the north face: the full interior width, floor to roof.
        for (int x = cx - MAW; x <= cx + MAW; x++) {
            for (int y = floorY; y <= roofY - 1; y++) {
                b.put(x, y, az, air);
                // Make the mouth deep and dark: two courses back into the hall.
                b.put(x, y, az + 1, air);
            }
        }

        // Tall narrow windows on the east and west walls.
        for (int z = cz - 12; z <= cz + 12; z += 6) {
            for (int y = floorY + 3; y <= roofY - 3; y++) {
                b.put(cx - HALF, y, z, air);
                b.put(cx + HALF, y, z, air);
            }
        }
    }

    /** The feasting hall fit-out: runner, long high table, pillars, chancel and braziers. */
    private static void feast(StructureBuilder b, int cx, int cz, int ground) {
        int floorY = ground + 2 + FLOOR;
        BlockState runner = Blocks.CONCRETE.pick(DyeColor.RED).defaultBlockState();
        BlockState black = Blocks.BLACKSTONE.defaultBlockState();
        BlockState crimson = Blocks.CRIMSON_PLANKS.defaultBlockState();
        BlockState stem = Blocks.CRIMSON_STEM.defaultBlockState();
        BlockState tile = Blocks.DEEPSLATE_TILES.defaultBlockState();

        // A red runner up the centre of the hall toward the chancel.
        b.fill(cx - 2, floorY, cz - HALF + 1, cx + 2, floorY, cz + HALF - 1, runner);

        // Flanking pillars: blackstone shafts with crimson caps, one every 5 blocks.
        for (int z = cz - 14; z <= cz + 12; z += 5) {
            for (int sx = -1; sx <= 1; sx += 2) {
                int px = cx + sx * 11;
                for (int y = floorY + 1; y <= floorY + 6; y++) {
                    b.put(px, y, z, y == floorY + 6 ? crimson : black);
                }
                b.put(px, floorY + 7, z, Blocks.RED_NETHER_BRICKS.defaultBlockState());
            }
        }

        // The high table: a long blackstone slab on crimson legs, toward the chancel end.
        b.fill(cx - 2, floorY + 3, cz + 6, cx + 2, floorY + 3, cz + 12, black);
        for (int x = cx - 2; x <= cx + 2; x += 2) {
            for (int z = cz + 6; z <= cz + 12; z += 3) {
                b.put(x, floorY + 1, z, crimson);
                b.put(x, floorY + 2, z, stem);
            }
        }

        // The raised chancel: a stone dais with an obsidian idol.
        b.fill(cx - CHANCEL, floorY, cz + HALF - CHANCEL, cx + CHANCEL, floorY, cz + HALF - 1, tile);
        b.fill(cx - 1, floorY + 1, cz + HALF - CHANCEL + 1, cx + 1, floorY + 3, cz + HALF - CHANCEL + 1,
                Blocks.CRIMSON_PLANKS.defaultBlockState());
        b.put(cx, floorY + 4, cz + HALF - CHANCEL + 1, Blocks.OBSIDIAN.defaultBlockState());
        b.put(cx, floorY + 5, cz + HALF - CHANCEL + 1, Blocks.REDSTONE_BLOCK.defaultBlockState());

        // Two braziers inside the maw.
        for (int sx = -1; sx <= 1; sx += 2) {
            int px = cx + sx * 6;
            b.put(px, floorY, cz - HALF + 3, black);
            b.put(px, floorY + 1, cz - HALF + 3, Blocks.CRIMSON_STEM.defaultBlockState());
            b.put(px, floorY + 2, cz - HALF + 3, Blocks.CONCRETE.pick(DyeColor.RED).defaultBlockState());
        }
    }

    /** Mean surface over a 5x5 sample of the courtyard: one datum for the whole temple. */
    private static int baseLevel(StructureBuilder b, int cx, int cz, int field) {
        long sum = 0;
        int n = 0;
        for (int i = -2; i <= 2; i++) {
            for (int j = -2; j <= 2; j++) {
                int x = cx + i * field / 2;
                int z = cz + j * field / 2;
                if (b.isLand(x, z)) {
                    sum += b.groundY(x, z);
                    n++;
                }
            }
        }
        return n == 0 ? b.groundY(cx, cz) : (int) (sum / n);
    }

    /** Cut hills to the datum, fill hollows, and clear vegetation above. Chunk-local, no rng. */
    private static void level(StructureBuilder b, int cx, int cz, int ground, int field, Palette p) {
        BlockState air = Blocks.AIR.defaultBlockState();
        int x0 = b.chunkMinX();
        int z0 = b.chunkMinZ();
        int f2 = field * field;
        for (int x = x0; x < x0 + 16; x++) {
            int dx = x - cx;
            if (dx * dx > f2) {
                continue;
            }
            for (int z = z0; z < z0 + 16; z++) {
                int dz = z - cz;
                if (dx * dx + dz * dz > f2 || !b.isLand(x, z)) {
                    continue;
                }
                int surface = b.groundY(x, z);
                if (surface > ground) {
                    b.fill(x, ground + 1, z, x, surface, z, air);
                } else if (surface < ground) {
                    b.fill(x, surface + 1, z, x, ground, z, p.foundation());
                }
                b.put(x, ground, z, p.ground());
                for (int y = ground + 1; y <= ground + 20; y++) {
                    b.put(x, y, z, air);
                }
            }
        }
    }
}
