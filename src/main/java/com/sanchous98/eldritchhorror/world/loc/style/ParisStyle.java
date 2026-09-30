package com.sanchous98.eldritchhorror.world.loc.style;

import com.sanchous98.eldritchhorror.world.loc.Palette;
import com.sanchous98.eldritchhorror.world.loc.StructureBuilder;
import com.sanchous98.eldritchhorror.world.loc.StructureBuilder.Doorway;
import com.sanchous98.eldritchhorror.world.loc.StructureBuilder.Side;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Blocks;

/**
 * Paris — cultural city style. Haussmann: cream limestone facades with grey stone quoins, steep
 * zinc mansard roofs (with dormers), wrought-iron balconies, tall French windows and grand
 * tree-lined boulevards.
 *
 * <p>Landmark: a Notre-Dame-like gothic cathedral — the shared {@link StyleKit#cathedral} nave
 * given a mirrored <b>twin belfry</b> and a row of <b>flying buttresses</b>. Street props: gaslit
 * lamp posts, an obelisk and a statue on the plaza. The per-building flourish adds the wrap-around
 * iron balconies that define a Parisian facade.
 */
public final class ParisStyle implements CityStyle {

    @Override
    public String id() {
        return "paris";
    }

    @Override
    public Palette palette(int koppenClass, boolean coastal) {
        // Culture leads: limestone + zinc + wrought iron regardless of Köppen. Climate only varies
        // the damp overgrowth (vines/moss in the wet, kelp on the coast).
        Palette bio = Palette.fromBiome(koppenClass, coastal);
        return new Palette(
                Blocks.SMOOTH_STONE.defaultBlockState(),            // ground: grey boulevard paving
                Blocks.STONE_BRICKS.defaultBlockState(),            // foundation: ashlar base course
                Materials.terracotta(DyeColor.WHITE),               // wall: cream Haussmann limestone
                Blocks.CALCITE.defaultBlockState(),                 // weathered limestone
                Blocks.POLISHED_DIORITE.defaultBlockState(),        // accent: grey stone quoins / trim
                Blocks.DEEPSLATE_TILES.defaultBlockState(),         // roof: zinc/slate mansard
                Blocks.DEEPSLATE_TILE_STAIRS.defaultBlockState(),   // roof stairs
                Blocks.DEEPSLATE_TILE_SLAB.defaultBlockState(),     // roof slabs / eaves
                Blocks.GLASS_PANE.defaultBlockState(),              // window: tall French glazing
                Blocks.SPRUCE_TRAPDOOR.defaultBlockState(),         // frame: light window mullions
                Blocks.DARK_OAK_DOOR.defaultBlockState(),           // door
                Blocks.IRON_BARS.defaultBlockState(),               // rail: wrought-iron balcony
                Blocks.LANTERN.defaultBlockState(),                 // light: gas lamp
                bio.overgrowth(),
                Blocks.GRAVEL.defaultBlockState());                 // rubble
    }

    @Override
    public void landmark(StructureBuilder b, RandomSource rng, int cx, int cz, int ground, Palette p) {
        // The Eiffel Tower: four splayed lattice piers on arched bases, meeting at the first
        // platform, then a tapering shaft through two more observation decks to a beacon spire.
        int baseHalf = 16;                 // splayed footprint: 33 blocks across at the feet
        int legR = 2;                      // lattice pier half-width at the base
        int y1 = ground + 32;              // first platform (the legs converge here)
        int y2 = ground + 60;              // second platform
        int y3 = ground + 80;              // top observation deck

        // Plaza apron and four ashlar footing pads under the piers.
        b.ground(cx - 20, cz - 20, cx + 20, cz + 20, ground, ground, p.ground());
        for (int sx = -1; sx <= 1; sx += 2) {
            for (int sz = -1; sz <= 1; sz += 2) {
                b.fill(cx + sx * baseHalf - 2, ground, cz + sz * baseHalf - 2,
                        cx + sx * baseHalf + 2, ground, cz + sz * baseHalf + 2, p.foundation());
            }
        }

        // Splayed piers, the arched spandrels between them, and horizontal lattice girders.
        for (int y = ground + 1; y <= y1; y++) {
            int dy = y - ground;
            int half = baseHalf - (baseHalf - 6) * dy / 32;   // 16 -> 6 as the legs lean in
            int lr = Math.max(1, legR - dy / 14);             // pier thins 5x5 -> 3x3
            for (int sx = -1; sx <= 1; sx += 2) {
                for (int sz = -1; sz <= 1; sz += 2) {
                    int lx = cx + sx * half;
                    int lz = cz + sz * half;
                    for (int dx = -lr; dx <= lr; dx++) {
                        for (int dz = -lr; dz <= lr; dz++) {
                            boolean edge = dx == -lr || dx == lr || dz == -lr || dz == lr;
                            if (edge) {
                                b.put(lx + dx, y, lz + dz,
                                        ((dx + dz + dy) & 1) == 0 ? p.accent() : p.wall());
                            }
                        }
                    }
                }
            }
            // The signature arch: the opening between piers shrinks on a quarter-circle.
            if (dy <= 15) {
                int open = (int) Math.round(13.0 * Math.sqrt(Math.max(0.0,
                        1.0 - (double) (dy * dy) / (15.0 * 15.0))));
                int edge = half + lr;
                if (open < edge) {
                    b.fill(cx - edge, y, cz + half, cx - open, y, cz + half, p.accent());
                    b.fill(cx + open, y, cz + half, cx + edge, y, cz + half, p.accent());
                    b.fill(cx - edge, y, cz - half, cx - open, y, cz - half, p.accent());
                    b.fill(cx + open, y, cz - half, cx + edge, y, cz - half, p.accent());
                    b.fill(cx + half, y, cz - edge, cx + half, y, cz - open, p.accent());
                    b.fill(cx + half, y, cz + open, cx + half, y, cz + edge, p.accent());
                    b.fill(cx - half, y, cz - edge, cx - half, y, cz - open, p.accent());
                    b.fill(cx - half, y, cz + open, cx - half, y, cz + edge, p.accent());
                }
            }
            // Lattice girders bracing the piers.
            if (dy % 6 == 0 || dy == 16) {
                b.walls(cx - half - lr, y, cz - half - lr,
                        cx + half + lr, y, cz + half + lr, p.wall());
            }
        }

        // First platform: slab deck with a two-block iron railing.
        b.fill(cx - 11, y1, cz - 11, cx + 11, y1, cz + 11, p.foundation());
        b.walls(cx - 11, y1 + 1, cz - 11, cx + 11, y1 + 1, cz + 11, p.rail());
        b.walls(cx - 11, y1 + 2, cz - 11, cx + 11, y1 + 2, cz + 11, p.rail());

        // Second stage: taper 6 -> 3.
        for (int y = y1 + 3; y <= y2; y++) {
            int t = y - (y1 + 3);
            int span = Math.max(1, y2 - (y1 + 3));
            int half = 6 - (6 - 3) * t / span;
            b.walls(cx - half, y, cz - half, cx + half, y, cz + half, p.accent());
            if ((y - y1) % 6 == 0) {
                b.walls(cx - half - 1, y, cz - half - 1,
                        cx + half + 1, y, cz + half + 1, p.wall());
            }
        }

        // Second platform.
        b.fill(cx - 5, y2, cz - 5, cx + 5, y2, cz + 5, p.foundation());
        b.walls(cx - 5, y2 + 1, cz - 5, cx + 5, y2 + 1, cz + 5, p.rail());
        b.walls(cx - 5, y2 + 2, cz - 5, cx + 5, y2 + 2, cz + 5, p.rail());

        // Upper shaft: taper 3 -> 1.
        for (int y = y2 + 3; y <= y3; y++) {
            int t = y - (y2 + 3);
            int span = Math.max(1, y3 - (y2 + 3));
            int half = 3 - (3 - 1) * t / span;
            b.walls(cx - half, y, cz - half, cx + half, y, cz + half, p.accent());
        }

        // Top observation deck and the beacon spire.
        b.fill(cx - 4, y3, cz - 4, cx + 4, y3, cz + 4, p.foundation());
        b.walls(cx - 4, y3 + 1, cz - 4, cx + 4, y3 + 1, cz + 4, p.rail());
        b.walls(cx - 3, y3 + 2, cz - 3, cx + 3, y3 + 2, cz + 3, p.rail());
        b.spire(cx, cz, y3 + 3, 11);
    }

    /** A flat-topped, crenellated belfry tower — the mirrored partner of the cathedral's tower. */
    private static void twinTower(StructureBuilder b, int cx, int zA, int zB, int ground, Palette p) {
        int tx0 = cx - 4;
        int tx1 = cx + 4;
        int z0 = Math.min(zA, zB);
        int z1 = Math.max(zA, zB);
        int y0 = ground + 1;
        int top = y0 + 26;
        b.room(tx0, y0, z0, tx1, top, z1, new Doorway(Side.N, 4));
        for (int y = y0; y <= top; y++) {
            b.put(tx0, y, z0, p.accent());
            b.put(tx1, y, z0, p.accent());
            b.put(tx0, y, z1, p.accent());
            b.put(tx1, y, z1, p.accent());
        }
        for (int y = y0 + 5; y <= top - 4; y += 5) {
            b.window(tx0, y, (z0 + z1) / 2, 3, 1, true);
            b.window(tx1, y, (z0 + z1) / 2, 3, 1, true);
            b.window((tx0 + tx1) / 2, y, z0, 3, 1, true);
        }
        b.crenellations(tx0 - 1, z0 - 1, tx1 + 1, z1 + 1, top + 1);
    }

    /** A stepped flyer arching from the clerestory out to a free-standing pier, both flanks. */
    private static void flyingButtresses(StructureBuilder b, int x0, int x1, int z0, int z1,
                                         int y0, Palette p) {
        for (int z = z0 + 2; z <= z1 - 2; z += 4) {
            for (int i = 0; i <= 3; i++) {
                b.put(x0 - i, y0 + 9 - i, z, p.accent());
                b.put(x1 + i, y0 + 9 - i, z, p.accent());
            }
            b.fill(x0 - 3, y0, z, x0 - 3, y0 + 6, z, p.foundation());
            b.fill(x1 + 3, y0, z, x1 + 3, y0 + 6, z, p.foundation());
        }
    }

    @Override
    public void flourish(StructureBuilder b, RandomSource rng, int x, int y0, int z,
                         int x1, int y1, int z1, Palette p) {
        // The signature of the style: a continuous wrought-iron balcony wrapping one floor.
        int by = y0 + 3;
        if (by < y1) {
            for (int fx = x; fx <= x1; fx++) {
                b.put(fx, by, z - 1, p.rail());
                b.put(fx, by, z1 + 1, p.rail());
            }
            for (int fz = z; fz <= z1; fz++) {
                b.put(x - 1, by, fz, p.rail());
                b.put(x1 + 1, by, fz, p.rail());
            }
        }
        // A small zinc dormer breaking the mansard on the front facade.
        int dx = x + 1 + rng.nextInt(Math.max(1, x1 - x - 1));
        b.put(dx, y1 + 1, z - 1, p.frame());
        b.put(dx, y1 + 2, z - 1, p.window());
    }

    @Override
    public void streetProps(StructureBuilder b, RandomSource rng, int cx, int cz,
                            int district, int ground, Palette p) {
        // Gaslit lamp posts along the two grand axes (les boulevards).
        for (int d = 24; d <= district - 10; d += 12) {
            StyleKit.lanternPost(b, cx + d, cz + 3, ground, p);
            StyleKit.lanternPost(b, cx - d, cz - 3, ground, p);
            StyleKit.lanternPost(b, cx + 3, cz + d, ground, p);
            StyleKit.lanternPost(b, cx - 3, cz - d, ground, p);
        }
        // A plaza obelisk (Concorde) and a statue on the cross-axes.
        StyleKit.obelisk(b, cx + district / 2, cz, ground, p);
        StyleKit.statue(b, cx - district / 2, cz, ground, p);
    }
}
