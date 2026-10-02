package com.sanchous98.eldritchhorror.world.loc.style;

import com.sanchous98.eldritchhorror.world.loc.Palette;
import com.sanchous98.eldritchhorror.world.loc.StructureBuilder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Lagos — Yoruba / West African with a modern overlay: ochre earth and mudbrick compound walls,
 * rusty corrugated-metal roofs, and bright painted trim (the signature green/yellow band over a
 * doorway). Culture leads; climate only supplies the overgrowth (jungle creepers, or kelp on the
 * lagoon).
 *
 * <p>Landmark: a walled <b>courtyard palace</b> — a colonnaded hall under a pitched metal roof,
 * ringed by a low compound wall and entered through an ornate painted gate. Street props: market
 * stalls with striped awnings, lantern posts and guardian statues. Follows {@link TokyoStyle};
 * shared shapes live in {@link StyleKit}.
 */
public final class LagosStyle implements CityStyle {

    /** The bright house-paint palette daubed on pillars, gates and trim. */
    private static final DyeColor[] PAINT = {
            DyeColor.GREEN, DyeColor.YELLOW, DyeColor.RED, DyeColor.CYAN, DyeColor.ORANGE
    };

    /** Market awning canvas. */
    private static final DyeColor[] AWNING = {
            DyeColor.RED, DyeColor.YELLOW, DyeColor.ORANGE, DyeColor.LIME, DyeColor.CYAN
    };

    /** A rotation of the painted-trim colours, deterministic in {@code i}. */
    private static BlockState paint(int i) {
        return Materials.concrete(PAINT[Math.floorMod(i, PAINT.length)]);
    }

    @Override
    public String id() {
        return "lagos";
    }

    @Override
    public Palette palette(int koppenClass, boolean coastal) {
        // Culture leads: ochre earth, mudbrick and painted trim, whatever the Köppen class.
        Palette bio = Palette.fromBiome(koppenClass, coastal);
        return new Palette(
                Blocks.PACKED_MUD.defaultBlockState(),               // ground: beaten earth street
                Blocks.TERRACOTTA.defaultBlockState(),               // foundation: fired-clay footing
                Blocks.MUD_BRICKS.defaultBlockState(),               // wall: ochre mudbrick
                Materials.concrete(DyeColor.LIGHT_GRAY),             // weathered: faded plaster patch
                Materials.concrete(DyeColor.GREEN),                  // accent: bright painted trim
                Materials.terracotta(DyeColor.BROWN),                // roof: rusty corrugated sheet
                Blocks.BRICK_STAIRS.defaultBlockState(),             // roof stairs (folded sheet)
                Blocks.BRICK_SLAB.defaultBlockState(),               // roof slabs / eaves
                Materials.stainedPane(DyeColor.LIGHT_BLUE),          // window: tinted glazing
                Blocks.ACACIA_TRAPDOOR.defaultBlockState(),          // frame: carved timber louvre
                Blocks.DARK_OAK_DOOR.defaultBlockState(),            // door: heavy carved gate leaf
                Blocks.ACACIA_FENCE.defaultBlockState(),             // rail
                Blocks.LANTERN.defaultBlockState(),                  // light: hanging lantern
                bio.overgrowth(),
                Blocks.COARSE_DIRT.defaultBlockState());             // rubble: dry earth
    }

    @Override
    public void landmark(StructureBuilder b, RandomSource rng, int cx, int cz, int ground, Palette p) {
        // National Theatre Lagos: a broad octagonal drum on a tall painted colonnade, carrying a
        // wide, shallow shell roof. The shell is built as shrinking elliptical rings whose Z radius
        // collapses first, so the cap folds into a long X-aligned crest. A tall central drum and a
        // crest/mast then rise ~85 blocks, so the theatre dominates the skyline instead of reading
        // as a low blob.
        int y0 = ground + 1;

        // --- podium: a broad paved apron around the theatre -------------------
        b.ground(cx - 31, cz - 26, cx + 31, cz + 28, ground, ground, p.foundation());
        b.ground(cx - 25, cz - 20, cx + 25, cz + 22, ground, ground, p.ground());

        // --- the drum: an elongated octagonal auditorium ----------------------
        int ax = 17;
        int az = 12;
        int wallTop = ground + 32;
        int gw = 2 * ax + 1;
        int gd = 2 * az + 1;
        boolean[][] in = new boolean[gw][gd];
        for (int ix = 0; ix < gw; ix++) {
            for (int iz = 0; iz < gd; iz++) {
                int dx = Math.abs(ix - ax);
                int dz = Math.abs(iz - az);
                in[ix][iz] = dx <= ax && dz <= az && dx + dz <= ax + az - 4;
            }
        }
        for (int ix = 0; ix < gw; ix++) {
            for (int iz = 0; iz < gd; iz++) {
                if (!in[ix][iz]) {
                    continue;
                }
                int x = cx - ax + ix;
                int z = cz - az + iz;
                boolean per = (ix == 0 || !in[ix - 1][iz]) || (ix == gw - 1 || !in[ix + 1][iz])
                        || (iz == 0 || !in[ix][iz - 1]) || (iz == gd - 1 || !in[ix][iz + 1]);
                if (!per) {
                    continue;
                }
                for (int y = y0; y <= wallTop; y++) {
                    b.put(x, y, z, Math.floorMod(ix + iz, 6) == 0 ? paint(x + z) : p.wall());
                }
            }
        }

        // Tall glazing bands around the cardinal faces of the drum.
        for (int x = cx - 6; x <= cx + 6; x += 3) {
            b.window(x, y0 + 9, cz + az, 5, 1, true);
            b.window(x, y0 + 20, cz + az, 5, 1, true);
            b.window(x, y0 + 9, cz - az, 5, 1, true);
            b.window(x, y0 + 20, cz - az, 5, 1, true);
        }
        for (int z = cz - 6; z <= cz + 6; z += 3) {
            b.window(cx - ax, y0 + 9, z, 5, 1, true);
            b.window(cx - ax, y0 + 20, z, 5, 1, true);
            b.window(cx + ax, y0 + 9, z, 5, 1, true);
            b.window(cx + ax, y0 + 20, z, 5, 1, true);
        }

        // --- entrance: a bright painted portal on the south face --------------
        int ez = cz + az;
        b.fill(cx - 3, y0, ez, cx - 3, y0 + 7, ez, p.accent());
        b.fill(cx + 3, y0, ez, cx + 3, y0 + 7, ez, p.accent());
        b.fill(cx - 3, y0 + 8, ez, cx + 3, y0 + 8, ez, paint(1));
        b.put(cx, y0 + 9, ez, p.light());
        b.put(cx - 1, y0, ez, p.door());
        b.put(cx + 1, y0, ez, p.door());
        b.fill(cx, y0, ez, cx, y0 + 1, ez, p.window());

        // --- the colonnade: a tall ring of painted columns under the shell -----
        int ax2 = ax + 4;
        int az2 = az + 4;
        int colTop = ground + 30;
        int cw = 2 * ax2 + 1;
        int cd = 2 * az2 + 1;
        boolean[][] out = new boolean[cw][cd];
        for (int ix = 0; ix < cw; ix++) {
            for (int iz = 0; iz < cd; iz++) {
                int dx = Math.abs(ix - ax2);
                int dz = Math.abs(iz - az2);
                out[ix][iz] = dx <= ax2 && dz <= az2 && dx + dz <= ax2 + az2 - 5;
            }
        }
        for (int ix = 0; ix < cw; ix++) {
            for (int iz = 0; iz < cd; iz++) {
                if (!out[ix][iz]) {
                    continue;
                }
                boolean per = (ix == 0 || !out[ix - 1][iz]) || (ix == cw - 1 || !out[ix + 1][iz])
                        || (iz == 0 || !out[ix][iz - 1]) || (iz == cd - 1 || !out[ix][iz + 1]);
                if (!per || Math.floorMod(ix + iz, 4) != 0) {
                    continue;
                }
                int x = cx - ax2 + ix;
                int z = cz - az2 + iz;
                for (int y = y0; y <= colTop; y++) {
                    b.put(x, y, z, Math.floorMod(y - y0, 5) == 0 ? p.accent() : p.wall());
                }
                b.put(x, colTop + 1, z, p.roofSlab());
            }
        }

        // --- the shell roof: a broad, shallow folded canopy -------------------
        int shellBase = wallTop + 1;
        int sx = ax + 7;
        int sz = az + 7;
        int layers = 16;
        for (int i = 0; i < layers; i++) {
            int y = shellBase + i;
            double t = (double) i / (layers - 1);
            double kx = Math.cos(t * Math.PI / 2.0);
            double kz = Math.cos(Math.min(1.0, t * 1.5) * Math.PI / 2.0);
            int rx = (int) Math.round(sx * kx);
            int rz = (int) Math.round(sz * kz);
            for (int x = cx - rx; x <= cx + rx; x++) {
                for (int z = cz - rz; z <= cz + rz; z++) {
                    boolean inside;
                    if (rx <= 0 || rz <= 0) {
                        inside = true;
                    } else {
                        double e = ((double) (x - cx) * (x - cx)) / ((double) rx * rx)
                                + ((double) (z - cz) * (z - cz)) / ((double) rz * rz);
                        inside = e <= 1.0001;
                    }
                    if (!inside) {
                        continue;
                    }
                    b.put(x, y, z, Math.floorMod(x - cx, 4) == 0
                            ? p.accent() : Materials.lightGrayConcrete());
                }
            }
        }
        b.put(cx, shellBase + layers, cz, p.light());

        // --- tall central drum: a slender tower rising from the shell ---------
        // The drum reads as the theatre's fly tower / lantern, carrying the composition from the
        // broad low shell (~48) up to the mast tip (~85) so it dominates the city skyline.
        int drumBase = ground + 30;
        int r = 4;
        for (int y = drumBase; y <= ground + 64; y++) {
            for (int x = cx - r; x <= cx + r; x++) {
                for (int z = cz - r; z <= cz + r; z++) {
                    boolean edge = x == cx - r || x == cx + r || z == cz - r || z == cz + r;
                    if (!edge) {
                        continue;
                    }
                    b.put(x, y, z, ((x + z + y) & 1) == 0 ? p.accent() : p.wall());
                }
            }
            // Painted glazing bands every few courses.
            if (Math.floorMod(y - drumBase, 6) == 0) {
                b.window(cx - r, y, cz, 3, 1, true);
                b.window(cx + r, y, cz, 3, 1, true);
                b.window(cx, y, cz - r, 3, 1, true);
                b.window(cx, y, cz + r, 3, 1, true);
            }
        }
        // Painted cornice and crenellated parapet around the drum head.
        b.walls(cx - r - 1, ground + 65, cz - r - 1, cx + r + 1, ground + 65, cz + r + 1, p.roofSlab());
        b.crenellations(cx - r - 1, cz - r - 1, cx + r + 1, cz + r + 1, ground + 66);

        // A small capped roof on the drum, then the mast spire to ~85 blocks.
        b.pitchedRoof(cx - 6, cz - 6, cx + 6, cz + 6, ground + 68, 5, 0);
        b.spire(cx, cz, ground + 74, 8);

        // --- tall crest / mast on the shell ridge (X-aligned) ------------------
        // Two thin vanes at the ends of the long shell ridge, joined by a bright crest, so the
        // whole shell reads as a single tall feature rather than a flat cap.
        int crestTop = ground + 44;
        for (int dx : new int[]{-22, 22}) {
            for (int y = ground + 36; y <= crestTop; y++) {
                b.put(cx + dx, y, cz, paint(y));
            }
        }
        b.fill(cx - 22, crestTop + 1, cz, cx + 22, crestTop + 1, cz, Materials.glazed(DyeColor.YELLOW));
        b.fill(cx - 20, crestTop + 2, cz, cx + 20, crestTop + 2, cz, p.roof());
        b.fill(cx - 14, crestTop + 3, cz, cx + 14, crestTop + 3, cz, p.roofSlab());
        for (int x = cx - 18; x <= cx + 18; x += 6) {
            b.put(x, crestTop + 4, cz, p.light());
        }
    }





    @Override
    public void flourish(StructureBuilder b, RandomSource rng, int x, int y0, int z,
                         int x1, int y1, int z1, Palette p) {
        // The signature paint band under the eaves.
        b.fill(x, y1, z, x1, y1, z1, paint(rng.nextInt(PAINT.length)));
        // A corrugated awning over some facades, on short timber brackets.
        if (rng.nextFloat() < 0.5F) {
            b.fill(x - 1, y0 + 2, z - 1, x1 + 1, y0 + 2, z - 1, p.roof());
            for (int xx = x; xx <= x1; xx += 3) {
                b.put(xx, y0 + 1, z - 1, p.frame());
            }
        }
    }

    @Override
    public void streetProps(StructureBuilder b, RandomSource rng, int cx, int cz,
                            int district, int ground, Palette p) {
        // Guardian statues flanking the palace approach.
        StyleKit.statue(b, cx - 9, cz + district - 30, ground, p);
        StyleKit.statue(b, cx + 9, cz + district - 30, ground, p);
        // Lantern posts down the two main axes, spaced deterministically.
        int step = 12 + rng.nextInt(3);
        for (int d = 26; d <= district - 12; d += step) {
            StyleKit.lanternPost(b, cx + d, cz - 3, ground, p);
            StyleKit.lanternPost(b, cx - d, cz + 3, ground, p);
        }
        // Market stalls clustered off the main street.
        for (int i = 0; i < 6; i++) {
            int sx = cx + rng.nextInt(district) - district / 2;
            int sz = cz + district / 2 - rng.nextInt(12);
            marketStall(b, sx, sz, ground, p, rng);
        }
    }

    /** A small market stall: a raised counter under a striped awning with a hanging light. */
    private static void marketStall(StructureBuilder b, int x, int z, int ground, Palette p, RandomSource rng) {
        int y = ground + 1;
        b.fill(x, y, z, x + 1, y, z + 1, p.foundation());          // counter
        for (int dx = 0; dx <= 1; dx++) {
            for (int dz = 0; dz <= 1; dz++) {
                b.put(x + dx, y + 1, z + dz, p.accent());          // corner posts
                b.put(x + dx, y + 2, z + dz, p.accent());
            }
        }
        b.fill(x - 1, y + 3, z - 1, x + 2, y + 3, z + 2, Materials.wool(AWNING[rng.nextInt(AWNING.length)]));
        b.put(x, y + 2, z, p.light());                             // a lamp under the canvas
    }
}
