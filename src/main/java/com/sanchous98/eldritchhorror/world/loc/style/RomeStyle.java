package com.sanchous98.eldritchhorror.world.loc.style;

import com.sanchous98.eldritchhorror.world.loc.Palette;
import com.sanchous98.eldritchhorror.world.loc.StructureBuilder;
import com.sanchous98.eldritchhorror.world.loc.StructureBuilder.Doorway;
import com.sanchous98.eldritchhorror.world.loc.StructureBuilder.Side;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Rome — classical: pale travertine/limestone ashlar, terracotta pantile roofs, engaged columns,
 * arches and colonnaded piazzas, with umbrella pines in the streets. Culture leads; climate only
 * keeps the region's overgrowth (and swaps it for kelp on the coast).
 *
 * <p>Landmark: a great domed basilica with a forward portico and a sweeping piazza colonnade
 * (St Peter's-like), built from {@link StyleKit#dome} over a tiled nave. Street props: colonnades,
 * statues and obelisks on the axis, and lantern posts. Follows {@link TokyoStyle}; shared shapes
 * live in {@link StyleKit}.
 */
public final class RomeStyle implements CityStyle {

    /** Terracotta pantile — the warm roof of the whole city. */
    private static BlockState pantile() {
        return Materials.terracotta(DyeColor.ORANGE);
    }

    @Override
    public String id() {
        return "rome";
    }

    @Override
    public Palette palette(int koppenClass, boolean coastal) {
        // Culture leads: travertine ashlar + terracotta pantiles, whatever the Köppen class.
        Palette bio = Palette.fromBiome(koppenClass, coastal);
        return new Palette(
                Blocks.SMOOTH_STONE.defaultBlockState(),            // ground: travertine paving
                Blocks.BRICKS.defaultBlockState(),                  // foundation: brick footing
                Blocks.SMOOTH_QUARTZ.defaultBlockState(),           // wall: travertine ashlar
                Blocks.CALCITE.defaultBlockState(),                 // weathered: mottled pale stone
                Blocks.QUARTZ_PILLAR.defaultBlockState(),           // accent: column shafts
                pantile(),                                          // roof: terracotta pantile
                Blocks.BRICK_STAIRS.defaultBlockState(),            // roof stairs (terracotta)
                Blocks.BRICK_SLAB.defaultBlockState(),              // roof slabs / eaves
                Blocks.GLASS_PANE.defaultBlockState(),              // window
                Blocks.CHISELED_QUARTZ_BLOCK.defaultBlockState(),   // frame: carved stone capital
                Blocks.DARK_OAK_DOOR.defaultBlockState(),           // door: heavy timber gate
                Blocks.IRON_BARS.defaultBlockState(),               // rail: bronze/iron grille
                Blocks.LANTERN.defaultBlockState(),                 // light: hanging lantern
                coastal ? Blocks.KELP.defaultBlockState() : bio.overgrowth(),
                Blocks.GRAVEL.defaultBlockState());                 // rubble
    }

    /** The palette with a pale stone dome — the cupola of the basilica is masonry, not tile. */
    private static Palette stoneDomed(Palette p) {
        return new Palette(p.ground(), p.foundation(), p.wall(), p.weathered(),
                p.accent(), Blocks.CALCITE.defaultBlockState(), Blocks.CALCITE.defaultBlockState(),
                Blocks.CALCITE.defaultBlockState(), p.window(), p.frame(), p.door(), p.rail(),
                p.light(), p.overgrowth(), p.rubble());
    }

    // ------------------------------------------------------------------ landmark

    @Override
    public void landmark(StructureBuilder b, RandomSource rng, int cx, int cz, int ground, Palette p) {
        // The Colosseum: a wide, low elliptical ring of stacked arched tiers. One arc of the
        // outer wall is ruined away to a single storey with a jagged broken edge.
        int aOut = 30;      // outer semi-axis (60 blocks across)
        int bOut = 24;      // outer semi-axis on Z (48 blocks across)
        int aIn = 18;       // arena semi-axis
        int bIn = 12;
        int wallTop = ground + 38;
        int ruinsStart = 300;      // degrees where the ruin arc begins
        int ruinsEnd = 360;        // ...and ends

        // --- Paved plaza apron under the whole monument. ---
        b.ground(cx - aOut - 4, cz - bOut - 4, cx + aOut + 4, cz + bOut + 4, ground, ground, p.ground());

        // --- Arena floor, sunken one course. ---
        b.ground(cx - aIn + 1, cz - bIn + 1, cx + aIn - 1, cz + bIn - 1, ground - 1, ground - 1, p.rubble());

        // --- Walk the ellipse in 3° segments; connect each to the previous. ---
        int steps = 120;
        int prevX = 0;
        int prevZ = 0;
        int prevTop = ground;
        boolean prevRuin = false;
        for (int s = 0; s <= steps; s++) {
            int deg = (s * 3) % 360;
            double t = Math.toRadians(deg);
            double ct = Math.cos(t);
            double st = Math.sin(t);
            int xo = (int) Math.round(cx + aOut * ct);
            int zo = (int) Math.round(cz + bOut * st);
            int xi = (int) Math.round(cx + aIn * ct);
            int zi = (int) Math.round(cz + bIn * st);
            boolean ruin = deg >= ruinsStart && deg < ruinsEnd;
            int top;
            if (ruin) {
                top = ground + 8 + ((deg / 3) % 3);            // jagged broken top
            } else {
                top = wallTop - (((deg / 6) % 2) == 0 ? 0 : 1); // slight raggedness on the skyline
            }
            if (s > 0) {
                if (prevRuin || ruin) {
                    b.fill(prevX, ground + 1, prevZ, xo, Math.min(prevTop, top), zo, p.rubble());
                } else {
                    archedRing(b, prevX, prevZ, xo, zo, ground, top, p);
                }
            }
            // The inner wall of the arena is always plain travertine, lower where ruined.
            b.fill(xi, ground + 1, zi, xi, ruin ? ground + 6 : wallTop - 3, zi, p.weathered());
            prevX = xo;
            prevZ = zo;
            prevTop = top;
            prevRuin = ruin;
        }

        // --- A rubble fall tumbling out of the broken end. ---
        b.fill((int) Math.round(cx + aOut * Math.cos(Math.toRadians(330))) - 2, ground,
                (int) Math.round(cz + bOut * Math.sin(Math.toRadians(330))) - 2,
                (int) Math.round(cx + aOut * Math.cos(Math.toRadians(330))) + 2, ground + 3,
                (int) Math.round(cz + bOut * Math.sin(Math.toRadians(330))) + 2, p.rubble());
    }

    /**
     * A short run of the outer wall between two ring points: solid travertine with two tiers of
     * arched openings painted on the outer face, and a cornice band at the top.
     */
    private static void archedRing(StructureBuilder b, int x0, int z0, int x1, int z1,
                                   int ground, int top, Palette p) {
        b.fill(x0, ground + 1, z0, x1, top, z1, p.wall());
        // Ground-floor arcade: a tall arch on a 3-column rhythm.
        for (int y = ground + 2; y <= ground + 11; y++) {
            b.put(x0, y, z0, p.accent());
        }
        b.put(x0, ground + 5, z0, p.window());
        b.put(x0, ground + 6, z0, p.window());
        b.put(x0, ground + 7, z0, p.window());
        // Second storey, slightly narrower arch.
        for (int y = ground + 14; y <= ground + 22; y++) {
            b.put(x0, y, z0, p.accent());
        }
        b.put(x0, ground + 17, z0, p.window());
        b.put(x0, ground + 18, z0, p.window());
        // Third storey: a small square opening.
        b.put(x0, ground + 26, z0, p.frame());
        b.put(x0, ground + 27, z0, p.frame());
        // Cornice cap over this segment.
        b.fill(x0, top, z0, x1, top, z1, p.roofSlab());
    }

    /** A domed basilica: a tiled nave, a columned portico and a piazza colonnade around it. */
    private static void basilica(StructureBuilder b, RandomSource rng, int cx, int cz,
                                 int ground, Palette p) {
        int w = 11;
        int l = 15;
        int h = 9;
        int x0 = cx - w / 2;
        int x1 = x0 + w - 1;
        int z0 = cz - l / 2;
        int z1 = z0 + l - 1;
        int y0 = ground + 1;
        int y1 = y0 + h;

        // Paved apron, then the nave and its terracotta pantile roof.
        b.ground(x0 - 2, z0 - 2, x1 + 2, z1 + 4, ground, ground, p.ground());
        b.room(x0, y0, z0, x1, y1, z1, new Doorway(Side.S, w / 2));
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, y1, 6, 1);

        // A row of tall arched windows down each flank.
        for (int z = z0 + 3; z <= z1 - 3; z += 3) {
            b.window(x0, y0 + 3, z, 4, 1, true);
            b.window(x1, y0 + 3, z, 4, 1, true);
        }

        // The great dome over the crossing, on a low drum.
        int drum = y1;
        b.crenellations(x0, z0 + 4, x1, z0 + 6, drum + 1);
        StyleKit.dome(b, cx, cz, drum, 6, stoneDomed(p));

        int py = y0 + h - 1;               // entablature height of the portico
        portico(b, cx, z1 + 2, w, py, ground, p);
        piazzaColonnade(b, cx, cz, ground, p);
    }

    /** A classical portico: a columned front row carrying an architrave, with steps below. */
    private static void portico(StructureBuilder b, int cx, int z, int w, int topY,
                                int ground, Palette p) {
        int half = w / 2;
        for (int x = cx - half; x <= cx + half; x += 2) {
            column(b, x, z, ground, p);
        }
        b.fill(cx - half, topY, z, cx + half, topY, z, p.foundation());          // architrave
        b.fill(cx - half, topY + 1, z, cx + half, topY + 1, z, p.roofSlab());    // cornice
        for (int s = 0; s < 2; s++) {                                            // front steps
            b.fill(cx - half, ground, z + 2 + s, cx + half, ground, z + 2 + s, p.foundation());
        }
    }

    /** A U-shaped colonnade enclosing the piazza in front of the basilica, under a cornice. */
    private static void piazzaColonnade(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int arm = 16;
        int back = cz + 20;
        int topY = ground + 7;
        for (int z = cz + 7; z <= back; z += 2) {
            column(b, cx - arm, z, ground, p);
            column(b, cx + arm, z, ground, p);
        }
        for (int x = cx - arm; x <= cx + arm; x += 2) {
            column(b, x, back, ground, p);
        }
        // Continuous entablature and a balustrade silhouette over the whole sweep.
        b.fill(cx - arm, topY, cz + 7, cx - arm, topY, back, p.foundation());
        b.fill(cx + arm, topY, cz + 7, cx + arm, topY, back, p.foundation());
        b.fill(cx - arm, topY, back, cx + arm, topY, back, p.foundation());
        b.crenellations(cx - arm, cz + 7, cx - arm, back, topY + 1);
        b.crenellations(cx + arm, cz + 7, cx + arm, back, topY + 1);
        b.crenellations(cx - arm, back, cx + arm, back, topY + 1);
    }

    // ------------------------------------------------------------------ props

    /** A squat travertine column: base, fluted shaft in accent, carved capital. */
    private static void column(StructureBuilder b, int x, int z, int ground, Palette p) {
        b.put(x, ground + 1, z, p.foundation());
        for (int y = ground + 2; y <= ground + 5; y++) {
            b.put(x, y, z, p.accent());
        }
        b.put(x, ground + 6, z, p.frame());
    }

    /** An umbrella pine: a bare pale trunk under a broad flat crown of needles (palette leaves). */
    private static void umbrellaPine(StructureBuilder b, int x, int z, int ground, Palette p) {
        if (p.overgrowth() == null) {
            return; // polar snowscape: no foliage to make a pine from
        }
        for (int y = ground + 1; y <= ground + 4; y++) {
            b.put(x, y, z, p.accent());
        }
        BlockState crown = p.overgrowth();
        b.fill(x - 3, ground + 5, z - 3, x + 3, ground + 5, z + 3, crown);
        b.fill(x - 2, ground + 6, z - 2, x + 2, ground + 6, z + 2, crown);
        b.put(x, ground + 7, z, crown);
    }

    @Override
    public void streetProps(StructureBuilder b, RandomSource rng, int cx, int cz,
                            int district, int ground, Palette p) {
        // Obelisk and statues on axis: the piazza's classical furniture.
        StyleKit.obelisk(b, cx - 13, cz + 13, ground, p);
        StyleKit.statue(b, cx - 5, cz + 20, ground, p);
        StyleKit.statue(b, cx + 5, cz + 20, ground, p);

        // Lantern posts down the two main avenues, jittered deterministically.
        int step = 12 + rng.nextInt(4);
        for (int d = 30; d <= district - 12; d += step) {
            StyleKit.lanternPost(b, cx + d, cz - 3, ground, p);
            StyleKit.lanternPost(b, cx - d, cz + 3, ground, p);
        }

        // Umbrella pines scattered through the district, avoiding the clear central ring.
        for (int i = 0; i < 14; i++) {
            int px = cx + rng.nextInt(district * 2 + 1) - district;
            int pz = cz + rng.nextInt(district * 2 + 1) - district;
            int dx = px - cx;
            int dz = pz - cz;
            if (dx * dx + dz * dz < 26 * 26) {
                continue; // keep the plaza and landmark clear
            }
            umbrellaPine(b, px, pz, b.groundY(px, pz), p);
        }
    }

    @Override
    public void flourish(StructureBuilder b, RandomSource rng, int x, int y0, int z,
                         int x1, int y1, int z1, Palette p) {
        // A stone cornice under the eaves, and a pair of engaged pilasters on some facades.
        b.fill(x, y1, z, x1, y1, z1, p.foundation());
        if (rng.nextFloat() < 0.4F) {
            b.put(x, y0 + 1, z, p.accent());
            b.put(x1, y0 + 1, z, p.accent());
        }
    }
}
