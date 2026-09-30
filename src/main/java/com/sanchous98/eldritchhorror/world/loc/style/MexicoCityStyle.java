package com.sanchous98.eldritchhorror.world.loc.style;

import com.sanchous98.eldritchhorror.world.loc.Palette;
import com.sanchous98.eldritchhorror.world.loc.StructureBuilder;
import com.sanchous98.eldritchhorror.world.loc.StructureBuilder.Doorway;
import com.sanchous98.eldritchhorror.world.loc.StructureBuilder.Side;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Mexico City — Spanish-colonial + Aztec. Warm cream plaster over cut-stone bases, red clay-tile
 * and flat-parapet roofs, wrought-iron balconies and lanterns, courtyards and bright blue talavera
 * tile.
 *
 * <p>Landmark: a <b>colonial cathedral dominated by a great tiled dome</b> — a long, low nave with
 * a flat parapet (no gothic verticality), a high domed crossing, and two <b>low square bell towers
 * with tiled pyramidal caps</b> (bell openings, no spire, no gothic crenellation). An <b>Aztec
 * stepped platform</b> sits on the plaza edge as the cultural signature. This deliberately does not
 * share Paris's flying-buttress / crenellated-twin gothic silhouette.
 *
 * <p>Street props: arcaded colonnades, wrought-iron lantern posts and statues. All deterministic
 * (only {@link StructureBuilder#rng()}) and built from {@link Palette} / {@link Materials} blocks.
 */
public final class MexicoCityStyle implements CityStyle {

    @Override
    public String id() {
        return "mexicocity";
    }

    @Override
    public Palette palette(int koppenClass, boolean coastal) {
        // Culture leads: warm plaster + terracotta tile + wrought iron, whatever the climate.
        // Climate only changes the overgrowth (coast swaps it for kelp).
        Palette bio = Palette.fromBiome(koppenClass, coastal);
        return new Palette(
                Blocks.STONE_BRICKS.defaultBlockState(),            // ground: cobbled street / plaza
                Blocks.POLISHED_GRANITE.defaultBlockState(),        // foundation: cut stone base course
                Materials.terracotta(DyeColor.WHITE),               // wall: warm cream plaster
                Materials.concrete(DyeColor.LIGHT_GRAY),            // weathered plaster (stained patch)
                Materials.terracotta(DyeColor.ORANGE),              // accent: terracotta trim / quoins
                Materials.terracotta(DyeColor.RED),                 // roof: red clay tile
                Blocks.BRICK_STAIRS.defaultBlockState(),            // roof stairs (tile-coloured)
                Blocks.BRICK_SLAB.defaultBlockState(),              // roof slabs / eaves
                Blocks.GLASS_PANE.defaultBlockState(),              // window
                Blocks.IRON_BARS.defaultBlockState(),               // frame: wrought-iron mullions
                Blocks.DARK_OAK_DOOR.defaultBlockState(),           // door: heavy timber
                Blocks.IRON_BARS.defaultBlockState(),               // rail: wrought-iron balcony
                Blocks.LANTERN.defaultBlockState(),                 // light: hanging lantern
                bio.overgrowth(),
                Blocks.GRAVEL.defaultBlockState());                 // rubble
    }

    @Override
    public void landmark(StructureBuilder b, RandomSource rng, int cx, int cz, int ground, Palette p) {
        // La Independencia: a tall golden fluted column on a stepped stone base, crowned by a
        // bright gilded winged Victory. The gilded figure is the unmistakable silhouette.
        int y0 = ground + 1;

        // 1. Stepped stone base (four shrinking tiers) on a paved apron.
        b.ground(cx - 7, cz - 7, cx + 7, cz + 7, ground, ground, p.ground());
        b.fill(cx - 5, y0, cz - 5, cx + 5, y0, cz + 5, p.foundation());
        b.fill(cx - 4, y0 + 1, cz - 4, cx + 4, y0 + 1, cz + 4, p.foundation());
        b.fill(cx - 3, y0 + 2, cz - 3, cx + 3, y0 + 2, cz + 3, p.foundation());
        b.fill(cx - 2, y0 + 3, cz - 2, cx + 2, y0 + 3, cz + 2, p.accent());

        // 2. Solid pedestal drum and its cornice.
        b.fill(cx - 1, y0 + 4, cz - 1, cx + 1, y0 + 7, cz + 1, p.wall());
        b.fill(cx - 2, y0 + 8, cz - 2, cx + 2, y0 + 8, cz + 2, p.accent());

        // 3. The tall fluted golden column: gilded 3x3 shaft with corner ribs for the flutes.
        BlockState gold = Materials.glazed(DyeColor.YELLOW);
        BlockState goldBright = Materials.glazed(DyeColor.WHITE);
        int colBase = y0 + 9;
        int colTop = y0 + 58;
        for (int y = colBase; y <= colTop; y++) {
            b.fill(cx - 1, y, cz - 1, cx + 1, y, cz + 1, gold);
            b.put(cx - 1, y, cz - 1, goldBright);
            b.put(cx + 1, y, cz - 1, goldBright);
            b.put(cx - 1, y, cz + 1, goldBright);
            b.put(cx + 1, y, cz + 1, goldBright);
        }

        // 4. Capital, then the golden winged Victory.
        b.fill(cx - 2, colTop + 1, cz - 2, cx + 2, colTop + 1, cz + 2, p.accent());
        int fig = colTop + 2;                 // body base
        b.fill(cx, fig, cz, cx, fig + 3, cz, goldBright);   // body/robe
        b.fill(cx - 2, fig + 1, cz, cx + 2, fig + 1, cz, goldBright); // outstretched wings
        b.put(cx, fig + 4, cz, goldBright);   // head

        // 5. The Aztec stepped platform, kept as a nearby secondary accent.
        StyleKit.steppedTemple(b, cx - 24, cz + 22, ground, 4, p);
    }

    /** A low square bell tower: plastered shaft, a high dark bell opening, a tiled pyramid cap. */
    private static void bellTower(StructureBuilder b, int x, int z, int ground, Palette p) {
        int y0 = ground + 1;
        int top = y0 + 16;
        b.room(x - 2, y0, z - 2, x + 2, top, z + 2);
        for (int y = y0; y <= top; y++) {
            b.put(x - 2, y, z - 2, p.accent());
            b.put(x + 2, y, z - 2, p.accent());
            b.put(x - 2, y, z + 2, p.accent());
            b.put(x + 2, y, z + 2, p.accent());
        }
        // Bell opening (a dark arch) high up, on two faces.
        b.window(x, top - 5, z - 2, 4, 1, true);
        b.window(x, top - 5, z + 2, 4, 1, true);
        // Tiled pyramidal cap.
        b.pitchedRoof(x - 2, z - 2, x + 2, z + 2, top, 3, 0);
    }

    @Override
    public void flourish(StructureBuilder b, RandomSource rng, int x, int y0, int z,
                         int x1, int y1, int z1, Palette p) {
        if (y1 - y0 < 4) {
            return;
        }
        // Talavera tile: a bright glazed panel beside each front corner.
        BlockState tile = Materials.glazed(DyeColor.LIGHT_BLUE);
        b.put(x, y0 + 1, z, tile);
        b.put(x1, y0 + 1, z1, tile);
        // A wrought-iron balcony overhanging one face.
        switch (rng.nextInt(4)) {
            case 0 -> b.fill(x + 1, y0 + 1, z - 1, x1 - 1, y0 + 1, z - 1, p.rail());
            case 1 -> b.fill(x + 1, y0 + 1, z1 + 1, x1 - 1, y0 + 1, z1 + 1, p.rail());
            case 2 -> b.fill(x - 1, y0 + 1, z + 1, x - 1, y0 + 1, z1 - 1, p.rail());
            default -> b.fill(x1 + 1, y0 + 1, z + 1, x1 + 1, y0 + 1, z1 - 1, p.rail());
        }
    }

    @Override
    public void streetProps(StructureBuilder b, RandomSource rng, int cx, int cz,
                            int district, int ground, Palette p) {
        // Wrought-iron lantern posts down the four approaches.
        for (int d = 26; d <= district - 12; d += 16) {
            StyleKit.lanternPost(b, cx + d, cz + 3, ground, p);
            StyleKit.lanternPost(b, cx - d, cz - 3, ground, p);
            StyleKit.lanternPost(b, cx + 3, cz + d, ground, p);
            StyleKit.lanternPost(b, cx - 3, cz - d, ground, p);
        }
        // Statues at the plaza corners, off the landmark axis.
        StyleKit.statue(b, cx + 14, cz - 14, ground, p);
        StyleKit.statue(b, cx - 14, cz + 14, ground, p);
        // Arcades framing the plaza to the east and west.
        arcade(b, cx + 16, cz - 12, cz + 12, ground, p);
        arcade(b, cx - 16, cz - 12, cz + 12, ground, p);
    }

    /** A low colonnade: terracotta piers under a continuous entablature, along the Z axis. */
    private static void arcade(StructureBuilder b, int x, int z0, int z1, int ground, Palette p) {
        for (int z = z0; z <= z1; z += 3) {
            for (int y = ground + 1; y <= ground + 3; y++) {
                b.put(x, y, z, p.accent());
            }
        }
        b.fill(x, ground + 4, z0, x, ground + 4, z1, p.roofSlab());
    }
}
