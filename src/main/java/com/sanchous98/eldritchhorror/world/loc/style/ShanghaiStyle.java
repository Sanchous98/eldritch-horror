package com.sanchous98.eldritchhorror.world.loc.style;

import com.sanchous98.eldritchhorror.world.loc.Palette;
import com.sanchous98.eldritchhorror.world.loc.StructureBuilder;
import com.sanchous98.eldritchhorror.world.loc.StructureBuilder.Doorway;
import com.sanchous98.eldritchhorror.world.loc.StructureBuilder.Side;
import net.minecraft.core.Direction;
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
        BlockState overgrowth = coastal ? Blocks.KELP.defaultBlockState() : bio.overgrowth();
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
        int y0 = ground + 1;
        // Colonnaded stone base.
        b.ground(cx - 8, cz - 8, cx + 8, cz + 8, ground, ground, p.foundation());
        b.room(cx - 7, y0, cz - 7, cx + 7, y0 + 9, cz + 7, new Doorway(Side.S, 7));
        colonnade(b, cx, cz, y0, p);
        for (int dz = -4; dz <= 4; dz += 8) {
            b.window(cx - 7, y0 + 3, cz + dz, 5, 1, true);
            b.window(cx + 7, y0 + 3, cz + dz, 5, 1, true);
            b.window(cx + dz, y0 + 3, cz - 7, 5, 1, true);
            b.window(cx + dz, y0 + 3, cz + 7, 5, 1, true);
        }
        StyleKit.twoHighDoor(b, p, cx, cz + 7, y0 + 1, Direction.SOUTH);

        // Stepped setbacks — the Art-Deco crown.
        int y = y0 + 10;
        for (int i = 0; i < 3; i++) {
            int s = 5 - i * 2;                     // 5, 3, 1
            b.room(cx - s, y, cz - s, cx + s, y + 5, cz + s);
            b.crenellations(cx - s - 1, cz - s - 1, cx + s + 1, cz + s + 1, y + 6);
            y += 7;
        }
        b.spire(cx, cz, y, 16);
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
