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
 * Jakarta — Dutch-colonial and Javanese. Whitewashed plaster over red-brick bases under red
 * tile roofs, shuttered windows, teak verandas, tiled mosques, canals and tropical growth.
 *
 * <p>Landmark: a colonial town hall — a long whitewashed block under a red tile roof, a
 * columned portico, and a central domed cupola ({@code room} + {@code pitchedRoof} +
 * {@link StyleKit#dome}). Flourish: a timber veranda on every terrace. Street props: lantern
 * posts, statues, market stalls and a short canal along the district edge.
 *
 * <p>Culture leads; Köppen only nudges the overgrowth (coast swaps it for kelp). Deterministic;
 * only {@link StructureBuilder} + {@link Palette} (+ {@link StyleKit}/ {@link Materials}) blocks.
 */
public final class JakartaStyle implements CityStyle {

    @Override
    public String id() {
        return "jakarta";
    }

    @Override
    public Palette palette(int koppenClass, boolean coastal) {
        Palette bio = Palette.fromBiome(koppenClass, coastal);
        return new Palette(
                Blocks.SMOOTH_QUARTZ.defaultBlockState(),           // ground: pale flagstone street
                Blocks.BRICKS.defaultBlockState(),                  // foundation: red brick base
                Materials.whiteConcrete(),                          // wall: whitewashed plaster
                Materials.lightGrayConcrete(),                      // weathered: aged whitewash
                Blocks.STRIPPED_JUNGLE_LOG.defaultBlockState(),     // accent: Javanese teak
                Materials.terracotta(DyeColor.RED),                 // roof: red clay tile body
                Blocks.BRICK_STAIRS.defaultBlockState(),            // roof stairs: red tile
                Blocks.BRICK_SLAB.defaultBlockState(),              // roof slabs / eaves
                Blocks.GLASS_PANE.defaultBlockState(),              // window
                Materials.concrete(DyeColor.CYAN),                  // frame: teal louvred shutters
                Blocks.JUNGLE_DOOR.defaultBlockState(),             // door
                Blocks.JUNGLE_FENCE.defaultBlockState(),            // rail: veranda railing
                Blocks.LANTERN.defaultBlockState(),                 // light: oil lantern
                coastal ? Blocks.KELP.defaultBlockState() : bio.overgrowth(),
                Blocks.GRAVEL.defaultBlockState());                 // rubble
    }

    // ------------------------------------------------------------------ landmark

    @Override
    public void landmark(StructureBuilder b, RandomSource rng, int cx, int cz, int ground, Palette p) {
        // Monas (National Monument): a stepped white-marble plinth carrying a slender square
        // obelisk, crowned by a gold-coated flame. Read as silhouette: plaza, five receding
        // steps, a square "cawan" cup, the tapering shaft and the flame.
        int g = ground;

        // Paved plaza around the base.
        b.ground(cx - 16, cz - 16, cx + 16, cz + 16, g, g, p.ground());

        // Five receding square steps — a dark base course, then marble above.
        for (int i = 0; i < 5; i++) {
            int half = 14 - i * 2;                 // 14, 12, 10, 8, 6
            b.fill(cx - half, g + 1 + i, cz - half, cx + half, g + 1 + i, cz + half,
                    i < 2 ? p.foundation() : p.wall());
        }

        // The "cawan": a square marble cup flaring at the foot of the shaft.
        int cupBase = g + 6;
        b.fill(cx - 7, cupBase, cz - 7, cx + 7, cupBase + 1, cz + 7, p.foundation());
        b.fill(cx - 6, cupBase + 2, cz - 6, cx + 6, cupBase + 4, cz + 6, p.wall());
        b.walls(cx - 6, cupBase + 4, cz - 6, cx + 6, cupBase + 4, cz + 6, p.foundation());

        // The slender obelisk shaft, tapering 7x7 -> 3x3.
        int shaftBase = cupBase + 5;               // g + 11
        int shaftTop = shaftBase + 58;             // g + 69
        int span = shaftTop - shaftBase;
        for (int y = shaftBase; y <= shaftTop; y++) {
            int half = 3 - (y - shaftBase) * 2 / span;   // 3 -> 1
            b.fill(cx - half, y, cz - half, cx + half, y, cz + half, p.wall());
        }
        // A slightly darker seam down opposite vertical corners.
        for (int y = shaftBase; y <= shaftTop - 4; y++) {
            int half = 3 - (y - shaftBase) * 2 / span;
            b.put(cx - half, y, cz - half, p.weathered());
            b.put(cx + half, y, cz + half, p.weathered());
        }

        // The golden flame: a gold collar, then a tapering teardrop and its lit core.
        int flameBase = shaftTop + 1;
        b.fill(cx - 2, flameBase, cz - 2, cx + 2, flameBase, cz + 2,
                Materials.glazed(DyeColor.YELLOW));
        int[] flame = {3, 3, 3, 2, 2, 2, 1, 1, 0};
        for (int i = 0; i < flame.length; i++) {
            int half = flame[i];
            b.fill(cx - half, flameBase + 1 + i, cz - half,
                    cx + half, flameBase + 1 + i, cz + half,
                    Materials.glazed(DyeColor.YELLOW));
        }
        b.put(cx, flameBase + 5, cz, p.light());
    }

    /** A colonial town hall: whitewashed hall, teak pilasters, shuttered facade, domed cupola. */
    private static void townHall(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int w = 21;
        int d = 15;
        int wallH = 8;
        int x0 = cx - w / 2;
        int x1 = x0 + w - 1;
        int z0 = cz - d / 2;
        int z1 = z0 + d - 1;
        int y0 = ground + 1;
        int y1 = y0 + wallH;

        // Paved werf, the hall shell and its long red-tile ridge.
        b.ground(x0 - 3, z0 - 3, x1 + 3, z1 + 3, ground, ground, p.foundation());
        b.room(x0, y0, z0, x1, y1, z1, new Doorway(Side.S, w / 2));
        b.pitchedRoof(x0 - 1, z0 - 1, x1 + 1, z1 + 1, y1, 6, 0);
        StyleKit.twoHighDoor(b, p, cx, z1, y0 + 1, Direction.SOUTH);

        // Teak corner pilasters and a cornice band under the eaves.
        for (int y = y0; y < y1; y++) {
            b.put(x0, y, z0, p.accent());
            b.put(x1, y, z0, p.accent());
            b.put(x0, y, z1, p.accent());
            b.put(x1, y, z1, p.accent());
        }
        b.walls(x0, y1 - 1, z0, x1, y1 - 1, z1, p.accent());

        // Rows of shuttered windows (the trapdoor frame reads as louvres).
        for (int x = x0 + 3; x <= x1 - 3; x += 4) {
            b.window(x, y0 + 3, z0, 3, 2, true);
            b.window(x, y0 + 3, z1, 3, 2, true);
        }
        for (int z = z0 + 3; z <= z1 - 3; z += 4) {
            b.window(x0, y0 + 3, z, 3, 1, true);
            b.window(x1, y0 + 3, z, 3, 1, true);
        }

        portico(b, cx, z1, y0, ground, p);
        cupola(b, cx, cz, y1, p);
    }

    /** A projecting colonnaded entrance portico with a small gable on the south front. */
    private static void portico(StructureBuilder b, int cx, int z1, int y0, int ground, Palette p) {
        int pz0 = z1 + 1;
        int pz1 = z1 + 6;
        b.fill(cx - 6, ground, pz0, cx + 6, ground, pz1, p.foundation()); // terrace
        for (int dx = -5; dx <= 5; dx += 5) {
            for (int y = y0 + 1; y <= y0 + 5; y++) {
                b.put(cx + dx, y, pz0, p.accent());
                b.put(cx + dx, y, pz1, p.accent());
            }
        }
        b.pitchedRoof(cx - 6, pz0, cx + 6, pz1, y0 + 6, 3, 0);
    }

    /** The crowning drum and dome of the cupola, with shuttered clerestory windows. */
    private static void cupola(StructureBuilder b, int cx, int cz, int roofTop, Palette p) {
        int drumBase = roofTop + 10; // sits clear of the tile ridge below
        int drumTop = drumBase + 4;
        int r = 3;
        b.room(cx - r, drumBase, cz - r, cx + r, drumTop, cz + r);
        for (int y = drumBase; y <= drumTop; y++) {
            b.put(cx - r, y, cz - r, p.accent());
            b.put(cx + r, y, cz - r, p.accent());
            b.put(cx - r, y, cz + r, p.accent());
            b.put(cx + r, y, cz + r, p.accent());
        }
        b.window(cx - r, drumBase + 1, cz, 2, 1, true);
        b.window(cx + r, drumBase + 1, cz, 2, 1, true);
        b.window(cx, drumBase + 1, cz - r, 2, 1, true);
        b.window(cx, drumBase + 1, cz + r, 2, 1, true);
        StyleKit.dome(b, cx, cz, drumTop, 4, p);
    }

    // ------------------------------------------------------------------ per-building

    @Override
    public void flourish(StructureBuilder b, RandomSource rng, int x, int y0, int z,
                         int x1, int y1, int z1, Palette p) {
        // Too small for a veranda front.
        if (x1 - x < 4 || y1 - y0 < 4) {
            return;
        }
        int pz = z1 + 1;
        // Raised stoep under the timber veranda on the street (south) face.
        b.fill(x, y0 - 1, pz, x1, y0 - 1, pz + 1, p.foundation());
        // Rail between the posts, then two-high teak posts on top of it.
        b.fill(x, y0 + 1, pz + 1, x1, y0 + 1, pz + 1, p.rail());
        for (int xx = x; xx <= x1; xx += 2) {
            b.put(xx, y0, pz + 1, p.accent());
            b.put(xx, y0 + 1, pz + 1, p.accent());
        }
        // Red-tile awning over the veranda.
        b.fill(x, y0 + 2, pz, x1, y0 + 2, pz + 1, p.roofSlab());
    }

    // ------------------------------------------------------------------ street furniture

    @Override
    public void streetProps(StructureBuilder b, RandomSource rng, int cx, int cz,
                            int district, int ground, Palette p) {
        // Oil-lantern posts down the two main axes.
        for (int d = 26; d <= district - 12; d += 18) {
            StyleKit.lanternPost(b, cx + d, cz + 3, ground, p);
            StyleKit.lanternPost(b, cx - d, cz - 3, ground, p);
        }
        // Statues on the approaches to the hall.
        StyleKit.statue(b, cx + 14, cz - 10, ground, p);
        StyleKit.statue(b, cx - 14, cz + 10, ground, p);
        // A row of market stalls along one street.
        for (int i = 0; i < 3; i++) {
            marketStall(b, cx - 14 + i * 14, cz + 26, ground, p);
        }
        // A short brick-banked canal along the far edge of the district.
        canal(b, cx, cz + district - 10, ground, p);
    }

    /** A canopy market stall: four teak posts, a red awning, a counter and a hanging lantern. */
    private static void marketStall(StructureBuilder b, int x, int z, int ground, Palette p) {
        b.fill(x, ground + 1, z, x, ground + 2, z, p.accent());
        b.fill(x + 3, ground + 1, z, x + 3, ground + 2, z, p.accent());
        b.fill(x, ground + 1, z + 2, x, ground + 2, z + 2, p.accent());
        b.fill(x + 3, ground + 1, z + 2, x + 3, ground + 2, z + 2, p.accent());
        b.fill(x, ground + 3, z, x + 3, ground + 3, z + 2, Materials.terracotta(DyeColor.RED));
        b.fill(x, ground + 1, z + 1, x + 3, ground + 1, z + 1, p.foundation());
        b.put(x + 1, ground + 2, z + 1, p.light());
    }

    /** A straight canal: a water channel with brick banks, reading as the city's waterways. */
    private static void canal(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int half = 22;
        b.ground(cx - half, cz - 2, cx + half, cz + 2, ground - 2, ground - 1,
                Blocks.WATER.defaultBlockState());
        b.ground(cx - half, cz - 3, cx + half, cz - 3, ground, ground, p.foundation());
        b.ground(cx - half, cz + 3, cx + half, cz + 3, ground, ground, p.foundation());
    }
}
