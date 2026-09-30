package com.sanchous98.eldritchhorror.world.loc.city;

import com.sanchous98.eldritchhorror.world.city.City;
import com.sanchous98.eldritchhorror.world.loc.Location;
import com.sanchous98.eldritchhorror.world.loc.Palette;
import com.sanchous98.eldritchhorror.world.loc.StructureBuilder;
import com.sanchous98.eldritchhorror.world.loc.StructureBuilder.Side;
import com.sanchous98.eldritchhorror.world.loc.Tier;
import com.sanchous98.eldritchhorror.world.loc.style.CityStyle;
import com.sanchous98.eldritchhorror.world.loc.style.CityStyles;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

/**
 * The reference {@link Tier#METROPOLIS}: an irregular, decaying gothic district for a curated
 * city. A clear silhouette upgrade over a box grid — every building has a steep pitched roof,
 * quoins and tall narrow windows, a few sprout spires or buttresses, and one cathedral-spire
 * dominates the skyline over a plaza with a monument.
 *
 * <p>All deterministic (seeded by {@link #id()}) and chunk-clipped: the layout is generated the
 * same way for every chunk, and {@link StructureBuilder} drops writes outside the generating
 * chunk. Only {@link Palette} blocks are used — no hardcoded block types.
 *
 * <p>Decay (see "Atmosphere rules"): weathered courses low on walls, overgrowth on wet walls,
 * rubble at foundations, cobwebs in the corners, and punched holes in a fraction of buildings.
 */
public final class CityLocation implements Location {

    /** Layout pitch of the jittered building grid (blocks). */
    private static final int CELL = 13;
    /**
     * Hard cap on buildings per city. Raised from 240 so the much larger district still fills:
     * the loop rebuilds the whole (chunk-clipped) layout for every generating chunk, so this
     * bounds the worst-case per-chunk cost. Density is kept comparable to the old city.
     */
    private static final int MAX_BUILDINGS = 1100;
    /** Built district half-extent (blocks); the cull radius ({@link #radius()}) can be larger. */
    private static final int DISTRICT_CAP = 380;
    /** Plaza half-extent: wide enough to seat the enlarged style landmarks on paving. */
    private static final int PLAZA = 30;
    /** Radius kept clear of ordinary buildings so the (now large) landmark has room. */
    private static final int INNER_CLEAR = 46;

    private final City city;

    public CityLocation(City city) {
        this.city = city;
    }

    /** The city this location represents (used to resolve its cultural style). */
    public City city() {
        return this.city;
    }

    /** Half-extent in blocks, scaled from real population: {@code clamp(220 + sqrt(pop)/40, 220, 400)}. */
    @Override
    public int radius() {
        double r = 220.0 + Math.sqrt(Math.max(this.city.population(), 1)) / 40.0;
        int rounded = (int) Math.round(r);
        return Math.max(220, Math.min(400, rounded));
    }

    @Override
    public String id() {
        return "eldritch_horror:city/" + this.city.id();
    }

    @Override
    public Tier tier() {
        return Tier.METROPOLIS;
    }

    @Override
    public void build(StructureBuilder b) {
        int cx = this.city.x();
        int cz = this.city.z();
        int ground = b.groundY(cx, cz);
        Palette p = b.palette();
        RandomSource rng = b.rng();
        CityStyle style = CityStyles.forCity(this.city.name());

        int district = Math.min(radius(), DISTRICT_CAP);
        int plaza = PLAZA;
        int inner = INNER_CLEAR;

        // 1. Plaza: a paved apron at the heart of the city.
        b.ground(cx - plaza, cz - plaza, cx + plaza, cz + plaza, ground - 2, ground, p.ground());
        b.ground(cx - plaza, cz - plaza, cx + plaza, cz + plaza, ground + 1, ground + 1, p.ground());

        // 1b. Pave the whole district on land, so the city reads as urban fabric rather than
        // scattered buildings on wild terrain. Chunk-clipped: only this chunk's columns write.
        paveDistrict(b, rng, cx, cz, district, p);

        // 2. Landmark: the cultural skyline piece (cathedral / temple / mosque / pagoda …).
        style.landmark(b, rng, cx, cz, ground, p);

        // 3. Plaza monument, off the landmark axis.
        b.monument(cx + plaza - 5, cz + plaza - 5, ground + 1);

        // 4. Buildings on a jittered grid — irregular blocks and 2–3 wide alleys, never a grid.
        int built = 0;
        for (int gx = -district; gx <= district && built < MAX_BUILDINGS; gx += CELL) {
            for (int gz = -district; gz <= district && built < MAX_BUILDINGS; gz += CELL) {
                int jx = gx + rng.nextInt(5) - 2;
                int jz = gz + rng.nextInt(5) - 2;
                int x = cx + jx;
                int z = cz + jz;
                int dx = x - cx;
                int dz = z - cz;
                if (dx * dx + dz * dz > district * district) {
                    continue;
                }
                if (Math.abs(dx) < inner && Math.abs(dz) < inner) {
                    continue; // keep the landmark and plaza clear
                }
                if (rng.nextFloat() > 0.82f) {
                    continue; // a few vacant lots
                }
                building(b, rng, x, z, p, style);
                built++;
            }
        }

        // 5. Street furniture for the culture (lanterns, torii, neon, statues…).
        style.streetProps(b, rng, cx, cz, district, ground, p);

        b.marker("city_center", cx, ground + 1, cz);
    }

    // ------------------------------------------------------------------ pieces

    /**
     * Paves the district on land only: a ground course on every land column within the district
     * radius. This gives the city a continuous urban surface (streets and courts) instead of
     * buildings floating on untouched terrain. Deterministic and chunk-clipped.
     */
    private static void paveDistrict(StructureBuilder b, RandomSource rng, int cx, int cz,
                                     int district, Palette p) {
        // Only the columns of the chunk currently generating are visited (O(256) per chunk),
        // so paving costs the same regardless of district size.
        int x0 = b.chunkMinX();
        int z0 = b.chunkMinZ();
        for (int x = x0; x < x0 + 16; x++) {
            int dx = x - cx;
            if (Math.abs(dx) > district) {
                continue;
            }
            for (int z = z0; z < z0 + 16; z++) {
                int dz = z - cz;
                if (dx * dx + dz * dz > district * district) {
                    continue;
                }
                if (!b.isLand(x, z)) {
                    continue;
                }
                int gy = b.groundY(x, z);
                b.put(x, gy, z, p.ground());
            }
        }
    }

    /** One building: quoined shell, tall windows, a steep ridge, optional spire/buttress, decay. */
    private static void building(StructureBuilder b, RandomSource rng, int x, int z, Palette p,
                                 CityStyle style) {
        if (!b.isLand(x, z)) {
            return; // never build on water
        }
        int w = 5 + rng.nextInt(4);   // 5..8 across X
        int d = 5 + rng.nextInt(4);   // 5..8 across Z
        int h = 4 + rng.nextInt(6);   // wall height 4..9
        int x1 = x + w - 1;
        int z1 = z + d - 1;

        int gy = b.groundY(x, z);
        int y0 = gy + 1;
        int y1 = y0 + h;

        // Paved margin acts as the alley surface.
        b.ground(x - 1, z - 1, x1 + 1, z1 + 1, gy - 1, gy, p.ground());

        // Shell + hollow interior + floor.
        Side doorSide = Side.values()[rng.nextInt(4)];
        int doorOffset = 1 + rng.nextInt(Math.max(1, (doorSide == Side.N || doorSide == Side.S ? w : d) - 2));
        b.room(x, y0, z, x1, y1, z1, new StructureBuilder.Doorway(doorSide, doorOffset));

        // Foundation course and corner quoins.
        b.fill(x, y0, z, x1, y0, z1, p.foundation());
        for (int y = y0; y < y1; y++) {
            b.put(x, y, z, p.accent());
            b.put(x1, y, z, p.accent());
            b.put(x, y, z1, p.accent());
            b.put(x1, y, z1, p.accent());
        }

        // Real door at the doorway (the room carving leaves air).
        placeDoor(b, p, x, z, x1, z1, y0 + 1, doorSide, doorOffset);

        // Steep pitched roof; ridge follows the longer side.
        int roofH = 3 + rng.nextInt(3);
        int axis = w >= d ? 0 : 1;
        b.pitchedRoof(x - 1, z - 1, x1 + 1, z1 + 1, y1, roofH, axis);

        // Tall narrow windows, a row on each wall.
        int wy = y0 + 2;
        for (int wx = x + 2; wx <= x1 - 2; wx += 3) {
            b.window(wx, wy, z, 3, 2, true);
            b.window(wx, wy, z1, 3, 2, true);
        }
        for (int wz = z + 2; wz <= z1 - 2; wz += 3) {
            b.window(x, wy, wz, 3, 1, true);
            b.window(x1, wy, wz, 3, 1, true);
        }

        // Some buildings sprout a small spire or lean on a buttress.
        float roll = rng.nextFloat();
        if (roll < 0.18f) {
            b.spire((x + x1) / 2, (z + z1) / 2, y1 + roofH - 1, 4 + rng.nextInt(4));
        } else if (roll < 0.5f) {
            Side out = Side.values()[rng.nextInt(4)];
            switch (out) {
                case N -> b.buttress(x, z, y0, Math.max(2, h / 2), Side.N);
                case S -> b.buttress(x1, z1, y0, Math.max(2, h / 2), Side.S);
                case W -> b.buttress(x, z1, y0, Math.max(2, h / 2), Side.W);
                case E -> b.buttress(x1, z, y0, Math.max(2, h / 2), Side.E);
            }
        }

        decay(b, rng, x, y0, z, x1, y1, z1, p);

        // Cultural flourish (signs, balconies, verandas…) — default does nothing.
        style.flourish(b, rng, x, y0, z, x1, y1, z1, p);

        // A single guttering light in some buildings only.
        if (rng.nextFloat() < 0.3f) {
            b.put((x + x1) / 2, y1 - 2, (z + z1) / 2, p.light());
        }
    }

    /** The decay pass: weathering, overgrowth, rubble, cobwebs and punched holes. */
    private static void decay(StructureBuilder b, RandomSource rng, int x, int y0, int z,
                              int x1, int y1, int z1, Palette p) {
        // Weathered courses low on the walls.
        for (int y = y0 + 1; y <= y0 + 2 && y < y1; y++) {
            for (int wx = x; wx <= x1; wx++) {
                if (rng.nextFloat() < 0.3f) {
                    b.put(wx, y, z, p.weathered());
                }
                if (rng.nextFloat() < 0.3f) {
                    b.put(wx, y, z1, p.weathered());
                }
            }
            for (int wz = z; wz <= z1; wz++) {
                if (rng.nextFloat() < 0.3f) {
                    b.put(x, y, wz, p.weathered());
                }
                if (rng.nextFloat() < 0.3f) {
                    b.put(x1, y, wz, p.weathered());
                }
            }
        }

        // Rubble piled at the foundations (only lands on the air of the outer margin).
        b.scatter(x - 1, z - 1, x1 + 1, z1 + 1, y0, y0, p.rubble(), 0.12f);

        // Overgrowth crawling up the wet walls.
        if (p.overgrowth() != null) {
            b.scatter(x - 1, z - 1, x1 + 1, z1 + 1, y0 + 1, y0 + 3, p.overgrowth(), 0.08f);
        }

        // Cobwebs in the upper interior corners.
        b.scatter(x + 1, z + 1, x1 - 1, z1 - 1, y1 - 3, y1 - 1, Palette.cobweb(), 0.06f);

        // A fraction of buildings have collapsed: punch holes in walls and the roof.
        if (rng.nextFloat() < 0.22f) {
            b.ruins(x, y0 + 1, z, x1, y1, z1, 0.10f);
            b.ruins(x - 1, y1 + 1, z - 1, x1 + 1, y1 + 2, z1 + 1, 0.12f);
        }
    }

    // ------------------------------------------------------------------ helpers

    private static void placeDoor(StructureBuilder b, Palette p, int x0, int z0, int x1, int z1,
                                  int y, Side side, int offset) {
        int dx;
        int dz;
        Direction facing;
        switch (side) {
            case N -> { dx = x0 + offset; dz = z0; facing = Direction.NORTH; }
            case S -> { dx = x0 + offset; dz = z1; facing = Direction.SOUTH; }
            case W -> { dx = x0; dz = z0 + offset; facing = Direction.WEST; }
            default -> { dx = x1; dz = z0 + offset; facing = Direction.EAST; }
        }
        BlockState lower = p.door()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, facing)
                .setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.LOWER);
        b.put(dx, y, dz, lower);
        b.put(dx, y + 1, dz, lower.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER));
    }
}
