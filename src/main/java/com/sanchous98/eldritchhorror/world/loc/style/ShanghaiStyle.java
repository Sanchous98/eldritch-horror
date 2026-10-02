package com.sanchous98.eldritchhorror.world.loc.style;

import com.sanchous98.eldritchhorror.world.loc.Palette;
import com.sanchous98.eldritchhorror.world.loc.StructureBuilder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Shanghai — the treaty-port city. Grey stone and brick waterfront blocks with arched
 * colonnades, iron mullions, and hard Art-Deco silhouettes against a wash of neon.
 *
 * <p>Landmark: a stepped-setback Art-Deco tower crowning a colonnaded stone base.
 * Street props: lamp posts, statues and neon shopfront signs. See
 * {@code docs/STRUCTURES-CONTRACT.md} § Cultural styles; helpers live in {@link StyleKit}.
 */
public final class ShanghaiStyle implements CityStyle {

    /** Neon colours for shopfront signs — magenta/cyan/red read as the Bund at night. */
    private static final DyeColor[] NEON = {
            DyeColor.RED, DyeColor.MAGENTA, DyeColor.CYAN,
            DyeColor.LIGHT_BLUE, DyeColor.YELLOW
    };

    @Override
    public String id() {
        return "shanghai";
    }

    @Override
    public Palette palette(int koppenClass, boolean coastal) {
        // Culture leads: grey treaty-port masonry with dark slate roofs. Climate only nudges the
        // overgrowth; the coast swaps it for kelp (the Huangpu riverfront).
        Palette bio = Palette.fromBiome(koppenClass, coastal);
        BlockState overgrowth = bio.overgrowth();
        return new Palette(
                Blocks.POLISHED_ANDESITE.defaultBlockState(),        // ground: grey stone paving
                Blocks.POLISHED_DIORITE.defaultBlockState(),         // foundation: stone plinth
                Blocks.STONE_BRICKS.defaultBlockState(),             // wall: Bund masonry
                Blocks.CRACKED_STONE_BRICKS.defaultBlockState(),     // weathered
                Blocks.CHISELED_STONE_BRICKS.defaultBlockState(),    // accent: carved columns
                Blocks.DEEPSLATE_TILES.defaultBlockState(),          // roof: dark slate
                Blocks.DEEPSLATE_TILE_STAIRS.defaultBlockState(),    // roof stairs
                Blocks.DEEPSLATE_TILE_SLAB.defaultBlockState(),      // roof slabs
                Materials.stainedPane(DyeColor.LIGHT_BLUE),          // window: river-glass
                Blocks.IRON_BARS.defaultBlockState(),                // frame: iron mullions
                Blocks.IRON_DOOR.defaultBlockState(),                // door
                Blocks.IRON_BARS.defaultBlockState(),                // rail: iron balustrade
                Blocks.SEA_LANTERN.defaultBlockState(),              // light: neon glow
                overgrowth,
                Blocks.GRAVEL.defaultBlockState());                  // rubble
    }

    @Override
    public void landmark(StructureBuilder b, RandomSource rng, int cx, int cz, int ground, Palette p) {
        // Plaza.
        b.ground(cx - 24, cz - 24, cx + 24, cz + 24, ground, ground, p.foundation());

        // Splayed legs carrying the column (a wide, braced foot).
        int[][] legs = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        for (int[] d : legs) {
            for (int i = 0; i <= 13; i++) {
                int y = ground + 1 + i;
                int dist = 12 - i;                 // 12 -> 0, leaning inward as it rises
                int x = cx + d[0] * dist;
                int z = cz + d[1] * dist;
                int ex = x + (d[0] == 0 ? 1 : 0);
                int ez = z + (d[1] == 0 ? 1 : 0);
                b.fill(Math.min(x, ex), y, Math.min(z, ez), Math.max(x, ex), y, Math.max(z, ez),
                        p.foundation());
            }
        }

        // Tapering concrete column.
        int columnTop = ground + 72;
        for (int y = ground + 1; y <= columnTop; y++) {
            int rr = Math.max(1, 5 - (y - ground) / 16);
            b.fill(cx - rr, y, cz - rr, cx + rr, y, cz + rr, p.wall());
            b.put(cx - rr, y, cz, p.accent());
            b.put(cx + rr, y, cz, p.accent());
            b.put(cx, y, cz - rr, p.accent());
            b.put(cx, y, cz + rr, p.accent());
        }

        // Three spheres of different sizes threaded on the column — the Pearl spheres.
        int[][] spheres = {{22, 6}, {40, 9}, {58, 5}};
        for (int[] s : spheres) {
            int cy = ground + s[0];
            int r = s[1];
            for (int dy = -r; dy <= r; dy++) {
                int rr = (int) Math.round(Math.sqrt((double) (r * r - dy * dy)));
                int y = cy + dy;
                b.fill(cx - rr, y, cz - rr, cx + rr, y, cz + rr, p.roof());
                for (int i = -rr; i <= rr; i++) {
                    b.put(cx + i, y, cz - rr, p.accent());
                    b.put(cx + i, y, cz + rr, p.accent());
                    b.put(cx - rr, y, cz + i, p.accent());
                    b.put(cx + rr, y, cz + i, p.accent());
                }
            }
            // Glowing equatorial observation band.
            b.fill(cx - r, cy, cz - r, cx + r, cy, cz + r, p.roof());
            for (int i = -r; i <= r; i++) {
                b.put(cx + i, cy, cz - r, p.light());
                b.put(cx + i, cy, cz + r, p.light());
                b.put(cx - r, cy, cz + i, p.light());
                b.put(cx + r, cy, cz + i, p.light());
            }
        }

        // Spire crowning the tower.
        b.spire(cx, cz, columnTop + 1, 18);
    }

    /** Corner and mid-wall stone piers of the arcade, in the accent colour. */
    private static void colonnade(StructureBuilder b, int cx, int cz, int y0, Palette p) {
        int top = y0 + 9;
        for (int x = cx - 7; x <= cx + 7; x += 7) {
            for (int z = cz - 7; z <= cz + 7; z += 7) {
                for (int y = y0; y <= top; y++) {
                    b.put(x, y, z, p.accent());
                }
            }
        }
    }

    @Override
    public void flourish(StructureBuilder b, RandomSource rng, int x, int y0, int z,
                         int x1, int y1, int z1, Palette p) {
        // Iron ground-floor arcade with a neon shopfront band above it, on the long walls.
        DyeColor c = NEON[rng.nextInt(NEON.length)];
        int ys = Math.min(y1, y0 + 2);
        for (int xx = x + 1; xx < x1; xx += 3) {
            b.put(xx, y0, z, p.accent());
            b.put(xx, y0 + 1, z, Materials.stainedGlass(c));
        }
        b.fill(x, ys, z, x1, ys, z, Materials.stainedGlass(c));
        b.fill(x, ys, z1, x1, ys, z1, Materials.stainedGlass(c));
    }

    @Override
    public void streetProps(StructureBuilder b, RandomSource rng, int cx, int cz,
                            int district, int ground, Palette p) {
        // Statues flanking the riverfront plaza.
        StyleKit.statue(b, cx - 6, cz + 22, ground, p);
        StyleKit.statue(b, cx + 6, cz + 22, ground, p);
        // Lamp posts down the four approach axes.
        for (int d = 24; d <= district - 8; d += 16) {
            StyleKit.lanternPost(b, cx + d, cz + 3, ground, p);
            StyleKit.lanternPost(b, cx - d, cz - 3, ground, p);
            StyleKit.lanternPost(b, cx + 3, cz + d, ground, p);
            StyleKit.lanternPost(b, cx - 3, cz - d, ground, p);
        }
        // Neon shopfront signs along the streets.
        for (int d = 18; d <= district - 12; d += 22) {
            neonSign(b, rng, cx + d, cz - 4, ground, p);
            neonSign(b, rng, cx - d, cz + 4, ground, p);
        }
    }

    /** A slim advertising column: iron post topped with coloured glass and a glowing cap. */
    private static void neonSign(StructureBuilder b, RandomSource rng, int x, int z, int ground, Palette p) {
        DyeColor c = NEON[rng.nextInt(NEON.length)];
        b.put(x, ground + 1, z, p.rail());
        b.put(x, ground + 2, z, Materials.stainedGlass(c));
        b.put(x, ground + 3, z, Materials.stainedGlass(c));
        b.put(x, ground + 4, z, p.light());
    }
}
