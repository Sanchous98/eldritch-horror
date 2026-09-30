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
                coastal ? Blocks.KELP.defaultBlockState() : bio.overgrowth(),
                Blocks.GRAVEL.defaultBlockState());                 // rubble
    }

    @Override
    public void landmark(StructureBuilder b, RandomSource rng, int cx, int cz, int ground, Palette p) {
        // Cathedral nave + north tower + spire (x0=cx-5..cx+5, z0=cz-10..cz+10, y0=ground+1).
        StyleKit.cathedral(b, rng, cx, cz, ground, p);
        int x0 = cx - 5;
        int x1 = cx + 5;
        int z0 = cz - 10;
        int z1 = cz + 10;
        int y0 = ground + 1;
        // Mirror the north belfry at the south front for the twin-tower silhouette.
        twinTower(b, cx, z1 + 1, z1 + 6, ground, p);
        // Flying buttresses down both flanks of the nave.
        flyingButtresses(b, x0, x1, z0, z1, y0, p);
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
