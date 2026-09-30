package com.sanchous98.eldritchhorror.world.loc.style;

import com.sanchous98.eldritchhorror.world.loc.Palette;
import com.sanchous98.eldritchhorror.world.loc.StructureBuilder;
import com.sanchous98.eldritchhorror.world.loc.StructureBuilder.Doorway;
import com.sanchous98.eldritchhorror.world.loc.StructureBuilder.Side;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Blocks;

/**
 * CapeTown — Cape Dutch local colour: whitewashed walls under ornate curved/stepped gables,
 * green shutters and trim, golden thatch over dark timber, cobbled streets, and a working
 * harbour of cranes. Culture leads; Köppen only nudges the overgrowth (coast → kelp).
 *
 * <p>Landmark: a Cape Dutch manor with a stepped ornamental gable and a finial spire, opening
 * onto a stoep and paved werf. Street props: lamp posts, a statue and plinth monument, and
 * harbour cranes (mast + jib) along the seaward edge. See docs/STRUCTURES-CONTRACT.md.
 */
public final class CapeTownStyle implements CityStyle {

    /** Cape Dutch gable half-widths, base to apex — the stepped/curved silhouette. */
    private static final int[] GABLE_HALF = {4, 4, 3, 2, 1, 0};

    @Override
    public String id() {
        return "capetown";
    }

    @Override
    public Palette palette(int koppenClass, boolean coastal) {
        Palette bio = Palette.fromBiome(koppenClass, coastal);
        return new Palette(
                Blocks.COBBLESTONE.defaultBlockState(),             // ground: cobbled street
                Blocks.STONE_BRICKS.defaultBlockState(),            // foundation: plastered base
                Materials.whiteConcrete(),                          // wall: whitewashed plaster
                Materials.lightGrayConcrete(),                      // weathered: aged whitewash
                Materials.concrete(DyeColor.GREEN),                 // accent: green shutters / trim
                Blocks.HAY_BLOCK.defaultBlockState(),               // roof: thatch body
                Blocks.OAK_STAIRS.defaultBlockState(),              // roof stairs: golden thatch edge
                Blocks.OAK_SLAB.defaultBlockState(),                // roof slabs / thatch eaves
                Blocks.GLASS_PANE.defaultBlockState(),              // window
                Blocks.DARK_OAK_PLANKS.defaultBlockState(),         // frame: dark timber mullions
                Blocks.DARK_OAK_DOOR.defaultBlockState(),           // door
                Blocks.DARK_OAK_FENCE.defaultBlockState(),          // rail
                Blocks.LANTERN.defaultBlockState(),                 // light
                coastal ? Blocks.KELP.defaultBlockState() : bio.overgrowth(),
                Blocks.GRAVEL.defaultBlockState());                 // rubble: harbour gravel
    }

    @Override
    public void landmark(StructureBuilder b, RandomSource rng, int cx, int cz, int ground, Palette p) {
        // The Castle of Good Hope, doubled: a broad stone star-fort with a projecting bastion at
        // each of its five points and a gate in the south curtain, but now dominated by a tall
        // whitewashed keep/watchtower rising ~78 blocks from the courtyard within the rampart.
        final int n = 10;
        double[] ox = new double[n];
        double[] oz = new double[n];
        double[] ix = new double[n];
        double[] iz = new double[n];
        double[] ex = new double[n];
        double[] ez = new double[n];
        double rTip = 34.0;   // bastion points
        double rCurt = 25.0;  // curtain midpoints between bastions
        for (int i = 0; i < n; i++) {
            double a = -Math.PI / 2.0 + i * Math.PI / 5.0; // 36° steps; first bastion due north
            double ro = (i % 2 == 0) ? rTip : rCurt;
            ox[i] = cx + ro * Math.cos(a);
            oz[i] = cz + ro * Math.sin(a);
            ix[i] = cx + (ro - 4.0) * Math.cos(a); // inner face of the rampart
            iz[i] = cz + (ro - 4.0) * Math.sin(a);
            ex[i] = cx + (ro - 1.5) * Math.cos(a); // outer lip, for merlons
            ez[i] = cz + (ro - 1.5) * Math.sin(a);
        }

        int yBottom = ground + 1;
        int yTop = ground + 9;
        for (int x = cx - 36; x <= cx + 36; x++) {
            for (int z = cz - 36; z <= cz + 36; z++) {
                boolean inOuter = false;
                boolean inInner = false;
                boolean inEdge = false;
                for (int i = 0, j = n - 1; i < n; j = i++) {
                    if ((oz[i] > z) != (oz[j] > z)
                            && x < (ox[j] - ox[i]) * (z - oz[i]) / (oz[j] - oz[i]) + ox[i]) {
                        inOuter = !inOuter;
                    }
                    if ((iz[i] > z) != (iz[j] > z)
                            && x < (ix[j] - ix[i]) * (z - iz[i]) / (iz[j] - iz[i]) + ix[i]) {
                        inInner = !inInner;
                    }
                    if ((ez[i] > z) != (ez[j] > z)
                            && x < (ex[j] - ex[i]) * (z - ez[i]) / (ez[j] - ez[i]) + ex[i]) {
                        inEdge = !inEdge;
                    }
                }
                if (inOuter && !inInner) {
                    b.fill(x, yBottom, z, x, yTop, z, p.foundation());
                    if (!inEdge && ((x + z) & 1) == 0) {
                        b.put(x, yTop + 1, z, p.foundation()); // alternating merlon
                    }
                } else if (inInner) {
                    b.put(x, ground, z, p.ground()); // paved courtyard
                }
            }
        }

        // Gatehouse breaching the south curtain wall.
        b.room(cx - 4, ground + 1, cz + 21, cx + 4, ground + 11, cz + 27);
        b.crenellations(cx - 4, cz + 21, cx + 4, cz + 27, ground + 12);
        StyleKit.twoHighDoor(b, p, cx, cz + 27, ground + 1, Direction.SOUTH);
        b.put(cx - 1, ground + 4, cz + 27, p.light());
        b.put(cx + 1, ground + 4, cz + 27, p.light());

        // Domineering central keep/watchtower: a tall whitewashed shaft on a battered base,
        // string-coursed into storeys, with a balcony ring of crenellations, a thatch cap and a
        // flag spire — the vertical element that lifts the star-fort over the city skyline.
        int kr = 9;
        int kz = 2;
        int keepBase = ground + 1;
        int keepTop = ground + 66;
        b.fill(cx - kr - 1, keepBase, cz - kz - 1, cx + kr + 1, keepBase + 7, cz + kz + 1, p.foundation());
        b.room(cx - kr, keepBase, cz - kz, cx + kr, keepTop, cz + kz,
                new Doorway(Side.S, kr));
        for (int y = keepBase; y <= keepTop; y++) {
            b.put(cx - kr, y, cz - kz, p.accent());
            b.put(cx + kr, y, cz - kz, p.accent());
            b.put(cx - kr, y, cz + kz, p.accent());
            b.put(cx + kr, y, cz + kz, p.accent());
        }
        // Cream string-courses ring the shaft every eight blocks.
        for (int y = keepBase + 8; y <= keepTop - 4; y += 8) {
            b.fill(cx - kr, y, cz - kz, cx + kr, y, cz + kz, p.weathered());
        }
        // Paired slit windows up each long face, storey by storey.
        for (int y = keepBase + 6; y <= keepTop - 10; y += 8) {
            b.window(cx - kr, y, cz, 4, 1, true);
            b.window(cx + kr, y, cz, 4, 1, true);
            b.window(cx, y, cz - kz, 4, 1, true);
        }
        // Wide arched watch-room just under the parapet, then the crenellated balcony.
        b.window(cx, keepTop - 9, cz - kz, 8, 3, true);
        b.window(cx, keepTop - 9, cz + kz, 8, 3, true);
        b.window(cx - kr, keepTop - 9, cz, 8, 3, true);
        b.window(cx + kr, keepTop - 9, cz, 8, 3, true);
        b.crenellations(cx - kr - 1, cz - kz - 1, cx + kr + 1, cz + kz + 1, keepTop);
        b.pitchedRoof(cx - kr, cz - kz, cx + kr, cz + kz, keepTop + 1, 6, 0);
        b.spire(cx, cz, keepTop + 7, 6);
        b.put(cx, keepTop + 13, cz, p.light());
    }

    @Override
    public void flourish(StructureBuilder b, RandomSource rng, int x, int y0, int z,
                         int x1, int y1, int z1, Palette p) {
        // Painted green cornice along the top of the walls.
        b.walls(x, y1, z, x1, y1, z1, p.accent());
        // A raised stoep step along the front (minimum-z) face.
        b.fill(x, y0 - 1, z - 1, x1, y0 - 1, z - 1, p.foundation());
    }

    @Override
    public void streetProps(StructureBuilder b, RandomSource rng, int cx, int cz,
                            int district, int ground, Palette p) {
        // Lamp posts down the two main axes.
        for (int d = 24; d <= district - 8; d += 16) {
            StyleKit.lanternPost(b, cx + d, cz + 3, ground, p);
            StyleKit.lanternPost(b, cx - d, cz - 3, ground, p);
            StyleKit.lanternPost(b, cx + 4, cz + d, ground, p);
            StyleKit.lanternPost(b, cx - 4, cz - d, ground, p);
        }
        // A standing statue and a plinth monument mark the district.
        StyleKit.statue(b, cx + 10, cz - 10, ground, p);
        b.monument(cx - 12, cz + 12, ground);
        // Harbour cranes on the seaward edge.
        harbourCrane(b, cx + district - 12, cz - 6, ground, p);
        harbourCrane(b, cx + district - 6, cz + 8, ground, p);
    }

    // ------------------------------------------------------------------ local helpers

    /** A long whitewashed manor with a thatch roof, front stoep and an ornate stepped gable. */
    private static void capeDutchManor(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int halfW = 6;
        int halfD = 7;
        int wallH = 6;
        int x0 = cx - halfW;
        int x1 = cx + halfW;
        int z0 = cz - halfD;
        int z1 = cz + halfD;
        int y0 = ground + 1;
        int y1 = y0 + wallH;

        // Paved werf and the manor shell, entered at the south end.
        b.ground(x0 - 2, z0 - 2, x1 + 2, z1 + 2, ground, ground, p.foundation());
        b.room(x0, y0, z0, x1, y1, z1, new Doorway(Side.S, halfW));
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, y1, 6, 1);

        // Tall windows along the long east/west faces.
        for (int z = z0 + 3; z <= z1 - 3; z += 3) {
            b.window(x0, y0 + 2, z, 2, 2, true);
            b.window(x1, y0 + 2, z, 2, 2, true);
        }

        // Stoep steps up to the entrance, then the ornamental gable over it.
        b.fill(cx - 1, ground, z1 + 1, cx + 1, ground, z1 + 2, p.foundation());
        StyleKit.twoHighDoor(b, p, cx, z1, y0 + 1, Direction.SOUTH);
        capeDutchGable(b, cx, z1, y1, p);
    }

    /** A stepped/curved Cape Dutch gable of whitewash, green pilasters, slab scrolls and a spire. */
    private static void capeDutchGable(StructureBuilder b, int cx, int z, int baseY, Palette p) {
        for (int dy = 0; dy < GABLE_HALF.length; dy++) {
            int y = baseY + dy;
            int h = GABLE_HALF[dy];
            b.fill(cx - h, y, z, cx + h, y, z, p.wall());
            // Green pilaster trim stepping inward at both gable edges.
            b.put(cx - h, y, z, p.accent());
            b.put(cx + h, y, z, p.accent());
            // Slab scrolls capping each shoulder of the step.
            if (h > 0) {
                b.put(cx - h, y + 1, z, p.roofSlab());
                b.put(cx + h, y + 1, z, p.roofSlab());
            }
        }
        // Central green motif and glazed vent, then a small finial spire.
        b.put(cx, baseY + 1, z, p.accent());
        b.put(cx, baseY + 3, z, p.window());
        b.spire(cx, z, baseY + GABLE_HALF.length, 3);
    }

    /** A harbour crane: a timber mast, an out-reaching jib beam, and a hanging hoist. */
    private static void harbourCrane(StructureBuilder b, int x, int z, int ground, Palette p) {
        int mastH = 7;
        int top = ground + mastH;
        b.fill(x - 1, ground + 1, z - 1, x + 1, ground + 1, z + 1, p.foundation());
        for (int y = ground + 1; y <= top; y++) {
            b.put(x, y, z, p.accent());
        }
        // Jib beam reaching out over the water, with a brace back to the mast.
        b.fill(x, top, z, x + 6, top, z, p.accent());
        b.put(x + 4, top - 1, z, p.accent());
        // Hoist hook: fence links dropping to a lantern.
        b.put(x + 6, top - 1, z, p.rail());
        b.put(x + 6, top - 2, z, p.rail());
        b.put(x + 6, top - 3, z, p.light());
    }
}
