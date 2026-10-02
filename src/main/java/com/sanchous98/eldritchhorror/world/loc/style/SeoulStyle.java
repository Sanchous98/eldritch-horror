package com.sanchous98.eldritchhorror.world.loc.style;

import com.sanchous98.eldritchhorror.world.loc.Palette;
import com.sanchous98.eldritchhorror.world.loc.StructureBuilder;
import com.sanchous98.eldritchhorror.world.loc.StructureBuilder.Doorway;
import com.sanchous98.eldritchhorror.world.loc.StructureBuilder.Side;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Seoul — Korean cultural style. Grey granite hanok walls under dark timber posts and beams,
 * blue-green tiled roofs with broad upturned eaves, palace gates, and mountain temples rising
 * behind the modern towers.
 *
 * <p>Landmark: Gyeongbokgung — a large double-eaved throne hall with a dark tiled roof on a stone
 * terrace, flanked by two smaller pavilions and fronted by a tall decorative gate. Street props:
 * stone lanterns (seokdeung), guardian statues and timber gate posts along the approaches.
 *
 * <p>Deterministic and cheap: only {@link StructureBuilder} + {@link Palette} blocks (and
 * {@link StyleKit}); all randomness comes from the passed {@link RandomSource}.
 */
public final class SeoulStyle implements CityStyle {

    @Override
    public String id() {
        return "seoul";
    }

    @Override
    public Palette palette(int koppenClass, boolean coastal) {
        // Culture leads: grey hanok stone, dark timber, blue-green tiles, whatever the climate.
        // Climate only nudges the overgrowth (coast swaps it for kelp).
        Palette bio = Palette.fromBiome(koppenClass, coastal);
        return new Palette(
                Blocks.STONE_BRICKS.defaultBlockState(),            // ground: grey stone paving
                Blocks.POLISHED_ANDESITE.defaultBlockState(),       // foundation: hanok stone base
                Blocks.STONE_BRICKS.defaultBlockState(),            // wall: grey hanok stone wall
                Blocks.CRACKED_STONE_BRICKS.defaultBlockState(),    // weathered wall
                Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState(),   // accent: dark timber posts/beams
                Blocks.WARPED_PLANKS.defaultBlockState(),           // roof: blue-green tiled roof
                Blocks.WARPED_STAIRS.defaultBlockState(),           // roof stairs / upturned eaves
                Blocks.WARPED_SLAB.defaultBlockState(),             // roof slabs / eaves
                Blocks.GLASS_PANE.defaultBlockState(),              // window: papered lattice
                Blocks.DARK_OAK_TRAPDOOR.defaultBlockState(),       // frame: timber lattice
                Blocks.DARK_OAK_DOOR.defaultBlockState(),           // door: dark timber gate
                Blocks.DARK_OAK_FENCE.defaultBlockState(),          // rail
                Blocks.LANTERN.defaultBlockState(),                 // light: lantern glow
                bio.overgrowth(),
                Blocks.GRAVEL.defaultBlockState());                 // rubble
    }

    @Override
    public void landmark(StructureBuilder b, RandomSource rng, int cx, int cz, int ground, Palette p) {
        // Gyeongbokgung, raised to dominate the skyline: a broad, many-storeyed throne hall under
        // a dark double-eaved roof whose ridge crest reaches ~60 blocks, flanked by two low
        // double-eaved pavilions and fronted by a tall multi-storey gate tower (~65 blocks). The
        // stepped stone terraces and dark timber frames are the same vocabulary, just carried far
        // higher so the palace reads as a vertical silhouette over the city.
        BlockState air = Blocks.AIR.defaultBlockState();

        // Flanking pavilions (reused, low) anchor the base of the composition.
        pavilion(b, cx - 27, cz - 3, ground, p);
        pavilion(b, cx + 27, cz - 3, ground, p);

        // --- throne hall (Geunjeongjeon): a tall hall under two tiled eaves ---------------
        int hz = cz - 7;
        int w = 45;
        int d = 25;
        int hx0 = cx - w / 2;              // cx - 22
        int hx1 = hx0 + w - 1;             // cx + 22
        int hz0 = hz - d / 2;              // hz - 12
        int hz1 = hz0 + d - 1;             // hz + 12
        int y0 = ground + 3;               // the hall sits on a two-course stone terrace
        int h = 24;
        int top = y0 + h;                  // ground + 27

        // Stone terrace with a stepped south approach.
        b.ground(hx0 - 4, hz0 - 4, hx1 + 4, hz1 + 4, ground, ground + 2, p.foundation());
        for (int s = 0; s < 3; s++) {
            b.fill(hx0 + 8, ground, hz1 + 5 + s, hx1 - 8, ground + 2 - s, hz1 + 5 + s,
                    p.foundation());
        }

        // The tall hall body, doors front (south) and back (north).
        b.room(hx0, y0, hz0, hx1, top, hz1, new Doorway(Side.S, w / 2),
                new Doorway(Side.N, w / 2));

        // Dark timber posts marching along both long facades and across the short ends.
        for (int x = hx0; x <= hx1; x += 4) {
            b.fill(x, y0 + 1, hz0, x, top, hz0, p.accent());
            b.fill(x, y0 + 1, hz1, x, top, hz1, p.accent());
        }
        for (int z = hz0 + 1; z <= hz1 - 1; z += 4) {
            b.fill(hx0, y0 + 1, z, hx0, top, z, p.accent());
            b.fill(hx1, y0 + 1, z, hx1, top, z, p.accent());
        }
        // Two tiers of papered lattice door-bays between the posts.
        for (int x = hx0 + 2; x <= hx1 - 2; x += 4) {
            b.window(x, y0 + 3, hz0, 6, 1, true);
            b.window(x, y0 + 3, hz1, 6, 1, true);
            b.window(x, y0 + 15, hz0, 6, 1, true);
            b.window(x, y0 + 15, hz1, 6, 1, true);
        }
        b.fill(hx0, y0 + 10, hz0, hx1, y0 + 10, hz0, p.accent());
        b.fill(hx0, y0 + 10, hz1, hx1, y0 + 10, hz1, p.accent());
        // Re-open the doors through the timber skin.
        for (int yy = y0 + 1; yy <= y0 + 3; yy++) {
            b.put(cx, yy, hz0, air);
            b.put(cx, yy, hz1, air);
        }

        // Broad lower eave: blue-green tiled roof overhanging four blocks, ridge along X.
        b.pitchedRoof(hx0 - 4, hz0 - 4, hx1 + 4, hz1 + 4, top, 10, 0);

        // Timber clerestory standing on the lower eave — the storey between the two roofs.
        int ux0 = cx - 16;
        int ux1 = cx + 16;
        int uz0 = hz - 6;
        int uz1 = hz + 6;
        for (int y = top + 10; y <= top + 18; y++) {
            for (int x = ux0; x <= ux1; x++) {
                b.put(x, y, uz0, p.accent());
                b.put(x, y, uz1, p.accent());
            }
            for (int z = uz0 + 1; z <= uz1 - 1; z++) {
                b.put(ux0, y, z, p.accent());
                b.put(ux1, y, z, p.accent());
            }
        }
        for (int x = ux0 + 2; x <= ux1 - 2; x += 4) {
            b.window(x, top + 12, uz0, 4, 1, true);
            b.window(x, top + 12, uz1, 4, 1, true);
        }
        // Tall upper eave, capped by a raised ridge crest and a lantern (~60 blocks).
        b.pitchedRoof(ux0 - 4, uz0 - 3, ux1 + 4, uz1 + 3, top + 18, 12, 0);
        b.fill(cx - 3, top + 29, hz, cx + 3, top + 29, hz, p.roof());
        b.put(cx, top + 30, hz, p.accent());
        b.put(cx, top + 31, hz, p.accent());
        b.put(cx, top + 32, hz, p.accent());
        b.put(cx, top + 33, hz, p.light());

        // --- front gate tower: a tall multi-storey palace gate on the south approach ------
        int gz = cz + 24;
        int gw = 27;
        int gd = 11;
        int gx0 = cx - gw / 2;             // cx - 13
        int gx1 = gx0 + gw - 1;            // cx + 13
        int gz0 = gz - gd / 2;             // gz - 5
        int gz1 = gz0 + gd - 1;            // gz + 5
        int gy0 = ground + 1;
        int baseTop = gy0 + 28;            // ground + 29

        b.ground(gx0 - 3, gz0 - 3, gx1 + 3, gz1 + 3, ground, ground, p.foundation());
        b.room(gx0, gy0, gz0, gx1, baseTop, gz1, new Doorway(Side.S, gw / 2),
                new Doorway(Side.N, gw / 2));
        // Two more gate openings beside the central one.
        for (int dx : new int[]{-8, 8}) {
            for (int y = gy0 + 1; y <= gy0 + 4; y++) {
                b.put(cx + dx, y, gz0, air);
                b.put(cx + dx, y, gz1, air);
            }
        }
        // Dark timber posts, corner posts and horizontal bands up the tall stone base.
        for (int x = gx0; x <= gx1; x += 4) {
            b.fill(x, gy0 + 1, gz0, x, baseTop, gz0, p.accent());
            b.fill(x, gy0 + 1, gz1, x, baseTop, gz1, p.accent());
        }
        for (int y = gy0 + 1; y <= baseTop; y++) {
            b.put(gx0, y, gz0, p.accent());
            b.put(gx1, y, gz0, p.accent());
            b.put(gx0, y, gz1, p.accent());
            b.put(gx1, y, gz1, p.accent());
        }
        for (int y : new int[]{gy0 + 9, gy0 + 19}) {
            b.fill(gx0, y, gz0, gx1, y, gz0, p.accent());
            b.fill(gx0, y, gz1, gx1, y, gz1, p.accent());
        }
        // Stacked lattice windows up the gate.
        for (int y : new int[]{gy0 + 6, gy0 + 16, gy0 + 25}) {
            for (int x = cx - 10; x <= cx + 10; x += 5) {
                b.window(x, y, gz0, 3, 1, true);
                b.window(x, y, gz1, 3, 1, true);
            }
        }

        // Lower roof, then two narrowing timber galleries each under their own tiled eave.
        b.pitchedRoof(gx0 - 3, gz0 - 3, gx1 + 3, gz1 + 3, baseTop, 6, 0);
        int tx0 = cx - 11;
        int tx1 = cx + 11;
        int tz0 = gz - 3;
        int tz1 = gz + 3;
        for (int y = baseTop + 6; y <= baseTop + 14; y++) {
            for (int x = tx0; x <= tx1; x++) {
                b.put(x, y, tz0, p.accent());
                b.put(x, y, tz1, p.accent());
            }
            for (int z = tz0 + 1; z <= tz1 - 1; z++) {
                b.put(tx0, y, z, p.accent());
                b.put(tx1, y, z, p.accent());
            }
        }
        for (int x = tx0 + 2; x <= tx1 - 2; x += 6) {
            b.window(x, baseTop + 8, tz0, 4, 1, true);
            b.window(x, baseTop + 8, tz1, 4, 1, true);
        }
        b.pitchedRoof(tx0 - 3, tz0 - 2, tx1 + 3, tz1 + 2, baseTop + 14, 6, 0);

        int sx0 = cx - 7;
        int sx1 = cx + 7;
        int sz0 = gz - 2;
        int sz1 = gz + 2;
        for (int y = baseTop + 20; y <= baseTop + 27; y++) {
            for (int x = sx0; x <= sx1; x++) {
                b.put(x, y, sz0, p.accent());
                b.put(x, y, sz1, p.accent());
            }
            for (int z = sz0 + 1; z <= sz1 - 1; z++) {
                b.put(sx0, y, z, p.accent());
                b.put(sx1, y, z, p.accent());
            }
        }
        b.window(cx - 4, baseTop + 22, gz - 2, 3, 1, true);
        b.window(cx + 4, baseTop + 22, gz + 2, 3, 1, true);
        b.pitchedRoof(sx0 - 2, sz0 - 2, sx1 + 2, sz1 + 2, baseTop + 27, 5, 0);

        // Ridge crest and lantern at the very top (~65 blocks).
        b.fill(cx - 2, baseTop + 32, gz, cx + 2, baseTop + 32, gz, p.roof());
        b.put(cx, baseTop + 33, gz, p.accent());
        b.put(cx, baseTop + 34, gz, p.accent());
        b.put(cx, baseTop + 35, gz, p.accent());
        b.put(cx, baseTop + 36, gz, p.light());
    }


    /** A small double-eaved pavilion (a flanking hall) on its own stone terrace. */
    private static void pavilion(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int r = 7;
        int y0 = ground + 3;
        b.ground(cx - r - 2, cz - r - 2, cx + r + 2, cz + r + 2, ground, ground + 2, p.foundation());
        // A south entrance, consistent with the throne hall and gate.
        b.room(cx - r, y0, cz - r, cx + r, y0 + 6, cz + r, new Doorway(Side.S, r));
        // Dark timber corner and mid posts.
        for (int y = y0 + 1; y <= y0 + 6; y++) {
            b.put(cx - r, y, cz - r, p.accent());
            b.put(cx + r, y, cz - r, p.accent());
            b.put(cx - r, y, cz + r, p.accent());
            b.put(cx + r, y, cz + r, p.accent());
            b.put(cx, y, cz - r, p.accent());
            b.put(cx, y, cz + r, p.accent());
            b.put(cx - r, y, cz, p.accent());
            b.put(cx + r, y, cz, p.accent());
        }
        // Lower wide eave.
        b.pitchedRoof(cx - r - 3, cz - r - 3, cx + r + 3, cz + r + 3, y0 + 6, 4, 0);
        // Upper drum and its smaller roof.
        b.room(cx - 3, y0 + 8, cz - 3, cx + 3, y0 + 10, cz + 3);
        b.pitchedRoof(cx - 6, cz - 6, cx + 6, cz + 6, y0 + 10, 4, 0);
        b.put(cx, y0 + 14, cz, p.light());
    }


    @Override
    public void flourish(StructureBuilder b, RandomSource rng, int x, int y0, int z,
                         int x1, int y1, int z1, Palette p) {
        // Hanok post-and-beam band: dark timber all round just under the eaves.
        int beam = y1 - 1;
        if (beam <= y0) {
            return;
        }
        b.fill(x, beam, z, x1, beam, z, p.accent());
        b.fill(x, beam, z1, x1, beam, z1, p.accent());
        b.fill(x, beam, z, x, beam, z1, p.accent());
        b.fill(x1, beam, z, x1, beam, z1, p.accent());
    }

    @Override
    public void streetProps(StructureBuilder b, RandomSource rng, int cx, int cz,
                            int district, int ground, Palette p) {
        // Stone lanterns (seokdeung) line the two main axes.
        for (int d = 26; d <= district - 10; d += 16) {
            stoneLantern(b, cx + d, cz + 3, ground, p);
            stoneLantern(b, cx - d, cz - 3, ground, p);
            stoneLantern(b, cx + 3, cz + d, ground, p);
            stoneLantern(b, cx - 3, cz - d, ground, p);
        }
        // Guardian stone figures on the four approaches to the plaza.
        int s = Math.max(24, district - 22);
        StyleKit.statue(b, cx, cz - s, ground, p);
        StyleKit.statue(b, cx, cz + s, ground, p);
        StyleKit.statue(b, cx - s, cz, ground, p);
        StyleKit.statue(b, cx + s, cz, ground, p);
        // A pair of timber gate posts framing the north approach.
        gatePosts(b, cx, cz - s + 2, ground, p);
    }

    // ------------------------------------------------------------------ Korean forms



    /** A pair of dark timber gate posts joined by a tiled lintel, with a lantern beneath. */
    private static void gatePosts(StructureBuilder b, int cx, int z, int y0, Palette p) {
        for (int y = y0; y <= y0 + 4; y++) {
            b.put(cx - 2, y, z + 1, p.accent());
            b.put(cx + 2, y, z + 1, p.accent());
        }
        b.fill(cx - 2, y0 + 5, z + 1, cx + 2, y0 + 5, z + 1, p.roof());
        b.fill(cx - 3, y0 + 6, z + 1, cx + 3, y0 + 6, z + 1, p.roofSlab());
        b.put(cx, y0 + 3, z, p.light());
    }

    /** A stone lantern (seokdeung): plinth, shaft, glowing light box and a slab cap. */
    private static void stoneLantern(StructureBuilder b, int x, int z, int ground, Palette p) {
        b.put(x, ground + 1, z, p.foundation());
        b.put(x, ground + 2, z, p.wall());
        b.put(x, ground + 3, z, p.light());
        b.put(x, ground + 4, z, p.roofSlab());
    }

}
