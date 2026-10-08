package com.sanchous98.eldritchhorror.world.loc.site;

import com.sanchous98.eldritchhorror.world.loc.SiteLoot;

import com.sanchous98.eldritchhorror.world.ElevationCurve;
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
 * A half-sunk temple to something that lives under the water. A low crenellated platform rings a
 * central shrine; two drowned side sanctuaries list at its edge and the middle hall is a rank
 * floor of silt where the water has stood. Kelp and moss climb the walls, the nave swallows the
 * altar, and only one lantern still burns over the sunken steps.
 */
public final class DrownedTemple implements Location {

    /** Half-extent of the temple platform (blocks). */
    private static final int PLATFORM = 27;

    private final int centerX;
    private final int centerZ;

    /** Constructed at a fixed centre; {@link #build} has no coordinate argument. */
    public DrownedTemple(int x, int z) {
        this.centerX = x;
        this.centerZ = z;
    }

    @Override
    public String id() {
        return "eldritch_horror:site/drowned_temple";
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
    public int renderRadius() {
        return PLATFORM + 30; // render the raised temple, not the 110 cull radius
    }

    @Override
    public void build(StructureBuilder b) {
        int cx = this.centerX;
        int cz = this.centerZ;
        int ground = b.groundY(cx, cz);
        Palette p = b.palette();
        RandomSource rng = b.rng();
        // The temple rises OUT of the water. On the deep seabed `groundY` sits far below the
        // waterline, so a platform laid at it would be entirely drowned and invisible. Raise the
        // deck just above sea level on a solid pedestal reaching down to the seabed, so the
        // shrine reads as half-sunk rather than lost.
        int surface = Math.max(ground, ElevationCurve.SEA_LEVEL + 2);
        int y0 = surface + 1;
        int pedestalBottom = Math.min(ground, ElevationCurve.SEA_LEVEL) - 2;

        // A low stone platform standing out of the water on a foundation to the seabed.
        b.fill(cx - PLATFORM, pedestalBottom, cz - PLATFORM, cx + PLATFORM, surface, cz + PLATFORM,
                p.foundation());
        b.ground(cx - PLATFORM, cz - PLATFORM, cx + PLATFORM, cz + PLATFORM, surface, surface,
                p.ground());
        b.crenellations(cx - PLATFORM, cz - PLATFORM, cx + PLATFORM, cz + PLATFORM, y0);

        // The sunken sanctuary in the middle.
        shrine(b, p, cx, cz, surface);

        // Two subsidiary sanctuaries.
        sanctuary(b, p, cx - 18, cz - 14, surface);
        sanctuary(b, p, cx + 18, cz - 14, surface);

        // Drowned decay.
        decay(b, rng, cx, cz, y0, p);

        b.marker("drowned_temple", cx, y0, cz);
        // Relics lost in the two subsidiary sanctuaries.
        SiteLoot.fillRoom(b, cx - 18 - 4, cz - 14 - 4, cx - 18 + 4, cz - 14 + 4, y0, SiteLoot.RELIC);
        SiteLoot.fillRoom(b, cx + 18 - 4, cz - 14 - 4, cx + 18 + 4, cz - 14 + 4, y0, SiteLoot.RELIC);
    }

    // ------------------------------------------------------------------ pieces

    private static void shrine(StructureBuilder b, Palette p, int cx, int cz, int ground) {
        int y0 = ground + 1;
        int h = 12;
        int half = 9;
        // Wall ring, open to the sky at the rim: the roof itself is broken away.
        b.walls(cx - half, y0, cz - half, cx + half, y0 + h, cz + half, p.wall());
        b.fill(cx - half, y0, cz - half, cx + half, y0, cz + half, p.foundation());
        // Flooded nave: a rank floor of silt and debris where the water has stood.
        b.fill(cx - half + 1, y0 + 1, cz - half + 1, cx + half - 1, y0 + 1, cz + half - 1,
                p.ground());
        b.scatter(cx - half + 1, cz - half + 1, cx + half - 1, cz + half - 1,
                y0 + 2, y0 + 2, p.rubble(), 0.35f);

        for (int y = y0; y < y0 + h; y++) {
            b.put(cx - half, y, cz - half, p.accent());
            b.put(cx + half, y, cz - half, p.accent());
            b.put(cx - half, y, cz + half, p.accent());
            b.put(cx + half, y, cz + half, p.accent());
        }
        for (int wx = cx - half + 3; wx <= cx + half - 3; wx += 4) {
            b.window(wx, y0 + 4, cz - half, 5, 1, true);
            b.window(wx, y0 + 4, cz + half, 5, 1, true);
        }
        for (int wz = cz - half + 3; wz <= cz + half - 3; wz += 4) {
            b.window(cx - half, y0 + 4, wz, 5, 1, true);
            b.window(cx + half, y0 + 4, wz, 5, 1, true);
        }
        b.spire(cx, cz, y0 + h, 14);
        StyleKit.twoHighDoor(b, p, cx, cz + half, y0 + 1, Direction.SOUTH);
        b.put(cx, y0 + h - 2, cz, p.light());
    }

    private static void sanctuary(StructureBuilder b, Palette p, int cx, int cz, int ground) {
        int y0 = ground + 1;
        int h = 7;
        int half = 5;
        b.room(cx - half, y0, cz - half, cx + half, y0 + h, cz + half, new Doorway(Side.S, half));
        b.fill(cx - half, y0, cz - half, cx + half, y0, cz + half, p.foundation());
        for (int y = y0; y < y0 + h; y++) {
            b.put(cx - half, y, cz - half, p.accent());
            b.put(cx + half, y, cz + half, p.accent());
        }
        b.window(cx, y0 + 3, cz - half, 3, 1, true);
        // A collapsed, one-sided roof.
        b.pitchedRoof(cx - half - 1, cz - half - 1, cx + half + 1, cz + half + 1, y0 + h, 6, 1);
        StyleKit.twoHighDoor(b, p, cx, cz + half, y0 + 1, Direction.SOUTH);
    }

    private static void decay(StructureBuilder b, RandomSource rng, int cx, int cz, int y0, Palette p) {
        b.scatter(cx - PLATFORM, cz - PLATFORM, cx + PLATFORM, cz + PLATFORM,
                y0 + 1, y0 + 3, p.weathered(), 0.12f);
        b.scatter(cx - PLATFORM, cz - PLATFORM, cx + PLATFORM, cz + PLATFORM,
                y0, y0, p.rubble(), 0.15f);
        if (p.overgrowth() != null) {
            b.scatter(cx - PLATFORM, cz - PLATFORM, cx + PLATFORM, cz + PLATFORM,
                    y0 + 1, y0 + 4, p.overgrowth(), 0.08f);
        }
        b.scatter(cx - 8, cz - 8, cx + 8, cz + 8, y0 + 1, y0 + 8, Palette.cobweb(), 0.05f);
        // The north-west quarter of the wall is gone.
        b.ruins(cx - PLATFORM, y0, cz - PLATFORM, cx - 10, y0 + 8, cz - 10, 0.16f);
    }
}
