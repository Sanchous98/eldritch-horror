package com.sanchous98.eldritchhorror.world.loc.site;

import com.sanchous98.eldritchhorror.world.loc.Location;
import com.sanchous98.eldritchhorror.world.loc.Palette;
import com.sanchous98.eldritchhorror.world.loc.StructureBuilder;
import com.sanchous98.eldritchhorror.world.loc.StructureBuilder.Doorway;
import com.sanchous98.eldritchhorror.world.loc.StructureBuilder.Side;
import com.sanchous98.eldritchhorror.world.loc.Tier;
import com.sanchous98.eldritchhorror.world.loc.style.StyleKit;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;

/**
 * The vault of a militant order that keeps what must not be seen. A tall crenellated enclosure
 * with a fortified gatehouse holds a central strongroom ringed by deep archive cells; a ring of
 * obelisks marks the seal at the centre. The roof of the strongroom is holed, the archive doors
 * hang open, and the order itself is long dead.
 */
public final class OrderVault implements Location {

    /** Half-extent of the enclosure walls (blocks). */
    private static final int WALL = 29;

    private final int centerX;
    private final int centerZ;

    /** Constructed at a fixed centre; {@link #build} has no coordinate argument. */
    public OrderVault(int x, int z) {
        this.centerX = x;
        this.centerZ = z;
    }

    @Override
    public String id() {
        return "eldritch_horror:site/order_vault";
    }

    @Override
    public Tier tier() {
        return Tier.TOWN;
    }

    @Override
    public int radius() {
        return 110;
    }

    @Override
    public void build(StructureBuilder b) {
        int cx = this.centerX;
        int cz = this.centerZ;
        int ground = b.groundY(cx, cz);
        Palette p = b.palette();
        RandomSource rng = b.rng();
        int y0 = ground + 1;

        // Paved precinct inside the enclosure.
        b.ground(cx - WALL, cz - WALL, cx + WALL, cz + WALL, ground, ground, p.ground());

        // Tall enclosure wall, crenellated.
        b.walls(cx - WALL, y0, cz - WALL, cx + WALL, y0 + 6, cz + WALL, p.wall());
        b.crenellations(cx - WALL, cz - WALL, cx + WALL, cz + WALL, y0 + 7);

        // Gatehouse breaking the south wall.
        gatehouse(b, p, cx, cz + WALL, ground);

        // The central strongroom.
        strongroom(b, p, cx, cz, ground);

        // Archive cells set into the precinct below the inner wall.
        archive(b, p, cx - WALL + 6, cz - WALL + 6, ground);
        archive(b, p, cx + WALL - 6, cz - WALL + 6, ground);
        archive(b, p, cx - WALL + 6, cz + WALL - 6, ground);
        archive(b, p, cx + WALL - 6, cz + WALL - 6, ground);

        // The ring of seals.
        b.monument(cx - 14, cz, y0);
        b.monument(cx + 14, cz, y0);
        b.monument(cx, cz - 14, y0);
        b.monument(cx, cz + 14, y0);

        decay(b, rng, cx, cz, y0, p);

        b.marker("order_vault", cx, y0, cz);
    }

    // ------------------------------------------------------------------ pieces

    private static void gatehouse(StructureBuilder b, Palette p, int cx, int z, int ground) {
        int y0 = ground + 1;
        int h = 10;
        int half = 5;
        int z0 = z - 4;
        int z1 = z + 4;
        b.room(cx - half, y0, z0, cx + half, y0 + h, z1,
                new Doorway(Side.S, half), new Doorway(Side.N, half));
        b.fill(cx - half, y0, z0, cx + half, y0, z1, p.foundation());
        b.pitchedRoof(cx - half, z0, cx + half, z1, y0 + h, 6, 1);
        b.crenellations(cx - half, z0, cx + half, z1, y0 + h + 1);
        StyleKit.twoHighDoor(b, p, cx, z1, y0 + 1, Direction.SOUTH);
        StyleKit.twoHighDoor(b, p, cx, z0, y0 + 1, Direction.NORTH);
        b.put(cx, y0 + h - 2, (z0 + z1) / 2, p.light());
    }

    private static void strongroom(StructureBuilder b, Palette p, int cx, int cz, int ground) {
        int y0 = ground + 1;
        int h = 14;
        int half = 11;
        b.room(cx - half, y0, cz - half, cx + half, y0 + h, cz + half, new Doorway(Side.S, half));
        b.fill(cx - half, y0, cz - half, cx + half, y0, cz + half, p.foundation());
        for (int y = y0; y < y0 + h; y++) {
            b.put(cx - half, y, cz - half, p.accent());
            b.put(cx + half, y, cz - half, p.accent());
            b.put(cx - half, y, cz + half, p.accent());
            b.put(cx + half, y, cz + half, p.accent());
        }
        for (int wx = cx - half + 3; wx <= cx + half - 3; wx += 4) {
            b.window(wx, y0 + 5, cz - half, 5, 1, true);
            b.window(wx, y0 + 5, cz + half, 5, 1, true);
        }
        for (int wz = cz - half + 3; wz <= cz + half - 3; wz += 4) {
            b.window(cx - half, y0 + 5, wz, 5, 1, true);
            b.window(cx + half, y0 + 5, wz, 5, 1, true);
        }
        for (int z = cz - half + 2; z <= cz + half - 2; z += 5) {
            b.buttress(cx - half, z, y0, 6, Side.W);
            b.buttress(cx + half, z, y0, 6, Side.E);
        }
        b.pitchedRoof(cx - half - 1, cz - half - 1, cx + half + 1, cz + half + 1, y0 + h, 10, 0);
        b.spire(cx, cz, y0 + h + 10, 16);
        StyleKit.twoHighDoor(b, p, cx, cz + half, y0 + 1, Direction.SOUTH);
        b.put(cx, y0 + h - 3, cz, p.light());
    }

    private static void archive(StructureBuilder b, Palette p, int cx, int cz, int ground) {
        int y0 = ground + 1;
        int h = 5;
        int half = 4;
        b.room(cx - half, y0, cz - half, cx + half, y0 + h, cz + half, new Doorway(Side.E, half));
        b.fill(cx - half, y0, cz - half, cx + half, y0, cz + half, p.foundation());
        b.window(cx, y0 + 2, cz - half, 2, 1, true);
        b.pitchedRoof(cx - half - 1, cz - half - 1, cx + half + 1, cz + half + 1, y0 + h, 4, 1);
    }

    private static void decay(StructureBuilder b, RandomSource rng, int cx, int cz, int y0, Palette p) {
        b.scatter(cx - WALL, cz - WALL, cx + WALL, cz + WALL, y0 + 1, y0 + 2, p.weathered(), 0.10f);
        b.scatter(cx - WALL, cz - WALL, cx + WALL, cz + WALL, y0, y0, p.rubble(), 0.13f);
        if (p.overgrowth() != null) {
            b.scatter(cx - WALL, cz - WALL, cx + WALL, cz + WALL, y0 + 1, y0 + 4,
                    p.overgrowth(), 0.06f);
        }
        b.scatter(cx - 10, cz - 10, cx + 10, cz + 10, y0 + 1, y0 + 10, Palette.cobweb(), 0.06f);
        // The strongroom roof has been forced open from within.
        b.ruins(cx - 11, y0 + 14, cz - 11, cx + 11, y0 + 24, cz + 11, 0.12f);
        // The east enclosure wall is breached.
        b.ruins(cx + WALL - 6, y0, cz - WALL, cx + WALL, y0 + 7, cz + WALL, 0.14f);
    }
}
