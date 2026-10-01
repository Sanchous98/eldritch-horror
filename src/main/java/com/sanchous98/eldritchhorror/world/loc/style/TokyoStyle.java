package com.sanchous98.eldritchhorror.world.loc.style;

import com.sanchous98.eldritchhorror.world.loc.Palette;
import com.sanchous98.eldritchhorror.world.loc.StructureBuilder;
import com.sanchous98.eldritchhorror.world.loc.StructureBuilder.Doorway;
import com.sanchous98.eldritchhorror.world.loc.StructureBuilder.Side;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;

/**
 * Tokyo — the reference cultural style. Japanese: white shikkui plaster with dark timber, grey
 * kawara tile roofs with flared eaves, vermilion gates, paper lanterns and neon signage.
 *
 * <p>Landmark: a five-level pagoda. Street props: torii gates and lantern posts. This is the
 * pattern every other style copies.
 */
public final class TokyoStyle implements CityStyle {

    @Override
    public String id() {
        return "japanese";
    }

    @Override
    public Palette palette(int koppenClass, boolean coastal) {
        // Culture leads: shikkui + timber + kawara, regardless of Köppen. Climate only nudges
        // the overgrowth (wetter → vines); the coast swaps the overgrowth for kelp.
        Palette bio = Palette.fromBiome(koppenClass, coastal);
        return new Palette(
                Blocks.GRAVEL.defaultBlockState(),                  // ground: raked gravel street
                Blocks.POLISHED_ANDESITE.defaultBlockState(),       // foundation
                Materials.whiteConcrete(),                          // wall: shikkui plaster
                Blocks.CALCITE.defaultBlockState(),                 // weathered plaster
                Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState(),   // accent: dark timber beams
                Blocks.DEEPSLATE_TILES.defaultBlockState(),         // roof: kawara tile
                Blocks.DEEPSLATE_TILE_STAIRS.defaultBlockState(),   // roof stairs
                Blocks.DEEPSLATE_TILE_SLAB.defaultBlockState(),     // roof slabs / eaves
                Blocks.GLASS_PANE.defaultBlockState(),              // window
                Blocks.SPRUCE_TRAPDOOR.defaultBlockState(),         // frame: koushi lattice
                Blocks.DARK_OAK_DOOR.defaultBlockState(),           // door
                Blocks.DARK_OAK_FENCE.defaultBlockState(),          // rail
                Blocks.SHROOMLIGHT.defaultBlockState(),             // light: paper lantern glow
                bio.overgrowth(),
                Blocks.GRAVEL.defaultBlockState());                 // rubble: raked gravel
    }

    @Override
    public void flourish(StructureBuilder b, RandomSource rng, int x, int y0, int z,
                         int x1, int y1, int z1, Palette p) {
        // The defining Japanese look: dark timber beams framing pale shikkui walls.
        // Horizontal sill and lintel courses all round, plus a vertical post at one corner.
        for (int y = y0; y <= y0 + 1 && y <= y1; y++) {
            b.fill(x, y, z, x1, y, z, p.accent());
            b.fill(x, y, z1, x1, y, z1, p.accent());
            b.fill(x, y, z, x, y, z1, p.accent());
            b.fill(x1, y, z, x1, y, z1, p.accent());
        }
        for (int y = y0; y < y1; y += 2) {
            b.fill(x, y, z, x1, y, z, p.accent());
            b.fill(x, y, z1, x1, y, z1, p.accent());
        }
        // A cherry-blossom accent tree beside some buildings.
        if (rng.nextFloat() < 0.3f) {
            int tx = x - 2;
            int tz = z - 2;
            int gy = b.groundY(tx, tz);
            b.put(tx, gy + 1, tz, Blocks.CHERRY_LOG.defaultBlockState());
            b.put(tx, gy + 2, tz, Blocks.CHERRY_LOG.defaultBlockState());
            b.fill(tx - 1, gy + 3, tz - 1, tx + 1, gy + 3, tz + 1,
                    Blocks.CHERRY_LEAVES.defaultBlockState());
            b.put(tx, gy + 4, tz, Blocks.CHERRY_LEAVES.defaultBlockState());
        }
    }

    @Override
    public void landmark(StructureBuilder b, RandomSource rng, int cx, int cz, int ground, Palette p) {
        int y0 = ground + 1;

        // Plaza and the wide, stepped stone tenshu-dai (castle base).
        b.ground(cx - 24, cz - 24, cx + 24, cz + 24, ground, ground, p.foundation());
        int baseH = 7;
        for (int i = 0; i < baseH; i++) {
            int h = 20 - i;                       // 20 .. 14, wide at the foot, battered inward
            int y = y0 + i;
            b.fill(cx - h, y, cz - h, cx + h, y, cz + h,
                    (i % 3 == 2) ? p.weathered() : p.foundation());
            if (i == baseH - 1) {                 // dark timber cap course under the keep
                b.walls(cx - h, y, cz - h, cx + h, y, cz + h, p.accent());
            }
        }

        // A real, walk-in gate: cut a stepped passage up through the stone base on the south
        // face, from the plaza to the keep floor, so the tenshu-dai is entered, not sealed.
        for (int i = 0; i <= baseH; i++) {
            int z = cz + 20 - i;                 // one course higher for each block inward
            b.fill(cx - 1, y0 + i, z, cx + 1, y0 + i, z, p.foundation());   // tread
        }
        // Clear the headroom above the rising treads so the stair is walkable, not buried.
        for (int i = 0; i <= baseH - 1; i++) {
            int z = cz + 20 - i;
            for (int y = y0 + i + 1; y <= y0 + baseH; y++) {
                b.put(cx - 1, y, z, Blocks.AIR.defaultBlockState());
                b.put(cx, y, z, Blocks.AIR.defaultBlockState());
                b.put(cx + 1, y, z, Blocks.AIR.defaultBlockState());
            }
        }
        // Dark timber posts flank the gate mouth; the gate leaves sit in the keep wall at floor level.
        for (int y = y0; y <= y0 + 4; y++) {
            b.put(cx - 2, y, cz + 20, p.accent());
            b.put(cx + 2, y, cz + 20, p.accent());
        }
        StyleKit.twoHighDoor(b, p, cx, cz + 13, y0 + baseH + 1, Direction.SOUTH);

        // Five white-walled tiers, each smaller than the last, over dark flared tile eaves.
        int y = y0 + baseH;                       // first tier floor, sits on the stone base
        int half = 13;
        for (int t = 0; t < 5; t++) {
            int h = 10 - t;
            int x0 = cx - half;
            int x1 = cx + half;
            int z0 = cz - half;
            int z1 = cz + half;
            // Every tier keeps a south doorway on the gate axis (x = cx), so the keep is walk-in.
            b.room(x0, y, z0, x1, y + h - 1, z1, new Doorway(Side.S, half));
            // Himeji's black-and-white banding: dark timber sill and lintel round each tier.
            b.walls(x0, y, z0, x1, y, z1, p.accent());
            b.walls(x0, y + h - 1, z0, x1, y + h - 1, z1, p.accent());
            // Koushi-lattice windows on all four faces.
            int wy = y + 3;
            for (int dx = -half + 3; dx <= half - 3; dx += 4) {
                b.window(cx + dx, wy, z0, 3, 1, true);
                b.window(cx + dx, wy, z1, 3, 1, true);
                b.window(x0, wy, cz + dx, 3, 1, true);
                b.window(x1, wy, cz + dx, 3, 1, true);
            }
            // Dark, deeply overhanging kawara-tile eaves.
            b.pitchedRoof(x0 - 3, z0 - 3, x1 + 3, z1 + 3, y + h - 1, 4, 0);

            y += h + 2;
            half -= 2;
        }

        // Gabled top storey with golden shachihoko on the ridge ends (no spire, unlike the pagoda).
        int topHalf = half;
        b.room(cx - topHalf, y, cz - topHalf, cx + topHalf, y + 5, cz + topHalf);
        b.walls(cx - topHalf, y, cz - topHalf, cx + topHalf, y, cz + topHalf, p.accent());
        b.window(cx - topHalf, y + 2, cz, 2, 1, true);
        b.window(cx + topHalf, y + 2, cz, 2, 1, true);
        b.pitchedRoof(cx - topHalf - 4, cz - topHalf - 4, cx + topHalf + 4, cz + topHalf + 4,
                y + 5, 9, 1);
        int peak = y + 5 + 9;
        b.put(cx, peak, cz - topHalf - 4, p.accent());
        b.put(cx, peak + 1, cz - topHalf - 4, p.light());
        b.put(cx, peak, cz + topHalf + 4, p.accent());
        b.put(cx, peak + 1, cz + topHalf + 4, p.light());
    }

    @Override
    public void streetProps(StructureBuilder b, RandomSource rng, int cx, int cz,
                            int district, int ground, Palette p) {
        // Torii on the four approaches to the plaza.
        StyleKit.torii(b, cx, cz - district + 20, ground, p);
        StyleKit.torii(b, cx, cz + district - 20, ground, p);
        StyleKit.torii(b, cx - district + 20, cz, ground, p);
        StyleKit.torii(b, cx + district - 20, cz, ground, p);
        // Lantern posts down the two main axes.
        for (int d = 30; d <= district - 10; d += 14) {
            StyleKit.lanternPost(b, cx + d, cz + 3, ground, p);
            StyleKit.lanternPost(b, cx - d, cz - 3, ground, p);
        }
    }
}
