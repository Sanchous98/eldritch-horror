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
                coastal ? Blocks.KELP.defaultBlockState() : bio.overgrowth(),
                Blocks.GRAVEL.defaultBlockState());                 // rubble
    }

    @Override
    public void landmark(StructureBuilder b, RandomSource rng, int cx, int cz, int ground, Palette p) {
        // Gyeongbokgung: the great throne hall behind, flanked by two smaller double-eaved
        // pavilions, fronted by a tall decorative gate. Dark tiled roofs on stone terraces.
        gyeongbokHall(b, cx, cz - 7, ground, p);
        pavilion(b, cx - 27, cz - 3, ground, p);
        pavilion(b, cx + 27, cz - 3, ground, p);
        seoulGate(b, cx, cz + 24, ground, p);
    }

    /**
     * The throne hall (Geunjeongjeon): a wide grey-granite hall framed by dark timber posts under a
     * broad double-eaved blue-green tiled roof, raised on a two-course stone terrace. Broad and low
     * (about 53 wide, 32 tall) with heavy overhanging eaves, like its Gyeongbokgung model.
     */
    private static void gyeongbokHall(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int w = 45;
        int d = 25;
        int h = 12;
        int x0 = cx - w / 2;          // cx - 22
        int x1 = x0 + w - 1;          // cx + 22
        int z0 = cz - d / 2;          // cz - 12
        int z1 = z0 + d - 1;          // cz + 12
        int y0 = ground + 3;
        BlockState air = Blocks.AIR.defaultBlockState();

        // Stone terrace with a stepped south approach.
        b.ground(x0 - 4, z0 - 4, x1 + 4, z1 + 4, ground, ground + 2, p.foundation());
        for (int s = 0; s < 3; s++) {
            b.fill(x0 + 8, ground, z1 + 5 + s, x1 - 8, ground + 2 - s, z1 + 5 + s, p.foundation());
        }

        // Hall body with doors front (south) and back (north).
        b.room(x0, y0, z0, x1, y0 + h, z1, new Doorway(Side.S, w / 2), new Doorway(Side.N, w / 2));

        // Dark timber posts marching along both long facades and across the short ends.
        for (int x = x0; x <= x1; x += 4) {
            b.fill(x, y0 + 1, z0, x, y0 + h, z0, p.accent());
            b.fill(x, y0 + 1, z1, x, y0 + h, z1, p.accent());
        }
        for (int z = z0 + 1; z <= z1 - 1; z += 4) {
            b.fill(x0, y0 + 1, z, x0, y0 + h, z, p.accent());
            b.fill(x1, y0 + 1, z, x1, y0 + h, z, p.accent());
        }
        // Papered lattice door-bays between the posts.
        for (int x = x0 + 2; x <= x1 - 2; x += 4) {
            b.window(x, y0 + 3, z0, 6, 1, true);
            b.window(x, y0 + 3, z1, 6, 1, true);
        }
        // Re-open the doors through the timber skin.
        b.put(cx, y0 + 1, z0, air);
        b.put(cx, y0 + 2, z0, air);
        b.put(cx, y0 + 1, z1, air);
        b.put(cx, y0 + 2, z1, air);

        // Broad lower eave: blue-green tiled roof overhanging four blocks, ridge along X.
        b.pitchedRoof(x0 - 4, z0 - 4, x1 + 4, z1 + 4, y0 + h, 7, 0);

        // Timber clerestory standing on the lower eave — the storey between the two roofs.
        int ux0 = cx - 14;
        int ux1 = cx + 14;
        int uz0 = cz - 5;
        int uz1 = cz + 5;
        for (int y = y0 + h + 8; y <= y0 + h + 9; y++) {
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
            b.window(x, y0 + h + 8, uz0, 2, 1, true);
            b.window(x, y0 + h + 8, uz1, 2, 1, true);
        }
        // Upper eave: a narrower tiled roof with a ridge crest and a lantern.
        b.pitchedRoof(ux0 - 3, uz0 - 3, ux1 + 3, uz1 + 3, y0 + h + 10, 5, 0);
        b.fill(cx - 3, y0 + h + 16, cz, cx + 3, y0 + h + 16, cz, p.roof());
        b.put(cx, y0 + h + 17, cz, p.light());
    }

    /** A small double-eaved pavilion (a flanking hall) on its own stone terrace. */
    private static void pavilion(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int r = 7;
        int y0 = ground + 3;
        b.ground(cx - r - 2, cz - r - 2, cx + r + 2, cz + r + 2, ground, ground + 2, p.foundation());
        b.room(cx - r, y0, cz - r, cx + r, y0 + 6, cz + r);
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

    /** The tall decorative palace gate: a stone base with three archways under a double roof. */
    private static void seoulGate(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int w = 27;
        int d = 11;
        int h = 11;
        int x0 = cx - w / 2;
        int x1 = x0 + w - 1;
        int z0 = cz - d / 2;
        int z1 = z0 + d - 1;
        int y0 = ground + 1;
        BlockState air = Blocks.AIR.defaultBlockState();

        b.ground(x0 - 3, z0 - 3, x1 + 3, z1 + 3, ground, ground, p.foundation());
        b.room(x0, y0, z0, x1, y0 + h, z1, new Doorway(Side.S, w / 2), new Doorway(Side.N, w / 2));
        // Two more gate openings beside the central one.
        for (int dx : new int[]{-8, 8}) {
            for (int y = y0 + 1; y <= y0 + 3; y++) {
                b.put(cx + dx, y, z0, air);
                b.put(cx + dx, y, z1, air);
            }
        }
        // Dark timber posts up the base, carried past the first eave.
        for (int x = x0; x <= x1; x += 4) {
            b.fill(x, y0 + 1, z0, x, y0 + h, z0, p.accent());
            b.fill(x, y0 + 1, z1, x, y0 + h, z1, p.accent());
        }
        // Lower roof, then the upper gallery and its roof.
        b.pitchedRoof(x0 - 3, z0 - 3, x1 + 3, z1 + 3, y0 + h, 5, 0);
        int ux0 = cx - 7;
        int ux1 = cx + 7;
        int uz0 = cz - 3;
        int uz1 = cz + 3;
        for (int y = y0 + h + 2; y <= y0 + h + 5; y++) {
            for (int x = ux0; x <= ux1; x++) {
                b.put(x, y, uz0, p.accent());
                b.put(x, y, uz1, p.accent());
            }
            for (int z = uz0 + 1; z <= uz1 - 1; z++) {
                b.put(ux0, y, z, p.accent());
                b.put(ux1, y, z, p.accent());
            }
        }
        b.pitchedRoof(ux0 - 3, uz0 - 3, ux1 + 3, uz1 + 3, y0 + h + 5, 4, 0);
        b.put(cx, y0 + h + 11, cz, p.light());
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

    /**
     * A palette override for {@link StyleKit#pagoda}: dark timber frames and the blue-green
     * tiled roof of Korean palaces, instead of the vermilion of the Japanese reference.
     */
    private static Palette koreanRoof(Palette p) {
        return new Palette(
                p.ground(), p.foundation(), p.wall(), p.weathered(),
                Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState(),   // frames: dark timber
                Blocks.WARPED_PLANKS.defaultBlockState(),           // roof: blue-green tile
                Blocks.WARPED_STAIRS.defaultBlockState(),
                Blocks.WARPED_SLAB.defaultBlockState(),
                p.window(), p.frame(), p.door(), p.rail(), p.light(),
                p.overgrowth(), p.rubble());
    }

    /**
     * A wide palace gate-hall on a stone terrace: grey granite walls framed by dark timber posts,
     * a broad sweeping tiled roof overhanging on every side, and a timber gate with a lantern
     * above the doorway.
     */
    private static void palaceHall(StructureBuilder b, int cx, int cz, int ground, Palette p) {
        int w = 21;
        int d = 11;
        int h = 8;
        int x0 = cx - w / 2;
        int x1 = x0 + w - 1;
        int z0 = cz - d / 2;
        int z1 = z0 + d - 1;
        int y0 = ground + 3;   // the hall sits on a two-course stone terrace
        int y1 = y0 + h;

        b.ground(x0 - 3, z0 - 3, x1 + 3, z1 + 3, ground, ground, p.foundation());
        // Stone terrace (wolseong), with the hall standing on it.
        b.fill(x0 - 2, ground + 1, z0 - 2, x1 + 2, ground + 2, z1 + 2, p.foundation());
        b.room(x0, y0, z0, x1, y1, z1, new Doorway(Side.S, w / 2));
        // Dark timber posts marching along the long facades.
        for (int x = x0; x <= x1; x += 4) {
            for (int y = y0; y <= y1; y++) {
                b.put(x, y, z0, p.accent());
                b.put(x, y, z1, p.accent());
            }
        }
        // Corner posts on the short ends.
        for (int y = y0; y <= y1; y++) {
            b.put(x0, y, cz, p.accent());
            b.put(x1, y, cz, p.accent());
        }
        // The sweeping tiled roof: broad eaves overhanging two blocks on every side.
        b.pitchedRoof(x0 - 2, z0 - 2, x1 + 2, z1 + 2, y1, 6, 0);
        // The timber gate frame against the south face, with a lantern over the doorway.
        gatePosts(b, cx, z1, y0, p);
    }

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

    /** A slim modern tower of grey stone with dark window bands — the city behind the palace. */
    private static void modernTower(StructureBuilder b, int x, int z, int ground, int height) {
        int r = 2;
        b.room(x - r, ground + 1, z - r, x + r, ground + 1 + height, z + r);
        for (int y = ground + 4; y <= ground + height - 1; y += 4) {
            b.window(x - r, y, z, 2, 1, true);
            b.window(x + r, y, z, 2, 1, true);
            b.window(x, y, z - r, 2, 1, true);
            b.window(x, y, z + r, 2, 1, true);
        }
    }
}
