package com.sanchous98.eldritchhorror.world.loc.site;

import com.sanchous98.eldritchhorror.world.loc.SiteLoot;

import com.sanchous98.eldritchhorror.world.loc.Location;
import com.sanchous98.eldritchhorror.world.loc.Palette;
import com.sanchous98.eldritchhorror.world.loc.StructureBuilder;
import com.sanchous98.eldritchhorror.world.loc.StructureBuilder.Doorway;
import com.sanchous98.eldritchhorror.world.loc.StructureBuilder.Side;
import com.sanchous98.eldritchhorror.world.loc.Tier;
import com.sanchous98.eldritchhorror.world.loc.style.StyleKit;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A walled stronghold of a nameless cult: a crenellated curtain wall around a paved courtyard,
 * a gatehouse that only opens inward, squat side chapels and a spire-crowned central keep whose
 * altar is lit by a single guttering flame. The masonry is already failing — the north-east
 * corner has collapsed and cobwebs hang over the pews.
 *
 * <p>The keep now carries a real, small working altar — an {@code altar_core} ringed by eight
 * {@code rune_stone} marks on the chapel floor — so a stronghold population has somewhere to
 * worship. The NPC ritual cell pass ({@code world/cult/CultRituals}) recognises any valid
 * {@link com.sanchous98.eldritchhorror.rite.RitualAltar} ring, and this is the one the stronghold's
 * worshippers, zealots and rite binders gather at when no player has built one nearby.
 */
public final class CultStronghold implements Location {

    /** Built half-extent of the curtain wall (blocks). */
    private static final int WALL = 30;
    /** Height of the curtain wall above the courtyard floor. */
    private static final int WALL_H = 7;

    private final int centerX;
    private final int centerZ;

    /** Constructed at a fixed centre; {@link #build} has no coordinate argument. */
    public CultStronghold(int x, int z) {
        this.centerX = x;
        this.centerZ = z;
    }

    @Override
    public String id() {
        return "eldritch_horror:site/cult_stronghold";
    }

    @Override
    public Tier tier() {
        return Tier.TOWN;
    }

    @Override
    public int radius() {
        return 110; // cull radius — unchanged
    }

    @Override
    public int renderRadius() {
        return WALL + 30; // render the built curtain wall, not the 110 cull radius
    }

    @Override
    public void build(StructureBuilder b) {
        int cx = this.centerX;
        int cz = this.centerZ;
        int ground = b.groundY(cx, cz);
        Palette p = b.palette();
        RandomSource rng = b.rng();
        int y0 = ground + 1;

        // Courtyard apron inside the walls.
        b.ground(cx - WALL, cz - WALL, cx + WALL, cz + WALL, ground, ground, p.ground());

        // Curtain wall with battlements all round: taller and thicker at the base so it reads as a
        // stronghold rather than a cottage fence.
        b.walls(cx - WALL, y0, cz - WALL, cx + WALL, y0 + WALL_H, cz + WALL, p.wall());
        b.walls(cx - WALL, y0, cz - WALL, cx + WALL, y0 + 1, cz + WALL, p.foundation());
        b.crenellations(cx - WALL, cz - WALL, cx + WALL, cz + WALL, y0 + WALL_H + 1);

        // Corner towers break the rectangular silhouette.
        cornerTower(b, p, cx - WALL, cz - WALL, ground);
        cornerTower(b, p, cx + WALL, cz - WALL, ground);
        cornerTower(b, p, cx - WALL, cz + WALL, ground);
        cornerTower(b, p, cx + WALL, cz + WALL, ground);

        // Gatehouse on the south curtain; the gate faces outward.
        gatehouse(b, p, cx, cz + WALL, ground);

        // The keep-chapel at the heart of the courtyard.
        keep(b, p, cx, cz, ground);
        // A real working altar on the chapel floor, so the population can hold a ritual cell here.
        altar(b, p, cx, cz, y0);

        // Two squat side chapels against the north curtain.
        sideChapel(b, p, cx - 20, cz - WALL + 8, ground);
        sideChapel(b, p, cx + 20, cz - WALL + 8, ground);

        // Decay: weathering, rubble, overgrowth, cobwebs and a collapsed corner.
        decay(b, rng, cx, cz, y0, p);

        b.marker("cult_stronghold", cx, y0, cz);
        // Cult offerings in the keep interior.
        SiteLoot.fillRoom(b, cx - 7, cz - 7, cx + 7, cz + 7, y0, SiteLoot.CULT);
    }

    // ------------------------------------------------------------------ pieces

    private static void gatehouse(StructureBuilder b, Palette p, int cx, int z, int ground) {
        int y0 = ground + 1;
        int h = 9;
        int half = 5;
        int z0 = z - 4;
        int z1 = z + 4;
        b.room(cx - half, y0, z0, cx + half, y0 + h, z1,
                new Doorway(Side.S, half), new Doorway(Side.N, half));
        b.fill(cx - half, y0, z0, cx + half, y0, z1, p.foundation());
        b.pitchedRoof(cx - half, z0, cx + half, z1, y0 + h, 5, 1);
        b.crenellations(cx - half, z0, cx + half, z1, y0 + h + 1);
        StyleKit.twoHighDoor(b, p, cx, z1, y0 + 1, Direction.SOUTH);
        StyleKit.twoHighDoor(b, p, cx, z0, y0 + 1, Direction.NORTH);
        b.put(cx, y0 + h - 2, (z0 + z1) / 2, p.light());
    }

    private static void keep(StructureBuilder b, Palette p, int cx, int cz, int ground) {
        int y0 = ground + 1;
        int h = 13;
        int half = 10;
        b.room(cx - half, y0, cz - half, cx + half, y0 + h, cz + half, new Doorway(Side.S, half));

        b.fill(cx - half, y0, cz - half, cx + half, y0, cz + half, p.foundation());
        for (int y = y0; y < y0 + h; y++) {
            b.put(cx - half, y, cz - half, p.accent());
            b.put(cx + half, y, cz - half, p.accent());
            b.put(cx - half, y, cz + half, p.accent());
            b.put(cx + half, y, cz + half, p.accent());
        }

        // Tall narrow lancets, mostly dark.
        for (int wx = cx - half + 3; wx <= cx + half - 3; wx += 4) {
            b.window(wx, y0 + 4, cz - half, 5, 1, true);
            b.window(wx, y0 + 4, cz + half, 5, 1, true);
        }
        for (int wz = cz - half + 3; wz <= cz + half - 3; wz += 4) {
            b.window(cx - half, y0 + 4, wz, 5, 1, true);
            b.window(cx + half, y0 + 4, wz, 5, 1, true);
        }

        for (int z = cz - half + 2; z <= cz + half - 2; z += 5) {
            b.buttress(cx - half, z, y0, 5, Side.W);
            b.buttress(cx + half, z, y0, 5, Side.E);
        }

        // Steep roof pierced by a dominating spire.
        b.pitchedRoof(cx - half - 1, cz - half - 1, cx + half + 1, cz + half + 1, y0 + h, 9, 0);
        b.spire(cx, cz, y0 + h + 9, 18);
        StyleKit.twoHighDoor(b, p, cx, cz + half, y0 + 1, Direction.SOUTH);
        b.put(cx, y0 + h - 3, cz, p.light());
    }

    /**
     * The keep's working altar: an {@code altar_core} on the chapel floor with eight
     * {@code rune_stone} marks on a two-block ring around it. That is exactly the shape the shared
     * {@link com.sanchous98.eldritchhorror.rite.RitualAltar} gate accepts (marks stand off the core
     * and surround it), so a cell can start here.
     */
    private static void altar(StructureBuilder b, Palette p, int cx, int cz, int floorY) {
        BlockState core = block("altar_core", p.frame());
        BlockState rune = block("rune_stone", p.accent());
        for (int i = 0; i < 8; i++) {
            double a = i * Math.PI / 4.0;
            int dx = (int) Math.round(Math.cos(a) * 2.0);
            int dz = (int) Math.round(Math.sin(a) * 2.0);
            b.put(cx + dx, floorY + 1, cz + dz, rune);
        }
        b.put(cx, floorY + 1, cz, core);
    }

    /** Resolves a mod block by id for worldgen, falling back to {@code fallback} if unregistered. */
    private static BlockState block(String id, BlockState fallback) {
        BlockState resolved = net.minecraft.core.registries.BuiltInRegistries.BLOCK
                .getValue(com.sanchous98.eldritchhorror.EldritchHorror.id(id)).defaultBlockState();
        return resolved.isAir() ? fallback : resolved;
    }

    private static void sideChapel(StructureBuilder b, Palette p, int cx, int cz, int ground) {
        int y0 = ground + 1;
        int h = 6;
        int half = 4;
        b.room(cx - half, y0, cz - half, cx + half, y0 + h, cz + half, new Doorway(Side.S, half));
        b.fill(cx - half, y0, cz - half, cx + half, y0, cz + half, p.foundation());
        for (int y = y0; y < y0 + h; y++) {
            b.put(cx - half, y, cz - half, p.accent());
            b.put(cx + half, y, cz + half, p.accent());
        }
        b.window(cx, y0 + 3, cz - half, 3, 1, true);
        b.pitchedRoof(cx - half - 1, cz - half - 1, cx + half + 1, cz + half + 1, y0 + h, 5, 0);
        StyleKit.twoHighDoor(b, p, cx, cz + half, y0 + 1, Direction.SOUTH);
    }

    /** A squat crenellated tower at a wall corner: the stronghold's second silhouette feature. */
    private static void cornerTower(StructureBuilder b, Palette p, int x, int z, int ground) {
        int y0 = ground + 1;
        int half = 4;
        int h = 10;
        b.room(x - half, y0, z - half, x + half, y0 + h, z + half);
        b.fill(x - half, y0, z - half, x + half, y0, z + half, p.foundation());
        for (int y = y0; y < y0 + h; y++) {
            b.put(x - half, y, z - half, p.accent());
            b.put(x + half, y, z - half, p.accent());
            b.put(x - half, y, z + half, p.accent());
            b.put(x + half, y, z + half, p.accent());
        }
        b.window(x, y0 + 5, z - half, 3, 1, true);
        b.crenellations(x - half, z - half, x + half, z + half, y0 + h + 1);
    }

    /** The decay pass: weathering, rubble, overgrowth, cobwebs and a collapsed corner. */
    private static void decay(StructureBuilder b, RandomSource rng, int cx, int cz, int y0, Palette p) {
        // Weather the curtain wall and let the roof-timbers show through.
        b.scatter(cx - WALL, cz - WALL, cx + WALL, cz + WALL, y0 + 1, y0 + 2, p.weathered(), 0.10f);
        b.scatter(cx - WALL, cz - WALL, cx + WALL, cz + WALL, y0, y0, p.rubble(), 0.12f);
        if (p.overgrowth() != null) {
            b.scatter(cx - WALL, cz - WALL, cx + WALL, cz + WALL, y0 + 1, y0 + 3,
                    p.overgrowth(), 0.05f);
        }
        b.scatter(cx - 9, cz - 9, cx + 9, cz + 9, y0 + 1, y0 + 9, Palette.cobweb(), 0.05f);

        // One corner of the wall has long since fallen.
        b.ruins(cx + 12, y0, cz + 12, cx + WALL, y0 + 5, cz + WALL, 0.18f);
    }
}
