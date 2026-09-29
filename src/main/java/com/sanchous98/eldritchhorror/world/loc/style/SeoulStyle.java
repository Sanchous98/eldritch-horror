package com.sanchous98.eldritchhorror.world.loc.style;

import com.sanchous98.eldritchhorror.world.loc.Palette;
import com.sanchous98.eldritchhorror.world.loc.StructureBuilder;
import com.sanchous98.eldritchhorror.world.loc.StructureBuilder.Doorway;
import com.sanchous98.eldritchhorror.world.loc.StructureBuilder.Side;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;

/**
 * Seoul — Korean cultural style. Grey granite hanok walls under dark timber posts and beams,
 * blue-green tiled roofs with broad upturned eaves, palace gates, and mountain temples rising
 * behind the modern towers.
 *
 * <p>Landmark: a wide palace gate-hall ({@code room} + sweeping {@code pitchedRoof}) in dark
 * timber, flanked by a pagoda tower recoloured to Korean blue-green tiles. Street props: stone
 * lanterns (seokdeung), guardian statues and timber gate posts along the approaches.
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
        // The dominant palace gate-hall, facing the city to the south.
        palaceHall(b, cx, cz + 6, ground, p);
        // A pagoda temple tower rising behind it, recoloured to dark timber + blue-green tiles.
        StyleKit.pagoda(b, cx, cz - 22, ground + 2, 5, koreanRoof(p));
        // Modern towers on the shoulders, reading as the city against the mountains.
        modernTower(b, cx - 18, cz - 16, ground, 18);
        modernTower(b, cx + 18, cz - 16, ground, 22);
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
