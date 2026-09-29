package com.sanchous98.eldritchhorror.world.loc.city;

import com.sanchous98.eldritchhorror.world.city.City;
import com.sanchous98.eldritchhorror.world.loc.Location;
import com.sanchous98.eldritchhorror.world.loc.Palette;
import com.sanchous98.eldritchhorror.world.loc.StructureBuilder;
import com.sanchous98.eldritchhorror.world.loc.Tier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;

/**
 * The reference {@link Tier#METROPOLIS}: a modest, deterministic district layout for a curated
 * city. This is the pattern other location authors copy — clarity over beauty.
 *
 * <p>Layout, all through {@link StructureBuilder} + {@link Palette}:
 * <ul>
 *   <li>a central plaza with a taller accent landmark tower,</li>
 *   <li>a square ring road plus a block grid of avenues,</li>
 *   <li>buildings (box rooms with a door, windows, roof and interior light) in the grid cells.</li>
 * </ul>
 *
 * <p>Called once per generating chunk; {@link StructureBuilder} drops everything outside the
 * chunk, so only this chunk's slice is written. Seeded by {@link #id()} so it never varies.
 */
public final class CityLocation implements Location {

    /** Buildings are laid out on a square grid of this pitch. */
    private static final int CELL = 12;
    /** Upper bound on buildings per location, to keep generation cheap. */
    private static final int MAX_BUILDINGS = 300;
    /** Plaza half-extent. */
    private static final int PLAZA = 14;

    private final City city;

    public CityLocation(City city) {
        this.city = city;
    }

    @Override
    public String id() {
        return "eldritch_horror:city/" + this.city.id();
    }

    @Override
    public Tier tier() {
        return Tier.METROPOLIS;
    }

    /**
     * Half-extent in blocks, scaled from real population so big cities sprawl further:
     * {@code clamp(220 + sqrt(pop)/40, 220, 400)}.
     */
    @Override
    public int radius() {
        double r = 220.0 + Math.sqrt(Math.max(this.city.population(), 1)) / 40.0;
        int rounded = (int) Math.round(r);
        return Math.max(220, Math.min(400, rounded));
    }

    @Override
    public void build(StructureBuilder b) {
        int cx = this.city.x();
        int cz = this.city.z();
        int ground = b.groundY(cx, cz);
        Palette p = b.palette();
        RandomSource rng = b.rng();

        // The built district is denser than the full cull radius, so a huge city still costs
        // roughly the same per chunk.
        int district = Math.min(radius(), 160);

        // 1. Central plaza (flattened to the centre height).
        b.ground(cx - PLAZA, cz - PLAZA, cx + PLAZA, cz + PLAZA, ground - 2, ground, p.surface());

        // 2. Roads: a square ring plus avenues on the grid.
        ringRoad(b, cx, cz, district, ground, p);
        for (int k = -district; k <= district; k += CELL) {
            roadLine(b, cx + k, cz - district, cx + k, cz + district, ground, p);
            roadLine(b, cx - district, cz + k, cx + district, cz + k, ground, p);
        }

        // 3. Buildings in the grid cells (kept clear of the plaza and roads).
        int built = 0;
        for (int bx = cx - district + 2; bx + 5 < cx + district && built < MAX_BUILDINGS; bx += CELL) {
            for (int bz = cz - district + 2; bz + 5 < cz + district && built < MAX_BUILDINGS; bz += CELL) {
                int dx = bx + 2 - cx;
                int dz = bz + 2 - cz;
                if (dx * dx + dz * dz > district * district) {
                    continue;
                }
                if (Math.abs(dx) < PLAZA + 3 && Math.abs(dz) < PLAZA + 3) {
                    continue;
                }
                if (rng.nextFloat() > 0.85f) {
                    continue;
                }
                building(b, rng, bx, bz, ground, p);
                built++;
            }
        }

        // 4. Landmark: a taller accent tower on the plaza.
        landmark(b, cx, cz, ground, p);
    }

    // ------------------------------------------------------------------ pieces
    private static void ringRoad(StructureBuilder b, int cx, int cz, int r, int ground, Palette p) {
        roadLine(b, cx - r, cz - r, cx + r, cz - r, ground, p);
        roadLine(b, cx - r, cz + r, cx + r, cz + r, ground, p);
        roadLine(b, cx - r, cz - r, cx - r, cz + r, ground, p);
        roadLine(b, cx + r, cz - r, cx + r, cz + r, ground, p);
    }

    private static void roadLine(StructureBuilder b, int x0, int z0, int x1, int z1, int ground, Palette p) {
        int n = Math.max(Math.abs(x1 - x0), Math.abs(z1 - z0));
        int sx = Integer.signum(x1 - x0);
        int sz = Integer.signum(z1 - z0);
        for (int i = 0; i <= n; i++) {
            int x = x0 + sx * i;
            int z = z0 + sz * i;
            b.put(x, ground, z, p.surface());
            b.put(x + 1, ground, z, p.surface());
            b.put(x, ground, z + 1, p.surface());
            b.put(x + 1, ground, z + 1, p.surface());
        }
    }

    /** A single building: a hollow room with a door, windows, roof and an interior light. */
    private static void building(StructureBuilder b, RandomSource rng, int x, int z, int ground, Palette p) {
        int w = 4 + rng.nextInt(3);      // 4..6
        int d = 4 + rng.nextInt(3);
        int h = 3 + rng.nextInt(4);      // wall height 3..6
        int x1 = x + w - 1;
        int z1 = z + d - 1;
        int gy = b.groundY(x, z);        // sit on the local terrain
        int y0 = gy + 1;
        int y1 = y0 + h;

        StructureBuilder.Side side = StructureBuilder.Side.values()[rng.nextInt(4)];
        int offset = 1 + rng.nextInt(2);
        b.room(x, y0, z, x1, y1, z1, new StructureBuilder.Doorway(side, offset));

        // Roof overhang + a ridge.
        b.fill(x - 1, y1 + 1, z - 1, x1 + 1, y1 + 1, z1 + 1, p.roof());
        b.fill(x, y1 + 2, z, x1, y1 + 2, z1, p.accent());

        // Windows, one row on each wall.
        int wy = y0 + 1;
        for (int wx = x + 1; wx <= x1 - 1; wx += 2) {
            b.put(wx, wy, z, p.window());
            b.put(wx, wy, z1, p.window());
        }
        for (int wz = z + 1; wz <= z1 - 1; wz += 2) {
            b.put(x, wy, wz, p.window());
            b.put(x1, wy, wz, p.window());
        }

        // Interior light.
        b.put((x + x1) / 2, y1 - 1, (z + z1) / 2, p.light());
    }

    /** A taller accent tower on the plaza. */
    private static void landmark(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int r = 3;
        int top = 24;
        b.room(cx - r, ground + 1, cz - r, cx + r, ground + top, cz + r,
                new StructureBuilder.Doorway(StructureBuilder.Side.W, r));
        // Pyramid cap.
        for (int i = 0; i <= r; i++) {
            b.fill(cx - r + i, ground + top + 1 + i, cz - r + i,
                    cx + r - i, ground + top + 1 + i, cz + r - i, p.accent());
        }
        b.put(cx, ground + top + r + 2, cz, p.light());
        // Windows up the tower.
        for (int y = ground + 4; y < ground + top - 1; y += 4) {
            b.put(cx - r, y, cz, p.window());
            b.put(cx + r, y, cz, p.window());
            b.put(cx, y, cz - r, p.window());
            b.put(cx, y, cz + r, p.window());
        }
    }
}
