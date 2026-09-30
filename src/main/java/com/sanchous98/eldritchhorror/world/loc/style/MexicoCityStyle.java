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
                coastal ? Blocks.KELP.defaultBlockState() : bio.overgrowth(),
                Blocks.GRAVEL.defaultBlockState());                 // rubble
    }

    @Override
    public void landmark(StructureBuilder b, RandomSource rng, int cx, int cz, int ground, Palette p) {
        int y0 = ground + 1;

        // 1. Long, low nave with a flat parapet — colonial, not gothic.
        int nx0 = cx - 6;
        int nx1 = cx + 6;
        int nz0 = cz - 16;
        int nz1 = cz + 8;
        b.ground(nx0 - 2, nz0 - 2, nx1 + 2, nz1 + 2, ground, ground, p.foundation());
        b.room(nx0, y0, nz0, nx1, y0 + 10, nz1, new Doorway(Side.N, 6));
        // Flat parapet roof with a one-block tiled skirt.
        b.fill(nx0 - 1, y0 + 11, nz0 - 1, nx1 + 1, y0 + 11, nz1 + 1, p.roof());
        // Round-arch windows down both flanks.
        for (int z = nz0 + 3; z <= nz1 - 3; z += 4) {
            b.window(nx0, y0 + 4, z, 3, 1, true);
            b.window(nx1, y0 + 4, z, 3, 1, true);
        }

        // 2. The dominant feature: a great tiled dome on a drum at the crossing.
        int drumTop = y0 + 13;
        b.room(cx - 4, drumTop, cz - 4, cx + 4, drumTop + 3, cz + 4,
                new Doorway(Side.N, 4), new Doorway(Side.S, 4),
                new Doorway(Side.E, 4), new Doorway(Side.W, 4));
        for (int y = drumTop; y <= drumTop + 3; y++) {
            b.put(cx - 4, y, cz - 4, p.accent());
            b.put(cx + 4, y, cz - 4, p.accent());
            b.put(cx - 4, y, cz + 4, p.accent());
            b.put(cx + 4, y, cz + 4, p.accent());
        }
        StyleKit.dome(b, cx, cz, drumTop + 4, 7, p);

        // 3. Two low square bell towers with tiled pyramidal caps.
        bellTower(b, cx - 5, nz0 - 3, ground, p);
        bellTower(b, cx + 5, nz0 - 3, ground, p);

        // 4. An Aztec stepped platform on the plaza edge — the cultural signature.
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
