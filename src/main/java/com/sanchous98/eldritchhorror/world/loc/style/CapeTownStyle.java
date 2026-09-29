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
        capeDutchManor(b, cx, cz, ground, p);
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
