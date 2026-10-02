package com.sanchous98.eldritchhorror.world.loc.style;

import com.sanchous98.eldritchhorror.world.loc.Palette;
import com.sanchous98.eldritchhorror.world.loc.StructureBuilder;
import com.sanchous98.eldritchhorror.world.loc.StructureBuilder.Doorway;
import com.sanchous98.eldritchhorror.world.loc.StructureBuilder.Side;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Blocks;

/**
 * London — cultural city style. Victorian: soot-dark red/brown brick with white stone trim, near-black
 * slate roofs, tall chimneys, iron railings, tall narrow sash windows and a gaslit fog mood.
 *
 * <p>Landmark: a domed cathedral with a square clock tower (St Paul's / Big Ben). Street props:
 * gaslit lamp posts and a few obelisks and statues on the plaza.
 */
public final class LondonStyle implements CityStyle {

    @Override
    public String id() {
        return "london";
    }

    @Override
    public Palette palette(int koppenClass, boolean coastal) {
        // Culture leads: brick + dark slate regardless of Köppen. Climate only varies the damp
        // overgrowth (moss/vines in the wet, kelp on the coast).
        Palette bio = Palette.fromBiome(koppenClass, coastal);
        return new Palette(
                Blocks.STONE_BRICKS.defaultBlockState(),            // ground: grey flagstone paving
                Blocks.COBBLESTONE.defaultBlockState(),             // foundation: granite setts
                Blocks.BRICKS.defaultBlockState(),                  // wall: soot-red London brick
                Materials.terracotta(DyeColor.BROWN),               // weathered: soot-brown brick
                Materials.whiteConcrete(),                          // accent: white stone trim / banding
                Blocks.DEEPSLATE_TILES.defaultBlockState(),         // roof: near-black slate
                Blocks.DEEPSLATE_TILE_STAIRS.defaultBlockState(),   // roof stairs
                Blocks.DEEPSLATE_TILE_SLAB.defaultBlockState(),     // roof slabs / eaves
                Blocks.GLASS_PANE.defaultBlockState(),              // window: narrow sash glazing
                Blocks.DARK_OAK_TRAPDOOR.defaultBlockState(),       // frame: timber mullions
                Blocks.DARK_OAK_DOOR.defaultBlockState(),           // door
                Blocks.IRON_BARS.defaultBlockState(),               // rail: iron railings
                Blocks.LANTERN.defaultBlockState(),                 // light: gas lamp
                bio.overgrowth(),
                Blocks.GRAVEL.defaultBlockState());                 // rubble
    }

    @Override
    public void landmark(StructureBuilder b, RandomSource rng, int cx, int cz, int ground, Palette p) {
        // ------------------------------------------------------------------ Elizabeth Tower (Big Ben)
        // A square 7x7 Gothic clock tower, ~70 blocks to the spire tip: a tall banded shaft, a bold
        // lit clock stage on all four faces, a crenellated belfry and a slender spire.
        int tr = 3;
        int shaftTop = ground + 58;
        b.ground(cx - tr - 1, cz - tr - 1, cx + tr + 1, cz + tr + 1, ground, ground, p.foundation());
        b.room(cx - tr, ground + 1, cz - tr, cx + tr, shaftTop, cz + tr, new Doorway(Side.S, tr));

        for (int y = ground + 1; y <= shaftTop; y++) {
            b.put(cx - tr, y, cz - tr, p.accent());
            b.put(cx + tr, y, cz - tr, p.accent());
            b.put(cx - tr, y, cz + tr, p.accent());
            b.put(cx + tr, y, cz + tr, p.accent());
            if (y % 4 == 0) {                       // banded stone string courses up the shaft
                b.fill(cx - tr, y, cz - tr, cx + tr, y, cz + tr, p.accent());
            }
        }
        for (int y = ground + 7; y <= shaftTop - 5; y += 5) {
            b.window(cx, y, cz - tr, 3, 1, true);
            b.window(cx, y, cz + tr, 3, 1, true);
            b.window(cx - tr, y, cz, 3, 1, true);
            b.window(cx + tr, y, cz, 3, 1, true);
        }

        // Bold clock stage: a lit dial ringed by a dark stone rim, one on each of the four faces.
        int dialY = shaftTop - 4;
        b.fill(cx - 1, dialY, cz - tr, cx + 1, dialY, cz - tr, p.accent());
        b.fill(cx - 1, dialY, cz + tr, cx + 1, dialY, cz + tr, p.accent());
        b.fill(cx - tr, dialY, cz - 1, cx - tr, dialY, cz + 1, p.accent());
        b.fill(cx + tr, dialY, cz - 1, cx + tr, dialY, cz + 1, p.accent());
        b.put(cx, dialY, cz - tr, p.light());
        b.put(cx, dialY, cz + tr, p.light());
        b.put(cx - tr, dialY, cz, p.light());
        b.put(cx + tr, dialY, cz, p.light());

        b.crenellations(cx - tr - 1, cz - tr - 1, cx + tr + 1, cz + tr + 1, shaftTop + 1);
        b.spire(cx, cz, shaftTop + 2, 14);

        // ------------------------------------------------------- Houses of Parliament (west stub)
        // A long buttressed Gothic river front running west from the tower, with a central
        // Victoria Tower breaking the ridge and a lower crenellated terrace at its foot.
        int px0 = cx - 42;
        int px1 = cx - 5;
        int pz0 = cz - 9;
        int pz1 = cz + 9;
        int py0 = ground + 1;
        int py1 = py0 + 19;                 // 20-block riverside facade

        b.ground(px0 - 1, pz0 - 1, px1, pz1 + 1, ground, ground, p.foundation());
        b.room(px0, py0, pz0, px1, py1, pz1);
        b.pitchedRoof(px0 - 1, pz0 - 1, px1, pz1 + 1, py1 + 1, 8, 1);   // ridge runs along Z

        // Buttresses and tall traceried windows in bays down both long faces.
        for (int x = px0 + 3; x <= px1 - 3; x += 5) {
            b.buttress(x, pz0, py0, 7, Side.N);
            b.buttress(x, pz1, py0, 7, Side.S);
            b.window(x, py0 + 5, pz0, 8, 1, true);
            b.window(x, py0 + 5, pz1, 8, 1, true);
        }
        // A lower crenellated river terrace in front of the wing.
        b.walls(px0, py0, pz0 - 3, px1, py0 + 2, pz0 - 3, p.foundation());
        b.crenellations(px0, pz0 - 3, px1, pz0 - 3, py0 + 3);

        // Central Victoria Tower breaking the ridge, capped with a spire.
        int vx0 = cx - 26;
        int vx1 = cx - 21;
        int vz0 = cz - 5;
        int vz1 = cz + 5;
        int vy1 = py0 + 34;
        b.room(vx0, py0, vz0, vx1, vy1, vz1);
        for (int y = py0; y <= vy1; y++) {
            b.put(vx0, y, vz0, (y & 1) == 0 ? p.accent() : p.wall());
            b.put(vx1, y, vz0, (y & 1) == 0 ? p.accent() : p.wall());
            b.put(vx0, y, vz1, (y & 1) == 0 ? p.accent() : p.wall());
            b.put(vx1, y, vz1, (y & 1) == 0 ? p.accent() : p.wall());
        }
        for (int y = py0 + 6; y <= vy1 - 6; y += 8) {
            b.window(vx0, y, cz, 5, 1, true);
            b.window(vx1, y, cz, 5, 1, true);
            b.window((vx0 + vx1) / 2, y, vz0, 5, 1, true);
            b.window((vx0 + vx1) / 2, y, vz1, 5, 1, true);
        }
        b.crenellations(vx0 - 1, vz0 - 1, vx1 + 1, vz1 + 1, vy1 + 1);
        b.spire((vx0 + vx1) / 2, cz, vy1 + 3, 18);
    }




    @Override
    public void flourish(StructureBuilder b, RandomSource rng, int x, int y0, int z,
                         int x1, int y1, int z1, Palette p) {
        // A tall brick chimney stack on a back corner.
        int cxp = x + rng.nextInt(Math.max(1, x1 - x));
        int czp = z + rng.nextInt(Math.max(1, z1 - z));
        int h = 3 + rng.nextInt(4);
        b.fill(cxp, y1 + 1, czp, Math.min(cxp + 1, x1), y1 + h, Math.min(czp + 1, z1), p.wall());
        b.fill(cxp, y1 + h + 1, czp, Math.min(cxp + 1, x1), y1 + h + 1, Math.min(czp + 1, z1), p.accent());

        // Iron railings along the front (min-Z) facade at ground level.
        for (int fx = x; fx <= x1; fx++) {
            if (fx != cxp) {
                b.put(fx, y0 + 1, z, p.rail());
            }
        }
    }

    @Override
    public void streetProps(StructureBuilder b, RandomSource rng, int cx, int cz,
                            int district, int ground, Palette p) {
        // Gaslit lamp posts down the two main axes.
        for (int d = 24; d <= district - 10; d += 12) {
            StyleKit.lanternPost(b, cx + d, cz + 3, ground, p);
            StyleKit.lanternPost(b, cx - d, cz - 3, ground, p);
            StyleKit.lanternPost(b, cx + 3, cz + d, ground, p);
            StyleKit.lanternPost(b, cx - 3, cz - d, ground, p);
        }
        // Plaza monuments: an obelisk and statues on the cross-axes.
        StyleKit.obelisk(b, cx + district / 2, cz, ground, p);
        StyleKit.statue(b, cx - district / 2, cz, ground, p);
        StyleKit.statue(b, cx, cz + district / 2, ground, p);
    }
}
