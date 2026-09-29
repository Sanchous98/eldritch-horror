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
 * Lima — cultural city style. Peruvian colonial over Andean adobe: warm ochre/yellow walls,
 * carved dark-timber balconies (the famous <b>miradores</b>), red clay-tile roofs, adobe and a
 * cold coastal fog that leaves the walls damp and lightly overgrown.
 *
 * <p>Landmark: a colonial cathedral — a buttressed nave with a dome and twin bell towers, the
 * Andean/Peruvian silhouette over the plaza. Street props: lantern posts, statues and enclosed
 * wooden balconies (miradores) at the corners. Flourish: a protruding lattice balcony box on
 * most facades.
 *
 * <p>Culture leads; climate only varies the damp overgrowth (kelp on the coast). See
 * {@code docs/STRUCTURES-CONTRACT.md} § Cultural styles.
 */
public final class LimaStyle implements CityStyle {

    @Override
    public String id() {
        return "lima";
    }

    @Override
    public Palette palette(int koppenClass, boolean coastal) {
        Palette bio = Palette.fromBiome(koppenClass, coastal);
        return new Palette(
                Blocks.SMOOTH_SANDSTONE.defaultBlockState(),        // ground: pale coastal paving
                Blocks.CHISELED_SANDSTONE.defaultBlockState(),      // foundation: dressed base course
                Materials.concrete(DyeColor.YELLOW),                // wall: warm ochre
                Materials.terracotta(DyeColor.ORANGE),              // weathered adobe
                Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState(),   // accent: carved dark timber
                Materials.terracotta(DyeColor.RED),                // roof: red clay tile
                Blocks.BRICK_STAIRS.defaultBlockState(),            // roof stairs
                Blocks.BRICK_SLAB.defaultBlockState(),              // roof slabs / eaves
                Blocks.GLASS_PANE.defaultBlockState(),              // window
                Blocks.DARK_OAK_TRAPDOOR.defaultBlockState(),       // frame: lattice / mullions
                Blocks.DARK_OAK_DOOR.defaultBlockState(),           // door
                Blocks.DARK_OAK_FENCE.defaultBlockState(),          // rail
                Blocks.LANTERN.defaultBlockState(),                 // light: lantern
                coastal ? Blocks.KELP.defaultBlockState() : bio.overgrowth(),
                Blocks.COARSE_DIRT.defaultBlockState());            // rubble: dry coastal dirt
    }

    @Override
    public void landmark(StructureBuilder b, RandomSource rng, int cx, int cz, int ground, Palette p) {
        int w = 15;
        int d = 13;
        int h = 11;
        int x0 = cx - w / 2;
        int x1 = x0 + w - 1;
        int z0 = cz - d / 2;
        int z1 = z0 + d - 1;
        int y0 = ground + 1;
        int y1 = y0 + h;

        b.ground(x0 - 2, z0 - 2, x1 + 2, z1 + 2, ground, ground, p.foundation());
        b.room(x0, y0, z0, x1, y1, z1, new Doorway(Side.S, 7));

        // Andean-quake buttresses down the flanks, with tall dark-window slits between.
        for (int z = z0 + 2; z <= z1 - 2; z += 4) {
            b.buttress(x0, z, y0, 5, Side.W);
            b.buttress(x1, z, y0, 5, Side.E);
        }
        for (int z = z0 + 3; z <= z1 - 3; z += 3) {
            b.window(x0, y0 + 3, z, 4, 1, true);
            b.window(x1, y0 + 3, z, 4, 1, true);
        }

        // The crossing dome over the nave.
        StyleKit.dome(b, cx, cz, y1 + 2, 6, p);

        // Twin bell towers flanking the west front (the landmark skyline).
        tower(b, x0 + 3, z0 - 5, ground, p);
        tower(b, x1 - 3, z0 - 5, ground, p);

        StyleKit.twoHighDoor(b, p, cx, z1, y0 + 1, Direction.SOUTH);
    }

    /** A colonial bell tower: ochre shaft, dark quoined corners, arched belfry and a tiled cap. */
    private static void tower(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int r = 2;
        int top = ground + 17;
        b.ground(cx - r - 1, cz - r - 1, cx + r + 1, cz + r + 1, ground, ground, p.foundation());
        b.room(cx - r, ground + 1, cz - r, cx + r, top, cz + r);
        for (int y = ground + 1; y <= top; y++) {
            b.put(cx - r, y, cz - r, p.accent());
            b.put(cx + r, y, cz - r, p.accent());
            b.put(cx - r, y, cz + r, p.accent());
            b.put(cx + r, y, cz + r, p.accent());
        }
        // Open belfry openings near the top.
        b.window(cx, top - 3, cz - r, 2, 2, true);
        b.window(cx, top - 3, cz + r, 2, 2, true);
        b.window(cx - r, top - 3, cz, 2, 2, true);
        b.window(cx + r, top - 3, cz, 2, 2, true);
        b.pitchedRoof(cx - r - 1, cz - r - 1, cx + r + 1, cz + r + 1, top, 3, 0);
        b.put(cx, top + 4, cz, p.light());
    }

    @Override
    public void flourish(StructureBuilder b, RandomSource rng, int x, int y0, int z,
                         int x1, int y1, int z1, Palette p) {
        if (x1 - x < 4 || y1 - y0 < 4) {
            return; // too small for a mirador
        }
        int by = y0 + 2;
        // An enclosed wooden balcony box projecting from the south (street) facade: dark
        // lattice trapdoors with a carved-timber rail and a red-tile eave.
        for (int xx = x; xx <= x1; xx++) {
            b.put(xx, by, z1 + 1, p.frame());
            b.put(xx, by + 1, z1 + 1, p.window());
            b.put(xx, by + 2, z1 + 1, p.frame());
        }
        b.fill(x, by + 3, z1, x1, by + 3, z1 + 1, p.roofSlab());
        int cornerX = rng.nextBoolean() ? x : x1;
        b.put(cornerX, by + 4, z1 + 1, p.light());
    }

    @Override
    public void streetProps(StructureBuilder b, RandomSource rng, int cx, int cz,
                            int district, int ground, Palette p) {
        // Lantern posts down the four approaches (fog-lit colonial streets).
        for (int d = 26; d <= district - 12; d += 14) {
            StyleKit.lanternPost(b, cx + d, cz + 3, ground, p);
            StyleKit.lanternPost(b, cx - d, cz - 3, ground, p);
        }
        // Enclosed mirador boxes sitting out over the plaza corners.
        mirador(b, cx + district / 2, cz + district / 2, ground, p);
        mirador(b, cx - district / 2, cz - district / 2, ground, p);
        // Plaza statues.
        StyleKit.statue(b, cx + 12, cz - 8, ground, p);
        StyleKit.statue(b, cx - 12, cz + 8, ground, p);
    }

    /** A freestanding enclosed wooden balcony (mirador): a dark lattice box with a timber eaves. */
    private static void mirador(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int r = 2;
        int by = ground + 3;
        b.fill(cx - r, by, cz - r, cx + r, by + 2, cz + r, p.frame());
        b.fill(cx - r, by + 3, cz - r, cx + r, by + 3, cz + r, p.roofSlab());
        // Punch the lattice: leave the corners post-like and glaze the middle band.
        for (int i = -r; i <= r; i++) {
            b.put(cx + i, by + 1, cz - r, p.window());
            b.put(cx + i, by + 1, cz + r, p.window());
            b.put(cx - r, by + 1, cz + i, p.window());
            b.put(cx + r, by + 1, cz + i, p.window());
        }
        b.put(cx, by + 4, cz, p.light());
    }
}
