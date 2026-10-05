package com.sanchous98.eldritchhorror.world.loc.style;

import com.sanchous98.eldritchhorror.world.loc.Palette;
import com.sanchous98.eldritchhorror.world.loc.StructureBuilder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Rio de Janeiro — Portuguese-colonial and modern carioca local colour: whitewashed and pastel
 * plaster, terracotta tile roofs, black-and-white mosaic pavements, tropical palms, mountains and
 * beaches.
 *
 * <p>Landmark: a stepped stone pedestal carrying an outstretched art-deco figure (Cristo
 * Redentor) on the peak. Street props: a Copacabana-style wave mosaic plaza, palm posts, statues
 * and lamp posts. Culture leads; the coast only swaps the overgrowth for kelp.
 *
 * <p>Deterministic and cheap: only {@link StructureBuilder} + {@link Palette} blocks (and
 * {@link StyleKit}/{@link Materials}); all randomness comes from the passed {@link RandomSource}.
 */
public final class RioStyle implements CityStyle {

    @Override
    public String id() {
        return "rio";
    }

    @Override
    public Palette palette(int koppenClass, boolean coastal) {
        return new Palette(
                Blocks.SMOOTH_QUARTZ.defaultBlockState(),           // ground: pale mosaic pavement
                Blocks.STONE_BRICKS.defaultBlockState(),            // foundation
                Materials.whiteConcrete(),                          // wall: whitewashed plaster
                Materials.lightGrayConcrete(),                      // weathered: aged whitewash
                Blocks.SMOOTH_QUARTZ.defaultBlockState(),           // accent: pale stone trim
                Blocks.TERRACOTTA.defaultBlockState(),              // roof: terracotta tile
                Blocks.BRICK_STAIRS.defaultBlockState(),            // roof stairs
                Blocks.BRICK_SLAB.defaultBlockState(),              // roof slabs / eaves
                Blocks.GLASS_PANE.defaultBlockState(),              // window
                Materials.concrete(DyeColor.LIGHT_BLUE),            // frame: azulejo blue
                Blocks.OAK_DOOR.defaultBlockState(),                // door
                Blocks.OAK_FENCE.defaultBlockState(),               // rail
                Blocks.LANTERN.defaultBlockState(),                 // light
                Blocks.JUNGLE_LEAVES.defaultBlockState(),           // overgrowth (never kelp: waterlogged)
                Blocks.GRAVEL.defaultBlockState());                 // rubble
    }

    @Override
    public void landmark(StructureBuilder b, RandomSource rng, int cx, int cz, int ground, Palette p) {
        int by = ground + 1;

        // Mountain-top plaza paved in pale stone with a running dark-tile band.
        b.ground(cx - 24, cz - 24, cx + 24, cz + 24, ground, ground, p.ground());
        b.ground(cx - 20, cz - 20, cx + 20, cz + 20, ground, ground, p.foundation());
        for (int i = -24; i <= 24; i++) {
            if (Math.floorMod(i, 4) == 0) {
                b.put(cx + i, ground, cz - 24, p.frame());
                b.put(cx + i, ground, cz + 24, p.frame());
                b.put(cx - 24, ground, cz + i, p.frame());
                b.put(cx + 24, ground, cz + i, p.frame());
            }
        }

        // Stepped stone pedestal rising to a tall tower.
        b.fill(cx - 8, by, cz - 8, cx + 8, by + 1, cz + 8, p.foundation());
        b.fill(cx - 6, by + 2, cz - 6, cx + 6, by + 3, cz + 6, p.wall());
        b.fill(cx - 5, by + 4, cz - 5, cx + 5, by + 5, cz + 5, p.accent());
        for (int y = by + 6; y <= by + 32; y++) {
            b.fill(cx - 2, y, cz - 2, cx + 2, y, cz + 2, p.wall());
            b.put(cx - 2, y, cz - 2, p.accent());
            b.put(cx + 2, y, cz + 2, p.accent());
        }
        b.fill(cx - 3, by + 33, cz - 3, cx + 3, by + 33, cz + 3, p.accent());

        // The figure: a robed body flaring at the feet.
        int fb = by + 34;
        b.fill(cx - 4, fb, cz - 3, cx + 4, fb, cz + 3, p.accent());
        b.fill(cx - 3, fb + 1, cz - 3, cx + 3, fb + 1, cz + 3, p.accent());
        b.fill(cx - 3, fb + 2, cz - 2, cx + 3, fb + 2, cz + 2, p.accent());
        b.fill(cx - 2, fb + 3, cz - 2, cx + 2, fb + 23, cz + 2, p.accent());
        int sy = fb + 24;
        b.fill(cx - 3, sy - 1, cz - 2, cx + 3, sy, cz + 2, p.accent()); // shoulders

        // Outstretched arms — the widest part of the silhouette (a T).
        int arm = 23;
        for (int d = 4; d <= arm; d++) {
            int drop = d >= arm - 3 ? 1 : 0;
            b.put(cx - d, sy - drop, cz, p.accent());
            b.put(cx + d, sy - drop, cz, p.accent());
            b.put(cx - d, sy - 1 - drop, cz, p.accent());
            b.put(cx + d, sy - 1 - drop, cz, p.accent());
        }
        b.put(cx - arm, sy - 2, cz, p.accent());
        b.put(cx + arm, sy - 2, cz, p.accent());

        // Head, crowned with a single beacon (a halo ring here floated unsupported in the sky).
        b.fill(cx - 1, sy + 2, cz - 1, cx + 1, sy + 3, cz + 1, p.accent());
        b.put(cx, sy + 4, cz, p.light());
    }

    @Override
    public void flourish(StructureBuilder b, RandomSource rng, int x, int y0, int z,
                         int x1, int y1, int z1, Palette p) {
        if (x1 - x < 2) {
            return;
        }
        // An azulejo-tiled cornice band along the front (minimum-z) face.
        b.fill(x, y1, z, x1, y1, z, p.frame());
    }

    @Override
    public void streetProps(StructureBuilder b, RandomSource rng, int cx, int cz,
                            int district, int ground, Palette p) {
        mosaicPlaza(b, cx, cz, ground, p);
        // Palm posts and lamp posts down the main axes.
        for (int d = 24; d <= district - 10; d += 16) {
            palmPost(b, cx + d, cz + 3, ground, p);
            palmPost(b, cx - d, cz - 3, ground, p);
            StyleKit.lanternPost(b, cx + 3, cz + d, ground, p);
            StyleKit.lanternPost(b, cx - 3, cz - d, ground, p);
        }
        // Statues on the north / south approaches to the plaza.
        int off = Math.max(20, district - 6);
        StyleKit.statue(b, cx, cz - off, ground, p);
        StyleKit.statue(b, cx, cz + off, ground, p);
    }

    // ------------------------------------------------------------------ local helpers



    /** A Copacabana-style wave mosaic on the plaza: pale stone with running dark tiles. */
    private static void mosaicPlaza(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int r = 7;
        BlockState dark = Materials.concrete(DyeColor.BLACK);
        BlockState blue = Materials.concrete(DyeColor.LIGHT_BLUE);
        for (int x = -r; x <= r; x++) {
            for (int z = -r; z <= r; z++) {
                int band = Math.floorMod(x + z, 4);
                BlockState tile = band == 0 ? dark : (band == 2 ? blue : p.ground());
                b.put(cx + x, ground, cz + z, tile);
            }
        }
        // An azulejo-blue border framing the plaza.
        for (int i = -r; i <= r; i++) {
            b.put(cx + i, ground, cz - r, blue);
            b.put(cx + i, ground, cz + r, blue);
            b.put(cx - r, ground, cz + i, blue);
            b.put(cx + r, ground, cz + i, blue);
        }
    }

    /** A palm post: a fence trunk crowned with a splay of tropical leaves. */
    private static void palmPost(StructureBuilder b, int x, int z, int ground, Palette p) {
        int h = 5;
        for (int i = 1; i <= h; i++) {
            b.put(x, ground + i, z, p.rail());
        }
        BlockState frond = p.overgrowth() != null ? p.overgrowth() : p.frame();
        b.fill(x - 1, ground + h + 1, z - 1, x + 1, ground + h + 1, z + 1, frond);
        b.put(x, ground + h + 2, z, frond);
    }
}
