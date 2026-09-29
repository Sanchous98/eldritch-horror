package com.sanchous98.eldritchhorror.world.loc.style;

import com.sanchous98.eldritchhorror.world.loc.Palette;
import com.sanchous98.eldritchhorror.world.loc.StructureBuilder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Mexico City — Spanish-colonial + Aztec. Warm cream plaster over stone bases, red clay-tile and
 * flat-parapet roofs, wrought-iron balconies and lanterns, courtyards, and bright blue talavera
 * tile; a colonial cathedral with twin towers and a dome dominates the plaza (the Aztec
 * {@link StyleKit#steppedTemple} is the alternate silhouette).
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
        // Colonial cathedral: the shared nave, then a matching twin tower and a domed crossing.
        StyleKit.cathedral(b, rng, cx, cz, ground, p);
        int y0 = ground + 1;
        int towerTop = y0 + 26;
        twinTower(b, p, cx, cz + 11, cz + 16, y0, towerTop);
        StyleKit.dome(b, cx, cz, y0 + 16, 5, p);
    }

    /** The second of the cathedral's twin bell towers (mirrors the shared north tower). */
    private static void twinTower(StructureBuilder b, Palette p, int cx, int tz0, int tz1,
                                  int y0, int towerTop) {
        int tx0 = cx - 4;
        int tx1 = cx + 4;
        b.room(tx0, y0, tz0, tx1, towerTop, tz1);
        for (int y = y0; y <= towerTop; y++) {
            b.put(tx0, y, tz0, p.accent());
            b.put(tx1, y, tz0, p.accent());
            b.put(tx0, y, tz1, p.accent());
            b.put(tx1, y, tz1, p.accent());
        }
        for (int y = y0 + 5; y <= towerTop - 4; y += 5) {
            b.window(tx0, y, (tz0 + tz1) / 2, 3, 1, true);
            b.window(tx1, y, (tz0 + tz1) / 2, 3, 1, true);
            b.window((tx0 + tx1) / 2, y, tz1, 3, 1, true);
        }
        b.crenellations(tx0 - 1, tz0 - 1, tx1 + 1, tz1 + 1, towerTop + 1);
        b.spire(cx, (tz0 + tz1) / 2, towerTop + 3, 20);
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
