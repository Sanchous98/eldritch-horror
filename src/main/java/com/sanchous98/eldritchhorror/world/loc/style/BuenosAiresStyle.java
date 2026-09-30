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
 * Buenos Aires — cultural city style. Mediterranean / porteno: pastel rendered plaster (rose, blue,
 * ochre) with white trim, orange terracotta tile roofs, wrought-iron balconies, grand avenues and
 * the colourful painted houses of La Boca.
 *
 * <p>Landmark: a grand beaux-arts palace/congress — a colonnaded portico under a high slate dome
 * (Congress, the Colón). Street props: iron lamp posts, statues and the Obelisk on the avenue.
 * Flourishes: wrought-iron balconies and colourful La Boca shopfronts. Deterministic and
 * cheap; uses only {@link StructureBuilder} + {@link Palette} (via {@link StyleKit}/{@link Materials}).
 */
public final class BuenosAiresStyle implements CityStyle {

    @Override
    public String id() {
        return "buenosaires";
    }

    @Override
    public Palette palette(int koppenClass, boolean coastal) {
        // Culture leads: pastel rendered plaster + terracotta tile, whatever the Köppen class.
        // Climate only varies the damp overgrowth (moss/vines, kelp on the coast).
        Palette bio = Palette.fromBiome(koppenClass, coastal);
        return new Palette(
                Blocks.SMOOTH_STONE.defaultBlockState(),            // ground: paved grand avenue
                Blocks.CUT_SANDSTONE.defaultBlockState(),           // foundation: rendered warm base course
                Materials.terracotta(DyeColor.PINK),                // wall: pastel rose plaster render
                Materials.terracotta(DyeColor.WHITE),               // weathered: sun-faded plaster
                Materials.whiteConcrete(),                          // accent: white trim / quoins
                Materials.terracotta(DyeColor.ORANGE),              // roof: orange terracotta tile
                Blocks.BRICK_STAIRS.defaultBlockState(),            // roof stairs
                Blocks.BRICK_SLAB.defaultBlockState(),              // roof slabs / eaves
                Blocks.GLASS_PANE.defaultBlockState(),              // window: tall French glazing
                Blocks.BIRCH_TRAPDOOR.defaultBlockState(),          // frame: pale timber mullions
                Blocks.DARK_OAK_DOOR.defaultBlockState(),           // door: dark porte-cochère
                Blocks.IRON_BARS.defaultBlockState(),               // rail: wrought-iron balcony
                Blocks.LANTERN.defaultBlockState(),                 // light: gas/electric lamp
                coastal ? Blocks.KELP.defaultBlockState() : bio.overgrowth(),
                Blocks.GRAVEL.defaultBlockState());                 // rubble
    }

    @Override
    public void landmark(StructureBuilder b, RandomSource rng, int cx, int cz, int ground, Palette p) {
        palace(b, cx, cz, ground, p);
    }

    /**
     * A beaux-arts congress palace: a long balustraded block with a projecting colonnaded portico
     * on the north front and a high slate dome over the crossing.
     */
    private static void palace(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int w = 19;
        int d = 13;
        int h = 12;
        int x0 = cx - w / 2;
        int x1 = x0 + w - 1;
        int z0 = cz - d / 2;
        int z1 = z0 + d - 1;
        int y0 = ground + 1;
        int y1 = y0 + h;

        b.ground(x0 - 3, z0 - 3, x1 + 3, z1 + 2, ground, ground, p.foundation());
        b.room(x0, y0, z0, x1, y1, z1, new Doorway(Side.S, 9));

        // String course and crowning cornice in pale stone.
        b.fill(x0 - 1, y0 + 6, z0 - 1, x1 + 1, y0 + 6, z1 + 1, p.accent());
        b.fill(x0 - 1, y1, z0 - 1, x1 + 1, y1, z1 + 1, p.accent());

        // Tall windows in two tiers down both flanks; a wide row across the south front.
        for (int z = z0 + 3; z <= z1 - 3; z += 3) {
            b.window(x0, y0 + 2, z, 3, 1, true);
            b.window(x1, y0 + 2, z, 3, 1, true);
            b.window(x0, y0 + 8, z, 2, 1, true);
            b.window(x1, y0 + 8, z, 2, 1, true);
        }
        for (int x = x0 + 4; x <= x1 - 4; x += 4) {
            b.window(x, y0 + 2, z1, 3, 1, true);
        }

        // Projecting portico: a free-standing colonnade across the north front.
        colonnade(b, x0 + 3, x1 - 3, z0 - 2, ground, p, 5);

        // High dome on a lit drum, capped with a spire (the Congress lantern).
        StyleKit.dome(b, cx, cz, y1 + 2, 6, p);
        b.spire(cx, cz, y1 + 9, 8);

        StyleKit.twoHighDoor(b, p, cx, z1, y0 + 1, Direction.SOUTH);
    }

    /** A run of paired columns and an entablature along X at fixed Z. Small and cheap. */
    private static void colonnade(StructureBuilder b, int x0, int x1, int z, int ground, Palette p, int h) {
        int y = ground + 1;
        for (int x = x0; x <= x1; x += 3) {
            b.fill(x, y, z, x, y + h, z, p.accent());
            b.put(x, y + h + 1, z, p.accent()); // capital
        }
        b.fill(x0 - 1, y + h + 2, z, x1 + 1, y + h + 2, z, p.accent()); // entablature
    }

    @Override
    public void flourish(StructureBuilder b, RandomSource rng, int x, int y0, int z,
                         int x1, int y1, int z1, Palette p) {
        // Colourful La Boca painted shopfront on a ground face.
        if (rng.nextFloat() < 0.5f) {
            shopfront(b, rng.nextInt(4), x, y0, z, x1, z1, pastel(rng), p);
        }
        // Wrought-iron balcony on an upper face.
        if (rng.nextFloat() < 0.6f) {
            balcony(b, rng.nextInt(4), y1 - 2, x, z, x1, z1, p);
        }
    }

    @Override
    public void streetProps(StructureBuilder b, RandomSource rng, int cx, int cz,
                            int district, int ground, Palette p) {
        // Iron lamp posts down both grand avenues.
        for (int d = 26; d <= district - 8; d += 12) {
            StyleKit.lanternPost(b, cx + d, cz + 3, ground, p);
            StyleKit.lanternPost(b, cx - d, cz - 3, ground, p);
            StyleKit.lanternPost(b, cx + 3, cz + d, ground, p);
            StyleKit.lanternPost(b, cx - 3, cz - d, ground, p);
        }
        // The Obelisk on the avenue, clear of the palace front.
        StyleKit.obelisk(b, cx, cz + district - 30, ground, p);
        // Statues flanking the avenue approaches.
        StyleKit.statue(b, cx - district / 2, cz, ground, p);
        StyleKit.statue(b, cx + district / 2, cz, ground, p);
    }

    // ------------------------------------------------------------------ helpers

    /** A painted panel and a projecting awning band on one ground-level face. */
    private static void shopfront(StructureBuilder b, int side, int x, int y0, int z,
                                  int x1, int z1, BlockState paint, Palette p) {
        switch (side) {
            case 0 -> {
                b.fill(x + 1, y0 + 1, z, x1 - 1, y0 + 2, z, paint);
                b.fill(x, y0 + 3, z, x1, y0 + 3, z, p.rail());
            }
            case 1 -> {
                b.fill(x + 1, y0 + 1, z1, x1 - 1, y0 + 2, z1, paint);
                b.fill(x, y0 + 3, z1, x1, y0 + 3, z1, p.rail());
            }
            case 2 -> {
                b.fill(x, y0 + 1, z + 1, x, y0 + 2, z1 - 1, paint);
                b.fill(x, y0 + 3, z, x, y0 + 3, z1, p.rail());
            }
            default -> {
                b.fill(x1, y0 + 1, z + 1, x1, y0 + 2, z1 - 1, paint);
                b.fill(x1, y0 + 3, z, x1, y0 + 3, z1, p.rail());
            }
        }
    }

    /** A wrought-iron railing band set into one upper face. */
    private static void balcony(StructureBuilder b, int side, int y, int x, int z, int x1, int z1, Palette p) {
        switch (side) {
            case 0 -> b.fill(x + 1, y, z, x1 - 1, y, z, p.rail());
            case 1 -> b.fill(x + 1, y, z1, x1 - 1, y, z1, p.rail());
            case 2 -> b.fill(x, y, z + 1, x, y, z1 - 1, p.rail());
            default -> b.fill(x1, y, z + 1, x1, y, z1 - 1, p.rail());
        }
    }

    /** A bright pastel paint block for a La Boca facade (colour-safe via {@link Materials}). */
    private static BlockState pastel(RandomSource rng) {
        return switch (rng.nextInt(6)) {
            case 0 -> Materials.terracotta(DyeColor.PINK);
            case 1 -> Materials.glazed(DyeColor.LIGHT_BLUE);   // azulejo blue
            case 2 -> Materials.concrete(DyeColor.YELLOW);
            case 3 -> Materials.terracotta(DyeColor.ORANGE);   // terracotta render
            case 4 -> Materials.concrete(DyeColor.WHITE);
            default -> Materials.terracotta(DyeColor.LIGHT_BLUE);
        };
    }
}
